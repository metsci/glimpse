package com.metsci.glimpse.painter.group;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.google.common.collect.Lists;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.WrappedAxis1D;
import com.metsci.glimpse.axis.painter.label.WrappedLabelHandler;
import com.metsci.glimpse.canvas.FBOGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.support.projection.FlatProjection;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.support.texture.TextureProjected2D;

/**
 * @see WrappedAxis1D
 * @see WrappedLabelHandler
 * @author ulman
 */
public class WrappedPainter extends GlimpsePainter2D
{
    private List<GlimpsePainter2D> painters;

    private boolean isVisible = true;
    private boolean isDisposed = false;

    private FBOGlimpseCanvas offscreen;
    private TextureProjected2D texture;
    private ShadedTexturePainter texturePainter;

    private Axis2D dummyAxis;
    private GlimpseAxisLayout2D dummyLayout;

    public WrappedPainter( )
    {
        this.painters = new CopyOnWriteArrayList<GlimpsePainter2D>( );

        this.dummyAxis = new Axis2D( );
        this.dummyLayout = new GlimpseAxisLayout2D( dummyAxis );
    }

    public void addPainter( GlimpsePainter2D painter )
    {
        this.painters.add( painter );
    }

    public void removePainter( GlimpsePainter2D painter )
    {
        this.painters.remove( painter );
    }

    public void removeAll( )
    {
        this.painters.clear( );
    }

    public boolean isVisible( )
    {
        return this.isVisible;
    }

    public void setVisible( boolean visible )
    {
        this.isVisible = visible;
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        if ( !this.isVisible ) return;

        GL2 gl2 = context.getGL( ).getGL2( );
        gl2.glBlendFuncSeparate( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA, GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_ALPHA );
        gl2.glEnable( GL2.GL_BLEND );

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        boolean wrapX = axisX instanceof WrappedAxis1D;
        boolean wrapY = axisY instanceof WrappedAxis1D;

        // if no WrappedAxis1D is being used, simply paint normally
        if ( !wrapX && !wrapY )
        {
            for ( GlimpsePainter2D painter : painters )
            {
                painter.paintTo( context, bounds, axis );
            }
        }
        else
        {
            if ( !axisX.isInitialized( ) || !axisY.isInitialized( ) || bounds.getHeight( ) == 0 || bounds.getWidth( ) == 0 ) return;

            // lazily allocate offscreen buffer if necessary
            //

            if ( this.offscreen == null )
            {
                this.offscreen = new FBOGlimpseCanvas( context.getGLContext( ), 0, 0, false );
                this.texture = this.offscreen.getProjectedTexture( );
                this.offscreen.addLayout( dummyLayout );

                this.texturePainter = new ShadedTexturePainter( );
                this.texturePainter.addDrawableTexture( this.texture );
            }

            this.dummyLayout.removeAllLayouts( );
            this.dummyLayout.addPainter( new BackgroundPainter( ).setColor( 0, 0, 0, 0 ) );
            for ( GlimpsePainter2D painter : this.painters )
            {
                this.dummyLayout.addPainter( painter );
            }

            List<WrappedTextureBounds> boundsX = Lists.newArrayList( iterator( axisX, bounds.getWidth( ) ) );
            List<WrappedTextureBounds> boundsY = Lists.newArrayList( iterator( axisY, bounds.getHeight( ) ) );

            // always require a redraw for the first image
            boolean forceRedraw = true;

            for ( WrappedTextureBounds boundX : boundsX )
            {
                for ( WrappedTextureBounds boundY : boundsY )
                {
                    drawTile( context, bounds, axis, boundX, boundY, forceRedraw );
                    forceRedraw = false;
                }
            }
        }
    }

    protected void drawTile( GlimpseContext context, GlimpseBounds bounds, Axis2D axis, WrappedTextureBounds boundsX, WrappedTextureBounds boundsY, boolean forceRedraw )
    {
        if ( boundsX.isRedraw( ) || boundsY.isRedraw( ) || forceRedraw )
        {
            this.offscreen.resize( boundsX.getTextureSize( ), boundsY.getTextureSize( ) );

            // when we draw offscreen, do so in "wrapped coordinates" (if the wrapped axis is
            // bounded from 0 to 10, it should be because that is the domain that the painters
            // are set up to draw in)
            this.dummyAxis.set( boundsX.getStartValueWrapped( ), boundsX.getEndValueWrapped( ), boundsY.getStartValueWrapped( ), boundsY.getEndValueWrapped( ) );

            // release the onscreen context and make the offscreen context current
            context.getGLContext( ).release( );
            try
            {
                GLContext glContext = this.offscreen.getGLDrawable( ).getContext( );
                glContext.makeCurrent( );
                try
                {
                    // draw the dummy layout onto the offscreen canvas
                    this.offscreen.paint( );
                }
                finally
                {
                    glContext.release( );
                }
            }
            finally
            {
                context.getGLContext( ).makeCurrent( );
            }
        }

        // use a projection to position the texture in non-wrapped coordinates (since we've
        // split up the image such that we don't have to worry about seams)
        FlatProjection proj = new FlatProjection( boundsX.getStartValue( ), boundsX.getEndValue( ), boundsY.getStartValue( ), boundsY.getEndValue( ) );
        this.texture.setProjection( proj );

        // paint the texture from the offscreen buffer onto the screen
        this.texturePainter.paintTo( context, bounds, axis );
    }

    // Heuristic to determine how we will draw the offscreen image.
    //
    // Two cases:
    //
    // 1) If the axis is not wrapped, the offscreen buffer will be the same size as the on-screen buffer
    // 2) Otherwise, the size will be determined based on the zoom level (what percentage of the wrapped
    //    image is visible). There are two cases here:
    //        a) X% to 100% of the wrapped image is visible (i.e. the user has zoomed out: the wrapped image may
    //           be arbitrarily small with many copies of itself drawn). At exactly 100% (when just one
    //           wrapped copy needs to be drawn at full size, the on-screen and offscreen dimensions should
    //           be the same.
    //        b) 0% to X% of the wrapped image is visible (i.e. the user has zoomed in: only a small fraction of
    //           the wrapped image is drawn). In the best case, the user is right in the middle of the wrap
    //           (not on a seam), so we could technically draw normally. However, even when zoomed in, the user
    //           might be on a seam. We don't want to draw the entire wrapped image at large resolution to handle
    //           this (when we just need a small piece of one side and a small piece of the other). So we draw
    //           part of the image twice.
    //    The cutoff between the two cases is arbitrary and chosen for performance. Here we choose case (b) when
    //    drawing offscreen at the correct resolution would require an offscreen buffer twice the size of the on-screen.
    //
    // see comment above: true indicates "case a", false indicates "case b", value ignored if wrap is false
    protected Iterator<WrappedTextureBounds> iterator( Axis1D axis, int boundsSize )
    {
        boolean wrap = axis instanceof WrappedAxis1D;

        if ( wrap )
        {
            WrappedAxis1D wrappedAxis = ( WrappedAxis1D ) axis;
            if ( axis.getMax( ) - axis.getMin( ) < wrappedAxis.getWrapSpan( ) )
            {
                return new ZoomedInIterator( wrappedAxis, boundsSize );
            }
            else
            {
                return new ZoomedOutIterator( wrappedAxis, boundsSize );
            }
        }
        else
        {
            return new NoWrapIterator( axis, boundsSize );
        }
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        if ( !this.isDisposed )
        {
            this.isDisposed = true;

            for ( GlimpsePainter painter : this.painters )
            {
                painter.dispose( context );
            }
        }
    }

    @Override
    public boolean isDisposed( )
    {
        return this.isDisposed;
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        for ( GlimpsePainter painter : this.painters )
        {
            painter.setLookAndFeel( laf );
        }
    }

    private class WrappedTextureBounds
    {
        private double startValue;
        private double endValue;

        private double startValueWrapped;
        private double endValueWrapped;

        private int textureSize;

        // whether the contents of the offscreen buffer can be reused
        private boolean redraw;

        public WrappedTextureBounds( double startValue, double endValue, double startValueWrapped, double endValueWrapped, int textureSize, boolean redraw )
        {
            this.startValue = startValue;
            this.endValue = endValue;
            this.startValueWrapped = startValueWrapped;
            this.endValueWrapped = endValueWrapped;
            this.textureSize = textureSize;
            this.redraw = redraw;
        }

        public double getStartValue( )
        {
            return startValue;
        }

        public double getEndValue( )
        {
            return endValue;
        }

        public double getStartValueWrapped( )
        {
            return startValueWrapped;
        }

        public double getEndValueWrapped( )
        {
            return endValueWrapped;
        }

        public int getTextureSize( )
        {
            return textureSize;
        }

        public boolean isRedraw( )
        {
            return redraw;
        }
    }

    // If we are not wrapping, then simply draw the image as we normally would, using the axis bounds
    private class NoWrapIterator implements Iterator<WrappedTextureBounds>
    {
        private Axis1D axis;
        private int boundsSize;
        private boolean used = false;

        public NoWrapIterator( Axis1D axis, int boundsSize )
        {
            this.axis = axis;
            this.boundsSize = boundsSize;
        }

        @Override
        public boolean hasNext( )
        {
            return !used;
        }

        @Override
        public WrappedTextureBounds next( )
        {
            if ( hasNext( ) )
            {
                used = true;
                return new WrappedTextureBounds( axis.getMin( ), axis.getMax( ), axis.getMin( ), axis.getMax( ), boundsSize, false );
            }
            else
            {
                throw new NoSuchElementException( );
            }
        }

        @Override
        public void remove( )
        {
            throw new UnsupportedOperationException( );
        }

    }

    // In the zoomed in case, we draw one half of the image then the other half.
    private class ZoomedInIterator implements Iterator<WrappedTextureBounds>
    {
        private WrappedAxis1D axis;
        private int boundsSize;
        private int step;

        public ZoomedInIterator( WrappedAxis1D axis, int boundsSize )
        {
            this.axis = axis;
            this.boundsSize = boundsSize;
            this.step = 0;
        }

        @Override
        public boolean hasNext( )
        {
            return this.step < 2;
        }

        @Override
        public WrappedTextureBounds next( )
        {
            if ( hasNext( ) )
            {
                if ( step == 0 )
                {
                    double start = axis.getMin( );
                    double distanceToSeam = axis.getWrapSpan( ) - axis.getWrappedMod( axis.getMin( ) );
                    double distanceToEnd = axis.getMax( ) - axis.getMin( );
                    double distance;

                    double wrappedStart, wrappedEnd;

                    // only one image needed in this case (the seam is not visible)
                    if ( distanceToEnd <= distanceToSeam || distanceToSeam <= 0 )
                    {
                        distance = distanceToEnd;
                        wrappedStart = axis.getWrappedValue( start );
                        wrappedEnd = axis.getWrappedValue( start + distance, true );
                        step = 2;
                    }
                    // we crossed over a seam, so two images will be needed
                    else
                    {
                        distance = distanceToSeam;
                        wrappedStart = axis.getWrappedValue( start );
                        wrappedEnd = axis.getWrapMax( );
                        step = 1;
                    }

                    return new WrappedTextureBounds( start, start + distance, wrappedStart, wrappedEnd, getTextureSize( distance ), true );
                }
                else if ( step == 1 )
                {
                    double start = axis.getMin( );
                    double distanceToSeam = axis.getWrapSpan( ) - axis.getWrappedMod( axis.getMin( ) );
                    double end = axis.getMax( );
                    double distance = end - ( start + distanceToSeam );
                    double wrappedStart = axis.getWrapMin( );
                    double wrappedEnd = axis.getWrappedValue( end, true );

                    step = 2;

                    return new WrappedTextureBounds( start + distanceToSeam, end, wrappedStart, wrappedEnd, getTextureSize( distance ), true );
                }
            }

            throw new NoSuchElementException( );
        }

        @Override
        public void remove( )
        {
            throw new UnsupportedOperationException( );
        }

        protected int getTextureSize( double distance )
        {
            double percent = distance / ( axis.getMax( ) - axis.getMin( ) );
            return ( int ) Math.ceil( percent * boundsSize );
        }

    }

    // In the zoomed out case, we draw the whole image once, then draw it onto the screen multiple times to tile the space.
    // We could use this approach in the ZoomedIn case as well, but we would need to allocate a very large offscreen buffer
    // to draw at the appropriate resolution and some (perhaps most if very zoomed in) of what we draw wouldn't get seen anyway.
    private class ZoomedOutIterator implements Iterator<WrappedTextureBounds>
    {
        private WrappedAxis1D axis;
        private int boundsSize;
        private double current;

        public ZoomedOutIterator( WrappedAxis1D axis, int boundsSize )
        {
            this.axis = axis;
            this.boundsSize = boundsSize;
            this.current = axis.getMin( ) - axis.getWrappedMod( axis.getMin( ) );
        }

        @Override
        public boolean hasNext( )
        {
            return this.current < axis.getMax( );
        }

        @Override
        public WrappedTextureBounds next( )
        {
            if ( hasNext( ) )
            {
                double start = this.current;
                double end = start + axis.getWrapSpan( );
                this.current = end;

                //TODO the texture bounds could be made smaller here -- the image is zoomed out and doesn't take up the whole screen
                return new WrappedTextureBounds( start, end, axis.getWrapMin( ), axis.getWrapMax( ), boundsSize, false );
            }
            else
            {
                throw new NoSuchElementException( );
            }
        }

        @Override
        public void remove( )
        {
            throw new UnsupportedOperationException( );
        }

    }
}

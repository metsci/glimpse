package com.metsci.glimpse.painter.group;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.media.opengl.GLContext;

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

    public WrappedPainter( )
    {
        this.painters = new CopyOnWriteArrayList<GlimpsePainter2D>( );
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

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        // if no WrappedAxis1D is being used, simply paint normally
        if ( ! ( axisX instanceof WrappedAxis1D ) && ! ( axisY instanceof WrappedAxis1D ) )
        {
            for ( GlimpsePainter2D painter : painters )
            {
                painter.paintTo( context, bounds, axis );
            }
        }
        else
        {
            if ( !axisX.isInitialized( ) || !axisY.isInitialized( ) || bounds.getHeight( ) == 0 || bounds.getWidth( ) == 0 ) return;
            
            // calculate wrapped axis bounds
            //

            double minX = axisX.getMin( );
            double maxX = axisX.getMax( );
            double minY = axisY.getMin( );
            double maxY = axisY.getMax( );

            /*
            if ( axisX instanceof WrappedAxis1D )
            {
                WrappedAxis1D wrappedX = ( WrappedAxis1D ) axisX;
                minX = wrappedX.getWrappedValue( minX );
                maxX = wrappedX.getWrappedValue( maxX );
            }

            if ( axisY instanceof WrappedAxis1D )
            {
                WrappedAxis1D wrappedY = ( WrappedAxis1D ) axisY;
                minY = wrappedY.getWrappedValue( minY );
                maxY = wrappedY.getWrappedValue( maxY );
            }
            */

            // lazily allocate offscreen buffer if necessary
            //

            if ( this.offscreen == null )
            {
                this.offscreen = new FBOGlimpseCanvas( context.getGLContext( ), bounds.getWidth( ), bounds.getHeight( ) );
                this.texture = this.offscreen.getProjectedTexture( );
                this.texturePainter = new ShadedTexturePainter( );
                this.texturePainter.addDrawableTexture( this.texture );
            }

            // create a new axis and layout to place the painters in
            // (these new axes will get the wrapped coordinates so that the painters
            //  see the correct axis min/max values)
            //

            Axis2D dummyAxis = new Axis2D( );
            dummyAxis.set( minX, maxX, minY, maxY );
            GlimpseAxisLayout2D dummyLayout = new GlimpseAxisLayout2D( dummyAxis );

            for ( GlimpsePainter2D painter : this.painters )
            {
                dummyLayout.addPainter( painter );
            }

            // prepare the offscreen canvas by resizing it and adding the dummy layout with
            // all the painters to paint
            //

            this.offscreen.resize( bounds.getWidth( ), bounds.getHeight( ) );
            this.offscreen.removeAllLayouts( );
            this.offscreen.addLayout( dummyLayout );

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
            
            // use a projection to position the texture
            FlatProjection proj = new FlatProjection( dummyAxis );
            this.texture.setProjection( proj );
            
            // paint the texture from the offscreen buffer onto the screen
            this.texturePainter.paintTo( context, bounds, dummyAxis );
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
}

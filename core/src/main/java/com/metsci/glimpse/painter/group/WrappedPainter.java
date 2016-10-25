/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.painter.group;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.google.common.collect.Lists;
import com.jogamp.opengl.FBObject;
import com.jogamp.opengl.FBObject.TextureAttachment;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.WrappedAxis1D;
import com.metsci.glimpse.axis.painter.label.WrappedLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.support.shader.triangle.ColorTexture2DProgram;

/**
 * @see WrappedAxis1D
 * @see WrappedLabelHandler
 * @author ulman
 */
public class WrappedPainter extends GlimpsePainterBase
{
    private static final Logger logger = Logger.getLogger( WrappedPainter.class.getName( ) );

    private List<GlimpsePainter> painters;

    private FBObject fbo;
    private TextureAttachment fboTextureAttachment;
    private int fboTextureUnit = 0;

    private GLEditableBuffer vertCoordBuffer;
    private GLEditableBuffer texCoordBuffer;
    private ColorTexture2DProgram prog;

    private Axis2D dummyAxis;
    private GlimpseAxisLayout2D dummyLayout;

    public WrappedPainter( )
    {
        this.painters = new CopyOnWriteArrayList<GlimpsePainter>( );

        this.dummyAxis = new Axis2D( );
        this.dummyLayout = new GlimpseAxisLayout2D( dummyAxis );
    }

    public void addPainter( GlimpsePainter painter )
    {
        this.painters.add( painter );
    }

    public void removePainter( GlimpsePainter painter )
    {
        this.painters.remove( painter );
    }

    public void removeAll( )
    {
        this.painters.clear( );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        Axis2D axis = requireAxis2D( context );
        GlimpseBounds bounds = getBounds( context );
        GL3 gl = context.getGL( ).getGL3( );

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        boolean wrapX = axisX instanceof WrappedAxis1D;
        boolean wrapY = axisY instanceof WrappedAxis1D;

        // if no WrappedAxis1D is being used, simply paint normally
        if ( !wrapX && !wrapY )
        {
            for ( GlimpsePainter painter : painters )
            {
                painter.paintTo( context );
            }
        }
        else
        {
            if ( !axisX.isInitialized( ) || !axisY.isInitialized( ) || bounds.getHeight( ) == 0 || bounds.getWidth( ) == 0 ) return;

            // lazily allocate offscreen buffer if necessary
            if ( this.fbo == null )
            {
                this.fbo = new FBObject( );
                this.fbo.init( gl, 0, 0, 0 );
                this.fboTextureAttachment = this.fbo.attachTexture2D( gl, this.fboTextureUnit, true );
                this.fbo.unbind( gl );

                this.texCoordBuffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
                this.vertCoordBuffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );

                this.prog = new ColorTexture2DProgram( );
            }

            this.dummyLayout.removeAllLayouts( );
            for ( GlimpsePainter painter : this.painters )
            {
                this.dummyLayout.addPainter( painter );
            }

            // before figuring out which tiles need to be rendered, make sure constraints are applied, etc.
            // XXX: not sure why this doesn't get called automatically somewhere else
            axis.validate( );

            List<WrappedTextureBounds> boundsX = Lists.newArrayList( iterator( axisX, bounds.getWidth( ) ) );
            List<WrappedTextureBounds> boundsY = Lists.newArrayList( iterator( axisY, bounds.getHeight( ) ) );

            // always require a redraw for the first image
            boolean forceRedraw = true;

            for ( WrappedTextureBounds boundX : boundsX )
            {
                for ( WrappedTextureBounds boundY : boundsY )
                {
                    drawTile( context, axis, boundX, boundY, forceRedraw );
                    forceRedraw = false;
                }
            }
        }
    }

    protected void drawTile( GlimpseContext context, Axis2D axis, WrappedTextureBounds boundsX, WrappedTextureBounds boundsY, boolean forceRedraw )
    {
        if ( boundsX.isRedraw( ) || boundsY.isRedraw( ) || forceRedraw )
        {
            GL3 gl = context.getGL( ).getGL3( );

            // when we draw offscreen, do so in "wrapped coordinates" (if the wrapped axis is
            // bounded from 0 to 10, it should be because that is the domain that the painters
            // are set up to draw in)
            this.dummyAxis.set( boundsX.getStartValueWrapped( ), boundsX.getEndValueWrapped( ), boundsY.getStartValueWrapped( ), boundsY.getEndValueWrapped( ) );
            this.dummyAxis.validate( );

            if ( this.fbo.getWidth( ) < boundsX.getTextureSize( ) || this.fbo.getHeight( ) < boundsY.getTextureSize( ) )
            {
                this.fbo.reset( gl, boundsX.getTextureSize( ), boundsY.getTextureSize( ), 0 );
            }

            GlimpseContext glimpseContext = new GlimpseContextImpl( context.getGLContext( ), new int[] { 1, 1 } );
            glimpseContext.getTargetStack( ).push( this.dummyLayout, new GlimpseBounds( 0, 0, boundsX.getTextureSize( ), boundsY.getTextureSize( ) ) );

            this.fbo.bind( gl );
            try
            {
                this.dummyLayout.paintTo( glimpseContext );
            }
            finally
            {
                this.fbo.unbind( gl );
            }

            // reset the viewport and scissor (which will be modified by dummyLayout.paintTo( )
            GLUtils.setViewportAndScissor( context );
        }

        drawTexture( context, axis, boundsX, boundsY );
    }

    protected void drawTexture( final GlimpseContext context, final Axis2D axis, final WrappedTextureBounds boundsX, final WrappedTextureBounds boundsY )
    {
        GL3 gl = context.getGL( ).getGL3( );

        // position the drawn data in non-wrapped coordinates
        // (since we've split up the image such that we don't have to worry about seams)
        this.vertCoordBuffer.clear( );
        this.vertCoordBuffer.growQuad2f( ( float ) boundsX.getStartValue( ), ( float ) boundsY.getStartValue( ), ( float ) boundsX.getEndValue( ), ( float ) boundsY.getEndValue( ) );

        // we don't necessarily use the whole texture, so only texture with the part we drew onto
        this.texCoordBuffer.clear( );
        this.texCoordBuffer.growQuad2f( 0, 0, ( float ) boundsX.getTextureSize( ) / ( float ) this.fbo.getWidth( ), ( float ) boundsY.getTextureSize( ) / ( float ) fbo.getHeight( ) );

        GLUtils.enableStandardBlending( gl );
        this.fbo.use( gl, this.fboTextureAttachment );
        this.prog.begin( context );
        try
        {
            this.prog.setAxisOrtho( context, axis );
            this.prog.setColor( context, GlimpseColor.getWhite( ) );
            this.prog.setTexture( context, fboTextureUnit );

            this.prog.draw( context, GL.GL_TRIANGLES, this.vertCoordBuffer, this.texCoordBuffer, 0, this.texCoordBuffer.sizeFloats( ) / 2 );
        }
        finally
        {
            this.prog.end( context );
            this.fbo.unuse( gl );
            GLUtils.disableBlending( gl );
        }
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
    public void doDispose( GlimpseContext context )
    {
        for ( GlimpsePainter painter : this.painters )
        {
            painter.dispose( context );
        }
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

                    int textureSize = getTextureSize( boundsSize, axis, distance );

                    return new WrappedTextureBounds( start, start + distance, wrappedStart, wrappedEnd, textureSize, true );
                }
                else if ( step == 1 )
                {
                    double start = axis.getMin( );
                    double distanceToSeam = axis.getWrapSpan( ) - axis.getWrappedMod( axis.getMin( ) );
                    double end = axis.getMax( );
                    double distance = end - ( start + distanceToSeam );
                    double wrappedStart = axis.getWrapMin( );
                    double wrappedEnd = axis.getWrappedValue( end, true );

                    int textureSize = getTextureSize( boundsSize, axis, distance );

                    step = 2;

                    return new WrappedTextureBounds( start + distanceToSeam, end, wrappedStart, wrappedEnd, textureSize, true );
                }
            }

            throw new NoSuchElementException( );
        }

        @Override
        public void remove( )
        {
            throw new UnsupportedOperationException( );
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

                int textureSize = getTextureSize( boundsSize, axis, axis.getWrapSpan( ) );

                return new WrappedTextureBounds( start, end, axis.getWrapMin( ), axis.getWrapMax( ), textureSize, true );
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

    protected static int getTextureSize( int boundsSize, Axis1D axis, double distanceAlongAxis )
    {
        double fractionOfAxis = distanceAlongAxis / ( axis.getMax( ) - axis.getMin( ) );
        return ( int ) Math.ceil( fractionOfAxis * boundsSize );
    }
}

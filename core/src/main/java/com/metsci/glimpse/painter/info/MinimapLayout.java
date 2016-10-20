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
package com.metsci.glimpse.painter.info;

import static com.metsci.glimpse.context.TargetStackUtil.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.factory.DefaultAxisFactory2D;
import com.metsci.glimpse.axis.factory.FixedAxisFactory2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseMotionListener;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.plot.Plot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

/**
 * A {@link com.metsci.glimpse.layout.GlimpseLayout} which
 * floats inside a larger GlimpseLayout and provides a zoomed out
 * view of the scene inside the larger GlimpseLayout. Clicking
 * inside the minimap allows quick navigation inside the larger layout.
 *
 * @author ulman
 * @see com.metsci.glimpse.examples.retarget.MiniMapExample
 */
public class MinimapLayout extends GlimpseAxisLayout2D
{
    protected GlimpseAxisLayout2D delegateLayer;

    public MinimapLayout( )
    {
        Axis2D minimapAxis = new Axis2D( );
        this.setAxis( minimapAxis );

        this.delegateLayer = new GlimpseAxisLayout2D( );
        this.delegateLayer.setAxisFactory( new DefaultAxisFactory2D( ) );
        this.delegateLayer.setEventConsumer( false );
        this.delegateLayer.setEventGenerator( true );

        this.addLayout( this.delegateLayer );
        this.delegateLayer.addPainter( new MiniMapBoundsPainter( ), Plot2D.FOREGROUND_LAYER );
        this.delegateLayer.addPainter( new BorderPainter( ), Plot2D.FOREGROUND_LAYER );

        addGlimpseMouseMotionListener( new GlimpseMouseMotionListener( )
        {

            @Override
            public void mouseMoved( GlimpseMouseEvent event )
            {
                if ( event.isButtonDown( MouseButton.Button1 ) )
                {
                    recenterMiniMap0( event );
                }
            }

        } );

        addGlimpseMouseListener( new GlimpseMouseListener( )
        {
            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                recenterMiniMap0( event );
            }

            @Override
            public void mouseEntered( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseExited( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent event )
            {
            }

        } );
    }

    @Override
    public void addPainter( GlimpsePainter painter )
    {
        this.delegateLayer.addPainter( painter );
    }

    @Override
    public void removePainter( GlimpsePainter painter )
    {
        this.delegateLayer.removePainter( painter );
    }

    /**
     * @param  xOffset
     *         The horizontal offset between the minimap and the edge of its container.
     *         <ul>
     *           <li>if positive: positions the minimap inside the container's left edge
     *           <li>if negative: positions the minimap inside the container's right edge
     *           <li>if 0: positions the minimap exactly on the container's left edge
     *           <li>if -1: positions the minimap exactly on the container's right edge
     *         </ul>
     *
     * @param  yOffset
     *         The vertical offset between the minimap and the edge of its container.
     *         <ul>
     *           <li>if positive: positions the minimap inside the container's bottom edge
     *           <li>if negative: positions the minimap inside the container's top edge
     *           <li>if 0: positions the minimap exactly on the container's bottom edge
     *           <li>if -1: positions the minimap exactly on the container's top edge
     *         </ul>
     *
     * @param  width
     *         The width of the minimap, including its border.
     *
     * @param  height
     *         The height of the minimap, including its border.
     */
    public void setPosition( int xOffset, int yOffset, int width, int height )
    {
        String x;
        String x2;
        if ( xOffset >= 0 )
        {
            x = String.format( "%d", xOffset );
            x2 = String.format( "%d", ( xOffset + width ) );
        }
        else
        {
            x = String.format( "(container.w-%d)", ( -xOffset - 1 + width ) );
            x2 = String.format( "(container.w-%d)", ( -xOffset - 1 ) );
        }

        String y;
        String y2;
        if ( yOffset >= 0 )
        {
            y = String.format( "%d", yOffset );
            y2 = String.format( "%d", ( yOffset + height ) );
        }
        else
        {
            y = String.format( "(container.h-%d)", ( -yOffset - 1 + height ) );
            y2 = String.format( "(container.h-%d)", ( -yOffset - 1 ) );
        }

        setLayoutData( String.format( "pos %s %s %s %s", x, y, x2, y2 ) );
    }

    public void setBounds( double minX, double maxX, double minY, double maxY )
    {
        setAxis( axis, minX, maxX, minY, maxY );
        setAxisFactory( new FixedAxisFactory2D( minX, maxX, minY, maxY ) );

        axis.getAxisX( ).validate( );
        axis.getAxisY( ).validate( );
    }

    protected void setAxis( Axis2D axis, double minX, double maxX, double minY, double maxY )
    {
        axis.getAxisX( ).setMin( minX );
        axis.getAxisX( ).setMax( maxX );
        axis.getAxisY( ).setMin( minY );
        axis.getAxisY( ).setMax( maxY );

        axis.getAxisX( ).lockMin( minX );
        axis.getAxisX( ).lockMax( maxX );
        axis.getAxisY( ).lockMin( minY );
        axis.getAxisY( ).lockMax( maxY );
    }

    public class MiniMapBoundsPainter extends GlimpsePainterBase
    {
        protected float[] cursorColor = GlimpseColor.getBlack( ); //new float[] { 0.0f, 0.769f, 1.0f, 1.0f };
        protected float[] shadeColor = new float[] { 0.0f, 0.769f, 1.0f, 0.25f };

        protected FlatColorProgram fillProg;
        protected GLEditableBuffer fillBuffer;

        protected LineProgram lineProg;
        protected LinePath linePath;
        protected LineStyle lineStyle;

        public MiniMapBoundsPainter( )
        {
            this.fillBuffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );

            this.lineStyle = new LineStyle( );
            this.lineStyle.stippleEnable = false;
            this.lineStyle.feather_PX = 0.5f;
            this.lineStyle.rgba = cursorColor;
            this.lineStyle.thickness_PX = 1.0f;

            this.linePath = new LinePath( );

            this.fillProg = new FlatColorProgram( );
            this.lineProg = new LineProgram( );
        }

        @Override
        public void doPaintTo( GlimpseContext context )
        {
            GlimpseBounds bounds = getBounds( context );
            Axis2D miniMapAxis = getMiniMapAxis0( context );
            Axis2D mainMapAxis = getMainMapAxis0( context );

            GL3 gl = context.getGL( ).getGL3( );

            float minX = ( float ) mainMapAxis.getMinX( );
            float maxX = ( float ) mainMapAxis.getMaxX( );
            float minY = ( float ) mainMapAxis.getMinY( );
            float maxY = ( float ) mainMapAxis.getMaxY( );

            GLUtils.enableStandardBlending( gl );
            try
            {
                this.fillBuffer.clear( );
                this.fillBuffer.growQuad2f( minX, minY, maxX, maxY );

                this.fillProg.begin( gl );
                try
                {
                    this.fillProg.setAxisOrtho( gl, miniMapAxis );
                    this.fillProg.draw( gl, this.fillBuffer, this.shadeColor );
                }
                finally
                {
                    this.fillProg.end( gl );
                }

                this.linePath.clear( );
                this.linePath.addRectangle( minX, minY, maxX, maxY );

                this.lineProg.begin( gl );
                try
                {
                    this.lineProg.setAxisOrtho( gl, miniMapAxis );
                    this.lineProg.setViewport( gl, bounds );

                    this.lineProg.draw( gl, lineStyle, linePath );
                }
                finally
                {
                    this.lineProg.end( gl );
                }
            }
            finally
            {
                GLUtils.disableBlending( gl );
            }
        }

        @Override
        protected void doDispose( GlimpseContext context )
        {
            this.lineProg.dispose( context.getGL( ).getGL3( ) );
            this.fillProg.dispose( context.getGL( ).getGL3( ) );

            this.fillBuffer.dispose( context.getGL( ) );
            this.linePath.dispose( context.getGL( ) );
        }
    }

    protected Axis2D getMiniMapAxis0( GlimpseContext context )
    {
        return getAxis0( context.getTargetStack( ) );
    }

    protected Axis2D getMiniMapAxis0( GlimpseTargetStack stack )
    {
        return getAxis0( stack );
    }

    // assume that the main map is always two GlimpseTargets up from the mini-map
    // (this is how things are currently set up)
    // subclasses may override for use in situations where this is not the case
    //
    // because this minimap layout could be painted to multiple contexts with different
    // axes, we essentially need a "relative path" from the current display context to
    // the display context containing the parent axes. This is the price we pay for
    // the added generality that Glimpse 0.8 provides (previously we would simply pass
    // in the main map axes as an argument).
    protected Axis2D getMainMapAxis0( GlimpseContext context )
    {
        return getAxis0( getMainMapTargetStack( context.getTargetStack( ) ) );
    }

    protected Axis2D getMainMapAxis0( GlimpseTargetStack stack )
    {
        return getAxis0( getMainMapTargetStack( stack ) );
    }

    protected GlimpseTargetStack getMainMapTargetStack( GlimpseTargetStack stack )
    {
        return newTargetStack( stack ).pop( ).pop( );
    }

    protected Axis2D getAxis0( GlimpseTargetStack stack )
    {
        GlimpseTarget target = stack.getTarget( );

        Axis2D axis = null;

        if ( target instanceof GlimpseAxisLayout2D )
        {
            axis = ( ( GlimpseAxisLayout2D ) target ).getAxis( stack );
        }

        return axis;
    }

    protected void recenterMiniMap0( GlimpseMouseEvent event )
    {
        Axis2D miniMapAxis = getMiniMapAxis0( event.getTargetStack( ) );
        Axis2D mainMapAxis = getMainMapAxis0( event.getTargetStack( ) );

        double x = miniMapAxis.getAxisX( ).screenPixelToValue( event.getX( ) );
        double y = miniMapAxis.getAxisY( ).screenPixelToValue( miniMapAxis.getAxisY( ).getSizePixels( ) - event.getY( ) );

        double width = mainMapAxis.getMaxX( ) - mainMapAxis.getMinX( );
        double height = mainMapAxis.getMaxY( ) - mainMapAxis.getMinY( );

        mainMapAxis.getAxisX( ).setMin( x - width / 2.0 );
        mainMapAxis.getAxisX( ).setMax( x + width / 2.0 );
        mainMapAxis.getAxisX( ).validate( );

        mainMapAxis.getAxisY( ).setMin( y - height / 2.0 );
        mainMapAxis.getAxisY( ).setMax( y + height / 2.0 );
        mainMapAxis.getAxisY( ).validate( );
    }
}

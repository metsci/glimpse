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
package com.metsci.glimpse.axis.painter;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.triangle.ColorTexture1DProgram;

/**
 * A vertical (y) axis with a color bar and labeled ticks along the right hand side.
 * Suitable for a color bar which should sit flush against the right hand side of a plot.
 *
 * @author ulman
 */
public class ColorYAxisPainter extends NumericYAxisPainter
{
    protected ColorTexture1DProgram progTex;
    protected GLEditableBuffer xyBuffer;
    protected GLEditableBuffer sBuffer;
    protected ColorTexture1D colorTexture;

    protected LineProgram progOutline;
    protected LinePath pathOutline;

    protected LineStyle style;

    protected int colorBarSize = 10;
    protected boolean outline = true;

    public ColorYAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );

        this.pathOutline = new LinePath( );
        this.xyBuffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
        this.sBuffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );

        this.style = new LineStyle( );
        this.style.joinType = LineJoinType.JOIN_MITER;
        this.style.stippleEnable = false;
        this.style.thickness_PX = 1.0f;
        this.style.feather_PX = 0.0f;
        this.style.rgba = GlimpseColor.getBlack( );

        setTickSize( this.colorBarSize + 0 );
    }

    public void setEnableOutline( boolean doOutline )
    {
        this.outline = doOutline;
    }

    public void setColorScale( ColorTexture1D colorTexture )
    {
        this.colorTexture = colorTexture;
    }

    public void setColorBarSize( int size )
    {
        this.colorBarSize = size;
        setTickSize( colorBarSize + 2 );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        updateTextRenderer( );
        if ( textRenderer == null ) return;

        GL3 gl = context.getGL( ).getGL3( );
        Axis1D axis = getAxis1D( context );
        GlimpseBounds bounds = getBounds( context );

        paintColorScale( context );
        TickInfo info = getTickInfo( axis, bounds );
        paintTicks( gl, axis, bounds, info );
        paintTickLabels( gl, axis, bounds, info );
        paintAxisLabel( gl, axis, bounds );
        paintSelectionLine( gl, axis, bounds );
    }

    @Override
    protected void initShaders( )
    {
        super.initShaders( );

        progOutline = new LineProgram( );
        progTex = new ColorTexture1DProgram( );
    }

    protected void paintColorScale( GlimpseContext context )
    {
        if ( colorTexture != null )
        {
            GlimpseBounds bounds = getBounds( context );
            GL3 gl = context.getGL( ).getGL3( );

            int height = bounds.getHeight( );
            int width = bounds.getWidth( );

            float x1 = getColorBarMinX( width );
            float x2 = getColorBarMaxX( width );

            float inset_PX = 0.5f * style.thickness_PX;

            pathOutline.clear( );
            pathOutline.addRectangle( x1 + inset_PX, inset_PX, x2 - inset_PX, height - inset_PX );

            xyBuffer.clear( );
            xyBuffer.growQuad2f( x1 + inset_PX, inset_PX, x2 - inset_PX, height - inset_PX );

            sBuffer.clear( );
            sBuffer.growQuad1f( 0, 1, 0, 1 );

            GLUtils.enableStandardBlending( gl );
            try
            {
                // draw color scale
                progTex.begin( context );
                try
                {
                    progTex.setPixelOrtho( context, bounds );

                    progTex.draw( context, colorTexture, xyBuffer, sBuffer );
                }
                finally
                {
                    progTex.end( context );
                }

                // draw outline box
                progOutline.begin( gl );
                try
                {
                    progOutline.setPixelOrtho( gl, bounds );
                    progOutline.setViewport( gl, bounds );

                    progOutline.draw( gl, style, pathOutline );
                }
                finally
                {
                    progOutline.end( gl );
                }
            }
            finally
            {
                GLUtils.disableBlending( gl );
            }
        }
    }

    public float getColorBarMinX( int width )
    {
        return width - tickBufferSize - colorBarSize - 0.5f;
    }

    public float getColorBarMaxX( int width )
    {
        return width - tickBufferSize - 0.5f;
    }
}

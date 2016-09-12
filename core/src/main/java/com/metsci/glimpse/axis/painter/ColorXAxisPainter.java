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

import static javax.media.opengl.GL.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.line.LinePath;
import com.metsci.glimpse.support.line.LineProgram;
import com.metsci.glimpse.support.line.LineStyle;
import com.metsci.glimpse.support.line.util.LineUtils;
import com.metsci.glimpse.support.line.util.MappableBuffer;
import com.metsci.glimpse.support.shader.ColorTexture1DProgram;

/**
 * A horizontal (x) axis with a color bar and labeled ticks along the bottom.
 *
 * @author ulman
 * @see com.metsci.glimpse.examples.axis.MultiAxisPlotExample
 */
public class ColorXAxisPainter extends NumericXAxisPainter
{
    protected ColorTexture1DProgram progTex;
    protected LinePath pathTex;

    protected LineProgram progLine;
    protected LinePath pathLine;

    protected LineStyle style;

    protected MappableBuffer sVbo;
    protected ColorTexture1D colorTexture;

    protected int colorBarSize = 10;
    protected boolean outline = true;

    public ColorXAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );

        this.pathLine = new LinePath( );
        this.pathTex = new LinePath( );

        this.sVbo = new MappableBuffer( GL_ARRAY_BUFFER, GL_STATIC_DRAW, 1 );

        this.style = new LineStyle( );
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
        GL3 gl = context.getGL( ).getGL3( );
        Axis1D axis = getAxis1D( context );
        GlimpseBounds bounds = getBounds( context );

        updateTextRenderer( );
        if ( textRenderer == null ) return;

        initShaderPrograms( gl );

        paintColorScale( context );
        paintTicks( gl, axis, bounds );
        paintAxisLabel( gl, axis, bounds );
        paintSelectionLine( gl, axis, bounds );
    }

    @Override
    protected void initShaderPrograms( GL gl )
    {
        super.initShaderPrograms( gl );

        if ( progTex == null )
        {
            progLine = new LineProgram( gl.getGL3( ) );

            progTex = new ColorTexture1DProgram( gl.getGL3( ) );
            progTex.setTexture( gl.getGL3( ), 0 );

            // although the vertex coordinates may change, the texture coordinates
            // stay constant, so just set them up once here
            sVbo.mapFloats( gl, 4 ).put( 0.0f ).put( 0.0f ).put( 1.0f ).put( 1.0f );
            sVbo.seal( gl );
        }
    }

    protected void paintColorScale( GlimpseContext context )
    {
        if ( colorTexture != null )
        {
            GlimpseBounds bounds = getBounds( context );
            GL3 gl = context.getGL( ).getGL3( );

            int height = bounds.getHeight( );
            int width = bounds.getWidth( );

            float y1 = getColorBarMinY( height );
            float y2 = getColorBarMaxY( height );

            pathLine.clear( );
            pathLine.lineTo( 0.5f, y2 );
            pathLine.lineTo( 0.5f, y1 );
            pathLine.lineTo( width, y1 );
            pathLine.lineTo( width, y2 );
            pathLine.lineTo( 0.5f, y2 );

            pathTex.clear( );

            pathTex.lineTo( 0.5f, y2 );
            pathTex.lineTo( 0.5f, y1 );
            pathTex.lineTo( width, y2 );
            pathTex.lineTo( width, y1 );

            LineUtils.enableStandardBlending( gl );
            try
            {
                // draw color scale
                progTex.begin( gl );
                try
                {
                    progTex.setPixelOrtho( gl, bounds );

                    progTex.draw( gl, colorTexture, pathTex.xyVbo( gl ), sVbo, 0, pathTex.numVertices( ) );
                }
                finally
                {
                    progTex.end( gl );
                }

                // draw outline box
                progLine.begin( gl );
                try
                {
                    progLine.setPixelOrtho( gl, bounds );
                    progLine.setViewport( gl, bounds );

                    progLine.draw( gl, style, pathLine );
                }
                finally
                {
                    progLine.end( gl );
                }
            }
            finally
            {
                gl.glDisable( GL2.GL_BLEND );
            }
        }
    }

    protected float getColorBarMinY( int height )
    {
        return height - tickBufferSize - colorBarSize - 0.5f;
    }

    protected float getColorBarMaxY( int height )
    {
        return height - tickBufferSize - 0.5f;
    }
}

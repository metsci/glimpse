/*
 * Copyright (c) 2016 Metron, Inc.
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
package com.metsci.glimpse.charts.bathy;

import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_STATIC_DRAW;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLStreamingBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.shape.DynamicLineSetPainter.DynamicLineSetPainterProgram;
import com.metsci.glimpse.painter.shape.DynamicLineSetPainter.DynamicLineSetPainterProgram.LineProgramHandles;
import com.metsci.glimpse.support.shader.line.LineStyle;

/**
 * @author ulman
 */
public class ContourPainter extends GlimpsePainterBase
{
    protected float[] coordsX;
    protected float[] coordsY;
    protected DynamicLineSetPainterProgram program;
    protected GLStreamingBuffer xyVbo;
    protected GLStreamingBuffer rgbaVbo;

    protected LineStyle style;
    protected boolean colorDirty;

    public ContourPainter( ContourData data )
    {
        this( data.getCoordsX( ), data.getCoordsY( ) );
    }

    public ContourPainter( float[] coordsX, float[] coordsY )
    {
        this.coordsX = coordsX;
        this.coordsY = coordsY;

        this.style = new LineStyle( );
        this.program = new DynamicLineSetPainterProgram( );
        this.xyVbo = new GLStreamingBuffer( GL_STATIC_DRAW, 1 );
        this.rgbaVbo = new GLStreamingBuffer( GL_STATIC_DRAW, 1 );

        colorDirty = true;
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );
        Axis2D axis = requireAxis2D( context );
        GlimpseBounds bounds = getBounds( context );

        if ( xyVbo.sealedOffset( ) < 0 || rgbaVbo.sealedOffset( ) < 0 || colorDirty )
        {
            FloatBuffer xyBuf = xyVbo.mapFloats( gl, coordsX.length * 2 );
            FloatBuffer rgbaBuf = rgbaVbo.mapFloats( gl, coordsX.length * 4 );
            for ( int i = 0; i < coordsX.length; i++ )
            {
                xyBuf.put( coordsX[i] );
                xyBuf.put( coordsY[i] );
                rgbaBuf.put( style.rgba );
            }

            xyVbo.seal( gl );
            rgbaVbo.seal( gl );
            colorDirty = false;
        }

        program.begin( gl );
        try
        {
            program.setAxisOrtho( gl, axis );
            program.setViewport( gl, bounds );
            program.setStyle( gl, style );

            LineProgramHandles handles = program.handles( gl );

            gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo.buffer( gl ) );
            gl.glVertexAttribPointer( handles.inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

            gl.glBindBuffer( GL_ARRAY_BUFFER, rgbaVbo.buffer( gl ) );
            gl.glVertexAttribPointer( handles.inRgba, 4, GL_FLOAT, false, 0, rgbaVbo.sealedOffset( ) );

            gl.glDrawArrays( GL.GL_LINES, 0, coordsX.length );
        }
        finally
        {
            program.end( gl );
        }
    }

    public void setLineColor( float r, float g, float b, float a )
    {
        this.style.rgba = new float[] { r, g, b, a };
        colorDirty = true;
    }

    public void setLineWidth( float width )
    {
        this.style.thickness_PX = width;
    }

    public void setStyle( LineStyle style )
    {
        this.style = style;
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        this.program.dispose( gl );
        this.xyVbo.dispose( gl );
        this.rgbaVbo.dispose( gl );
    }
}

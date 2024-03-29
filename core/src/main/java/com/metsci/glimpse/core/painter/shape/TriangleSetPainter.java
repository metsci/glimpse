/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.painter.shape;

import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.metsci.glimpse.core.gl.util.GLUtils.disableBlending;
import static com.metsci.glimpse.core.gl.util.GLUtils.enableStandardBlending;

import java.nio.FloatBuffer;

import com.jogamp.opengl.GL3;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.gl.GLEditableBuffer;
import com.metsci.glimpse.core.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.core.support.shader.triangle.FlatColorProgram;

/**
 * Draw triangles with a fixed color. May be triangle fan, triangle strip, or simple triangles.
 *
 * @author borkholder
 */
public class TriangleSetPainter extends GlimpsePainterBase
{
    protected FlatColorProgram prog;
    protected float[] rgba;
    protected int drawMode;

    protected GLEditableBuffer buffer;

    public TriangleSetPainter( )
    {
        this.prog = new FlatColorProgram( );
        this.buffer = new GLEditableBuffer( GL_STATIC_DRAW, 0 );

        this.rgba = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        this.drawMode = GL_TRIANGLES;
    }

    /**
     * Set one of {@code GL_TRIANGLE_FAN}, {@code GL_TRIANGLE_STRIP}, or {@code GL_TRIANGLES}.
     */
    public void setDrawMode( int mode )
    {
        this.drawMode = mode;
    }

    public void setFillColor( float[] rgba )
    {
        this.rgba = rgba;
    }

    public void setData( float[] dataXY, int offset, int length )
    {
        assert length <= dataXY.length && ( length - offset ) % 2 == 0;

        this.painterLock.lock( );
        try
        {
            buffer.clear( );

            FloatBuffer fbuf = buffer.growFloats( length - offset );
            fbuf.put( dataXY, offset, length );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setData( float[] dataX, float[] dataY )
    {
        assert dataX.length == dataY.length;

        this.painterLock.lock( );
        try
        {
            buffer.clear( );

            FloatBuffer fbuf = buffer.growFloats( dataX.length * 2 );
            for ( int i = 0; i < dataX.length; i++ )
            {
                fbuf.put( dataX[i] ).put( dataY[i] );
            }
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        this.prog.dispose( context.getGL( ).getGL3( ) );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        Axis2D axis = requireAxis2D( context );
        GL3 gl = context.getGL( ).getGL3( );

        enableStandardBlending( gl );
        try
        {
            prog.begin( gl );
            try
            {
                prog.setAxisOrtho( gl, axis );
                prog.setColor( gl, rgba );

                prog.draw( gl, drawMode, buffer, 0, buffer.sizeFloats( ) / 2 );
            }
            finally
            {
                prog.end( gl );
            }
        }
        finally
        {
            disableBlending( gl );
        }
    }
}

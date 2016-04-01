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

import java.nio.FloatBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.jogamp.common.nio.Buffers;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;

/**
 * @author ulman
 */
public class ContourPainter extends GlimpsePainter2D
{
    protected float[] lineColor = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
    protected float lineWidth = 1;

    protected float[] coordsX;
    protected float[] coordsY;

    protected boolean initialized;
    protected int[] bufferHandle;

    protected FloatBuffer dataBuffer;
    protected int totalPointCount;

    public ContourPainter( ContourData data )
    {
        this( data.getCoordsX( ), data.getCoordsY( ) );
    }

    public ContourPainter( float[] coordsX, float[] coordsY )
    {
        this.coordsX = coordsX;
        this.coordsY = coordsY;

        int size = Math.min( coordsX.length, coordsY.length );
        this.totalPointCount = size;
        this.dataBuffer = Buffers.newDirectFloatBuffer( size * 2 );

        for ( int i = 0; i < size; i++ )
        {
            this.dataBuffer.put( coordsX[i] ).put( coordsY[i] );
        }
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        GL2 gl = context.getGL( ).getGL2();

        if ( !initialized )
        {
            bufferHandle = new int[1];
            gl.glGenBuffers( 1, bufferHandle, 0 );

            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, bufferHandle[0] );

            // copy data from the host memory buffer to the device
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, totalPointCount * 2 * BYTES_PER_FLOAT, dataBuffer.rewind( ), GL2.GL_STATIC_DRAW );

            glHandleError( gl );

            initialized = true;
        }

        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, bufferHandle[0] );
        gl.glVertexPointer( 2, GL2.GL_FLOAT, 0, 0 );
        gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( axis.getMinX( ), axis.getMaxX( ), axis.getMinY( ), axis.getMaxY( ), -1, 1 );

        gl.glColor4fv( lineColor, 0 );
        gl.glLineWidth( lineWidth );

        gl.glEnable( GL2.GL_LINE_SMOOTH );
        gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL2.GL_BLEND );

        gl.glDrawArrays( GL2.GL_LINES, 0, totalPointCount );

        gl.glDisable( GL2.GL_BLEND );
        gl.glDisable( GL2.GL_LINE_SMOOTH );

        gl.glDisableClientState( GL2.GL_VERTEX_ARRAY );
    }

    public void setLineColor( float r, float g, float b, float a )
    {
        this.lineColor[0] = r;
        this.lineColor[1] = g;
        this.lineColor[2] = b;
        this.lineColor[3] = a;
    }

    public void setLineWidth( float width )
    {
        this.lineWidth = width;
    }

    @Override
    public void dispose( GLContext context )
    {
        if ( initialized )
        {
            context.getGL( ).glDeleteBuffers( 1, bufferHandle, 0 );
        }
    }
}

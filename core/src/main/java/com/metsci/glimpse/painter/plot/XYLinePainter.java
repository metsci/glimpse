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
package com.metsci.glimpse.painter.plot;

import java.nio.FloatBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.jogamp.common.nio.Buffers;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.support.colormap.ColorMap;

/**
 * Plots a simple x-y lineplot. Provides options for modifying
 * line thickness and color.
 *
 * @author ulman
 */
public class XYLinePainter extends GlimpseDataPainter2D
{
    protected float[] lineColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    protected float lineThickness = 1;
    protected boolean showLines = true;

    protected float pointSize = 6;
    protected boolean showPoints = true;

    protected int stippleFactor = 1;
    protected short stipplePattern = ( short ) 0x00FF;
    protected boolean stippleOn = false;

    protected int dataSize = 0;

    protected int[] colorHandle = null;
    protected FloatBuffer colorBuffer = null;
    protected boolean useColorDevice = false;
    protected boolean useColorHost = false;

    protected int[] bufferHandle = null;
    protected FloatBuffer dataBuffer = null;

    protected ReentrantLock dataBufferLock = null;

    protected volatile boolean newData = false;
    protected volatile boolean bufferInitialized = false;

    public XYLinePainter( )
    {
        this.dataBufferLock = new ReentrantLock( );
    }

    public void setDataAndColor( double[] dataX, double[] dataY, double[] dataZ, ColorMap scale )
    {
        this.dataBufferLock.lock( );
        try
        {
            setData( dataX, dataY );
            setColor( dataZ, scale );
        }
        finally
        {
            this.dataBufferLock.unlock( );
        }
    }

    public void setDataAndColor( float[] dataX, float[] dataY, float[] dataZ, ColorMap scale )
    {
        this.dataBufferLock.lock( );
        try
        {
            setData( dataX, dataY );
            setColor( dataZ, scale );
        }
        finally
        {
            this.dataBufferLock.unlock( );
        }
    }

    public void setData( float[] dataX, float[] dataY )
    {
        this.dataBufferLock.lock( );
        try
        {
            dataSize = Math.min( dataX.length, dataY.length );

            if ( dataBuffer == null || dataBuffer.rewind( ).capacity( ) < dataSize * 2 )
            {
                this.dataBuffer = Buffers.newDirectFloatBuffer( dataSize * 2 );
            }

            // copy data from the provided arrays into the host memory buffer
            for ( int i = 0; i < dataSize; i++ )
            {
                this.dataBuffer.put( dataX[i] ).put( dataY[i] );
            }

            this.newData = true;
        }
        finally
        {
            this.dataBufferLock.unlock( );
        }
    }

    public void setData( double[] dataX, double[] dataY )
    {
        this.dataBufferLock.lock( );
        try
        {
            dataSize = Math.min( dataX.length, dataY.length );

            if ( dataBuffer == null || dataBuffer.rewind( ).capacity( ) < dataSize * 2 )
            {
                this.dataBuffer = Buffers.newDirectFloatBuffer( dataSize * 2 );
            }

            // copy data from the provided arrays into the host memory buffer
            for ( int i = 0; i < dataSize; i++ )
            {
                this.dataBuffer.put( ( float ) dataX[i] ).put( ( float ) dataY[i] );
            }

            this.newData = true;
        }
        finally
        {
            this.dataBufferLock.unlock( );
        }
    }

    public void setColor( float[] dataZ, ColorMap scale )
    {
        this.dataBufferLock.lock( );
        try
        {
            if ( colorBuffer == null || colorBuffer.rewind( ).capacity( ) < dataSize * 4 )
            {
                this.colorBuffer = Buffers.newDirectFloatBuffer( dataSize * 4 );
            }

            float[] color = new float[4];

            // copy data from the provided arrays into the host memory buffer
            for ( int i = 0; i < dataSize; i++ )
            {
                scale.toColor( dataZ[i], color );

                this.colorBuffer.put( color[0] ).put( color[1] ).put( color[2] ).put( color[3] );
            }

            this.useColorHost = true;
        }
        finally
        {
            this.dataBufferLock.unlock( );
        }
    }

    public void setColor( double[] dataZ, ColorMap scale )
    {
        this.dataBufferLock.lock( );
        try
        {
            if ( colorBuffer == null || colorBuffer.rewind( ).capacity( ) < dataSize * 4 )
            {
                this.colorBuffer = Buffers.newDirectFloatBuffer( dataSize * 4 );
            }

            float[] color = new float[4];

            // copy data from the provided arrays into the host memory buffer
            for ( int i = 0; i < dataSize; i++ )
            {
                scale.toColor( ( float ) dataZ[i], color );

                this.colorBuffer.put( color[0] ).put( color[1] ).put( color[2] ).put( color[3] );
            }

            this.useColorHost = true;
        }
        finally
        {
            this.dataBufferLock.unlock( );
        }
    }

    public void setLineStipple( boolean activate )
    {
        this.stippleOn = activate;
    }

    public void setLineStipple( int stippleFactor, short stipplePattern )
    {
        this.stippleFactor = stippleFactor;
        this.stipplePattern = stipplePattern;
    }

    public void setLineThickness( float lineThickness )
    {
        this.lineThickness = lineThickness;
    }

    public void setLineColor( float[] rgba )
    {
        this.lineColor = rgba;
    }

    public void setLineColor( float r, float g, float b, float a )
    {
        this.lineColor[0] = r;
        this.lineColor[1] = g;
        this.lineColor[2] = b;
        this.lineColor[3] = a;
    }

    public void setPointSize( float pointSize )
    {
        this.pointSize = pointSize;
    }

    public void showPoints( boolean show )
    {
        this.showPoints = show;
    }

    public void showLines( boolean show )
    {
        this.showLines = show;
    }

    @Override
    public void dispose( GLContext context )
    {
        if ( bufferInitialized )
        {
            context.getGL( ).glDeleteBuffers( 1, colorHandle, 0 );
            context.getGL( ).glDeleteBuffers( 1, bufferHandle, 0 );
        }
    }

    @Override
    public void paintTo( GL2 gl, GlimpseBounds bounds, Axis2D axis )
    {
        if ( dataSize == 0 ) return;

        if ( !bufferInitialized )
        {
            bufferHandle = new int[1];
            gl.glGenBuffers( 1, bufferHandle, 0 );

            colorHandle = new int[1];
            gl.glGenBuffers( 1, colorHandle, 0 );

            bufferInitialized = true;
        }

        if ( newData )
        {
            this.dataBufferLock.lock( );
            try
            {
                gl.glPixelStorei( GL2.GL_UNPACK_ALIGNMENT, 1 );

                gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, bufferHandle[0] );

                // copy data from the host memory buffer to the device
                gl.glBufferData( GL2.GL_ARRAY_BUFFER, dataSize * 2 * BYTES_PER_FLOAT, dataBuffer.rewind( ), GL2.GL_DYNAMIC_DRAW );

                glHandleError( gl );

                useColorDevice = useColorHost;
                if ( useColorDevice )
                {
                    gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, colorHandle[0] );

                    // copy data from the host memory buffer to the device
                    gl.glBufferData( GL2.GL_ARRAY_BUFFER, dataSize * 4 * BYTES_PER_FLOAT, colorBuffer.rewind( ), GL2.GL_DYNAMIC_DRAW );

                    glHandleError( gl );
                }

                newData = false;
            }
            finally
            {
                this.dataBufferLock.unlock( );
            }
        }

        gl.glShadeModel( GL2.GL_FLAT );

        if ( useColorDevice )
        {
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, colorHandle[0] );
            gl.glColorPointer( 4, GL2.GL_FLOAT, 0, 0 );
            gl.glEnableClientState( GL2.GL_COLOR_ARRAY );
        }

        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, bufferHandle[0] );
        gl.glVertexPointer( 2, GL2.GL_FLOAT, 0, 0 );
        gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );

        gl.glColor4fv( lineColor, 0 );
        gl.glLineWidth( lineThickness );

        if ( showLines )
        {
            if ( stippleOn )
            {
                gl.glEnable( GL2.GL_LINE_STIPPLE );
                gl.glLineStipple( stippleFactor, stipplePattern );
            }

            gl.glDrawArrays( GL2.GL_LINE_STRIP, 0, dataSize );
        }

        if ( showPoints )
        {
            gl.glPointSize( pointSize );
            gl.glDrawArrays( GL2.GL_POINTS, 0, dataSize );
        }
    }
}

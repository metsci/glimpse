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
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.util.GLErrorUtils;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

import it.unimi.dsi.fastutil.floats.Float2IntMap;
import it.unimi.dsi.fastutil.floats.Float2IntOpenHashMap;

/**
 * Plots a simple frequency histogram. Binning of
 * data is handled automatically.
 *
 * Construct with asDensity = true to scale as a density
 * estimate instead of as a frequency histogram.
 *
 * @author ulman
 */
public class HistogramPainter extends GlimpsePainterBase
{
    private static final Logger logger = Logger.getLogger( HistogramPainter.class.getName( ) );

    public static final int FLOATS_PER_BAR = 12;

    protected float[] barColor = new float[] { 1.0f, 0.0f, 0.0f, 0.6f };

    protected int[] bufferHandle = null;
    protected FloatBuffer dataBuffer = null;
    protected ReentrantLock dataBufferLock = null;

    protected volatile int dataSize = 0;
    protected volatile boolean newData = false;
    protected volatile boolean bufferInitialized = false;

    protected float binSize;
    protected float binStart;

    protected float minY;
    protected float maxY;
    protected float minX;
    protected float maxX;

    protected final boolean asDensity;

    protected FlatColorProgram fillProg;

    public HistogramPainter( boolean asDensity )
    {
        this.dataBufferLock = new ReentrantLock( );
        this.asDensity = asDensity;

        this.fillProg = new FlatColorProgram( );
    }

    public HistogramPainter( )
    {
        this( false );
    }

    public void setColor( float[] rgba )
    {
        this.barColor = rgba;
    }

    public void setColor( float r, float g, float b, float a )
    {
        this.barColor[0] = r;
        this.barColor[1] = g;
        this.barColor[2] = b;
        this.barColor[3] = a;
    }

    public void autoAdjustAxisBounds( Axis2D axis )
    {
        axis.getAxisX( ).setMin( minX );
        axis.getAxisX( ).setMax( maxX + getBinSize( ) );
        axis.getAxisY( ).setMin( minY );
        axis.getAxisY( ).setMax( maxY );
    }

    public void setData( double[] data )
    {
        setData( data, data.length );
    }

    public void setData( double[] data, int size )
    {
        if ( data == null || data.length == 0 ) return;

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for ( int i = 0; i < size; i++ )
        {
            double value = data[i];

            if ( value > max ) max = value;
            if ( value < min ) min = value;
        }

        double binSize = ( max - min ) / Math.sqrt( size );

        setData( data, size, binSize, min );
    }

    public void setData( float[] data )
    {
        setData( data, data.length );
    }

    public void setData( float[] data, int size )
    {
        if ( data == null || data.length == 0 ) return;

        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;

        for ( int i = 0; i < size; i++ )
        {
            float value = data[i];

            if ( value > max ) max = value;
            if ( value < min ) min = value;
        }

        float binSize = ( max - min ) / ( float ) Math.sqrt( size );

        setData( data, size, binSize, min );
    }

    public void setData( double[] data, double binSize, double binStart )
    {
        setData( data, data.length, binSize, binStart );
    }

    public void setData( double[] data, int size, double binSize, double binStart )
    {
        this.binStart = ( float ) binStart;

        Float2IntMap counts = new Float2IntOpenHashMap( );

        for ( int i = 0; i < size; i++ )
        {
            double value = data[i];

            float bin = getBin( value, binSize, binStart );

            if ( !counts.containsKey( bin ) )
            {
                counts.put( bin, 1 );
            }
            else
            {
                counts.put( bin, counts.get( bin ) + 1 );
            }
        }

        setData( counts, size, ( float ) binSize );
    }

    /**
     * Sets the histogram data without automatically binning.
     *
     * @param counts map from left edge of bin to number of values in bin
     * @param binSize the width of each bin
     */
    public void setData( Float2IntMap counts, float binSize )
    {
        int sum = 0;
        for ( Float2IntMap.Entry entry : counts.float2IntEntrySet( ) )
        {
            sum += entry.getIntValue( );
        }
        setData( counts, sum, binSize );
    }

    /**
     * Sets the histogram data without automatically binning.
     *
     * @param counts map from left edge of bin to number of values in bin
     * @param totalSize the sum of the count values from the counts map
     * @param binSize the width of each bin
     */
    public void setData( Float2IntMap counts, int totalCount, float binSize )
    {
        dataBufferLock.lock( );
        try
        {
            newData = true;

            this.binSize = binSize;

            minY = 0;
            maxY = 0;

            minX = Float.POSITIVE_INFINITY;
            maxX = Float.NEGATIVE_INFINITY;

            dataSize = counts.size( );

            if ( dataBuffer == null || dataBuffer.rewind( ).capacity( ) < dataSize * FLOATS_PER_BAR )
            {
                dataBuffer = Buffers.newDirectFloatBuffer( dataSize * FLOATS_PER_BAR );
            }

            final float denom = ( asDensity ) ? ( binSize * totalCount ) : totalCount;

            for ( Float2IntMap.Entry entry : counts.float2IntEntrySet( ) )
            {
                float bin = entry.getFloatKey( );
                int count = entry.getIntValue( );

                float freq = count / denom;

                if ( freq > maxY ) maxY = freq;

                if ( bin < minX ) minX = bin;

                if ( bin > maxX ) maxX = bin;

                dataBuffer.put( bin ).put( 0 );
                dataBuffer.put( bin ).put( freq );
                dataBuffer.put( bin + this.binSize ).put( freq );

                dataBuffer.put( bin + this.binSize ).put( freq );
                dataBuffer.put( bin + this.binSize ).put( 0 );
                dataBuffer.put( bin ).put( 0 );
            }
        }
        finally
        {
            dataBufferLock.unlock( );
        }
    }

    public void setData( double[] data, float binSize, float binStart )
    {
        setData( data, data.length, binSize, binStart );
    }

    public void setData( float[] data, int size, float binSize, float binStart )
    {
        this.binStart = binStart;

        Float2IntMap counts = new Float2IntOpenHashMap( );

        for ( int i = 0; i < size; i++ )
        {
            float value = data[i];

            float bin = getBin( value, binSize, binStart );

            if ( !counts.containsKey( bin ) )
            {
                counts.put( bin, 1 );
            }
            else
            {
                counts.put( bin, counts.get( bin ) + 1 );
            }
        }

        setData( counts, size, binSize );
    }

    public float getBinSize( )
    {
        return this.binSize;
    }

    public float getBinStart( )
    {
        return this.binStart;
    }

    public float getMinY( )
    {
        return this.minY;
    }

    public float getMaxY( )
    {
        return this.maxY;
    }

    public float getMinX( )
    {
        return this.minX;
    }

    public float getMaxX( )
    {
        return this.maxX;
    }

    protected static float getBin( double data, double binSize, double binStart )
    {
        return ( float ) ( Math.floor( ( data - binStart ) / binSize ) * binSize + binStart );
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        if ( this.bufferInitialized )
        {
            context.getGL( ).glDeleteBuffers( 1, this.bufferHandle, 0 );
        }

        this.fillProg.dispose( context.getGL( ).getGL3( ) );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        Axis2D axis = requireAxis2D( context );
        GL3 gl = context.getGL( ).getGL3( );

        if ( this.dataSize == 0 ) return;

        if ( !this.bufferInitialized )
        {
            this.bufferHandle = new int[1];
            gl.glGenBuffers( 1, this.bufferHandle, 0 );
            this.bufferInitialized = true;
        }

        gl.glBindBuffer( GL.GL_ARRAY_BUFFER, this.bufferHandle[0] );

        int dataSizeTemp = this.dataSize;

        if ( this.newData )
        {
            this.dataBufferLock.lock( );
            try
            {
                dataSizeTemp = this.dataSize;

                // copy data from the host memory buffer to the device
                gl.glBufferData( GL.GL_ARRAY_BUFFER, dataSizeTemp * FLOATS_PER_BAR * GLUtils.BYTES_PER_FLOAT, dataBuffer.rewind( ), GL.GL_DYNAMIC_DRAW );

                GLErrorUtils.logGLError( logger, gl, "Error copying HistogramPainter data to device." );

                this.newData = false;
            }
            finally
            {
                this.dataBufferLock.unlock( );
            }
        }

        this.fillProg.begin( gl );
        GLUtils.enableStandardBlending( gl );
        try
        {
            this.fillProg.setAxisOrtho( gl, axis );
            this.fillProg.setColor( gl, barColor );

            this.fillProg.draw( gl, GL.GL_TRIANGLES, bufferHandle[0], 0, dataSizeTemp * 6 );
        }
        finally
        {
            GLUtils.disableBlending( gl );
            this.fillProg.end( gl );
        }
    }
}

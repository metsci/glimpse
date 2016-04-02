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
public class HistogramPainter extends GlimpseDataPainter2D
{
    public static final int FLOATS_PER_BAR = 8;

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

    public HistogramPainter( boolean asDensity )
    {
        this.dataBufferLock = new ReentrantLock( );
        this.asDensity = asDensity;
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

                float freq = ( float ) count / denom;

                if ( freq > maxY ) maxY = freq;

                if ( bin < minX ) minX = bin;

                if ( bin > maxX ) maxX = bin;

                dataBuffer.put( bin ).put( 0 );
                dataBuffer.put( bin ).put( freq );
                dataBuffer.put( bin + this.binSize ).put( freq );
                dataBuffer.put( bin + this.binSize ).put( 0 );
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
        return binSize;
    }

    public float getBinStart( )
    {
        return binStart;
    }

    public float getMinY( )
    {
        return minY;
    }

    public float getMaxY( )
    {
        return maxY;
    }

    public float getMinX( )
    {
        return minX;
    }

    public float getMaxX( )
    {
        return maxX;
    }

    protected static float getBin( double data, double binSize, double binStart )
    {
        return ( float ) ( Math.floor( ( data - binStart ) / binSize ) * binSize + binStart );
    }

    @Override
    public void dispose( GLContext context )
    {
        if ( bufferInitialized )
        {
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
            bufferInitialized = true;
        }

        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, bufferHandle[0] );

        int dataSizeTemp = dataSize;

        if ( newData )
        {
            dataBufferLock.lock( );
            try
            {
                dataSizeTemp = dataSize;

                // copy data from the host memory buffer to the device
                gl.glBufferData( GL2.GL_ARRAY_BUFFER, dataSizeTemp * FLOATS_PER_BAR * BYTES_PER_FLOAT, dataBuffer.rewind( ), GL2.GL_DYNAMIC_DRAW );

                glHandleError( gl );

                newData = false;
            }
            finally
            {
                dataBufferLock.unlock( );
            }
        }

        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, bufferHandle[0] );
        gl.glVertexPointer( 2, GL2.GL_FLOAT, 0, 0 );
        gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );

        gl.glColor4fv( barColor, 0 );

        gl.glDrawArrays( GL2.GL_QUADS, 0, dataSizeTemp * 4 );
    }
}

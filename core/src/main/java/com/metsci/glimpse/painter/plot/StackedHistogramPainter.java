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

import static java.lang.Math.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
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

import it.unimi.dsi.fastutil.floats.Float2FloatMap;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import it.unimi.dsi.fastutil.floats.Float2IntMap;
import it.unimi.dsi.fastutil.floats.Float2IntOpenHashMap;

/**
 * Plots a simple frequency histogram. Binning of data can be handled
 * automatically using a fixed-width binning algorithm.
 *
 * Construct with asDensity = true to scale as a density estimate instead
 * of as a frequency histogram.
 *
 * @author borkholder
 */
public class StackedHistogramPainter extends GlimpsePainterBase
{
    private static final Logger logger = Logger.getLogger( StackedHistogramPainter.class.getName( ) );

    public static final int FLOATS_PER_BAR = 12;

    protected float[] defaultSeriesColor = new float[] { 1.0f, 0.0f, 0.0f, 0.6f };

    protected int[] bufferHandle = null;
    protected FloatBuffer dataBuffer = null;
    protected List<HistogramEntry> dataSeries = null;
    protected ReentrantLock dataBufferLock = null;
    protected volatile boolean newData = false;
    protected volatile boolean bufferInitialized = false;

    protected float binSize;
    protected float binStart;

    protected float minY;
    protected float maxY;
    protected float minX;
    protected float maxX;

    protected FlatColorProgram fillProg;

    public StackedHistogramPainter( )
    {
        dataBufferLock = new ReentrantLock( );

        this.fillProg = new FlatColorProgram( );
    }

    public void setDefaultSeriesColor( float[] rgba )
    {
        if ( rgba == null )
        {
            throw new NullPointerException( "rgba cannot be null" );
        }
        else
        {
            defaultSeriesColor = rgba;
        }
    }

    public void setSeriesColor( int series, float[] rgba )
    {
        if ( rgba == null )
        {
            throw new NullPointerException( "rgba cannot be null" );
        }
        else
        {
            // will throw an exception if data is not already in painter
            dataSeries.get( series ).color = rgba;
        }
    }

    public void autoAdjustAxisBounds( Axis2D axis )
    {
        axis.getAxisX( ).setMin( minX );
        axis.getAxisX( ).setMax( maxX );
        axis.getAxisY( ).setMin( minY );
        axis.getAxisY( ).setMax( maxY );
    }

    public void setData( float[]... data )
    {
        int size = 0;
        for ( int i = 0; i < data.length; i++ )
        {
            size = max( size, data[i].length );
        }

        setData( size, data );
    }

    public void setData( int totalNumValues, float[]... data )
    {
        if ( data == null || data.length == 0 )
        {
            return;
        }

        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;

        for ( int i = 0; i < data.length; i++ )
        {
            for ( int j = 0; j < data[i].length; j++ )
            {
                float value = data[i][j];

                if ( value > max )
                {
                    max = value;
                }
                if ( value < min )
                {
                    min = value;
                }
            }
        }

        float binSize = ( max - min ) / ( float ) Math.sqrt( totalNumValues );

        setData( totalNumValues, binSize, min, data );
    }

    public void setData( int totalNumValues, float binSize, float binStart, float[]... data )
    {
        this.binStart = binStart;

        Float2IntMap[] counts = new Float2IntMap[data.length];

        for ( int i = 0; i < data.length; i++ )
        {
            counts[i] = new Float2IntOpenHashMap( data[i].length );

            for ( int j = 0; j < data[i].length; j++ )
            {
                float value = data[i][j];
                float bin = getBin( value, binSize, binStart );

                if ( !counts[i].containsKey( bin ) )
                {
                    counts[i].put( bin, 1 );
                }
                else
                {
                    counts[i].put( bin, counts[i].get( bin ) + 1 );
                }
            }
        }

        setData( totalNumValues, binSize, counts );
    }

    /**
     * Sets the histogram data without automatically binning.
     */
    public void setData( int totalNumValues, float binSize, Float2IntMap... counts )
    {
        this.binSize = binSize;

        dataBufferLock.lock( );
        try
        {
            minY = 0;
            maxY = 0;

            minX = Float.POSITIVE_INFINITY;
            maxX = Float.NEGATIVE_INFINITY;

            int numQuads = 0;
            int numBars = 0;
            for ( Float2IntMap countMap : counts )
            {
                numQuads += countMap.size( );
                numBars = max( numBars, countMap.size( ) );
            }

            if ( dataBuffer == null || dataBuffer.rewind( ).capacity( ) < numQuads * FLOATS_PER_BAR )
            {
                dataBuffer = Buffers.newDirectFloatBuffer( numQuads * FLOATS_PER_BAR );
            }

            /*
             * Keeps the last height for the previous series so we can stack bars on top of each other.
             */
            Float2FloatMap lastBarHeights = new Float2FloatOpenHashMap( numBars );
            lastBarHeights.defaultReturnValue( 0 );

            dataSeries = new ArrayList<HistogramEntry>( );

            for ( Float2IntMap countMap : counts )
            {
                int quadFloatsStart = dataBuffer.position( );

                for ( Float2IntMap.Entry entry : countMap.float2IntEntrySet( ) )
                {
                    float bin = entry.getFloatKey( );
                    int count = entry.getIntValue( );

                    float height = getBarHeight( bin, count, totalNumValues );
                    float lastHeight = lastBarHeights.get( bin );

                    float top = lastHeight + height;

                    maxY = max( top, maxY );
                    minX = min( bin, minX );
                    maxX = max( bin, maxX );

                    dataBuffer.put( bin ).put( lastHeight );
                    dataBuffer.put( bin ).put( top );
                    dataBuffer.put( bin + this.binSize ).put( top );

                    dataBuffer.put( bin + this.binSize ).put( top );
                    dataBuffer.put( bin + this.binSize ).put( lastHeight );
                    dataBuffer.put( bin ).put( lastHeight );

                    lastBarHeights.put( bin, top );
                }

                int numQuadsForSeries = ( dataBuffer.position( ) - quadFloatsStart ) / FLOATS_PER_BAR;
                dataSeries.add( new HistogramEntry( defaultSeriesColor, quadFloatsStart / 2, numQuadsForSeries ) );
            }

            newData = true;
        }
        finally
        {
            dataBufferLock.unlock( );
        }
    }

    protected float getBarHeight( float bin, int count, int totalValues )
    {
        return count;
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

        if ( this.dataSeries == null || this.dataSeries.isEmpty( ) )
        {
            return;
        }

        if ( !this.bufferInitialized )
        {
            this.bufferHandle = new int[1];
            gl.glGenBuffers( 1, this.bufferHandle, 0 );
            this.bufferInitialized = true;
        }

        gl.glBindBuffer( GL.GL_ARRAY_BUFFER, bufferHandle[0] );

        if ( this.newData )
        {
            this.dataBufferLock.lock( );
            try
            {
                // copy data from the host memory buffer to the device
                gl.glBufferData( GL.GL_ARRAY_BUFFER, dataBuffer.position( ) * GLUtils.BYTES_PER_FLOAT, dataBuffer.rewind( ), GL.GL_STATIC_DRAW );

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

            for ( HistogramEntry entry : this.dataSeries )
            {
                this.fillProg.setColor( gl, entry.color );

                this.fillProg.draw( gl, GL.GL_TRIANGLES, bufferHandle[0], entry.quadsFloatStart, entry.numQuads * 6 );
            }
        }
        finally
        {
            GLUtils.disableBlending( gl );
            this.fillProg.end( gl );
        }
    }

    protected static class HistogramEntry
    {
        public float[] color;
        public final int quadsFloatStart;
        public final int numQuads;

        public HistogramEntry( float[] color, int quadsFloatStart, int numQuads )
        {
            this.color = color;
            this.quadsFloatStart = quadsFloatStart;
            this.numQuads = numQuads;
        }
    }
}

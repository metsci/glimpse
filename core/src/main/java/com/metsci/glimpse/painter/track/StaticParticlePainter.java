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
package com.metsci.glimpse.painter.track;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.primitives.LongsArray;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * <p>Displays a static set of tracks with associated timestamp, x position, and y position. Unlike
 * {@link TrackPainter}, each track  must have an xy position for the same set of timestamps. This
 * allows ParticlePainter to be more efficient in this case.</p>
 *
 * <p>The set of particles cannot be modified once ParticlePainter is constructed, but a custom time slice of
 * the particles may be displayed via {@code StaticParticlePainter#displayTimeRange(long, long)}.</p>
 *
 * @author ulman
 *
 */
public class StaticParticlePainter extends GlimpseDataPainter2D
{
    protected IntBuffer firstData;
    protected IntBuffer countData;
    protected FloatBuffer positionData;
    protected FloatBuffer colorData;
    protected LongsArray timeData;
    protected int countTimes;
    protected int countParticles;

    protected TimeStamp startTime;
    protected TimeStamp endTime;

    protected int startIndex;
    protected int endIndex;

    protected int glBufferHandleData = -1;
    protected int glBufferHandleColor = -1;
    protected boolean glInitialized;

    protected float[] color;
    protected float lineWidth;

    public StaticParticlePainter( TimeStamp[] time, float[][] xPositions, float[][] yPositions )
    {
        this( time, xPositions, yPositions, null );
    }

    /**
     * 
     * @param time common array of times (each particle must have an x/y position for each time)
     * @param xPositions square array of x positions indexed as [particleIndex][timeIndex] (second index must match size of time array)
     * @param yPositions square array of y positions indexed as [particleIndex][timeIndex] (second index must match size of time array)
     * @param colors color values indexed as [particleIndex][timeIndex][rgba]
     */
    public StaticParticlePainter( TimeStamp[] time, float[][] xPositions, float[][] yPositions, float[][][] colors )
    {
        this.countTimes = time.length;
        this.countParticles = xPositions[0].length;

        assert ( countTimes == xPositions.length && countTimes == yPositions.length );

        this.positionData = FloatBuffer.allocate( countTimes * countParticles * 2 );
        this.timeData = new LongsArray( countTimes );

        // load data into time array
        for ( int t = 0; t < countTimes; t++ )
        {
            this.timeData.append( time[t].toPosixMillis( ) );
        }

        // load data into array particle-wise (x/y positions for each particle are contiguous)
        for ( int p = 0; p < countParticles; p++ )
        {
            for ( int t = 0; t < countTimes; t++ )
            {
                this.positionData.put( xPositions[p][t] );
                this.positionData.put( yPositions[p][t] );
            }
        }

        this.firstData = IntBuffer.allocate( countParticles );
        this.countData = IntBuffer.allocate( countParticles );

        for ( int p = 0; p < countParticles; p++ )
        {
            this.countData.put( 0 );
            this.firstData.put( 0 );
        }

        if ( colors != null )
        {
            this.colorData = FloatBuffer.allocate( countTimes * countParticles * 4 );

            for ( int p = 0; p < countParticles; p++ )
            {
                for ( int t = 0; t < countTimes; t++ )
                {
                    for ( int c = 0; c < 4; c++ )
                    {
                        this.colorData.put( colors[p][t][c] );
                    }
                }
            }
        }
    }

    public void setColor( float[] color )
    {
        this.color = color;
    }

    public void setLineWidth( float lineWidth )
    {
        this.lineWidth = lineWidth;
    }

    public void displayTimeRange( long startMillis, long endMillis )
    {
        startIndex = Arrays.binarySearch( timeData.a, 0, timeData.n, startMillis );
        if ( startIndex < 0 ) startIndex = - ( startIndex + 1 );
        if ( startIndex > timeData.n - 1 ) startIndex = timeData.n - 1;
        if ( startIndex > 0 && timeData.v( startIndex ) > startMillis ) startIndex--;

        endIndex = Arrays.binarySearch( timeData.a, 0, timeData.n, endMillis );
        if ( endIndex < 0 ) endIndex = - ( endIndex + 1 );
        if ( endIndex > timeData.n - 1 ) endIndex = timeData.n - 1;
        if ( endIndex < timeData.n - 1 && timeData.v( endIndex ) < endMillis ) endIndex++;

        int size = endIndex - startIndex + 1;

        for ( int p = 0; p < countParticles; p++ )
        {
            this.firstData.put( p, ( countTimes * p + startIndex ) );
            this.countData.put( p, size );
        }

    }

    public void displayTimeRange( TimeStamp startTime, TimeStamp endTime )
    {
        this.displayTimeRange( startTime.toPosixMillis( ), endTime.toPosixMillis( ) );
    }

    @Override
    public void paintTo( GL2 gl, GlimpseBounds bounds, Axis2D axis )
    {
        if ( startIndex == 0 && endIndex == 0 ) return;
        if ( startIndex == countParticles - 1 && endIndex == countParticles - 1 ) return;

        if ( !glInitialized )
        {
            // create a new device buffer handle
            int[] bufferHandle = new int[1];
            gl.glGenBuffers( 1, bufferHandle, 0 );
            glBufferHandleData = bufferHandle[0];

            // copy data from the host buffer into the device buffer
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, glBufferHandleData );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, countTimes * countParticles * 2 * BYTES_PER_FLOAT, positionData.rewind( ), GL2.GL_STATIC_DRAW );

            if ( colorData != null )
            {
                // create a new device buffer handle
                int[] bufferHandleColor = new int[1];
                gl.glGenBuffers( 1, bufferHandleColor, 0 );
                glBufferHandleColor = bufferHandleColor[0];

                // copy data from the host buffer into the device buffer
                gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, glBufferHandleColor );
                gl.glBufferData( GL2.GL_ARRAY_BUFFER, countTimes * countParticles * 4 * BYTES_PER_FLOAT, colorData.rewind( ), GL2.GL_STATIC_DRAW );

            }

            glInitialized = true;

            // we no longer need the buffers once data is loaded onto gpu
            positionData = null;
            colorData = null;
        }

        gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );
        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, glBufferHandleData );
        gl.glVertexPointer( 2, GL2.GL_FLOAT, 0, 0 );

        if ( glBufferHandleColor != -1 )
        {
            gl.glEnableClientState( GL2.GL_COLOR_ARRAY );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, glBufferHandleColor );
            gl.glColorPointer( 4, GL2.GL_FLOAT, 0, 0 );
        }
        else
        {
            GlimpseColor.glColor( gl, color );
        }

        gl.glLineWidth( lineWidth );

        firstData.rewind( );
        countData.rewind( );
        gl.glMultiDrawArrays( GL.GL_LINE_STRIP, firstData, countData, countParticles );
    }
}
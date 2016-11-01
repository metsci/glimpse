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

import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_BYTE;
import static javax.media.opengl.GL.GL_FLOAT;

import java.nio.IntBuffer;
import java.util.Arrays;

import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLStreamingBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.shader.line.ColorLinePath;
import com.metsci.glimpse.support.shader.line.ColorLineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.line.LineUtils;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;
import com.metsci.glimpse.util.primitives.LongsArray;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * <p>Displays a static set of tracks with associated timestamp, x position, and y position. Unlike
 * {@link TrackPainter}, each track  must have an xy position for the same set of timestamps. This
 * allows StaticTrackPainter to be more efficient in this case.</p>
 *
 * <p>The set of tracks cannot be modified once StaticTrackPainter is constructed, but a custom time slice of
 * the particles may be displayed via {@code StaticTrackPainter#displayTimeRange(long, long)}.</p>
 *
 * @author ulman
 *
 */
public class StaticTrackPainter extends GlimpsePainterBase
{
    protected IntBuffer firstData;
    protected IntBuffer countData;
    protected LongsArray timeData;
    protected int countTimes;
    protected int countParticles;

    protected TimeStamp startTime;
    protected TimeStamp endTime;

    protected int startIndex;
    protected int endIndex;

    protected ColorLineProgram prog;
    protected ColorLinePath path;
    protected LineStyle style;

    protected FlatColorProgram flatProg;

    protected static long[] toLongArray( TimeStamp[] time )
    {
        long[] array = new long[time.length];

        for ( int t = 0; t < time.length; t++ )
        {
            array[t] = time[t].toPosixMillis( );
        }

        return array;
    }

    public StaticTrackPainter( TimeStamp[] time, float[][] xPositions, float[][] yPositions, float[][][] colors )
    {
        this( toLongArray( time ), xPositions, yPositions, colors );
    }

    public StaticTrackPainter( TimeStamp[] time, float[][] xPositions, float[][] yPositions )
    {
        this( toLongArray( time ), xPositions, yPositions );
    }

    public StaticTrackPainter( long[] time, float[][] xPositions, float[][] yPositions )
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
    public StaticTrackPainter( long[] time, float[][] xPositions, float[][] yPositions, float[][][] colors )
    {
        this.prog = new ColorLineProgram( );
        this.style = new LineStyle( );
        this.path = new ColorLinePath( );

        this.flatProg = new FlatColorProgram( );

        this.countTimes = time.length;
        this.countParticles = xPositions.length;

        this.timeData = new LongsArray( time );

        // load data into array particle-wise (x/y positions for each particle are contiguous)
        for ( int p = 0; p < countParticles; p++ )
        {
            for ( int t = 0; t < countTimes; t++ )
            {
                if ( t == 0 )
                {
                    this.path.moveTo( xPositions[p][t], yPositions[p][t], colors[p][t] );
                }
                else
                {
                    this.path.lineTo( xPositions[p][t], yPositions[p][t], colors[p][t] );
                }
            }
        }

        this.firstData = IntBuffer.allocate( countParticles );
        this.countData = IntBuffer.allocate( countParticles );

        for ( int p = 0; p < countParticles; p++ )
        {
            this.countData.put( 0 );
            this.firstData.put( 0 );
        }
    }

    public LineStyle getLineStyle( )
    {
        return this.style;
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

        // each particle contains countTimes+1 vertices because of the placeholder vertex added by LinePathData
        for ( int p = 0; p < countParticles; p++ )
        {
            this.firstData.put( p, ( ( countTimes + 1 ) * p + startIndex ) );
            // account for two phantom vertices in the line strip (see LinePathData)
            this.countData.put( p, size + 2 );
        }

    }

    public void displayTimeRange( TimeStamp startTime, TimeStamp endTime )
    {
        this.displayTimeRange( startTime.toPosixMillis( ), endTime.toPosixMillis( ) );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = requireAxis2D( context );
        double ppvAspectRatio = LineUtils.ppvAspectRatio( axis );
        GL3 gl = context.getGL( ).getGL3( );

        if ( startIndex == 0 && endIndex == 0 ) return;
        if ( startIndex == countParticles - 1 && endIndex == countParticles - 1 ) return;

        firstData.rewind( );
        countData.rewind( );

        GLUtils.enableStandardBlending( gl );
        prog.begin( gl );
        try
        {
            prog.setAxisOrtho( gl, axis );
            prog.setViewport( gl, bounds );
            prog.setStyle( gl, style );

            gl.glBindBuffer( GL_ARRAY_BUFFER, path.xyVbo( gl ).buffer( gl ) );
            gl.glVertexAttribPointer( prog.handles( gl ).inXy, 2, GL_FLOAT, false, 0, path.xyVbo( gl ).sealedOffset( ) );

            gl.glBindBuffer( GL_ARRAY_BUFFER, path.flagsVbo( gl ).buffer( gl ) );
            gl.glVertexAttribIPointer( prog.handles( gl ).inFlags, 1, GL_BYTE, 0, path.flagsVbo( gl ).sealedOffset( ) );

            GLStreamingBuffer mileageVbo = ( style.stippleEnable ? path.mileageVbo( gl, ppvAspectRatio ) : path.rawMileageVbo( gl ) );
            gl.glBindBuffer( GL_ARRAY_BUFFER, mileageVbo.buffer( gl ) );
            gl.glVertexAttribPointer( prog.handles( gl ).inMileage, 1, GL_FLOAT, false, 0, mileageVbo.sealedOffset( ) );

            gl.glBindBuffer( GL_ARRAY_BUFFER, path.rgbaVbo( gl ).buffer( gl ) );
            gl.glVertexAttribPointer( prog.handles( gl ).inRgba, 4, GL_FLOAT, false, 0, path.rgbaVbo( gl ).sealedOffset( ) );

            gl.glMultiDrawArrays( GL3.GL_LINE_STRIP_ADJACENCY, firstData, countData, countParticles );

        }
        finally
        {
            prog.end( gl );
            GLUtils.disableBlending( gl );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        this.prog.dispose( gl );
        this.path.dispose( gl );
    }
}
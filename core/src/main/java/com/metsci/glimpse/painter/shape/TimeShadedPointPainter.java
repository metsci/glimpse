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
package com.metsci.glimpse.painter.shape;

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * A Scatterplot point painter which associates a time with each data point. A subset of
 * the points can be displayed based on these time values.
 *
 * @author ulman
 */
public class TimeShadedPointPainter extends ShadedPointPainter
{
    protected FloatBuffer timeAttributeBuffer;

    protected float startTime = Float.NEGATIVE_INFINITY;
    protected float endTime = Float.POSITIVE_INFINITY;

    protected int startIndex;
    protected int endIndex;

    public TimeShadedPointPainter( Axis1D colorAxis, Axis1D sizeAxis ) throws IOException
    {
        super( colorAxis, sizeAxis );
    }

    /**
     * Sets the selected range of times which will be displayed by this painter. The Epoch
     * argument is used to convert from absolute TimeStamps to relative times. It is generally
     * provided by a {@ StackedTimePlot2D}. The same Epoch should be used to fill the point
     * time values in the data buffer passed to {@code #useTimeAttribData(FloatBuffer)}.
     */
    public void displayTimeRange( Epoch epoch, TimeStamp startTime, TimeStamp endTime )
    {
        displayTimeRange( epoch.fromTimeStamp( startTime ), epoch.fromTimeStamp( endTime ) );
    }

    /**
     * Sets the selected range of times which will be displayed by this painter. Times should generally
     * be provided via {@code Epoch#fromTimeStamp(TimeStamp)}.
     */
    public void displayTimeRange( double startTime, double endTime )
    {
        painterLock.lock( );
        try
        {
            this.startTime = ( float ) startTime;
            this.endTime = ( float ) endTime;

            updateSelectedTime( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    /**
     * Assigns a time value to each point painted by the time painter. Points
     * must be added in increasing time order for time selection to function.
     */
    public void useTimeAttribData( FloatBuffer attributeBuffer )
    {
        painterLock.lock( );
        try
        {
            this.timeAttributeBuffer = attributeBuffer;
            this.updateSelectedTime( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    @Override
    public void useVertexPositionData( FloatBuffer positionBuffer )
    {
        painterLock.lock( );
        try
        {
            super.useVertexPositionData( positionBuffer );
            updateSelectedTime( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    @Override
    public void useColorAttribData( FloatBuffer attributeBuffer )
    {
        painterLock.lock( );
        try
        {
            this.program.setColorData( attributeBuffer );
            this.setVariablePointColor0( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    @Override
    public void useSizeAttribData( FloatBuffer attributeBuffer )
    {
        painterLock.lock( );
        try
        {
            this.program.setSizeData( attributeBuffer );
            this.setVariablePointColor0( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    protected void updateSelectedTime( )
    {
        if ( timeAttributeBuffer != null )
        {
            startIndex = binarySearch0( timeAttributeBuffer, 0, timeAttributeBuffer.limit( ), startTime );
            if ( startIndex < 0 )
            {
                // index of the first element > startTime (inclusive lower index)
                startIndex = - ( startIndex + 1 );
                if ( startIndex >= timeAttributeBuffer.limit( ) )
                {
                    startIndex = 0;
                    endIndex = 0;
                    return;
                }
            }
            else
            {
                startIndex = firstIndex0( timeAttributeBuffer, startIndex, startTime );
            }

            endIndex = binarySearch0( timeAttributeBuffer, 0, timeAttributeBuffer.limit( ), endTime );
            if ( endIndex < 0 )
            {
                // index of the first element > endTime (exclusive upper index)
                endIndex = - ( endIndex + 1 );
                if ( endIndex <= 0 )
                {
                    startIndex = 0;
                    endIndex = 0;
                    return;
                }
            }
            else
            {
                endIndex = lastIndex0( timeAttributeBuffer, endIndex, endTime ) + 1;
            }
        }
        else
        {
            startIndex = 0;
            endIndex = vertexCount;
        }
    }

    private static int lastIndex0( FloatBuffer b, int startIndex, float key )
    {
        for ( int i = startIndex; i < b.limit( ); i++ )
        {
            if ( b.get( i ) != key ) return i - 1;
        }

        return b.limit( ) - 1;
    }

    private static int firstIndex0( FloatBuffer b, int startIndex, float key )
    {
        for ( int i = startIndex; i >= 0; i-- )
        {
            if ( b.get( i ) != key ) return i + 1;
        }

        return 0;
    }

    // java.util.Arrays.binarySearch0() modified to work with FloatBuffer
    private static int binarySearch0( FloatBuffer b, int fromIndex, int toIndex, float key )
    {
        int low = fromIndex;
        int high = toIndex - 1;

        while ( low <= high )
        {
            int mid = ( low + high ) >>> 1;
            float midVal = b.get( mid );

            if ( midVal < key )
                low = mid + 1;
            else if ( midVal > key )
                high = mid - 1;
            else
                return mid; // key found
        }
        return - ( low + 1 ); // key not found.
    }

    @Override
    protected void drawArrays( GL gl )
    {
        gl.glDrawArrays( GL.GL_POINTS, startIndex, endIndex - startIndex );
    }

}

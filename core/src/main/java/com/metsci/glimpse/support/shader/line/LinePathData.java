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
package com.metsci.glimpse.support.shader.line;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.support.shader.line.LineUtils.*;
import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class LinePathData
{

    /**
     * Mask for the CONNECT bit, which indicates whether to draw a line segment
     * from the previous vertex to the current vertex.
     * <p>
     * Expressed as an {@code int} because bitwise operations with {@code byte}s
     * are extremely error-prone in Java.
     */
    public static final int FLAGS_CONNECT = 1 << 0;

    /**
     * Mask for the JOIN bit, which indicates whether to use a join (e.g. miter)
     * at the current vertex.
     * <p>
     * Expressed as an {@code int} because bitwise operations with {@code byte}s
     * are extremely error-prone in Java.
     */
    public static final int FLAGS_JOIN = 1 << 1;

    /**
     * The index of the first vertex in the current line-strip. Assigned a new
     * value when {@link #moveTo(float, float, float)} is called. Set to -1 when
     * when {@link #closeLoop()} is called.
     * <p>
     * Note the "first" vertex actually comes after the leading phantom vertex.
     */
    protected int stripFirst;

    /**
     * Contains two floats (x and y) for each vertex, indicating the position of
     * the vertex.
     */
    protected FloatBuffer xyBuffer;

    /**
     * Contains one byte for each vertex, containing bit-flags:
     * <ul>
     * <li>Bit 0: CONNECT  (Least Significant Bit)
     * <li>Bit 1: JOIN
     * </ul>
     * <p>
     * Example vertex flags for a line-strip:
     * <pre>
     *   Vertex:   (   [----*----*----]
     *   Flags:    0   0   CJ   CJ    C </pre>
     * And for a loop:
     * <pre>
     *   Vertex:   (   [----*----*----]   )
     *   Flags:    0   J   CJ   CJ   CJ   0 </pre>
     * Where:
     * <pre>
     *   ( = Leading Phantom Vertex
     *   [ = Strip First Vertex
     *   * = Normal Vertex
     *   ] = Strip Last Vertex
     *   ) = Trailing Phantom Vertex </pre>
     * Note the following about the line-strip:
     * <ul>
     * <li>The strip's first and last vertices do not have their JOIN flags set
     * <li>The strip's leading phantom vertex is just a placeholder
     * <li>The strip's trailing phantom vertex is just a placeholder -- in fact, if there
     *     are vertices after this strip, then this strip has no trailing phantom vertex,
     *     and the next strip's leading phantom vertex is used as placeholder instead
     * </ul>
     * Note the following about the loop:
     * <ul>
     * <li>The loop's first and last vertices have their JOIN flags set
     * <li>The loop's first and last vertices have the same xy
     * <li>The loop's leading phantom vertex and second-to-last vertex have the same xy
     * <li>The loop's trailing phantom vertex and second vertex have the same xy
     * </ul>
     */
    protected ByteBuffer flagsBuffer;

    /**
     * Contains one float for each vertex, indicating the cumulative distance
     * to the vertex from the start of the connected line strip.
     * <p>
     * Mileage is dependent on ppv-aspect-ratio. The ppv-aspect-ratio that was
     * used to compute the current mileage values is stored in {@link #mileagePpvAspectRatio}.
     * <p>
     * Only needed when stippling is enabled, and can be expensive to compute.
     * Therefore, values are not updated until specifically requested by calling
     * {@link #updateMileage(double, double)}.
     * <p>
     * A vertex that starts a new line strip will have an initial-mileage value.
     * These initial-mileages are stored in this buffer -- they are <em>read</em>,
     * rather than written, by {@link #updateMileage(double, double)}.
     */
    protected FloatBuffer mileageBuffer;

    /**
     * The first index at which mileage values need to be updated.
     */
    protected int mileageValidCount;

    /**
     * The ppv-aspect-ratio used to populate the first {@link #mileageValidCount}
     * values of {@link #mileageBuffer}.
     */
    protected double mileagePpvAspectRatio;

    public LinePathData( int initialNumVertices )
    {
        this.stripFirst = -1;

        this.xyBuffer = newDirectFloatBuffer( 2 * initialNumVertices );
        this.flagsBuffer = newDirectByteBuffer( 1 * initialNumVertices );
        this.mileageBuffer = newDirectFloatBuffer( 1 * initialNumVertices );

        this.mileageValidCount = 0;
        this.mileagePpvAspectRatio = Double.NaN;
    }

    public void moveTo( float x, float y, float stripInitialMileage )
    {
        // Rewrite flags of previous vertex, to end previous strip
        int stripLast = this.flagsBuffer.position( ) - 1;
        if ( stripLast >= 0 )
        {
            byte flagsLast = this.flagsBuffer.get( 1 * stripLast );
            this.flagsBuffer.put( 1 * stripLast, ( byte ) ( flagsLast & ~FLAGS_JOIN ) );
        }

        // Leading phantom vertex, in case this strip is at the start of the VBO,
        // or turns out to be a loop
        this.appendVertex( x, y, 0, stripInitialMileage );

        // Index of first vertex in strip
        this.stripFirst = this.flagsBuffer.position( );

        // First vertex in strip
        this.appendVertex( x, y, 0, stripInitialMileage );
    }

    public void lineTo( float x, float y )
    {
        if ( this.stripFirst < 0 )
        {
            throw new RuntimeException( "No current line-strip -- moveTo() must be called after instantiation, and after each call to closeLoop()" );
        }

        this.appendVertex( x, y, FLAGS_CONNECT | FLAGS_JOIN );
    }

    /**
     * After calling this method, client code must next call {@link #moveTo(float, float, float)},
     * before calling either {@link #lineTo(float, float)} or {@link #closeLoop()} again.
     */
    public void closeLoop( )
    {
        if ( this.stripFirst < 0 )
        {
            throw new RuntimeException( "No current line-strip -- moveTo() must be called after instantiation, and after each call to closeLoop()" );
        }

        // Read last vertex in strip
        int stripLast = this.flagsBuffer.position( ) - 1;
        float xLast = this.xyBuffer.get( 2 * stripLast + 0 );
        float yLast = this.xyBuffer.get( 2 * stripLast + 1 );

        // Rewrite position of leading phantom vertex
        int stripLeader = this.stripFirst - 1;
        this.xyBuffer.put( 2 * stripLeader + 0, xLast );
        this.xyBuffer.put( 2 * stripLeader + 1, yLast );

        // Rewrite flags of first vertex in strip
        this.flagsBuffer.put( 1 * stripFirst, ( byte ) FLAGS_JOIN );

        // Append loop-closing vertex
        float xFirst = this.xyBuffer.get( 2 * stripFirst + 0 );
        float yFirst = this.xyBuffer.get( 2 * stripFirst + 1 );
        this.appendVertex( xFirst, yFirst, FLAGS_CONNECT | FLAGS_JOIN );

        // Append trailing phantom vertex
        float xSecond = this.xyBuffer.get( 2 * stripFirst + 2 );
        float ySecond = this.xyBuffer.get( 2 * stripFirst + 3 );
        this.appendVertex( xSecond, ySecond, 0 );

        // Next vertex must start a new strip
        this.stripFirst = -1;
    }

    protected void appendVertex( float x, float y, int flags )
    {
        // Use a placeholder for mileage -- the first vertex in each strip should be
        // given a real mileage, but others will be computed later in updateMileage
        this.appendVertex( x, y, flags, 0f );
    }

    protected void appendVertex( float x, float y, int flags, float mileage )
    {
        this.xyBuffer = grow2f( this.xyBuffer, x, y );
        this.flagsBuffer = grow1b( this.flagsBuffer, ( byte ) flags );
        this.mileageBuffer = grow1f( this.mileageBuffer, mileage );
    }

    public void clear( )
    {
        this.stripFirst = -1;

        this.xyBuffer.clear( );
        this.flagsBuffer.clear( );
        this.mileageBuffer.clear( );

        this.mileageValidCount = 0;
        this.mileagePpvAspectRatio = Double.NaN;
    }

    /**
     * Deallocates buffers.
     * <p>
     * <em>This object must not be used again after this method has been called.</em>
     */
    public void dispose( )
    {
        deallocateDirectBuffers( this.xyBuffer, this.flagsBuffer, this.mileageBuffer );
        this.xyBuffer = null;
        this.flagsBuffer = null;
        this.mileageBuffer = null;
    }

    public int numVertices( )
    {
        return this.flagsBuffer.position( );
    }

    public FloatBuffer xyBuffer( )
    {
        return flipped( this.xyBuffer );
    }

    public ByteBuffer flagsBuffer( )
    {
        return flipped( this.flagsBuffer );
    }

    public FloatBuffer mileageBuffer( )
    {
        return flipped( this.mileageBuffer );
    }

    public int updateMileage( double ppvAspectRatio, double ppvAspectRatioChangeThreshold )
    {
        // If any relevant values are NaN, all inequalities will return false, so keepPpvAspectRatio will be false
        boolean keepPpvAspectRatio = ( mileagePpvAspectRatio / ppvAspectRatioChangeThreshold <= ppvAspectRatio && ppvAspectRatio <= mileagePpvAspectRatio * ppvAspectRatioChangeThreshold );
        if ( !keepPpvAspectRatio )
        {
            this.mileageValidCount = 0;
            this.mileagePpvAspectRatio = ppvAspectRatio;
        }

        this.mileageBuffer.position( this.mileageValidCount );

        updateMileageBuffer( flipped( this.xyBuffer ),
                             flipped( this.flagsBuffer ),
                             this.mileageBuffer,
                             true,
                             this.mileagePpvAspectRatio );

        int oldValidCount = this.mileageValidCount;
        this.mileageValidCount = this.mileageBuffer.position( );
        return ( this.mileageValidCount - oldValidCount );
    }

    public static void updateMileageBuffer( FloatBuffer xyBuffer,
                                            ByteBuffer flagsBuffer,
                                            FloatBuffer mileageBuffer,
                                            boolean useInitialMileages,
                                            double ppvAspectRatio )
    {
        int firstVertex = mileageBuffer.position( );
        xyBuffer.position( 2 * firstVertex );
        flagsBuffer.position( 1 * firstVertex );

        float x;
        float y;
        float mileage;

        if ( firstVertex > 0 )
        {
            // If we're starting partway through, initialize loop vars based on last valid values
            int i = firstVertex - 1;
            x = xyBuffer.get( 2*i + 0 );
            y = xyBuffer.get( 2*i + 1 );
            mileage = mileageBuffer.get( 1*i );
        }
        else
        {
            // If we're starting from the beginning, the first vertex must be non-connected,
            // so these initial values don't matter
            x = 0;
            y = 0;
            mileage = 0;
        }

        while ( flagsBuffer.hasRemaining( ) )
        {
            float xNew = xyBuffer.get( );
            float yNew = xyBuffer.get( );
            byte flagsNew = flagsBuffer.get( );

            boolean connect = ( ( flagsNew & FLAGS_CONNECT ) != 0 );
            if ( connect )
            {
                mileage += distance( x, y, xNew, yNew, ppvAspectRatio );
                mileageBuffer.put( mileage );
            }
            else if ( useInitialMileages )
            {
                // The intention is to run this block if the vertex starts a new strip -- because
                // such a vertex has an initial-mileage value. However, this block will also run
                // if the vertex is the trailing phantom vertex after a loop (because CONNECT will
                // be false). Such a vertex will have only a placeholder mileage. This works out
                // okay, though, because the shader doesn't use mileage when CONNECT is false.

                // The value in mileageBuffer here is the strip's initial mileage, so get the
                // existing value INSTEAD OF putting a new one
                mileage = mileageBuffer.get( );
            }
            else
            {
                mileage = 0;
                mileageBuffer.put( mileage );
            }

            x = xNew;
            y = yNew;
        }
    }

}

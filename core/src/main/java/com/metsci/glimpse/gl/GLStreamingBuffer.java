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
package com.metsci.glimpse.gl;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.gl.util.GLUtils.*;
import static java.lang.Math.*;
import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

/**
 * Represents a device buffer that needs to be re-written frequently, and
 * frequently read for rendering as well. Helpful for migrating away from
 * immediate-mode rendering.
 * <p>
 * Uses the recommended approach for
 * <a href="https://www.opengl.org/wiki/Buffer_Object_Streaming">buffer object streaming</a>.
 * Specifically:
 * <ol>
 * <li>Allocate a large block of device-buffer memory
 * <li>Write to a small section of the block
 * <li>Render using the written section
 * <li>Write to the <i>next</i> section of the block
 * <li>Render using the newly written section
 * <li>Repeat
 * <li>Once the whole block has been used, allocate a new block
 * </ol>
 * When a new block is allocated, the old block is dereferenced, but not
 * actually deleted until pending render operations are done with it. This
 * approach avoids driver-level synchronization, because the new block can
 * be written to without waiting for pending render operations to finish.
 * <p>
 * The cost is an increase in device-memory allocations. However, allocating
 * in large blocks keeps the frequency of allocations down. Furthermore, in
 * most cases, successive blocks will be the same size, so it is reasonable
 * to assume that old blocks will be recycled by the driver's memory manager.
 * For more info see the explanation <a href="https://www.opengl.org/discussion_boards/showthread.php/170118-VBOs-strangely-slow?p=1197780#post1197780">here</a>.
 * <p>
 * Expected usage looks something like this:
 * <pre>
 *    // At init-time
 *
 *    GLStreamingBuffer xyVbo = new GLStreamingBuffer( GL_ARRAY_BUFFER, GL_STREAM_DRAW, 100 );
 *    ...
 *
 *    // At render-time
 *
 *    int maxVertices = 10000;
 *    int floatsPerVertex = 2;
 *    FloatBuffer xyBuffer = xyVbo.mapFloats( gl, floatsPerVertex * maxVertices );
 *
 *    xyBuffer.put( x0 ).put( y0 );
 *    xyBuffer.put( x1 ).put( y1 );
 *    ...
 *
 *    int numVertices = xyBuffer.position( ) / floatsPerVertex;
 *    xyVbo.seal( gl );
 *
 *    gl.glBindBuffer( xyVbo.target, xyVbo.buffer( ) );
 *    gl.glVertexAttribPointer( ..., xyVbo.sealedOffset( ) );
 *    gl.glDrawArrays( ..., 0, numVertices );
 * </pre>
 */
public class GLStreamingBuffer
{

    /**
     * Passed to {@link GL#glBufferData(int, long, java.nio.Buffer, int)}
     * when allocating buffer space
     */
    public final int usage;

    /**
     * How many times larger than the mapped size to allocate, when
     * we have to allocate new space for the buffer -- larger factors
     * result in less frequent reallocs, but higher memory use
     */
    protected final int blockSizeFactor;

    /**
     * Zero until the first call to {@link #buffer(GL)}
     */
    protected int buffer;

    /**
     * The byte size of the space currently allocated for the buffer
     */
    protected long blockSize;

    /**
     * The byte offset of the most recently sealed range, or -1 if
     * no ranges have been sealed yet
     */
    protected long sealedOffset;

    /**
     * When mapped: the byte offset of the mapped range
     * When sealed: the byte offset of the next range to be mapped
     */
    protected long mappedOffset;

    /**
     * When mapped: the byte size of the mapped range
     * When sealed: zero
     */
    protected long mappedSize;

    public GLStreamingBuffer( int usage, int blockSizeFactor )
    {
        this.usage = usage;
        this.blockSizeFactor = blockSizeFactor;

        this.buffer = 0;
        this.blockSize = 0;
        this.sealedOffset = -1;
        this.mappedOffset = 0;
        this.mappedSize = 0;
    }

    /**
     * Returns the buffer handle, as created by e.g. {@link GL#glGenBuffers(int, java.nio.IntBuffer)}.
     */
    public int buffer( GL gl )
    {
        if ( this.buffer == 0 )
        {
            this.buffer = genBuffer( gl );
        }

        return this.buffer;
    }

    /**
     * Returns the offset into {@link #buffer()} of the most recently sealed range -- e.g. for use
     * with {@link javax.media.opengl.GL2ES2#glVertexAttribPointer(int, int, int, boolean, int, long)}.
     * <p>
     * Returns -1 if {@link #seal(GL)} has not been called yet.
     */
    public long sealedOffset( )
    {
        return this.sealedOffset;
    }

    /**
     * Convenience method that maps a region, copies data into it using {@link FloatBuffer#put(FloatBuffer)},
     * and then seals the region.
     * <p>
     * Note that this will modify the buffer's {@code position}.
     */
    public void setFloats( GL gl, FloatBuffer floats )
    {
        FloatBuffer mapped = this.mapFloats( gl, floats.remaining( ) );
        mapped.put( floats );
        this.seal( gl );
    }

    /**
     * Convenience method that maps a region, copies data into it using {@link DoubleBuffer#put(DoubleBuffer)},
     * and then seals the region.
     * <p>
     * Note that this will modify the buffer's {@code position}.
     */
    public void setDoubles( GL gl, DoubleBuffer doubles )
    {
        DoubleBuffer mapped = this.mapDoubles( gl, doubles.remaining( ) );
        mapped.put( doubles );
        this.seal( gl );
    }

    /**
     * Convenience method that maps a region, copies data into it using {@link IntBuffer#put(IntBuffer)},
     * and then seals the region.
     * <p>
     * Note that this will modify the buffer's {@code position}.
     */
    public void setInts( GL gl, IntBuffer ints )
    {
        IntBuffer mapped = this.mapInts( gl, ints.remaining( ) );
        mapped.put( ints );
        this.seal( gl );
    }

    /**
     * Convenience method that maps a region, copies data into it using {@link ByteBuffer#put(ByteBuffer)},
     * and then seals the region.
     * <p>
     * Note that this will modify the buffer's {@code position}.
     */
    public void setBytes( GL gl, ByteBuffer bytes )
    {
        ByteBuffer mapped = this.mapBytes( gl, bytes.remaining( ) );
        mapped.put( bytes );
        this.seal( gl );
    }

    /**
     * Convenience wrapper around {@link #mapBytes(GL, long)} -- converts {@code numFloats} to a
     * byte count, and converts the returned buffer to a {@link FloatBuffer}.
     */
    public FloatBuffer mapFloats( GL gl, long numFloats )
    {
        return this.mapBytes( gl, numFloats * SIZEOF_FLOAT ).asFloatBuffer( );
    }

    /**
     * Convenience wrapper around {@link #mapBytes(GL, long)} -- converts {@code numDoubles} to a
     * byte count, and converts the returned buffer to a {@link DoubleBuffer}.
     */
    public DoubleBuffer mapDoubles( GL gl, long numDoubles )
    {
        return this.mapBytes( gl, numDoubles * SIZEOF_DOUBLE ).asDoubleBuffer( );
    }

    /**
     * Convenience wrapper around {@link #mapBytes(GL, long)} -- converts {@code numInts} to a
     * byte count, and converts the returned buffer to an {@link IntBuffer}.
     */
    public IntBuffer mapInts( GL gl, long numInts )
    {
        return this.mapBytes( gl, numInts * SIZEOF_INT ).asIntBuffer( );
    }

    /**
     * Returns a buffer representing host memory owned by the graphics driver. The returned buffer
     * should be treated as <em>write-only</em>. After writing to the buffer, call {@link #seal(GL)}
     * to indicate to the driver that the newly written contents are ready to be pushed to the device.
     * <p>
     * It is okay to request more bytes than will actually be written, as long as you then avoid
     * actually trying to use the values in the unwritten region (e.g. by passing the appropriate
     * number of vertices to {@link GL#glDrawArrays(int, int, int)}.
     * <p>
     * Note, however, that the driver may push the entire mapped region to the device -- so while
     * {@code numBytes} is an upper bound, it should be a reasonably tight upper bound.
     */
    public ByteBuffer mapBytes( GL gl, long numBytes )
    {
        if ( this.mappedSize != 0 )
        {
            throw new RuntimeException( "Buffer is already mapped -- must be sealed before being mapped again" );
        }

        gl.glBindBuffer( GL_ARRAY_BUFFER, this.buffer( gl ) );

        // Seems recommended to map in multiples of 64 ... I guess for alignment reasons?
        this.mappedSize = nextMultiple( numBytes, 64 );

        if ( this.mappedOffset + this.mappedSize > this.blockSize )
        {
            // Allocate a block large enough that we don't have to allocate too frequently
            this.blockSize = max( this.blockSize, this.blockSizeFactor * this.mappedSize );

            // Allocate new space, and orphan the old space
            gl.glBufferData( GL_ARRAY_BUFFER, this.blockSize, null, this.usage );

            // Start at the beginning of the new space
            this.mappedOffset = 0;
        }

        return gl.glMapBufferRange( GL_ARRAY_BUFFER, this.mappedOffset, this.mappedSize, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_RANGE_BIT | GL_MAP_UNSYNCHRONIZED_BIT );
    }

    /**
     * Returns the smallest multiple of b that is greater than or equal to a.
     */
    protected static long nextMultiple( long a, long b )
    {
        return ( b * ( ( ( a - 1 ) / b ) + 1 ) );
    }

    /**
     * Unmaps the currently mapped range. After this, the sealed range can be read by GL calls.
     */
    public void seal( GL gl )
    {
        if ( this.mappedSize == 0 )
        {
            throw new RuntimeException( "Buffer is not currently mapped" );
        }

        gl.glBindBuffer( GL_ARRAY_BUFFER, this.buffer );
        gl.glUnmapBuffer( GL_ARRAY_BUFFER );

        this.sealedOffset = this.mappedOffset;
        this.mappedOffset += this.mappedSize;
        this.mappedSize = 0;
    }

    /**
     * Deletes the buffer (unmapping first, if necessary), and resets this object to the way
     * it was before {@link #mapBytes(GL, long)} was first called.
     * <p>
     * This object can be safely reused after being disposed, but in most cases there is no
     * significant advantage to doing so.
     */
    public void dispose( GL gl )
    {
        if ( this.mappedSize != 0 )
        {
            gl.glBindBuffer( GL_ARRAY_BUFFER, this.buffer );
            gl.glUnmapBuffer( GL_ARRAY_BUFFER );
            this.mappedSize = 0;
        }

        if ( this.buffer != 0 )
        {
            deleteBuffers( gl, this.buffer );
            this.buffer = 0;
        }

        this.blockSize = 0;
        this.sealedOffset = -1;
        this.mappedOffset = 0;
    }

}

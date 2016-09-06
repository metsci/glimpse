package com.metsci.glimpse.support.line.util;

import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.metsci.glimpse.gl.util.GLUtils.genBuffer;
import static java.lang.Math.max;
import static javax.media.opengl.GL.GL_MAP_UNSYNCHRONIZED_BIT;
import static javax.media.opengl.GL.GL_MAP_WRITE_BIT;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

/**
 * Represents a buffer object, and provides methods that (should) allow the
 * buffer to be mapped for convenient writing without incurring surprising
 * performance penalties.
 * <p>
 * Follows advice from Rob Barris in March 2010 (which can be found
 * <a href="https://www.opengl.org/discussion_boards/showthread.php/170118-VBOs-strangely-slow?p=1197780#post1197780">here</a>)
 * on how to avoid driver-level synchronization. However, synchronization is
 * ultimately up to the driver, and the approach taken here is not guaranteed
 * to work on every driver.
 * <p>
 * Expected usage looks something like this:
 * <pre>
 *    // At init-time
 *
 *    MappableBuffer xyVbo = new MappableBuffer( GL_ARRAY_BUFFER, GL_STREAM_DRAW, 100 );
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
public class MappableBuffer
{

    public final int target;

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
     * Assigned only once, the first time {@link #map(GL, long)} is
     * called
     */
    protected int buffer;

    /**
     * The byte size of the space currently allocated for the buffer
     */
    protected long blockSize;

    /**
     * The byte offset of the most recently sealed range
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


    public MappableBuffer( int target, int usage, int blockSizeFactor )
    {
        this.target = target;
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
     * <p>
     * Returns zero if none of the {@code map} methods (e.g. {@link #mapBytes(GL, long)}) have been
     * called yet.
     */
    public int buffer( )
    {
        return buffer;
    }

    /**
     * Returns the offset into {@link #buffer()} of the most recently sealed range -- e.g. for use
     * with {@link javax.media.opengl.GL2ES2#glVertexAttribPointer(int, int, int, boolean, int, long)}.
     * <p>
     * Returns -1 if {@link #seal(GL)} has not been called yet.
     */
    public long sealedOffset( )
    {
        return sealedOffset;
    }

    /**
     * Convenience wrapper around {@link #mapBytes(GL, long)} -- converts {@code numFloats} to a
     * byte count, and converts the returned buffer to a {@link FloatBuffer}.
     */
    public FloatBuffer mapFloats( GL gl, long numFloats )
    {
        return mapBytes( gl, numFloats * SIZEOF_FLOAT ).asFloatBuffer( );
    }

    /**
     * Returns a buffer representing host memory owned by the graphics driver. The returned buffer
     * should be treated as <em>write-only</em>. After writing to the buffer, call {@link #seal(GL)}
     * to indicate to the driver that the newly written contents are ready to be pushed to the device.
     *
     * It is okay to request more bytes than will actually be written, as long as you then avoid
     * actually trying to use the values in the unwritten region (e.g. by passing the appropriate
     * number of vertices to {@link GL#glDrawArrays(int, int, int)}.
     *
     * Note, however, that the driver may push the entire mapped region to the device -- so while
     * {@code numBytes} is just an upper bound, it should be a reasonably tight upper bound.
     */
    public ByteBuffer mapBytes( GL gl, long numBytes )
    {
        if ( mappedSize != 0 )
        {
            throw new RuntimeException( "Buffer is already mapped -- must be sealed before being mapped again" );
        }

        if ( buffer == 0 )
        {
            this.buffer = genBuffer( gl );
        }

        gl.glBindBuffer( target, buffer );

        // Seems recommended to map in multiples of 64 ... I guess for alignment reasons?
        this.mappedSize = nextMultiple( numBytes, 64 );

        if ( mappedOffset + mappedSize > blockSize )
        {
            // Allocate a block large enough that we don't have to allocate too frequently
            this.blockSize = max( blockSize, blockSizeFactor * mappedSize );

            // Allocate new space, and orphan the old space
            gl.glBufferData( target, blockSize, null, usage );

            // Start at the beginning of the new space
            this.mappedOffset = 0;
        }

        return gl.glMapBufferRange( target, mappedOffset, mappedSize, GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT );
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
        if ( mappedSize == 0 )
        {
            throw new RuntimeException( "Buffer is not currently mapped" );
        }

        gl.glBindBuffer( target, buffer );
        gl.glUnmapBuffer( target );

        this.sealedOffset = mappedOffset;
        this.mappedOffset += mappedSize;
        this.mappedSize = 0;
    }

}

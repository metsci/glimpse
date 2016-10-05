package com.metsci.glimpse.gl;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.gl.util.GLUtils.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2ES2.*;
import static javax.media.opengl.GL2ES3.*;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES3;

public class GLEditableBuffer
{

    public final int target;
    public final long numBytes;
    protected int buffer;

    protected final GLStreamingBuffer scratch;
    protected long mappedOffset;
    protected long mappedSize;


    public GLEditableBuffer( int target, long numBytes, int scratchBlockSizeFactor )
    {
        this.target = target;
        this.numBytes = numBytes;
        this.buffer = 0;

        this.scratch = new GLStreamingBuffer( GL_ARRAY_BUFFER, GL_STREAM_DRAW, scratchBlockSizeFactor );
        this.mappedOffset = 0;
        this.mappedSize = 0;
    }

    public int buffer( GL gl )
    {
        if ( this.buffer == 0 )
        {
            this.buffer = genBuffer( gl );
            gl.glBindBuffer( this.target, this.buffer );
            gl.glBufferData( this.target, this.numBytes, null, GL_STATIC_COPY );
        }

        return buffer;
    }

    public FloatBuffer mapFloats( GL gl, long firstFloat, long numFloats )
    {
        return this.mapBytes( gl, firstFloat * SIZEOF_FLOAT, numFloats * SIZEOF_FLOAT ).asFloatBuffer( );
    }

    public DoubleBuffer mapDoubles( GL gl, long firstDouble, long numDoubles )
    {
        return this.mapBytes( gl, firstDouble * SIZEOF_DOUBLE, numDoubles * SIZEOF_DOUBLE ).asDoubleBuffer( );
    }

    public IntBuffer mapInts( GL gl, long firstInt, long numInts )
    {
        return this.mapBytes( gl, firstInt * SIZEOF_INT, numInts * SIZEOF_INT ).asIntBuffer( );
    }

    public ByteBuffer mapBytes( GL gl, long firstByte, long numBytes )
    {
        this.mappedOffset = firstByte;
        this.mappedSize = numBytes;
        return this.scratch.mapBytes( gl, numBytes );
    }

    public void seal( GL2ES3 gl )
    {
        this.scratch.seal( gl );

        gl.glBindBuffer( GL_COPY_READ_BUFFER, this.scratch.buffer( gl ) );
        gl.glBindBuffer( GL_COPY_WRITE_BUFFER, this.buffer( gl ) );
        gl.glCopyBufferSubData( GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, this.scratch.sealedOffset( ), this.mappedOffset, this.mappedSize );

        this.mappedOffset = 0;
        this.mappedSize = 0;
    }

    public void dispose( GL gl )
    {
        this.scratch.dispose( gl );
        this.mappedOffset = 0;
        this.mappedSize = 0;

        if ( this.buffer != 0 )
        {
            deleteBuffers( gl, this.buffer );
            this.buffer = 0;
        }
    }

}

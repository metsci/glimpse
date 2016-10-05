package com.metsci.glimpse.gl;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.util.primitives.sorted.SortedInts;

public class GLEditableBuffer2
{

    protected ByteBuffer hBuffer;
    protected GLEditableBuffer dBuffer;
    protected final DirtyIndexSet dirtyRanges;


    public GLEditableBuffer2( int target, int numBytes, int scratchBlockSizeFactor )
    {
        this.hBuffer = newDirectByteBuffer( numBytes );
        this.dBuffer = new GLEditableBuffer( target, numBytes, scratchBlockSizeFactor );
        this.dirtyRanges = new DirtyIndexSet( );
    }

    public void overwriteFloats( int firstFloat, FloatBuffer floats )
    {
        this.dirtyRanges.add( firstFloat * SIZEOF_FLOAT, floats.remaining( ) * SIZEOF_FLOAT );

        FloatBuffer dest = this.hBuffer.duplicate( ).asFloatBuffer( );
        dest.position( firstFloat );
        dest.put( floats );
    }

    public void overwriteDoubles( int firstDouble, DoubleBuffer doubles )
    {
        this.dirtyRanges.add( firstDouble * SIZEOF_DOUBLE, doubles.remaining( ) * SIZEOF_DOUBLE );

        DoubleBuffer dest = this.hBuffer.duplicate( ).asDoubleBuffer( );
        dest.position( firstDouble );
        dest.put( doubles );
    }

    public void overwriteInts( int firstInt, IntBuffer ints )
    {
        this.dirtyRanges.add( firstInt * SIZEOF_INT, ints.remaining( ) * SIZEOF_INT );

        IntBuffer dest = this.hBuffer.duplicate( ).asIntBuffer( );
        dest.position( firstInt );
        dest.put( ints );
    }

    public void overwriteBytes( int firstByte, ByteBuffer bytes )
    {
        this.dirtyRanges.add( firstByte, bytes.remaining( ) );

        ByteBuffer dest = this.hBuffer.duplicate( );
        dest.position( firstByte );
        dest.put( bytes );
    }

    public int buffer( GL2ES3 gl )
    {
        // XXX: Tolerance should probably be higher
        this.dirtyRanges.coalesce( 1024 );

        SortedInts ranges = this.dirtyRanges.ranges( );
        for ( int i = 0; i < ranges.n( ); i += 2 )
        {
            int rangeStart = ranges.v( i + 0 );
            int rangeEnd = ranges.v( i + 1 );

            ByteBuffer rangeBytes = this.hBuffer.duplicate( );
            rangeBytes.limit( rangeEnd );
            rangeBytes.position( rangeStart );

            this.dBuffer.setBytes( gl, rangeStart, rangeBytes );
        }

        this.dirtyRanges.clear( );

        return this.dBuffer.buffer( gl );
    }

    public void dispose( GL gl )
    {
        deallocateDirectBuffers( this.hBuffer );
        this.hBuffer = null;

        this.dBuffer.dispose( gl );

        this.dirtyRanges.clear( );
    }

}

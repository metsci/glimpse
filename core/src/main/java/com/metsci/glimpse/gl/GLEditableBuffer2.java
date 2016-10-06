package com.metsci.glimpse.gl;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;

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
    protected final IntRangeSet dirtyRanges;


    public GLEditableBuffer2( int target, int numBytes, int scratchBlockSizeFactor )
    {
        this.hBuffer = newDirectByteBuffer( numBytes );
        this.dBuffer = new GLEditableBuffer( target, numBytes, scratchBlockSizeFactor );
        this.dirtyRanges = new IntRangeSet( );
    }

    public void growFloats( int additionalFloats )
    {
        this.hBuffer = ensureAdditionalCapacity( this.hBuffer, additionalFloats * SIZEOF_FLOAT, true );
    }

    public void growDoubles( int additionalDoubles )
    {
        this.hBuffer = ensureAdditionalCapacity( this.hBuffer, additionalDoubles * SIZEOF_DOUBLE, true );
    }

    public void growInts( int additionalInts )
    {
        this.hBuffer = ensureAdditionalCapacity( this.hBuffer, additionalInts * SIZEOF_INT, true );
    }

    public void growBytes( int additionalBytes )
    {
        this.hBuffer = ensureAdditionalCapacity( this.hBuffer, additionalBytes * SIZEOF_BYTE, true );
    }

    public void appendFloats( FloatBuffer floats )
    {
        int numBytes = floats.remaining( ) * SIZEOF_FLOAT;

        this.dirtyRanges.add( this.hBuffer.position( ), numBytes );

        this.growFloats( floats.remaining( ) );
        this.hBuffer.asFloatBuffer( ).put( floats );
        this.hBuffer.position( this.hBuffer.position( ) + numBytes );
    }

    public void appendDoubles( DoubleBuffer doubles )
    {
        int numBytes = doubles.remaining( ) * SIZEOF_DOUBLE;

        this.dirtyRanges.add( this.hBuffer.position( ), numBytes );

        this.growDoubles( doubles.remaining( ) );
        this.hBuffer.asDoubleBuffer( ).put( doubles );
        this.hBuffer.position( this.hBuffer.position( ) + numBytes );
    }

    public void appendInts( IntBuffer ints )
    {
        int numBytes = ints.remaining( ) * SIZEOF_INT;

        this.dirtyRanges.add( this.hBuffer.position( ), numBytes );

        this.growInts( ints.remaining( ) );
        this.hBuffer.asIntBuffer( ).put( ints );
        this.hBuffer.position( this.hBuffer.position( ) + numBytes );
    }

    public void appendBytes( ByteBuffer bytes )
    {
        this.dirtyRanges.add( this.hBuffer.position( ), bytes.remaining( ) * SIZEOF_BYTE );
        this.growBytes( bytes.remaining( ) );
        this.hBuffer.put( bytes );
    }

    public void setFloats( int firstFloat, FloatBuffer floats )
    {
        this.dirtyRanges.add( firstFloat * SIZEOF_FLOAT, floats.remaining( ) * SIZEOF_FLOAT );

        ByteBuffer hBuffer2 = this.hBuffer.duplicate( );
        hBuffer2.position( firstFloat * SIZEOF_FLOAT );
        hBuffer2.asFloatBuffer( ).put( floats );
    }

    public void setDoubles( int firstDouble, DoubleBuffer doubles )
    {
        this.dirtyRanges.add( firstDouble * SIZEOF_DOUBLE, doubles.remaining( ) * SIZEOF_DOUBLE );

        ByteBuffer hBuffer2 = this.hBuffer.duplicate( );
        hBuffer2.position( firstDouble * SIZEOF_DOUBLE );
        hBuffer2.asDoubleBuffer( ).put( doubles );
    }

    public void setInts( int firstInt, IntBuffer ints )
    {
        this.dirtyRanges.add( firstInt * SIZEOF_INT, ints.remaining( ) * SIZEOF_INT );

        ByteBuffer hBuffer2 = this.hBuffer.duplicate( );
        hBuffer2.position( firstInt * SIZEOF_INT );
        hBuffer2.asIntBuffer( ).put( ints );
    }

    public void setBytes( int firstByte, ByteBuffer bytes )
    {
        this.dirtyRanges.add( firstByte * SIZEOF_BYTE, bytes.remaining( ) * SIZEOF_BYTE );

        ByteBuffer hBuffer2 = this.hBuffer.duplicate( );
        hBuffer2.position( firstByte );
        hBuffer2.put( bytes );
    }

    public int buffer( GL2ES3 gl )
    {
        this.dBuffer.ensureCapacity( gl, this.hBuffer.position( ) );

        // XXX: Higher tolerance might be better
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

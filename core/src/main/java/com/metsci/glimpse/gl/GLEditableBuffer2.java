package com.metsci.glimpse.gl;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;
import static java.lang.Math.*;

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
            ByteBuffer rangeBytes = sliced( this.hBuffer, rangeStart, rangeEnd - rangeStart );
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


    // Size

    public int numFloats( )
    {
        return ( this.numBytes( ) / SIZEOF_FLOAT );
    }

    public int numDoubles( )
    {
        return ( this.numBytes( ) / SIZEOF_DOUBLE );
    }

    public int numInts( )
    {
        return ( this.numBytes( ) / SIZEOF_INT );
    }

    public int numBytes( )
    {
        return this.hBuffer.position( );
    }


    // Grow

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


    // Append

    public void putFloats( float[] floats )
    {
        this.putFloats( floats, 0, floats.length );
    }

    public void putFloats( float[] floats, int offset, int length )
    {
        this.editBytes( this.hBuffer.position( ), ( length - offset ) * SIZEOF_FLOAT ).asFloatBuffer( ).put( floats, offset, length );
    }

    public void putFloats( FloatBuffer floats )
    {
        this.editBytes( this.hBuffer.position( ), floats.remaining( ) * SIZEOF_FLOAT ).asFloatBuffer( ).put( floats );
    }

    public void putDoubles( double[] doubles )
    {
        this.putDoubles( doubles, 0, doubles.length );
    }

    public void putDoubles( double[] doubles, int offset, int length )
    {
        this.editBytes( this.hBuffer.position( ), ( length - offset ) * SIZEOF_DOUBLE ).asDoubleBuffer( ).put( doubles, offset, length );
    }

    public void putDoubles( DoubleBuffer doubles )
    {
        this.editBytes( this.hBuffer.position( ), doubles.remaining( ) * SIZEOF_DOUBLE ).asDoubleBuffer( ).put( doubles );
    }

    public void putInts( int[] ints )
    {
        this.putInts( ints, 0, ints.length );
    }

    public void putInts( int[] ints, int offset, int length )
    {
        this.editBytes( this.hBuffer.position( ), ( length - offset ) * SIZEOF_INT ).asIntBuffer( ).put( ints, offset, length );
    }

    public void putInts( IntBuffer ints )
    {
        this.editBytes( this.hBuffer.position( ), ints.remaining( ) * SIZEOF_INT ).asIntBuffer( ).put( ints );
    }

    public void putBytes( byte[] bytes )
    {
        this.putBytes( bytes, 0, bytes.length );
    }

    public void putBytes( byte[] bytes, int offset, int length )
    {
        this.editBytes( this.hBuffer.position( ), ( length - offset ) ).put( bytes, offset, length );
    }

    public void putBytes( ByteBuffer bytes )
    {
        this.editBytes( this.hBuffer.position( ), bytes.remaining( ) ).put( bytes );
    }


    // Overwrite

    public void putFloats( int firstFloat, FloatBuffer floats )
    {
        this.editFloats( firstFloat, floats.remaining( ) ).put( floats );
    }

    public void putDoubles( int firstDouble, DoubleBuffer doubles )
    {
        this.editDoubles( firstDouble, doubles.remaining( ) ).put( doubles );
    }

    public void putInts( int firstInt, IntBuffer ints )
    {
        this.editInts( firstInt, ints.remaining( ) ).put( ints );
    }

    public void putBytes( int firstByte, ByteBuffer bytes )
    {
        this.editBytes( firstByte, bytes.remaining( ) ).put( bytes );
    }


    // Edit

    public FloatBuffer editFloats( int firstFloat, int numFloats )
    {
        return this.editBytes( firstFloat * SIZEOF_FLOAT, numFloats * SIZEOF_FLOAT ).asFloatBuffer( );
    }

    public DoubleBuffer editDoubles( int firstDouble, int numDoubles )
    {
        return this.editBytes( firstDouble * SIZEOF_DOUBLE, numDoubles * SIZEOF_DOUBLE ).asDoubleBuffer( );
    }

    public IntBuffer editInts( int firstInt, int numInts )
    {
        return this.editBytes( firstInt * SIZEOF_INT, numInts * SIZEOF_INT ).asIntBuffer( );
    }

    public ByteBuffer editBytes( int firstByte, int numBytes )
    {
        this.dirtyRanges.add( firstByte, numBytes );
        this.hBuffer.position( max( this.hBuffer.position( ), firstByte + numBytes ) );
        return sliced( this.hBuffer, firstByte, numBytes );
    }

}

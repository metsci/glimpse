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

    public int deviceBuffer( GL2ES3 gl )
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


    // Buffer

    public FloatBuffer hostFloats( )
    {
        return this.hostBytes( ).asFloatBuffer( );
    }

    public DoubleBuffer hostDoubles( )
    {
        return this.hostBytes( ).asDoubleBuffer( );
    }

    public IntBuffer hostInts( )
    {
        return this.hostBytes( ).asIntBuffer( );
    }

    public ByteBuffer hostBytes( )
    {
        return flipped( readonly( this.hBuffer ) );
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

package com.metsci.glimpse.gl;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.gl.util.GLUtils.*;
import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;
import static java.lang.Math.*;
import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.util.primitives.rangeset.IntRangeSet;
import com.metsci.glimpse.util.primitives.rangeset.IntRangeSetModifiable;
import com.metsci.glimpse.util.primitives.sorted.SortedInts;

public class GLEditableBuffer
{

    protected ByteBuffer hBuffer;

    protected int dBuffer;
    protected long dCapacity;

    protected final IntRangeSetModifiable dirtyRanges;


    public GLEditableBuffer( int capacityBytes )
    {
        this.hBuffer = newDirectByteBuffer( capacityBytes );

        this.dBuffer = 0;
        this.dCapacity = 0;

        this.dirtyRanges = new IntRangeSetModifiable( );
    }

    public int sizeBytes( )
    {
        return this.hBuffer.position( );
    }

    public ByteBuffer hostBytes( )
    {
        return flipped( readonly( this.hBuffer ) );
    }

    public IntRangeSet dirtyByteRanges( )
    {
        return this.dirtyRanges;
    }

    public void ensureRemainingBytes( int minRemainingBytes )
    {
        long minCapacity = ( ( long ) this.hBuffer.position( ) ) + minRemainingBytes;
        this.hEnsureCapacity( minCapacity );
    }

    public void ensureCapacityBytes( int minCapacityBytes )
    {
        this.hEnsureCapacity( minCapacityBytes );
    }

    protected void hEnsureCapacity( long minCapacity )
    {
        if ( minCapacity > Integer.MAX_VALUE )
        {
            throw new RuntimeException( "Cannot create a buffer larger than MAX_INT bytes: requested-capacity = " + minCapacity + " bytes" );
        }

        this.hBuffer = ensureCapacity( this.hBuffer, ( int ) minCapacity, true );
    }

    public ByteBuffer editBytes( int firstByte, int countBytes )
    {
        this.dirtyRanges.add( firstByte, countBytes );
        this.hBuffer.position( max( this.hBuffer.position( ), firstByte + countBytes ) );
        return sliced( this.hBuffer, firstByte, countBytes );
    }

    public void clear( )
    {
        this.hBuffer.clear( );
        this.dirtyRanges.clear( );
    }

    public int deviceBuffer( GL gl )
    {
        if ( this.dBuffer == 0 )
        {
            this.dBuffer = genBuffer( gl );
        }
        gl.glBindBuffer( GL_ARRAY_BUFFER, this.dBuffer );

        int hCapacity = this.hBuffer.capacity( );
        if ( this.dCapacity != hCapacity )
        {
            gl.glBufferData( GL_ARRAY_BUFFER, hCapacity, null, GL_DYNAMIC_DRAW );
            this.dCapacity = hCapacity;
            this.dirtyRanges.add( 0, this.hBuffer.position( ) );
        }

        // glBufferSubData should do its own write-combining anyway, but it may
        // help performance to reduce the number of calls to glBufferSubData
        this.dirtyRanges.coalesce( 1024 );

        SortedInts ranges = this.dirtyRanges.ranges( );
        for ( int i = 0; i < ranges.n( ); i += 2 )
        {
            int first = ranges.v( i + 0 );
            int count = ranges.v( i + 1 ) - first;
            ByteBuffer hRange = sliced( this.hBuffer, first, count );
            gl.glBufferSubData( GL_ARRAY_BUFFER, first, count, hRange );
        }

        this.dirtyRanges.clear( );

        return this.dBuffer;
    }

    public void dispose( GL gl )
    {
        deallocateDirectBuffers( this.hBuffer );
        this.hBuffer = null;

        if ( this.dBuffer != 0 )
        {
            deleteBuffers( gl, this.dBuffer );
            this.dBuffer = 0;
        }

        this.dirtyRanges.clear( );
    }


    // Floats
    //

    public int sizeFloats( )
    {
        return this.sizeBytes( ) / SIZEOF_FLOAT;
    }

    public FloatBuffer hostFloats( )
    {
        return this.hostBytes( ).asFloatBuffer( );
    }

    public void ensureRemainingFloats( int minRemainingFloats )
    {
        this.ensureRemainingBytes( minRemainingFloats * SIZEOF_FLOAT );
    }

    public void ensureCapacityFloats( int minCapacityFloats )
    {
        this.ensureCapacityBytes( minCapacityFloats * SIZEOF_FLOAT );
    }

    public FloatBuffer editFloats( int firstFloat, int countFloats )
    {
        return this.editBytes( firstFloat * SIZEOF_FLOAT, countFloats * SIZEOF_FLOAT ).asFloatBuffer( );
    }

}

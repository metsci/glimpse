package com.metsci.glimpse.gl;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.gl.util.GLUtils.*;
import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;
import static java.lang.Math.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2ES3.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.util.primitives.rangeset.IntRangeSet;
import com.metsci.glimpse.util.primitives.rangeset.IntRangeSetModifiable;
import com.metsci.glimpse.util.primitives.sorted.SortedInts;

public class GLEditableBuffer
{

    protected ByteBuffer hBuffer;

    protected int dBuffer;
    protected long dPosition;
    protected long dCapacity;

    protected final IntRangeSetModifiable dirtyRanges;


    public GLEditableBuffer( int capacityBytes, int scratchBlockSizeFactor )
    {
        this.hBuffer = newDirectByteBuffer( capacityBytes );

        this.dBuffer = 0;
        this.dPosition = 0;
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

    public int deviceBuffer( GL2ES3 gl )
    {
        this.dUpdateCapacity( gl );

        // glBufferSubData should do some write-combining for us anyway, but it
        // may help performance to reduce the number of calls to glBufferSubData
        this.dirtyRanges.coalesce( 1024 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, this.dBuffer );
        SortedInts ranges = this.dirtyRanges.ranges( );
        for ( int i = 0; i < ranges.n( ); i += 2 )
        {
            int first = ranges.v( i + 0 );
            int count = ranges.v( i + 1 ) - first;
            ByteBuffer hRange = sliced( this.hBuffer, first, count );
            gl.glBufferSubData( GL_ARRAY_BUFFER, first, count, hRange );
        }

        this.dirtyRanges.clear( );

        this.dPosition = this.hBuffer.position( );

        return this.dBuffer;
    }

    /**
     * Make device-buffer capacity match host-buffer capacity.
     * <p>
     * If a new device buffer gets created, data will be copied into it from the old device buffer, using
     * glCopyBufferSubData(). The amount copied will be the smaller of the device-buffer position and the
     * host-buffer position.
     */
    protected void dUpdateCapacity( GL2ES3 gl )
    {
        int dNewCapacity = this.hBuffer.capacity( );
        if ( dNewCapacity != this.dCapacity )
        {
            int dNewBuffer = genBuffer( gl );

            // Allocate new space
            gl.glBindBuffer( GL_COPY_WRITE_BUFFER, dNewBuffer );
            gl.glBufferData( GL_COPY_WRITE_BUFFER, dNewCapacity, null, GL_STATIC_COPY );

            // Copy data from old to new
            if ( this.dBuffer != 0 )
            {
                this.dPosition = min( this.dPosition, this.hBuffer.position( ) );
                gl.glBindBuffer( GL_COPY_READ_BUFFER, this.dBuffer );
                gl.glCopyBufferSubData( GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0, this.dPosition );
            }

            this.dBuffer = dNewBuffer;
            this.dCapacity = dNewCapacity;
        }
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

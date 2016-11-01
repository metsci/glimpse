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

    public final int usage;

    protected ByteBuffer hBuffer;

    protected int dBuffer;
    protected long dCapacity;

    protected final IntRangeSetModifiable dirtyRanges;


    public GLEditableBuffer( int usage, int capacityBytes )
    {
        this.usage = usage;

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

    public ByteBuffer growBytes( int countBytes )
    {
        this.ensureRemainingBytes( countBytes );
        return this.editBytes( this.sizeBytes( ), countBytes );
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
            gl.glBufferData( GL_ARRAY_BUFFER, hCapacity, null, this.usage );
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

    public void grow1f( float a )
    {
        this.growFloats( 1 ).put( a );
    }

    public void grow2f( float a, float b )
    {
        this.growFloats( 2 ).put( a ).put( b );
    }

    public void grow3f( float a, float b, float c )
    {
        this.growFloats( 3 ).put( a ).put( b ).put( c );
    }

    public void grow4f( float a, float b, float c, float d )
    {
        this.growFloats( 4 ).put( a ).put( b ).put( c ).put( d );
    }

    public void growNfv( float[] array, int offset, int length )
    {
        this.growFloats( length ).put( array, offset, length );
    }

    public FloatBuffer growFloats( int countFloats )
    {
        return this.growBytes( countFloats * SIZEOF_FLOAT ).asFloatBuffer( );
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


    // Convenience methods
    //

    public void growQuad2f( float left, float bottom, float right, float top )
    {
        FloatBuffer floats = this.growFloats( 12 );

        put2f( floats, left,  bottom );
        put2f( floats, left,  top    );
        put2f( floats, right, bottom );

        put2f( floats, right, bottom );
        put2f( floats, left,  top    );
        put2f( floats, right, top    );
    }

    public void growQuad1f( float leftBottom, float leftTop, float rightBottom, float rightTop )
    {
        FloatBuffer floats = this.growFloats( 6 );

        put1f( floats, leftBottom  );
        put1f( floats, leftTop     );
        put1f( floats, rightBottom );

        put1f( floats, rightBottom );
        put1f( floats, leftTop     );
        put1f( floats, rightTop    );
    }

    public void growQuadSolidColor( float[] color )
    {
        FloatBuffer floats = this.growFloats( 24 );

        floats.put( color );
        floats.put( color );
        floats.put( color );

        floats.put( color );
        floats.put( color );
        floats.put( color );
    }

}

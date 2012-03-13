package com.metsci.glimpse.gl.attribute;

import static com.metsci.glimpse.gl.util.GLUtils.genBuffer;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;

public abstract class GLBuffer
{
    protected ReentrantLock lock = new ReentrantLock( );

    protected int elementSize; // 1, 2, 3, or 4

    protected int glHandle;
    protected boolean dirty;
    
    protected ByteBuffer data;
    
    protected GLVertexAttribute boundType;
    protected int boundGenericIndex;

    public GLBuffer( int length, int elementSize )
    {
        if ( elementSize > 4 || elementSize < 1 ) throw new IllegalArgumentException( "length must be 1, 2, 3, or 4" );

        this.glHandle = -1;
        this.dirty = true;

        this.elementSize = elementSize;
        
        this.data = createBuffer( length, elementSize );

        this.boundType = null;
        this.boundGenericIndex = -1;
    }

    public abstract int getGlType( );
    
    public abstract int getBytesPerElement( );

    public ByteBuffer createBuffer( int length, int elementSize )
    {
        return BufferUtil.newByteBuffer( length * elementSize * getBytesPerElement( ) );
    }
    
    public boolean isDirty( )
    {
        return dirty;
    }

    public void makeDirty( )
    {
        dirty = true;
    }

    public int getNumVertices( )
    {
        return data.limit( ) / elementSize / getBytesPerElement( );
    }

    public int getMaxVertices( )
    {
        return data.capacity( ) / elementSize / getBytesPerElement( );
    }

    public void bind( GLVertexAttribute type, GL gl )
    {
        lock.lock( );
        try
        {
            prepare( gl );
            type.bind( gl, 0, 0 );
            boundType = type;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void bind( int genericIndex, GL gl )
    {
        lock.lock( );
        try
        {
            prepare( gl );
            gl.glEnableVertexAttribArray( genericIndex );
            gl.glVertexAttribPointer( genericIndex, elementSize, getGlType( ), false, 0, 0 );
            boundGenericIndex = genericIndex;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void unbind( GL gl )
    {
        lock.lock( );
        try
        {
            if ( boundType != null )
            {
                boundType.unbind( gl );
                boundType = null;
            }
            else if ( boundGenericIndex > 0 )
            {
                GLVertexAttribute.unbind( gl, boundGenericIndex );
                boundGenericIndex = -1;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    public boolean prepare( GL gl )
    {
        lock.lock( );
        try
        {
            if ( glHandle == -1 )
            {
                glHandle = genBuffer( gl );
                makeDirty( );
            }

            gl.glBindBuffer( GL.GL_ARRAY_BUFFER, glHandle );

            if ( isDirty( ) )
            {
                gl.glBufferData( GL.GL_ARRAY_BUFFER, data.limit( ), data.rewind( ), GL.GL_STATIC_DRAW );
                dirty = false;
            }

            return !isDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public boolean dispose( GL gl )
    {
        lock.lock( );
        try
        {
            if ( glHandle != -1 )
            {
                gl.glDeleteBuffers( 1, new int[] { glHandle }, 0 );
                glHandle = -1;
            }
        }
        finally
        {
            lock.unlock( );
        }

        lock = null;
        return true;
    }

    public void mutate( Mutator mutator )
    {
        lock.lock( );
        try
        {
            mutator.mutate( data, elementSize );
            data.flip( );
            makeDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public static interface Mutator
    {
        public void mutate( ByteBuffer data, int length );
    }
}

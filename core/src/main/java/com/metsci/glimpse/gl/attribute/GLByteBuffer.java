package com.metsci.glimpse.gl.attribute;

import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;

public class GLByteBuffer extends GLBuffer
{
    public GLByteBuffer( int length, int elementSize )
    {
        super( length, elementSize );
    }

    @Override
    public final int getGlType( )
    {
        return GL.GL_UNSIGNED_BYTE;
    }

    @Override
    public int getBytesPerElement( )
    {
        return BufferUtil.SIZEOF_BYTE;
    }
}
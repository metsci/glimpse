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
package com.metsci.glimpse.dnc;

import static com.jogamp.common.nio.Buffers.newDirectByteBuffer;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.nextPowerOfTwo;
import static com.metsci.glimpse.gl.util.GLUtils.genTexture;
import static com.metsci.glimpse.util.GeneralUtils.ints;
import static java.lang.Math.min;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES3.GL_R8UI;
import static com.jogamp.opengl.GL2ES3.GL_RED_INTEGER;

import java.nio.ByteBuffer;

import com.jogamp.opengl.GL;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class IndexSetTexture
{

    private static byte TRUE = 1;
    private static byte FALSE = 0;


    protected final IntSet hSet;
    protected boolean hSetChanged;

    protected IntSet dSet;
    protected ByteBuffer dBuffer;
    protected int dRowLength;
    protected int dRowCount;
    protected int dTextureHandle;


    public IndexSetTexture( )
    {
        this.hSet = new IntOpenHashSet( );
        this.hSetChanged = true;

        this.dSet = null;
        this.dBuffer = null;
        this.dRowLength = 0;
        this.dRowCount = 0;
        this.dTextureHandle = 0;
    }

    public void set( IntCollection indices )
    {
        if ( !hSet.isEmpty( ) )
        {
            hSet.clear( );
            hSetChanged = true;
        }

        if ( hSet.addAll( indices ) )
        {
            hSetChanged = true;
        }
    }

    public void bind( GL gl, int indexCount, int textureMaxRowLength )
    {

        // Prepare dBuffer
        boolean dBufferChanged = false;
        if ( dSet == null || dBuffer == null || dRowLength * dRowCount < indexCount || dRowLength > textureMaxRowLength )
        {
            dRowLength = min( textureMaxRowLength, nextPowerOfTwo( indexCount ) );
            dRowCount = ( indexCount + dRowLength - 1 ) / dRowLength;

            dBuffer = newDirectByteBuffer( dRowLength * dRowCount );
            for ( int i = 0; dBuffer.hasRemaining( ); i++ )
            {
                dBuffer.put( hSet.contains( i ) ? TRUE : FALSE );
            }
            dBuffer.flip( );
            dBufferChanged = true;

            dSet = new IntOpenHashSet( hSet );
            hSetChanged = false;
        }
        else if ( hSetChanged )
        {
            for ( IntIterator it = dSet.iterator( ); it.hasNext( ); )
            {
                int index = it.nextInt( );
                if ( !hSet.contains( index ) )
                {
                    dBuffer.put( index, FALSE );
                    dBufferChanged = true;
                }
            }

            for ( IntIterator it = hSet.iterator( ); it.hasNext( ); )
            {
                int index = it.nextInt( );
                if ( dBuffer.get( index ) != TRUE )
                {
                    dBuffer.put( index, TRUE );
                    dBufferChanged = true;
                }
            }

            dSet.clear( );
            dSet.addAll( hSet );
            hSetChanged = false;
        }


        // Prepare dTextureHandle
        boolean dTextureHandleChanged = false;
        if ( dTextureHandle == 0 )
        {
            dTextureHandle = genTexture( gl );
            gl.glBindTexture( GL_TEXTURE_2D, dTextureHandle );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );

            dTextureHandleChanged = true;
        }
        else
        {
            gl.glBindTexture( GL_TEXTURE_2D, dTextureHandle );
        }


        // Push data to the device
        if ( dBufferChanged || dTextureHandleChanged )
        {
            gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );
            gl.glTexImage2D( GL_TEXTURE_2D, 0, GL_R8UI, dRowLength, dRowCount, 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, dBuffer );
        }

    }

    public void freeDeviceResources( GL gl )
    {
        if ( dTextureHandle != 0 )
        {
            gl.glDeleteTextures( 1, ints( dTextureHandle ), 0 );
        }

        dSet = null;
        dBuffer = null;
        dRowLength = 0;
        dRowCount = 0;
        dTextureHandle = 0;
    }

}

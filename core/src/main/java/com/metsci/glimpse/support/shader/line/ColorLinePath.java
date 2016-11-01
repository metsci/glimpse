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
package com.metsci.glimpse.support.shader.line;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;
import static javax.media.opengl.GL.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.gl.GLStreamingBuffer;
import com.metsci.glimpse.support.color.GlimpseColor;

public class ColorLinePath
{
    protected static final float[] ColorNotUsed = GlimpseColor.getBlack( );

    protected LinePath path;

    protected FloatBuffer rgbaBuffer;
    protected GLStreamingBuffer rgbaVbo;
    protected boolean rgbaDirty;

    public ColorLinePath( )
    {
        this( 0, 10 );
    }

    public ColorLinePath( int initialNumVertices, int vboBlockSizeFactor )
    {
        this.path = new LinePath( initialNumVertices, vboBlockSizeFactor );

        this.rgbaBuffer = newDirectFloatBuffer( 4 * initialNumVertices );
        this.rgbaVbo = new GLStreamingBuffer( GL_STATIC_DRAW, vboBlockSizeFactor );
        this.rgbaDirty = true;
    }

    public LinePath getLinePath( )
    {
        return this.path;
    }

    public void moveTo( float x, float y, float[] rgba )
    {
        this.moveTo( x, y, 0f, rgba );
    }

    public void moveTo( float x, float y, float mileage, float[] rgba )
    {
        this.path.moveTo( x, y, mileage );

        this.rgbaBuffer = grow4fv( this.rgbaBuffer, ColorNotUsed );
        this.rgbaBuffer = grow4fv( this.rgbaBuffer, rgba );

        this.setDirty( );
    }

    public void lineTo( float x, float y, float[] rgba )
    {
        this.path.lineTo( x, y );

        this.rgbaBuffer = grow4fv( this.rgbaBuffer, rgba );

        this.setDirty( );
    }

    /**
     * After calling this method, client code must next call {@link #moveTo(float, float, float)},
     * before calling either {@link #lineTo(float, float)} or {@link #closeLoop()} again.
     */
    public void closeLoop( )
    {
        this.path.closeLoop( );

        this.rgbaBuffer = grow4fv( this.rgbaBuffer, ColorNotUsed );
        this.rgbaBuffer = grow4fv( this.rgbaBuffer, ColorNotUsed );

        this.setDirty( );
    }

    public void clear( )
    {
        this.path.clear( );

        this.rgbaBuffer.clear( );

        this.setDirty( );
    }

    /**
     * Convenience method for adding an axis-aligned rectangle.
     */
    public void addRectangle( float x1, float y1, float x2, float y2, float[] rgba )
    {
        this.moveTo( x1, y1, rgba );
        this.lineTo( x2, y1, rgba );
        this.lineTo( x2, y2, rgba );
        this.lineTo( x1, y2, rgba );
        this.closeLoop( );
    }

    protected void setDirty( )
    {
        this.path.setDirty( );
        this.rgbaDirty = true;
    }

    /**
     * Disposes buffers on host and device.
     * <p>
     * <em>This object must not be used again after this method has been called.</em>
     */
    public void dispose( GL gl )
    {
        this.path.dispose( gl );
        this.rgbaVbo.dispose( gl );

        deallocateDirectBuffers( this.rgbaBuffer );
        this.rgbaBuffer = null;
    }

    public int numVertices( )
    {
        return this.path.numVertices( );
    }

    protected FloatBuffer rgbaBuffer( )
    {
        return flipped( this.rgbaBuffer );
    }

    public GLStreamingBuffer rgbaVbo( GL gl )
    {
        if ( this.rgbaDirty )
        {
            FloatBuffer rgbaData = rgbaBuffer( );

            FloatBuffer rgbaMapped = this.rgbaVbo.mapFloats( gl, rgbaData.remaining( ) + 4 );
            rgbaMapped.put( rgbaData );

            // Append an extra vertex to make sure GL_LINE_STRIP_ADJACENCY works right
            if ( rgbaData.position( ) >= 4 )
            {
                rgbaMapped.put( ColorNotUsed );
            }

            this.rgbaVbo.seal( gl );

            this.rgbaDirty = false;
        }

        return this.rgbaVbo;
    }

    public GLStreamingBuffer xyVbo( GL gl )
    {
        return this.path.xyVbo( gl );
    }

    public GLStreamingBuffer flagsVbo( GL gl )
    {
        return this.path.flagsVbo( gl );
    }

    public GLStreamingBuffer mileageVbo( GL gl, double ppvAspectRatio )
    {
        return this.path.mileageVbo( gl, ppvAspectRatio );
    }

    public GLStreamingBuffer rawMileageVbo( GL gl )
    {
        return this.path.rawMileageVbo( gl );
    }
}

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

import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.gl.GLStreamingBuffer;

public class LinePath
{

    protected final LinePathData data;

    protected GLStreamingBuffer xyVbo;
    protected boolean xyDirty;

    protected GLStreamingBuffer flagsVbo;
    protected boolean flagsDirty;

    protected GLStreamingBuffer mileageVbo;
    protected boolean mileageDirty;


    public LinePath( )
    {
        this( 0, 10 );
    }

    public LinePath( int initialNumVertices, int vboBlockSizeFactor )
    {
        this.data = new LinePathData( initialNumVertices );

        this.xyVbo = new GLStreamingBuffer( GL_STATIC_DRAW, vboBlockSizeFactor );
        this.xyDirty = true;

        this.flagsVbo = new GLStreamingBuffer( GL_STATIC_DRAW, vboBlockSizeFactor );
        this.flagsDirty = true;

        this.mileageVbo = new GLStreamingBuffer( GL_STATIC_DRAW, vboBlockSizeFactor );
        this.mileageDirty = true;
    }

    public void moveTo( float x, float y )
    {
        this.moveTo( x, y, 0f );
    }

    public void moveTo( float x, float y, float mileage )
    {
        this.data.moveTo( x, y, mileage );
        this.setDirty( );
    }

    public void lineTo( float x, float y )
    {
        this.data.lineTo( x, y );
        this.setDirty( );
    }

    /**
     * After calling this method, client code must next call {@link #moveTo(float, float, float)},
     * before calling either {@link #lineTo(float, float)} or {@link #closeLoop()} again.
     */
    public void closeLoop( )
    {
        this.data.closeLoop( );
        this.setDirty( );
    }

    public void clear( )
    {
        this.data.clear( );
        this.setDirty( );
    }

    /**
     * Convenience method for adding an axis-aligned rectangle.
     */
    public void addRectangle( float x1, float y1, float x2, float y2 )
    {
        this.moveTo( x1, y1 );
        this.lineTo( x2, y1 );
        this.lineTo( x2, y2 );
        this.lineTo( x1, y2 );
        this.closeLoop( );
    }

    /**
     * Convenience method for adding a polygon.
     * <p>
     * Use of varargs makes this method convenient, but may also make it inefficient in some cases,
     * since the args must be passed in a float[] array.
     */
    public void addPolygon( float... xys )
    {
        int n = xys.length / 2;
        if ( n > 0 )
        {
            this.moveTo( xys[ 0 ], xys[ 1 ] );
            for ( int i = 1; i < n; i++ )
            {
                this.lineTo( xys[ 2*i + 0 ], xys[ 2*i + 1 ] );
            }
            this.closeLoop( );
        }
    }

    protected void setDirty( )
    {
        this.xyDirty = true;
        this.flagsDirty = true;
        this.mileageDirty = true;
    }

    /**
     * Disposes buffers on host and device.
     * <p>
     * <em>This object must not be used again after this method has been called.</em>
     */
    public void dispose( GL gl )
    {
        this.data.dispose( );
        this.xyVbo.dispose( gl );
        this.flagsVbo.dispose( gl );
        this.mileageVbo.dispose( gl );
    }

    public int numVertices( )
    {
        // The vbo() methods append an extra vertex to make sure GL_LINE_STRIP_ADJACENCY works right
        int numVertices = this.data.numVertices( );
        return ( numVertices == 0 ? 0 : numVertices + 1 );
    }

    public GLStreamingBuffer xyVbo( GL gl )
    {
        if ( this.xyDirty )
        {
            FloatBuffer xyData = this.data.xyBuffer( );

            FloatBuffer xyMapped = this.xyVbo.mapFloats( gl, xyData.remaining( ) + 2 );
            xyMapped.put( xyData );

            // Append an extra vertex to make sure GL_LINE_STRIP_ADJACENCY works right
            if ( xyData.position( ) >= 2 )
            {
                xyData.position( xyData.position( ) - 2 );
                float xTrailer = xyData.get( );
                float yTrailer = xyData.get( );
                xyMapped.put( xTrailer ).put( yTrailer );
            }

            this.xyVbo.seal( gl );

            this.xyDirty = false;
        }

        return this.xyVbo;
    }

    public GLStreamingBuffer flagsVbo( GL gl )
    {
        if ( this.flagsDirty )
        {
            ByteBuffer flagsData = this.data.flagsBuffer( );

            ByteBuffer flagsMapped = this.flagsVbo.mapBytes( gl, flagsData.remaining( ) + 1 );
            flagsMapped.put( flagsData );

            // Append an extra vertex to make sure GL_LINE_STRIP_ADJACENCY works right
            if ( flagsData.position( ) >= 1 )
            {
                byte flagsTrailer = ( byte ) 0;
                flagsMapped.put( flagsTrailer );
            }

            this.flagsVbo.seal( gl );

            this.flagsDirty = false;
        }

        return this.flagsVbo;
    }

    /**
     * Calls {@link #mileageVbo(GL, double, double)} with a threshold of 1+epsilon.
     */
    public GLStreamingBuffer mileageVbo( GL gl, double ppvAspectRatio )
    {
        return this.mileageVbo( gl, ppvAspectRatio, 1.0000000001 );
    }

    /**
     * Recomputes mileages as necessary (based on the given threshold, and on the change in
     * ppv-aspect-ratio), then returns the mileage VBO.
     */
    public GLStreamingBuffer mileageVbo( GL gl, double ppvAspectRatio, double ppvAspectRatioChangeThreshold )
    {
        int mileageDirtyCount = this.data.updateMileage( ppvAspectRatio, ppvAspectRatioChangeThreshold );
        this.mileageDirty |= ( mileageDirtyCount > 0 );

        return this.rawMileageVbo( gl );
    }

    /**
     * Returns the mileage VBO <em>without recomputing the mileage values</em>. This is useful
     * when meaningful values are not required, such as when rendering with stippling disabled.
     */
    public GLStreamingBuffer rawMileageVbo( GL gl )
    {
        if ( this.mileageDirty )
        {
            FloatBuffer mileageData = this.data.mileageBuffer( );

            FloatBuffer mileageMapped = this.mileageVbo.mapFloats( gl, mileageData.remaining( ) + 1 );
            mileageMapped.put( mileageData );

            // Append an extra vertex to make sure GL_LINE_STRIP_ADJACENCY works right
            if ( mileageData.position( ) >= 1 )
            {
                float mileageTrailer = 0f;
                mileageMapped.put( mileageTrailer );
            }

            this.mileageVbo.seal( gl );

            this.mileageDirty = false;
        }

        return mileageVbo;
    }

}

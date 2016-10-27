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

import static com.metsci.glimpse.support.shader.line.LinePathData.*;
import static com.metsci.glimpse.support.shader.line.LineUtils.*;
import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.gl.GLStreamingBuffer;

public class StreamingLinePath
{

    public final GLStreamingBuffer xyVbo;
    public final GLStreamingBuffer flagsVbo;
    public final GLStreamingBuffer mileageVbo;

    // Per mapping
    protected FloatBuffer xyBuffer;
    protected ByteBuffer flagsBuffer;
    protected FloatBuffer mileageBuffer;
    protected boolean needMileage;
    protected double ppvAspectRatio;

    // Per strip
    protected int iStrip;
    protected float xFirst;
    protected float yFirst;
    protected float xSecond;
    protected float ySecond;
    protected float xHead;
    protected float yHead;
    protected int flagsHead;
    protected float mileageHead;

    // Valid only when sealed
    protected int numSealedVertices;

    public StreamingLinePath( )
    {
        this( 10 );
    }

    public StreamingLinePath( int vboBlockSizeFactor )
    {
        this.xyVbo = new GLStreamingBuffer( GL_STATIC_DRAW, vboBlockSizeFactor );
        this.flagsVbo = new GLStreamingBuffer( GL_STATIC_DRAW, vboBlockSizeFactor );
        this.mileageVbo = new GLStreamingBuffer( GL_STATIC_DRAW, vboBlockSizeFactor );

        // Per mapping
        this.xyBuffer = null;
        this.flagsBuffer = null;
        this.mileageBuffer = null;
        this.needMileage = false;
        this.ppvAspectRatio = 0.0;

        // Per strip
        this.iStrip = -1;
        this.xFirst = 0f;
        this.yFirst = 0f;
        this.xSecond = 0f;
        this.ySecond = 0f;
        this.xHead = 0f;
        this.yHead = 0f;
        this.flagsHead = 0;
        this.mileageHead = 0f;

        // Valid only when sealed
        this.numSealedVertices = 0;
    }

    public void map( GL gl, int maxVertices )
    {
        this.map( gl, maxVertices, false, 0.0 );
    }

    protected void map( GL gl, int maxVertices, double ppvAspectRatio )
    {
        this.map( gl, maxVertices, true, ppvAspectRatio );
    }

    public void map( GL gl, int maxVertices, boolean needMileage, double ppvAspectRatio )
    {
        this.xyBuffer = this.xyVbo.mapFloats( gl, 2 * maxVertices );
        this.flagsBuffer = this.flagsVbo.mapBytes( gl, maxVertices );
        this.mileageBuffer = this.mileageVbo.mapFloats( gl, maxVertices );
        this.needMileage = needMileage;
        this.ppvAspectRatio = ( needMileage ? ppvAspectRatio : 0.0 );

        this.iStrip = -1;
    }

    /**
     * Equivalent to {@code this.moveTo( x, y, 0f )}.
     * <p>
     * Appends 2 vertices.
     */
    public void moveTo( float x, float y )
    {
        this.moveTo( x, y, 0f );
    }

    /**
     * Appends 2 vertices: a leading phantom vertex, and then a vertex with the given coords.
     */
    public void moveTo( float x, float y, float mileage )
    {
        // Rewrite flags of previous vertex, to end previous strip
        int iLast = this.flagsBuffer.position( ) - 1;
        if ( iLast >= 0 )
        {
            int flagsLast = this.flagsHead;
            this.flagsBuffer.put( 1 * iLast, ( byte ) ( flagsLast & ~FLAGS_JOIN ) );
        }

        // About to start a new strip
        this.iStrip = -1;

        // Leading phantom vertex, in case this strip is at the start of the mapped section,
        // or turns out to be a loop
        this.appendVertex( x, y, 0, mileage );

        // First vertex in strip
        this.appendVertex( x, y, 0, mileage );
    }

    /**
     * Appends 1 vertex.
     */
    public void lineTo( float x, float y )
    {
        this.appendVertex( x, y, FLAGS_CONNECT | FLAGS_JOIN );
    }

    /**
     * Appends 2 vertices: a loop-closing vertex, and a trailing phantom vertex.
     * <p>
     * After calling this method, client code must next call {@link #moveTo(float, float, float)},
     * before calling either {@link #lineTo(float, float)} or {@link #closeLoop()} again.
     */
    public void closeLoop( )
    {
        if ( this.iStrip < 0 )
        {
            throw new RuntimeException( "No current line-strip -- moveTo() must be called after instantiation, and after each call to closeLoop()" );
        }

        // Rewrite position of leading phantom vertex
        float xSecondToLast = this.xHead;
        float ySecondToLast = this.yHead;
        this.xyBuffer.put( 2 * this.iStrip + 0, xSecondToLast );
        this.xyBuffer.put( 2 * this.iStrip + 1, ySecondToLast );

        // Rewrite flags of first vertex in strip
        int iFirst = this.iStrip + 1;
        this.flagsBuffer.put( 1 * iFirst, ( byte ) FLAGS_JOIN );

        // Append loop-closing vertex
        this.appendVertex( this.xFirst, this.yFirst, FLAGS_CONNECT | FLAGS_JOIN );

        // Append trailing phantom vertex
        this.appendVertex( this.xSecond, this.ySecond, 0 );

        // Next vertex must start a new strip
        this.iStrip = -1;
    }

    protected void appendVertex( float x, float y, int flags )
    {
        if ( this.iStrip < 0 )
        {
            throw new RuntimeException( "No current line-strip -- moveTo() must be called after instantiation, and after each call to closeLoop()" );
        }

        float mileage;
        if ( this.needMileage )
        {
            mileage = ( float ) ( this.mileageHead + distance( this.xHead, this.yHead, x, y, this.ppvAspectRatio ) );
        }
        else
        {
            mileage = 0f;
        }

        this.appendVertex( x, y, flags, mileage );
    }

    protected void appendVertex( float x, float y, int flags, float mileage )
    {
        if ( this.iStrip < 0 )
        {
            this.iStrip = this.flagsBuffer.position( );
            this.xFirst = x;
            this.yFirst = y;
        }
        else if ( this.flagsBuffer.position( ) == this.iStrip + 1 )
        {
            this.xSecond = x;
            this.ySecond = y;
        }

        this.xHead = x;
        this.yHead = y;
        this.flagsHead = flags;
        this.mileageHead = mileage;

        this.xyBuffer.put( x ).put( y );
        this.flagsBuffer.put( ( byte ) flags );
        this.mileageBuffer.put( mileage );
    }

    /**
     * Appends 1 vertex, if the current line-strip is not empty.
     */
    public void seal( GL gl )
    {
        // Append an extra vertex to make sure GL_LINE_STRIP_ADJACENCY works right
        if ( this.iStrip >= 0 )
        {
            this.appendVertex( this.xHead, this.yHead, 0 );
        }

        this.numSealedVertices = this.flagsBuffer.position( );

        this.xyVbo.seal( gl );
        this.flagsVbo.seal( gl );
        this.mileageVbo.seal( gl );

        this.xyBuffer = null;
        this.flagsBuffer = null;
        this.mileageBuffer = null;

        this.iStrip = -1;
    }

    public int numVertices( )
    {
        if ( this.flagsBuffer != null )
        {
            // Currently mapped, so check how many flags bytes we've written
            return this.flagsBuffer.position( );
        }
        else
        {
            // Currently sealed, so check how many flags bytes we'd written when we last sealed
            return this.numSealedVertices;
        }
    }

    /**
     * This object can be safely reused after being disposed, but in most cases there is no
     * significant advantage to doing so.
     */
    public void dispose( GL gl )
    {
        this.xyVbo.dispose( gl );
        this.flagsVbo.dispose( gl );
        this.mileageVbo.dispose( gl );

        this.xyBuffer = null;
        this.flagsBuffer = null;
        this.mileageBuffer = null;

        this.iStrip = -1;
    }

}

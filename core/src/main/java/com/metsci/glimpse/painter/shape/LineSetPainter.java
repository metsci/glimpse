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
package com.metsci.glimpse.painter.shape;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.jogamp.common.nio.Buffers;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.painter.shape.PointSetPainter.IdXy;
import com.metsci.glimpse.util.quadtree.QuadTreeXys;

/**
 * Efficiently draws a static set of line segments. Can also efficiently
 * query for the line segments contained in a particular region of the
 * plot using a {@link com.metsci.glimpse.util.quadtree.QuadTree}.
 *
 * @author ulman
 */
public class LineSetPainter extends GlimpseDataPainter2D
{
    public static final int QUAD_TREE_BIN_MAX = 1000;

    protected float[] lineColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    protected float lineWidth = 1;

    protected int totalPointCount = 0;
    protected int lineCount = 0;
    protected int[] bufferHandle = null;
    protected FloatBuffer dataBuffer = null;
    protected IntBuffer offsetBuffer = null;
    protected IntBuffer sizeBuffer = null;
    protected ReentrantLock dataBufferLock = null;
    protected volatile boolean newData = false;
    protected volatile boolean bufferInitialized = false;

    // spatial index on Points
    protected QuadTreeXys<IdXy> spatialIndex;
    protected boolean enableSpatialIndex;

    public LineSetPainter( )
    {
        this( false );
    }

    public LineSetPainter( boolean enableSpatialIndex )
    {
        this.dataBufferLock = new ReentrantLock( );
        this.enableSpatialIndex = enableSpatialIndex;
    }

    public void setData( float[] dataX, float[] dataY )
    {
        float[][] tempX = new float[1][dataX.length];
        tempX[0] = dataX;
        float[][] tempY = new float[1][dataY.length];
        tempY[0] = dataY;

        setData( tempX, tempY );
    }

    /**
     * Adds multiple tracks containing series of (x,y) positions to be painter. The
     * positions of a single track are connected by lines.
     *
     * dataX[0] is an array containing the x coordinates of the positions in track id 0.
     * dataX[0][0] is the x coordinate of point id 0 in track id 0.
     *
     * The ids are used when making spatial or temporal queries on this painter.
     *
     * @param dataX x coordinate data for all points in all tracks
     * @param dataY y coordinate data for all points in all tracks
     */
    public void setData( float[][] dataX, float[][] dataY )
    {
        this.dataBufferLock.lock( );
        try
        {
            lineCount = Math.min( dataX.length, dataY.length );

            totalPointCount = 0;
            for ( int trackId = 0; trackId < lineCount; trackId++ )
            {
                int pointCount = Math.min( dataX[trackId].length, dataY[trackId].length );
                totalPointCount += pointCount;
            }

            if ( dataBuffer == null || dataBuffer.rewind( ).capacity( ) < totalPointCount * 2 )
            {
                this.dataBuffer = Buffers.newDirectFloatBuffer( totalPointCount * 2 );
            }

            if ( sizeBuffer == null || sizeBuffer.rewind( ).capacity( ) < lineCount )
            {
                this.sizeBuffer = Buffers.newDirectIntBuffer( lineCount );
            }

            if ( offsetBuffer == null || offsetBuffer.rewind( ).capacity( ) < lineCount )
            {
                this.offsetBuffer = Buffers.newDirectIntBuffer( lineCount );
            }

            // copy data from the provided arrays into the host memory buffer
            int pointCount = 0;
            for ( int trackId = 0; trackId < lineCount; trackId++ )
            {
                float[] trackX = dataX[trackId];
                float[] trackY = dataY[trackId];

                int trackLength = Math.min( dataX[trackId].length, dataY[trackId].length );

                this.offsetBuffer.put( pointCount );
                this.sizeBuffer.put( trackLength );

                pointCount += trackLength;

                for ( int i = 0; i < trackLength; i++ )
                {
                    this.dataBuffer.put( trackX[i] ).put( trackY[i] );
                }
            }

            pointCount = 0;
            if ( this.enableSpatialIndex )
            {
                this.spatialIndex = new QuadTreeXys<IdXy>( QUAD_TREE_BIN_MAX );

                for ( int trackId = 0; trackId < lineCount; trackId++ )
                {
                    float[] trackX = dataX[trackId];
                    float[] trackY = dataY[trackId];

                    int trackLength = Math.min( dataX[trackId].length, dataY[trackId].length );

                    for ( int i = 0; i < trackLength; i++ )
                    {
                        this.spatialIndex.add( new IdXy( pointCount++, trackX[i], trackY[i] ) );
                    }
                }
            }

            this.newData = true;
        }
        finally
        {
            this.dataBufferLock.unlock( );
        }
    }

    public void setLineColor( float r, float g, float b, float a )
    {
        this.lineColor[0] = r;
        this.lineColor[1] = g;
        this.lineColor[2] = b;
        this.lineColor[3] = a;
    }

    public void setLineColor( float[] color )
    {
        this.lineColor = color;
    }

    public void setLineWidth( float width )
    {
        this.lineWidth = width;
    }

    @Override
    public void dispose( GLContext context )
    {
        if ( bufferInitialized )
        {
            context.getGL( ).glDeleteBuffers( 1, bufferHandle, 0 );
        }
    }

    @Override
    public void paintTo( GL2 gl, GlimpseBounds bounds, Axis2D axis )
    {
        if ( lineCount == 0 ) return;

        if ( !bufferInitialized )
        {
            bufferHandle = new int[1];
            gl.glGenBuffers( 1, bufferHandle, 0 );
            bufferInitialized = true;
        }

        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, bufferHandle[0] );

        dataBufferLock.lock( );
        try
        {
            if ( newData )
            {
                // copy data from the host memory buffer to the device
                gl.glBufferData( GL2.GL_ARRAY_BUFFER, totalPointCount * 2 * BYTES_PER_FLOAT, dataBuffer.rewind( ), GL2.GL_DYNAMIC_DRAW );

                glHandleError( gl );

                newData = false;
            }

            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, bufferHandle[0] );
            gl.glVertexPointer( 2, GL2.GL_FLOAT, 0, 0 );
            gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );

            gl.glColor4fv( lineColor, 0 );
            gl.glLineWidth( lineWidth );

            offsetBuffer.rewind( );
            sizeBuffer.rewind( );

            gl.glMultiDrawArrays( GL2.GL_LINE_STRIP, offsetBuffer, sizeBuffer, lineCount );
        }
        finally
        {
            dataBufferLock.unlock( );
        }
    }
}

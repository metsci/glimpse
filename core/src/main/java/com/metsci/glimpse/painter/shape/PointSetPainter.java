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
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.jogamp.common.nio.Buffers;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.support.colormap.ColorMap;
import com.metsci.glimpse.util.quadtree.QuadTreeXys;
import com.metsci.glimpse.util.quadtree.Xy;

/**
 * Efficiently draws a static set of points. Can also efficiently
 * query for the points contained in a particular region of the
 * plot using a {@link com.metsci.glimpse.util.quadtree.QuadTree}.
 *
 * @author ulman
 */
public class PointSetPainter extends GlimpseDataPainter2D
{
    public static final int QUAD_TREE_BIN_MAX = 1000;

    public static final long SPATIAL_SELECTION_UPDATE_RATE = 50;

    protected float[] pointColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    protected float pointSize = 2;

    protected int dataSize = 0;

    protected int[] colorHandle = null;
    protected FloatBuffer colorBuffer = null;
    protected boolean useColorDevice = false;
    protected boolean useColorHost = false;

    protected int[] bufferHandle = null;
    protected FloatBuffer dataBuffer = null;

    protected ReentrantLock dataBufferLock = null;

    protected volatile boolean newData = false;
    protected volatile boolean bufferInitialized = false;

    // spatial index on Points
    protected QuadTreeXys<IdXy> spatialIndex;
    protected boolean enableSpatialIndex;

    public PointSetPainter( boolean enableSpatialIndex )
    {
        this.dataBufferLock = new ReentrantLock( );
        this.enableSpatialIndex = enableSpatialIndex;
    }

    public void setData( float[] dataX, float[] dataY )
    {
        int dataSize = Math.min( dataX.length, dataY.length );
        setData( dataX, dataY, dataSize );
    }

    public void setData( float[] dataX, float[] dataY, int dataSize )
    {
        if ( dataSize < 0 || dataSize > dataX.length || dataSize > dataY.length )
        {
            throw new IllegalArgumentException( "Illegal dataSize: dataSize = " + dataSize + ", dataX.length = " + dataX.length + ", dataY.length = " + dataY.length );
        }

        this.dataBufferLock.lock( );
        try
        {
            this.dataSize = dataSize;

            if ( dataBuffer == null || dataBuffer.rewind( ).capacity( ) < dataSize * 2 )
            {
                this.dataBuffer = Buffers.newDirectFloatBuffer( dataSize * 2 );
            }

            // copy data from the provided arrays into the host memory buffer
            for ( int i = 0; i < dataSize; i++ )
            {
                this.dataBuffer.put( dataX[i] ).put( dataY[i] );
            }

            if ( this.enableSpatialIndex )
            {
                this.spatialIndex = new QuadTreeXys<IdXy>( QUAD_TREE_BIN_MAX );

                for ( int i = 0; i < dataSize; i++ )
                {
                    this.spatialIndex.add( new IdXy( i, dataX[i], dataY[i] ) );
                }
            }

            this.newData = true;
        }
        finally
        {
            this.dataBufferLock.unlock( );
        }
    }

    public void setColor( float[] dataZ, ColorMap scale )
    {
        this.dataBufferLock.lock( );
        try
        {
            if ( colorBuffer == null || colorBuffer.rewind( ).capacity( ) < dataSize * 4 )
            {
                this.colorBuffer = Buffers.newDirectFloatBuffer( dataSize * 4 );
            }

            float[] color = new float[4];

            // copy data from the provided arrays into the host memory buffer
            for ( int i = 0; i < dataSize; i++ )
            {
                scale.toColor( dataZ[i], color );

                this.colorBuffer.put( color[0] ).put( color[1] ).put( color[2] ).put( color[3] );
            }

            this.useColorHost = true;
        }
        finally
        {
            this.dataBufferLock.unlock( );
        }
    }

    public Collection<IdXy> selectGeoRange( double minX, double maxX, double minY, double maxY )
    {
        return enableSpatialIndex ? spatialIndex.search( ( float ) minX, ( float ) maxX, ( float ) minY, ( float ) maxY ) : null;
    }

    public Collection<IdXy> currentSelection( Axis2D axis )
    {
        double centerX = axis.getAxisX( ).getSelectionCenter( );
        double sizeX = axis.getAxisX( ).getSelectionSize( ) / 2.0f;

        double centerY = axis.getAxisY( ).getSelectionCenter( );
        double sizeY = axis.getAxisY( ).getSelectionSize( ) / 2.0f;

        double minX = centerX - sizeX;
        double maxX = centerX + sizeX;

        double minY = centerY - sizeY;
        double maxY = centerY + sizeY;

        return selectGeoRange( minX, maxX, minY, maxY );
    }

    public void setPointColor( float r, float g, float b, float a )
    {
        this.pointColor[0] = r;
        this.pointColor[1] = g;
        this.pointColor[2] = b;
        this.pointColor[3] = a;
    }

    public void setPointSize( float pointSize )
    {
        this.pointSize = pointSize;
    }

    @Override
    public void dispose( GLContext context )
    {
        if ( bufferInitialized )
        {
            context.getGL( ).glDeleteBuffers( 1, colorHandle, 0 );
            context.getGL( ).glDeleteBuffers( 1, bufferHandle, 0 );
        }
    }

    public static class IdXy implements Xy
    {
        private float x;
        private float y;
        private int id;

        public IdXy( int id, float x, float y )
        {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        public int id( )
        {
            return id;
        }

        @Override
        public float x( )
        {
            return x;
        }

        @Override
        public float y( )
        {
            return y;
        }
    }

    @Override
    public void paintTo( GL2 gl, GlimpseBounds bounds, Axis2D axis )
    {
        if ( dataSize == 0 ) return;

        if ( !bufferInitialized )
        {
            bufferHandle = new int[1];
            gl.glGenBuffers( 1, bufferHandle, 0 );

            colorHandle = new int[1];
            gl.glGenBuffers( 1, colorHandle, 0 );

            bufferInitialized = true;
        }

        this.dataBufferLock.lock( );
        try
        {
            if ( newData )
            {
                gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, bufferHandle[0] );

                // copy data from the host memory buffer to the device
                gl.glBufferData( GL2.GL_ARRAY_BUFFER, dataSize * 2 * BYTES_PER_FLOAT, dataBuffer.rewind( ), GL2.GL_DYNAMIC_DRAW );

                glHandleError( gl );

                useColorDevice = useColorHost;
                if ( useColorDevice )
                {
                    gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, colorHandle[0] );

                    // copy data from the host memory buffer to the device
                    gl.glBufferData( GL2.GL_ARRAY_BUFFER, dataSize * 4 * BYTES_PER_FLOAT, colorBuffer.rewind( ), GL2.GL_DYNAMIC_DRAW );

                    glHandleError( gl );
                }
            }

            newData = false;

            if ( useColorDevice )
            {
                gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, colorHandle[0] );
                gl.glColorPointer( 4, GL2.GL_FLOAT, 0, 0 );
                gl.glEnableClientState( GL2.GL_COLOR_ARRAY );
            }

            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, bufferHandle[0] );
            gl.glVertexPointer( 2, GL2.GL_FLOAT, 0, 0 );
            gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );

            gl.glColor4fv( pointColor, 0 );
            gl.glPointSize( pointSize );

            gl.glDrawArrays( GL2.GL_POINTS, 0, dataSize );
        }
        finally
        {
            this.dataBufferLock.unlock( );
        }
    }
}

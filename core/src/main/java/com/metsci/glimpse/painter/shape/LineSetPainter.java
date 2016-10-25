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

import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;

import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.shape.PointSetPainter.IdXy;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.line.LineUtils;
import com.metsci.glimpse.util.quadtree.QuadTreeXys;

/**
 * Efficiently draws a static set of line segments. Can also efficiently
 * query for the line segments contained in a particular region of the
 * plot using a {@link com.metsci.glimpse.util.quadtree.QuadTree}.
 *
 * @author ulman
 */
public class LineSetPainter extends GlimpsePainterBase
{
    public static final int QUAD_TREE_BIN_MAX = 1000;

    // spatial index on Points
    protected QuadTreeXys<IdXy> spatialIndex;
    protected boolean enableSpatialIndex;

    protected LineProgram prog;
    protected LinePath path;
    protected LineStyle style;

    public LineSetPainter( )
    {
        this( false );
    }

    public LineSetPainter( boolean enableSpatialIndex )
    {
        this.enableSpatialIndex = enableSpatialIndex;

        this.prog = new LineProgram( );
        this.path = new LinePath( );
        this.style = new LineStyle( );

        this.style.rgba = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        this.style.joinType = LineJoinType.JOIN_MITER;
        this.style.thickness_PX = 1.0f;
        this.style.stippleEnable = false;
        this.style.stippleScale = 1;
        this.style.stipplePattern = ( short ) 0x00FF;
    }

    public void setLineStyle( LineStyle style )
    {
        this.style = style;
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
        this.painterLock.lock( );
        try
        {
            int lineCount = Math.min( dataX.length, dataY.length );

            this.path.clear( );

            for ( int trackId = 0; trackId < lineCount; trackId++ )
            {
                float[] trackX = dataX[trackId];
                float[] trackY = dataY[trackId];

                int trackLength = Math.min( dataX[trackId].length, dataY[trackId].length );

                for ( int i = 0; i < trackLength; i++ )
                {
                    if ( i == 0 )
                        this.path.moveTo( trackX[i], trackY[i] );
                    else
                        this.path.lineTo( trackX[i], trackY[i] );
                }
            }

            if ( this.enableSpatialIndex )
            {
                this.spatialIndex = new QuadTreeXys<IdXy>( QUAD_TREE_BIN_MAX );

                int pointCount = 0;
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
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setLineColor( float r, float g, float b, float a )
    {
        this.style.rgba[0] = r;
        this.style.rgba[1] = g;
        this.style.rgba[2] = b;
        this.style.rgba[3] = a;
    }

    public void setLineColor( float[] color )
    {
        this.style.rgba = color;
    }

    public void setLineWidth( float size )
    {
        this.style.thickness_PX = size;
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        this.prog.dispose( context.getGL( ).getGL3( ) );
        this.path.dispose( context.getGL( ) );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = requireAxis2D( context );
        GL3 gl = context.getGL( ).getGL3( );

        enableStandardBlending( gl );
        try
        {
            prog.begin( gl );
            try
            {
                prog.setViewport( gl, bounds );
                prog.setAxisOrtho( gl, axis );
                prog.setStyle( gl, style );

                prog.draw( gl, style, path, LineUtils.ppvAspectRatio( axis ) );
            }
            finally
            {
                prog.end( gl );
            }
        }
        finally
        {
            GLUtils.disableBlending( gl );
        }
    }
}

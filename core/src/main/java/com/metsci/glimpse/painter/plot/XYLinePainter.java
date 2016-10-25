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
package com.metsci.glimpse.painter.plot;

import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.colormap.ColorMap;
import com.metsci.glimpse.support.shader.line.ColorLinePath;
import com.metsci.glimpse.support.shader.line.ColorLineProgram;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.line.LineUtils;
import com.metsci.glimpse.support.shader.point.PointArrayColorProgram;

/**
 * Plots a simple x-y lineplot. Provides options for modifying line thickness and color.
 *
 * @author ulman
 */
public class XYLinePainter extends GlimpsePainterBase
{
    protected static final float[] defaultColor = GlimpseColor.getBlack( );

    protected ColorLinePath path;
    protected ColorLineProgram prog;
    protected LineStyle style;
    
    protected PointArrayColorProgram pointProg;

    protected float pointSize = 8.0f;
    protected float pointFeather = 4.0f;
    
    protected boolean showPoints = true;
    protected boolean showLines = true;

    public XYLinePainter( )
    {
        this.path = new ColorLinePath( );
        this.prog = new ColorLineProgram( );
        this.style = new LineStyle( );
        this.style.thickness_PX = 3.5f;
        this.style.joinType = LineJoinType.JOIN_BEVEL;
        
        this.pointProg = new PointArrayColorProgram( );
    }

    public void setDataAndColor( double[] dataX, double[] dataY, double[] dataZ, ColorMap scale )
    {
        this.painterLock.lock( );
        try
        {
            int dataSize = Math.min( dataX.length, dataY.length );

            this.path.clear( );

            float[] rgba = new float[4];

            for ( int i = 0; i < dataSize; i++ )
            {
                scale.toColor( ( float ) dataZ[i], rgba );

                if ( i == 0 )
                    this.path.moveTo( ( float ) dataX[i], ( float ) dataY[i], rgba );
                else
                    this.path.lineTo( ( float ) dataX[i], ( float ) dataY[i], rgba );
            }
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setDataAndColor( float[] dataX, float[] dataY, float[] dataZ, ColorMap scale )
    {
        this.painterLock.lock( );
        try
        {
            int dataSize = Math.min( dataX.length, dataY.length );

            this.path.clear( );

            float[] rgba = new float[4];

            for ( int i = 0; i < dataSize; i++ )
            {
                scale.toColor( dataZ[i], rgba );

                if ( i == 0 )
                    this.path.moveTo( dataX[i], dataY[i], rgba );
                else
                    this.path.lineTo( dataX[i], dataY[i], rgba );
            }
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setDataAndColor( float[] dataX, float[] dataY, float[][] rgba )
    {
        this.painterLock.lock( );
        try
        {
            int dataSize = Math.min( dataX.length, dataY.length );

            this.path.clear( );

            for ( int i = 0; i < dataSize; i++ )
            {
                if ( i == 0 )
                    this.path.moveTo( dataX[i], dataY[i], rgba[i] );
                else
                    this.path.lineTo( dataX[i], dataY[i], rgba[i] );
            }
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setData( float[] dataX, float[] dataY )
    {
        this.setData( dataX, dataY, defaultColor );
    }
    
    public void setData( float[] dataX, float[] dataY, float[] color )
    {
        this.painterLock.lock( );
        try
        {
            int dataSize = Math.min( dataX.length, dataY.length );

            this.path.clear( );

            for ( int i = 0; i < dataSize; i++ )
            {
                if ( i == 0 )
                    this.path.moveTo( dataX[i], dataY[i], color );
                else
                    this.path.lineTo( dataX[i], dataY[i], color );
            }
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setLineStipple( boolean activate )
    {
        this.style.stippleEnable = activate;
    }

    public void setLineStipple( int stippleFactor, short stipplePattern )
    {
        this.style.stippleEnable = true;
        this.style.stippleScale = stippleFactor;
        this.style.stipplePattern = stipplePattern;
    }

    public void setLineThickness( float lineThickness )
    {
        this.style.thickness_PX = lineThickness;
    }

    public void setLineStyle( LineStyle style )
    {
        this.style = style;
    }

    public void setPointSize( float pointSize )
    {
        this.pointSize = pointSize;
    }
    
    public void setPointFeather( float pointFeather )
    {
        this.pointFeather = pointFeather;
    }

    public void showPoints( boolean show )
    {
        this.showPoints = show;
    }

    public void showLines( boolean show )
    {
        this.showLines = show;
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        this.path.dispose( context.getGL( ) );
        this.prog.dispose( context.getGL( ).getGL3( ) );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = requireAxis2D( context );
        double ppvAspectRatio = LineUtils.ppvAspectRatio( axis );

        GLUtils.enableStandardBlending( gl );
        try
        {
            if ( this.showLines )
            {
                this.prog.begin( gl );
                try
                {
                    this.prog.setAxisOrtho( gl, axis );
                    this.prog.setViewport( gl, bounds );

                    this.prog.draw( gl, style, path, ppvAspectRatio );
                }
                finally
                {
                    this.prog.end( gl );
                }
            }
            
            if ( this.showPoints )
            {
                this.pointProg.begin( gl );
                try
                {
                    this.pointProg.setAxisOrtho( gl, axis );
                    this.pointProg.setPointSize( gl, this.pointSize );
                    this.pointProg.setFeatherThickness( gl, this.pointFeather );

                    // skip the first and last phantom vertices
                    this.pointProg.draw( gl, path.xyVbo( gl ), path.rgbaVbo( gl ), 1, path.numVertices( )-2 );
                }
                finally
                {
                    this.pointProg.end( gl );
                }
            }
        }
        finally
        {
            GLUtils.disableBlending( gl );
        }
    }
}

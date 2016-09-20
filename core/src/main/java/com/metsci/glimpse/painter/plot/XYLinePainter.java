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

import java.util.logging.Logger;

import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.colormap.ColorMap;
import com.metsci.glimpse.support.line.ColorLinePath;
import com.metsci.glimpse.support.line.ColorLineProgram;
import com.metsci.glimpse.support.line.LineStyle;

/**
 * Plots a simple x-y lineplot. Provides options for modifying line thickness and color.
 *
 * @author ulman
 */
public class XYLinePainter extends GlimpsePainterBase
{
    private static final Logger logger = Logger.getLogger( XYLinePainter.class.getName( ) );

    //    protected float[] lineColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    //    protected float lineThickness = 1;

    //
    //    protected float pointSize = 6;

    //    protected int stippleFactor = 1;
    //    protected short stipplePattern = ( short ) 0x00FF;
    //    protected boolean stippleOn = false;

    //    protected int[] colorHandle = null;
    //    protected FloatBuffer colorBuffer = null;
    //    protected boolean useColorDevice = false;
    //    protected boolean useColorHost = false;

    //    protected int dataSize = 0;
    //    
    //    protected int[] bufferHandle = null;
    //    protected FloatBuffer dataBuffer = null;
    //
    //    protected ReentrantLock dataBufferLock = null;
    //
    //    protected volatile boolean newData = false;
    //    protected volatile boolean bufferInitialized = false;

    protected static final float[] defaultColor = GlimpseColor.getBlack( );

    protected ColorLinePath path;
    protected ColorLineProgram prog;
    protected LineStyle style;

    protected boolean showPoints = true;
    protected boolean showLines = true;

    public XYLinePainter( )
    {
        this.path = new ColorLinePath( );
        this.prog = new ColorLineProgram( );
        this.style = new LineStyle( );
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

    public void setData( double[] dataX, double[] dataY )
    {
        this.painterLock.lock( );
        try
        {
            int dataSize = Math.min( dataX.length, dataY.length );

            this.path.clear( );

            for ( int i = 0; i < dataSize; i++ )
            {
                if ( i == 0 )
                    this.path.moveTo( ( float ) dataX[i], ( float ) dataY[i], defaultColor );
                else
                    this.path.lineTo( ( float ) dataX[i], ( float ) dataY[i], defaultColor );
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

    //    public void setPointSize( float pointSize )
    //    {
    //        this.pointSize = pointSize;
    //    }

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

                    this.prog.draw( gl, style, path );
                }
                finally
                {
                    this.prog.end( gl );
                }
            }
        }
        finally
        {
            GLUtils.disableBlending( gl );
        }
    }
}

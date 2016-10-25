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
package com.metsci.glimpse.painter.decoration;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.triangle.ArrayColorProgram;

/**
 * An alternative {@link BorderPainter} which displays alternating
 * white and black lines (to emulate a geographic map border).
 *
 * @author ulman
 */
public class MapBorderPainter extends GlimpsePainterBase
{
    protected float[] outerColor = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
    protected float[] innerColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

    protected int borderSize = 20;

    protected AxisLabelHandler ticksX;
    protected AxisLabelHandler ticksY;

    protected double savedValueX = -1;
    protected boolean savedOrientX = false;

    protected double savedValueY = -1;
    protected boolean savedOrientY = false;

    protected ArrayColorProgram fillProg;
    protected GLEditableBuffer inXys;
    protected GLEditableBuffer inRgba;

    protected LineProgram lineProg;
    protected LineStyle lineStyle;
    protected LinePath linePath;

    public MapBorderPainter( AxisLabelHandler ticksX, AxisLabelHandler ticksY )
    {
        this.ticksX = ticksX;
        this.ticksY = ticksY;

        this.fillProg = new ArrayColorProgram( );

        this.inXys = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
        this.inRgba = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );

        this.lineProg = new LineProgram( );

        this.lineStyle = new LineStyle( );
        this.lineStyle.feather_PX = 0;
        this.lineStyle.thickness_PX = 1.0f;

        this.linePath = new LinePath( );
    }

    public int getBorderSize( )
    {
        return borderSize;
    }

    public void setBorderSize( int size )
    {
        borderSize = size;
    }

    public void setInnerColor( float r, float g, float b, float a )
    {
        innerColor[0] = r;
        innerColor[1] = g;
        innerColor[2] = b;
        innerColor[3] = a;
    }

    public void setInnerColor( float[] rgba )
    {
        innerColor = rgba;
    }

    public void setOuterColor( float r, float g, float b, float a )
    {
        outerColor[0] = r;
        outerColor[1] = g;
        outerColor[2] = b;
        outerColor[3] = a;
    }

    public void setOuterColor( float[] rgba )
    {
        outerColor = rgba;
    }

    private float[] getColor( int i, boolean orient )
    {
        if ( i % 2 == 0 )
        {
            if ( orient )
            {
                return outerColor;
            }
            else
            {
                return innerColor;
            }
        }
        else
        {
            if ( orient )
            {
                return innerColor;
            }
            else
            {
                return outerColor;
            }
        }
    }

    private boolean innerOrOuterFirstX( double[] ticks )
    {
        for ( int i = 0; i < ticks.length; i++ )
        {
            if ( ticks[i] == savedValueX )
            {
                if ( i % 2 == 0 )
                {
                    return savedOrientX;
                }
                else
                {
                    return !savedOrientX;
                }
            }
        }

        savedValueX = ticks[ticks.length / 2];
        savedOrientX = true;
        return savedOrientX;
    }

    private boolean innerOrOuterFirstY( double[] ticks )
    {
        for ( int i = 0; i < ticks.length; i++ )
        {
            if ( ticks[i] == savedValueY )
            {
                if ( i % 2 == 0 )
                {
                    return savedOrientY;
                }
                else
                {
                    return !savedOrientY;
                }
            }
        }

        savedValueY = ticks[ticks.length / 2];
        savedOrientY = true;
        return savedOrientY;
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        if ( ticksX == null || ticksY == null ) return;

        GL3 gl = context.getGL( ).getGL3( );
        Axis2D axis = requireAxis2D( context );
        GlimpseBounds bounds = getBounds( context );

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        if ( axisX == null || axisY == null ) return;

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        double[] xPositions = ticksX.getTickPositions( axis.getAxisX( ) );
        double[] yPositions = ticksY.getTickPositions( axis.getAxisY( ) );

        boolean orientX = innerOrOuterFirstX( xPositions );
        boolean orientY = innerOrOuterFirstY( yPositions );

        inXys.clear( );
        inRgba.clear( );

        for ( int i = 0; i < xPositions.length; i++ )
        {
            float[] color1 = getColor( i, orientX );

            int pos1X = axisX.valueToScreenPixel( xPositions[i] );
            int pos2X = i == xPositions.length - 1 ? width : axisX.valueToScreenPixel( xPositions[i + 1] );

            inXys.growQuad2f( pos1X, 0, pos2X, borderSize );
            inRgba.growQuadSolidColor( color1 );

            float[] color2 = getColor( i, !orientX );

            inXys.growQuad2f( pos1X, height - borderSize, pos2X, height );
            inRgba.growQuadSolidColor( color2 );
        }

        for ( int i = 0; i < yPositions.length; i++ )
        {
            float[] color1 = getColor( i, orientY );

            int pos1Y = axisY.valueToScreenPixel( yPositions[i] );
            int pos2Y = i == yPositions.length - 1 ? height : axisY.valueToScreenPixel( yPositions[i + 1] );

            inXys.growQuad2f( 0, pos1Y, borderSize, pos2Y );
            inRgba.growQuadSolidColor( color1 );

            float[] color2 = getColor( i, !orientY );

            inXys.growQuad2f( width - borderSize, pos1Y, width, pos2Y );
            inRgba.growQuadSolidColor( color2 );
        }

        addFillCorners( width, height );

        fillProg.begin( gl );
        try
        {
            fillProg.setPixelOrtho( gl, bounds );

            fillProg.draw( gl, inXys, inRgba );
        }
        finally
        {
            fillProg.end( gl );
        }

        linePath.clear( );

        addLineCorners( width, height );

        linePath.moveTo( borderSize, borderSize );
        linePath.lineTo( borderSize, height - borderSize );
        linePath.lineTo( width - borderSize, height - borderSize );
        linePath.lineTo( width - borderSize, borderSize );
        linePath.lineTo( borderSize, borderSize );

        linePath.moveTo( 0.5f, 0.5f );
        linePath.lineTo( 0.5f, height - 0.5f );
        linePath.lineTo( width - 0.5f, height - 0.5f );
        linePath.lineTo( width - 0.5f, 0.5f );
        linePath.lineTo( 0.5f, 0.5f );

        lineProg.begin( gl );
        try
        {
            lineStyle.rgba = outerColor;
            lineProg.setPixelOrtho( gl, bounds );
            lineProg.setViewport( gl, bounds );

            lineProg.draw( gl, lineStyle, linePath );
        }
        finally
        {
            lineProg.end( gl );
        }
    }

    private void addLineCorners( int width, int height )
    {
        linePath.addRectangle( 0, 0, borderSize, borderSize );
        linePath.addRectangle( 0, height, borderSize, height - borderSize );
        linePath.addRectangle( width, 0, width - borderSize, borderSize );
        linePath.addRectangle( width - borderSize, height - borderSize, width, height );
    }

    private void addFillCorners( int width, int height )
    {
        inXys.growQuad2f( 0, 0, borderSize, borderSize );
        inXys.growQuad2f( 0, height, borderSize, height - borderSize );
        inXys.growQuad2f( width, 0, width - borderSize, borderSize );
        inXys.growQuad2f( width - borderSize, height - borderSize, width, height );

        inRgba.growQuadSolidColor( innerColor );
        inRgba.growQuadSolidColor( innerColor );
        inRgba.growQuadSolidColor( innerColor );
        inRgba.growQuadSolidColor( innerColor );
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        fillProg.dispose( context.getGL( ).getGL3( ) );
        inXys.dispose( context.getGL( ) );
        inRgba.dispose( context.getGL( ) );

        lineProg.dispose( context.getGL( ).getGL3( ) );
        linePath.dispose( context.getGL( ) );
    }
}

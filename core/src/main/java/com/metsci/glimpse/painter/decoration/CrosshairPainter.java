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
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

/**
 * Displays crosshairs and a selection box centered over the position
 * of the mouse cursor within the plot. The selected region of a plot
 * can be locked using the middle mouse button. The size of the selection
 * box can be increased or decreased by holding down the ctrl key
 * and scrolling the mouse wheel. Either of these behaviors can be altered
 * by using a modified {@link com.metsci.glimpse.axis.listener.mouse.AxisMouseListener}
 * subclass.
 *
 * @author ulman
 */
public class CrosshairPainter extends GlimpsePainterBase
{
    protected float[] xorColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    protected float[] cursorColor = new float[] { 0.0f, 0.769f, 1.0f, 1.0f };
    protected float[] shadeColor = new float[] { 0.0f, 0.769f, 1.0f, 0.25f };

    protected boolean showSelectionBox = true;
    protected boolean shadeSelectionBox = false;
    protected boolean showSelectionCrosshairs = true;
    protected boolean hideHorizontalHairs = false;
    protected boolean hideVerticalHairs = false;
    protected boolean paintXor = false;

    protected boolean colorSet = false;

    protected FlatColorProgram flatProg;
    protected GLEditableBuffer flatPath;

    protected LineProgram lineProg;
    protected LineStyle lineStyle;
    protected LinePath linePath;

    public CrosshairPainter( )
    {
        this.lineProg = new LineProgram( );
        this.flatProg = new FlatColorProgram( );

        this.lineStyle = new LineStyle( );
        this.lineStyle.feather_PX = 0;
        this.lineStyle.stippleEnable = false;
        this.lineStyle.thickness_PX = 1.0f;
        this.lineStyle.joinType = LineJoinType.JOIN_MITER;

        this.linePath = new LinePath( );
        this.flatPath = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
    }

    public void setCursorColor( float[] rgba )
    {
        this.cursorColor = rgba;
        this.colorSet = true;
    }

    public void setCursorColor( float r, float g, float b, float a )
    {
        this.cursorColor[0] = r;
        this.cursorColor[1] = g;
        this.cursorColor[2] = b;
        this.cursorColor[3] = a;
        this.colorSet = true;
    }

    public void setShadeColor( float[] rgba )
    {
        this.shadeColor = rgba;
    }

    public void setShadeColor( float r, float g, float b, float a )
    {
        this.shadeColor[0] = r;
        this.shadeColor[1] = g;
        this.shadeColor[2] = b;
        this.shadeColor[3] = a;
    }

    public void setHideVerticalHairs( boolean doHide )
    {
        hideVerticalHairs = doHide;
    }

    public void setHideHorizontalHairs( boolean doHide )
    {
        hideHorizontalHairs = doHide;
    }

    public void setLineWidth( float width )
    {
        this.lineStyle.thickness_PX = width;
    }

    public void showSelectionBox( boolean show )
    {
        this.showSelectionBox = show;
    }

    public void setShadeSelectionBox( boolean doShade )
    {
        shadeSelectionBox = doShade;
    }

    public void showSelectionCrosshairs( boolean show )
    {
        this.showSelectionCrosshairs = show;
        this.hideHorizontalHairs = false;
        this.hideVerticalHairs = false;
    }

    public void setXor( boolean xor )
    {
        this.paintXor = xor;
    }

    private void conditionallyEnableXor( GL3 gl )
    {
        if ( paintXor )
        {
            gl.glEnable( GL3.GL_COLOR_LOGIC_OP );
            gl.glLogicOp( GL3.GL_XOR );
        }
    }

    private void conditionallyDisableXor( GL gl )
    {
        if ( paintXor )
        {
            gl.glDisable( GL3.GL_COLOR_LOGIC_OP );
        }
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // ignore the look and feel if a color has been manually set
        if ( !colorSet )
        {
            setCursorColor( laf.getColor( AbstractLookAndFeel.CROSSHAIR_COLOR ) );
            colorSet = false;
        }
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );
        Axis2D axis = requireAxis2D( context );
        GlimpseBounds bounds = getBounds( context );

        if ( axis == null || axis.getAxisX( ) == null || axis.getAxisY( ) == null ) return;

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );
        float minX = ( float ) Math.min( axisX.getMax( ), axisX.getMin( ) );
        float maxX = ( float ) Math.max( axisX.getMax( ), axisX.getMin( ) );
        float minY = ( float ) Math.min( axisY.getMax( ), axisY.getMin( ) );
        float maxY = ( float ) Math.max( axisY.getMax( ), axisY.getMin( ) );

        float centerX = ( float ) axisX.getSelectionCenter( );
        float sizeX = ( float ) axisX.getSelectionSize( ) / 2;

        float centerY = ( float ) axisY.getSelectionCenter( );
        float sizeY = ( float ) axisY.getSelectionSize( ) / 2;

        if ( showSelectionBox )
        {
            lineStyle.rgba = paintXor ? xorColor : cursorColor;

            conditionallyEnableXor( gl );
            lineProg.begin( gl );
            try
            {
                lineProg.setAxisOrtho( gl, axis );
                lineProg.setViewport( gl, bounds );

                linePath.clear( );
                linePath.addRectangle( centerX - sizeX, centerY - sizeY, centerX + sizeX, centerY + sizeY );

                lineProg.draw( gl, lineStyle, linePath );
            }
            finally
            {
                lineProg.end( gl );
                conditionallyDisableXor( gl );
            }

            if ( shadeSelectionBox )
            {
                GLUtils.enableStandardBlending( gl );
                flatProg.begin( gl );
                try
                {
                    flatProg.setAxisOrtho( gl, axis );

                    flatPath.clear( );
                    flatPath.growQuad2f( centerX - sizeX, centerY - sizeY, centerX + sizeX, centerY + sizeY );

                    flatProg.draw( gl, flatPath, shadeColor );
                }
                finally
                {
                    flatProg.end( gl );
                    GLUtils.disableBlending( gl );
                }
            }
        }

        if ( showSelectionCrosshairs )
        {
            if ( showSelectionBox )
            {
                lineStyle.rgba = paintXor ? xorColor : cursorColor;

                conditionallyEnableXor( gl );
                lineProg.begin( gl );
                try
                {
                    lineProg.setAxisOrtho( gl, axis );
                    lineProg.setViewport( gl, bounds );

                    linePath.clear( );

                    if ( !hideVerticalHairs )
                    {
                        linePath.moveTo( centerX, minY );
                        linePath.lineTo( centerX, centerY - sizeY );

                        linePath.moveTo( centerX, centerY + sizeY );
                        linePath.lineTo( centerX, maxY );
                    }

                    if ( !hideHorizontalHairs )
                    {
                        linePath.moveTo( minX, centerY );
                        linePath.lineTo( centerX - sizeX, centerY );

                        linePath.moveTo( centerX + sizeX, centerY );
                        linePath.lineTo( maxX, centerY );
                    }

                    lineProg.draw( gl, lineStyle, linePath );

                }
                finally
                {
                    lineProg.end( gl );
                    conditionallyDisableXor( gl );
                }
            }
            else
            {
                lineStyle.rgba = paintXor ? xorColor : cursorColor;

                conditionallyEnableXor( gl );
                lineProg.begin( gl );
                try
                {
                    lineProg.setAxisOrtho( gl, axis );
                    lineProg.setViewport( gl, bounds );

                    linePath.clear( );

                    if ( !hideVerticalHairs )
                    {
                        linePath.moveTo( centerX, minY );
                        linePath.lineTo( centerX, maxY );
                    }

                    if ( !hideHorizontalHairs )
                    {
                        linePath.moveTo( minX, centerY );
                        linePath.lineTo( maxX, centerY );
                    }

                    lineProg.draw( gl, lineStyle, linePath );

                }
                finally
                {
                    lineProg.end( gl );
                    conditionallyDisableXor( gl );
                }
            }
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        lineProg.dispose( context.getGL( ).getGL3( ) );
        linePath.dispose( context.getGL( ) );

        flatProg.dispose( context.getGL( ).getGL3( ) );
        flatPath.dispose( context.getGL( ) );
    }
}

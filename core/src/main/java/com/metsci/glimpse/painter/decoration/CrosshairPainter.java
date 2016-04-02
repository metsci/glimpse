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
import javax.media.opengl.GL2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

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
public class CrosshairPainter extends GlimpseDataPainter2D
{
    protected float[] xorColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    protected float[] cursorColor = new float[] { 0.0f, 0.769f, 1.0f, 1.0f };
    protected float[] shadeColor = new float[] { 0.0f, 0.769f, 1.0f, 0.25f };

    protected float lineWidth = 2.0f;

    protected boolean showSelectionBox = true;
    protected boolean shadeSelectionBox = false;
    protected boolean showSelectionCrosshairs = true;
    protected boolean hideHorizontalHairs = false;
    protected boolean hideVerticalHairs = false;
    protected boolean paintXor = false;

    protected boolean colorSet = false;

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
        this.lineWidth = width;
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

    private void conditionallyEnableXor( GL2 gl )
    {
        if ( paintXor )
        {
            gl.glEnable( GL2.GL_COLOR_LOGIC_OP );
            gl.glLogicOp( GL2.GL_XOR );
        }
    }

    private void conditionallyDisableXor( GL gl )
    {
        if ( paintXor )
        {
            gl.glDisable( GL2.GL_COLOR_LOGIC_OP );
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
    public void paintTo( GL2 gl, GlimpseBounds bounds, Axis2D axis )
    {
        if ( axis == null || axis.getAxisX( ) == null || axis.getAxisY( ) == null ) return;

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );
        double minX = Math.min( axisX.getMax( ), axisX.getMin( ) );
        double maxX = Math.max( axisX.getMax( ), axisX.getMin( ) );
        double minY = Math.min( axisY.getMax( ), axisY.getMin( ) );
        double maxY = Math.max( axisY.getMax( ), axisY.getMin( ) );

        double centerX = axisX.getSelectionCenter( );
        double sizeX = axisX.getSelectionSize( ) / 2;

        double centerY = axisY.getSelectionCenter( );
        double sizeY = axisY.getSelectionSize( ) / 2;

        if ( showSelectionBox )
        {
            gl.glLineWidth( lineWidth );
            gl.glColor4fv( paintXor ? xorColor : cursorColor, 0 );

            conditionallyEnableXor( gl );

            gl.glBegin( GL2.GL_LINE_LOOP );
            try
            {
                gl.glVertex2d( centerX - sizeX, centerY - sizeY );
                gl.glVertex2d( centerX - sizeX, centerY + sizeY );
                gl.glVertex2d( centerX + sizeX, centerY + sizeY );
                gl.glVertex2d( centerX + sizeX, centerY - sizeY );
            }
            finally
            {
                gl.glEnd( );
                conditionallyDisableXor( gl );
            }

            if ( shadeSelectionBox )
            {
                gl.glColor4fv( shadeColor, 0 );
                gl.glBegin( GL2.GL_QUADS );
                try
                {
                    gl.glVertex2d( centerX - sizeX, centerY - sizeY );
                    gl.glVertex2d( centerX - sizeX, centerY + sizeY );
                    gl.glVertex2d( centerX + sizeX, centerY + sizeY );
                    gl.glVertex2d( centerX + sizeX, centerY - sizeY );
                }
                finally
                {
                    gl.glEnd( );
                }
            }
        }

        if ( showSelectionCrosshairs )
        {
            if ( showSelectionBox )
            {
                gl.glLineWidth( lineWidth );
                gl.glColor4fv( paintXor ? xorColor : cursorColor, 0 );

                conditionallyEnableXor( gl );

                gl.glBegin( GL2.GL_LINES );
                try
                {
                    if ( !hideVerticalHairs )
                    {
                        gl.glVertex2d( centerX, minY );
                        gl.glVertex2d( centerX, centerY - sizeY );

                        gl.glVertex2d( centerX, centerY + sizeY );
                        gl.glVertex2d( centerX, maxY );
                    }

                    if ( !hideHorizontalHairs )
                    {
                        gl.glVertex2d( minX, centerY );
                        gl.glVertex2d( centerX - sizeX, centerY );

                        gl.glVertex2d( centerX + sizeX, centerY );
                        gl.glVertex2d( maxX, centerY );
                    }
                }
                finally
                {
                    gl.glEnd( );
                    conditionallyDisableXor( gl );
                }
            }
            else
            {
                gl.glLineWidth( lineWidth );
                gl.glColor4fv( paintXor ? xorColor : cursorColor, 0 );

                conditionallyEnableXor( gl );

                gl.glBegin( GL2.GL_LINES );
                try
                {
                    if ( !hideVerticalHairs )
                    {
                        gl.glVertex2d( centerX, minY );
                        gl.glVertex2d( centerX, maxY );
                    }

                    if ( !hideHorizontalHairs )
                    {
                        gl.glVertex2d( minX, centerY );
                        gl.glVertex2d( maxX, centerY );
                    }
                }
                finally
                {
                    gl.glEnd( );
                    conditionallyDisableXor( gl );
                }
            }
        }
    }
}

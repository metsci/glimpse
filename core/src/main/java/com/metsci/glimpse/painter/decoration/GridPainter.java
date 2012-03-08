/*
 * Copyright (c) 2012, Metron, Inc.
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

import static java.lang.Integer.parseInt;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * Displays dotted horizontal and vertical grid lines.
 *
 * @author ulman
 */
public class GridPainter extends GlimpsePainter2D
{
    protected float[] majorLineColor = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
    protected int majorLineThickness = 1;

    protected float[] minorLineColor = new float[] { 0.5f, 0.5f, 0.5f, 0.15f };
    protected int minorLineThickness = 1;

    protected boolean showHorizontal = true;
    protected boolean showVertical = true;

    protected boolean showMinorTicks = true;

    protected AxisLabelHandler ticksX;
    protected AxisLabelHandler ticksY;

    public GridPainter( AxisLabelHandler ticksX, AxisLabelHandler ticksY )
    {
        this.ticksX = ticksX;
        this.ticksY = ticksY;
    }

    public GridPainter( )
    {
        this( new GridAxisLabelHandler( ), new GridAxisLabelHandler( ) );
    }

    public GridPainter setLineColor( float r, float g, float b, float a )
    {
        majorLineColor[0] = r;
        majorLineColor[1] = g;
        majorLineColor[2] = b;
        majorLineColor[3] = a;

        return this;
    }

    public GridPainter setLineColor( float[] rgba )
    {
        this.majorLineColor = rgba;

        return this;
    }

    public GridPainter setMinorLineColor( float[] rgba )
    {
        this.minorLineColor = rgba;

        return this;
    }

    public GridPainter setShowMinorGrid( boolean showMinorTicks )
    {
        this.showMinorTicks = showMinorTicks;

        return this;
    }

    public GridPainter setShowVerticalLines( boolean show )
    {
        this.showVertical = show;

        return this;
    }

    public GridPainter setShowHorizontalLines( boolean show )
    {
        this.showHorizontal = show;

        return this;
    }

    public GridPainter setTickHandlerX( AxisLabelHandler ticksX )
    {
        this.ticksX = ticksX;

        return this;
    }

    public GridPainter setTickHandlerY( AxisLabelHandler ticksY )
    {
        this.ticksY = ticksY;

        return this;
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        if ( ticksX == null || ticksY == null || axis == null || axis.getAxisX( ) == null || axis.getAxisY( ) == null || width <= 0 || height <= 0 ) return;

        GL gl = context.getGL( );

        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( -0.5, width - 1 + 0.5f, -0.5, height - 1 + 0.5f, -1, 1 );

        gl.glEnable( GL.GL_LINE_STIPPLE );
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL.GL_BLEND );

        double[] xTicks = ticksX.getTickPositions( axis.getAxisX( ) );

        AxisUnitConverter converterX = ticksX.getAxisUnitConverter( );
        converterX = converterX == null ? new AxisUnitConverter( )
        {
            public double toAxisUnits( double value )
            {
                return value;
            }

            public double fromAxisUnits( double value )
            {
                return value;
            }
        } : converterX;
        Axis1D axisX = axis.getAxisX( );

        double[] yTicks = ticksY.getTickPositions( axis.getAxisY( ) );
        AxisUnitConverter converterY = ticksY.getAxisUnitConverter( );
        converterY = converterY == null ? new AxisUnitConverter( )
        {
            public double toAxisUnits( double value )
            {
                return value;
            }

            public double fromAxisUnits( double value )
            {
                return value;
            }
        } : converterY;
        Axis1D axisY = axis.getAxisY( );

        //////////////
        //////////////  Vertical Lines
        //////////////

        if ( showVertical )
        {
            gl.glLineWidth( majorLineThickness );
            gl.glColor4fv( majorLineColor, 0 );

            // make the stipple look like it's translating during drags
            int stipplePhase = ( int ) ( axisY.valueToScreenPixelUnits( converterY.fromAxisUnits( 0 ) ) ) % 4;
            stipplePhase = stipplePhase < 0 ? stipplePhase + 4 : stipplePhase;

            switch ( stipplePhase )
            {
            case 3:
                gl.glLineStipple( 1, ( short ) parseInt( "1100110011001100", 2 ) );
                break;
            case 2:
                gl.glLineStipple( 1, ( short ) parseInt( "0110011001100110", 2 ) );
                break;
            case 1:
                gl.glLineStipple( 1, ( short ) parseInt( "0011001100110011", 2 ) );
                break;
            case 0:
                gl.glLineStipple( 1, ( short ) parseInt( "1001100110011001", 2 ) );
                break;
            default:
                break;
            }

            for ( int i = 0; i < xTicks.length; i++ )
            {
                int iTick = axisX.valueToScreenPixel( converterX.fromAxisUnits( xTicks[i] ) );

                // keep the last tick on the screen
                if ( iTick == width ) iTick -= 1;

                // don't draw ticks off the screen
                if ( iTick < 0 )
                {
                    continue;
                }
                else if ( iTick > width )
                {
                    break;
                }

                gl.glBegin( GL.GL_LINES );
                gl.glVertex2f( iTick, ( float ) 0 );
                gl.glVertex2f( iTick, ( float ) height );
                gl.glEnd( );
            }

            if ( showMinorTicks )
            {
                GlimpseColor.glColor( gl, majorLineColor, 0.1f );
                double[] xMinor = ticksX.getMinorTickPositions( xTicks );

                for ( int i = 0; i < xMinor.length; i++ )
                {
                    int iTick = axisX.valueToScreenPixel( converterX.fromAxisUnits( xMinor[i] ) );

                    gl.glBegin( GL.GL_LINES );
                    gl.glVertex2f( iTick, ( float ) 0 );
                    gl.glVertex2f( iTick, ( float ) height );
                    gl.glEnd( );
                }
            }
        }

        //////////////
        //////////////  Horizontal Lines
        //////////////

        if ( showHorizontal )
        {
            gl.glLineWidth( majorLineThickness );
            gl.glColor4fv( majorLineColor, 0 );

            // make the stipple look like it's translating during drags
            int stipplePhase = ( int ) ( axisX.valueToScreenPixelUnits( converterX.fromAxisUnits( 0 ) ) ) % 4;
            stipplePhase = stipplePhase < 0 ? stipplePhase + 4 : stipplePhase;

            switch ( stipplePhase )
            {
            case 3:
                gl.glLineStipple( 1, ( short ) parseInt( "1100110011001100", 2 ) );
                break;
            case 2:
                gl.glLineStipple( 1, ( short ) parseInt( "0110011001100110", 2 ) );
                break;
            case 1:
                gl.glLineStipple( 1, ( short ) parseInt( "0011001100110011", 2 ) );
                break;
            case 0:
                gl.glLineStipple( 1, ( short ) parseInt( "1001100110011001", 2 ) );
                break;
            default:
                break;
            }

            for ( int i = 0; i < yTicks.length; i++ )
            {
                int jTick = axisY.valueToScreenPixel( converterY.fromAxisUnits( yTicks[i] ) );

                // keep the last tick on the screen
                if ( jTick == height ) jTick -= 1;

                // don't draw ticks off the screen
                if ( jTick < 0 )
                {
                    continue;
                }
                else if ( jTick > height )
                {
                    break;
                }

                gl.glBegin( GL.GL_LINES );
                gl.glVertex2f( ( float ) 0, jTick );
                gl.glVertex2f( ( float ) width, jTick );
                gl.glEnd( );
            }

            if ( showMinorTicks )
            {
                GlimpseColor.glColor( gl, majorLineColor, 0.1f );
                double[] yMinor = ticksY.getMinorTickPositions( yTicks );

                for ( int i = 0; i < yMinor.length; i++ )
                {
                    int jTick = axisY.valueToScreenPixel( converterY.fromAxisUnits( yMinor[i] ) );

                    gl.glBegin( GL.GL_LINES );
                    gl.glVertex2f( ( float ) 0, jTick );
                    gl.glVertex2f( ( float ) width, jTick );
                    gl.glEnd( );
                }
            }
        }
    }
}

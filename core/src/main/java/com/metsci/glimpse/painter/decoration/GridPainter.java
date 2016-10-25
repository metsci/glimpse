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

import static java.lang.Integer.parseInt;

import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;

/**
 * Displays dotted horizontal and vertical grid lines.
 *
 * @author ulman
 */
public class GridPainter extends GlimpsePainterBase
{
    protected static final short CASE3 = ( short ) parseInt( "1100110011001100", 2 );
    protected static final short CASE2 = ( short ) parseInt( "0110011001100110", 2 );
    protected static final short CASE1 = ( short ) parseInt( "0011001100110011", 2 );
    protected static final short CASE0 = ( short ) parseInt( "1001100110011001", 2 );

    protected float[] majorLineColor = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
    protected int majorLineThickness = 1;

    protected float[] minorLineColor = new float[] { 0.5f, 0.5f, 0.5f, 0.15f };
    protected int minorLineThickness = 1;

    protected boolean showHorizontal = true;
    protected boolean showVertical = true;

    protected boolean showMinorTicks = true;

    protected boolean stipple = true;

    protected AxisLabelHandler ticksX;
    protected AxisLabelHandler ticksY;

    protected LineProgram prog;
    protected LineStyle style;
    protected LinePath path;

    public GridPainter( AxisLabelHandler ticksX, AxisLabelHandler ticksY )
    {
        this.ticksX = ticksX;
        this.ticksY = ticksY;

        this.prog = new LineProgram( );

        this.style = new LineStyle( );
        this.style.feather_PX = 0;
        this.style.stippleEnable = false;
        this.style.stipplePattern = ( short ) 0x00FF;
        this.style.stippleScale = 1;

        this.path = new LinePath( );
    }

    public GridPainter( )
    {
        this( new GridAxisLabelHandler( ), new GridAxisLabelHandler( ) );
    }

    public void setDotted( boolean dotted )
    {
        this.stipple = dotted;
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

    // make the stipple look like it's translating during drags
    protected short getStipplePattern( Axis1D axis, AxisUnitConverter converter )
    {
        int stipplePhase = ( int ) ( axis.valueToScreenPixelUnits( converter.fromAxisUnits( 0 ) ) ) % 4;
        stipplePhase = stipplePhase < 0 ? stipplePhase + 4 : stipplePhase;
        return getStipplePattern( stipplePhase );
    }

    protected short getStipplePattern( int stipplePhase )
    {
        switch ( stipplePhase )
        {
            case 3:
                return CASE3;
            case 2:
                return CASE2;
            case 1:
                return CASE1;
            case 0:
                return CASE0;
            default:
                return CASE0;
        }
    }

    protected void setStippleStyle( Axis1D axis, AxisUnitConverter converter )
    {
        if ( this.stipple )
        {
            style.stippleEnable = true;
            style.stipplePattern = getStipplePattern( axis, converter );
            style.stippleScale = 1.0f;
        }
        else
        {
            style.stippleEnable = false;
        }
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );
        Axis2D axis = requireAxis2D( context );
        GlimpseBounds bounds = getBounds( context );

        if ( ticksX == null || ticksY == null ) return;

        double[] xTicks = ticksX.getTickPositions( axis.getAxisX( ) );

        AxisUnitConverter converterX = ticksX.getAxisUnitConverter( );
        converterX = converterX == null ? AxisUnitConverters.identity : converterX;
        Axis1D axisX = axis.getAxisX( );

        double[] yTicks = ticksY.getTickPositions( axis.getAxisY( ) );
        AxisUnitConverter converterY = ticksY.getAxisUnitConverter( );
        converterY = converterY == null ? AxisUnitConverters.identity : converterY;
        Axis1D axisY = axis.getAxisY( );

        //////////////
        //////////////  Vertical Lines
        //////////////

        GLUtils.enableStandardBlending( gl );
        prog.begin( gl );
        try
        {
            prog.setAxisOrtho( gl, axis );
            prog.setViewport( gl, bounds );

            if ( showVertical )
            {
                style.thickness_PX = majorLineThickness;
                style.rgba = majorLineColor;
                setStippleStyle( axisY, converterY );

                path.clear( );

                for ( int i = 0; i < xTicks.length; i++ )
                {
                    double iTick = converterX.fromAxisUnits( xTicks[i] );
                    path.moveTo( ( float ) iTick, ( float ) axis.getMinY( ) );
                    path.lineTo( ( float ) iTick, ( float ) axis.getMaxY( ) );
                }

                prog.draw( gl, style, path );

                if ( showMinorTicks )
                {
                    style.thickness_PX = minorLineThickness;
                    style.rgba = minorLineColor;
                    style.stippleEnable = false;

                    double[] xMinor = ticksX.getMinorTickPositions( xTicks );

                    path.clear( );

                    for ( int i = 0; i < xMinor.length; i++ )
                    {
                        double iTick = converterX.fromAxisUnits( xMinor[i] );
                        path.moveTo( ( float ) iTick, ( float ) axis.getMinY( ) );
                        path.lineTo( ( float ) iTick, ( float ) axis.getMaxY( ) );
                    }

                    prog.draw( gl, style, path );
                }
            }

            //////////////
            //////////////  Horizontal Lines
            //////////////

            if ( showHorizontal )
            {
                style.thickness_PX = majorLineThickness;
                style.rgba = majorLineColor;
                setStippleStyle( axisX, converterX );

                path.clear( );

                for ( int i = 0; i < yTicks.length; i++ )
                {
                    double jTick = converterY.fromAxisUnits( yTicks[i] );
                    path.moveTo( ( float ) axis.getMinX( ), ( float ) jTick );
                    path.lineTo( ( float ) axis.getMaxX( ), ( float ) jTick );
                }

                prog.draw( gl, style, path );

                if ( showMinorTicks )
                {
                    style.thickness_PX = minorLineThickness;
                    style.rgba = minorLineColor;
                    style.stippleEnable = false;

                    double[] yMinor = ticksY.getMinorTickPositions( yTicks );

                    path.clear( );

                    for ( int i = 0; i < yMinor.length; i++ )
                    {
                        double jTick = converterY.fromAxisUnits( yMinor[i] );
                        path.moveTo( ( float ) axis.getMinX( ), ( float ) jTick );
                        path.lineTo( ( float ) axis.getMaxX( ), ( float ) jTick );
                    }

                    prog.draw( gl, style, path );
                }
            }
        }
        finally
        {
            prog.end( gl );
            GLUtils.disableBlending( gl );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        prog.dispose( context.getGL( ).getGL3( ) );
        path.dispose( context.getGL( ) );
    }
}

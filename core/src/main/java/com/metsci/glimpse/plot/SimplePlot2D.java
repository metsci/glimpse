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
package com.metsci.glimpse.plot;

import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.base.GlimpsePainterCallback;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;

/**
 * Identical in structure to {@link Plot2D}, but also provides
 * a pre-selected set of common plot elements including:
 *
 * <ul>
 *  <li> a solid color background to the plot
 *  <li> a simple line border around the plotting area
 *  <li> vertical and horizontal grid lines which match the axis tick marks
 *  <li> a crosshair which follows the mouse position
 * </ul>
 *
 * @author ulman
 *
 */
public class SimplePlot2D extends Plot2D
{
    protected BorderPainter borderPainter;
    protected BackgroundPainter plotBackgroundPainter;
    protected GridPainter gridPainter;
    protected CrosshairPainter crosshairPainter;

    public SimplePlot2D( )
    {
        this.initialize( );
    }

    public SimplePlot2D( String name )
    {
        this.initialize( );
        this.setName( name );
    }

    @Override
    protected void initializePainters( )
    {
        super.initializePainters( );

        // add a painter to display a solid background on the plot area
        plotBackgroundPainter = new BackgroundPainter( false );
        axisLayoutXY.addPainter( plotBackgroundPainter, Integer.MIN_VALUE );

        // add a painter to display grid lines
        gridPainter = new GridPainter( tickX, tickY );
        axisLayoutXY.addPainter( gridPainter, Plot2D.BACKGROUND_LAYER );

        // add a painter to display x and y crosshairs
        crosshairPainter = new CrosshairPainter( );
        axisLayoutXY.addPainter( crosshairPainter, Plot2D.FOREGROUND_LAYER );

        // add a painter to display a thin line border around the plot area
        borderPainter = new BorderPainter( );
        axisLayoutXY.addPainter( borderPainter, Plot2D.FOREGROUND_LAYER );

        // hide the Z axis by default
        setAxisSizeZ( 0 );
    }

    public void addPainterBackground( GlimpsePainter painter )
    {
        axisLayoutXY.addPainter( painter, Plot2D.BACKGROUND_LAYER );
    }

    public void addPainterForeground( GlimpsePainter painter )
    {
        axisLayoutXY.addPainter( painter, Plot2D.FOREGROUND_LAYER );
    }

    @Override
    public void setZOrder( GlimpsePainter painter, int zOrder )
    {
        axisLayoutXY.setZOrder( painter, zOrder );
    }

    @Override
    public void addPainter( GlimpsePainter painter )
    {
        axisLayoutXY.addPainter( painter );
    }

    @Override
    public void addPainter( GlimpsePainter painter, GlimpsePainterCallback callback )
    {
        axisLayoutXY.addPainter( painter, callback );
    }

    @Override
    public void addPainter( GlimpsePainter painter, int zOrder )
    {
        axisLayoutXY.addPainter( painter, zOrder );
    }

    @Override
    public void addPainter( GlimpsePainter painter, GlimpsePainterCallback callback, int zOrder )
    {
        axisLayoutXY.addPainter( painter, callback, zOrder );
    }

    @Override
    public void removePainter( GlimpsePainter painter )
    {
        axisLayoutXY.removePainter( painter );
    }

    public void setPlotBackgroundColor( float[] color )
    {
        plotBackgroundPainter.setColor( color );
    }

    public void setBackgroundColor( float[] color )
    {
        backgroundPainter.setColor( color );
    }

    public CrosshairPainter getCrosshairPainter( )
    {
        return crosshairPainter;
    }

    public BorderPainter getBorderPainter( )
    {
        return borderPainter;
    }

    public GridPainter getGridPainter( )
    {
        return gridPainter;
    }

    @Override
    public String toString( )
    {
        return SimplePlot2D.class.getSimpleName( );
    }
}

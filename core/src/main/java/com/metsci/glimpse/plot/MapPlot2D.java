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

import java.awt.Font;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.painter.NumericAxisPainter;
import com.metsci.glimpse.axis.painter.NumericRotatedRightYAxisPainter;
import com.metsci.glimpse.axis.painter.NumericRotatedYAxisPainter;
import com.metsci.glimpse.axis.painter.NumericTopXAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.LatLonAxisLabelHandler;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.decoration.MapBorderPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * A geographic plotting area.
 *
 * @author ulman
 */
public class MapPlot2D extends Plot2D
{
    protected MapBorderPainter borderPainter;
    protected BackgroundPainter plotBackgroundPainter;
    protected GridPainter gridPainter;
    protected DelegatePainter mapContentPainter;
    protected CrosshairPainter crosshairPainter;

    /*
     * In order to have a nice thick map border painter and have it not hide
     * the content, we'll use the actual axes for the inner map content, but
     * we'll use linked axes for the outside border.
     */
    protected Axis2D outerContentAxis;
    protected DelegatePainter outerContentPainter;

    protected GeoProjection geoProjection;

    protected AxisMouseListener listenerTopX;
    protected AxisMouseListener listenerRightY;

    protected GlimpseLayout axisLayoutRightY;
    protected GlimpseLayout axisLayoutTopX;
    protected GlimpseAxisLayout2D mapContentLayout;

    protected NumericAxisPainter painterTopX;
    protected NumericAxisPainter painterRightY;

    protected GridAxisLabelHandler tickTopX;
    protected GridAxisLabelHandler tickRightY;

    public MapPlot2D( GeoProjection projection )
    {
        this.geoProjection = projection;
        this.initialize( );
    }

    @Override
    protected void initializeAxes( )
    {
        super.initializeAxes( );

        this.outerContentAxis = getAxis( ).clone( );
    }

    @Override
    protected void initializePainters( )
    {
        this.axisLayoutTopX = new GlimpseAxisLayoutX( this, "AxisTopX", outerContentAxis.getAxisX( ) );
        this.axisLayoutRightY = new GlimpseAxisLayoutY( this, "AxisRightX", outerContentAxis.getAxisY( ) );

        this.outerContentPainter = new DelegatePainter( );

        super.initializePainters( );

        // reset the outer content painter to use the right axes
        this.axisLayoutXY.setAxis( outerContentAxis );
        this.axisLayoutX.setAxis( outerContentAxis.getAxisX( ) );
        this.axisLayoutY.setAxis( outerContentAxis.getAxisY( ) );

        this.tickTopX = createLabelHandlerTopX( );
        this.tickRightY = createLabelHandlerRightY( );

        this.painterTopX = createPainterTopX( tickTopX );
        this.painterRightY = createPainterRightY( tickRightY );

        if ( painterTopX != null ) axisLayoutTopX.addPainter( painterTopX );
        if ( painterRightY != null ) axisLayoutRightY.addPainter( painterRightY );

        this.listenerTopX = createAxisMouseListenerX( );
        this.listenerRightY = createAxisMouseListenerY( );

        this.plotBackgroundPainter = new BackgroundPainter( false );
        this.axisLayoutXY.addPainter( plotBackgroundPainter, Integer.MIN_VALUE );

        this.gridPainter = new GridPainter( tickX, tickY );
        this.axisLayoutXY.addPainter( gridPainter, Plot2D.BACKGROUND_LAYER );

        this.borderPainter = new MapBorderPainter( tickX, tickY );
        this.axisLayoutXY.addPainter( borderPainter );

        /*
         * Here we'll create a new layout for the map content.
         */
        this.mapContentPainter = new DelegatePainter( );

        this.mapContentLayout = new GlimpseAxisLayout2D( axisLayoutXY, getAxis( ) );
        this.mapContentLayout.setName( "Map" );
        this.mapContentLayout.addPainter( mapContentPainter );

        this.outerContentPainter.addPainter( mapContentLayout );
        this.axisLayoutXY.addPainter( outerContentPainter );

        this.crosshairPainter = new CrosshairPainter( );
        this.mapContentLayout.addPainter( crosshairPainter, FOREGROUND_LAYER );

        this.setShowAxisMarkerX( false );
        this.setShowAxisMarkerY( false );

        this.setTickSpacingX( 200 );
        this.setTickSpacingY( 200 );

        this.setAxisSizeX( 25 );
        this.setAxisSizeY( 25 );
        this.setAxisSizeZ( 0 );
    }

    @Override
    public void attachAxisMouseListeners( )
    {
        super.attachAxisMouseListeners( );

        attachAxisMouseListener( axisLayoutTopX, listenerTopX );
        attachAxisMouseListener( axisLayoutRightY, listenerRightY );

        attachAxisMouseListener( mapContentLayout, mouseListenerXY );
    }

    @Override
    protected void updatePainterLayout( )
    {
        getLayoutManager( ).setLayoutConstraints( String.format( "bottomtotop, gapx 0, gapy 0, insets %d %2$d %2$d %2$d", getTopInset( ), outerBorder ) );

        titleLayout.setLayoutData( String.format( "cell 1 0 1 1, pushx, growx, height %d!", showTitle ? titleSpacing : 0 ) );

        axisLayoutY.setLayoutData( String.format( "cell 0 2 1 1, pushy, growy, width %d!", axisThicknessY ) );

        axisLayoutRightY.setLayoutData( String.format( "cell 2 2 1 1, pushy, growy, width %d!", axisThicknessY ) );

        axisLayoutX.setLayoutData( String.format( "cell 1 3 1 1, pushx, growx, height %d!", axisThicknessX ) );

        axisLayoutTopX.setLayoutData( String.format( "cell 1 1 1 1, pushx, growx, height %d!", axisThicknessX ) );

        axisLayoutXY.setLayoutData( "cell 1 2 1 1, push, grow" );

        axisLayoutZ.setLayoutData( String.format( "cell 3 2 1 1, pushy, growy, width %d!", axisThicknessZ ) );

        /*
         * This is where the content will be painted. We'll make a gap the size
         * of the map border. This layout painter is not in the same group as
         * the layout painters above - it lives inside the axisLayoutXY painter.
         *
         * TODO how do we reset this layout each time someone calls
         * borderPainter.setBorderSize(int)?
         */
        mapContentLayout.setLayoutData( String.format( "gap %1$d %1$d %1$d %1$d, push, grow", borderPainter.getBorderSize( ) ) );
    }

    protected GridAxisLabelHandler createLabelHandlerTopX( )
    {
        return new LatLonAxisLabelHandler( geoProjection, true );
    }

    protected GridAxisLabelHandler createLabelHandlerRightY( )
    {
        return new LatLonAxisLabelHandler( geoProjection, false );
    }

    protected NumericAxisPainter createPainterTopX( AxisLabelHandler tickHandler )
    {
        NumericTopXAxisPainter painter = new NumericTopXAxisPainter( tickHandler );
        painter.setShowMarker( false );
        return painter;
    }

    protected NumericAxisPainter createPainterRightY( AxisLabelHandler tickHandler )
    {
        return new NumericRotatedRightYAxisPainter( tickHandler );
    }

    @Override
    protected NumericAxisPainter createAxisPainterY( AxisLabelHandler tickHandler )
    {
        return new NumericRotatedYAxisPainter( tickHandler );
    }

    @Override
    protected GridAxisLabelHandler createLabelHandlerX( )
    {
        return new LatLonAxisLabelHandler( geoProjection, true );
    }

    @Override
    protected GridAxisLabelHandler createLabelHandlerY( )
    {
        return new LatLonAxisLabelHandler( geoProjection, false );
    }

    public NumericAxisPainter getAxisPainterTopX( )
    {
        return painterTopX;
    }

    public NumericAxisPainter getAxisPainterRightY( )
    {
        return painterRightY;
    }

    public GlimpseLayout getLayoutPainterTopX( )
    {
        return axisLayoutTopX;
    }

    public GlimpseLayout getLayoutPainterRightY( )
    {
        return axisLayoutRightY;
    }

    @Override
    public void setAxisFont( Font font )
    {
        setAxisFont( font, true );
    }

    @Override
    public void setAxisFont( Font font, boolean antialias )
    {
        if ( this.painterX != null ) this.painterX.setFont( font );
        if ( this.painterY != null ) this.painterY.setFont( font );
        if ( this.painterZ != null ) this.painterZ.setFont( font );
        if ( this.painterTopX != null ) this.painterTopX.setFont( font );
        if ( this.painterRightY != null ) this.painterRightY.setFont( font );
    }

    @Override
    public void setAxisLabelX( String label )
    {
        setAxisLabelX( label, "" );
    }

    @Override
    public void setAxisLabelX( String label, String abbreviatedUnits )
    {
        tickX.setAxisLabel( label );
        tickX.setAxisUnits( abbreviatedUnits, true );

        tickTopX.setAxisLabel( label );
        tickTopX.setAxisUnits( abbreviatedUnits, true );
    }

    @Override
    public void setAxisLabelX( String label, String units, boolean abbreviated )
    {
        tickX.setAxisLabel( label );
        tickX.setAxisUnits( units, abbreviated );

        tickTopX.setAxisLabel( label );
        tickTopX.setAxisUnits( units, abbreviated );
    }

    @Override
    public void setShowAxisMarkerX( boolean show )
    {
        painterX.setShowMarker( show );
        painterTopX.setShowMarker( show );
    }

    @Override
    public void setMarkerWidthX( int width )
    {
        painterX.setMarkerWidth( width );
        painterTopX.setMarkerWidth( width );
    }

    @Override
    public void setShowMinorTicksX( boolean show )
    {
        painterX.setShowMinorTicks( show );
        painterTopX.setShowMinorTicks( show );
    }

    @Override
    public void setTickSpacingX( int spacing )
    {
        tickX.setTickSpacing( spacing );
        tickTopX.setTickSpacing( spacing );
    }

    @Override
    public void setMinorTickCountX( int count )
    {
        tickX.setMinorTickCount( count );
        tickTopX.setMinorTickCount( count );
    }

    @Override
    public void setAxisLabelY( String label )
    {
        setAxisLabelY( label, "" );
    }

    @Override
    public void setAxisLabelY( String label, String abbreviatedUnits )
    {
        tickY.setAxisLabel( label );
        tickY.setAxisUnits( abbreviatedUnits, true );

        tickRightY.setAxisLabel( label );
        tickRightY.setAxisUnits( abbreviatedUnits, true );
    }

    @Override
    public void setAxisLabelY( String label, String units, boolean abbreviated )
    {
        tickY.setAxisLabel( label );
        tickY.setAxisUnits( units, abbreviated );

        tickRightY.setAxisLabel( label );
        tickRightY.setAxisUnits( units, abbreviated );
    }

    @Override
    public void setShowAxisMarkerY( boolean show )
    {
        painterY.setShowMarker( show );
        painterRightY.setShowMarker( show );
    }

    @Override
    public void setMarkerWidthY( int width )
    {
        painterY.setMarkerWidth( width );
        painterRightY.setMarkerWidth( width );
    }

    @Override
    public void setShowMinorTicksY( boolean show )
    {
        painterY.setShowMinorTicks( show );
        painterRightY.setShowMinorTicks( show );
    }

    @Override
    public void setTickSpacingY( int spacing )
    {
        tickY.setTickSpacing( spacing );
        tickRightY.setTickSpacing( spacing );
    }

    @Override
    public void setMinorTickCountY( int count )
    {
        tickY.setMinorTickCount( count );
        tickRightY.setMinorTickCount( count );
    }

    @Override
    public void addPainter( GlimpsePainter painter )
    {
        mapContentPainter.addPainter( painter );
    }

    @Override
    public void removePainter( GlimpsePainter painter )
    {
        mapContentPainter.removePainter( painter );
    }

    @Override
    public GlimpseAxisLayout2D getLayoutCenter( )
    {
        return mapContentLayout;
    }

    public void setPlotBackgroundColor( float[] color )
    {
        plotBackgroundPainter.setColor( color );
    }

    @Override
    public void setBackgroundColor( float[] color )
    {
        backgroundPainter.setColor( color );
    }

    public CrosshairPainter getCrosshairPainter( )
    {
        return crosshairPainter;
    }

    public MapBorderPainter getBorderPainter( )
    {
        return borderPainter;
    }

    public GridPainter getGridPainter( )
    {
        return gridPainter;
    }

    public GeoProjection getGeoProjection( )
    {
        return geoProjection;
    }

    @Override
    public String toString( )
    {
        return MapPlot2D.class.getSimpleName( );
    }
}

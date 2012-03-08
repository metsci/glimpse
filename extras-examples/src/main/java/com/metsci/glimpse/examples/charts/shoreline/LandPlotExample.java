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
package com.metsci.glimpse.examples.charts.shoreline;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.axis.painter.NumericAxisPainter;
import com.metsci.glimpse.axis.painter.NumericRotatedYAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.charts.shoreline.LandShapePainter;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.examples.basic.LinePlotExample;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CopyrightPainter;
import com.metsci.glimpse.painter.decoration.LegendPainter.LegendPlacement;
import com.metsci.glimpse.painter.decoration.LegendPainter.LineLegendPainter;
import com.metsci.glimpse.painter.info.MeasurementPainter;
import com.metsci.glimpse.painter.plot.XYLinePainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.TangentPlane;

/**
 * Displays shoreline data available from the NOAA/NGDC Coastline Extractor
 * tool at http://www.ngdc.noaa.gov/mgg/coast/</p>
 *
 * When downloading data, choose the following options:
 * World Vector Shoreline
 * Compression Method: None
 * Coast Format: Mapgen
 * Coast Preview: No Preview
 *
 * @author ulman
 */
public class LandPlotExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new LandPlotExample( ) );
    }
    
    protected int plotHeight = 200;
    protected int plotWidth = 200;

    protected double plotMinX = 0.0;
    protected double plotMinY = 0.0;


    @Override
    public SimplePlot2D getLayout( ) throws ParseException
    {
        // create a tangent plane
        // land coordinates are specified as lat lons and must be converted
        // into flat rectangular coordinates to be displayed by glimpse
        final TangentPlane tangentPlane = new TangentPlane( LatLonGeo.fromDeg( 20.000067, -155 ) );

        // create a GeoPlot
        // here we just display land, but the GeoPlot could be used to display
        // tracks or other objects on top of the land
        //MapPlot2D geoplot = new MapPlot2D( tangentPlane );
        final SimplePlot2D plot = new SimplePlot2D( );

        
        // create a new plot for the floating layout area
        // it uses a vertical text orientation for its y axis painter to save space
        final SimplePlot2D floatingPlot = new SimplePlot2D( )
        {
            @Override
            protected NumericAxisPainter createAxisPainterY( AxisLabelHandler tickHandler )
            {
                return new NumericRotatedYAxisPainter( tickHandler );
            }
        };

        // add an axis listener which adjusts the position of the floating layout painter as the axis changes
        // (the layout painter is tied to a fixed axis value)
        plot.addAxisListener( new AxisListener2D( )
        {
            @Override
            public void axisUpdated( Axis2D axis )
            {
                int minX = plot.getAxisX( ).valueToScreenPixel( plotMinX );
                int minY = plot.getAxisY( ).valueToScreenPixel( plotMinY );

                floatingPlot.setLayoutData( String.format( "pos %d %d %d %d", minX, minY, minX + plotWidth, minY + plotHeight ) );
                plot.invalidateLayout( );
            }
        } );

        // add a mouse listener which listens for middle mouse button (mouse wheel) clicks
        // and moves the floating plot in response
        plot.getLayoutCenter( ).addGlimpseMouseListener( new GlimpseMouseListener( )
        {
            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                if ( event.isButtonDown( MouseButton.Button2 ) )
                {
                    plotMinX = plot.getAxisX( ).screenPixelToValue( event.getX( ) );
                    plotMinY = plot.getAxisY( ).screenPixelToValue( plot.getAxisY( ).getSizePixels( ) - event.getY( ) );
                }
            }

            @Override
            public void mouseEntered( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseExited( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent event )
            {
            }
        } );

        // the floating plot is quite small, so use a smaller font, tighter tick spacing, and smaller bounds for the axes
        floatingPlot.setAxisFont( FontUtils.getSilkscreen( ), false );
        floatingPlot.setAxisSizeX( 25 );
        floatingPlot.setAxisSizeY( 25 );
        floatingPlot.setTickSpacingX( 35 );
        floatingPlot.setTickSpacingY( 35 );
        floatingPlot.setBorderSize( 4 );

        // don't show crosshairs in the floating plot or the main plot
        floatingPlot.getCrosshairPainter( ).setVisible( false );
        plot.getCrosshairPainter( ).setVisible( false );

        // don't provide any space for a title in the floating plot
        floatingPlot.setTitleHeight( 0 );

        // add a border to the outside of the floating plot
        floatingPlot.addPainterOuter( new BorderPainter( ).setLineWidth( 4 ) );

        // create a color scale axis for the heat maps created below
        Axis1D colorAxis = new Axis1D( );
        colorAxis.setMin( 0.0 );
        colorAxis.setMax( 1000.0 );

//        // add a heat map painter to the floating plot
//        floatingPlot.addPainter( HeatMapExample.newHeatMapPainter( colorAxis ) );
//        floatingPlot.getAxis( ).set( 0, 1000, 0, 1000 );
//
//        // add a heat map painter to the outer plot
//        plot.addPainter( HeatMapExample.newHeatMapPainter( colorAxis ) );
//        plot.getAxis( ).set( 0, 1000, 0, 1000 );

        
        
        // creating a data series painter, passing it the lineplot frame
        // this constructor will have the painter draw according to the lineplot x and y axis
        XYLinePainter series1 = LinePlotExample.createXYLinePainter1( );
        floatingPlot.addPainter( series1 );

        // in order for our second data series to use the right hand
        // axis as its y axis, we must manually specify the axes which it should use
        XYLinePainter series2 = LinePlotExample.createXYLinePainter2( );
        floatingPlot.addPainter( series2 );

        LineLegendPainter legend = new LineLegendPainter( LegendPlacement.NW );

        //Move the legend further away from the right side;
        legend.setOffsetY( 10 );
        legend.setOffsetX( 10 );
        legend.addItem( "Series 1", GlimpseColor.fromColorRgba( 1.0f, 0.0f, 0.0f, 0.8f ) );
        legend.addItem( "Series 2", GlimpseColor.fromColorRgba( 0.0f, 0.0f, 1.0f, 0.8f ), true );
        
        plot.addPainter( legend );
        
       
        
        
        // create a custom layer to display land and load data into it
        // the data file used was created using the NOAA/NGDC Coastline Extractor
        // tool at http://www.ngdc.noaa.gov/mgg_coastline/
        LandShapePainter landLayer = new LandShapePainter( );
        try
        {
            landLayer.loadNgdcLandFileAndCenterAxis( new File( "src/main/resources/data/Okinawa.land" ), tangentPlane, plot.getAxis( ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace( );
            throw new RuntimeException( e );
        }

        plot.setPlotBackgroundColor( GlimpseColor.fromColorHex( "#bbc2c7" ) );
        
        // add a Metron copywrite notice to the plot
        plot.addPainter( new CopyrightPainter( ) );

        // add the land layer created above to the plot
        plot.addPainter( landLayer );

        // add a painter which displays angles and distances between points (activated by right clicking)
        plot.addPainter( new MeasurementPainter( "m" ) );

        // lock the plot to a 1 to 1 aspect ratio
        plot.lockAspectRatioXY( 1.0f );

        // set the plot title
        plot.setTitle( "Okinawa" );

        // don't show horizontal/vertical bars marking the cursor position
        plot.getCrosshairPainter( ).setVisible( false );

        
        // add the floating plot to the main plot
        plot.getLayoutCenter( ).addLayout( floatingPlot );
        plot.getLayoutCenter( ).invalidateLayout( );
        
        

        
        plot.setAxisSizeX( 0 );
        plot.setAxisSizeY( 0 );
        plot.setTitleHeight( 0 );
        plot.setBorderSize( 10 );
        
        return plot;
    }
}

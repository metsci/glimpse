/*
 * Copyright (c) 2019, Metron, Inc.
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

import static com.metsci.glimpse.support.QuickUtils.quickGlimpseApp;
import static com.metsci.glimpse.support.QuickUtils.swingInvokeLater;
import static javax.media.opengl.GLProfile.GL3bc;

import java.io.InputStream;

import com.metsci.glimpse.charts.shoreline.LandShapePainter;
import com.metsci.glimpse.painter.decoration.CopyrightPainter;
import com.metsci.glimpse.painter.info.MeasurementPainter;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.io.StreamOpener;

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
public class LandPlotExample
{
    public static void main( String[] args )
    {
        swingInvokeLater( ( ) ->
        {
            // land coordinates are specified as lat lons and must be converted
            // into flat rectangular coordinates to be displayed by glimpse
            TangentPlane tangentPlane = new TangentPlane( LatLonGeo.fromDeg( 20, -155 ) );

            // create a plot, which automatically provides axes, mouse interation, and
            // plot decorations like grid lines and mouse crosshairs
            SimplePlot2D plot = new SimplePlot2D( );

            // don't show crosshairs
            plot.getCrosshairPainter( ).setVisible( false );

            // hide the plot title and the x and y axes
            plot.showTitle( false );
            plot.setTitleHeight( 0 );
            plot.setAxisSizeX( 0 );
            plot.setAxisSizeY( 0 );

            // set a 10 pixel border around the edge of the plot
            plot.setBorderSize( 10 );

            // create a custom layer to display land and load data into it
            // the data file used was created using the NOAA/NGDC Coastline Extractor
            // tool at http://www.ngdc.noaa.gov/mgg_coastline/
            LandShapePainter landLayer = new LandShapePainter( );
            InputStream in = StreamOpener.fileThenResource.openForRead( "data/Okinawa.land" );
            landLayer.loadNgdcLandFileAndCenterAxis( in, tangentPlane, plot.getAxis( ) );

            // customize the background color of the plot
            plot.setPlotBackgroundColor( GlimpseColor.fromColorHex( "#bbc2c7" ) );

            // add a Metron copywrite notice to the plot
            plot.addPainter( new CopyrightPainter( ) );

            // add the land layer created above to the plot
            plot.addPainter( landLayer );

            // add a painter which displays angles and distances between points (activated by right clicking)
            plot.addPainter( new MeasurementPainter( "m" ) );

            // lock the plot to a 1 to 1 aspect ratio
            plot.lockAspectRatioXY( 1.0f );
            // don't show horizontal/vertical bars marking the cursor position
            plot.getCrosshairPainter( ).setVisible( false );

            // create a window and show the plot
            quickGlimpseApp( "Land Plot Example", GL3bc, plot );
        } );
    }
}

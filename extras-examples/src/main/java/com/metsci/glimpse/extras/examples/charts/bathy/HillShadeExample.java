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
package com.metsci.glimpse.extras.examples.charts.bathy;

import static com.jogamp.opengl.GLProfile.GL3bc;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseApp;
import static com.metsci.glimpse.core.support.QuickUtils.swingInvokeLater;
import static com.metsci.glimpse.util.logging.LoggerUtils.setTerseConsoleLogger;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

import com.metsci.glimpse.charts.bathy.ShadedReliefTiledPainter;
import com.metsci.glimpse.charts.bathy.TileKey;
import com.metsci.glimpse.charts.bathy.TopoTileProvider;
import com.metsci.glimpse.charts.bathy.TopographyData;
import com.metsci.glimpse.core.layout.GlimpseLayout;
import com.metsci.glimpse.core.painter.geo.ScalePainter;
import com.metsci.glimpse.core.plot.MapPlot2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.TangentPlane;

/**
 * @author borkholder
 */
public class HillShadeExample implements TopoTileProvider
{
    private TopographyData singleTile;

    public static void main( String[] args )
    {
        setTerseConsoleLogger( Level.FINE );

        swingInvokeLater( ( ) ->
        {
            // create a window and show the plot
            quickGlimpseApp( "Hill Shade Example", GL3bc, new HillShadeExample( ).getLayout( ) );
        } );
    }

    public HillShadeExample( ) throws IOException
    {
        singleTile = new TopographyData( HillShadeExample.class.getResource( "Cayman.bathy" ) );
    }

    @Override
    public String getAttribution( )
    {
        return "Obtained from http://www.ngdc.noaa.gov/mgg/gdas/gd_designagrid.html";
    }

    @Override
    public Collection<TileKey> keys( )
    {
        double maxLat = singleTile.getStartLat( ) + singleTile.getHeightStep( ) * singleTile.getImageHeight( );
        double maxLon = singleTile.getStartLon( ) + singleTile.getWidthStep( ) * singleTile.getImageWidth( );
        TileKey key = new TileKey( 0, singleTile.getStartLat( ), maxLat, singleTile.getStartLon( ), maxLon );
        return Collections.singleton( key );
    }

    @Override
    public TopographyData getTile( TileKey key ) throws IOException
    {
        return singleTile;
    }

    public GlimpseLayout getLayout( )
    {
        GeoProjection projection = new TangentPlane( LatLonGeo.fromDeg( 20.14, -79.23 ) );

        MapPlot2D plot = new MapPlot2D( projection );
        ShadedReliefTiledPainter painter = new ShadedReliefTiledPainter( projection, this );
        plot.addPainter( painter );

        // set the x and y axis bounds based on the extent of the bathemetry data
        singleTile.setAxisBounds( plot.getAxis( ), projection );

        ScalePainter scale = new ScalePainter( );
        scale.setPixelBufferX( 8 );
        scale.setPixelBufferY( 8 );
        plot.addPainter( scale );

        return plot;
    }
}

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
package com.metsci.glimpse.examples.charts.bathy;

import static com.metsci.glimpse.examples.Example.showWithSwing;
import static com.metsci.glimpse.util.logging.LoggerUtils.setTerseConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;

import org.opengis.referencing.FactoryException;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.charts.bathy.GeotiffTileProvider;
import com.metsci.glimpse.charts.bathy.ShadedReliefTiledPainter;
import com.metsci.glimpse.charts.bathy.TopoTileProvider;
import com.metsci.glimpse.charts.bathy.UnderseaFeatureNamesPainter;
import com.metsci.glimpse.charts.shoreline.LandBox;
import com.metsci.glimpse.charts.shoreline.LandShapePainter;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsFile;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsPolygonHeader.PolygonType;
import com.metsci.glimpse.charts.shoreline.gshhs.GshhsPolygonHeader.UnrecognizedValueException;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.MapPlot2D;
import com.metsci.glimpse.util.GlimpseDataPaths;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.units.Length;

/**
 * @author borkholder
 */
public class Etopo1Example implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        setTerseConsoleLogger( Level.FINE );
        showWithSwing( new Etopo1Example( ) );
    }

    @Override
    public GlimpseLayout getLayout( )
    {
        try
        {
            GeoProjection projection = new TangentPlane( LatLonGeo.fromDeg( 20.14, -79.23 ) );
            MapPlot2D plot = new MapPlot2D( projection );
            TopoTileProvider tileProvider = GeotiffTileProvider.getGebco2014( );
            LandBox box = new LandBox( 60, 10, -100, -50, false );
//            GshhsFile f = new GshhsFile( new File( GlimpseDataPaths.glimpseUserDataDir, "gshhs/gshhs_h.b" ), box, PolygonType.land );
//            LandShapePainter landPainter = new LandShapePainter( );
//            landPainter.setShowLines( false );
//            landPainter.loadLandFileAndCenterAxis( f.toShape( ), projection, new Axis2D( ) );

            plot.getLayoutCenter( ).addPainter( new ShadedReliefTiledPainter( projection, tileProvider ) );
            plot.getLayoutCenter( ).addPainter( new UnderseaFeatureNamesPainter( projection ) );
//            plot.getLayoutCenter( ).addPainter( landPainter );
            plot.getAxis( ).set( 0, Length.fromNauticalMiles( 300 ), 0, Length.fromNauticalMiles( 300 ) );
            plot.getAxis( ).validate( );
            return plot;
        }
        catch ( Exception ex )
        {
            throw new RuntimeException( ex );
        }
    }
}

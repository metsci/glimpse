/*
 * Copyright (c) 2020, Metron, Inc.
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

import static com.jogamp.opengl.GLProfile.GL3;
import static com.metsci.glimpse.core.support.FrameUtils.screenFracSize;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseApp;
import static com.metsci.glimpse.core.support.QuickUtils.swingInvokeLater;
import static com.metsci.glimpse.topo.io.TopoCache.topoCacheDataset;
import static com.metsci.glimpse.topo.io.TopoDataPaths.requireTopoDataFile;
import static com.metsci.glimpse.topo.io.TopoReader.readTopoLevel;
import static com.metsci.glimpse.util.logging.LoggerUtils.setTerseConsoleLogger;
import static com.metsci.glimpse.util.units.Length.fromNauticalMiles;
import static java.util.logging.Level.FINE;

import java.io.File;

import com.metsci.glimpse.core.painter.geo.ScalePainter;
import com.metsci.glimpse.core.plot.MapPlot2D;
import com.metsci.glimpse.topo.ShadedReliefTiledPainter;
import com.metsci.glimpse.topo.io.TopoDataFile;
import com.metsci.glimpse.topo.io.TopoDataset;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.TangentPlane;

/**
 * @author borkholder
 */
public class HillShadeExample
{
    public static void main( String[] args )
    {
        setTerseConsoleLogger( FINE );

        swingInvokeLater( ( ) -> {
            File topoDataFile = requireTopoDataFile( );
            TopoDataFile topoBase = readTopoLevel( topoDataFile );
            TopoDataset topoDataset = topoCacheDataset( topoBase );

            GeoProjection projection = new TangentPlane( LatLonGeo.fromDeg( 19, -77 ) );

            MapPlot2D plot = new MapPlot2D( projection );
            ShadedReliefTiledPainter painter = new ShadedReliefTiledPainter( projection, topoDataset );
            plot.addPainter( painter );

            double dx = fromNauticalMiles( 400 );
            double dy = dx;
            plot.getAxis( ).set( -dx, +dx, -dy, +dy );

            ScalePainter scale = new ScalePainter( );
            scale.setPixelBufferX( 8 );
            scale.setPixelBufferY( 8 );
            plot.addPainter( scale );

            quickGlimpseApp( "HillShadeExample", GL3, plot, screenFracSize( 0.8 ) );
        } );
    }
}

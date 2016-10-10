/*
 * Copyright (c) 2016 Metron, Inc.
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
package com.metsci.glimpse.charts.slippy;

import static com.metsci.glimpse.charts.slippy.SlippyDataPaths.slippyCacheRoot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 *
 * Utility methods for constructing SlippyMapPainters from common sources.
 *
 * TODO look into license/attribution requirements
 *
 * @author oren
 */
public class SlippyPainterFactory
{

    private SlippyPainterFactory( )
    {
    }

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder( ).setDaemon( true ).setNameFormat( "slippy-tile-fetcher-%d" ).build( );

    private static final ExecutorService EXEC = Executors.newFixedThreadPool( 4, THREAD_FACTORY );

    public static SlippyMapPainter getOpenStreetMaps( GeoProjection geoProj )
    {
        return getOpenStreetMaps( geoProj, slippyCacheRoot.resolve( "osm-maps" ) );
    }

    public static SlippyMapPainter getOpenStreetMaps( GeoProjection geoProj, Path cacheDir )
    {
        List<String> prefixes = new ArrayList<String>( );
        prefixes.add( "http://a.tile.openstreetmap.org/" );
        prefixes.add( "http://b.tile.openstreetmap.org/" );
        prefixes.add( "http://c.tile.openstreetmap.org/" );
        SlippyMapTilePainter painter = new SlippyMapTilePainter( geoProj, prefixes, EXEC, cacheDir, 18 );
        return new SlippyMapPainter( painter, "\u00A9 OpenStreetMap contributors" );
    }

    public static SlippyMapPainter getCartoMap( GeoProjection geoProj, boolean light, boolean labels )
    {
        String cacheStr = "cartodb-" + ( light ? "light" : "dark" ) + ( labels ? "-all" : "-nolabels" );
        return getCartoMap( geoProj, slippyCacheRoot.resolve( cacheStr ), light, labels );
    }

    public static SlippyMapPainter getCartoMap( GeoProjection geoProj, Path cacheDir, boolean light, boolean labels )
    {
        String type = ( light ? "light" : "dark" ) + ( labels ? "_all" : "_nolabels" );
        String template = "http://%s.basemaps.cartocdn.com/" + type + "/";
        List<String> prefixes = new ArrayList<String>( );
        prefixes.add( String.format( template, "a" ) );
        prefixes.add( String.format( template, "b" ) );
        prefixes.add( String.format( template, "c" ) );
        SlippyMapTilePainter painter = new SlippyMapTilePainter( geoProj, prefixes, EXEC, cacheDir, 18 );
        return new SlippyMapPainter( painter, "\u00A9 OpenStreetMap contributors \u00A9 CartoDB" );
    }
}

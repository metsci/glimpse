package com.metsci.glimpse.charts.slippy;

import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static final Path CACHE_ROOT = Paths.get( System.getProperty( "user.home" ) ).resolve( ".glimpse-slippy-cache" );

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder( ).setDaemon( true ).setNameFormat( "slippy-tile-fetcher-%d" ).build( );

    private static final ExecutorService EXEC = Executors.newFixedThreadPool( 4, THREAD_FACTORY );

    public static SlippyMapPainter getOpenStreetMaps( GeoProjection geoProj )
    {
        return getOpenStreetMaps( geoProj, CACHE_ROOT.resolve( "osm-maps" ) );
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

    public static SlippyMapPainter getMapQuestMaps( GeoProjection geoProj )
    {
        return getMapQuestMaps( geoProj, CACHE_ROOT.resolve( "mapquest-map" ) );
    }

    public static SlippyMapPainter getMapQuestMaps( GeoProjection geoProj, Path cacheDir )
    {
        List<String> prefixes = new ArrayList<String>( );
        for ( int i = 1; i <= 4; i++ )
        {
            prefixes.add( "http://otile" + i + ".mqcdn.com/tiles/1.0.0/osm/" );
        }
        SlippyMapTilePainter painter = new SlippyMapTilePainter( geoProj, prefixes, EXEC, cacheDir, 17 );
        return new SlippyMapPainter( painter, "\u00A9 OpenStreetMap contributors, tiles courtesy of MapQuest" );
    }

    public static SlippyMapPainter getMapQuestImagery( GeoProjection geoProj, boolean inUS )
    {
        return getMapQuestImagery( geoProj, CACHE_ROOT.resolve( "mapquest-sat" ), inUS );
    }

    public static SlippyMapPainter getMapQuestImagery( GeoProjection geoProj, Path cacheDir, boolean inUS )
    {
        List<String> prefixes = new ArrayList<String>( );
        for ( int i = 1; i <= 4; i++ )
        {
            prefixes.add( "http://otile" + i + ".mqcdn.com/tiles/1.0.0/sat/" );
        }
        SlippyMapTilePainter painter = new SlippyMapTilePainter( geoProj, prefixes, EXEC, cacheDir, inUS ? 16 : 11 );
        return new SlippyMapPainter( painter, "\u00A9 OpenStreetMap contributors, tiles courtesy of MapQuest" );
    }

    public static SlippyMapPainter getCartoMap( GeoProjection geoProj, boolean light, boolean labels )
    {
        String cacheStr = "cartodb-" + ( light ? "light" : "dark" ) + ( labels ? "-all" : "-nolabels" );
        return getCartoMap( geoProj, CACHE_ROOT.resolve( cacheStr ), light, labels );
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

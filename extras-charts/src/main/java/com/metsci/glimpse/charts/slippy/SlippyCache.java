package com.metsci.glimpse.charts.slippy;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.metsci.glimpse.support.projection.LatLonProjection;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.texture.RGBTextureProjected2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * An in-memory cache that uses soft references.
 * If a path is given the images are checked for locally before fetching them from the web. Multiple URLs may be given,
 * but only one thread will pull from each URL at a given time.
 * @author oren
 *
 */
public class SlippyCache
{

    private static final Logger logger = Logger.getLogger( SlippyCache.class.getName( ) );

    /*
     * suffix pattern for slippy tiles zoom/x/y.png
     */
    private static final String KEY_PATTERN = "%d/%d/%d.png";

    /*
     * Queue of URL prefixes for the tile server. You should only fetch from a single server at a time.
     */
    private final BlockingDeque<String> prefixQueue;

    /*
     * Where to store tiles on disk. Set to null to disable disk caching.
     */
    private final Path cacheDir;

    /*
     * The in memory cache of textures.
     */
    private final LoadingCache<String, RGBTextureProjected2D> cache;

    /*
     * The base geo projection of the map display.
     */
    private final GeoProjection geoProj;

    /*
     * Slippy projections for various zoom levels
     */
    private final SlippyProjection[] slippyProj = new SlippyProjection[20];

    public SlippyCache( GeoProjection geoProj, String urlPrefix )
    {
        this( geoProj, Collections.singletonList( urlPrefix ) );
    }

    public SlippyCache( GeoProjection geoProj, String urlPrefix, Path cacheDir )
    {
        this( geoProj, Collections.singletonList( urlPrefix ), cacheDir );
    }

    public SlippyCache( GeoProjection geoProj, List<String> urlPrefixes )
    {
        this( geoProj, urlPrefixes, null );
    }

    public SlippyCache( GeoProjection geoProj, List<String> urlPrefixes, Path cacheDir )
    {
        this.geoProj = geoProj;
        this.prefixQueue = new LinkedBlockingDeque<>( urlPrefixes.size( ) );
        if ( urlPrefixes.isEmpty( ) )
        {
            throw new IllegalArgumentException( "must supply at least one slippy server" );
        }
        else
        {
            for ( String prefix : urlPrefixes )
            {
                try
                {
                    new URL( prefix );
                }
                catch ( Exception e )
                {
                    throw new IllegalArgumentException( prefix + " is not a valid url" );
                }
                prefixQueue.add( prefix + ( prefix.endsWith( "/" ) ? "" : "/" ) );
            }
        }

        if ( cacheDir != null )
        {
            if ( Files.isDirectory( cacheDir ) )
            {
                this.cacheDir = cacheDir;
            }
            else if ( !Files.exists( cacheDir ) )
            {
                try
                {
                    Files.createDirectories( cacheDir );
                }
                catch ( Exception e )
                {
                    String msg = "Failed to created directory for disk cache: " + cacheDir.toAbsolutePath( ).toString( );
                    logger.log( Level.WARNING, msg, e );
                }
                if ( Files.isDirectory( cacheDir ) )
                {
                    this.cacheDir = cacheDir;
                }
                else
                {
                    this.cacheDir = null;
                }
            }
            else
            {
                throw new IllegalArgumentException( "specified cache directory (" + cacheDir.toString( ) + ")is a file" );
            }
        }
        else
        {
            this.cacheDir = null;
        }

        for ( int zoom = 0; zoom < slippyProj.length; zoom++ )
        {
            this.slippyProj[zoom] = new SlippyProjection( zoom );
        }

        this.cache = CacheBuilder.newBuilder( )
                .concurrencyLevel( urlPrefixes.size( ) )
                .softValues( )
                .build( new SlippyLoader( ) );
    }

    public RGBTextureProjected2D getTexture( int zoom, int x, int y )
    {
        String key = String.format( KEY_PATTERN, zoom, x, y );
        RGBTextureProjected2D tex = null;
        try
        {
            tex = cache.get( key );
        }
        catch ( Exception e )
        {
            logger.log( Level.WARNING, "Failed to get texture from cache", e );
        }
        return tex;
    }

    public RGBTextureProjected2D getTextureIfPresent( int zoom, int x, int y )
    {
        String key = String.format( KEY_PATTERN, zoom, x, y );
        RGBTextureProjected2D tex = null;
        try
        {
            tex = cache.getIfPresent( key );
        }
        catch ( Exception e )
        {
            logger.log( Level.WARNING, "Failed to get texture from cache (if present)", e );
        }
        return tex;
    }

    private class SlippyLoader extends CacheLoader<String, RGBTextureProjected2D>
    {
        @Override
        public RGBTextureProjected2D load( String key ) throws Exception
        {
            BufferedImage img = fetchFromDisk( key );
            //if we were able to get the image from disk, return it
            if ( img == null )
            {
                img = fetchFromWeb( key );
            }
            return makeTex( key, img );
        }

        private BufferedImage fetchFromWeb( String key )
        {
            BufferedImage img = null;
            //Now try to pull the image from the web
            String prefix = null;
            try
            {
                prefix = prefixQueue.take( );
                String urlStr = prefix + key;
                try
                {
                    img = ImageIO.read( new URL( urlStr ) );
                }
                catch ( IOException e )
                {
                    logger.log( Level.WARNING, "Exception fetching tile from the web", e );
                }
                //If we got an image, try to cache it to disk
                try
                {
                    saveToDisk( key, img );
                }
                catch ( IOException e )
                {
                    logger.log( Level.WARNING, "Exception saving tile to disk", e );
                }
            }
            catch ( InterruptedException e )
            {
                logger.log( Level.WARNING, "Interrupted while getting a URL", e );
            }
            finally
            {
                if ( prefix != null )
                {
                    try
                    {
                        prefixQueue.put( prefix );
                    }
                    catch ( InterruptedException e )
                    {
                        logger.log( Level.WARNING, "Interrupted while putting a URL back on the queue", e );
                    }
                }
            }
            return img;
        }

        private void saveToDisk( String key, BufferedImage img ) throws IOException
        {
            if ( img != null && cacheDir != null )
            {
                Path imgPath = cacheDir.resolve( key );
                if ( !Files.exists( imgPath.getParent( ) ) )
                {
                    try
                    {
                        Files.createDirectories( imgPath.getParent( ) );
                    }
                    catch ( Exception e )
                    {
                        //the img write should fail anyway
                    }
                }
                ImageIO.write( img, "PNG", imgPath.toFile( ) );
            }
        }

        private BufferedImage fetchFromDisk( String key )
        {
            BufferedImage img = null;
            if ( cacheDir != null )
            {
                Path imgPath = cacheDir.resolve( key );
                if ( Files.exists( imgPath ) )
                {
                    try
                    {
                        img = ImageIO.read( imgPath.toFile( ) );
                    }
                    catch ( Exception e )
                    {
                        logger.log( Level.WARNING, "Exception while attempting to read the tile from disk", e );
                    }
                }
            }
            return img;
        }
    }

    private RGBTextureProjected2D makeTex( String key, BufferedImage img )
    {
        if ( img == null )
        {
            return null;
        }
        RGBTextureProjected2D tex = new RGBTextureProjected2D( img );
        String[] parts = key.substring( 0, key.length( ) - 4 ).split( "/" );
        int zoom = Integer.parseInt( parts[0] );
        int x = Integer.parseInt( parts[1] );
        int y = Integer.parseInt( parts[2] );
        tex.setProjection( getProjection( zoom, x, y ) );
        return tex;
    }

    private Projection getProjection( int zoom, int x, int y )
    {
        LatLonGeo nw = slippyProj[zoom].unproject( x, y );
        LatLonGeo se = slippyProj[zoom].unproject( x + 1, y + 1 );
        double minLat = se.getLatDeg( );
        double maxLat = nw.getLatDeg( );
        double minLon = nw.getLonDeg( );
        double maxLon = se.getLonDeg( );
        return new LatLonProjection( geoProj, minLat, maxLat, minLon, maxLon, false );
    }
}

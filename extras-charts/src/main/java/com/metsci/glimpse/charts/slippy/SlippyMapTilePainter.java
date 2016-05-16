package com.metsci.glimpse.charts.slippy;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.DrawableTexture;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.painter.texture.TextureUnit;
import com.metsci.glimpse.support.projection.LatLonProjection;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.texture.RGBTextureProjected2D;
import com.metsci.glimpse.support.texture.TextureProjected2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * Paints the slippy tiles. New tiles are fetched dynamically as the user zooms/pans and stale textures are removed.
 * @author oren
 */
public class SlippyMapTilePainter extends ShadedTexturePainter
{
    private static final Logger logger = Logger.getLogger( SlippyCache.class.getName( ) );
    private static final double LOG2 = Math.log( 2 );

    private final GeoProjection geoProj;
    private final int maxZoom;
    private final SlippyProjection[] slippyProj;
    private final ExecutorService exec;
    private final SlippyCache cache;

    /**
     * These are checked during the asynchronous texture fetch to see if we still need to fetch it,
     * and after we fetch it if we still need to display it.
     */
    private final AtomicReference<double[]> lastBounds = new AtomicReference<>( );
    private final AtomicInteger lastZoom = new AtomicInteger( );

    /**
     * We need to know what zoom level each texture lives at so we can remove stale textures
     * when the zoom level changes.
     */
    private final ConcurrentHashMap<RGBTextureProjected2D, Integer> texZoomMap = new ConcurrentHashMap<>( );

    public SlippyMapTilePainter( GeoProjection geoProj, List<String> prefixes, ExecutorService exec, Path cacheDir, int maxZoom )
    {
        this.geoProj = geoProj;
        this.cache = new SlippyCache( geoProj, prefixes, cacheDir );
        this.maxZoom = maxZoom;
        this.slippyProj = new SlippyProjection[maxZoom + 1];
        for ( int zoom = 0; zoom <= maxZoom; zoom++ )
        {
            this.slippyProj[zoom] = new SlippyProjection( zoom );
        }
        this.exec = exec;
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        updateTiles( axis );
        super.paintTo( context, bounds, axis );
    }

    protected void updateTiles( Axis2D axis )
    {
        int xPix = axis.getAxisX( ).getSizePixels( );
        if ( !isVisible( ) || xPix <= 0 )
        {
            return;
        }

        double minx = axis.getMinX( );
        double maxx = axis.getMaxX( );
        double maxy = axis.getMaxY( );
        double miny = axis.getMinY( );
        double[] bounds = new double[] { minx, maxx, miny, maxy };
        if ( Arrays.equals( bounds, lastBounds.get( ) ) )
        {
            return;
        }
        lastBounds.set( bounds );

        double xTileDim = xPix / 256.;

        LatLonGeo ne = geoProj.unproject( maxx, maxy );
        LatLonGeo sw = geoProj.unproject( minx, miny );

        double east = ne.getLonDeg( );
        double west = sw.getLonDeg( );
        double lonSizeDeg = ( east - west ) / xTileDim;

        double zoomApprox = Math.log( 360 / lonSizeDeg ) / LOG2;
        final int zoom = ( int ) Math.min( Math.round( zoomApprox ), maxZoom );
        lastZoom.set( zoom );

        Vector2d tileNE = slippyProj[zoom].project( ne );
        Vector2d tileSW = slippyProj[zoom].project( sw );

        final int tileYmin = ( int ) Math.floor( tileNE.getY( ) );
        final int tileYmax = ( int ) Math.ceil( tileSW.getY( ) );
        final int tileXmin = ( int ) Math.floor( tileSW.getX( ) );
        final int tileXmax = ( int ) Math.ceil( tileNE.getX( ) );

        lock.lock( );
        try
        {
            for ( int y = tileYmin; y < tileYmax; y++ )
            {
                for ( int x = tileXmin; x < tileXmax; x++ )
                {
                    RGBTextureProjected2D tex = cache.getTextureIfPresent( zoom, x, y );
                    if ( tex != null && drawableTextures.containsKey( new TextureUnit<DrawableTexture>( tex ) ) )
                    {
                        continue;
                    }
                    exec.submit( new FetchTexture( zoom, x, y ) );
                }
            }

            Iterator<TextureUnit<DrawableTexture>> itr = drawableTextures.keySet( ).iterator( );
            while ( itr.hasNext( ) )
            {
                TextureUnit<DrawableTexture> texUnit = itr.next( );
                TextureProjected2D tex = ( TextureProjected2D ) texUnit.getTexture( );
                double[] texBounds = getBounds( tex.getProjection( ) );
                int texZoom = texZoomMap.get( tex );
                if ( texZoom != zoom || !intersect( bounds, texBounds ) )
                {
                    itr.remove( );
                    texZoomMap.remove( tex );
                }
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    private double[] getBounds( final Projection proj )
    {
        float[] min = new float[2];
        float[] max = new float[2];
        proj.getVertexXY( 0, 0, min );
        proj.getVertexXY( 1, 1, max );
        return new double[] { min[0], max[0], min[1], max[1] };
    }

    private double[] getBounds( int zoom, int tileX, int tileY )
    {
        return getBounds( getProjection( zoom, tileX, tileY ) );
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

    private static boolean intersect( final double[] outerBounds, final double[] bounds )
    {
        return contains( outerBounds, bounds[0], bounds[2] )
                || contains( outerBounds, bounds[0], bounds[3] )
                || contains( outerBounds, bounds[1], bounds[2] )
                || contains( outerBounds, bounds[1], bounds[3] )
                || contains( bounds, outerBounds[0], outerBounds[2] )
                || contains( bounds, outerBounds[0], outerBounds[3] )
                || contains( bounds, outerBounds[1], outerBounds[2] )
                || contains( bounds, outerBounds[1], outerBounds[3] );
    }

    private static boolean contains( final double[] bounds, final double x, final double y )
    {
        return ! ( x < bounds[0] || bounds[1] < x || y < bounds[2] || bounds[3] < y );
    }

    private final class FetchTexture implements Runnable
    {
        private final int zoom;
        private final int x;
        private final int y;

        private FetchTexture( int zoom, int x, int y )
        {
            this.zoom = zoom;
            this.x = x;
            this.y = y;
        }

        @Override
        public void run( )
        {
            final double[] bounds = getBounds( zoom, x, y );
            try
            {
                RGBTextureProjected2D tex = null;
                int zoomCheck = lastZoom.get( );
                double[] boundsCheck = lastBounds.get( );
                if ( zoom == zoomCheck && intersect( boundsCheck, bounds ) )
                {
                    tex = cache.getTexture( zoom, x, y );
                }
                if ( tex == null )
                {
                    return;
                }
                zoomCheck = lastZoom.get( );
                boundsCheck = lastBounds.get( );
                if ( zoom == zoomCheck && intersect( boundsCheck, bounds ) )
                {
                    lock.lock( );
                    try
                    {
                        addDrawableTexture( tex );
                        texZoomMap.put( tex, zoom );
                    }
                    finally
                    {
                        lock.unlock( );
                    }
                }
            }
            catch ( Exception e )
            {
                logger.log( Level.WARNING, "Exception in tile fetching thread", e );
            }
            finally
            {
                //nothing?
            }
        }
    }
}

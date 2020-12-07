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
package com.metsci.glimpse.core.painter.geo;

import static com.metsci.glimpse.core.painter.base.GlimpsePainterBase.getAxis2D;
import static com.metsci.glimpse.core.support.PainterCache.SHARED_EXEC;
import static com.metsci.glimpse.util.GeneralUtils.clamp;
import static java.lang.Math.abs;
import static java.lang.Math.hypot;
import static java.lang.Math.max;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.painter.group.DelegatePainter;
import com.metsci.glimpse.core.support.PainterCache;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.primitives.sorted.SortedDoubles;
import com.metsci.glimpse.util.primitives.sorted.SortedDoublesArray;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * Paints a geo with tiled data. The subclass must implement the actual painting of the tiles themselves.
 * This class takes care of identifying the visible tiles (and tiles that are appropriate to paint in
 * the current projection) and providing those to the subclass to paint.
 *
 * @author borkholder
 */
public abstract class TilePainter<V> extends DelegatePainter
{
    protected static final double ANTIMERIDIAN_EPSILON = 1e-5;

    protected GeoProjection projection;
    protected SortedDoubles lengthScale;
    protected Map<TileKey, Area> tileBounds;

    protected PainterCache<TileKey, V> cacheData;
    protected Rectangle2D.Double lastAxis;

    public TilePainter( GeoProjection projection )
    {
        this.projection = projection;
        lastAxis = new Rectangle2D.Double( );

        cacheData = new PainterCache<>( this::loadTileData, SHARED_EXEC );
    }

    @Override
    public void paintTo( GlimpseContext context )
    {
        checkNewState( context );
        super.paintTo( context );
    }

    private void checkNewState( GlimpseContext context )
    {
        Axis2D axis = getAxis2D( context );

        if ( tileBounds == null )
        {
            tileBounds = createTileAreas( );
            lengthScale = populateLengthScale( tileBounds.keySet( ) );
        }

        if ( abs( lastAxis.getMinX( ) - axis.getMinX( ) ) > 1e-9 ||
                abs( lastAxis.getMaxX( ) - axis.getMaxX( ) ) > 1e-9 ||
                abs( lastAxis.getMinY( ) - axis.getMinY( ) ) > 1e-9 ||
                abs( lastAxis.getMaxY( ) - axis.getMaxY( ) ) > 1e-9 )
        {
            lastAxis = new Rectangle2D.Double( axis.getMinX( ), axis.getMinY( ), axis.getMaxX( ) - axis.getMinX( ), axis.getMaxY( ) - axis.getMinY( ) );

            Collection<TileKey> tiles = getVisibleTiles( lastAxis, axis, tileBounds.entrySet( ).stream( ) );
            List<Entry<TileKey, V>> newTileData = new ArrayList<>( tiles.size( ) );
            boolean anyMissed = false;
            for ( TileKey key : tiles )
            {
                V data = cacheData.get( key );
                if ( data == null )
                {
                    anyMissed = true;
                }
                else
                {
                    newTileData.add( new AbstractMap.SimpleImmutableEntry<>( key, data ) );
                }
            }

            replaceTileData( newTileData );

            if ( anyMissed )
            {
                lastAxis = new Rectangle2D.Double( );
            }
        }
    }

    protected static double clampAntiMeridian( double lon_DEG )
    {
        return clamp( lon_DEG, -180 + ANTIMERIDIAN_EPSILON, 180 - ANTIMERIDIAN_EPSILON );
    }

    protected static double clampNorthSouth( double lat_DEG )
    {
        return clamp( lat_DEG, -90 + ANTIMERIDIAN_EPSILON, 90 - ANTIMERIDIAN_EPSILON );
    }

    protected Map<TileKey, Area> createTileAreas( )
    {
        Map<TileKey, Area> keys = new HashMap<>( );
        for ( TileKey key : allKeys( ) )
        {
            double minLat = clampNorthSouth( key.minLat_DEG );
            double minLon = clampAntiMeridian( key.minLon_DEG );
            double maxLat = clampNorthSouth( key.maxLat_DEG );
            double maxLon = clampAntiMeridian( key.maxLon_DEG );

            Vector2d sw = projection.project( LatLonGeo.fromDeg( minLat, minLon ) );
            Vector2d nw = projection.project( LatLonGeo.fromDeg( maxLat, minLon ) );
            Vector2d ne = projection.project( LatLonGeo.fromDeg( maxLat, maxLon ) );
            Vector2d se = projection.project( LatLonGeo.fromDeg( minLat, maxLon ) );

            /*
             * If the border is clockwise, the tile is valid in the current
             * projection. This test will fail for tiles at the edges of a
             * TangentPlane because of how skewed they are.
             */
            double sumOverEdge = 0;
            sumOverEdge += ( se.getX( ) - ne.getX( ) ) * ( se.getY( ) + ne.getY( ) );
            sumOverEdge += ( sw.getX( ) - se.getX( ) ) * ( sw.getY( ) + se.getY( ) );
            sumOverEdge += ( nw.getX( ) - sw.getX( ) ) * ( nw.getY( ) + sw.getY( ) );
            sumOverEdge += ( ne.getX( ) - nw.getX( ) ) * ( ne.getY( ) + nw.getY( ) );
            if ( sumOverEdge > 0 )
            {
                /*
                 * Now we're going to create a shape in the projection with enough points to compute a reasonable intersection with the viewport.
                 */
                double dlat = ( maxLat - minLat ) / 5;
                double dlon = ( maxLon - minLon ) / 5;
                Path2D path = new Path2D.Double( Path2D.WIND_EVEN_ODD );

                // sw to nw
                path.moveTo( sw.getX( ), sw.getY( ) );
                for ( double lat = minLat + dlat; lat < maxLat; lat += dlat )
                {
                    Vector2d p = projection.project( LatLonGeo.fromDeg( lat, minLon ) );
                    path.lineTo( p.getX( ), p.getY( ) );
                }

                // nw to ne
                path.lineTo( nw.getX( ), nw.getY( ) );
                for ( double lon = minLon + dlon; lon < maxLon; lon += dlon )
                {
                    Vector2d p = projection.project( LatLonGeo.fromDeg( maxLat, lon ) );
                    path.lineTo( p.getX( ), p.getY( ) );
                }

                // ne to se
                path.lineTo( ne.getX( ), ne.getY( ) );
                for ( double lat = maxLat - dlat; lat > minLat; lat -= dlat )
                {
                    Vector2d p = projection.project( LatLonGeo.fromDeg( lat, maxLon ) );
                    path.lineTo( p.getX( ), p.getY( ) );
                }

                // se to sw
                path.lineTo( se.getX( ), se.getY( ) );
                for ( double lon = maxLon - dlon; lon > minLon; lon -= dlon )
                {
                    Vector2d p = projection.project( LatLonGeo.fromDeg( minLat, lon ) );
                    path.lineTo( p.getX( ), p.getY( ) );
                }

                path.closePath( );
                keys.put( key, new Area( path ) );
            }
        }

        return keys;
    }

    protected SortedDoubles populateLengthScale( Collection<TileKey> allKeys )
    {
        double[] array = allKeys.stream( )
                .mapToDouble( k -> k.lengthScale )
                .distinct( )
                .sorted( )
                .toArray( );
        return new SortedDoublesArray( array );
    }

    protected Collection<TileKey> getVisibleTiles( Rectangle2D bounds, Axis2D axis, Stream<Entry<TileKey, Area>> keys )
    {
        // Pad for irregular projections
        double padX = bounds.getWidth( ) * 0.02;
        double padY = bounds.getHeight( ) * 0.02;
        Rectangle2D b = new Rectangle2D.Double( bounds.getMinX( ) - padX, bounds.getMinY( ) - padY, bounds.getWidth( ) + 2 * padX, bounds.getHeight( ) + 2 * padY );
        double scale = getLengthScale( bounds, axis );

        return keys.filter( e -> e.getKey( ).lengthScale == scale )
                .filter( e -> e.getValue( ).intersects( b ) )
                .map( Entry::getKey )
                .collect( Collectors.toList( ) );
    }

    protected double getLengthScale( Rectangle2D bounds, Axis2D axis )
    {
        if ( lengthScale.n( ) == 1 )
        {
            return lengthScale.v( 0 );
        }

        LatLonGeo center = projection.unproject( bounds.getCenterX( ), bounds.getCenterY( ) );
        LatLonGeo off = projection.unproject( bounds.getCenterX( ) + 1.0 / axis.getAxisX( ).getPixelsPerValue( ), bounds.getCenterY( ) + 1.0 / axis.getAxisY( ).getPixelsPerValue( ) );
        double dist = center.getDistanceTo( off );
        dist *= hypot( bounds.getWidth( ) / 2 * axis.getAxisX( ).getPixelsPerValue( ), bounds.getHeight( ) / 2 * axis.getAxisY( ).getPixelsPerValue( ) );

        int idx = max( 0, lengthScale.indexAtOrBefore( dist ) );
        return lengthScale.v( idx );
    }

    /**
     * Provides all available keys.  This does not need to cover the globe.
     *
     * This method does not need to be fast, it will be called once when
     * painting starts.
     */
    protected abstract Collection<TileKey> allKeys( );

    /**
     * Loads the tile data for a specific key.  This method must be thread-safe
     * and will be called on multiple threads at the same time.
     */
    protected abstract V loadTileData( TileKey key );

    /**
     * Loads the given tile data into the painter.  This is called on the painter
     * thread and all given tile data is valid.  However, not all tiles in the
     * visible area are loaded and provided here.  This method may be called
     * multiple times successively as new tiles are loaded into the view.
     */
    protected abstract void replaceTileData( Collection<Entry<TileKey, V>> tileData );
}

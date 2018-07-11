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
package com.metsci.glimpse.charts.bathy;

import static com.metsci.glimpse.painter.base.GlimpsePainterBase.getAxis2D;
import static com.metsci.glimpse.util.GeneralUtils.clamp;
import static java.lang.Math.hypot;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

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
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.support.PainterCache;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.primitives.sorted.SortedDoubles;
import com.metsci.glimpse.util.primitives.sorted.SortedDoublesArray;
import com.metsci.glimpse.util.vector.Vector2d;

import it.unimi.dsi.fastutil.doubles.DoubleAVLTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;

/**
 * @author borkholder
 */
public abstract class TilePainter<V> extends DelegatePainter
{
    protected static final double ANTIMERIDIAN_EPSILON = 1e-5;
    protected static Executor SHARED_EXEC;

    protected GeoProjection projection;
    protected SortedDoubles lengthScale;
    protected Map<TileKey, Area> tileBounds;

    protected PainterCache<TileKey, V> cacheData;
    protected Rectangle2D.Double lastAxis;

    public TilePainter( GeoProjection projection )

    {
        this.projection = projection;
        lastAxis = new Rectangle2D.Double( );

        synchronized ( TilePainter.class )
        {
            if ( SHARED_EXEC == null )
            {
                SHARED_EXEC = newFixedThreadPool( max( getRuntime( ).availableProcessors( ) - 2, 1 ) );
            }
        }

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

        if ( lastAxis.getMinX( ) != axis.getMinX( ) ||
                lastAxis.getMaxX( ) != axis.getMaxX( ) ||
                lastAxis.getMinY( ) != axis.getMinY( ) ||
                lastAxis.getMaxY( ) != axis.getMaxY( ) )
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

    protected double clampAntiMeridian( double lon_DEG )
    {
        return clamp( lon_DEG, -180 + ANTIMERIDIAN_EPSILON, 180 - ANTIMERIDIAN_EPSILON );
    }

    protected double clampNorthSouth( double lat_DEG )
    {
        return clamp( lat_DEG, -90 + ANTIMERIDIAN_EPSILON, 90 - ANTIMERIDIAN_EPSILON );
    }

    private Map<TileKey, Area> createTileAreas( )
    {
        Map<TileKey, Area> keys = new HashMap<>( );
        for ( TileKey key : allKeys( ) )
        {
            Vector2d sw = projection.project( LatLonGeo.fromDeg( clampNorthSouth( key.minLat ), clampAntiMeridian( key.minLon ) ) );
            Vector2d nw = projection.project( LatLonGeo.fromDeg( clampNorthSouth( key.maxLat ), clampAntiMeridian( key.minLon ) ) );
            Vector2d ne = projection.project( LatLonGeo.fromDeg( clampNorthSouth( key.maxLat ), clampAntiMeridian( key.maxLon ) ) );
            Vector2d se = projection.project( LatLonGeo.fromDeg( clampNorthSouth( key.minLat ), clampAntiMeridian( key.maxLon ) ) );

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
                Path2D path = new Path2D.Double( Path2D.WIND_EVEN_ODD );
                path.moveTo( sw.getX( ), sw.getY( ) );
                path.lineTo( nw.getX( ), nw.getY( ) );
                path.lineTo( ne.getX( ), ne.getY( ) );
                path.lineTo( se.getX( ), se.getY( ) );
                path.closePath( );
                keys.put( key, new Area( path ) );
            }
        }

        return keys;
    }

    protected SortedDoubles populateLengthScale( Collection<TileKey> allKeys )
    {
        DoubleSortedSet set = new DoubleAVLTreeSet( );
        allKeys.forEach( k -> set.add( k.lengthScale ) );
        return new SortedDoublesArray( set.toDoubleArray( ) );
    }

    protected Collection<TileKey> getVisibleTiles( Rectangle2D bounds, Axis2D axis, Stream<Entry<TileKey, Area>> keys )
    {
        // Pad for irregular projections
        double padX = bounds.getWidth( ) * 0.02;
        double padY = bounds.getHeight( ) * 0.02;
        Rectangle2D b = new Rectangle2D.Double( bounds.getMinX( ) - padX, bounds.getMinY( ) - padY, bounds.getWidth( ) + 2 * padX, bounds.getHeight( ) + 2 * padY );
        double scale = getLengthScale( bounds, axis );

        return keys.filter( e -> ( e.getKey( ) ).lengthScale == scale )
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

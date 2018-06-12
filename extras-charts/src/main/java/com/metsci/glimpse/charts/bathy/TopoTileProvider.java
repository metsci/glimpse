package com.metsci.glimpse.charts.bathy;

import java.io.IOException;
import java.util.Collection;

/**
 * Provides tiles to the {@link TilePainter}.  Tiles are expected to be rectilinear in lat/lon space.
 *
 * @author borkholder
 */
public interface TopoTileProvider
{
    Collection<TileKey> keys( );

    TopographyData getTile( TileKey key ) throws IOException;

    String getAttribution( );
}

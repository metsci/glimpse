package com.metsci.glimpse.charts.bathy;

import java.io.IOException;
import java.util.Collection;

/**
 * Provides tiles to the {@link TiledPainter}.  Tiles are expected to be rectilinear in lat/lon space.
 *
 * @author borkholder
 */
public interface TileProvider
{
    Collection<TileKey> keys();

    TopographyData getTile( int pixelX0, int pixelY0, int pixelWidth, int pixelHeight ) throws IOException;

    String getAttribution( );
}

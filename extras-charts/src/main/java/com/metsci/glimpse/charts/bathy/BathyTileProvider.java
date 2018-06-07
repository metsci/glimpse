package com.metsci.glimpse.charts.bathy;

import java.io.IOException;

import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * Provides bathymetry tiles to painters.  Tiles are expected to be rectilinear in lat/lon space.
 *
 * @author borkholder
 */
public interface BathyTileProvider
{
    int getPixelsX( );

    int getPixelsY( );

    BathymetryData getTile( GeoProjection projection, int pixelX0, int pixelY0, int pixelWidth, int pixelHeight ) throws IOException;
}

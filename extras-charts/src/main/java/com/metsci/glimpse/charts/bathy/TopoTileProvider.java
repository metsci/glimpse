package com.metsci.glimpse.charts.bathy;

import java.io.IOException;

/**
 * Provides topographic tiles to painters.  Tiles are expected to be rectilinear in lat/lon space and units are SU.
 *
 * @author borkholder
 */
public interface TopoTileProvider
{
    int getPixelsX( );

    int getPixelsY( );

    TopographyData getTile( int pixelX0, int pixelY0, int pixelWidth, int pixelHeight ) throws IOException;
}

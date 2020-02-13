/**
 * Rendering of geographic, coastline, and bathymetric data.
 */
open module com.metsci.glimpse.charts
{
    requires transitive com.metsci.glimpse.core;

    exports com.metsci.glimpse.charts.bathy;
    exports com.metsci.glimpse.charts.raster;
    exports com.metsci.glimpse.charts.shoreline;
    exports com.metsci.glimpse.charts.shoreline.gshhs;
    exports com.metsci.glimpse.charts.shoreline.ndgc;
    exports com.metsci.glimpse.charts.slippy;
}

/**
 * Rendering of geographic, coastline, and bathymetric data.
 */
module com.metsci.glimpse.charts
{
    exports com.metsci.glimpse.charts.raster;
    exports com.metsci.glimpse.charts.shoreline.ndgc;
    exports com.metsci.glimpse.charts.slippy;
    exports com.metsci.glimpse.charts.shoreline;
    exports com.metsci.glimpse.charts.shoreline.gshhs;
    exports com.metsci.glimpse.charts.bathy;

    opens com.metsci.glimpse.charts.shaders.relief;
    
    requires transitive com.metsci.glimpse.core;

    requires transitive com.google.common;
    requires transitive it.unimi.dsi.fastutil;

    requires transitive jogamp.fat;
}

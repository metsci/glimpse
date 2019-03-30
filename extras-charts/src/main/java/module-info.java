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

	requires transitive com.metsci.glimpse.core;
}

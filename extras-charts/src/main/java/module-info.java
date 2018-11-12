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

	requires com.metsci.glimpse.core;
	requires com.metsci.glimpse.util;
	requires fastutil;
	requires guava;
	requires java.desktop;
	requires java.logging;
	requires java.base;
}

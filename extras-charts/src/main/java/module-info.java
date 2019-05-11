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
	
	requires transitive jogl.all.main;
	requires transitive jogl.all;
	requires transitive gluegen.rt.main;
	requires transitive gluegen.rt;
	
	requires transitive com.google.common;
	requires transitive it.unimi.dsi.fastutil;

}

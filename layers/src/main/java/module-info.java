/**
 * A flexible data organization framework for building structured applications using Glimpse and Glimpse Layers.
 */
module com.metsci.glimpse.layers
{
	exports com.metsci.glimpse.layers;
	exports com.metsci.glimpse.layers.time;
	exports com.metsci.glimpse.layers.geo;
	exports com.metsci.glimpse.layers.misc;

	requires com.metsci.glimpse.core;
	requires com.metsci.glimpse.docking;
	requires com.metsci.glimpse.util;
	requires guava;
	requires miglayout.swing;
	requires java.desktop;
	requires java.logging;
	requires java.base;
}

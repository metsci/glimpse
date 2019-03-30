/**
 * A flexible data organization framework for building structured applications using Glimpse and Glimpse Layers.
 */
module com.metsci.glimpse.layers
{
	exports com.metsci.glimpse.layers;
	exports com.metsci.glimpse.layers.time;
	exports com.metsci.glimpse.layers.geo;
	exports com.metsci.glimpse.layers.misc;

	requires transitive com.metsci.glimpse.docking;
	requires transitive com.metsci.glimpse.core;

}

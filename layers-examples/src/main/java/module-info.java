/**
 * A flexible data organization framework for building structured applications using Glimpse and Glimpse Layers.
 */
module com.metsci.glimpse.layers.examples
{
	exports com.metsci.glimpse.examples.layers;

	requires transitive com.metsci.glimpse.docking;
	requires transitive com.metsci.glimpse.core;
	requires transitive com.metsci.glimpse.layers;
	requires transitive com.metsci.glimpse.tinylaf;
	
	requires transitive com.google.common;
}

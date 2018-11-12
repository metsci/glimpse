/**
 * Rendering of topographic data.
 */
module com.metsci.glimpse.topo
{
	exports com.metsci.glimpse.topo.proj;
	exports com.metsci.glimpse.topo;
	exports com.metsci.glimpse.topo.io;

	requires com.metsci.glimpse.core;
	requires com.metsci.glimpse.util;
	requires guava;
	requires java.desktop;
	requires java.logging;
	requires java.base;
}

/**
 * Rendering of topographic data.
 */
module com.metsci.glimpse.topo
{
	exports com.metsci.glimpse.topo.proj;
	exports com.metsci.glimpse.topo;
	exports com.metsci.glimpse.topo.io;

	requires transitive com.metsci.glimpse.core;
	
	requires transitive jogl.all.main;
	requires transitive jogl.all;
	requires transitive gluegen.rt.main;
	requires transitive gluegen.rt;
	
	requires transitive com.google.common;
}

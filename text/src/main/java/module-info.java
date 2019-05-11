/**
 * Text rendering support.
 */
module com.metsci.glimpse.text
{
	exports com.metsci.glimpse.com.jogamp.opengl.util.awt;
	exports com.metsci.glimpse.com.jogamp.opengl.util.packrect;
	exports com.metsci.glimpse.jogamp.opengl.util.awt.text;

	requires transitive jogl.all.main;
	requires transitive jogl.all;
	requires transitive gluegen.rt.main;
	requires transitive gluegen.rt;
	
	requires transitive java.desktop;
	requires java.base;
}

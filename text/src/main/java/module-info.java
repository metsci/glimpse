/**
 * Text rendering support.
 */
module com.metsci.glimpse.text
{
	exports com.metsci.glimpse.com.jogamp.opengl.util.awt;
	exports com.metsci.glimpse.com.jogamp.opengl.util.packrect;
	exports com.metsci.glimpse.jogamp.opengl.util.awt.text;

	requires jogl.all.main;
	requires jogl.all;
	requires gluegen.rt.main;
	requires gluegen.rt;
	requires java.desktop;
	requires java.base;
}

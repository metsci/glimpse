/**
 * Text rendering support.
 */
module com.metsci.glimpse.text
{
	exports com.metsci.glimpse.com.jogamp.opengl.util.awt;
	exports com.metsci.glimpse.com.jogamp.opengl.util.packrect;
	exports com.metsci.glimpse.jogamp.opengl.util.awt.text;

	requires transitive java.desktop;
	requires java.base;
	requires jogamp.fat;
}

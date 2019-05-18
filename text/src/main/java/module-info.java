/**
 * Text rendering support.
 */
module com.metsci.glimpse.text
{
	exports com.metsci.glimpse.com.jogamp.opengl.util.awt;
	exports com.metsci.glimpse.com.jogamp.opengl.util.packrect;
	exports com.metsci.glimpse.jogamp.opengl.util.awt.text;

	requires java.desktop;
	
	// transitive because many Glimpse API methods take JOLG
	// arguments, such as OpenGL context classes like com.jogl.opengl.GL
	requires transitive jogamp.fat;
}

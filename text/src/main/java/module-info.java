/**
 * Text rendering support.
 */
open module com.metsci.glimpse.text
{
    requires transitive java.desktop;
    requires transitive jogl.all;
    requires transitive gluegen.rt;

    exports com.metsci.glimpse.com.jogamp.opengl.util.awt;
    exports com.metsci.glimpse.com.jogamp.opengl.util.packrect;
    exports com.metsci.glimpse.jogamp.opengl.util.awt.text;
}

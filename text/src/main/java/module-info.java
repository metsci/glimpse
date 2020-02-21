/**
 * Text rendering support.
 */
open module com.metsci.glimpse.text
{
    requires transitive java.desktop;
    requires transitive com.jogamp.gluegen;
    requires transitive com.jogamp.opengl;

    exports com.metsci.glimpse.com.jogamp.opengl.util.awt;
    exports com.metsci.glimpse.com.jogamp.opengl.util.packrect;
    exports com.metsci.glimpse.jogamp.opengl.util.awt.text;
}

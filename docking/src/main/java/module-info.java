/**
 * Lightweight support for reconfigurable, draggable Swing tabbed panes which integrate well with GlimpseCanvas.
 */
module com.metsci.glimpse.docking
{
    exports com.metsci.glimpse.docking.group.dialog;
    exports com.metsci.glimpse.docking.xml;
    exports com.metsci.glimpse.docking;
    exports com.metsci.glimpse.docking.group;
    exports com.metsci.glimpse.docking.group.frame;

    requires transitive com.metsci.glimpse.util;

    requires transitive com.sun.tools.jxc;
    requires transitive java.xml.bind;
    requires transitive com.sun.xml.bind;

    requires transitive tinylaf;
    requires transitive com.google.common;
    requires transitive java.desktop;
    requires transitive java.logging;

}

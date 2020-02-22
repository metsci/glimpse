/**
 * Draggable tabbed panes in Swing.
 */
open module com.metsci.glimpse.docking
{
    requires transitive com.metsci.glimpse.util;
    requires transitive com.metsci.glimpse.tinylaf;
    requires transitive java.xml.bind;

    exports com.metsci.glimpse.docking;
    exports com.metsci.glimpse.docking.group;
    exports com.metsci.glimpse.docking.group.dialog;
    exports com.metsci.glimpse.docking.group.frame;
    exports com.metsci.glimpse.docking.xml;
}

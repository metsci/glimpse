open module com.metsci.glimpse.extras.examples
{
    requires transitive com.metsci.glimpse.core;
    requires transitive com.metsci.glimpse.core.examples;
    requires transitive com.metsci.glimpse.charts;
    requires transitive com.metsci.glimpse.dnc;
    requires transitive com.metsci.glimpse.docking;
    requires transitive com.metsci.glimpse.topo;
    requires transitive miglayout.swing;
    requires transitive swingx.core.and.plaf;
    requires transitive swingx.action;
    requires transitive swingx.autocomplete;
    requires transitive swingx.common;
    requires transitive swingx.graphics;
    requires transitive swingx.painters;
    requires transitive jts.core;

    exports com.metsci.glimpse.extras.examples.charts.shoreline;
    exports com.metsci.glimpse.extras.examples.charts.rnc;
    exports com.metsci.glimpse.extras.examples.charts.bathy;
    exports com.metsci.glimpse.extras.examples.topo;
    exports com.metsci.glimpse.extras.examples;
    exports com.metsci.glimpse.extras.examples.charts.slippy;
    exports com.metsci.glimpse.extras.examples.dnc;
}

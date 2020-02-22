/**
 * A flexible data organization framework for building structured applications using Glimpse and Glimpse Layers.
 */
open module com.metsci.glimpse.layers
{
    requires transitive com.metsci.glimpse.core;
    requires transitive com.metsci.glimpse.docking;
    requires transitive miglayout.swing;

    exports com.metsci.glimpse.layers;
    exports com.metsci.glimpse.layers.geo;
    exports com.metsci.glimpse.layers.misc;
    exports com.metsci.glimpse.layers.time;
}

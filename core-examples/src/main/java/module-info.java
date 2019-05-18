module com.metsci.glimpse.core.examples
{
    exports com.metsci.glimpse.examples.plot;
    exports com.metsci.glimpse.examples.axis;
    exports com.metsci.glimpse.examples.shape;
    exports com.metsci.glimpse.examples.icon;
    exports com.metsci.glimpse.examples.timeline;
    exports com.metsci.glimpse.examples;
    exports com.metsci.glimpse.examples.line;
    exports com.metsci.glimpse.examples.screenshot;
    exports com.metsci.glimpse.examples.projection;
    exports com.metsci.glimpse.examples.scatterplot;
    exports com.metsci.glimpse.examples.misc;
    exports com.metsci.glimpse.examples.layout;
    exports com.metsci.glimpse.examples.heatmap;
    exports com.metsci.glimpse.examples.track;
    
    opens com.metsci.glimpse.core.examples.icons.fugue;
    opens com.metsci.glimpse.core.examples.data;
    opens com.metsci.glimpse.core.examples.images;

    requires transitive com.metsci.glimpse.core;
    requires transitive com.metsci.glimpse.util;
    requires transitive com.metsci.glimpse.text;

    requires transitive it.unimi.dsi.fastutil;
    requires transitive com.google.common;
    requires transitive java.desktop;
    requires transitive java.logging;

    requires transitive jogamp.fat;
}
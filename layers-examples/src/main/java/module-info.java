/**
 * A flexible data organization framework for building structured applications using Glimpse and Glimpse Layers.
 */
module com.metsci.glimpse.layers.examples
{
    exports com.metsci.glimpse.examples.layers;

    requires transitive com.metsci.glimpse.docking;
    requires transitive com.metsci.glimpse.core;
    requires transitive com.metsci.glimpse.layers;
    requires transitive com.metsci.glimpse.tinylaf;

    requires transitive com.google.common;
    requires transitive com.sun.istack.runtime;
    requires transitive com.sun.tools.xjc;
    requires transitive com.sun.xml.fastinfoset;
    requires transitive com.sun.xml.txw2;
    requires transitive it.unimi.dsi.fastutil;
    requires transitive java.activation;
    requires transitive org.jvnet.staxex;
    requires transitive relaxngDatatype;
    
    opens com.metsci.glimpse.examples.layers.config;
    opens com.metsci.glimpse.examples.layers.shader;
}

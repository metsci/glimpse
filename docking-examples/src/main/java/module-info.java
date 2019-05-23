module com.metsci.glimpse.docking.examples
{
    exports com.metsci.glimpse.docking.examples;

    opens com.metsci.glimpse.docking.examples.docking;
    opens com.metsci.glimpse.docking.examples.icons;

    requires transitive com.metsci.glimpse.docking;
    requires transitive com.metsci.glimpse.tinylaf;
    requires transitive com.metsci.glimpse.core.examples;
    requires transitive com.metsci.glimpse.platformFixes;

    requires transitive com.sun.istack.runtime;
    requires transitive com.sun.tools.xjc;
    requires transitive com.sun.xml.fastinfoset;
    requires transitive com.sun.xml.txw2;
    requires transitive java.activation;
    requires transitive miglayout.core;
    requires transitive org.jvnet.staxex;
    requires transitive relaxngDatatype;
}

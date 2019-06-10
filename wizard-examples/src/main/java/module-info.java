/**
 * General utility methods.
 */
module com.metsci.glimpse.wizard.examples
{
    exports com.metsci.glimpse.wizard.simple;

    opens com.metsci.glimpse.wizard.simple.pages.descriptions;

    requires transitive com.metsci.glimpse.util;
    requires transitive com.metsci.glimpse.wizard;

    requires transitive com.sun.codemodel;
    requires transitive com.sun.istack.tools;
    requires transitive com.sun.tools.jxc;
    requires transitive com.sun.tools.rngom;
    requires transitive com.sun.xml.bind;
    requires transitive com.sun.xml.dtdparser;
    requires transitive com.sun.xml.xsom;
    requires transitive tinylaf;
    requires transitive java.desktop;
    requires java.base;
}

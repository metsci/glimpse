/**
 * Utility methods for working with Google Dataset Publishing Language (DSPL) in Java.
 */
module com.metsci.glimpse.dspl
{
    exports com.metsci.glimpse.dspl.lite;
    exports com.metsci.glimpse.dspl.util;
    exports com.metsci.glimpse.dspl.canonical;
    exports com.metsci.glimpse.dspl.parser;
    exports com.metsci.glimpse.dspl;
    exports com.metsci.glimpse.dspl.lite.schema;
    exports com.metsci.glimpse.dspl.schema;
    exports com.metsci.glimpse.dspl.parser.column;
    exports com.metsci.glimpse.dspl.parser.column.dynamic;
    exports com.metsci.glimpse.dspl.parser.table;
    exports com.metsci.glimpse.dspl.parser.util;

    opens com.metsci.glimpse.dspl.schema to java.xml.bind;

    opens com.metsci.glimpse.dspl;
    opens com.metsci.glimpse.dspl.canonical.google;
    opens com.metsci.glimpse.dspl.canonical.metron;

    requires transitive com.metsci.glimpse.util;

    requires transitive relaxngDatatype;
    requires transitive com.sun.xml.xsom;
    requires transitive com.sun.codemodel;
    requires transitive com.sun.tools.rngom;
    requires transitive com.sun.istack.tools;
    requires transitive com.sun.xml.dtdparser;
    requires transitive com.sun.tools.jxc;
    requires transitive java.xml.bind;
    requires transitive com.sun.xml.bind;

    requires transitive joda.time;
    requires transitive it.unimi.dsi.fastutil;
    requires transitive java.logging;
}

/**
 * 
 */
/**
 * @author ulman
 *
 */
module com.metsci.glimpse.extras.examples
{
    exports com.metsci.glimpse.extras.examples.charts.shoreline;
    exports com.metsci.glimpse.extras.examples.charts.rnc;
    exports com.metsci.glimpse.extras.examples.charts.bathy;
    exports com.metsci.glimpse.extras.examples.topo;
    exports com.metsci.glimpse.extras.examples;
    exports com.metsci.glimpse.extras.examples.charts.slippy;
    exports com.metsci.glimpse.extras.examples.dspl;
    exports com.metsci.glimpse.extras.examples.dnc;

    opens com.metsci.glimpse.extras.examples.data;
    opens com.metsci.glimpse.extras.examples.dnc;
    opens com.metsci.glimpse.extras.examples.dspl;
    opens com.metsci.glimpse.extras.examples.dspl.lite;
    opens com.metsci.glimpse.extras.examples.dspl.track;
    opens com.metsci.glimpse.extras.examples.icons.eclipse;
    opens com.metsci.glimpse.extras.examples.icons.fugue;
    opens com.metsci.glimpse.extras.examples.topo;
    
    requires transitive com.metsci.glimpse.charts;
    requires transitive com.metsci.glimpse.core;
    requires transitive com.metsci.glimpse.core.examples;
    requires transitive com.metsci.glimpse.dnc;
    requires transitive com.metsci.glimpse.docking;
    requires transitive com.metsci.glimpse.dspl;
    requires transitive com.metsci.glimpse.text;
    requires transitive com.metsci.glimpse.tinylaf;
    requires transitive com.metsci.glimpse.topo;
    requires transitive com.metsci.glimpse.util;

    requires transitive com.sun.tools.jxc;
    requires transitive java.xml.bind;
    requires transitive com.sun.xml.bind;

    requires transitive miglayout.swing;
    requires transitive swingx.core;
    requires transitive jts.core;

    requires transitive jogamp.fat;
}

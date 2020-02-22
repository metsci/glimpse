/**
 * Rendering of Digital Nautical Charts (DNC).
 */
open module com.metsci.glimpse.dnc
{
    requires transitive com.metsci.glimpse.core;
    requires transitive svg.salamander;
    requires transitive worldwind;

    exports com.metsci.glimpse.dnc;
    exports com.metsci.glimpse.dnc.convert;
    exports com.metsci.glimpse.dnc.facc;
    exports com.metsci.glimpse.dnc.geosym;
    exports com.metsci.glimpse.dnc.proj;
    exports com.metsci.glimpse.dnc.util;
}

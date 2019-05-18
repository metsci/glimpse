/**
 * Rendering of topographic data.
 */
module com.metsci.glimpse.topo
{
    exports com.metsci.glimpse.topo.proj;
    exports com.metsci.glimpse.topo;
    exports com.metsci.glimpse.topo.io;

    requires transitive com.metsci.glimpse.core;

    requires transitive com.google.common;
    requires transitive java.logging;

    requires transitive jogamp.fat;

}

/**
 * General utility methods.
 */
open module com.metsci.glimpse.util
{
    requires jdk.unsupported;
    requires java.management;

    requires transitive java.logging;
    requires transitive it.unimi.dsi.fastutil;
    requires transitive com.google.common;
    requires transitive com.google.errorprone.annotations;
    requires transitive org.checkerframework.checker.qual;
    requires transitive j2objc.annotations;
    requires transitive listenablefuture;
    requires transitive failureaccess;
    requires transitive jsr305;

    exports com.metsci.glimpse.util;
    exports com.metsci.glimpse.util.buffer;
    exports com.metsci.glimpse.util.concurrent;
    exports com.metsci.glimpse.util.geo;
    exports com.metsci.glimpse.util.geo.datum;
    exports com.metsci.glimpse.util.geo.format;
    exports com.metsci.glimpse.util.geo.projection;
    exports com.metsci.glimpse.util.geo.util;
    exports com.metsci.glimpse.util.io;
    exports com.metsci.glimpse.util.jnlu;
    exports com.metsci.glimpse.util.logging;
    exports com.metsci.glimpse.util.logging.format;
    exports com.metsci.glimpse.util.math;
    exports com.metsci.glimpse.util.math.approx;
    exports com.metsci.glimpse.util.math.fast;
    exports com.metsci.glimpse.util.math.stat;
    exports com.metsci.glimpse.util.math.stochastic;
    exports com.metsci.glimpse.util.math.stochastic.pdfcont;
    exports com.metsci.glimpse.util.math.stochastic.pdfcont2d;
    exports com.metsci.glimpse.util.math.stochastic.pdfcont3d;
    exports com.metsci.glimpse.util.math.stochastic.pdfdiscrete;
    exports com.metsci.glimpse.util.primitives;
    exports com.metsci.glimpse.util.primitives.algorithms;
    exports com.metsci.glimpse.util.primitives.rangeset;
    exports com.metsci.glimpse.util.primitives.sorted;
    exports com.metsci.glimpse.util.quadtree;
    exports com.metsci.glimpse.util.quadtree.longvalued;
    exports com.metsci.glimpse.util.ugly;
    exports com.metsci.glimpse.util.units;
    exports com.metsci.glimpse.util.units.time;
    exports com.metsci.glimpse.util.units.time.format;
    exports com.metsci.glimpse.util.var;
    exports com.metsci.glimpse.util.var2;
    exports com.metsci.glimpse.util.vector;
}

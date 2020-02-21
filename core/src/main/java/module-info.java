/**
 * Core Glimpse data structures for plots, axes, layouts and painters.
 */
open module com.metsci.glimpse.core
{
    requires transitive com.metsci.glimpse.util;
    requires transitive com.metsci.glimpse.text;
    requires transitive com.metsci.glimpse.platformFixes;
    requires transitive miglayout.core;

    exports com.metsci.glimpse.core.axis;
    exports com.metsci.glimpse.core.axis.factory;
    exports com.metsci.glimpse.core.axis.listener;
    exports com.metsci.glimpse.core.axis.listener.mouse;
    exports com.metsci.glimpse.core.axis.listener.touch;
    exports com.metsci.glimpse.core.axis.painter;
    exports com.metsci.glimpse.core.axis.painter.label;
    exports com.metsci.glimpse.core.axis.painter.label.time;
    exports com.metsci.glimpse.core.axis.tagged;
    exports com.metsci.glimpse.core.axis.tagged.painter;
    exports com.metsci.glimpse.core.canvas;
    exports com.metsci.glimpse.core.context;
    exports com.metsci.glimpse.core.event.key;
    exports com.metsci.glimpse.core.event.key.newt;
    exports com.metsci.glimpse.core.event.mouse;
    exports com.metsci.glimpse.core.event.mouse.newt;
    exports com.metsci.glimpse.core.event.mouse.swing;
    exports com.metsci.glimpse.core.event.touch;
    exports com.metsci.glimpse.core.gl;
    exports com.metsci.glimpse.core.gl.shader;
    exports com.metsci.glimpse.core.gl.texture;
    exports com.metsci.glimpse.core.gl.util;
    exports com.metsci.glimpse.core.layout;
    exports com.metsci.glimpse.core.layout.matcher;
    exports com.metsci.glimpse.core.painter.base;
    exports com.metsci.glimpse.core.painter.decoration;
    exports com.metsci.glimpse.core.painter.geo;
    exports com.metsci.glimpse.core.painter.group;
    exports com.metsci.glimpse.core.painter.info;
    exports com.metsci.glimpse.core.painter.plot;
    exports com.metsci.glimpse.core.painter.shape;
    exports com.metsci.glimpse.core.painter.texture;
    exports com.metsci.glimpse.core.painter.track;
    exports com.metsci.glimpse.core.painter.treemap;
    exports com.metsci.glimpse.core.plot;
    exports com.metsci.glimpse.core.plot.stacked;
    exports com.metsci.glimpse.core.plot.timeline;
    exports com.metsci.glimpse.core.plot.timeline.animate;
    exports com.metsci.glimpse.core.plot.timeline.data;
    exports com.metsci.glimpse.core.plot.timeline.event;
    exports com.metsci.glimpse.core.plot.timeline.event.listener;
    exports com.metsci.glimpse.core.plot.timeline.event.paint;
    exports com.metsci.glimpse.core.plot.timeline.group;
    exports com.metsci.glimpse.core.plot.timeline.layout;
    exports com.metsci.glimpse.core.plot.timeline.listener;
    exports com.metsci.glimpse.core.plot.timeline.painter;
    exports com.metsci.glimpse.core.support;
    exports com.metsci.glimpse.core.support.atlas;
    exports com.metsci.glimpse.core.support.atlas.painter;
    exports com.metsci.glimpse.core.support.atlas.shader;
    exports com.metsci.glimpse.core.support.atlas.support;
    exports com.metsci.glimpse.core.support.color;
    exports com.metsci.glimpse.core.support.colormap;
    exports com.metsci.glimpse.core.support.font;
    exports com.metsci.glimpse.core.support.interval;
    exports com.metsci.glimpse.core.support.polygon;
    exports com.metsci.glimpse.core.support.popup;
    exports com.metsci.glimpse.core.support.projection;
    exports com.metsci.glimpse.core.support.selection;
    exports com.metsci.glimpse.core.support.settings;
    exports com.metsci.glimpse.core.support.shader.colormap;
    exports com.metsci.glimpse.core.support.shader.line;
    exports com.metsci.glimpse.core.support.shader.point;
    exports com.metsci.glimpse.core.support.shader.triangle;
    exports com.metsci.glimpse.core.support.swing;
    exports com.metsci.glimpse.core.support.texture;
    exports com.metsci.glimpse.core.support.texture.mutator;
    exports com.metsci.glimpse.core.support.wrapped;
}

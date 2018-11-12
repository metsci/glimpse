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

	requires com.metsci.glimpse.core;
	requires com.metsci.glimpse.text;
	requires com.metsci.glimpse.util;
	requires fastutil;
	requires guava;
	requires java.desktop;
	requires java.logging;
	requires java.base;
}
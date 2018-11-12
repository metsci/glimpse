/**
 * Lightweight support for reconfigurable, draggable Swing tabbed panes which integrate well with GlimpseCanvas.
 */
module com.metsci.glimpse.docking
{
	exports com.metsci.glimpse.docking.group.dialog;
	exports com.metsci.glimpse.docking.xml;
	exports com.metsci.glimpse.docking;
	exports com.metsci.glimpse.docking.group;
	exports com.metsci.glimpse.docking.group.frame;

	requires com.metsci.glimpse.util;
	requires guava;
	requires tinylaf;
	requires java.desktop;
	requires java.logging;
	requires java.base;
}

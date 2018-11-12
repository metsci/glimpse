/**
 * Support using Glimpse with SWT.
 */
module com.metsci.glimpse.swt
{
	exports com.metsci.glimpse.swt.misc;
	exports com.metsci.glimpse.swt.event.mouse;
	exports com.metsci.glimpse.swt.canvas;
	exports com.metsci.glimpse.swt.event;

	requires com.metsci.glimpse.core;
	requires com.metsci.glimpse.util;
	requires java.desktop;
	requires java.logging;
	requires java.base;
}

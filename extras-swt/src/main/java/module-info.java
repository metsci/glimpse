/**
 * Support using Glimpse with SWT.
 */
module com.metsci.glimpse.swt
{
	exports com.metsci.glimpse.swt.misc;
	exports com.metsci.glimpse.swt.event.mouse;
	exports com.metsci.glimpse.swt.canvas;
	exports com.metsci.glimpse.swt.event;

	requires transitive com.metsci.glimpse.core;
}

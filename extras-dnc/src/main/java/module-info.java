/**
 * Rendering of Digital Nautical Charts (DNC).
 */
module com.metsci.glimpse.dnc
{
	exports com.metsci.glimpse.dnc;
	exports com.metsci.glimpse.dnc.proj;
	exports com.metsci.glimpse.dnc.convert;
	exports com.metsci.glimpse.dnc.geosym;
	exports com.metsci.glimpse.dnc.facc;
	exports com.metsci.glimpse.dnc.util;

	requires com.metsci.glimpse.core;
	requires com.metsci.glimpse.util;
	requires svg.salamander;
	requires worldwind;
	requires fastutil;
	requires guava;
	requires java.desktop;
	requires java.logging;
	requires java.base;
	requires java.xml;
}

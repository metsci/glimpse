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

	requires transitive com.metsci.glimpse.core;
	
	requires transitive jogamp.fat;
	
	requires transitive com.google.common;
	requires transitive it.unimi.dsi.fastutil;

	requires transitive java.xml.bind;

	requires svg.salamander;
	requires worldwind;
}

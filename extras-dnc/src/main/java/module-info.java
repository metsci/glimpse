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
	requires svg.salamander;
	requires worldwind;
}

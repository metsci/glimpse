/**
 * Utility methods for working with Google Dataset Publishing Language (DSPL) in Java.
 */
module com.metsci.glimpse.dspl
{
	exports com.metsci.glimpse.dspl.lite;
	exports com.metsci.glimpse.dspl.util;
	exports com.metsci.glimpse.dspl.canonical;
	exports com.metsci.glimpse.dspl.parser;
	exports com.metsci.glimpse.dspl;
	exports com.metsci.glimpse.dspl.lite.schema;
	exports com.metsci.glimpse.dspl.schema;
	exports com.metsci.glimpse.dspl.parser.column;
	exports com.metsci.glimpse.dspl.parser.column.dynamic;
	exports com.metsci.glimpse.dspl.parser.table;
	exports com.metsci.glimpse.dspl.parser.util;

	requires com.metsci.glimpse.util;
	requires java.logging;
	requires java.base;
	requires java.xml;
	requires joda.time;
	requires fastutil;
}

/**
 * Native code fixes to improve Glimpse experience on Windows OS.
 */
module com.metsci.glimpse.platformFixes
{
    exports com.metsci.glimpse.platformFixes;

    opens platformFixes.windows32;
    opens platformFixes.windows64;
    
    requires transitive com.metsci.glimpse.util;

    requires transitive java.desktop;
}

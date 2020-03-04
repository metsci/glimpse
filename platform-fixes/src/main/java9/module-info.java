/**
 * Platform-specific tweaks to improve the Glimpse experience.
 */
open module com.metsci.glimpse.platformFixes
{
    requires transitive java.desktop;
    requires transitive com.metsci.glimpse.util;

    exports com.metsci.glimpse.platformFixes;
}

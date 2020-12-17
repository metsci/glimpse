/**
 * Support for using the Tiny Look and Feel (TinyLAF) with Glimpse and Glimpse Docking.
 */
open module com.metsci.glimpse.tinylaf
{
    requires java.logging;

    requires transitive java.desktop;
    requires transitive tinylaf;

    exports com.metsci.glimpse.tinylaf;
}

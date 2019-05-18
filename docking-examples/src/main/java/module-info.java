module com.metsci.glimpse.docking.examples
{
    exports com.metsci.glimpse.docking.examples;

    opens com.metsci.glimpse.docking.examples.docking;
    opens com.metsci.glimpse.docking.examples.icons;

    requires transitive com.metsci.glimpse.docking;
    requires transitive com.metsci.glimpse.tinylaf;
    requires transitive com.metsci.glimpse.core.examples;
}

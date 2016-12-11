package com.metsci.glimpse.layers;

public interface LayeredExtension
{

    boolean allowsParent( LayeredExtension parent );

    /**
     * If the specified parent is null, then this method removes all ties
     * to its existing parent (if it has one).
     */
    void setParent( LayeredExtension parent );

    LayeredExtension getParent( );

    LayeredExtension createClone( );

}

package com.metsci.glimpse.layers;

public interface LayeredViewConfig
{

    boolean allowsParent( LayeredViewConfig parent );

    /**
     * If the specified parent is null, then this method removes all ties
     * to its existing parent (if it has one).
     */
    void setParent( LayeredViewConfig parent );

    LayeredViewConfig getParent( );

}

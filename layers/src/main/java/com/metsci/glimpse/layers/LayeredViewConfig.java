package com.metsci.glimpse.layers;

public interface LayeredViewConfig
{

    boolean allowsParent( LayeredViewConfig parent );

    void setParent( LayeredViewConfig parent );

}

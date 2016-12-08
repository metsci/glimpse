package com.metsci.glimpse.layers;

public interface LayerRepr
{

    boolean isVisible( );

    void setVisible( boolean isReprVisible );

    void dispose( boolean isReinstall );

}

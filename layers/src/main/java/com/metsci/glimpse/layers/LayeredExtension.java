package com.metsci.glimpse.layers;

import com.metsci.glimpse.util.var.Var;

public interface LayeredExtension
{

    Var<LayeredExtension> parent( );

    LayeredExtension createClone( );

}

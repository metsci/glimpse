package com.metsci.glimpse.layers;

import com.metsci.glimpse.util.var.Var;

public interface Trait
{

    Var<Trait> parent( );

    Trait createClone( );

}

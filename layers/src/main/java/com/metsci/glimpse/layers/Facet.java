package com.metsci.glimpse.layers;

import static com.metsci.glimpse.util.PredicateUtils.notNull;

import com.metsci.glimpse.util.var.Var;

public abstract class Facet
{

    public final Var<Boolean> isVisible;


    public Facet( )
    {
        this.isVisible = new Var<>( true, notNull );
    }

    public abstract void dispose( boolean isReinstall );

}

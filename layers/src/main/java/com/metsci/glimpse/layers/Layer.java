package com.metsci.glimpse.layers;

import static com.metsci.glimpse.util.PredicateUtils.notNull;

import java.util.Map;

import com.metsci.glimpse.util.var.ReadableVar;
import com.metsci.glimpse.util.var.Var;

public abstract class Layer
{

    public final Var<String> title;
    public final Var<Boolean> isVisible;


    public Layer( )
    {
        this.title = new Var<>( "Untitled Layer", notNull );
        this.isVisible = new Var<>( true, notNull );
    }

    public abstract ReadableVar<? extends Map<? extends LayeredView,? extends Facet>> facets( );

    public abstract void installTo( LayeredView view );

    public abstract void uninstallFrom( LayeredView view, boolean isReinstall );

}

package com.metsci.glimpse.layers;

import static com.metsci.glimpse.util.PredicateUtils.notNull;

import java.util.Map;

import com.metsci.glimpse.util.var.ReadableVar;
import com.metsci.glimpse.util.var.Var;

/**
 * A {@link Layer} is set of related renderers and input listeners. A layer that is added to
 * a {@link LayeredGui} is given the opportunity to add a representation of itself to each
 * {@link View}.
 * <p>
 * A layer typically has a different representation in each view -- for example, a TracksLayer
 * can show a spatial representation in a geo view, and a temporal representation in a timeline
 * view. The representation of a particular layer on a particular view is called a {@link Facet}.
 */
public abstract class Layer
{

    public final Var<String> title;
    public final Var<Boolean> isVisible;


    public Layer( )
    {
        this.title = new Var<>( "Untitled Layer", notNull );
        this.isVisible = new Var<>( true, notNull );
    }

    public abstract ReadableVar<? extends Map<? extends View,? extends Facet>> facets( );

    public abstract void installTo( View view );

    /**
     * The {@code isReinstall} arg indicates whether the layer is going to install a new facet
     * to replace this one, more or less immediately after removing this one. Some implementations
     * of this method may use this flag to decide whether to leave certain UI elements in place
     * for the replacement facet to reuse. This can reduce avoid effects due to UI elements being
     * removed and then immediately re-added.
     */
    public abstract void uninstallFrom( View view, boolean isReinstall );

}
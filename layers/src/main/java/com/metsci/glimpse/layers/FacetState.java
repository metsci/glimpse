package com.metsci.glimpse.layers;

/**
 * Represents the state of a {@link Facet} in a form that can be stored (e.g. while
 * the {@link Facet} gets disposed and a replacement {@link Facet} gets created), and
 * re-applied later to a new {@link Facet}.
 * <p>
 * TODO: Make this class XML-serializable
 * TODO: Can we require subclasses to be XML-serializable?
 * <p>
 * @see Facet#state()
 * @see Facet#applyState(FacetState)
 */
public class FacetState
{

    public final boolean isVisible;


    public FacetState( boolean isVisible )
    {
        this.isVisible = isVisible;
    }

}

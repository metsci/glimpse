package com.metsci.glimpse.layers;

import com.metsci.glimpse.layers.geo.GeoTrait;
import com.metsci.glimpse.layers.time.TimeTrait;
import com.metsci.glimpse.util.var.Var;

/**
 * A {@link Trait} is a fragment of state attached to a {@link View}. For example, a view
 * typically displays a bunch of different data from the same moment in time -- so to attach
 * a selected time to a view, a {@link TimeTrait} can be used. All the {@link Facet}s on
 * the view can query the time trait, and then do their rendering based on the selected time.
 * Since the facets are all on the same view, they all see the same time trait, and they all
 * render data from the same time.
 * <p>
 * The layers module includes classes for some commonly used traits, such as {@link GeoTrait}
 * and {@link TimeTrait}. However, there is no magic to the included traits; custom traits
 * can be defined as well, the same way the included ones are defined. If you want to define
 * your own custom trait, the included traits make good examples to follow.
 * <p>
 * Trait instances can be linked to each other. For example, two timeline views can have their
 * time traits linked together, so that adjusting the selection window in one timeline affects
 * the selection window in the other timeline as well. For another example, a geo view can have
 * its time trait linked to the time trait of a timeline view, so that a facet on the geo can
 * highlight points inside the time window that is selected on the timeline.
 * <p>
 * To be linked, two trait instances must be compatible with each other. Compatibility is checked
 * using the {@link Var#validateFn} of the {@link Trait#parent} var.
 * <p>
 * For flexibility, traits that are owned by views are not linked directly to each other, but
 * are instead linked to a common "linkage." A linkage is simply a trait object that is used
 * as a shared parent for some number of child traits. A regular (non-linkage) trait may have
 * a parent, but the parent of a linkage is always null.
 * <p>
 * See {@link LayeredGui} for more detail on how it uses traits and linkages.
 */
public abstract class Trait
{

    /**
     * Whether the trait is a linkage or a regular trait. Regular traits can be parented by a
     * linkage -- e.g. to link axes across several views. However, the parent of a linkage is
     * always null.
     */
    public final boolean isLinkage;

    /**
     * Can be modified to set this trait's parent, and listened to for notifications of changes
     * in parentage. The var's value may be set to null, indicating that this trait has no parent.
     */
    public final Var<Trait> parent;


    protected Trait( boolean isLinkage )
    {
        this.isLinkage = isLinkage;

        this.parent = new Var<>( null, ( candidate ) ->
        {
            if ( candidate == null )
            {
                return true;
            }
            else if ( isLinkage )
            {
                // A linkage cannot have a non-null parent
                return false;
            }
            else if ( !candidate.isLinkage )
            {
                // Only a linkage can be a parent
                return false;
            }
            else
            {
                return this.isValidParent( candidate );
            }
        } );
    }

    /**
     * Called internally by {@link Trait#parent}'s {@link Var#validateFn} to check compatibility.
     * Typically a trait should only accept a parent of its own subclass.
     */
    protected abstract boolean isValidParent( Trait parent );

    /**
     * Returns a copy of this trait (linkage or regular, depending on the {@code isLinkage} arg).
     * <p>
     * <em>Does not copy parentage.</em>
     */
    public abstract Trait copy( boolean isLinkage );

}

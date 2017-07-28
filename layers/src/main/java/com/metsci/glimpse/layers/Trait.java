/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.layers;

import com.metsci.glimpse.layers.geo.GeoTrait;
import com.metsci.glimpse.layers.time.TimeTrait;
import com.metsci.glimpse.util.var.Var;

/**
 * A {@link Trait} is a fragment of state that can be attached to a {@link View}. For example,
 * consider the case where a view displays a bunch of different data from the same moment in time.
 * A trait that contains a selected time can be attached to the view. Then all the {@link Facet}s
 * on the view can query the selected time from the trait, and do their rendering based on it.
 * Since the facets are all on the same view, they all query the same time trait, and they all
 * render data from the same time.
 * <p>
 * The name "trait" is intended in the general English-language sense, rather than the technical
 * sense used in some programming languages. In those terms, this class is more like a mixin,
 * because it includes state (rather than just method implementations).
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
 * The layers module includes classes for some commonly used traits, such as {@link GeoTrait}
 * and {@link TimeTrait}. However, there is no magic to the included traits; custom traits
 * can be defined as well, the same way the included ones are defined. If you want to define
 * your own custom trait, the included traits make good examples to follow.
 * <p>
 * A "linkage" is a trait object that is used as a shared parent for some number of child traits.
 * A regular (non-linkage) trait may have a parent, but the parent of a linkage is always null.
 * See {@link LayeredGui} for more detail on how it uses linkages and regular traits.
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

    /**
     * The {@code isLinkage} arg specifies whether the created instance will be a linkage or a
     * regular trait. A linkage is a trait object used as a shared parent for some number of
     * child traits. A regular trait may have a parent, but the parent of a linkage is always
     * null.
     */
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

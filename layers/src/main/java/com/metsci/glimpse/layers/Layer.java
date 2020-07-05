/*
 * Copyright (c) 2019 Metron, Inc.
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

    /**
     * An implementation of this method may (but is not required to) add a representation of
     * this Layer to the specified View. If such a representation is added to the View, a Facet
     * will be added to the map that is returned by this Layer's {@link #facets()} method.
     * <p>
     * If a call to this method does not add a Facet the View, then the method call will not
     * have changed the state of either the View or the Layer.
     * <p>
     * If a call to this method throws an exception, it will not add a Facet to the View, and
     * will not change the state of either the View or the Layer.
     */
    public abstract void installTo( View view ) throws Exception;

    /**
     * The {@code isReinstall} arg indicates whether the layer is going to install a new facet
     * to replace this one, more or less immediately after removing this one. Some implementations
     * of this method may use this flag to decide whether to leave certain UI elements in place
     * for the replacement facet to reuse. This can reduce avoid effects due to UI elements being
     * removed and then immediately re-added.
     */
    public abstract void uninstallFrom( View view, boolean isReinstall );

}

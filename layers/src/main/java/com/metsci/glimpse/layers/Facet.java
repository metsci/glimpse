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

import com.metsci.glimpse.core.canvas.GlimpseCanvas;
import com.metsci.glimpse.core.painter.base.GlimpsePainter;
import com.metsci.glimpse.layers.geo.GeoTrait;
import com.metsci.glimpse.layers.time.TimeTrait;
import com.metsci.glimpse.util.var.Var;

/**
 * A {@link Facet} is the representation of a {@link Layer} on a particular {@link View}. In
 * many cases, a facet will consist of a {@link GlimpsePainter} and some axis listeners.
 * <p>
 * A layer is responsible for creating its facets: one facet for each view on which the layer
 * wants to display itself. Typically the facet impl's constructor will take the view as an
 * argument, and call {@link View#requireTrait(String, Class)} (or a variation thereof -- see
 * next paragraph) to retrieve the traits that the facet needs.
 * <p>
 * Rather than calling {@link View#requireTrait(String, Class)} directly, it is usually better
 * to call a convenience function that has the appropriate trait key and class built in, such
 * as {@link GeoTrait#requireGeoTrait(View)}.
 * <p>
 * For example, a facet might call {@link TimeTrait#requireTimeTrait(View)}, add a listener
 * to the {@link TimeTrait#axis}, and in the axis listener do something along the lines of:
 * <pre>
 * {@code painter.setTime( selectedTime )}
 * </pre>
 */
public abstract class Facet
{

    /**
     * Whether this facet should be visible when its layer is visible.
     * <p>
     * When a layer is not visible, none of its facets should be visible. However, when the
     * layer is visible, each of its facets can be toggled independently. This allows a layer
     * to be shown in some views but not others.
     * <p>
     * Typically a facet should have code along the lines of:
     * <pre>
     * {@code painter.setVisible( layer.isVisible && facet.isVisible )}
     * </pre>
     */
    public final Var<Boolean> isVisible;

    /**
     * Typically the constructor of a subclass should take an arg of the relevant {@link Layer}
     * subclass, and another arg of the relevant {@link View} subclass. The facet will need to
     * check the layer's visibility, and will need to interact quite a bit with the view.
     */
    protected Facet( )
    {
        this.isVisible = new Var<>( true, notNull );
    }

    /**
     * Captures and returns the current state of this {@link Facet}, in a form that can be stored
     * (e.g. while the {@link Facet} gets disposed and a replacement {@link Facet} gets created),
     * and then possibly re-applied later to a new {@link Facet}.
     */
    public FacetState state( )
    {
        return new FacetState( this.isVisible.v( ) );
    }

    /**
     * Sets this {@link Facet}'s internal state to match a previously captured state.
     * <p>
     * The {@code state} arg will typically have been created by this {@link Facet}'s own
     * {@link Facet#state()} method, but this is NOT guaranteed -- if an overriding implementation
     * of this method expects {@code state} to satisfy some condition (e.g. be of a particular {@link FacetState}
     * subclass), it MUST CHECK. In particular, {@code state} could conceivably have been deserialized
     * from long-term storage, and may therefore have been created by an older version of the code.
     * <p>
     * The {@code state} arg will never be null -- if there is no state to apply, this method will
     * not be called.
     */
    public void applyState( FacetState state )
    {
        this.isVisible.set( state.isVisible );
    }

    /**
     * The {@code isReinstall} arg indicates whether the layer is going to install a new facet
     * to replace this one, more or less immediately after removing this one. Some implementations
     * of this method may use this flag to decide whether to leave certain UI elements in place
     * for the replacement facet to reuse. This can reduce avoid effects due to UI elements being
     * removed and then immediately re-added.
     * <p>
     * To dispose of GL resources, use the view's {@link GlimpseCanvas} to do an async GL invoke.
     * For example:
     * <pre><code> view.canvas.getGLDrawable( ).invoke( false, ( glDrawable ) ->
     * {
     *     GL gl = glDrawable.getGL( );
     *     // Dispose of GL resources
     *     return false;
     * } );</code></pre>
     * To call {@link GlimpsePainter#dispose(com.metsci.glimpse.context.GlimpseContext)}, do:
     * <pre><code> view.canvas.getGLDrawable( ).invoke( false, ( glDrawable ) ->
     * {
     *     GlimpseContext context = view.canvas.getGlimpseContext( );
     *     painter.dispose( context );
     *     return false;
     * } );</code></pre>
     */
    public abstract void dispose( boolean isReinstall );

}

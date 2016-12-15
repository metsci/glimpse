package com.metsci.glimpse.layers;

import static com.metsci.glimpse.util.PredicateUtils.notNull;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.layers.geo.GeoTrait;
import com.metsci.glimpse.layers.time.TimeTrait;
import com.metsci.glimpse.painter.base.GlimpsePainter;
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

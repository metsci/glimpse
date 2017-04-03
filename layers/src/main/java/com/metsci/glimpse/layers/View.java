package com.metsci.glimpse.layers;

import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.mapMinus;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.mapWith;
import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static java.util.Collections.singletonMap;

import java.awt.Component;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.media.opengl.GLAnimatorControl;
import javax.swing.Icon;
import javax.swing.JToolBar;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metsci.glimpse.layers.geo.GeoTrait;
import com.metsci.glimpse.layers.misc.UiUtils;
import com.metsci.glimpse.util.var.ReadableVar;
import com.metsci.glimpse.util.var.Var;

public abstract class View
{
    private static final Logger logger = getLogger( View.class );


    public final ReadableVar<ImmutableMap<String,Trait>> traits;
    public final ReadableVar<ImmutableMap<Layer,Facet>> facets;
    public final Var<String> title;
    public final JToolBar toolbar;
    public final ImmutableSet<ViewOption> viewOptions;

    protected final Var<ImmutableMap<String,Trait>> _traits;
    protected final Var<ImmutableMap<Layer,Facet>> _facets;


    public View( Collection<? extends ViewOption> viewOptions )
    {
        this._traits = new Var<>( ImmutableMap.of( ), notNull );
        this._facets = new Var<>( ImmutableMap.of( ), notNull );

        this.traits = this._traits;
        this.facets = this._facets;
        this.title = new Var<>( "Untitled View", notNull );
        this.toolbar = newToolbar( true );
        this.viewOptions = ImmutableSet.copyOf( viewOptions );
    }

    /**
     * Called by LayeredGui to supply this View with a shared animator.
     * <p>
     * glAnimator may not have been started yet, since LayeredGui doesn't know whether any
     * views need it. The simplest approach is for every view that uses the animator to call
     * {@link UiUtils#ensureAnimating(GLAnimatorControl)}.
     */
    public void setGLAnimator( GLAnimatorControl glAnimator )
    {
        // Do nothing by default
    }

    public abstract Component getComponent( );

    public String getTooltip( )
    {
        return null;
    }

    public Icon getIcon( )
    {
        return null;
    }

    public <T extends Trait> T requireTrait( String traitKey, Class<T> traitClass )
    {
        Trait trait = this.traits.v( ).get( traitKey );

        if ( trait == null )
        {
            throw new TraitMissingException( traitKey, traitClass );
        }

        if ( !traitClass.isInstance( trait ) )
        {
            throw new TraitClassMismatchException( traitKey, traitClass, trait );
        }

        return traitClass.cast( trait );
    }

    public void setTrait( String traitKey, Trait newTrait )
    {
        this.setTraits( singletonMap( traitKey, newTrait ) );
    }

    public void setTraits( Map<? extends String,? extends Trait> newTraits )
    {
        Set<Layer> layers = ImmutableSet.copyOf( this._facets.v( ).keySet( ) );

        // Uninstall layers
        for ( Layer layer : layers )
        {
            this.removeLayer( layer, true );
        }

        // Update traits map
        Map<String,Trait> oldTraits = this._traits.v( );
        this._traits.update( ( v ) -> mapWith( v, newTraits ) );

        // Migrate parentage where appropriate
        for ( Entry<? extends String,? extends Trait> en : newTraits.entrySet( ) )
        {
            String traitKey = en.getKey( );
            Trait newTrait = en.getValue( );
            Trait oldTrait = oldTraits.get( traitKey );

            // Link newTrait to oldParent (if they are compatible), unless newTrait already has a parent
            if ( oldTrait != null && newTrait.parent.v( ) == null )
            {
                Trait oldParent = oldTrait.parent.v( );

                // Unlink oldTrait from oldParent
                oldTrait.parent.set( null );

                // Link newTrait to oldParent, if possible
                if ( newTrait.parent.isValid( oldParent ) )
                {
                    newTrait.parent.set( oldParent );
                }
            }
        }

        // Re-init the view
        this.init( );

        // Re-install layers
        for ( Layer layer : layers )
        {
            this.addLayer( layer );
        }
    }

    protected void init( )
    {
        this.doInit( );
    }

    /**
     * Called after one or more traits have been added or replaced. This gives the view a
     * chance to initialize its UI components based on trait values -- for example, setting
     * the timeline epoch.
     * <p>
     * To query a trait, implementations of this method should use {@link View#requireTrait(String, Class)},
     * or a variation thereof. Rather than calling {@link View#requireTrait(String, Class)}
     * directly, it is usually better to call a convenience function that has the appropriate
     * trait key and class built in, such as {@link GeoTrait#requireGeoTrait(View)}.
     * <p>
     * The view is guaranteed to have no layers installed when this method is called. If traits
     * are changed after layers have already been installed, the following steps will be taken
     * automatically:
     * <ol>
     * <li>Uninstall its layers
     * <li>Set the trait (or traits) being changed
     * <li>Call this method
     * <li>Re-install the original layers
     * </ol>
     */
    protected abstract void doInit( );

    /**
     * Make a new instance that, once traits and layers are added, will be identical to this
     * view.
     * <p>
     * <em>Does not copy traits or layers</em> -- that is the responsibility of the caller.
     */
    public abstract View copy( );

    /**
     * This method is protected to discourage access from client code, while still allowing
     * access from {@link LayeredGui}. Client code should use {@link LayeredGui#addLayer(Layer)}
     * instead.
     * <p>
     * If {@link Layer#installTo(View)} throws an exception, this method will catch and log
     * the exception, and then carry on. Assuming the Layer satisfies the general contract of
     * {@link Layer#installTo(View)}, subsequent behavior will be the same as if the Layer
     * had simply chosen not to install a Facet to this View.
     */
    protected void addLayer( Layer layer )
    {
        if ( !this._facets.v( ).containsKey( layer ) )
        {
            try
            {
                layer.installTo( this );
            }
            catch ( Exception e )
            {
                // XXX: Allow client code to pass in a custom exception handler (e.g. for showing a dialog box)
                logWarning( logger, "Failed to install a " + layer.getClass( ).getName( ) + " to a " + this.getClass( ).getName( ), e );
            }

            Facet facet = layer.facets( ).v( ).get( this );
            if ( facet != null )
            {
                this._facets.update( ( v ) -> mapWith( v, layer, facet ) );
            }
        }
    }

    /**
     * This method is protected to discourage access from client code, while still allowing
     * access from {@link LayeredGui}. Client code should use {@link LayeredGui#removeLayer(Layer)}
     * instead.
     */
    protected void removeLayer( Layer layer, boolean isReinstall )
    {
        if ( this._facets.v( ).containsKey( layer ) )
        {
            this._facets.update( ( v ) -> mapMinus( v, layer ) );
            layer.uninstallFrom( this, isReinstall );
        }
    }

    /**
     * This method is protected to discourage access from client code, while still allowing
     * access from {@link LayeredGui}.
     */
    protected void dispose( )
    {
        for ( Layer layer : this._facets.v( ).keySet( ) )
        {
            this.removeLayer( layer, false );
        }

        for ( Trait trait : this._traits.v( ).values( ) )
        {
            trait.parent.set( null );
        }
        this._traits.set( ImmutableMap.of( ) );
    }

}

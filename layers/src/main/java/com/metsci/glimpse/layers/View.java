package com.metsci.glimpse.layers;

import static com.metsci.glimpse.util.ImmutableCollectionUtils.mapWith;
import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.Icon;

import com.google.common.collect.ImmutableMap;
import com.metsci.glimpse.util.var.ReadableVar;
import com.metsci.glimpse.util.var.Var;

public abstract class View
{

    public final Var<String> title;
    public final ReadableVar<ImmutableMap<String,Trait>> traits;

    protected final Var<ImmutableMap<String,Trait>> _traits;
    protected final List<Layer> layers;


    public View( )
    {
        this.title = new Var<>( "Untitled View", notNull );
        this._traits = new Var<>( ImmutableMap.of( ), notNull );
        this.traits = this._traits;
        this.layers = new ArrayList<>( );
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

    public Collection<Component> getToolbarComponents( )
    {
        return emptyList( );
    }

    public GLAutoDrawable getGLDrawable( )
    {
        return null;
    }

    public <T extends Trait> T requireTrait( String traitKey, Class<T> traitClass )
    {
        Trait trait = this.traits.v( ).get( traitKey );

        if ( trait == null )
        {
            throw new RuntimeException( "Required trait is missing: key = " + traitKey + ", required-class = " + traitClass.getName( ) );
        }

        if ( !traitClass.isInstance( trait ) )
        {
            throw new RuntimeException( "Trait type mismatch: key = " + traitKey + ", required-class = " + traitClass.getName( ) + ", actual-class = " + trait.getClass( ).getName( ) );
        }

        return traitClass.cast( trait );
    }

    public void setTrait( String traitKey, Trait newTrait )
    {
        this.setTraits( singletonMap( traitKey, newTrait ) );
    }

    public void setTraits( Map<? extends String,? extends Trait> newTraits )
    {
        // Uninstall layers
        for ( Layer layer : this.layers )
        {
            layer.uninstallFrom( this, true );
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
            if ( oldTrait != null && newTrait.parent( ).v( ) == null )
            {
                Trait oldParent = oldTrait.parent( ).v( );

                // Unlink oldTrait from oldParent
                oldTrait.parent( ).set( null );

                // Link newTrait to oldParent, if possible
                if ( newTrait.parent( ).validateFn.test( oldParent ) )
                {
                    newTrait.parent( ).set( oldParent );
                }
            }
        }

        // Re-init the view
        this.init( );

        // Re-install layers
        for ( Layer layer : this.layers )
        {
            layer.installTo( this );
        }
    }

    protected abstract void init( );

    public abstract View createClone( );

    /**
     * This method is protected to discourage access from client code, while still allowing
     * access from {@link LayeredGui}. Client code should use {@link LayeredGui#addLayer(Layer)}
     * instead.
     */
    protected void addLayer( Layer layer )
    {
        if ( !this.layers.contains( layer ) )
        {
            this.layers.add( layer );
            layer.installTo( this );
        }
    }

    /**
     * This method is protected to discourage access from client code, while still allowing
     * access from {@link LayeredGui}. Client code should use {@link LayeredGui#removeLayer(Layer)}
     * instead.
     */
    protected void removeLayer( Layer layer )
    {
        if ( !this.layers.contains( layer ) )
        {
            this.layers.remove( layer );
            layer.uninstallFrom( this, false );
        }
    }

    /**
     * This method is protected to discourage access from client code, while still allowing
     * access from {@link LayeredGui}.
     */
    protected void dispose( )
    {
        for ( Layer layer : this.layers )
        {
            layer.uninstallFrom( this, false );
        }
        this.layers.clear( );

        for ( Trait trait : this._traits.v( ).values( ) )
        {
            trait.parent( ).set( null );
        }
        this._traits.set( ImmutableMap.of( ) );
    }

}

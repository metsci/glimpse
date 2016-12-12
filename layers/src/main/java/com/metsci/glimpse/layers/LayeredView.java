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

public abstract class LayeredView
{

    public final Var<String> title;
    public final ReadableVar<ImmutableMap<String,LayeredExtension>> extensions;

    protected final Var<ImmutableMap<String,LayeredExtension>> _extensions;
    protected final List<Layer> layers;


    public LayeredView( )
    {
        this.title = new Var<>( "Untitled View", notNull );
        this._extensions = new Var<>( ImmutableMap.of( ), notNull );
        this.extensions = this._extensions;
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

    public <T extends LayeredExtension> T requireExtension( String extensionKey, Class<T> extensionClass )
    {
        LayeredExtension extension = this.extensions.v( ).get( extensionKey );

        if ( extension == null )
        {
            throw new RuntimeException( "Required extension is missing: key = " + extensionKey + ", required-class = " + extensionClass.getName( ) );
        }

        if ( !extensionClass.isInstance( extension ) )
        {
            throw new RuntimeException( "Extension type mismatch: key = " + extensionKey + ", required-class = " + extensionClass.getName( ) + ", actual-class = " + extension.getClass( ).getName( ) );
        }

        return extensionClass.cast( extension );
    }

    public void setExtension( String extensionKey, LayeredExtension newExtension )
    {
        this.setExtensions( singletonMap( extensionKey, newExtension ) );
    }

    public void setExtensions( Map<? extends String,? extends LayeredExtension> newExtensions )
    {
        // Uninstall layers
        for ( Layer layer : this.layers )
        {
            layer.uninstallFrom( this, true );
        }

        // Update extensions map
        Map<String,LayeredExtension> oldExtensions = this._extensions.v( );
        this._extensions.update( ( v ) -> mapWith( v, newExtensions ) );

        // Migrate parentage where possible
        for ( Entry<? extends String,? extends LayeredExtension> en : newExtensions.entrySet( ) )
        {
            String extensionKey = en.getKey( );

            // Unlink newExtension, to be sure we have a clean slate
            LayeredExtension newExtension = en.getValue( );
            newExtension.parent( ).set( null );

            // Migrate oldExtension's parentage to newExtension, if possible
            LayeredExtension oldExtension = oldExtensions.get( extensionKey );
            if ( oldExtension != null )
            {
                LayeredExtension oldParent = oldExtension.parent( ).v( );

                // Unlink oldExtension from oldParent
                oldExtension.parent( ).set( null );

                // Link newExtension to oldParent, if possible
                if ( newExtension.parent( ).validateFn.test( oldParent ) )
                {
                    newExtension.parent( ).set( oldParent );
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

    public abstract LayeredView createClone( );

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

        for ( LayeredExtension extension : this._extensions.v( ).values( ) )
        {
            extension.parent( ).set( null );
        }
        this._extensions.set( ImmutableMap.of( ) );
    }

}

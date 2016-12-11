package com.metsci.glimpse.layers;

import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.Icon;

import com.metsci.glimpse.util.var.Var;

public abstract class LayeredView
{

    public final Var<String> title;

    // WIP: This may need to be a Var to support a "Linkages" panel
    protected final Map<String,LayeredExtension> extensions;

    protected final List<Layer> layers;


    public LayeredView( )
    {
        this.title = new Var<>( "Untitled View", notNull );
        this.extensions = new LinkedHashMap<>( );
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
        LayeredExtension extension = this.extensions.get( extensionKey );

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

        // Update extensions, preserving parentage where possible
        Map<String,LayeredExtension> oldExtensionParents = new HashMap<>( );
        for ( String extensionKey : newExtensions.keySet( ) )
        {
            if ( this.extensions.containsKey( extensionKey ) )
            {
                LayeredExtension oldExtension = this.extensions.get( extensionKey );
                LayeredExtension oldParent = oldExtension.getParent( );
                oldExtensionParents.put( extensionKey, oldParent );
                oldExtension.setParent( null );
            }
        }

        this.extensions.putAll( newExtensions );

        for ( String extensionKey : newExtensions.keySet( ) )
        {
            LayeredExtension newExtension = this.extensions.get( extensionKey );
            LayeredExtension oldParent = oldExtensionParents.get( extensionKey );
            if ( newExtension.allowsParent( oldParent ) )
            {
                newExtension.setParent( oldParent );
            }
            else
            {
                newExtension.setParent( null );
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

        for ( LayeredExtension extension : this.extensions.values( ) )
        {
            extension.setParent( null );
        }
        this.extensions.clear( );
    }

}

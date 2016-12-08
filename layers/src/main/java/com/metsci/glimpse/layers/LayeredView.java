package com.metsci.glimpse.layers;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.Icon;

public abstract class LayeredView
{

    protected final Map<String,LayeredViewConfig> configs;
    protected final List<Layer> layers;


    public LayeredView( )
    {
        this.configs = new LinkedHashMap<>( );
        this.layers = new ArrayList<>( );
    }

    public abstract String getTitle( );

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

    public <T extends LayeredViewConfig> T requireConfig( String configKey, Class<T> configClass )
    {
        LayeredViewConfig config = this.configs.get( configKey );

        if ( config == null )
        {
            throw new RuntimeException( "Required view-config is missing: key = " + configKey + ", required-class = " + configClass.getName( ) );
        }

        if ( !configClass.isInstance( config ) )
        {
            throw new RuntimeException( "Required view-config has wrong type: key = " + configKey + ", required-class = " + configClass.getName( ) + ", actual-class = " + config.getClass( ).getName( ) );
        }

        return configClass.cast( config );
    }

    public void setConfig( String configKey, LayeredViewConfig newConfig )
    {
        this.setConfigs( singletonMap( configKey, newConfig ) );
    }

    public void setConfigs( Map<? extends String,? extends LayeredViewConfig> newConfigs )
    {
        for ( Layer layer : this.layers )
        {
            layer.uninstallFrom( this, true );
        }

        this.configs.putAll( newConfigs );
        this.init( );

        for ( Layer layer : this.layers )
        {
            layer.installTo( this );
        }
    }

    protected abstract void init( );

    protected void addLayer( Layer layer )
    {
        if ( !this.layers.contains( layer ) )
        {
            this.layers.add( layer );
            layer.installTo( this );
        }
    }

    protected void removeLayer( Layer layer )
    {
        if ( !this.layers.contains( layer ) )
        {
            this.layers.remove( layer );
            layer.uninstallFrom( this, false );
        }
    }

}

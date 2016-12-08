package com.metsci.glimpse.layers;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class LayeredView
{

    protected final Map<String,LayeredViewConfig> configs;


    public LayeredView( )
    {
        this.configs = new LinkedHashMap<>( );
    }

    public void setConfig( String configKey, LayeredViewConfig config )
    {
        this.configs.put( configKey, config );
    }

    public <T extends LayeredViewConfig> T requireConfig( String configKey, Class<T> configClass )
    {
        LayeredViewConfig config = this.configs.get( configKey );
        return configClass.cast( config );
    }

    public abstract void init( );

}

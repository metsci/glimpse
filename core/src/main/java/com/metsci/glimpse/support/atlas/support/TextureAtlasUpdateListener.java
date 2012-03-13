package com.metsci.glimpse.support.atlas.support;

/**
 * Interface for notification of TextureAtlas events. Generally used by data structures
 * or GlimpsePainters employing the TextureAtlas as a backing store.
 * 
 * @author ulman
 */
public interface TextureAtlasUpdateListener
{
    /**
     * Indicates the texture atlas was reorganized and all texture
     * coordinates previously provided by the texture atlas are now invalid.
     */
    public void reorganized( );
}

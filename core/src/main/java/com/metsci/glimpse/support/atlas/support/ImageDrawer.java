package com.metsci.glimpse.support.atlas.support;

import java.awt.Graphics2D;

/**
 * Interface for specifying how to fill a TextureAtlas entry using Graphics2D.
 * 
 * @author ulman
 */
public interface ImageDrawer
{
    public void drawImage( Graphics2D g, int width, int height );
}

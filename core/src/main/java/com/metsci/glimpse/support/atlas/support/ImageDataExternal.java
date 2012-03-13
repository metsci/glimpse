package com.metsci.glimpse.support.atlas.support;

/**
 * Metadata for images waiting in the queue to be added to the TextureAtlas.
 * 
 * @author ulman
 * @see ImageDataInternal
 */
public class ImageDataExternal
{
    // unique identifier for the image data
    private Object id;
    
    // the offset from the edge of the packed rectangle to the "center" of the image
    // (which might not be the middle of the image)
    private int centerX;
    private int centerY;
    
    // width and height of the image
    private int width;
    private int height;
    
    // the Graphics2D routine used to draw the image
    private ImageDrawer drawer;

    public ImageDataExternal( Object id, int centerX, int centerY, int width, int height, ImageDrawer drawer )
    {
        this.id = id;
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.drawer = drawer;
    }
    
    public int getCenterY( )
    {
        return centerY;
    }

    public int getCenterX( )
    {
        return centerX;
    }

    public int getWidth( )
    {
        return width;
    }

    public int getHeight( )
    {
        return height;
    }

    public Object getId( )
    {
        return id;
    }
    
    public ImageDrawer getImageDrawer( )
    {
        return drawer;
    }
}

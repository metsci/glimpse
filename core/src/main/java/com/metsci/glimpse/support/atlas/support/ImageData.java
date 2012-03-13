package com.metsci.glimpse.support.atlas.support;

import com.sun.opengl.util.texture.TextureCoords;

/**
 * An immutable wrapper around {@link ImageDataInternal} used to pass data about
 * images stored in the TextureAtlas to the user.
 * 
 * @author ulman
 */
public class ImageData
{
    private ImageDataInternal delegate;
    
    public ImageData( ImageDataInternal delegate )
    {
        this.delegate = delegate;
    }

    public final Object getId( )
    {
        return delegate.getId( );
    }
    
    public final int getBufferX( )
    {
        return delegate.getBufferX( );
    }

    public final int getBufferY( )
    {
        return delegate.getBufferY( );
    }
    
    public final int getLocationX( )
    {
        return delegate.getLocationX( );
    }

    public final int getLocationY( )
    {
        return delegate.getLocationY( );
    }

    public final int getCenterX( )
    {
        return delegate.getCenterX( );
    }

    public final int getCenterY( )
    {
        return delegate.getCenterY( );
    }
    
    public final int getBufferedWidth( )
    {
        return delegate.getBufferedWidth( );
    }
    
    public final int getBufferedHeight( )
    {
        return delegate.getBufferedHeight( );
    }

    public final int getWidth( )
    {
        return delegate.getWidth( );
    }

    public final int getHeight( )
    {
        return delegate.getHeight( );
    }
    
    public final TextureCoords getTextureCoordinates( )
    {
        return delegate.getTextureCoordinates( );
    }
}

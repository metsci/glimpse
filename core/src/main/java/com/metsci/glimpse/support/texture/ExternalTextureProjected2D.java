package com.metsci.glimpse.support.texture;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 * A wrapper around an OpenGL texture handle which is being handled (allocated, freed,
 * drawn into, data loaded onto, etc...) by another class. ExternalTextureProjected2D
 * simply allows the projection capabilities of TextureProjected2D to be applied
 * to such an externally handled texture.
 * 
 * @author ulman
 */
public class ExternalTextureProjected2D extends TextureProjected2D
{

    public ExternalTextureProjected2D( int texHandle, int dataSizeX, int dataSizeY, boolean useVertexZCoord )
    {
        super( dataSizeX, dataSizeY, useVertexZCoord );
        
        this.numTextures = 1;
        this.textureHandles = new int[] { texHandle };
    }

    @Override
    protected void allocate_genTextureHandles( GL gl )
    {
        // do nothing, the texture handle has already been created externally
    }
    
    @Override
    protected void prepare_setData( GL2 gl )
    {
        // do nothing, loading data into the texture is handled externally
    }

    @Override
    protected ByteBuffer newByteBuffer( )
    {
        // don't allocate any space, texture data is handled externally
        return null;
    }
    
    @Override
    protected int getRequiredCapacityBytes( )
    {
        throw new UnsupportedOperationException( "getRequiredCapacityBytes() is not supported by ExternalTextureProjected2D because its underlying OpenGL texture is handled externally." );
    }

    @Override
    protected float getData( int index )
    {
        throw new UnsupportedOperationException( "getData() is not supported by ExternalTextureProjected2D because its underlying OpenGL texture is handled externally." );
    }
    
    @Override
    public void resize( int dataSizeX, int dataSizeY )
    {
        throw new UnsupportedOperationException( "resize() is not supported by ExternalTextureProjected2D because its underlying OpenGL texture is handled externally." );
    }

}

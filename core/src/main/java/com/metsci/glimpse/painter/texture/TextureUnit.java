package com.metsci.glimpse.painter.texture;

import javax.media.opengl.GL2;

import com.metsci.glimpse.gl.texture.Texture;

public class TextureUnit<D extends Texture>
{
    protected int textureUnit;
    protected D texture;

    public TextureUnit( D texture )
    {
        this( 0, texture );
    }

    public TextureUnit( int textureUnit, D texture )
    {
        this.textureUnit = textureUnit;
        this.texture = texture;
    }
    
    public boolean prepare( GL2 gl )
    {
        return texture.prepare( gl, textureUnit );
    }

    public int getTextureUnit( )
    {
        return textureUnit;
    }

    public D getTexture( )
    {
        return texture;
    }

    @Override
    public int hashCode( )
    {
        return 31 + ( ( texture == null ) ? 0 : texture.hashCode( ) );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        TextureUnit<?> other = ( TextureUnit<?> ) obj;
        if ( texture == null ) return other.texture == null;
        return texture.equals( other.texture );
    }
}

/*
 * Copyright 2012 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
package com.metsci.glimpse.com.jogamp.opengl.util.awt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureData;
import com.metsci.glimpse.jogamp.opengl.util.awt.text.Check;
import com.metsci.glimpse.jogamp.opengl.util.awt.text.GlyphRenderer;
import com.metsci.glimpse.jogamp.opengl.util.awt.text.TextureRenderers;

//Copied and modified from com.jogamp.opengl.util.awt.TextureRenderer
public class TextureRenderer
{

    // Whether we have an alpha channel in the (RGB/A) backing store
    private final boolean alpha;

    // Whether we're using only a GL_INTENSITY backing store
    private final boolean intensity;

    // Whether we're attempting to use automatic mipmap generation support
    private boolean mipmap;

    // Whether smoothing is enabled for the OpenGL texture (switching
    // between GL_LINEAR and GL_NEAREST filtering)
    private boolean smoothing = true;
    private boolean smoothingChanged;

    // The backing store itself
    private BufferedImage image;

    private Texture texture;
    private AWTTextureData textureData;
    private boolean mustReallocateTexture;
    private Rectangle dirtyRegion;

    private com.metsci.glimpse.jogamp.opengl.util.awt.text.TextureRenderer renderer = new TextureRendererProxy( );

    public TextureRenderer( final int width, final int height, final boolean alpha )
    {
        this( width, height, alpha, false );
    }

    public TextureRenderer( final int width, final int height, final boolean alpha, final boolean mipmap )
    {
        this( width, height, alpha, false, mipmap );
    }

    private TextureRenderer( final int width, final int height, final boolean alpha, final boolean intensity, final boolean mipmap )
    {
        this.alpha = alpha;
        this.intensity = intensity;
        this.mipmap = mipmap;
        init( width, height );
    }

    public static TextureRenderer createAlphaOnlyRenderer( final int width, final int height )
    {
        return createAlphaOnlyRenderer( width, height, false );
    }

    public static TextureRenderer createAlphaOnlyRenderer( final int width, final int height, final boolean mipmap )
    {
        return new TextureRenderer( width, height, false, true, mipmap );
    }

    public int getWidth( )
    {
        return image.getWidth( );
    }

    public int getHeight( )
    {
        return image.getHeight( );
    }

    public Dimension getSize( )
    {
        return getSize( null );
    }

    public Dimension getSize( Dimension d )
    {
        if ( d == null ) d = new Dimension( );
        d.setSize( image.getWidth( ), image.getHeight( ) );
        return d;
    }

    public void setSize( final int width, final int height ) throws GLException
    {
        init( width, height );
    }

    public void setSize( final Dimension d ) throws GLException
    {
        setSize( d.width, d.height );
    }

    public void setSmoothing( final boolean smoothing )
    {
        this.smoothing = smoothing;
        smoothingChanged = true;
    }

    public boolean getSmoothing( )
    {
        return smoothing;
    }

    public Graphics2D createGraphics( )
    {
        return image.createGraphics( );
    }

    public Image getImage( )
    {
        return image;
    }

    public void markDirty( final int x, final int y, final int width, final int height )
    {
        final Rectangle curRegion = new Rectangle( x, y, width, height );
        if ( dirtyRegion == null )
        {
            dirtyRegion = curRegion;
        }
        else
        {
            dirtyRegion.add( curRegion );
        }
    }

    public Texture getTexture( ) throws GLException
    {
        if ( dirtyRegion != null )
        {
            sync( dirtyRegion.x, dirtyRegion.y, dirtyRegion.width, dirtyRegion.height );
            dirtyRegion = null;
        }

        ensureTexture( );
        return texture;
    }

    public void beginOrthoRendering( final int width, final int height ) throws GLException
    {
        beginOrthoRendering( width, height, true );
    }

    public void beginOrthoRendering( final int width, final int height, final boolean disableDepthTest ) throws GLException
    {
        beginRendering( true, width, height, disableDepthTest );
    }

    public void begin3DRendering( ) throws GLException
    {
        beginRendering( false, 0, 0, false );
    }

    public void dispose( ) throws GLException
    {
        if ( texture != null )
        {
            texture.destroy( GLContext.getCurrentGL( ) );
            texture = null;
        }
        if ( image != null )
        {
            image.flush( );
            image = null;
        }
    }

    public void setColor( final float r, final float g, final float b, final float a ) throws GLException
    {
        this.renderer.setColor( r, g, b, a );
    }

    private float[] compArray;

    public void setColor( final Color color ) throws GLException
    {
        // Get color's RGBA components as floats in the range [0,1].
        if ( compArray == null )
        {
            compArray = new float[4];
        }
        color.getRGBComponents( compArray );
        setColor( compArray[0], compArray[1], compArray[2], compArray[3] );
    }

    public void drawOrthoRect( final int screenx, final int screeny ) throws GLException
    {
        drawOrthoRect( screenx, screeny, 0, 0, getWidth( ), getHeight( ) );
    }

    public void drawOrthoRect( final int screenx, final int screeny,
            final int texturex, final int texturey,
            final int width, final int height ) throws GLException
    {
        draw3DRect( screenx, screeny, 0, texturex, texturey, width, height, 1 );
    }

    public void draw3DRect( final float x, final float y, final float z,
            final int texturex, final int texturey,
            final int width, final int height,
            final float scaleFactor ) throws GLException
    {
        final GL3 gl = GLContext.getCurrentGL( ).getGL3( );
        final Texture texture = getTexture( );
        renderer.drawTexture( gl, texture, x, y, z, texturex, texturey, width, height, scaleFactor );
    }

    public void endOrthoRendering( ) throws GLException
    {
        endRendering( true );
    }

    public void end3DRendering( ) throws GLException
    {
        endRendering( false );
    }

    public boolean isUsingAutoMipmapGeneration( )
    {
        return mipmap;
    }

    //----------------------------------------------------------------------
    // Internals only below this point
    //

    private void beginRendering( final boolean ortho, final int width, final int height, final boolean disableDepthTestForOrtho )
    {
        final GL3 gl = GLContext.getCurrentGL( ).getGL3( );

        Texture texture = getTexture( );

        texture.enable( gl ); // is a noop for core profiles
        texture.bind( gl );

        if ( smoothingChanged )
        {
            smoothingChanged = false;
            if ( smoothing )
            {
                texture.setTexParameteri( gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
                if ( mipmap )
                {
                    texture.setTexParameteri( gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR );
                }
                else
                {
                    texture.setTexParameteri( gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
                }
            }
            else
            {
                texture.setTexParameteri( gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST );
                texture.setTexParameteri( gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST );
            }
        }

        renderer.beginRendering( gl, ortho, width, height, disableDepthTestForOrtho );
    }

    private void endRendering( final boolean ortho )
    {
        final GL3 gl = GLContext.getCurrentGL( ).getGL3( );

        Texture texture = getTexture( );

        texture.disable( gl ); // is a noop for core profiles

        renderer.endRendering( gl );
    }

    private void init( final int width, final int height )
    {
        final GL3 gl = GLContext.getCurrentGL( ).getGL3( );

        // Discard previous BufferedImage if any
        if ( image != null )
        {
            image.flush( );
            image = null;
        }

        // Infer the internal format if not an intensity texture
        final int internalFormat = ( intensity ? GL3.GL_R32F : 0 );
        final int imageType = ( intensity ? BufferedImage.TYPE_BYTE_GRAY : ( alpha ? BufferedImage.TYPE_INT_ARGB_PRE : BufferedImage.TYPE_INT_RGB ) );

        image = new BufferedImage( width, height, imageType );
        // Always realllocate the TextureData associated with this
        // BufferedImage; it's just a reference to the contents but we
        // need it in order to update sub-regions of the underlying
        // texture
        textureData = new AWTTextureData( gl.getGLProfile( ), internalFormat, 0, mipmap, image );
        // For now, always reallocate the underlying OpenGL texture when
        // the backing store size changes
        mustReallocateTexture = true;
    }

    private void sync( final int x, final int y, final int width, final int height ) throws GLException
    {
        // Force allocation if necessary
        final boolean canSkipUpdate = ensureTexture( );

        if ( !canSkipUpdate )
        {
            // Update specified region.
            // NOTE that because BufferedImage-based TextureDatas now don't
            // do anything to their contents, the coordinate systems for
            // OpenGL and Java 2D actually line up correctly for
            // updateSubImage calls, so we don't need to do any argument
            // conversion here (i.e., flipping the Y coordinate).
            texture.updateSubImage( GLContext.getCurrentGL( ), textureData, 0, x, y, x, y, width, height );
        }
    }

    // Returns true if the texture was newly allocated, false if not
    private boolean ensureTexture( )
    {
        final GL gl = GLContext.getCurrentGL( );
        if ( mustReallocateTexture )
        {
            if ( texture != null )
            {
                texture.destroy( gl );
                texture = null;
            }
            mustReallocateTexture = false;
        }

        if ( texture == null )
        {
            texture = TextureIO.newTexture( textureData );
            if ( mipmap && !texture.isUsingAutoMipmapGeneration( ) )
            {
                // Only try this once
                texture.destroy( gl );
                mipmap = false;
                textureData.setMipmap( false );
                texture = TextureIO.newTexture( textureData );
            }

            if ( !smoothing )
            {
                // The TextureIO classes default to GL_LINEAR filtering
                texture.setTexParameteri( gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST );
                texture.setTexParameteri( gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST );
            }
            return true;
        }

        return false;
    }

    /**
     * <em>Proxy</em> for a {@link GlyphRenderer}.
     */
    /*@NotThreadSafe*/
    protected static final class TextureRendererProxy implements com.metsci.glimpse.jogamp.opengl.util.awt.text.TextureRenderer
    {

        /**
         * Delegate to actually render.
         */
        /*@CheckForNull*/
        private com.metsci.glimpse.jogamp.opengl.util.awt.text.TextureRenderer delegate;

        /**
         * Listeners added before a delegate is chosen.
         */
        /*@Nonnull*/
        private final List<EventListener> listeners = new ArrayList<EventListener>( );

        /**
         * Red component of color.
         */
        /*@CheckForSigned*/
        private Float r;

        /**
         * Green component of color.
         */
        /*@CheckForSigned*/
        private Float g;

        /**
         * Blue component of color.
         */
        /*@CheckForSigned*/
        private Float b;

        /**
         * Alpha component of color.
         */
        /*@CheckForSigned*/
        private Float a;

        /**
         * Transform matrix.
         */
        /*@CheckForNull*/
        private float[] transform;

        /**
         * True if transform is transposed.
         */
        /*@CheckForNull*/
        private Boolean transposed;

        /**
         * True to use vertex arrays.
         */
        private boolean useVertexArrays = true;

        TextureRendererProxy( )
        {
            // empty
        }

        @Override
        public void addListener( /*@Nonnull*/ final EventListener listener )
        {

            Check.notNull( listener, "Listener cannot be null" );

            if ( delegate == null )
            {
                listeners.add( listener );
            }
            else
            {
                delegate.addListener( listener );
            }
        }

        @Override
        public void beginRendering( /*@Nonnull*/ final GL gl, final boolean ortho, /*@Nonnegative*/ final int width, /*@Nonnegative*/ final int height, final boolean disableDepthTest )
        {

            Check.notNull( gl, "GL cannot be null" );
            Check.argument( width >= 0, "Width cannot be negative" );
            Check.argument( height >= 0, "Height cannot be negative" );

            if ( delegate == null )
            {
                // Create the glyph renderer
                delegate = TextureRenderers.get( gl );

                // Add the event listeners
                for ( EventListener listener : listeners )
                {
                    delegate.addListener( listener );
                }

                // Specify the color
                if ( ( r != null ) && ( g != null ) && ( b != null ) && ( a != null ) )
                {
                    delegate.setColor( r, g, b, a );
                }

                // Specify the transform
                if ( ( transform != null ) && ( transposed != null ) )
                {
                    delegate.setTransform( transform, transposed );
                }

                // Specify whether to use vertex arrays or not
                delegate.setUseVertexArrays( useVertexArrays );
            }

            delegate.beginRendering( gl, ortho, width, height, disableDepthTest );
        }

        @Override
        public void dispose( /*@Nonnull*/ final GL gl )
        {

            Check.notNull( gl, "GL cannot be null" );

            if ( delegate != null )
            {
                delegate.dispose( gl );
            }
        }

        @Override
        public void drawTexture( GL gl, Texture texture, float x, float y, float z, int texturex, int texturey, int width, int height, float scale )
        {
            Check.notNull( gl, "GL cannot be null" );
            Check.notNull( texture, "Texture cannot be null" );

            if ( delegate == null )
            {
                throw new IllegalStateException( "Must be in render cycle!" );
            }
            else
            {
                delegate.drawTexture( gl, texture, x, y, z, texturex, texturey, width, height, scale );
            }
        }

        @Override
        public void endRendering( /*@Nonnull*/ final GL gl )
        {

            Check.notNull( gl, "GL cannot be null" );

            if ( delegate == null )
            {
                throw new IllegalStateException( "Must be in render cycle!" );
            }
            else
            {
                delegate.endRendering( gl );
            }
        }

        @Override
        public void flush( /*@Nonnull*/ final GL gl )
        {

            Check.notNull( gl, "GL cannot be null" );

            if ( delegate == null )
            {
                throw new IllegalStateException( "Must be in render cycle!" );
            }
            else
            {
                delegate.flush( gl );
            }
        }

        @Override
        public boolean getUseVertexArrays( )
        {
            if ( delegate == null )
            {
                return useVertexArrays;
            }
            else
            {
                return delegate.getUseVertexArrays( );
            }
        }

        @Override
        public void setColor( /*@CheckForSigned*/ final float r, /*@CheckForSigned*/ final float g, /*@CheckForSigned*/ final float b, /*@CheckForSigned*/ final float a )
        {
            if ( delegate == null )
            {
                this.r = r;
                this.g = g;
                this.b = b;
                this.a = a;
            }
            else
            {
                delegate.setColor( r, g, b, a );
            }
        }

        @Override
        public void setTransform( /*@Nonnull*/ final float[] value, final boolean transpose )
        {

            Check.notNull( value, "Value cannot be null" );

            if ( delegate == null )
            {
                this.transform = Arrays.copyOf( value, value.length );
                this.transposed = transpose;
            }
            else
            {
                delegate.setTransform( value, transpose );
            }
        }

        @Override
        public void setUseVertexArrays( final boolean useVertexArrays )
        {
            if ( delegate == null )
            {
                this.useVertexArrays = useVertexArrays;
            }
            else
            {
                delegate.setUseVertexArrays( useVertexArrays );
            }
        }
    }
}

/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.canvas;

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLRunnable;

import com.jogamp.opengl.FBObject.Colorbuffer;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.texture.ExternalTextureProjected2D;
import com.metsci.glimpse.support.texture.TextureProjected2D;

// example JOGL FBO Usage: https://github.com/sgothel/jogl/blob/master/src/test/com/jogamp/opengl/test/junit/jogl/acore/TestFBOOffThreadSharedContextMix2DemosES2NEWT.java
//                         https://github.com/sgothel/jogl/blob/master/src/test/com/jogamp/opengl/test/junit/jogl/acore/TestFBOAutoDrawableFactoryNEWT.java
public class FBOGlimpseCanvas extends AbstractGlimpseCanvas
{
    private static final Logger logger = Logger.getLogger( FBOGlimpseCanvas.class.getName( ) );

    public static final int DEFAULT_TEXTURE_UNIT = 0;

    protected GLProfile glProfile;
    protected GLOffscreenAutoDrawable.FBO drawable;
    protected boolean isDestroyed;

    public FBOGlimpseCanvas( GLProfile glProfile, int width, int height )
    {
        init( glProfile, null, width, height, true );
    }

    public FBOGlimpseCanvas( GLContext glContext, int width, int height )
    {
        init( glContext.getGLDrawable( ).getGLProfile( ), glContext, width, height, true );
    }

    public FBOGlimpseCanvas( GLContext glContext, int width, int height, boolean isBackgroundOpaque )
    {
        init( glContext.getGLDrawable( ).getGLProfile( ), glContext, width, height, isBackgroundOpaque );
    }

    /**
     * @deprecated Use {@link #FBOGlimpseCanvas(GLContext,int,int)} instead. The context implicitly provides a GLProfile.
     */
    @Deprecated
    public FBOGlimpseCanvas( String glProfileName, GLContext glContext, int width, int height )
    {
        this( GLProfile.get( glProfileName ), glContext, width, height );
    }

    /**
     * @deprecated Use {@link #FBOGlimpseCanvas(GLContext,int,int)} instead. The context implicitly provides a GLProfile.
     */
    @Deprecated
    public FBOGlimpseCanvas( GLProfile glProfile, GLContext glContext, int width, int height )
    {
        init( glProfile, glContext, width, height, true );
    }

    private void init( GLProfile glProfile, GLContext glContext, int width, int height, boolean isbackgroundOpaque )
    {
        this.glProfile = glProfile;
        GLCapabilities caps = new GLCapabilities( glProfile );
        caps.setBackgroundOpaque( isbackgroundOpaque );
        caps.setDoubleBuffered( false );
        this.drawable = ( GLOffscreenAutoDrawable.FBO ) GLUtils.newOffscreenDrawable( caps, glProfile, glContext );
        this.drawable.addGLEventListener( createGLEventListener( ) );
        this.drawable.setSurfaceSize( width, height );
        this.drawable.setTextureUnit( DEFAULT_TEXTURE_UNIT );
    }

    public void resize( int width, int height )
    {
        this.drawable.setSurfaceSize( width, height );
    }

    public BufferedImage toBufferedImage( )
    {
        GLContext glContext = this.drawable.getContext( );
        glContext.makeCurrent( );
        try
        {
            this.paint( );
            AWTGLReadBufferUtil util = new AWTGLReadBufferUtil( this.glProfile, true );
            return util.readPixelsToBufferedImage( glContext.getGL( ), true );
        }
        finally
        {
            glContext.release( );
        }
    }

    //XXX this link probably no longer relevant to 2.2.0
    // see: http://forum.jogamp.org/querying-textures-bound-to-default-draw-read-framebuffers-td4026564.html
    public int getTextureName( )
    {
        return drawable.getColorbuffer( GL.GL_FRONT ).getName( );
    }

    public TextureProjected2D getProjectedTexture( )
    {
        Colorbuffer b = drawable.getColorbuffer( GL.GL_FRONT );
        return new ExternalTextureProjected2D( b.getName( ), b.getWidth( ), b.getHeight( ), false );
    }

    public Texture getTexture( )
    {
        return TextureIO.newTexture( drawable.getTextureUnit( ) );
    }

    protected GLEventListener createGLEventListener( )
    {
        return new GLEventListener( )
        {
            @Override
            public void init( GLAutoDrawable drawable )
            {
                try
                {
                    GL gl = drawable.getGL( );
                    gl.setSwapInterval( 0 );
                }
                catch ( Exception e )
                {
                    // without this, repaint rate is tied to screen refresh rate on some systems
                    // this doesn't work on some machines (Mac OSX in particular)
                    // but it's not a big deal if it fails
                    logWarning( logger, "Trouble in init.", e );
                }
            }

            @Override
            public void display( GLAutoDrawable drawable )
            {
                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.paintTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
            {
                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.layoutTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void dispose( GLAutoDrawable drawable )
            {
                for ( GLRunnable runnable : disposeListeners )
                {
                    runnable.run( drawable );
                }
            }
        };
    }

    @Override
    public GLProfile getGLProfile( )
    {
        return this.glProfile;
    }

    @Override
    public GLOffscreenAutoDrawable.FBO getGLDrawable( )
    {
        return drawable;
    }

    @Override
    public GLContext getGLContext( )
    {
        return drawable.getContext( );
    }

    @Override
    public void paint( )
    {
        drawable.display( );
    }

    @Override
    public GlimpseBounds getTargetBounds( )
    {
        Colorbuffer b = drawable.getColorbuffer( GL.GL_FRONT );
        return new GlimpseBounds( new Dimension( b.getWidth( ), b.getHeight( ) ) );
    }

    @Override
    public GlimpseBounds getTargetBounds( GlimpseTargetStack stack )
    {
        return getTargetBounds( );
    }

    @Override
    public void destroy( )
    {
        if ( !isDestroyed )
        {
            this.drawable.destroy( );
            this.isDestroyed = true;
        }
    }

    @Override
    public boolean isDestroyed( )
    {
        return this.isDestroyed;
    }

    @Override
    public boolean isVisible( )
    {
        return true;
    }

    @Override
    public int[] getSurfaceScale( )
    {
        return new int[] { 1, 1 };
    }
}

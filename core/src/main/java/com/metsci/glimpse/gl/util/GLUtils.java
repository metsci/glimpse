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
package com.metsci.glimpse.gl.util;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;

import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.support.settings.LookAndFeel;

public class GLUtils
{
    // GLES1.GL_POINT_SPRITE shouldn't be necessary (it is deprecated in GL3)
    // however it appears necessary in order for setting gl_PointSize in a vertex shader
    // to have an effect on certain cards/systems/gpus
    public static final boolean DISABLE_POINT_SPRITE = getBooleanProperty( "glimpse.disablePointSprite", false );

    // - unsetValue specifies the default value if the property is not set
    // - setting the property without specifying a value ("0", "1", "true", "false") sets
    //   the property to the negation of the unset value
    // - an invalid property value falls back on the unsetValue
    public static boolean getBooleanProperty( String name, boolean unsetValue )
    {
        String prop = System.getProperty( name );
        if ( prop == null )
            return !unsetValue;
        else if ( prop.equals( "1" ) || prop.equalsIgnoreCase( "true" ) )
            return true;
        else if ( prop.equals( "0" ) || prop.equalsIgnoreCase( "false" ) )
            return false;
        else
            return unsetValue;
    }

    public static final int BYTES_PER_FLOAT = 4;

    public static int defaultVertexAttributeArray( GL gl )
    {
        return gl.getContext( ).getDefaultVAO( );
    }

    public static int genVertexAttributeArray( GL gl )
    {
        final int[] handles = new int[1];
        gl.getGL3( ).glGenVertexArrays( 1, handles, 0 );
        return handles[0];
    }

    public static int genBuffer( GL gl )
    {
        int[] handle = new int[1];
        gl.glGenBuffers( 1, handle, 0 );
        return handle[0];
    }

    public static void deleteBuffers( GL gl, int... handles )
    {
        gl.glDeleteBuffers( handles.length, handles, 0 );
    }

    public static int genTexture( GL gl )
    {
        int[] handle = new int[1];
        gl.glGenTextures( 1, handle, 0 );
        return handle[0];
    }

    public static int queryGLInteger( int param, GL gl )
    {
        int[] value = new int[1];
        gl.glGetIntegerv( param, value, 0 );

        return value[0];
    }

    public static boolean queryGLBoolean( int param, GL gl )
    {
        byte[] value = new byte[1];
        gl.glGetBooleanv( param, value, 0 );

        return value[0] != 0;
    }

    /**
     * Enables blending, and set the blend func that gives the intuitive
     * behavior for most situations.
     * <p>
     * Blended RGB will be the weighted average of source RGB and dest RGB:
     * <pre>
     *    RGB = (A_s)*RGB_s + (1-A_s)*RGB_d
     * </pre>
     * Blended Alpha will be:
     * <pre>
     *    A = 1 - (1-A_d)*(1-A_s)
     *      = A_s*(1) + A_d*(1-A_s)
     * </pre>
     * Often the blended alpha has no visible effect. However, it matters
     * when reading pixels from the resulting framebuffer -- for export to
     * an image file, e.g.
     */
    public static void enableStandardBlending( GL gl )
    {
        gl.glBlendFuncSeparate( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL.GL_BLEND );
    }

    public static void disableBlending( GL gl )
    {
        gl.glDisable( GL.GL_BLEND );
    }

    public static int getGLTextureDim( int ndim )
    {
        switch ( ndim )
        {
            case 1:
                return GL3.GL_TEXTURE_1D;
            case 2:
                return GL3.GL_TEXTURE_2D;
            case 3:
                return GL3.GL_TEXTURE_3D;
            default:
                throw new IllegalArgumentException( "Only 1D, 2D, and 3D textures allowed." );
        }
    }

    public static int getGLTextureUnit( int texUnit )
    {
        if ( texUnit > 31 || texUnit < 0 ) throw new IllegalArgumentException( "Only 31 texture units supported." );

        return GL.GL_TEXTURE0 + texUnit;
    }

    public static GLOffscreenAutoDrawable newOffscreenDrawable( )
    {
        return newOffscreenDrawable( getDefaultGLProfile( ) );
    }

    public static GLOffscreenAutoDrawable newOffscreenDrawable( String profileName )
    {
        return newOffscreenDrawable( GLProfile.get( profileName ) );
    }

    public static GLOffscreenAutoDrawable newOffscreenDrawable( GLProfile profile )
    {
        return newOffscreenDrawable( profile, null );
    }

    public static GLOffscreenAutoDrawable newOffscreenDrawable( GLContext sharedContext )
    {
        return newOffscreenDrawable( sharedContext.getGLDrawable( ).getGLProfile( ), sharedContext );
    }

    public static GLOffscreenAutoDrawable newOffscreenDrawable( GLProfile profile, GLContext sharedContext )
    {
        return newOffscreenDrawable( new GLCapabilities( profile ), profile, sharedContext );
    }

    public static GLOffscreenAutoDrawable newOffscreenDrawable( GLCapabilities caps, GLProfile profile, GLContext sharedContext )
    {
        GLDrawableFactory drawableFactory = GLDrawableFactory.getFactory( profile );

        GLOffscreenAutoDrawable offscreenDrawable = drawableFactory.createOffscreenAutoDrawable( null, caps, null, 1, 1 );
        if ( sharedContext != null ) offscreenDrawable.setSharedContext( sharedContext );

        // Trigger context creation
        offscreenDrawable.display( );

        return offscreenDrawable;
    }

    public static GLProfile getDefaultGLProfile( )
    {
        return GLProfile.get( GLProfile.GL3 );
    }

    public static String profileNameOf( GLContext context )
    {
        return context.getGLDrawable( ).getGLProfile( ).getName( );
    }

    /**
     * Returns the profile-name of the given context, or the given fallback if context is null.
     */
    public static String profileNameOf( GLContext context, String fallback )
    {
        return ( context == null ? fallback : profileNameOf( context ) );
    }

    public static FPSAnimator startFpsAnimator( int fps, GlimpseCanvas... canvases )
    {
        FPSAnimator animator = new FPSAnimator( fps );
        for ( GlimpseCanvas canvas : canvases )
        {
            animator.add( canvas.getGLDrawable( ) );
        }
        animator.start( );
        return animator;
    }

    public static Runnable newPaintJob( final GlimpseCanvas canvas )
    {
        return new Runnable( )
        {
            @Override
            public void run( )
            {
                canvas.paint( );
            }
        };
    }

    public static void setLookAndFeel( LookAndFeel laf, GlimpseTarget... targets )
    {
        for ( GlimpseTarget target : targets )
        {
            target.setLookAndFeel( laf );
        }
    }

    public static void setViewportAndScissor( GlimpseContext context )
    {
        final int[] scale = context.getSurfaceScale( );
        final int scaleX = scale[0];
        final int scaleY = scale[1];
        GL gl = context.getGL( );

        GlimpseBounds bounds = context.getTargetStack( ).getBounds( );
        GlimpseBounds clippedBounds = getClippedBounds( context );

        gl.glEnable( GL.GL_SCISSOR_TEST );

        gl.glViewport( bounds.getX( ) * scaleX, bounds.getY( ) * scaleY, bounds.getWidth( ) * scaleX, bounds.getHeight( ) * scaleY );
        gl.glScissor( clippedBounds.getX( ) * scaleX, clippedBounds.getY( ) * scaleY, clippedBounds.getWidth( ) * scaleX, clippedBounds.getHeight( ) * scaleY );
    }

    public static GlimpseBounds getClippedBounds( GlimpseContext context )
    {
        int minX = Integer.MIN_VALUE;
        int maxX = Integer.MAX_VALUE;
        int minY = Integer.MIN_VALUE;
        int maxY = Integer.MAX_VALUE;

        for ( GlimpseBounds parentBounds : context.getTargetStack( ).getBoundsList( ) )
        {
            minX = Math.max( parentBounds.getX( ), minX );
            maxX = Math.min( parentBounds.getX( ) + parentBounds.getWidth( ), maxX );
            minY = Math.max( parentBounds.getY( ), minY );
            maxY = Math.min( parentBounds.getY( ) + parentBounds.getHeight( ), maxY );
        }

        return new GlimpseBounds( minX, minY, maxX - minX, maxY - minY );
    }
}

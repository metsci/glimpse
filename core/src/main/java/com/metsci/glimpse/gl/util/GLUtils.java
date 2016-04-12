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
import javax.media.opengl.GL2;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;

import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.support.settings.LookAndFeel;

public class GLUtils
{
    private GLUtils( )
    {
    };

    public static int genBuffer( GL gl )
    {
        int[] handle = new int[1];
        gl.glGenBuffers( 1, handle, 0 );
        return handle[0];
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

    public static int getGLTextureDim( int ndim )
    {
        switch ( ndim )
        {
            case 1:
                return GL2.GL_TEXTURE_1D;
            case 2:
                return GL2.GL_TEXTURE_2D;
            case 3:
                return GL2.GL_TEXTURE_3D;
            default:
                throw new IllegalArgumentException( "Only 1D, 2D, and 3D textures allowed." );
        }
    }

    public static int getGLTextureUnit( int texUnit )
    {
        if ( texUnit > 31 || texUnit < 0 ) throw new IllegalArgumentException( "Only 31 texture units supported." );

        return GL2.GL_TEXTURE0 + texUnit;
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
        return GLProfile.getMaxFixedFunc( true );
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
}

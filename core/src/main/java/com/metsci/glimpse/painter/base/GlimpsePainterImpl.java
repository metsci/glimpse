/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.painter.base;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.glu.GLU;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.util.GLErrorUtils;
import com.metsci.glimpse.support.settings.LookAndFeel;

public abstract class GlimpsePainterImpl implements GlimpsePainter
{
    public static final Logger logger = Logger.getLogger( GlimpsePainterImpl.class.getName( ) );

    public static final int BYTES_PER_FLOAT = 4;

    protected final GLU glu = new GLU( );

    protected boolean disposed = false;
    protected boolean displayOn = true;
    protected boolean doErrorHandling = true;

    protected abstract void paintTo( GlimpseContext context, GlimpseBounds bounds );

    public void setErrorHandling( boolean doErrorHandling )
    {
        this.doErrorHandling = doErrorHandling;
    }

    public void setVisible( boolean show )
    {
        this.displayOn = show;
    }

    public boolean isVisible( )
    {
        return displayOn;
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // default behavior is to simply do nothing
    }

    @Override
    public void paintTo( GlimpseContext context )
    {
        if ( !displayOn ) return;

        GlimpseBounds bounds = context.getTargetStack( ).getBounds( );

        if ( bounds == null ) return;

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        if ( width <= 0 || height <= 0 ) return;

        GL gl = context.getGL( );

        gl.glPushClientAttrib( ( int ) GL.GL_CLIENT_ALL_ATTRIB_BITS );
        gl.glPushAttrib( GL.GL_ALL_ATTRIB_BITS );
        gl.glMatrixMode( GL.GL_MODELVIEW );
        gl.glPushMatrix( );
        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glPushMatrix( );

        try
        {
            paintTo( context, bounds );
            if ( doErrorHandling ) glHandleError( gl, "GL ERROR: " + getClass( ).getName( ) );
        }
        finally
        {
            gl.glMatrixMode( GL.GL_MODELVIEW );
            gl.glPopMatrix( );
            gl.glMatrixMode( GL.GL_PROJECTION );
            gl.glPopMatrix( );
            gl.glPopAttrib( );
            gl.glPopClientAttrib( );
        }
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        if ( !this.disposed )
        {
            this.disposed = true;
            dispose( context.getGLContext( ) );
        }
    }

    protected void dispose( GLContext context )
    {
        // don't require a dispose method (by default do nothing)
    }

    @Override
    public boolean isDisposed( )
    {
        return this.disposed;
    }

    protected boolean glHandleError( GL gl )
    {
        return glHandleError( gl, "GL ERROR" );
    }

    protected boolean glHandleError( GL gl, String prefix )
    {
        return GLErrorUtils.logGLError( logger, Level.WARNING, gl, prefix );
    }
}

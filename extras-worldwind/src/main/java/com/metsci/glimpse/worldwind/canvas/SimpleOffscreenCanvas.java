/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.worldwind.canvas;

import java.awt.Dimension;
import java.util.List;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLRunnable;

import com.metsci.glimpse.canvas.FBOGlimpseCanvas;
import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.canvas.LayoutManager;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.gl.GLSimpleFrameBufferObject;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * Simplified version of {@link com.metsci.glimpse.canvas.FrameBufferGlimpseCanvas}
 *
 * @author ulman
 * @deprecated see {@link FBOGlimpseCanvas}
 */
public class SimpleOffscreenCanvas implements GlimpseCanvas
{
    protected GLSimpleFrameBufferObject fbo;

    protected LayoutManager layoutManager;

    protected int width;
    protected int height;

    protected boolean useDepth;
    protected boolean useStencil;

    protected boolean isDisposed = false;

    public SimpleOffscreenCanvas( int width, int height, boolean useDepth, boolean useStencil )
    {
        this.width = width;
        this.height = height;

        this.useDepth = useDepth;
        this.useStencil = useStencil;

        this.layoutManager = new LayoutManager( );
    }

    public SimpleOffscreenCanvas( int width, int height, boolean useDepth, boolean useStencil, GLContext context )
    {
        this( width, height, useDepth, useStencil );

        this.initialize( context );
    }

    public void initialize( GLContext context )
    {
        this.fbo = new GLSimpleFrameBufferObject( width, height, useDepth, useStencil, context );
    }

    protected void resize0( )
    {
        for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
        {
            layout.layoutTo( getGlimpseContext( ) );
        }
    }

    public void resize( int new_width, int new_height )
    {
        if ( this.width != new_width || this.height != new_height )
        {
            this.width = new_width;
            this.height = new_height;

            this.fbo.resize( this.width, this.height );
            this.resize0( );
        }
    }

    public GLSimpleFrameBufferObject getFrameBuffer( )
    {
        return this.fbo;
    }

    public Dimension getDimension( )
    {
        // the fbo is instantiated lazily, so don't rely on it
        //return fbo.getDimension( );
        return new Dimension( this.width, this.height );
    }

    @Override
    public GlimpseContext getGlimpseContext( )
    {
        return new GlimpseContextImpl( this );
    }

    @Override
    public GLContext getGLContext( )
    {
        return fbo.getGLContext( );
    }

    @Override
    public GlimpseBounds getTargetBounds( GlimpseTargetStack stack )
    {
        return new GlimpseBounds( getDimension( ) );
    }

    @Override
    public GlimpseBounds getTargetBounds( )
    {
        return getTargetBounds( null );
    }

    @Override
    public void addLayout( GlimpseLayout layout )
    {
        this.layoutManager.addLayout( layout );
    }

    @Override
    public void addLayout( GlimpseLayout layout, int zOrder )
    {
        this.layoutManager.addLayout( layout, zOrder );
    }

    @Override
    public void setZOrder( GlimpseLayout layout, int zOrder )
    {
        this.layoutManager.setZOrder( layout, zOrder );
    }

    @Override
    public void removeLayout( GlimpseLayout layout )
    {
        this.layoutManager.removeLayout( layout );
    }

    @Override
    public void removeAllLayouts( )
    {
        this.layoutManager.removeAllLayouts( );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Override
    public List<GlimpseTarget> getTargetChildren( )
    {
        // layoutManager returns an unmodifiable list, thus this cast is typesafe
        // (there is no way for the recipient of the List<GlimpseTarget> view to
        // add GlimpseTargets which are not GlimpseLayouts to the list)
        return ( List ) this.layoutManager.getLayoutList( );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
        {
            layout.setLookAndFeel( laf );
        }
    }

    @Override
    public void paint( )
    {
        // not a fully featured GlimpseCanvas
    }

    @Override
    public String toString( )
    {
        return SimpleOffscreenCanvas.class.getSimpleName( );
    }

    @Override
    public boolean isEventConsumer( )
    {
        return false;
    }

    @Override
    public void setEventConsumer( boolean consume )
    {
        // do nothing
    }

    @Override
    public boolean isEventGenerator( )
    {
        return false;
    }

    @Override
    public void setEventGenerator( boolean generate )
    {
        // do nothing
    }

    @Override
    public GLAutoDrawable getGLDrawable( )
    {
        // not a fully featured GlimpseCanvas
        return null;
    }

    @Override
    public GLProfile getGLProfile( )
    {
        // not a fully featured GlimpseCanvas
        return null;
    }

    @Override
    public void destroy( )
    {
        if ( !this.isDisposed )
        {
            fbo.getGLContext( ).destroy( );
            this.isDisposed = true;
        }
    }

    @Override
    public boolean isDestroyed( )
    {
        return this.isDisposed;
    }

    @Override
    public void addDisposeListener( GLRunnable runnable )
    {
        // not a fully featured GlimpseCanvas
    }

    @Override
    public void dispose( )
    {
        disposeAttached( );
        destroy( );
    }

    @Override
    public void disposeAttached( )
    {
        // not a fully featured GlimpseCanvas
    }

    @Override
    public void disposePainter( final GlimpsePainter painter )
    {
        this.getGLDrawable( ).invoke( false, new GLRunnable( )
        {
            @Override
            public boolean run( GLAutoDrawable drawable )
            {
                painter.dispose( getGlimpseContext( ) );
                return true;
            }
        } );
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

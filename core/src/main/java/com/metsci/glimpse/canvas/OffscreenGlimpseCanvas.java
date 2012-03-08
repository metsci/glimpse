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
package com.metsci.glimpse.canvas;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GLContext;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.gl.GLListenerInfo;
import com.metsci.glimpse.gl.GLSimpleListener;
import com.metsci.glimpse.gl.GLSimplePixelBuffer;
import com.metsci.glimpse.gl.GLSimplePixelBuffer.GLRunnable;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * A GlimpseCanvas which makes use of {@link javax.media.opengl.GLPbuffer} to
 * render Glimpse components of an offscreen drawing buffer which can then be
 * rendered to a BufferedImage.<p>
 *
 * This is no longer the preferred way of rendering Glimpse components offscreen.
 * The {@link FrameBufferGlimpseCanvas} GlimpseCanvas provides additional
 * capabilities (such as rendering the GlimpseCanvas onto an OpenGL texture).
 *
 * @author ulman
 * @deprecated FrameBufferGlimpseCanvas
 */
public class OffscreenGlimpseCanvas implements GlimpseCanvas
{
    protected GLSimplePixelBuffer pixelBuffer;

    protected boolean isDisposed;

    protected List<GlimpseTarget> unmodifiableList;
    protected List<GlimpseLayout> layoutList;

    public OffscreenGlimpseCanvas( int width, int height )
    {
        this( width, height, null );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public OffscreenGlimpseCanvas( int width, int height, GLContext _context )
    {
        this.pixelBuffer = new GLSimplePixelBuffer( width, height, _context );
        this.isDisposed = false;

        this.layoutList = new ArrayList<GlimpseLayout>( );

        // this is typesafe because unmodifiableList is unmodifiable, so it's not
        // possible to corrupt layoutList with non GlimpseLayouts
        this.unmodifiableList = (List) Collections.unmodifiableList( this.layoutList );

        this.pixelBuffer.addListener( new GLSimpleListener( )
        {
            @Override
            public void init( GLContext context )
            {
                // do nothing
            }

            @Override
            public void display( GLContext context )
            {
                for ( GlimpseLayout layout : layoutList )
                {
                    layout.paintTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void reshape( GLContext context, int x, int y, int width, int height )
            {
                for ( GlimpseLayout layout : layoutList )
                {
                    layout.layoutTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void displayChanged( GLContext context, boolean modeChanged, boolean deviceChanged )
            {
                // do nothing
            }

            @Override
            public void dispose( GLContext context )
            {
                // do nothing
            }

            @Override
            public boolean isDisposed( )
            {
                // return dummy value
                return false;
            }

            @Override
            public GLListenerInfo getInfo( )
            {
                // return dummy value
                return null;
            }

        } );
    }

    public Dimension getDimension( )
    {
        return pixelBuffer.getDimension( );
    }

    public Object glSyncExec( GLRunnable runnable )
    {
        return pixelBuffer.glSyncExec( runnable );
    }

    public BufferedImage drawToBufferedImage( )
    {
        return pixelBuffer.drawToBufferedImage( );
    }

    public void resize( int width, int height, boolean notifyListeners )
    {
        pixelBuffer.resize( width, height, notifyListeners );
    }

    @Override
    public GlimpseContext getGlimpseContext( )
    {
        return new GlimpseContextImpl( this );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        for ( GlimpseLayout layout : layoutList )
        {
            layout.setLookAndFeel( laf );
        }
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
        this.layoutList.add( layout );
    }

    @Override
    public void removeLayout( GlimpseLayout layout )
    {
        this.layoutList.remove( layout );
    }

    @Override
    public List<GlimpseTarget> getTargetChildren( )
    {
        return this.unmodifiableList;
    }

    @Override
    public void paint( )
    {
        pixelBuffer.draw( );
    }

    @Override
    public GLContext getGLContext( )
    {
        return pixelBuffer.getGLContext( );
    }

    @Override
    public void dispose( )
    {
        pixelBuffer.dispose( );
        isDisposed = true;
    }

    @Override
    public boolean isDisposed( )
    {
        return isDisposed;
    }

    @Override
    public String toString( )
    {
        return OffscreenGlimpseCanvas.class.getSimpleName( );
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
}

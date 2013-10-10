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
package com.metsci.glimpse.swt.canvas;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLProfile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.metsci.glimpse.gl.GLSimpleListener;

public class GLSimpleSwtCanvas extends GLCanvas
{

    private final static Logger logger = Logger.getLogger( GLSimpleSwtCanvas.class.getName( ) );

    private static class ListenerEntry
    {
        private final GLSimpleListener listener;
        private boolean needsInit;
        private boolean needsReshape;
        private boolean warnOnException;

        public ListenerEntry( GLSimpleListener listener )
        {
            this.listener = listener;
            needsInit = true;
            needsReshape = true;
            warnOnException = true;
        }

        public void requireReshape( )
        {
            needsReshape = true;
        }

        public void draw( GLContext context, Rectangle bounds )
        {
            try
            {
                if ( needsInit )
                {
                    listener.init( context );
                    needsInit = false;
                }

                if ( needsReshape )
                {
                    GL gl = context.getGL( );
                    gl.glViewport( 0, 0, bounds.width, bounds.height );
                    listener.reshape( context, 0, 0, bounds.width, bounds.height );
                    needsReshape = false;
                }

                listener.display( context );

                warnOnException = true;
            }
            catch ( Exception e )
            {
                if ( warnOnException )
                {
                    logger.log( Level.WARNING, "Listener failed to draw", e );
                    warnOnException = false;
                }
            }
        }

        @Override
        public boolean equals( Object o )
        {
            if ( o == null ) return false;

            if ( o instanceof ListenerEntry )
            {
                ListenerEntry l = ( ListenerEntry ) o;
                return listener.equals( l.listener );
            }

            return false;
        }
    }

    private static GLData glData( )
    {
        GLData data = new GLData( );
        data.doubleBuffer = true;
        return data;
    }

    private final GLContext context;
    private final List<ListenerEntry> listeners;

    public GLSimpleSwtCanvas( Composite parent, GLContext _context, int options, GLSimpleListener... listeners )
    {
        super( parent, options, glData( ) );

        setCurrent( );

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        
        if ( _context == null )
        {
            context = GLDrawableFactory.getFactory( profile ).createExternalGLContext( );
        }
        else
        {
            GLDrawable drawable = GLDrawableFactory.getFactory( profile ).createExternalGLDrawable( );
            context = drawable.createContext( _context );
        }

        this.listeners = new CopyOnWriteArrayList<ListenerEntry>( );
        for ( GLSimpleListener l : listeners )
            this.listeners.add( new ListenerEntry( l ) );

        addListener( SWT.Resize, new Listener( )
        {
            public void handleEvent( Event event )
            {
                handleResize( );
            }
        } );
    }

    public GLSimpleSwtCanvas( Composite parent, int options, GLSimpleListener... listeners )
    {
        this( parent, null, options, listeners );
    }

    public GLSimpleSwtCanvas( Composite parent, GLSimpleListener... listeners )
    {
        this( parent, null, SWT.NONE, listeners );
    }

    public void addListener( GLSimpleListener listener )
    {
        listeners.add( new ListenerEntry( listener ) );
    }

    public void removeListener( GLSimpleListener listener )
    {
        listeners.remove( listener );
    }

    private void handleResize( )
    {
        for ( ListenerEntry l : listeners )
            l.requireReshape( );
    }

    /**
     * This context must be shared with the one in this canvas or else the method will
     * cause an exception.
     */
    public void draw( GLContext glContext )
    {
        int status = glContext.makeCurrent( );

        if ( status == GLContext.CONTEXT_NOT_CURRENT )
        {
            logger.warning( "Unable to make correct context current.  Skipping draw." );
            return;
        }

        Rectangle bounds = getBounds( );
        for ( ListenerEntry l : listeners )
            l.draw( context, bounds );

        glContext.getGL( ).glFlush( );
    }

    public void draw( )
    {
        setCurrent( );
        draw( getGLContext( ) );
        swapBuffers( );
        getGLContext( ).release( );
    }

    public GLContext getGLContext( )
    {
        return context;
    }

    public void dispose( )
    {
        setCurrent( );
        getGLContext( ).makeCurrent( );
        try
        {
            for ( ListenerEntry entry : listeners )
            {
                entry.listener.dispose( context );
            }
        }
        finally
        {
            getGLContext( ).release( );
        }

        super.dispose( );
    }
}

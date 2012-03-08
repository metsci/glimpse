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
package com.metsci.glimpse.gl;

import static com.sun.opengl.util.Screenshot.readToBufferedImage;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLPbuffer;

import com.metsci.glimpse.gl.util.GLPBufferUtils;


public class GLSimplePixelBuffer
{
    private GLPbuffer buffer;

    private final static Logger logger = Logger.getLogger(GLSimplePixelBuffer.class.getName());


    public static class ListenerEntry
    {
        private final GLSimpleListener listener;
        private boolean needsInit;
        private boolean needsReshape;
        private boolean warnOnException;

        public ListenerEntry(GLSimpleListener listener)
        {
            this.listener = listener;
            needsInit = true;
            needsReshape = true;
            warnOnException = true;
        }

        public void requireReshape()
        {
            needsReshape = true;
        }

        public void draw(GLContext context, Rectangle bounds)
        {
            try
            {
                if (needsInit)
                {
                    listener.init(context);
                    needsInit = false;
                }

                if (needsReshape)
                {
                    GL gl = context.getGL();
                    gl.glViewport(0, 0, bounds.width, bounds.height);
                    listener.reshape(context, 0, 0, bounds.width, bounds.height);
                    needsReshape = false;
                }

                listener.display(context);

                warnOnException = true;
            }
            catch (Exception e)
            {
                if (warnOnException)
                {
                    logger.log(Level.WARNING, "Listener failed to draw", e);
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
                ListenerEntry l = (ListenerEntry) o;
                return listener.equals( l.listener );
            }

            return false;
        }
    }


    private GLContext context;
    private List<ListenerEntry> listeners;

    public GLSimplePixelBuffer( int width, int height, GLContext _context )
    {
        createPixelBuffer( width, height, _context );
        this.listeners = new CopyOnWriteArrayList<ListenerEntry>();
    }

    protected void createPixelBuffer( int width, int height, GLContext _context )
    {
        buffer = GLPBufferUtils.createPixelBuffer( width, height );
        context = buffer.createContext( _context );
    }

    public void addListener( GLSimpleListener listener )
    {
        listeners.add( new ListenerEntry( listener ) );
    }

    public void removeListener( GLSimpleListener listener )
    {
        listeners.remove( listener );
    }

    public Dimension getDimension()
    {
        return new Dimension( buffer.getHeight(), buffer.getWidth() );
    }

    public void resize( int width, int height, boolean notifyListeners )
    {
        int currHeight = buffer.getHeight();
        int currWidth = buffer.getWidth();

        if( currHeight != height || currWidth != width )
        {
            GLPbuffer oldBuffer = buffer;
            createPixelBuffer( width, height, context );
            oldBuffer.destroy();

            int newHeight = buffer.getHeight();
            int newWidth = buffer.getWidth();

            // resize worked properly
            if( newHeight == height && newWidth == width )
            {
                if( notifyListeners )
                {
                    for( ListenerEntry l : listeners )
                    {
                        l.requireReshape();
                    }
                }
            }
        }
    }

    public void draw()
    {
        glSyncExec( new GLRunnable() {
            @Override
            public Object run( GLContext context, Rectangle bounds, List<ListenerEntry> entries )
            {
                for( ListenerEntry l: entries )
                    l.draw( context, bounds );

                context.getGL().glFlush();
                return null;
            }
        });
    }

    public BufferedImage drawToBufferedImage()
    {
        return (BufferedImage) glSyncExec( new GLRunnable() {
            @Override
            public Object run( GLContext context, Rectangle bounds, List<ListenerEntry> entries )
            {
                for (ListenerEntry l : listeners)
                    l.draw(context, bounds);

                context.getGL().glFlush();
                return readToBufferedImage( buffer.getWidth(), buffer.getHeight() );
            }
        } );
    }

    // XXX: I have no idea if this is the right name for the method.
    public Object glSyncExec( GLRunnable runnable )
    {
        context.makeCurrent();
        Object result = runnable.run( context, getBounds(), new ArrayList<ListenerEntry>( listeners ) );
        context.release();

        return result;
    }

    public Rectangle getBounds()
    {
        return new Rectangle( 0, 0, buffer.getWidth(), buffer.getHeight() );
    }

    public GLContext getGLContext( )
    {
        return context;
    }

    public void dispose( )
    {
        glSyncExec( new GLRunnable() {
            @Override
            public Object run( GLContext context, Rectangle bounds, List<ListenerEntry> entries )
            {
                for ( ListenerEntry entry : entries )
                {
                    entry.listener.dispose( context );
                }

                return null;
            }
        });

        listeners.clear();
        context.destroy();
    }

    public interface GLRunnable
    {
        Object run( GLContext context, Rectangle bounds, List<ListenerEntry> entries );
    }
}

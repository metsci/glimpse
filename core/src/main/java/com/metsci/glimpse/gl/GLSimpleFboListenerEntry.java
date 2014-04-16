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

import java.awt.Rectangle;
import java.util.logging.Level;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.canvas.FBOGlimpseCanvas;

import java.util.logging.Logger;

/**
 * @deprecated use {@link FBOGlimpseCanvas} instead
 */
@Deprecated
public class GLSimpleFboListenerEntry
{
    private static final Logger logger = Logger.getLogger( GLSimpleFboListenerEntry.class.getName( ) );

    private final GLSimpleListener listener;
    private boolean needsInit;
    private boolean needsReshape;
    private boolean warnOnException;

    public GLSimpleFboListenerEntry( GLSimpleListener listener )
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

    public void dispose( GLContext context )
    {

    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == null ) return false;

        if ( o instanceof GLSimpleFboListenerEntry )
        {
            GLSimpleFboListenerEntry l = ( GLSimpleFboListenerEntry ) o;
            return listener.equals( l.listener );
        }

        return false;
    }
}

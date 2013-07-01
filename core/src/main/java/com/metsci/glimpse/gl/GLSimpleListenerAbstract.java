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


import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;

public abstract class GLSimpleListenerAbstract implements GLSimpleListener
{
    protected boolean isDisposed = false;

    public static class GLSimpleListenerBridge implements GLEventListener
    {
        private final GLSimpleListenerAbstract handler;

        public GLSimpleListenerBridge(GLSimpleListenerAbstract handler)
        {
            this.handler = handler;
        }

        public void init(GLAutoDrawable drawable)
        {
            handler.init(drawable.getContext());
        }

        public void display(GLAutoDrawable drawable)
        {
            handler.display(drawable.getContext());
        }

        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
        {
            handler.reshape(drawable.getContext(), x, y, width, height);
        }

        public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
        {
            handler.displayChanged(drawable.getContext(), modeChanged, deviceChanged);
        }

		@Override
		public void dispose(GLAutoDrawable drawable) {
			// TODO Hmmm. Pretty sure this is correct. --ttran17
			handler.dispose(drawable.getContext());
		}
    }

    public GLEventListener asJoglListener()
    {
        return new GLSimpleListenerBridge(this);
    }

    @Override
    public boolean isDisposed()
    {
        return isDisposed;
    }

    @Override
    public void dispose( GLContext context )
    {
        throw new UnsupportedOperationException( "Unimplemented for Class: " + getClass( ) );
    }

    @Override
    public GLListenerInfo getInfo( )
    {
        throw new UnsupportedOperationException( "Unimplemented for Class:" + getClass( ) );
    }
}

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
package com.metsci.glimpse.context;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

/**
 * GlimpseContext wraps a {@link javax.media.opengl.GLContext} and
 * provides additional contextual information necessary to display a
 * {@link com.metsci.glimpse.painter.base.GlimpsePainter}.
 *
 * @author ulman
 */
public interface GlimpseContext
{
    /**
     * @return a reference to the OpenGL rendering context wrapped by this GlimpseContext
     */
    public GLContext getGLContext( );

    /**
     * @return a convenience method shorthand for the common: {@link #getGLContext( ) getGLContext( )}.{@link javax.media.opengl.GLContext#getGL() getGL( )}
     */
    public GL getGL( );

    /**
     * The {@link GlimpseTargetStack} stack contains the chain of nested
     * {@link GlimpseTarget}s starting at the top level {@link GlimpseTarget}
     * (index 0) and ending at the GlimpseTarget currently being painted to.<p>
     *
     * The top level GlimpseTarget is always an instance of GlimpseCanvas. The
     * other levels are instances of {@link com.metsci.glimpse.layout.GlimpseLayout}.
     */
    public GlimpseTargetStack getTargetStack( );

    /**
     * @return the ratio of pixels per inch for the current context
     */
    public int getDPI( );

    /**
     * Returns the scale factors needed to convert the _native_ pixel coordinates to
     * the _window_ pixel coordinates. This only really matters on displays like like
     * Macbook Retina.
     *
     * @return the scale factors as {scaleX, scaleY}
     */
    public int[] getSurfaceScale( );
}

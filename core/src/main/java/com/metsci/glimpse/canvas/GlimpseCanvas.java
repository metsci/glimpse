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

import javax.media.opengl.GLContext;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.gl.GLRunnable;

/**
 * A heavy weight target for Glimpse rendering. Represents
 * the base component onto which all Glimpse rendering
 * is ultimately performed. GlimpseCanvas provides a facade
 * that hides the implementation details of the specific
 * widget toolkit (Swt or Swing) being used, allowing most
 * Glimpse applications to be easily ported between the two.
 *
 * @author ulman
 */
public interface GlimpseCanvas extends GlimpseTarget
{
    public GLContext getGLContext( );

    public GlimpseContext getGlimpseContext( );

    public GlimpseBounds getTargetBounds( );

    /**
     * Clears the canvas, removing all attached GlimpseLayouts.
     */
    public void removeAllLayouts( );

    /**
     * Lays out any {@link com.metsci.glimpse.layout.GlimpseLayout} instances
     * attached to the GlimpseCanvas and paints all
     * {@link com.metsci.glimpse.painter.base.GlimpsePainter} instances attached
     * to the GlimpseLayouts.
     */
    public void paint( );

    /**
     * Disposes of any native resources of GlimpseLayouts and GlimpsePainters associated with the GlimpseCanvas.
     */
    public void dispose( );

    /**
     *
     * @return whether or not dispose() has been successfully called. Once true,
     *         this GlimpseCanvas is no longer valid for rendering.
     */
    public boolean isDisposed( );
    
    /**
     * Called when the canvas is disposed. Can be used to clean up native resources used by this Canvas.
     */
    public void addDisposeListener( GLRunnable runnable );
}

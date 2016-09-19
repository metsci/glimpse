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
package com.metsci.glimpse.canvas;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLRunnable;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;

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
    public GLProfile getGLProfile( );

    public GLAutoDrawable getGLDrawable( );

    public GLContext getGLContext( );

    public GlimpseContext getGlimpseContext( );

    public GlimpseBounds getTargetBounds( );

    /**
     * Lays out any {@link com.metsci.glimpse.layout.GlimpseLayout} instances
     * attached to the GlimpseCanvas and paints all
     * {@link com.metsci.glimpse.painter.base.GlimpsePainter} instances attached
     * to the GlimpseLayouts.
     */
    public void paint( );

    /**
     * Destroys the native surface which this canvas draws to. Does not dispose of GL resources associated
     * with GlimpsePainters and GlimpseLayouts attached to the GlimpseCanvas (as these may be attached to other
     * GlimpseCanvases as well). Disposing of Glimpse resources can be done via {@code #disposeAttached()}.
     *
     * @see #disposeAttached()
     */
    public void destroy( );

    /**
     * Disposes native resources of GlimpseLayouts and GlimpsePainters associated with the GlimpseCanvas.
     *
     * @see #destroy()
     */
    public void disposeAttached( );

    /**
     * A convenience method which is equivalent to:
     *
     * <code>
     * disposeAttached( );
     * destroy( );
     * </code>
     */
    public void dispose( );

    /**
     * <p>Calls {@link GlimpsePainter#dispose(GlimpseContext)} the next time the GLContext associated with
     * this GlimpseCanvas is active. Generally this call is equivalent to:</p>
     *
     * <code>
     *  this.getGLDrawable( ).invoke( false, new GLRunnable( )
     *  {
     *      @Override
     *      public boolean run( GLAutoDrawable drawable )
     *      {
     *          painter.dispose( getGlimpseContext( ) );
     *          return true;
     *      }
     *  } );
     * </code>
     *
     * <p>The GlimpsePainter should be removed from all GlimpseLayouts via
     * {@link GlimpseLayout#removePainter(GlimpsePainter)} before disposePainter is called. After the GlimpsePainter
     * is disposed, it will no longer be valid for drawing on any GlimpseCanvas.</p>
     */
    public void disposePainter( GlimpsePainter painter );

    /**
     * @return whether or not {@code #dispose()} has been successfully called. Once true, this GlimpseCanvas is no longer valid for rendering.
     */
    public boolean isDestroyed( );

    /**
     * Called when {@link GLEventListener#dispose(GLAutoDrawable)} event is fired by the {@link GLAutoDrawable} associated with the
     * GlimpseCanvas. This can happen for reasons other than the window containing the GlimpseCanvas being closed (for example, moving
     * the window between physical monitors or moving the container between docks in a docking framework). Thus, GlimpsePainters and
     * GlimpseLayouts attached to this GlimpseCanvas should generally not be disposed when this callback occurs.
     */
    public void addDisposeListener( GLRunnable runnable );

    /**
     * Returns the scale factors needed to convert the _native_ pixel coordinates to
     * the _window_ pixel coordinates. This only really matters on displays like like
     * Macbook Retina.
     *
     * @return the scale factors as {scaleX, scaleY}
     */
    public int[] getSurfaceScale( );
}

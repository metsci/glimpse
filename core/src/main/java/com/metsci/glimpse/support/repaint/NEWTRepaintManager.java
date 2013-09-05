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
package com.metsci.glimpse.support.repaint;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLRunnable;

import com.metsci.glimpse.canvas.NewtGlimpseCanvas;

/**
 * A repaint manager for NEWT. Currently experimental: syncExec may not actually block
 * like other RepaintManager implementations.
 * 
 * SwingRepaintManager or RepaintManager will also work. However NEWT decouples
 * GL repainting from the EDT and SwingRepaintManager places the repaint loop on the EDT.
 * 
 * @author ulman
 */
public class NEWTRepaintManager extends RepaintManager
{
    public static NEWTRepaintManager newRepaintManager( NewtGlimpseCanvas canvas )
    {
        NEWTRepaintManager manager = new NEWTRepaintManager( canvas.getGLDrawable( ) );
        manager.addGlimpseCanvas( canvas );
        manager.start( );
        return manager;
    }

    protected GLAutoDrawable drawable;

    public NEWTRepaintManager( GLAutoDrawable drawable )
    {
        this.drawable = drawable;
    }

    public void asyncExec( final Runnable runnable )
    {
        exec( runnable, false );
    }

    public void syncExec( Runnable runnable )
    {
        exec( runnable, true );
    }

    protected void exec( final Runnable runnable, boolean sync )
    {
        // true argument causes invoke to wait until repaint
        // https://projectsforge.org/projects/bundles/browser/trunk/jogl-2.0-rc2/jogl/src/main/java/javax/media/opengl/GLAutoDrawable.java?rev=2
        drawable.invoke( sync, new GLRunnable( )
        {
            // true return value indicates framebuffer remains intact
            // http://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/javax/media/opengl/GLRunnable.html
            @Override
            public boolean run( GLAutoDrawable drawable )
            {
                runnable.run( );
                return true;
            }
        } );
    }
}

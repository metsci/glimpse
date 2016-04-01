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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLRunnable;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.support.settings.LookAndFeel;

public abstract class AbstractGlimpseCanvas implements GlimpseCanvas
{
    protected boolean isEventConsumer = true;
    protected boolean isEventGenerator = true;

    protected LayoutManager layoutManager;

    protected List<GLRunnable> disposeListeners;

    public AbstractGlimpseCanvas( )
    {
        this.layoutManager = new LayoutManager( );
        this.disposeListeners = new CopyOnWriteArrayList<GLRunnable>( );
    }

    @Override
    public boolean isEventConsumer( )
    {
        return isEventConsumer;
    }

    @Override
    public void setEventConsumer( boolean consume )
    {
        this.isEventConsumer = consume;
    }

    @Override
    public boolean isEventGenerator( )
    {
        return isEventGenerator;
    }

    @Override
    public void setEventGenerator( boolean generate )
    {
        this.isEventGenerator = generate;
    }

    @Override
    public void addLayout( GlimpseLayout layout )
    {
        layoutManager.addLayout( layout );
    }

    @Override
    public void addLayout( GlimpseLayout layout, int zOrder )
    {
        layoutManager.addLayout( layout, zOrder );
    }

    @Override
    public void setZOrder( GlimpseLayout layout, int zOrder )
    {
        layoutManager.setZOrder( layout, zOrder );
    }

    @Override
    public void removeLayout( GlimpseLayout layout )
    {
        layoutManager.removeLayout( layout );
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
    public void removeAllLayouts( )
    {
        layoutManager.removeAllLayouts( );
    }

    @Override
    public void addDisposeListener( GLRunnable runnable )
    {
        this.disposeListeners.add( runnable );
    }

    @Override
    public GlimpseContext getGlimpseContext( )
    {
        return new GlimpseContextImpl( this );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        for ( GlimpseTarget target : this.layoutManager.getLayoutList( ) )
        {
            target.setLookAndFeel( laf );
        }
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
        this.getGLDrawable( ).invoke( false, new GLRunnable( )
        {
            @Override
            public boolean run( GLAutoDrawable drawable )
            {
                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.dispose( getGlimpseContext( ) );
                }

                // after layouts are disposed they should not be painted
                // so remove them from the canvas
                removeAllLayouts( );

                return true;
            }
        } );
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
}
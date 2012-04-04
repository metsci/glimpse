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
package com.metsci.glimpse.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseAllListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseMotionListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseWheelListener;
import com.metsci.glimpse.event.mouse.Mouseable;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.base.GlimpsePainterCallback;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * GlimpseLayout provides a means of rendering to specific areas of a GlimpseCanvas
 * controlled by Mig Layout constraints. It also acts as a RenderTarget onto which
 * other GlimpsePainters may be painted. GlimpseLayout satisfies the Glimpse-facing
 * interfaces GlimpsePainter and RenderTarget.<p>
 *
 * GlimpseLayout uses a delegate class {@link GlimpseLayoutDelegate GlimpseLayoutDelegate}
 * to interface with Mig Layout and hold transient state during layout operations.
 * The final results of the layout are stored in a {@link GlimpseLayoutCache LayoutCache}.<p>
 *
 * Don't forget the "bottomtotop" layout constraint for MiG, or things will be
 * upside-down from what you probably expect.
 *
 * @author osborn
 * @author ulman
 * @see GlimpseLayoutDelegate
 */
public class GlimpseLayout implements GlimpsePainter, GlimpseTarget, Mouseable
{
    protected String name = "";

    protected GlimpseLayoutCache<GlimpseBounds> layoutCache;
    protected GlimpseLayoutDelegate layoutDelegate;

    protected List<GlimpseTarget> layoutChildren;
    protected List<GlimpseTarget> unmodifiableLayoutChildren;

    protected ReentrantLock lock = new ReentrantLock( );

    protected Set<GlimpseMouseListener> mouseListeners;
    protected Set<GlimpseMouseMotionListener> mouseMotionListeners;
    protected Set<GlimpseMouseWheelListener> mouseWheelListeners;

    protected Collection<GlimpseMouseListener> mouseListenersUnmodifiable;
    protected Collection<GlimpseMouseMotionListener> mouseMotionListenersUnmodifiable;
    protected Collection<GlimpseMouseWheelListener> mouseWheelListenersUnmodifiable;

    protected boolean isEventGenerator = true;
    protected boolean isEventConsumer = true;
    protected boolean isVisible = true;

    public GlimpseLayout( GlimpseLayout parent, String name )
    {
        this.layoutCache = new GlimpseLayoutCache<GlimpseBounds>( );
        this.layoutDelegate = new GlimpseLayoutDelegate( this );
        this.layoutChildren = new LinkedList<GlimpseTarget>( );
        this.unmodifiableLayoutChildren = Collections.unmodifiableList( layoutChildren );

        this.lock = new ReentrantLock( );

        this.mouseListeners = new HashSet<GlimpseMouseListener>( );
        this.mouseMotionListeners = new HashSet<GlimpseMouseMotionListener>( );
        this.mouseWheelListeners = new HashSet<GlimpseMouseWheelListener>( );

        this.mouseListenersUnmodifiable = Collections.unmodifiableCollection( this.mouseListeners );
        this.mouseMotionListenersUnmodifiable = Collections.unmodifiableCollection( this.mouseMotionListeners );
        this.mouseWheelListenersUnmodifiable = Collections.unmodifiableCollection( this.mouseWheelListeners );

        this.name = name;

        if ( parent != null )
        {
            parent.addLayout( this );
        }
    }

    public GlimpseLayout( GlimpseLayout parent )
    {
        this( parent, null );
    }

    public GlimpseLayout( String name )
    {
        this( null, name );
    }

    public GlimpseLayout( )
    {
        this( null, null );
    }

    public String getName( )
    {
        lock.lock( );
        try
        {
            return name;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setName( String name )
    {
        lock.lock( );
        try
        {
            this.name = name;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setLayoutManager( GlimpseLayoutManager manager )
    {
        lock.lock( );
        try
        {
            layoutDelegate.setLayoutManager( manager );
            invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setLayoutData( Object layoutData )
    {
        lock.lock( );
        try
        {
            layoutDelegate.setLayoutData( layoutData );
            invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removeLayout( GlimpseLayout layout )
    {
        lock.lock( );
        try
        {
            layoutChildren.remove( layout );
            layoutDelegate.removeLayout( layout );
            invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void addLayout( GlimpseLayout layout )
    {
        lock.lock( );
        try
        {
            layoutChildren.add( layout );
            layoutDelegate.addLayout( layout );
            invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void addLayout( GlimpseLayout layout, GlimpsePainterCallback callback )
    {
        lock.lock( );
        try
        {
            layoutChildren.add( layout );
            layoutDelegate.addLayout( layout, callback );
            invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void addPainter( GlimpsePainter painter )
    {
        lock.lock( );
        try
        {
            layoutDelegate.addPainter( painter );
            invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void addPainter( GlimpsePainter painter, GlimpsePainterCallback callback )
    {
        lock.lock( );
        try
        {
            layoutDelegate.addPainter( painter, callback );
            invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removePainter( GlimpsePainter painter )
    {
        lock.lock( );
        try
        {
            layoutDelegate.removePainter( painter );
            invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void invalidateLayout( )
    {
        lock.lock( );
        try
        {
            layoutCache.clear( );
            layoutDelegate.invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public GlimpseBounds layoutTo( GlimpseTargetStack stack )
    {
        lock.lock( );
        try
        {
            // get our cached bounds for the current context
            GlimpseBounds bounds = layoutCache.getValue( stack );

            // if the cache already contains our bounds then we're already laid out
            if ( bounds == null )
            {
                // if the cache doesn't contain our bounds then we take our size from
                // the size of the top GlimpseTarget in the current context
                // (i.e we fill our parent completely)
                bounds = stack.getBounds( );
                layoutCache.setValue( stack, bounds );

                // now that we know our size, lay out our children
                // (and their children, recursively)
                layoutDelegate.layoutTo( stack, bounds );
            }

            return bounds;
        }
        finally
        {
            lock.unlock( );
        }
    }

    // the top of the GlimpseTargetStack is the GlimpseLayout's immediate parent
    public GlimpseBounds layoutTo( GlimpseContext context )
    {
        return layoutTo( context.getTargetStack( ) );
    }

    @Override
    // the top of the GlimpseTargetStack is the GlimpseLayout's immediate parent
    // (the GlimseTarget which we are "painting onto")
    public void paintTo( GlimpseContext context )
    {
        lock.lock( );
        try
        {
            // ensure that we have been laid out properly
            GlimpseBounds bounds = layoutTo( context );
    
            if ( !isVisible ) return;
    
            // push our bounds onto the layout stack
            context.getTargetStack( ).push( this, bounds );
    
            // paint our children with our bounds on top of the layout stack
            layoutDelegate.paintTo( context );
    
            // once our children (and their children recursively) have finished
            // painting remove our bounds from the layout stack
            context.getTargetStack( ).pop( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public GlimpseBounds getTargetBounds( GlimpseTargetStack stack )
    {
        lock.lock( );
        try
        {
            GlimpseBounds bounds = layoutCache.getValue( stack );

            if ( bounds == null )
            {
                return layoutTo( stack );
            }
            else
            {
                return bounds;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public List<GlimpseTarget> getTargetChildren( )
    {
        return unmodifiableLayoutChildren;
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        lock.lock( );
        try
        {
            layoutDelegate.dispose( context );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public boolean isDisposed( )
    {
        lock.lock( );
        try
        {
            return layoutDelegate.isDisposed( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public String toString( )
    {
        return name == null ? super.toString( ) : name;
    }

    @Override
    public Collection<GlimpseMouseListener> getGlimpseMouseListeners( )
    {
        lock.lock( );
        try
        {
            return this.mouseListenersUnmodifiable;
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public Collection<GlimpseMouseMotionListener> getGlimpseMouseMotionListeners( )
    {
        lock.lock( );
        try
        {
            return this.mouseMotionListenersUnmodifiable;
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public Collection<GlimpseMouseWheelListener> getGlimpseMouseWheelListeners( )
    {
        lock.lock( );
        try
        {
            return this.mouseWheelListenersUnmodifiable;
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void addGlimpseMouseListener( GlimpseMouseListener listener )
    {
        lock.lock( );
        try
        {
            this.mouseListeners.add( listener );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void addGlimpseMouseMotionListener( GlimpseMouseMotionListener listener )
    {
        lock.lock( );
        try
        {
            this.mouseMotionListeners.add( listener );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void addGlimpseMouseWheelListener( GlimpseMouseWheelListener listener )
    {
        lock.lock( );
        try
        {
            this.mouseWheelListeners.add( listener );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void addGlimpseMouseAllListener( GlimpseMouseAllListener listener )
    {
        this.addGlimpseMouseListener( listener );
        this.addGlimpseMouseMotionListener( listener );
        this.addGlimpseMouseWheelListener( listener );
    }

    @Override
    public void removeGlimpseMouseAllListener( GlimpseMouseAllListener listener )
    {
        this.removeGlimpseMouseListener( listener );
        this.removeGlimpseMouseMotionListener( listener );
        this.removeGlimpseMouseWheelListener( listener );
    }

    @Override
    public void removeGlimpseMouseListener( GlimpseMouseListener listener )
    {
        lock.lock( );
        try
        {
            this.mouseListeners.remove( listener );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void removeGlimpseMouseMotionListener( GlimpseMouseMotionListener listener )
    {
        lock.lock( );
        try
        {
            this.mouseMotionListeners.remove( listener );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void removeGlimpseMouseWheelListener( GlimpseMouseWheelListener listener )
    {
        lock.lock( );
        try
        {
            this.mouseWheelListeners.remove( listener );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public void removeAllGlimpseListeners( )
    {
        lock.lock( );
        try
        {
            this.mouseListeners.clear( );
            this.mouseWheelListeners.clear( );
            this.mouseMotionListeners.clear( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected void cacheBounds( GlimpseContext context, GlimpseBounds bounds )
    {
        this.layoutCache.setValue( context, bounds );
    }

    protected void cacheBounds( GlimpseTargetStack stack, GlimpseBounds bounds )
    {
        this.layoutCache.setValue( stack, bounds );
    }

    protected GlimpseBounds getBounds( GlimpseContext context )
    {
        return layoutCache.getValue( context );
    }

    protected void preLayout( GlimpseTargetStack stack, GlimpseBounds bounds )
    {
        // do nothing, subclasses may override
    }

    protected void preLayout( GlimpseContext context, GlimpseBounds bounds )
    {
        preLayout( context.getTargetStack( ), bounds );
    }

    public GlimpseLayoutManager getLayoutManager( )
    {
        return getDelegate( ).getLayoutManager( );
    }

    protected GlimpseLayoutDelegate getDelegate( )
    {
        return layoutDelegate;
    }

    @Override
    public void mouseEntered( GlimpseMouseEvent event )
    {
        for ( GlimpseMouseListener listener : mouseListeners )
        {
            listener.mouseEntered( event );
        }
    }

    @Override
    public void mouseExited( GlimpseMouseEvent event )
    {
        for ( GlimpseMouseListener listener : mouseListeners )
        {
            listener.mouseExited( event );
        }
    }

    @Override
    public void mousePressed( GlimpseMouseEvent event )
    {
        for ( GlimpseMouseListener listener : mouseListeners )
        {
            listener.mousePressed( event );
        }
    }

    @Override
    public void mouseReleased( GlimpseMouseEvent event )
    {
        for ( GlimpseMouseListener listener : mouseListeners )
        {
            listener.mouseReleased( event );
        }
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent e )
    {
        for ( GlimpseMouseMotionListener listener : mouseMotionListeners )
        {
            listener.mouseMoved( e );
        }
    }

    @Override
    public void mouseWheelMoved( GlimpseMouseEvent e )
    {
        for ( GlimpseMouseWheelListener listener : mouseWheelListeners )
        {
            listener.mouseWheelMoved( e );
        }
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        lock.lock( );
        try
        {
            for ( GlimpseTarget layout : layoutChildren )
            {
                layout.setLookAndFeel( laf );
            }

            layoutDelegate.setLookAndFeel( laf );
        }
        finally
        {
            lock.unlock( );
        }
    }

    @Override
    public boolean isEventConsumer( )
    {
        return this.isEventConsumer;
    }

    @Override
    public void setEventConsumer( boolean consume )
    {
        this.isEventConsumer = consume;
    }

    @Override
    public boolean isEventGenerator( )
    {
        return this.isEventGenerator;
    }

    @Override
    public void setEventGenerator( boolean generate )
    {
        this.isEventGenerator = generate;
    }

    public void setVisible( boolean visible )
    {
        this.isVisible = visible;
    }

    public boolean isVisible( )
    {
        return this.isVisible;
    }
}

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
package com.metsci.glimpse.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;

import com.metsci.glimpse.canvas.LayoutManager;
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

    // stores true when child GlimpseLayouts have been laid out
    // for a given GlimpseLayoutStack. A null or false value
    // indicates that the children must be laid out again
    protected GlimpseLayoutCache<Boolean> layoutClean;

    // stores the location/bounds of this GlimpseLayout
    // as laid out inside its parent GlimpseLayout for
    // a given GlimpseLayoutStack
    protected GlimpseLayoutCache<GlimpseBounds> layoutCache;

    // delegate class which manages actually laying out
    // child GlimpseLayouts
    protected GlimpseLayoutDelegate layoutDelegate;

    // helper class which handles ordering of GlimpseLayouts
    protected LayoutManager manager;

    // lock controlling access to mutable state of this GlimpseLayout
    protected ReentrantLock lock = new ReentrantLock( );

    // listeners attached to this GlimpseLayout
    protected Set<GlimpseMouseListener> mouseListeners;
    protected Set<GlimpseMouseMotionListener> mouseMotionListeners;
    protected Set<GlimpseMouseWheelListener> mouseWheelListeners;

    // unmodifiable views to the above listeners for passing
    // to external classes
    protected Collection<GlimpseMouseListener> mouseListenersUnmodifiable;
    protected Collection<GlimpseMouseMotionListener> mouseMotionListenersUnmodifiable;
    protected Collection<GlimpseMouseWheelListener> mouseWheelListenersUnmodifiable;

    // flags indicating event handling and repaint behavior
    protected boolean isEventGenerator = true;
    protected boolean isEventConsumer = true;
    protected boolean isVisible = true;

    public GlimpseLayout( GlimpseLayout parent, String name )
    {
        this.layoutClean = new GlimpseLayoutCache<Boolean>( );
        this.layoutCache = new GlimpseLayoutCache<GlimpseBounds>( );
        this.layoutDelegate = new GlimpseLayoutDelegate( this );

        this.manager = new LayoutManager( );

        this.lock = new ReentrantLock( );

        this.mouseListeners = new CopyOnWriteArraySet<GlimpseMouseListener>( );
        this.mouseMotionListeners = new CopyOnWriteArraySet<GlimpseMouseMotionListener>( );
        this.mouseWheelListeners = new CopyOnWriteArraySet<GlimpseMouseWheelListener>( );

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

    public ReentrantLock getLock( )
    {
        return lock;
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

    @Override
    public void removeLayout( GlimpseLayout layout )
    {
        lock.lock( );
        try
        {
            manager.removeLayout( layout );
            layoutDelegate.removeLayout( layout );
            invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    /**
     * Removes all GlimpseLayouts added via {@code #addLayout(GlimpseLayout)}.
     */
    @Override
    public void removeAllLayouts( )
    {
        lock.lock( );
        try
        {
            manager.removeAllLayouts( );
            layoutDelegate.removeAll( );
            invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    /**
     * Historical accident caused removeAll() and removeAllLayouts() to both exist
     * they are both retained for backwards compatibility.
     *
     * @deprecated see {@link #removeAllLayouts()}
     */
    public void removeAll( )
    {
        removeAllLayouts( );
    }

    /**
     * @see {@link #setZOrder(GlimpseLayout, int)}
     */
    @Override
    public void setZOrder( GlimpseLayout layout, int zOrder )
    {
        lock.lock( );
        try
        {
            manager.setZOrder( layout, zOrder );
            layoutDelegate.setZOrder( layout, zOrder );
        }
        finally
        {
            lock.unlock( );
        }
    }

    /**
     * <p>Sets the relative ordering constant for this painter. Painters with low
     * z order will be painter first (in the back) and those with high z order
     * will be painted last (in the front).</p>
     *
     * <p>The value itself has no meaning; it is relative to the z orders
     * of the other painters in the GlimpseLayout.
     * For {@link com.metsci.glimpse.layout.com.metsciGlimpseLayout} instances,
     * the z order also affects the order in which mouse events are delivered to
     * overlapping components.</p>
     *
     * <p>The z order is set to 0 by default. GlimpsePainters with the same z order
     * are painted in the order they were added to the GlimpseLayout. This means the
     * first painters added will be obscured by later painters.</p>
     */
    public void setZOrder( GlimpsePainter painter, int zOrder )
    {
        if ( painter instanceof GlimpseLayout )
        {
            setZOrder( ( GlimpseLayout ) painter, zOrder );
        }
        else
        {
            lock.lock( );
            try
            {
                layoutDelegate.setZOrder( painter, zOrder );
            }
            finally
            {
                lock.unlock( );
            }
        }
    }

    @Override
    public void addLayout( GlimpseLayout layout )
    {
        addLayout( layout, null, 0 );
    }

    public void addLayout( GlimpseLayout layout, GlimpsePainterCallback callback )
    {
        addLayout( layout, callback, 0 );
    }

    @Override
    public void addLayout( GlimpseLayout layout, int zOrder )
    {
        addLayout( layout, null, zOrder );
    }

    public void addLayout( GlimpseLayout layout, GlimpsePainterCallback callback, int zOrder )
    {
        lock.lock( );
        try
        {
            manager.addLayout( layout, zOrder );
            layoutDelegate.addLayout( layout, callback, zOrder );
            invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void addPainter( GlimpsePainter painter )
    {
        addPainter( painter, null, 0 );
    }

    public void addPainter( GlimpsePainter painter, GlimpsePainterCallback callback )
    {
        addPainter( painter, callback, 0 );
    }

    public void addPainter( GlimpsePainter painter, int zOrder )
    {
        addPainter( painter, null, zOrder );
    }

    public void addPainter( GlimpsePainter painter, GlimpsePainterCallback callback, int zOrder )
    {
        if ( painter instanceof GlimpseLayout )
        {
            addLayout( ( GlimpseLayout ) painter, callback, zOrder );
        }
        else
        {
            addPainter0( painter, callback, zOrder );
        }
    }

    protected void addPainter0( GlimpsePainter painter, GlimpsePainterCallback callback, int zOrder )
    {
        lock.lock( );
        try
        {
            layoutDelegate.addPainter( painter, callback, zOrder );
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
            layoutClean.clear( );
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

            // if the cache doesn't contain our bounds then we take our size from
            // the size of the top GlimpseTarget in the current context
            // (i.e we fill our parent completely)
            if ( bounds == null )
            {
                bounds = stack.getBounds( );
                layoutCache.setValue( stack, bounds );
            }

            // now that we know our size, if we are marked as dirty,
            // lay out our children (and their children, recursively)
            if ( isDirty( stack ) )
            {
                layoutDelegate.layoutTo( stack, bounds );
                setDirty( stack, false );
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

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Override
    public List<GlimpseTarget> getTargetChildren( )
    {
        // layoutManager returns an unmodifiable list, thus this cast is typesafe
        // (there is no way for the recipient of the List<GlimpseTarget> view to
        // add GlimpseTargets which are not GlimpseLayouts to the list)
        return ( List ) manager.getLayoutList( );
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
        return this.mouseListenersUnmodifiable;

    }

    @Override
    public Collection<GlimpseMouseMotionListener> getGlimpseMouseMotionListeners( )
    {
        return this.mouseMotionListenersUnmodifiable;

    }

    @Override
    public Collection<GlimpseMouseWheelListener> getGlimpseMouseWheelListeners( )
    {
        return this.mouseWheelListenersUnmodifiable;

    }

    @Override
    public void addGlimpseMouseListener( GlimpseMouseListener listener )
    {
        this.mouseListeners.add( listener );

    }

    @Override
    public void addGlimpseMouseMotionListener( GlimpseMouseMotionListener listener )
    {
        this.mouseMotionListeners.add( listener );

    }

    @Override
    public void addGlimpseMouseWheelListener( GlimpseMouseWheelListener listener )
    {
        this.mouseWheelListeners.add( listener );

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
        this.mouseListeners.remove( listener );

    }

    @Override
    public void removeGlimpseMouseMotionListener( GlimpseMouseMotionListener listener )
    {
        this.mouseMotionListeners.remove( listener );

    }

    @Override
    public void removeGlimpseMouseWheelListener( GlimpseMouseWheelListener listener )
    {
        this.mouseWheelListeners.remove( listener );

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

    protected boolean isDirty( GlimpseTargetStack stack )
    {
        Boolean isClean = layoutClean.getValue( stack );

        return isClean == null || !isClean;
    }

    protected void setDirty( GlimpseTargetStack stack, boolean dirty )
    {
        layoutClean.setValue( stack, !dirty );
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

            if ( event.isHandled( ) ) break;
        }
    }

    @Override
    public void mouseExited( GlimpseMouseEvent event )
    {
        for ( GlimpseMouseListener listener : mouseListeners )
        {
            listener.mouseExited( event );

            if ( event.isHandled( ) ) break;
        }
    }

    @Override
    public void mousePressed( GlimpseMouseEvent event )
    {
        for ( GlimpseMouseListener listener : mouseListeners )
        {
            listener.mousePressed( event );

            if ( event.isHandled( ) ) break;
        }
    }

    @Override
    public void mouseReleased( GlimpseMouseEvent event )
    {
        for ( GlimpseMouseListener listener : mouseListeners )
        {
            listener.mouseReleased( event );

            if ( event.isHandled( ) ) break;
        }
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent event )
    {
        for ( GlimpseMouseMotionListener listener : mouseMotionListeners )
        {
            listener.mouseMoved( event );

            if ( event.isHandled( ) ) break;
        }
    }

    @Override
    public void mouseWheelMoved( GlimpseMouseEvent event )
    {
        for ( GlimpseMouseWheelListener listener : mouseWheelListeners )
        {
            listener.mouseWheelMoved( event );

            if ( event.isHandled( ) ) break;
        }
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        lock.lock( );
        try
        {
            layoutDelegate.setLookAndFeel( laf );
        }
        finally
        {
            lock.unlock( );
        }
    }

    /**
     * Event consumers do not pass on mouse events to their parents.
     */
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

    /**
     * Event generators do not generate mouse events if they are the top layout
     * when a mouse event occurs.
     */
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

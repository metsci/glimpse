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

import static com.metsci.glimpse.context.TargetStackUtil.popTo;
import static com.metsci.glimpse.context.TargetStackUtil.pushToBottom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.metsci.glimpse.axis.listener.touch.AxisGestureListener;
import com.metsci.glimpse.axis.listener.touch.AxisGestureListener1D;
import com.metsci.glimpse.axis.listener.touch.AxisGestureListener2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.touch.GlimpseGestureEvent;
import com.metsci.glimpse.event.touch.GlimpseGestureListener;
import com.metsci.glimpse.event.touch.GlimpseLongPressGestureEvent;
import com.metsci.glimpse.event.touch.GlimpsePanGestureEvent;
import com.metsci.glimpse.event.touch.GlimpsePinchGestureEvent;
import com.metsci.glimpse.event.touch.GlimpseSwipeGestureEvent;
import com.metsci.glimpse.event.touch.GlimpseTapGestureEvent;
import com.metsci.glimpse.event.touch.GlimpseTouchEvent;
import com.metsci.glimpse.event.touch.GlimpseTouchListener;
import com.metsci.glimpse.event.touch.Touchable;
import com.metsci.glimpse.painter.base.GlimpsePainterCallback;

/**
 * Provides a layout wrapper to augment any layout (and descendants) with
 * gesture support.  This wrapper sets EventConsumer and EventGenerator to
 * false for any child layouts.  Then, all the touch events get sent to this
 * layout.  Then this layout will find the actual target and get the gesture
 * listeners associated with that layout and execute them.
 *
 * <p>
 * We may want to preemptively add touch support to all layouts, like we do
 * mouse support. In that case, this class should be removed.
 * </p>
 *
 * @author borkholder
 */
public class GlimpseTouchWrapper extends GlimpseLayout implements Touchable
{
    private static final Logger LOGGER = Logger.getLogger( GlimpseTouchWrapper.class.getName( ) );

    protected Map<GlimpseLayout, List<GlimpseGestureListener>> gestureListeners;

    public GlimpseTouchWrapper( GlimpseLayout parent )
    {
        super( parent );
        gestureListeners = new HashMap<GlimpseLayout, List<GlimpseGestureListener>>( );
    }

    protected void addGestureListenersRecursive( GlimpseLayout layout )
    {
        layout.setEventGenerator( false );
        layout.setEventConsumer( false );

        addGestureListenerVarying( layout );
        for ( GlimpseTarget child : layout.getTargetChildren( ) )
        {
            if ( child instanceof GlimpseLayout )
            {
                addGestureListenersRecursive( ( GlimpseLayout ) child );
            }
        }
    }

    protected void removeGestureListenersRecursive( GlimpseLayout layout )
    {
        gestureListeners.remove( layout );
        for ( GlimpseTarget child : layout.getTargetChildren( ) )
        {
            if ( child instanceof GlimpseLayout )
            {
                removeGestureListenersRecursive( ( GlimpseLayout ) child );
            }
        }
    }

    protected void addGestureListenerVarying( GlimpseLayout layout )
    {
        if ( layout instanceof GlimpseAxisLayoutX )
        {
            addGlimpseGestureListener( createAxisGestureListener1D( true ), layout );
        }
        else if ( layout instanceof GlimpseAxisLayoutY )
        {
            addGlimpseGestureListener( createAxisGestureListener1D( false ), layout );
        }
        else if ( layout instanceof GlimpseAxisLayout2D )
        {
            addGlimpseGestureListener( createAxisGestureListener2D( ), layout );
        }
    }

    protected AxisGestureListener createAxisGestureListener1D( boolean horizontal )
    {
        return new AxisGestureListener1D( );
    }

    protected AxisGestureListener createAxisGestureListener2D( )
    {
        return new AxisGestureListener2D( );
    }

    public void addGlimpseGestureListener( GlimpseGestureListener listener, GlimpseLayout child )
    {
        List<GlimpseGestureListener> list = gestureListeners.get( child );
        if ( list == null )
        {
            list = new CopyOnWriteArrayList<GlimpseGestureListener>( );
            gestureListeners.put( child, list );
        }

        list.add( listener );
    }

    @Override
    public String toString( )
    {
        return getClass( ).getSimpleName( );
    }

    @Override
    public void addGlimpseGestureListener( GlimpseGestureListener listener )
    {
        LOGGER.fine( "Do not add gesture listeners directly, use addGlimpseGestureListener(GlimpseGestureListener, GlimpseLayout)" );
    }

    @Override
    public void addGlimpseTouchListener( GlimpseTouchListener listener )
    {
        LOGGER.finer( "This class does not support touch listeners" );
    }

    protected GlimpseTargetStack getActualTargetStack( GlimpseGestureEvent event )
    {
        GlimpseTargetStack stack = event.getTargetStack( );
        int x = event.getX( ) + stack.getBounds( ).getX( );
        int y = event.getY( ) + stack.getBounds( ).getY( );

        stack = popTo( stack, this );
        if ( stack == null )
        {
            return null;
        }

        stack = pushToBottom( stack, x, y );

        return stack;
    }

    @SuppressWarnings( "unchecked" )
    protected <E extends GlimpseGestureEvent> E convertEvent( E event, GlimpseTargetStack stack )
    {
        int x = event.getX( );
        int y = event.getY( );

        GlimpseBounds bounds = event.getTargetStack( ).getBounds( );
        x += bounds.getX( );
        y += bounds.getY( );

        bounds = stack.getBounds( );
        x -= bounds.getX( );
        y -= bounds.getY( );

        return ( E ) event.withNewTarget( stack, x, y );
    }

    @Override
    public void panDetected( GlimpsePanGestureEvent event )
    {
        GlimpseTargetStack stack = getActualTargetStack( event );
        if ( stack == null )
        {
            return;
        }

        GlimpsePanGestureEvent e = convertEvent( event, stack );
        List<GlimpseGestureListener> listeners = gestureListeners.get( stack.getTarget( ) );
        if ( listeners == null )
        {
            return;
        }

        for ( GlimpseGestureListener listener : listeners )
        {
            listener.panDetected( e );
        }
    }

    @Override
    public void pinchDetected( GlimpsePinchGestureEvent event )
    {
        GlimpseTargetStack stack = getActualTargetStack( event );
        if ( stack == null )
        {
            return;
        }

        GlimpsePinchGestureEvent e = convertEvent( event, stack );
        List<GlimpseGestureListener> listeners = gestureListeners.get( stack.getTarget( ) );
        if ( listeners == null )
        {
            return;
        }

        for ( GlimpseGestureListener listener : listeners )
        {
            listener.pinchDetected( e );
        }
    }

    @Override
    public void tapDetected( GlimpseTapGestureEvent event )
    {
        GlimpseTargetStack stack = getActualTargetStack( event );
        if ( stack == null )
        {
            return;
        }

        GlimpseTapGestureEvent e = convertEvent( event, stack );
        List<GlimpseGestureListener> listeners = gestureListeners.get( stack.getTarget( ) );
        if ( listeners == null )
        {
            return;
        }

        for ( GlimpseGestureListener listener : listeners )
        {
            listener.tapDetected( e );
        }
    }

    @Override
    public void longPressDetected( GlimpseLongPressGestureEvent event )
    {
        GlimpseTargetStack stack = getActualTargetStack( event );
        if ( stack == null )
        {
            return;
        }

        GlimpseLongPressGestureEvent e = convertEvent( event, stack );
        List<GlimpseGestureListener> listeners = gestureListeners.get( stack.getTarget( ) );
        if ( listeners == null )
        {
            return;
        }

        for ( GlimpseGestureListener listener : listeners )
        {
            listener.longPressDetected( e );
        }
    }

    @Override
    public void swipeDetected( GlimpseSwipeGestureEvent event )
    {
        GlimpseTargetStack stack = getActualTargetStack( event );
        if ( stack == null )
        {
            return;
        }

        GlimpseSwipeGestureEvent e = convertEvent( event, stack );
        List<GlimpseGestureListener> listeners = gestureListeners.get( stack.getTarget( ) );
        if ( listeners == null )
        {
            return;
        }

        for ( GlimpseGestureListener listener : listeners )
        {
            listener.swipeDetected( e );
        }
    }

    @Override
    public void touchesBegan( GlimpseTouchEvent event )
    {
        // nop
    }

    @Override
    public void touchesMoved( GlimpseTouchEvent event )
    {
        // nop
    }

    @Override
    public void touchesEnded( GlimpseTouchEvent event )
    {
        // nop
    }

    @Override
    public void removeGlimpseGestureListener( GlimpseGestureListener listener )
    {
        for ( List<GlimpseGestureListener> listeners : gestureListeners.values( ) )
        {
            listeners.remove( listener );
        }
    }

    public void removeGlimpseGestureListeners( GlimpseLayout layout )
    {
        gestureListeners.remove( layout );
    }

    @Override
    public void removeGlimpseTouchListener( GlimpseTouchListener listener )
    {
        // nop
    }

    @Override
    public Collection<GlimpseGestureListener> getGlimpseGestureListeners( )
    {
        List<GlimpseGestureListener> all = new ArrayList<GlimpseGestureListener>( );
        for ( List<GlimpseGestureListener> listeners : gestureListeners.values( ) )
        {
            all.addAll( listeners );
        }

        return Collections.unmodifiableList( all );
    }

    @Override
    public Collection<GlimpseTouchListener> getGlimpseTouchListeners( )
    {
        return Collections.emptyList( );
    }

    @Override
    public void removeAllGlimpseGestureListeners( )
    {
        for ( List<GlimpseGestureListener> listeners : gestureListeners.values( ) )
        {
            listeners.clear( );
        }
    }

    @Override
    public void addLayout( GlimpseLayout layout, GlimpsePainterCallback callback, int zOrder )
    {
        super.addLayout( layout, callback, zOrder );
        addGestureListenersRecursive( layout );
    }

    @Override
    public void removeLayout( GlimpseLayout layout )
    {
        super.removeLayout( layout );
        removeGestureListenersRecursive( layout );
    }

    @Override
    public void removeAll( )
    {
        super.removeAll( );
        gestureListeners.clear( );
    }

    public static GlimpseTouchWrapper wrap( GlimpseLayout parent, GlimpseLayout childAsTouchable )
    {
        GlimpseTouchWrapper wrapper = new GlimpseTouchWrapper( parent );
        wrapper.addLayout( childAsTouchable );
        return wrapper;
    }
}

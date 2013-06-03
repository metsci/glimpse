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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.metsci.glimpse.axis.listener.touch.AxisGestureListener;
import com.metsci.glimpse.axis.listener.touch.AxisGestureListener1D;
import com.metsci.glimpse.axis.listener.touch.AxisGestureListener2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.context.TargetStackUtil;
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
import com.metsci.glimpse.plot.Plot2D;

public class GlimpseTouchPlot2DWrapper extends GlimpseLayout implements Touchable
{
    protected Map<GlimpseLayout, List<GlimpseGestureListener>> gestureListeners;

    protected Plot2D plot2d;

    /**
     * Provided for subclasses which want to set fields before initialize is called (although they are
     * then responsible for calling initialize( )).
     */
    protected GlimpseTouchPlot2DWrapper( )
    {
        gestureListeners = new HashMap<GlimpseLayout, List<GlimpseGestureListener>>( );
    }

    public GlimpseTouchPlot2DWrapper( Plot2D plot2d )
    {
        this( );
        this.plot2d = plot2d;

        initialize( );
        setName( plot2d.getName( ) + "-touchwrapper" );
    }

    protected void initialize( )
    {
        initializeGestureListeners( );
    }

    protected void initializeGestureListeners( )
    {
        GlimpseGestureListener listener = createAxisGestureListenerX( );
        gestureListeners.get( plot2d.getLayoutX( ) ).add( listener );

        listener = createAxisGestureListenerY( );
        gestureListeners.get( plot2d.getLayoutY( ) ).add( listener );

        listener = createAxisGestureListenerZ( );
        gestureListeners.get( plot2d.getLayoutZ( ) ).add( listener );

        listener = createAxisGestureListenerXY( );
        gestureListeners.get( plot2d.getLayoutCenter( ) ).add( listener );
    }

    protected AxisGestureListener createAxisGestureListenerX( )
    {
        return new AxisGestureListener1D( );
    }

    protected AxisGestureListener createAxisGestureListenerY( )
    {
        return new AxisGestureListener1D( );
    }

    protected AxisGestureListener createAxisGestureListenerZ( )
    {
        return new AxisGestureListener1D( );
    }

    protected AxisGestureListener createAxisGestureListenerXY( )
    {
        return new AxisGestureListener2D( );
    }

    protected void addGestureListener( GlimpseGestureListener listener, GlimpseLayout child )
    {
        List<GlimpseGestureListener> list = gestureListeners.get( child );
        if ( list == null )
        {
            list = new CopyOnWriteArrayList<GlimpseGestureListener>( );
            gestureListeners.put( child, list );
        }

        list.add( listener );
    }

    public void addGestureListenerX( GlimpseGestureListener listener )
    {
        addGestureListener( listener, plot2d.getLayoutX( ) );
    }

    public void addGestureListenerY( GlimpseGestureListener listener )
    {
        addGestureListener( listener, plot2d.getLayoutY( ) );
    }

    public void addGestureListenerZ( GlimpseGestureListener listener )
    {
        addGestureListener( listener, plot2d.getLayoutZ( ) );
    }

    public void addGestureListenerCenter( GlimpseGestureListener listener )
    {
        addGestureListener( listener, plot2d.getLayoutCenter( ) );
    }

    @Override
    public String toString( )
    {
        return getClass( ).getSimpleName( );
    }

    @Override
    public void addGlimpseGestureListener( GlimpseGestureListener listener )
    {
        addGestureListenerCenter( listener );
    }

    @Override
    public void addGlimpseTouchListener( GlimpseTouchListener listener )
    {
        // nop
    }

    protected GlimpseTarget getActualTarget( GlimpseGestureEvent event )
    {
        int x = event.getX( );
        int y = event.getY( );

        GlimpseTargetStack copyStack = TargetStackUtil.newTargetStack( event.getTargetStack( ) );

        for ( GlimpseTarget child : plot2d.getTargetChildren( ) )
        {
            copyStack.push( child );

            GlimpseBounds bounds = child.getTargetBounds( copyStack );
            if ( bounds.contains( x, y ) )
            {
                return child;
            }

            copyStack.pop( );
        }

        return null;
    }

    @SuppressWarnings( "unchecked" )
    protected <E extends GlimpseGestureEvent> E convertEvent( E event, GlimpseTarget target )
    {
        int x = event.getX( );
        int y = event.getY( );

        GlimpseTargetStack newStack = TargetStackUtil.newTargetStack( event.getTargetStack( ) );
        newStack.push( target );

        GlimpseBounds bounds = target.getTargetBounds( newStack );
        x -= bounds.getX( );
        y -= bounds.getY( );

        return ( E ) event.withNewTarget( newStack, x, y );
    }

    @Override
    public void panDetected( GlimpsePanGestureEvent event )
    {
        GlimpseTarget target = getActualTarget( event );
        if ( target == null )
        {
            return;
        }

        GlimpsePanGestureEvent e = convertEvent( event, target );
        List<GlimpseGestureListener> listeners = gestureListeners.get( target );
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
        GlimpseTarget target = getActualTarget( event );
        if ( target == null )
        {
            return;
        }

        GlimpsePinchGestureEvent e = convertEvent( event, target );
        List<GlimpseGestureListener> listeners = gestureListeners.get( target );
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
        GlimpseTarget target = getActualTarget( event );
        if ( target == null )
        {
            return;
        }

        GlimpseTapGestureEvent e = convertEvent( event, target );
        List<GlimpseGestureListener> listeners = gestureListeners.get( target );
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
        GlimpseTarget target = getActualTarget( event );
        if ( target == null )
        {
            return;
        }

        GlimpseLongPressGestureEvent e = convertEvent( event, target );
        List<GlimpseGestureListener> listeners = gestureListeners.get( target );
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
        GlimpseTarget target = getActualTarget( event );
        if ( target == null )
        {
            return;
        }

        GlimpseSwipeGestureEvent e = convertEvent( event, target );
        List<GlimpseGestureListener> listeners = gestureListeners.get( target );
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
}

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
import java.util.concurrent.CopyOnWriteArrayList;

import com.metsci.glimpse.event.touch.GlimpseGestureListener;
import com.metsci.glimpse.event.touch.GlimpseLongPressGestureEvent;
import com.metsci.glimpse.event.touch.GlimpsePanGestureEvent;
import com.metsci.glimpse.event.touch.GlimpsePinchGestureEvent;
import com.metsci.glimpse.event.touch.GlimpseSwipeGestureEvent;
import com.metsci.glimpse.event.touch.GlimpseTapGestureEvent;
import com.metsci.glimpse.event.touch.GlimpseTouchEvent;
import com.metsci.glimpse.event.touch.GlimpseTouchListener;
import com.metsci.glimpse.event.touch.Touchable;

public class GlimpseTouchLayout extends GlimpseLayout implements Touchable
{
    protected Collection<GlimpseGestureListener> gestureListeners;
    protected Collection<GlimpseTouchListener> touchListeners;

    public GlimpseTouchLayout( )
    {
        this( null, null );
    }

    public GlimpseTouchLayout( String name )
    {
        this( null, name );
    }

    public GlimpseTouchLayout( GlimpseLayout parent )
    {
        this( parent, null );
    }

    public GlimpseTouchLayout( GlimpseLayout parent, String name )
    {
        super( parent, name );

        this.gestureListeners = new CopyOnWriteArrayList<GlimpseGestureListener>( );
        this.touchListeners = new CopyOnWriteArrayList<GlimpseTouchListener>( );
    }

    @Override
    public void addGlimpseGestureListener( GlimpseGestureListener listener )
    {
        gestureListeners.add( listener );
    }

    @Override
    public void addGlimpseTouchListener( GlimpseTouchListener listener )
    {
        touchListeners.add( listener );
    }

    @Override
    public void panDetected( GlimpsePanGestureEvent event )
    {
        for ( GlimpseGestureListener listener : gestureListeners )
        {
            listener.panDetected( event );
        }
    }

    @Override
    public void pinchDetected( GlimpsePinchGestureEvent event )
    {
        for ( GlimpseGestureListener listener : gestureListeners )
        {
            listener.pinchDetected( event );
        }
    }

    @Override
    public void tapDetected( GlimpseTapGestureEvent event )
    {
        for ( GlimpseGestureListener listener : gestureListeners )
        {
            listener.tapDetected( event );
        }
    }

    @Override
    public void longPressDetected( GlimpseLongPressGestureEvent event )
    {
        for ( GlimpseGestureListener listener : gestureListeners )
        {
            listener.longPressDetected( event );
        }
    }

    @Override
    public void swipeDetected( GlimpseSwipeGestureEvent event )
    {
        for ( GlimpseGestureListener listener : gestureListeners )
        {
            listener.swipeDetected( event );
        }
    }

    @Override
    public void touchesBegan( GlimpseTouchEvent event )
    {
        for ( GlimpseTouchListener listener : touchListeners )
        {
            listener.touchesBegan( event );
        }
    }

    @Override
    public void touchesMoved( GlimpseTouchEvent event )
    {
        for ( GlimpseTouchListener listener : touchListeners )
        {
            listener.touchesMoved( event );
        }
    }

    @Override
    public void touchesEnded( GlimpseTouchEvent event )
    {
        for ( GlimpseTouchListener listener : touchListeners )
        {
            listener.touchesEnded( event );
        }
    }

    @Override
    public void removeGlimpseGestureListener( GlimpseGestureListener listener )
    {
        gestureListeners.remove( listener );
    }

    @Override
    public void removeGlimpseTouchListener( GlimpseTouchListener listener )
    {
        touchListeners.remove( listener );
    }

    @Override
    public Collection<GlimpseGestureListener> getGlimpseGestureListeners( )
    {
        return Collections.unmodifiableCollection( gestureListeners );
    }

    @Override
    public Collection<GlimpseTouchListener> getGlimpseTouchListeners( )
    {
        return Collections.unmodifiableCollection( touchListeners );
    }

    @Override
    public void removeAllGlimpseGestureListeners( )
    {
        touchListeners.clear( );
        gestureListeners.clear( );
    }
}

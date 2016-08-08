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

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JScrollBar;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;

import net.miginfocom.layout.ComponentWrapper;

/**
 * A GlimpseLayout that shifts its layout-children up or down based on a vertical-
 * offset field, which can be set by calling the {@link #setVerticalOffset(int)}
 * method.
 *
 * For an example of controlling the vertical-offset using a Swing scrollbar, see
 * {@link com.metsci.glimpse.examples.layout.VerticallyScrollableLayoutExample}.
 *
 * In typical usage, when this layout's container is taller than minContentHeight,
 * the child-layouts will not be scrollable -- this layout will size its child-
 * layouts to fit the container.
 *
 * However, when this layout's container is shorter than minContentHeight, it will
 * set child-layout heights to minContentHeight -- and verticalOffset then affects
 * what portion of the content fits inside the container's bounds.
 *
 * @author hogye
 */
public class GlimpseVerticallyScrollableLayout extends GlimpseLayout
{

    /**
     * Returns a Runnable that, if called, will detach the layout from the scrollbar,
     * removing all of the listeners put in place by the attach call.
     *
     * If you are doing all Glimpse stuff on the Swing thread, then this function causes
     * no threading concerns.
     *
     * If you have multiple UI threads, you can generally rule out either deadlock or race
     * conditions, but not both. This function rules out deadlock, so it must leave the door
     * open to race conditions -- use at your own risk.
     *
     */
    public static Runnable attachScrollableToScrollbar( final GlimpseVerticallyScrollableLayout layout, final GlimpseTargetStack stack, final JScrollBar scrollbar )
    {
        final Runnable layoutListener = new Runnable( )
        {
            public void run( )
            {
                int layoutHeight;
                int minContentHeight;
                int verticalOffset;

                layout.getLock( ).lock( );
                try
                {
                    layoutHeight = layout.getCurrentBounds( stack ).getHeight( );
                    minContentHeight = layout.getMinContentHeight( );
                    verticalOffset = layout.getVerticalOffset( );
                }
                finally
                {
                    layout.getLock( ).unlock( );
                }

                int extent = layoutHeight;
                int min = 0;
                int max = max( minContentHeight, extent );
                int value = min( verticalOffset, max - extent );

                scrollbar.setValues( value, extent, min, max );
                scrollbar.repaint( );
            }
        };

        final AdjustmentListener scrollbarListener = new AdjustmentListener( )
        {
            public void adjustmentValueChanged( AdjustmentEvent ev )
            {
                layout.setVerticalOffset( scrollbar.getValue( ) );
            }
        };

        // Attach listeners
        layout.addListener( true, layoutListener );
        scrollbar.addAdjustmentListener( scrollbarListener );

        // Return a way to detach listeners later
        return new Runnable( )
        {
            public void run( )
            {
                layout.removeListener( layoutListener );
                scrollbar.removeAdjustmentListener( scrollbarListener );
            }
        };
    }

    protected int minContentHeight;
    protected int verticalOffset;

    // We rely on copy-on-write iteration semantics, so don't just declare as List
    protected final CopyOnWriteArrayList<Runnable> listeners;

    public GlimpseVerticallyScrollableLayout( int minContentHeight )
    {
        this.minContentHeight = minContentHeight;
        this.verticalOffset = 0;
        this.listeners = new CopyOnWriteArrayList<>( );

        setLayoutManager( new GlimpseLayoutManager( )
        {
            public void layout( GlimpseLayoutDelegate parent )
            {
                int left = parent.getX( );
                int contentWidth = parent.getWidth( );

                int minContentHeight = GlimpseVerticallyScrollableLayout.this.minContentHeight;
                int contentHeight = max( minContentHeight, parent.getHeight( ) + verticalOffset );
                int top = parent.getY( ) + parent.getHeight( ) + verticalOffset;
                int bottom = top - contentHeight;

                for ( ComponentWrapper child : parent.getComponents( ) )
                {
                    child.setBounds( left, bottom, contentWidth, contentHeight );
                }
            }
        } );
    }

    public void addListener( boolean runImmediately, Runnable listener )
    {
        if ( runImmediately )
        {
            listener.run( );
        }

        listeners.add( listener );
    }

    public void removeListener( Runnable listener )
    {
        listeners.remove( listener );
    }

    protected void notifyListeners( )
    {
        for ( Runnable listener : listeners )
        {
            listener.run( );
        }
    }

    @Override
    public GlimpseBounds layoutTo( GlimpseTargetStack stack )
    {
        lock.lock( );
        try
        {
            boolean wasDirty = isDirty( stack );

            GlimpseBounds result = super.layoutTo( stack );

            if ( wasDirty )
            {
                notifyListeners( );
            }

            return result;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public int getVerticalOffset( )
    {
        lock.lock( );
        try
        {
            return verticalOffset;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setVerticalOffset( int verticalOffset )
    {
        lock.lock( );
        try
        {
            if ( verticalOffset != this.verticalOffset )
            {
                this.verticalOffset = verticalOffset;
                invalidateLayout( );
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    public int getMinContentHeight( )
    {
        lock.lock( );
        try
        {
            return minContentHeight;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setMinContentHeight( int minContentHeight )
    {
        lock.lock( );
        try
        {
            if ( minContentHeight != this.minContentHeight )
            {
                this.minContentHeight = minContentHeight;
                invalidateLayout( );
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    public GlimpseBounds getCurrentBounds( GlimpseTargetStack stack )
    {
        lock.lock( );
        try
        {
            GlimpseBounds bounds = layoutCache.getValueNoBoundsCheck( stack );

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

}

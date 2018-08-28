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
package com.metsci.glimpse.docking;

import java.awt.Component;
import java.util.Collection;

import com.metsci.glimpse.util.var.Disposable;

public class DockingGroupListenerUtils
{

    public static void notifyAddedView( Collection<? extends DockingGroupListener> listeners, Tile tile, View view )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.addedView( tile, view );
        }
    }

    public static void notifyRemovedView( Collection<? extends DockingGroupListener> listeners, Tile tile, View view )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.removedView( tile, view );
        }
    }

    public static void notifySelectedView( Collection<? extends DockingGroupListener> listeners, Tile tile, View view )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.selectedView( tile, view );
        }
    }

    public static void notifyAddedLeaf( Collection<? extends DockingGroupListener> listeners, MultiSplitPane docker, Component leaf )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.addedLeaf( docker, leaf );
        }
    }

    public static void notifyRemovedLeaf( Collection<? extends DockingGroupListener> listeners, MultiSplitPane docker, Component leaf )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.removedLeaf( docker, leaf );
        }
    }

    public static void notifyMovedDivider( Collection<? extends DockingGroupListener> listeners, MultiSplitPane docker, SplitPane splitPane )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.movedDivider( docker, splitPane );
        }
    }

    public static void notifyMaximizedLeaf( Collection<? extends DockingGroupListener> listeners, MultiSplitPane docker, Component leaf )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.maximizedLeaf( docker, leaf );
        }
    }

    public static void notifyUnmaximizedLeaf( Collection<? extends DockingGroupListener> listeners, MultiSplitPane docker, Component leaf )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.unmaximizedLeaf( docker, leaf );
        }
    }

    public static void notifyRestoredTree( Collection<? extends DockingGroupListener> listeners, MultiSplitPane docker )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.restoredTree( docker );
        }
    }

    public static void notifyAddedFrame( Collection<? extends DockingGroupListener> listeners, DockingGroup group, DockingFrame frame )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.addedFrame( group, frame );
        }
    }

    public static void notifyUserRequestingDisposeFrame( Collection<? extends DockingGroupListener> listeners, DockingGroup group, DockingFrame frame )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.userRequestingDisposeFrame( group, frame );
        }
    }

    public static void notifyDisposingAllFrames( Collection<? extends DockingGroupListener> listeners, DockingGroup group )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.disposingAllFrames( group );
        }
    }

    public static void notifyDisposingFrame( Collection<? extends DockingGroupListener> listeners, DockingGroup group, DockingFrame frame )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.disposingFrame( group, frame );
        }
    }

    public static void notifyDisposedFrame( Collection<? extends DockingGroupListener> listeners, DockingGroup group, DockingFrame frame )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.disposedFrame( group, frame );
        }
    }

    public static void notifyUserRequestingCloseView( Collection<? extends DockingGroupListener> listeners, DockingGroup group, View view )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.userRequestingCloseView( group, view );
        }
    }

    public static void notifyClosingViews( Collection<? extends DockingGroupListener> listeners, DockingGroup group, Collection<? extends View> views )
    {
        for ( View view : views )
        {
            notifyClosingView( listeners, group, view );
        }
    }

    public static void notifyClosingView( Collection<? extends DockingGroupListener> listeners, DockingGroup group, View view )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.closingView( group, view );
        }
    }

    public static void notifyClosedViews( Collection<? extends DockingGroupListener> listeners, DockingGroup group, Collection<? extends View> views )
    {
        for ( View view : views )
        {
            notifyClosedView( listeners, group, view );
        }
    }

    public static void notifyClosedView( Collection<? extends DockingGroupListener> listeners, DockingGroup group, View view )
    {
        for ( DockingGroupListener listener : listeners )
        {
            listener.closedView( group, view );
        }
    }

    public static Disposable attachDockerListener( MultiSplitPane docker, MultiSplitPaneListener listener )
    {
        docker.addListener( listener );

        return ( ) ->
        {
            docker.removeListener( listener );
        };
    }

    public static Disposable attachMulticastDockerListener( MultiSplitPane docker, Collection<? extends DockingGroupListener> listeners )
    {
        return attachDockerListener( docker, new MultiSplitPaneListener( )
        {
            @Override
            public void addedLeaf( Component leaf )
            {
                notifyAddedLeaf( listeners, docker, leaf );
            }

            @Override
            public void removedLeaf( Component leaf )
            {
                notifyRemovedLeaf( listeners, docker, leaf );
            }

            @Override
            public void movedDivider( SplitPane splitPane )
            {
                notifyMovedDivider( listeners, docker, splitPane );
            }

            @Override
            public void maximizedLeaf( Component leaf )
            {
                notifyMaximizedLeaf( listeners, docker, leaf );
            }

            @Override
            public void unmaximizedLeaf( Component leaf )
            {
                notifyUnmaximizedLeaf( listeners, docker, leaf );
            }

            @Override
            public void restoredTree( )
            {
                notifyRestoredTree( listeners, docker );
            }
        } );
    }

    public static Disposable attachTileListener( Tile tile, TileListener listener )
    {
        tile.addListener( listener );

        return ( ) ->
        {
            tile.removeListener( listener );
        };
    }

    public static Disposable attachMulticastTileListener( Tile tile, Collection<? extends DockingGroupListener> listeners )
    {
        return attachTileListener( tile, new TileListener( )
        {
            @Override
            public void addedView( View view )
            {
                notifyAddedView( listeners, tile, view );
            }

            @Override
            public void removedView( View view )
            {
                notifyRemovedView( listeners, tile, view );
            }

            @Override
            public void selectedView( View view )
            {
                notifySelectedView( listeners, tile, view );
            }
        } );
    }

}

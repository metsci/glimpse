/*
 * Copyright (c) 2020, Metron, Inc.
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

import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.DisposableGroup;

public class DockingWindowTitler extends DockingGroupAdapter
{

    protected final Function<DockingWindow,String> titleFn;
    protected final Map<View,Disposable> viewDisposables;


    public DockingWindowTitler( Function<DockingWindow,String> titleFn )
    {
        this.titleFn = titleFn;
        this.viewDisposables = new HashMap<>( );
    }

    public void updateWindowTitle( DockingWindow window )
    {
        if ( window != null )
        {
            String title = this.titleFn.apply( window );
            window.setTitle( title );
        }
    }

    @Override
    public void addedView( Tile tile, View view )
    {
        Runnable updateWindowTitle = ( ) ->
        {
            this.updateWindowTitle( getAncestorOfClass( DockingWindow.class, tile ) );
        };

        updateWindowTitle.run( );

        DisposableGroup disposables = new DisposableGroup( );
        disposables.add( view.component.addListener( false, updateWindowTitle ) );
        disposables.add( view.tooltip.addListener( false, updateWindowTitle ) );
        disposables.add( view.title.addListener( false, updateWindowTitle ) );
        disposables.add( view.icon.addListener( false, updateWindowTitle ) );

        this.viewDisposables.put( view, disposables );
    }

    @Override
    public void removedView( Tile tile, View view )
    {
        this.updateWindowTitle( getAncestorOfClass( DockingWindow.class, tile ) );
        this.viewDisposables.remove( view ).dispose( );
    }

    @Override
    public void selectedView( Tile tile, View view )
    {
        this.updateWindowTitle( getAncestorOfClass( DockingWindow.class, tile ) );
    }

    @Override
    public void addedLeaf( MultiSplitPane docker, Component leaf )
    {
        this.updateWindowTitle( getAncestorOfClass( DockingWindow.class, docker ) );
    }

    @Override
    public void removedLeaf( MultiSplitPane docker, Component leaf )
    {
        this.updateWindowTitle( getAncestorOfClass( DockingWindow.class, docker ) );
    }

    @Override
    public void movedDivider( MultiSplitPane docker, SplitPane splitPane )
    {
        this.updateWindowTitle( getAncestorOfClass( DockingWindow.class, docker ) );
    }

    @Override
    public void maximizedLeaf( MultiSplitPane docker, Component leaf )
    {
        this.updateWindowTitle( getAncestorOfClass( DockingWindow.class, docker ) );
    }

    @Override
    public void unmaximizedLeaf( MultiSplitPane docker, Component leaf )
    {
        this.updateWindowTitle( getAncestorOfClass( DockingWindow.class, docker ) );
    }

    @Override
    public void restoredTree( MultiSplitPane docker )
    {
        this.updateWindowTitle( getAncestorOfClass( DockingWindow.class, docker ) );
    }

    @Override
    public void addedWindow( DockingGroup group, DockingWindow window )
    {
        this.updateWindowTitle( window );
    }

}

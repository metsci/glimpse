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
package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingUtils.*;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.util.Map;

import javax.swing.JTabbedPane;

public class BasicTile extends JTabbedPane implements Tile
{

    protected final Map<Component,View> viewsByComponent;


    public BasicTile( )
    {
        this.viewsByComponent = newHashMap( );
    }

    @Override
    public int numViews( )
    {
        return getTabCount( );
    }

    @Override
    public void addView( View view, int viewNum )
    {
        if ( viewsByComponent.containsKey( view.component ) ) throw new RuntimeException( "View already exists in this tile: view-id = " + view.viewKey.viewId );

        viewsByComponent.put( view.component, view );
        insertTab( view.title, view.icon, view.component, view.tooltip, viewNum );
    }

    @Override
    public void removeView( View view )
    {
        if ( !viewsByComponent.containsKey( view.component ) ) throw new RuntimeException( "View does not exist in this tile: view-id = " + view.viewKey.viewId );

        viewsByComponent.remove( view.component );
        remove( view.component );
    }

    @Override
    public View view( int viewNum )
    {
        return viewsByComponent.get( getComponentAt( viewNum ) );
    }

    @Override
    public void selectView( View view )
    {
        setSelectedComponent( view.component );
    }

    @Override
    public View selectedView( )
    {
        return viewsByComponent.get( getSelectedComponent( ) );
    }

    @Override
    public int viewNumForTabAt( int x, int y )
    {
        return getUI( ).tabForCoordinate( this, x, y );
    }

    @Override
    public Rectangle viewTabBounds( int viewNum )
    {
        return getBoundsAt( viewNum );
    }

    @Override
    public void addDockingMouseAdapter( MouseAdapter mouseAdapter )
    {
        addMouseListener( mouseAdapter );
        addMouseMotionListener( mouseAdapter );
    }

}

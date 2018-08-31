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
package com.metsci.glimpse.docking.group;

import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyClosedView;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyClosingView;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.metsci.glimpse.docking.DockingFrameCloseOperation;
import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingGroupListener;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.LandingIndicator;
import com.metsci.glimpse.docking.LandingRegions.LandingRegion;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.TileFactory;
import com.metsci.glimpse.docking.TileFactoryStandard;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.util.var.Disposable;

public abstract class DockingGroupBase implements DockingGroup
{

    protected final DockingTheme theme;
    protected final DockingFrameCloseOperation frameCloseOperation;
    protected final TileFactory tileFactory;

    protected final LandingIndicator landingIndicator;

    protected final Set<DockingGroupListener> listeners;
    protected final Set<DockingGroupListener> listenersUnmod;


    public DockingGroupBase( DockingFrameCloseOperation frameCloseOperation, DockingTheme theme )
    {
        this.theme = theme;
        this.frameCloseOperation = frameCloseOperation;
        this.tileFactory = new TileFactoryStandard( this );

        this.landingIndicator = new LandingIndicator( this.theme );

        this.listeners = new LinkedHashSet<>( );
        this.listenersUnmod = unmodifiableSet( this.listeners );
    }

    @Override
    public DockingTheme theme( )
    {
        return this.theme;
    }

    public Collection<? extends DockingGroupListener> listeners( )
    {
        return this.listenersUnmod;
    }

    @Override
    public Disposable addListener( DockingGroupListener listener )
    {
        this.listeners.add( listener );

        return ( ) ->
        {
            this.removeListener( listener );
        };
    }

    @Override
    public void removeListener( DockingGroupListener listener )
    {
        this.listeners.remove( listener );
    }

    @Override
    public void addView( View view )
    {
        this.addViews( view );
    }

    @Override
    public void addViews( View... views )
    {
        this.addViews( asList( views ) );
    }

    /**
     * Returns a {@link GroupArrangement} that reflects only existing components,
     * <em>not</em> the planned arrangement of potential future components.
     * <p>
     * If {@code componentsMap} is non-null, it will be populated with mappings
     * from {@link DockerArrangementNode}s to corresponding {@link Component}s.
     */
    public abstract GroupArrangement existingArrangement( Map<DockerArrangementNode,Component> componentsMap );

    public void onDragStarting( Tile fromTile )
    {
        // Do nothing by default
    }

    public abstract LandingRegion findLandingRegion( Tile fromTile, int fromViewNum, Point pOnScreen );

    public void setLandingIndicator( Rectangle bounds )
    {
        this.landingIndicator.setBounds( bounds );
    }

    public Tile createNewTile( )
    {
        return this.tileFactory.newTile( );
    }

    @Override
    public void closeView( View view )
    {
        Tile tile = getAncestorOfClass( Tile.class, view.component.v( ) );
        if ( tile == null )
        {
            throw new RuntimeException( "View does not belong to this docking-group: view-id = " + view.viewId );
        }

        notifyClosingView( this.listeners, this, view );

        tile.removeView( view );
        this.pruneEmptyTile( tile );

        notifyClosedView( this.listeners, this, view );
    }

    public abstract void pruneEmptyTile( Tile tile );

}

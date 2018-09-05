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

import static com.metsci.glimpse.docking.DockingUtils.allViewsAreAutoCloseable;
import static com.metsci.glimpse.docking.DockingUtils.findViews;
import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;
import static com.metsci.glimpse.docking.MiscUtils.reversed;
import static com.metsci.glimpse.docking.group.ArrangementUtils.removeView;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.attachMulticastDockerListener;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyAddedWindow;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyClosedView;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyClosedViews;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyClosingView;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyClosingViews;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyDisposedWindow;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyDisposingAllWindows;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyDisposingWindow;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyUserRequestingDisposeWindow;
import static com.metsci.glimpse.docking.group.DockingGroupUtils.newWindowsBackToFront;
import static com.metsci.glimpse.docking.group.DockingGroupUtils.restoreMaximizedTilesInNewWindows;
import static com.metsci.glimpse.docking.group.DockingGroupUtils.restoreSelectedViewsInNewTiles;
import static com.metsci.glimpse.docking.group.DockingGroupUtils.toArrNode;
import static com.metsci.glimpse.docking.group.ViewPlacementUtils.futureViewIds;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.metsci.glimpse.docking.DockingFrameCloseOperation;
import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingGroupAdapter;
import com.metsci.glimpse.docking.DockingGroupListener;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.DockingWindow;
import com.metsci.glimpse.docking.LandingIndicator;
import com.metsci.glimpse.docking.LandingRegions.LandingRegion;
import com.metsci.glimpse.docking.MultiSplitPane;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.TileFactory;
import com.metsci.glimpse.docking.TileFactoryStandard;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.util.var.Disposable;

public abstract class DockingGroupBase implements DockingGroup
{
    private static final Logger logger = Logger.getLogger( DockingGroupBase.class.getName( ) );


    protected final DockingTheme theme;
    protected final DockingFrameCloseOperation windowCloseOperation;
    protected final TileFactory tileFactory;

    protected boolean isVisible;
    protected final List<DockingWindow> windows;
    protected GroupArrangement planArr;

    protected final LandingIndicator landingIndicator;

    protected final Set<DockingGroupListener> listeners;
    protected final Set<DockingGroupListener> listenersUnmod;


    public DockingGroupBase( DockingFrameCloseOperation windowCloseOperation, DockingTheme theme )
    {
        this.theme = theme;
        this.windowCloseOperation = windowCloseOperation;
        this.tileFactory = new TileFactoryStandard( this );

        this.isVisible = false;
        this.windows = new ArrayList<>( );
        this.planArr = new GroupArrangement( );

        this.landingIndicator = new LandingIndicator( this.theme );

        this.listeners = new LinkedHashSet<>( );
        this.listenersUnmod = unmodifiableSet( this.listeners );

        this.addListener( new DockingGroupAdapter( )
        {
            @Override
            public void removedView( Tile tile, View view )
            {
                DockingGroupBase.this.pruneEmptyTile( tile );
            }

            @Override
            public void removedLeaf( MultiSplitPane docker, Component leaf )
            {
                DockingGroupBase.this.pruneEmptyWindow( docker );
            }
        } );
    }

    protected void pruneEmptyTile( Tile tile )
    {
        if ( tile.numViews( ) == 0 )
        {
            MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, tile );
            docker.removeLeaf( tile );
        }
    }

    protected void pruneEmptyWindow( MultiSplitPane docker )
    {
        if ( docker.numLeaves( ) == 0 )
        {
            DockingWindow window = getAncestorOfClass( DockingWindow.class, docker );
            if ( window != null )
            {
                notifyDisposingWindow( this.listeners, this, window );
                if ( window.getContentPane( ) == docker )
                {
                    window.dispose( );
                }

                // The WINDOW_CLOSED event only gets sent if the window was visible
                this.removeWindow( window );
            }
        }
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

    protected MultiSplitPane createDocker( )
    {
        MultiSplitPane docker = new MultiSplitPane( this.theme.dividerSize );
        attachMulticastDockerListener( docker, this.listeners );
        return docker;
    }

    @Override
    public List<? extends DockingWindow> windows( )
    {
        return unmodifiableList( this.windows );
    }

    protected <W extends DockingWindow> W addWindow( W window )
    {
        window.setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        window.addWindowListener( new WindowAdapter( )
        {
            @Override
            public void windowActivated( WindowEvent ev )
            {
                // Assumes windows get raised when activated -- not always true (e.g. with
                // a focus-follows-mouse WM), but it's as close as we can get with Swing
                DockingGroupBase.this.recordWindowRaise( window );
            }

            @Override
            public void windowClosing( WindowEvent ev )
            {
                DockingGroupBase.this.onWindowCloseButton( window );
            }

            @Override
            public void windowClosed( WindowEvent ev )
            {
                DockingGroupBase.this.removeWindow( window );
            }
        } );

        this.windows.add( 0, window );
        notifyAddedWindow( this.listeners, this, window );
        return window;
    }

    protected void recordWindowRaise( DockingWindow window )
    {
        boolean found = this.windows.remove( window );
        if ( !found )
        {
            throw new RuntimeException( "Window does not belong to this docking-group" );
        }

        this.windows.add( 0, window );
    }

    protected void onWindowCloseButton( DockingWindow window )
    {
        if ( this.windows.contains( window ) )
        {
            notifyUserRequestingDisposeWindow( this.listeners, this, window );

            switch ( this.windowCloseOperation )
            {
                case DO_NOTHING:
                    // Do nothing
                    break;

                case DISPOSE_CLOSED_FRAME:
                    Collection<View> views = this.views( ).values( );
                    if ( allViewsAreAutoCloseable( views ) )
                    {
                        notifyDisposingWindow( this.listeners, this, window );
                        notifyClosingViews( this.listeners, this, views );
                        window.dispose( );
                        notifyClosedViews( this.listeners, this, views );

                        // This would get triggered eventually by the WINDOW_CLOSED event, but not until
                        // after previously queued events have been dispatched -- which can cause problems,
                        // especially when there are modal dialogs involved
                        this.removeWindow( window );
                    }
                    else
                    {
                        logger.warning( "Refusing to dispose window, because it contains at least one view that is not auto-closeable" );
                    }
                    break;

                case DISPOSE_ALL_FRAMES:
                    this.dispose( );
                    break;

                case EXIT_JVM:
                    this.dispose( );
                    System.exit( 0 );
                    break;
            }
        }
    }

    protected void removeWindow( DockingWindow window )
    {
        if ( this.windows.remove( window ) )
        {
            notifyDisposedWindow( this.listeners, this, window );

            if ( this.windows.isEmpty( ) )
            {
                // Dispose the landingIndicator window, so that the JVM can shut down if
                // appropriate -- if the landingIndicator is needed again (e.g. after a
                // new window is added to the group), it will be automatically resurrected
                this.landingIndicator.dispose( );
            }
        }
    }

    @Override
    public void addViewPlacement( String viewId, ViewPlacementRule rule )
    {
        Map<String,View> existingViews = this.views( );
        View view = existingViews.remove( viewId );
        if ( view != null )
        {
            this.closeView( view );
        }

        GroupArrangement newPlanArr = this.captureArrangement( );
        removeView( newPlanArr, viewId );

        Set<String> existingViewIds = existingViews.keySet( );
        ViewPlacer<Void> placer = this.createViewPlacer( newPlanArr, viewId );
        rule.placeView( newPlanArr, existingViewIds, placer );

        this.planArr = newPlanArr;

        if ( view != null )
        {
            this.addView( view );
        }
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

    @Override
    public void addViews( Collection<View> views )
    {
        Collection<ViewDestination> viewDestinations = new ArrayList<>( );
        for ( View view : views )
        {
            Map<DockerArrangementNode,Component> existingComponents = new LinkedHashMap<>( );
            GroupArrangement existingArr = this.getExistingArr( existingComponents );
            ViewPlacer<ViewDestination> placer = this.createViewPlacer( existingComponents, view );
            ViewDestination destination = this.placeView( existingArr, this.planArr, view.viewId, placer );
            viewDestinations.add( destination );
        }

        restoreSelectedViewsInNewTiles( viewDestinations );
        restoreMaximizedTilesInNewWindows( viewDestinations );

        for ( DockingWindow window : newWindowsBackToFront( viewDestinations, this.planArr ) )
        {
            if ( this.isVisible )
            {
                // Triggers this.recordWindowRaise() automatically
                window.setVisible( true );
            }
            else
            {
                this.recordWindowRaise( window );
            }
        }
    }

    @Override
    public Map<String,View> views( )
    {
        return findViews( this.windows );
    }

    @Override
    public void setArrangement( GroupArrangement groupArr )
    {
        boolean wasVisible = this.isVisible;
        this.setVisible( false );

        Collection<View> views = this.views( ).values( );
        for ( View view : views )
        {
            this.closeView( view );
        }

        this.planArr = groupArr;

        this.addViews( views );

        this.setVisible( wasVisible );
    }

    @Override
    public GroupArrangement captureArrangement( )
    {
        GroupArrangement newPlanArr = this.getExistingArr( null );
        for ( String futureViewId : futureViewIds( this, this.planArr ) )
        {
            ViewPlacer<Void> placer = this.createViewPlacer( newPlanArr, futureViewId );
            this.placeView( newPlanArr, this.planArr, futureViewId, placer );
        }
        return newPlanArr;
    }

    /**
     * Returns a {@link GroupArrangement} that reflects only existing components,
     * <em>not</em> the planned arrangement of potential future components.
     * <p>
     * If {@code existingComponents} is non-null, it will be populated with mappings
     * from {@link DockerArrangementNode}s to corresponding {@link Component}s.
     */
    protected GroupArrangement getExistingArr( Map<DockerArrangementNode,Component> existingComponents )
    {
        GroupArrangement groupArr = new GroupArrangement( );
        for ( DockingWindow window : this.windows )
        {
            FrameArrangement frameArr = new FrameArrangement( );

            Rectangle bounds = window.getNormalBounds( );
            frameArr.x = bounds.x;
            frameArr.y = bounds.y;
            frameArr.width = bounds.width;
            frameArr.height = bounds.height;

            frameArr.isMaximizedHoriz = window.isMaximizedHorizontally( );
            frameArr.isMaximizedVert = window.isMaximizedVertically( );

            frameArr.dockerArr = toArrNode( window.docker( ).snapshot( ), existingComponents );
            groupArr.frameArrs.add( frameArr );
        }
        return groupArr;
    }

    protected abstract ViewPlacer<Void> createViewPlacer( GroupArrangement groupArr, String newViewId );

    protected abstract ViewPlacer<ViewDestination> createViewPlacer( Map<DockerArrangementNode,Component> existingComponents, View newView );

    /**
     * The {@code viewPlacer} arg is guaranteed to have been created by one of this
     * instance's {@code createViewPlacer()} methods. Casting it is safe as long as
     * the cast is compatible with the types returned by both methods.
     */
    protected abstract <T> T placeView( GroupArrangement existingArr, GroupArrangement planArr, String viewId, ViewPlacer<T> viewPlacer );

    @Override
    public void setVisible( boolean visible )
    {
        this.isVisible = visible;
        for ( DockingWindow window : reversed( this.windows ) )
        {
            window.setVisible( this.isVisible );
        }
    }

    @Override
    public boolean isVisible( )
    {
        return this.isVisible;
    }

    public void onDragStarting( Tile fromTile )
    {
        DockingWindow fromWindow = getAncestorOfClass( DockingWindow.class, fromTile );
        this.recordWindowRaise( fromWindow );

        // When choosing a landing-region, it's important to iterate over windows
        // according to stacking order. Unfortunately, Swing does not provide a
        // mechanism for checking the existing stacking order.
        //
        // As a workaround, we do our best to reconstruct the stacking order, by
        // listening for various events. Before we choose a landing-region, we
        // re-stack our windows to match our reconstructed ordering. This causes
        // the actual ordering to match the reconstructed ordering. We can then
        // iterate according to the reconstruction.
        //
        for ( DockingWindow window : reversed( this.windows ) )
        {
            window.toFront( );
        }
    }

    public abstract LandingRegion findLandingRegion( Tile fromTile, int fromViewNum, Point pOnScreen );

    public void setLandingIndicator( Rectangle bounds )
    {
        this.landingIndicator.setBounds( bounds );
    }

    public TileFactory tileFactory( )
    {
        return this.tileFactory;
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
        notifyClosedView( this.listeners, this, view );
    }

    @Override
    public void dispose( )
    {
        notifyDisposingAllWindows( this.listeners, this );
        for ( DockingWindow window : new ArrayList<>( this.windows ) )
        {
            notifyDisposingWindow( this.listeners, this, window );
            window.dispose( );

            // This would get triggered eventually by the WINDOW_CLOSED event, but not until
            // after previously queued events have been dispatched -- which can cause problems,
            // especially when there are modal dialogs involved
            this.removeWindow( window );
        }

        this.landingIndicator.dispose( );
    }

}

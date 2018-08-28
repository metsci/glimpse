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
package com.metsci.glimpse.docking.frame;

import static com.metsci.glimpse.docking.DockingGroupListenerUtils.attachMulticastDockerListener;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyAddedFrame;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyClosedView;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyClosedViews;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyClosingView;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyClosingViews;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyDisposedFrame;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyDisposingAllFrames;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyDisposingFrame;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyUserRequestingDisposeFrame;
import static com.metsci.glimpse.docking.DockingGroupUtils.chooseViewPlacement;
import static com.metsci.glimpse.docking.DockingGroupUtils.pruneEmptyTile;
import static com.metsci.glimpse.docking.DockingGroupUtils.restoreMaximizedTilesInNewDockers;
import static com.metsci.glimpse.docking.DockingGroupUtils.restoreSelectedViewsInNewTiles;
import static com.metsci.glimpse.docking.DockingGroupUtils.showNewFrames;
import static com.metsci.glimpse.docking.DockingGroupUtils.toGroupRealization;
import static com.metsci.glimpse.docking.DockingGroupUtils.withPlacement;
import static com.metsci.glimpse.docking.DockingGroupUtils.withPlannedPlacements;
import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.allViewsAreAutoCloseable;
import static com.metsci.glimpse.docking.DockingUtils.findViews;
import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.metsci.glimpse.docking.DockingFrameCloseOperation;
import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingGroupListener;
import com.metsci.glimpse.docking.DockingGroupUtils.GroupRealization;
import com.metsci.glimpse.docking.DockingGroupUtils.ViewDestination;
import com.metsci.glimpse.docking.DockingGroupUtils.ViewPlacement;
import com.metsci.glimpse.docking.DockingGroupUtils.ViewPlacementRule;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.LandingIndicator;
import com.metsci.glimpse.docking.MultiSplitPane;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.TileFactory;
import com.metsci.glimpse.docking.TileFactoryStandard;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.util.var.Disposable;

public class DockingGroupMultiFrame implements DockingGroup
{
    private static final Logger logger = Logger.getLogger( DockingGroup.class.getName( ) );


    protected final DockingTheme theme;
    protected final DockingFrameCloseOperation frameCloseOperation;
    protected final TileFactory tileFactory;

    protected final List<DockingFrame> frames;
    protected GroupArrangement planArr;

    protected final LandingIndicator landingIndicator;

    protected final Set<DockingGroupListener> listeners;


    public DockingGroupMultiFrame( DockingFrameCloseOperation frameCloseOperation )
    {
        this( frameCloseOperation, defaultDockingTheme( ) );
    }

    public DockingGroupMultiFrame( DockingFrameCloseOperation frameCloseOperation, DockingTheme theme )
    {
        this.theme = theme;
        this.frameCloseOperation = frameCloseOperation;
        this.tileFactory = new TileFactoryStandard( this );

        this.frames = new ArrayList<>( );
        this.planArr = new GroupArrangement( );

        this.landingIndicator = new LandingIndicator( this.theme );

        this.listeners = new LinkedHashSet<>( );
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
    public Collection<DockingGroupListener> listeners( )
    {
        return unmodifiableSet( this.listeners );
    }

    @Override
    public DockingTheme theme( )
    {
        return this.theme;
    }

    @Override
    public void setLandingIndicator( Rectangle bounds )
    {
        this.landingIndicator.setBounds( bounds );
    }

    @Override
    public void setArrangement( GroupArrangement groupArr )
    {
        // Remember existing views
        Collection<View> views = findViews( this.frames );

        // Remove existing views, pruning empty tiles and frames
        for ( View view : views )
        {
            this.closeView( view );
        }

        // Set the arrangement plan
        this.planArr = groupArr;

        // Re-add views
        this.addViews( views );
    }

    @Override
    public GroupArrangement captureArrangement( )
    {
        GroupArrangement existingArr = toGroupRealization( this ).groupArr;
        return withPlannedPlacements( existingArr, this.planArr );
    }

    @Override
    public List<DockingFrame> windows( )
    {
        return unmodifiableList( this.frames );
    }

    @Override
    public DockingFrame addNewWindow( )
    {
        MultiSplitPane docker = new MultiSplitPane( this.theme.dividerSize );
        attachMulticastDockerListener( docker, this.listeners );

        DockingFrame frame = new DockingFrame( docker );
        frame.setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        frame.addWindowListener( new WindowAdapter( )
        {
            @Override
            public void windowActivated( WindowEvent ev )
            {
                // Assumes windows get raised when activated -- not always true (e.g. with
                // a focus-follows-mouse WM), but it's as close as we can get with Swing
                DockingGroupMultiFrame.this.onWindowRaised( frame );
            }

            @Override
            public void windowClosing( WindowEvent ev )
            {
                DockingGroupMultiFrame.this.onWindowCloseButton( frame );
            }

            @Override
            public void windowClosed( WindowEvent ev )
            {
                DockingGroupMultiFrame.this.onWindowClosed( frame );
            }
        } );

        this.frames.add( 0, frame );
        notifyAddedFrame( this.listeners, this, frame );
        return frame;
    }

    @Override
    public void onWindowRaised( DockingFrame frame )
    {
        boolean found = this.frames.remove( frame );
        if ( !found )
        {
            throw new RuntimeException( "Frame does not belong to this docking-group" );
        }

        this.frames.add( 0, frame );
    }

    protected void onWindowCloseButton( DockingFrame frame )
    {
        notifyUserRequestingDisposeFrame( this.listeners, this, frame );

        switch ( this.frameCloseOperation )
        {
            case DO_NOTHING:
            {
                // Do nothing
            }
            break;

            case DISPOSE_CLOSED_FRAME:
            {
                Set<View> views = findViews( frame.docker );
                if ( allViewsAreAutoCloseable( views ) )
                {
                    notifyDisposingFrame( this.listeners, this, frame );
                    notifyClosingViews( this.listeners, this, views );
                    frame.dispose( );
                    notifyClosedViews( this.listeners, this, views );
                }
                else
                {
                    logger.warning( "Refusing to dispose frame, because it contains at least one view that is not auto-closeable" );
                }
            }
            break;

            case DISPOSE_ALL_FRAMES:
            {
                this.disposeAllWindows( );
            }
            break;

            case EXIT_JVM:
            {
                this.disposeAllWindows( );
                System.exit( 0 );
            }
            break;
        }
    }

    protected void onWindowClosed( DockingFrame frame )
    {
        this.frames.remove( frame );
        notifyDisposedFrame( this.listeners, this, frame );

        if ( this.frames.isEmpty( ) )
        {
            // Dispose the landingIndicator window, so that the JVM can shut down if
            // appropriate -- if the landingIndicator is needed again (e.g. after a
            // new frame is added to the group), it will be automatically resurrected
            this.landingIndicator.dispose( );
        }
    }

    @Override
    public void disposeAllWindows( )
    {
        notifyDisposingAllFrames( this.listeners, this );
        for ( DockingFrame frame : this.frames )
        {
            notifyDisposingFrame( this.listeners, this, frame );
            frame.dispose( );
        }
    }

    @Override
    public Tile createNewTile( )
    {
        return this.tileFactory.newTile( );
    }

    @Override
    public void addViewPlacement( String viewId, ViewPlacementRule placementRule )
    {
        GroupArrangement existingArr = toGroupRealization( this ).groupArr;
        this.planArr = withPlacement( existingArr, this.planArr, viewId, placementRule );
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
            GroupRealization existing = toGroupRealization( this );
            ViewPlacement placement = chooseViewPlacement( existing.groupArr, this.planArr, view.viewId );
            ViewDestination destination = placement.placeView( existing, view );
            viewDestinations.add( destination );
        }

        restoreSelectedViewsInNewTiles( viewDestinations );
        restoreMaximizedTilesInNewDockers( viewDestinations );
        showNewFrames( viewDestinations, this.planArr.frameArrs );
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
        pruneEmptyTile( tile, true ); // FIXME

        notifyClosedView( this.listeners, this, view );
    }

}

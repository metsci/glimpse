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

import static com.metsci.glimpse.docking.DockingGroupListenerUtils.attachMulticastDockerListener;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.attachMulticastTileListener;
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
import static com.metsci.glimpse.docking.DockingGroupUtils.restoreFrameOrder;
import static com.metsci.glimpse.docking.DockingGroupUtils.restoreMaximizedTiles;
import static com.metsci.glimpse.docking.DockingGroupUtils.restoreSelectedViews;
import static com.metsci.glimpse.docking.DockingGroupUtils.toGroupRealization;
import static com.metsci.glimpse.docking.DockingGroupUtils.withPlacement;
import static com.metsci.glimpse.docking.DockingGroupUtils.withPlannedPlacements;
import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.allViewsAreAutoCloseable;
import static com.metsci.glimpse.docking.DockingUtils.findViews;
import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
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

import com.metsci.glimpse.docking.DockingGroupUtils.GroupRealization;
import com.metsci.glimpse.docking.DockingGroupUtils.ViewDestination;
import com.metsci.glimpse.docking.DockingGroupUtils.ViewPlacement;
import com.metsci.glimpse.docking.DockingGroupUtils.ViewPlacementRule;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.util.var.Disposable;

public class DockingGroup
{
    private static final Logger logger = Logger.getLogger( DockingGroup.class.getName( ) );


    public final DockingTheme theme;
    public final DockingFrameCloseOperation frameCloseOperation;
    public final TileFactory tileFactory;

    protected final List<DockingFrame> framesMod;
    public final List<DockingFrame> frames;
    protected GroupArrangement planArr;

    protected final LandingIndicator landingIndicator;

    protected final Set<DockingGroupListener> listeners;


    public DockingGroup( DockingFrameCloseOperation frameCloseOperation )
    {
        this( frameCloseOperation, defaultDockingTheme( ) );
    }

    public DockingGroup( DockingFrameCloseOperation frameCloseOperation, DockingTheme theme )
    {
        this.theme = theme;
        this.frameCloseOperation = frameCloseOperation;
        this.tileFactory = new TileFactoryStandard( this );

        this.framesMod = new ArrayList<>( );
        this.frames = unmodifiableList( this.framesMod );
        this.planArr = new GroupArrangement( );

        this.landingIndicator = new LandingIndicator( this.theme );

        this.listeners = new LinkedHashSet<>( );
    }

    public Disposable addListener( DockingGroupListener listener )
    {
        this.listeners.add( listener );

        return ( ) ->
        {
            this.removeListener( listener );
        };
    }

    public void removeListener( DockingGroupListener listener )
    {
        this.listeners.remove( listener );
    }

    public DockingFrame addNewFrame( )
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
                DockingGroup.this.onWindowRaised( frame );
            }

            @Override
            public void windowClosing( WindowEvent ev )
            {
                DockingGroup.this.onWindowCloseButton( frame );
            }

            @Override
            public void windowClosed( WindowEvent ev )
            {
                DockingGroup.this.onWindowClosed( frame );
            }
        } );

        this.framesMod.add( 0, frame );
        notifyAddedFrame( this.listeners, this, frame );
        return frame;
    }

    public void onWindowRaised( DockingFrame frame )
    {
        boolean found = this.framesMod.remove( frame );
        if ( !found )
        {
            throw new RuntimeException( "Frame does not belong to this docking-group" );
        }

        this.framesMod.add( 0, frame );
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
                this.disposeAllFrames( );
            }
            break;

            case EXIT_JVM:
            {
                this.disposeAllFrames( );
                System.exit( 0 );
            }
            break;
        }
    }

    protected void onWindowClosed( DockingFrame frame )
    {
        this.framesMod.remove( frame );
        notifyDisposedFrame( this.listeners, this, frame );

        if ( this.frames.isEmpty( ) )
        {
            // Dispose the landingIndicator window, so that the JVM can shut
            // down if appropriate -- if the landingIndicator is needed again
            // (e.g. after a new frame is added to the group), it will be
            // automatically resurrected
            this.landingIndicator.dispose( );
        }
    }

    public void disposeAllFrames( )
    {
        notifyDisposingAllFrames( this.listeners, this );
        for ( DockingFrame frame : this.frames )
        {
            notifyDisposingFrame( this.listeners, this, frame );
            frame.dispose( );
        }
    }

    /**
     * Intended for use by {@link TileFactory} impls.
     */
    public void attachListenerTo( Tile tile )
    {
        attachMulticastTileListener( tile, this.listeners );
    }

    public void setLandingIndicator( Rectangle bounds )
    {
        this.landingIndicator.setBounds( bounds );
    }

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

    public GroupArrangement captureArrangement( )
    {
        GroupArrangement existingArr = toGroupRealization( this ).groupArr;
        return withPlannedPlacements( existingArr, this.planArr );
    }

    /**
     * @see DockingGroupUtils#withViewPlacement(GroupArrangement, GroupArrangement, String, ViewPlacementRule)
     */
    public void addViewPlacement( String viewId, ViewPlacementRule placementRule )
    {
        GroupArrangement existingArr = toGroupRealization( this ).groupArr;
        this.planArr = withPlacement( existingArr, this.planArr, viewId, placementRule );
    }

    public void addView( View view )
    {
        this.addViews( view );
    }

    public void addViews( View... views )
    {
        this.addViews( asList( views ) );
    }

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

        restoreSelectedViews( viewDestinations );
        restoreMaximizedTiles( viewDestinations );
        restoreFrameOrder( viewDestinations, this.planArr.frameArrs );
    }

    public void closeView( View view )
    {
        Tile tile = getAncestorOfClass( Tile.class, view.component.v( ) );
        if ( tile == null )
        {
            throw new RuntimeException( "View does not belong to this docking-group: view-id = " + view.viewId );
        }

        notifyClosingView( this.listeners, this, view );

        tile.removeView( view );
        pruneEmptyTile( tile, true );

        notifyClosedView( this.listeners, this, view );
    }

}

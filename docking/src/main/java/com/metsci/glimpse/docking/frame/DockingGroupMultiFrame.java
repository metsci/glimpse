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
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyClosedViews;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyClosingViews;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyDisposedFrame;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyDisposingAllFrames;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyDisposingFrame;
import static com.metsci.glimpse.docking.DockingGroupListenerUtils.notifyUserRequestingDisposeFrame;
import static com.metsci.glimpse.docking.DockingGroupUtils.chooseViewPlacement;
import static com.metsci.glimpse.docking.DockingGroupUtils.pruneEmpty;
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
import static com.metsci.glimpse.docking.LandingRegions.landingInExistingDocker;
import static com.metsci.glimpse.docking.MiscUtils.convertPointFromScreen;
import static com.metsci.glimpse.docking.MiscUtils.convertPointToScreen;
import static com.metsci.glimpse.docking.MiscUtils.reversed;
import static java.awt.Frame.ICONIFIED;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.metsci.glimpse.docking.DockingFrameCloseOperation;
import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingGroupBase;
import com.metsci.glimpse.docking.DockingGroupUtils.GroupRealization;
import com.metsci.glimpse.docking.DockingGroupUtils.ViewDestination;
import com.metsci.glimpse.docking.DockingGroupUtils.ViewPlacement;
import com.metsci.glimpse.docking.DockingGroupUtils.ViewPlacementRule;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.LandingRegions.LandingRegion;
import com.metsci.glimpse.docking.MultiSplitPane;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.TileFactory;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class DockingGroupMultiFrame extends DockingGroupBase
{
    private static final Logger logger = Logger.getLogger( DockingGroup.class.getName( ) );


    protected final List<DockingFrame> frames;
    protected GroupArrangement planArr;


    public DockingGroupMultiFrame( DockingFrameCloseOperation frameCloseOperation )
    {
        this( frameCloseOperation, defaultDockingTheme( ) );
    }

    public DockingGroupMultiFrame( DockingFrameCloseOperation frameCloseOperation, DockingTheme theme )
    {
        super( frameCloseOperation, theme );
        this.frames = new ArrayList<>( );
        this.planArr = new GroupArrangement( );
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

    protected void onWindowRaised( DockingFrame frame )
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
                // Do nothing
                break;

            case DISPOSE_CLOSED_FRAME:
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
    public void addViewPlacement( String viewId, ViewPlacementRule placementRule )
    {
        GroupArrangement existingArr = toGroupRealization( this ).groupArr;
        this.planArr = withPlacement( existingArr, this.planArr, viewId, placementRule );
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
    public void setArrangement( GroupArrangement groupArr )
    {
        Collection<View> views = findViews( this.frames );

        for ( View view : views )
        {
            this.closeView( view );
        }

        this.planArr = groupArr;

        this.addViews( views );
    }

    @Override
    public GroupArrangement captureArrangement( )
    {
        GroupArrangement existingArr = toGroupRealization( this ).groupArr;
        return withPlannedPlacements( existingArr, this.planArr );
    }

    @Override
    public void pruneEmptyTile( Tile tile )
    {
        pruneEmpty( tile, true );
    }

    @Override
    public void onDragStarting( Tile fromTile )
    {
        DockingFrame fromFrame = getAncestorOfClass( DockingFrame.class, fromTile );
        this.onWindowRaised( fromFrame );

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
        for ( DockingFrame frame : reversed( this.frames ) )
        {
            frame.toFront( );
        }
    }

    @Override
    public LandingRegion findLandingRegion( Tile fromTile, int fromViewNum, Point pOnScreen )
    {
        for ( DockingFrame frame : this.frames )
        {
            MultiSplitPane docker = frame.docker;
            if ( docker.isVisible( ) && frame.isVisible( ) && ( frame.getExtendedState( ) & ICONIFIED ) == 0 )
            {
                Point pInDocker = convertPointFromScreen( pOnScreen, docker );
                if ( docker.contains( pInDocker ) )
                {
                    return landingInExistingDocker( docker, fromTile, fromViewNum, pOnScreen );
                }
            }
        }
        return this.landingInNewFrame( fromTile, fromViewNum, pOnScreen );
    }

    protected LandingRegion landingInNewFrame( Tile fromTile, int fromViewNum, Point pOnScreen )
    {
        int xFrameInset;
        int yFrameInset;
        {
            DockingFrame frame = getAncestorOfClass( DockingFrame.class, fromTile );
            MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, fromTile );
            Point frameOrigin = convertPointToScreen( frame, new Point( 0, 0 ) );
            Point dockerOrigin = convertPointToScreen( docker, new Point( 0, 0 ) );
            Insets dockerInsets = docker.getInsets( );
            xFrameInset = ( dockerOrigin.x - frameOrigin.x ) + dockerInsets.left;
            yFrameInset = ( dockerOrigin.y - frameOrigin.y ) + dockerInsets.top;
        }

        Rectangle draggedTabBounds = fromTile.viewTabBounds( fromViewNum );
        Rectangle leftmostTabBounds = draggedTabBounds;
        for ( int i = 0; i < fromTile.numViews( ); i++ )
        {
            if ( fromTile.hasViewTab( i ) )
            {
                leftmostTabBounds = fromTile.viewTabBounds( i );
                break;
            }
        }

        // TODO: Could do better by accounting for the mouse-press point
        int xTileOffset = leftmostTabBounds.x + 7 * draggedTabBounds.width / 16;
        int yTileOffset = leftmostTabBounds.y + 5 * draggedTabBounds.height / 8;
        int xRegionOnScreen = pOnScreen.x - xTileOffset;
        int yRegionOnScreen = pOnScreen.y - yTileOffset;

        int regionWidth = fromTile.getWidth( );
        int regionHeight = fromTile.getHeight( );

        return new LandingRegion( )
        {
            @Override
            public Rectangle getIndicator( )
            {
                return new Rectangle( xRegionOnScreen, yRegionOnScreen, regionWidth, regionHeight );
            }

            @Override
            public void placeView( View view, TileFactory tileFactory )
            {
                Tile tile = tileFactory.newTile( );
                tile.addView( view, 0 );

                DockingFrame frame = DockingGroupMultiFrame.this.addNewFrame( );
                frame.docker.addInitialLeaf( tile );
                frame.setLocation( xRegionOnScreen - xFrameInset, yRegionOnScreen - yFrameInset );
                tile.setPreferredSize( new Dimension( regionWidth, regionHeight ) );
                frame.pack( );
                frame.setVisible( true );
            }
        };
    }

    @Override
    public void dispose( )
    {
        notifyDisposingAllFrames( this.listeners, this );
        for ( DockingFrame frame : this.frames )
        {
            notifyDisposingFrame( this.listeners, this, frame );
            frame.dispose( );
        }
    }

}

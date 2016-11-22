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

import static com.metsci.glimpse.docking.DockingGroupUtils.chooseViewPlacement;
import static com.metsci.glimpse.docking.DockingGroupUtils.containsView;
import static com.metsci.glimpse.docking.DockingGroupUtils.findViewIds;
import static com.metsci.glimpse.docking.DockingGroupUtils.toGroupRealization;
import static com.metsci.glimpse.docking.DockingUtils.allViewsAreCloseable;
import static com.metsci.glimpse.docking.DockingUtils.findTiles;
import static com.metsci.glimpse.docking.DockingUtils.findViews;
import static com.metsci.glimpse.docking.MiscUtils.getAncestorOfClass;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

import java.awt.Component;
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
import com.metsci.glimpse.docking.DockingGroupUtils.ViewPlacement;
import com.metsci.glimpse.docking.DockingThemes.DockingTheme;
import com.metsci.glimpse.docking.xml.GroupArrangement;

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

    public DockingGroup( DockingTheme theme, DockingFrameCloseOperation frameCloseOperation )
    {
        this.theme = theme;
        this.frameCloseOperation = frameCloseOperation;
        this.tileFactory = new TileFactoryStandard( this );

        this.framesMod = new ArrayList<>( );
        this.frames = unmodifiableList( framesMod );
        this.planArr = new GroupArrangement( );

        this.landingIndicator = new LandingIndicator( theme );

        this.listeners = new LinkedHashSet<>( );
    }

    public void addListener( DockingGroupListener listener )
    {
        listeners.add( listener );
    }

    public void removeListener( DockingGroupListener listener )
    {
        listeners.remove( listener );
    }

    public DockingFrame addNewFrame( )
    {
        MultiSplitPane docker = new MultiSplitPane( theme.dividerSize );
        attachListenerTo( docker );

        final DockingFrame frame = new DockingFrame( docker );
        frame.setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        frame.addWindowListener( new WindowAdapter( )
        {
            public void windowActivated( WindowEvent ev )
            {
                bringFrameToFront( frame );
            }

            // Frame's close button was clicked
            public void windowClosing( WindowEvent ev )
            {
                switch ( frameCloseOperation )
                {
                    case DO_NOTHING:
                    {
                        // Do nothing
                    }
                    break;

                    case DISPOSE_CLOSED_FRAME:
                    {
                        Set<View> views = findViews( frame.docker );
                        if ( allViewsAreCloseable( views ) )
                        {
                            for ( DockingGroupListener listener : listeners )
                            {
                                listener.disposingFrame( DockingGroup.this, frame );
                            }

                            for ( View view : views )
                            {
                                for ( DockingGroupListener listener : listeners )
                                {
                                    listener.closingView( DockingGroup.this, view );
                                }
                            }

                            frame.dispose( );

                            for ( View view : views )
                            {
                                for ( DockingGroupListener listener : listeners )
                                {
                                    listener.closedView( DockingGroup.this, view );
                                }
                            }
                        }
                        else
                        {
                            logger.warning( "Refusing to dispose frame, because it contains uncloseable views" );
                        }
                    }
                    break;

                    case DISPOSE_ALL_FRAMES:
                    {
                        for ( DockingGroupListener listener : listeners )
                        {
                            listener.disposingAllFrames( DockingGroup.this );
                        }
                        for ( DockingFrame frame : frames )
                        {
                            for ( DockingGroupListener listener : listeners )
                            {
                                listener.disposingFrame( DockingGroup.this, frame );
                            }
                            frame.dispose( );
                        }
                    }
                    break;

                    case EXIT_JVM:
                    {
                        for ( DockingGroupListener listener : listeners )
                        {
                            listener.disposingAllFrames( DockingGroup.this );
                        }
                        for ( DockingFrame frame : frames )
                        {
                            for ( DockingGroupListener listener : listeners )
                            {
                                listener.disposingFrame( DockingGroup.this, frame );
                            }
                            frame.dispose( );
                        }
                        // XXX: Can we keep this from interrupting the dispose calls? Should we?
                        System.exit( 0 );
                    }
                    break;
                }
            }

            // Frame has been disposed, including programmatically
            public void windowClosed( WindowEvent ev )
            {
                framesMod.remove( frame );

                for ( DockingGroupListener listener : listeners )
                {
                    listener.disposedFrame( DockingGroup.this, frame );
                }

                if ( frames.isEmpty( ) )
                {
                    // Dispose the landingIndicator frame, so that the JVM can shut
                    // down if appropriate. If the landingIndicator is needed again
                    // (e.g. after a new frame is added to the group), it will be
                    // automatically resurrected, and will work fine.
                    landingIndicator.dispose( );
                }
            }
        } );

        framesMod.add( 0, frame );
        for ( DockingGroupListener listener : listeners )
        {
            listener.addedFrame( this, frame );
        }
        return frame;
    }

    protected void attachListenerTo( final MultiSplitPane docker )
    {
        docker.addListener( new MultiSplitPaneListener( )
        {
            public void addedLeaf( Component leaf )
            {
                for ( DockingGroupListener listener : listeners )
                {
                    listener.addedLeaf( docker, leaf );
                }
            }

            public void removedLeaf( Component leaf )
            {
                for ( DockingGroupListener listener : listeners )
                {
                    listener.removedLeaf( docker, leaf );
                }
            }

            public void movedDivider( SplitPane splitPane )
            {
                for ( DockingGroupListener listener : listeners )
                {
                    listener.movedDivider( docker, splitPane );
                }
            }

            public void maximizedLeaf( Component leaf )
            {
                for ( DockingGroupListener listener : listeners )
                {
                    listener.maximizedLeaf( docker, leaf );
                }
            }

            public void unmaximizedLeaf( Component leaf )
            {
                for ( DockingGroupListener listener : listeners )
                {
                    listener.unmaximizedLeaf( docker, leaf );
                }
            }

            public void restoredTree( )
            {
                for ( DockingGroupListener listener : listeners )
                {
                    listener.restoredTree( docker );
                }
            }
        } );
    }

    protected void attachListenerTo( final Tile tile )
    {
        tile.addListener( new TileListener( )
        {
            public void addedView( View view )
            {
                for ( DockingGroupListener listener : listeners )
                {
                    listener.addedView( tile, view );
                }
            }

            public void removedView( View view )
            {
                for ( DockingGroupListener listener : listeners )
                {
                    listener.removedView( tile, view );
                }
            }

            public void selectedView( View view )
            {
                for ( DockingGroupListener listener : listeners )
                {
                    listener.selectedView( tile, view );
                }
            }
        } );
    }

    public void bringFrameToFront( DockingFrame frame )
    {
        boolean found = framesMod.remove( frame );
        if ( !found )
        {
            throw new RuntimeException( "Frame does not belong to this docking-group" );
        }

        framesMod.add( 0, frame );
    }

    public void setLandingIndicator( Rectangle bounds )
    {
        landingIndicator.setBounds( bounds );
    }

    public void setArrangement( GroupArrangement groupArr )
    {
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
        GroupArrangement groupArr = toGroupRealization( this ).groupArr;
        for ( String viewId : findViewIds( this.planArr ) )
        {
            if ( !containsView( groupArr, viewId ) )
            {
                ViewPlacement viewPlacement = chooseViewPlacement( groupArr, this.planArr, viewId );
                viewPlacement.placeView( groupArr, viewId );
            }
        }
        return groupArr;
    }

    public void addViews( View... views )
    {
        this.addViews( asList( views ) );
    }

    public void addViews( Collection<View> views )
    {
        // WIP: Set the selected view of each newly created tile

        // XXX: Restore frame stacking order
        for ( View view : views )
        {
            this.addView( view );
        }
    }

    public void addView( View view )
    {
        GroupRealization existing = toGroupRealization( this );
        ViewPlacement placement = chooseViewPlacement( existing.groupArr, this.planArr, view.viewId );
        placement.placeView( existing, view );
    }

    public void updateView( View view )
    {
        for ( DockingFrame frame : frames )
        {
            for ( Tile tile : findTiles( frame.docker ) )
            {
                if ( tile.hasView( view ) )
                {
                    tile.updateView( view );
                }
            }
        }
    }

    public void closeView( View view )
    {
        Tile tile = getAncestorOfClass( Tile.class, view.component );
        if ( tile == null )
        {
            throw new RuntimeException( "View does not belong to this docking-group: view-id = " + view.viewId );
        }

        for ( DockingGroupListener listener : listeners )
        {
            listener.closingView( this, view );
        }

        tile.removeView( view );
        pruneEmptyTileAndFrame( tile );

        for ( DockingGroupListener listener : listeners )
        {
            listener.closedView( this, view );
        }
    }

    public static void pruneEmptyTileAndFrame( Tile tile )
    {
        if ( tile.numViews( ) == 0 )
        {
            MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, tile );
            docker.removeLeaf( tile );

            if ( docker.numLeaves( ) == 0 )
            {
                DockingFrame frame = getAncestorOfClass( DockingFrame.class, docker );
                if ( frame != null && frame.getContentPane( ) == docker )
                {
                    frame.dispose( );
                }
            }
        }
    }

}

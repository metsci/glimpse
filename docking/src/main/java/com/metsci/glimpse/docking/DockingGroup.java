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

import static com.metsci.glimpse.docking.DockingUtils.allViewsAreCloseable;
import static com.metsci.glimpse.docking.DockingUtils.appendViewsToTile;
import static com.metsci.glimpse.docking.DockingUtils.findLargestComponent;
import static com.metsci.glimpse.docking.DockingUtils.findLargestTile;
import static com.metsci.glimpse.docking.DockingUtils.findViews;
import static com.metsci.glimpse.docking.DockingUtils.getFrameExtendedState;
import static com.metsci.glimpse.docking.MiscUtils.getAncestorOfClass;
import static com.metsci.glimpse.docking.MiscUtils.reversed;
import static com.metsci.glimpse.docking.Side.LEFT;
import static java.awt.Frame.MAXIMIZED_HORIZ;
import static java.awt.Frame.MAXIMIZED_VERT;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

import java.awt.Component;
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

import com.metsci.glimpse.docking.DockingThemes.DockingTheme;
import com.metsci.glimpse.docking.MultiSplitPane.MultiSplitPaneListener;
import com.metsci.glimpse.docking.Tile.TileListener;
import com.metsci.glimpse.docking.TileFactories.TileFactory;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class DockingGroup
{

    private static final Logger logger = Logger.getLogger( DockingGroup.class.getName( ) );

    public static enum DockingFrameCloseOperation
    {
        DO_NOTHING, DISPOSE_CLOSED_FRAME, DISPOSE_ALL_FRAMES, EXIT_JVM
    }

    public static interface DockingGroupListener
    {
        void addedView( Tile tile, View view );

        void removedView( Tile tile, View view );

        void selectedView( Tile tile, View view );

        void addedLeaf( MultiSplitPane docker, Component leaf );

        void removedLeaf( MultiSplitPane docker, Component leaf );

        void movedDivider( MultiSplitPane docker, SplitPane splitPane );

        void maximizedLeaf( MultiSplitPane docker, Component leaf );

        void unmaximizedLeaf( MultiSplitPane docker, Component leaf );

        void restoredTree( MultiSplitPane docker );

        void addedFrame( DockingGroup group, DockingFrame frame );

        void disposingAllFrames( DockingGroup group );

        void disposingFrame( DockingGroup group, DockingFrame frame );

        void disposedFrame( DockingGroup group, DockingFrame frame );

        void closingView( DockingGroup group, View view );

        void closedView( DockingGroup group, View view );
    }

    public static class DockingGroupAdapter implements DockingGroupListener
    {
        public void addedView( Tile tile, View view )
        {
        }

        public void removedView( Tile tile, View view )
        {
        }

        public void selectedView( Tile tile, View view )
        {
        }

        public void addedLeaf( MultiSplitPane docker, Component leaf )
        {
        }

        public void removedLeaf( MultiSplitPane docker, Component leaf )
        {
        }

        public void movedDivider( MultiSplitPane docker, SplitPane splitPane )
        {
        }

        public void maximizedLeaf( MultiSplitPane docker, Component leaf )
        {
        }

        public void unmaximizedLeaf( MultiSplitPane docker, Component leaf )
        {
        }

        public void restoredTree( MultiSplitPane docker )
        {
        }

        public void addedFrame( DockingGroup group, DockingFrame frame )
        {
        }

        public void disposingAllFrames( DockingGroup group )
        {
        }

        public void disposingFrame( DockingGroup group, DockingFrame frame )
        {
        }

        public void disposedFrame( DockingGroup group, DockingFrame frame )
        {
        }

        public void closingView( DockingGroup group, View view )
        {
        }

        public void closedView( DockingGroup group, View view )
        {
        }
    }

    public static void pruneEmptyTileAndFrame( DockingGroup dockingGroup, Tile tile )
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

    public final DockingTheme theme;
    public final DockingFrameCloseOperation frameCloseOperation;

    protected final List<DockingFrame> framesMod;
    public final List<DockingFrame> frames;

    protected final LandingIndicator landingIndicator;

    protected final Set<DockingGroupListener> listeners;

    public DockingGroup( DockingTheme theme, DockingFrameCloseOperation frameCloseOperation )
    {
        this.theme = theme;
        this.frameCloseOperation = frameCloseOperation;

        this.framesMod = new ArrayList<>( );
        this.frames = unmodifiableList( framesMod );

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
        if ( !found ) throw new RuntimeException( "Frame does not belong to this docking-group" );

        framesMod.add( 0, frame );
    }

    public void setLandingIndicator( Rectangle bounds )
    {
        landingIndicator.setBounds( bounds );
    }

    public void closeView( View view )
    {
        Tile tile = getAncestorOfClass( Tile.class, view.component );
        if ( tile == null ) throw new RuntimeException( "View does not belong to this docking-group: view-id = " + view.viewId );

        for ( DockingGroupListener listener : listeners )
        {
            listener.closingView( this, view );
        }

        tile.removeView( view );
        pruneEmptyTileAndFrame( this, tile );

        for ( DockingGroupListener listener : listeners )
        {
            listener.closedView( this, view );
        }
    }

    // update all Tiles containing a View with view.viewId to
    // reflect the content of the provided view
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

    // find all Tiles in the provied MultiSplitPane
    // helper function for {@code #updateView( View )}
    protected static Set<Tile> findTiles( MultiSplitPane docker )
    {
        Set<Tile> tiles = new LinkedHashSet<>( );
        for ( Component c : docker.leaves( ) )
        {
            if ( c instanceof Tile )
            {
                Tile tile = ( Tile ) c;
                tiles.add( tile );
            }
        }
        return tiles;
    }

    // Snapshots
    //

    public void restoreArrangement( GroupArrangement groupArr, TileFactory tileFactory, View... views )
    {
        restoreArrangement( groupArr, tileFactory, asList( views ) );
    }

    public void restoreArrangement( GroupArrangement groupArr, TileFactory tileFactory, Collection<View> views )
    {
        if ( !frames.isEmpty( ) ) throw new RuntimeException( "At least one frame already exists" );

        Map<String, View> remainingViews = new LinkedHashMap<>( );
        for ( View v : views )
            remainingViews.put( v.viewId, v );

        if ( groupArr != null )
        {
            for ( FrameArrangement frameArr : reversed( groupArr.frameArrs ) )
            {
                MultiSplitPane.Node dockerRoot = toDockingPaneNode( frameArr.dockerArr, remainingViews, tileFactory );
                if ( dockerRoot != null )
                {
                    DockingFrame frame = addNewFrame( );
                    frame.docker.restore( dockerRoot );
                    frame.setLocation( frameArr.x, frameArr.y );
                    frame.setSize( frameArr.width, frameArr.height );
                    frame.setExtendedState( getFrameExtendedState( frameArr ) );
                    frame.setVisible( true );
                }
            }
        }

        if ( !remainingViews.isEmpty( ) )
        {
            DockingFrame frame;
            if ( frames.isEmpty( ) )
            {
                Tile tile = tileFactory.newTile( );
                appendViewsToTile( tile, remainingViews.values( ) );
                frame = addNewFrame( );
                frame.docker.addInitialLeaf( tile );
                frame.setLocationByPlatform( true );
                frame.setSize( 1024, 768 );
                frame.setVisible( true );
            }
            else
            {
                frame = findLargestComponent( frames );
                Tile tile = findLargestTile( frame.docker );
                if ( tile == null )
                {
                    tile = tileFactory.newTile( );
                    appendViewsToTile( tile, remainingViews.values( ) );
                    frame.docker.addEdgeLeaf( tile, LEFT );
                }
                else
                {
                    appendViewsToTile( tile, remainingViews.values( ) );
                }
            }
        }
    }

    public GroupArrangement captureArrangement( )
    {
        GroupArrangement groupArr = new GroupArrangement( );
        for ( DockingFrame frame : frames )
        {
            FrameArrangement frameArr = new FrameArrangement( );

            Rectangle bounds = frame.getNormalBounds( );
            frameArr.x = bounds.x;
            frameArr.y = bounds.y;
            frameArr.width = bounds.width;
            frameArr.height = bounds.height;

            int state = frame.getExtendedState( );
            frameArr.isMaximizedHoriz = ( ( state & MAXIMIZED_HORIZ ) != 0 );
            frameArr.isMaximizedVert = ( ( state & MAXIMIZED_VERT ) != 0 );

            frameArr.dockerArr = toDockerArrNode( frame.docker.snapshot( ) );

            groupArr.frameArrs.add( frameArr );
        }
        return groupArr;
    }

    protected static MultiSplitPane.Node toDockingPaneNode( DockerArrangementNode arrNode, Map<String, View> remainingViews_INOUT, TileFactory tileFactory )
    {
        if ( arrNode instanceof DockerArrangementTile )
        {
            DockerArrangementTile arrTile = ( DockerArrangementTile ) arrNode;

            Map<String, View> views = new LinkedHashMap<>( );
            for ( String viewId : arrTile.viewIds )
            {
                View view = remainingViews_INOUT.remove( viewId );
                if ( view != null ) views.put( viewId, view );
            }

            if ( views.isEmpty( ) )
            {
                return null;
            }
            else
            {
                Tile tile = tileFactory.newTile( );

                for ( View view : views.values( ) )
                {
                    int viewNum = tile.numViews( );
                    tile.addView( view, viewNum );
                }

                View selectedView = views.get( arrTile.selectedViewId );
                if ( selectedView != null )
                {
                    tile.selectView( selectedView );
                }

                return new MultiSplitPane.Leaf( tile, arrTile.isMaximized );
            }
        }
        else if ( arrNode instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit arrSplit = ( DockerArrangementSplit ) arrNode;
            MultiSplitPane.Node childA = toDockingPaneNode( arrSplit.childA, remainingViews_INOUT, tileFactory );
            MultiSplitPane.Node childB = toDockingPaneNode( arrSplit.childB, remainingViews_INOUT, tileFactory );

            if ( childA != null && childB != null )
            {
                return new MultiSplitPane.Split( arrSplit.arrangeVertically, arrSplit.splitFrac, childA, childB );
            }
            else if ( childA != null )
            {
                return childA;
            }
            else if ( childB != null )
            {
                return childB;
            }
            else
            {
                return null;
            }
        }
        else if ( arrNode == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + arrNode.getClass( ).getName( ) );
        }
    }

    protected static DockerArrangementNode toDockerArrNode( MultiSplitPane.Node node )
    {
        if ( node instanceof MultiSplitPane.Leaf )
        {
            MultiSplitPane.Leaf leaf = ( MultiSplitPane.Leaf ) node;

            List<String> viewIds = new ArrayList<>( );
            String selectedViewId = null;
            Component c = leaf.component;
            if ( c instanceof Tile )
            {
                Tile tile = ( Tile ) c;
                for ( int i = 0; i < tile.numViews( ); i++ )
                {
                    String viewId = tile.view( i ).viewId;
                    viewIds.add( viewId );
                }
                View selectedView = tile.selectedView( );
                selectedViewId = ( selectedView == null ? null : selectedView.viewId );
            }
            else
            {
                // XXX: Handle arbitrary components
            }

            DockerArrangementTile arrTile = new DockerArrangementTile( );
            arrTile.viewIds = viewIds;
            arrTile.selectedViewId = selectedViewId;
            arrTile.isMaximized = leaf.isMaximized;
            return arrTile;
        }
        else if ( node instanceof MultiSplitPane.Split )
        {
            MultiSplitPane.Split split = ( MultiSplitPane.Split ) node;
            DockerArrangementSplit arrSplit = new DockerArrangementSplit( );
            arrSplit.arrangeVertically = split.arrangeVertically;
            arrSplit.splitFrac = split.splitFrac;
            arrSplit.childA = toDockerArrNode( split.childA );
            arrSplit.childB = toDockerArrNode( split.childB );
            return arrSplit;
        }
        else if ( node == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + MultiSplitPane.Node.class.getName( ) + ": " + node.getClass( ).getName( ) );
        }
    }

}

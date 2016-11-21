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
import static com.metsci.glimpse.docking.DockingUtils.findViews;
import static com.metsci.glimpse.docking.DockingUtils.getFrameExtendedState;
import static com.metsci.glimpse.docking.MiscUtils.getAncestorOfClass;
import static com.metsci.glimpse.docking.MiscUtils.intersection;
import static com.metsci.glimpse.docking.MiscUtils.reversed;
import static com.metsci.glimpse.docking.MiscUtils.union;
import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.RIGHT;
import static com.metsci.glimpse.docking.Side.TOP;
import static java.awt.Frame.MAXIMIZED_HORIZ;
import static java.awt.Frame.MAXIMIZED_VERT;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.metsci.glimpse.docking.DockingThemes.DockingTheme;
import com.metsci.glimpse.docking.MultiSplitPane.MultiSplitPaneListener;
import com.metsci.glimpse.docking.Tile.TileListener;
import com.metsci.glimpse.docking.TileFactories.TileFactory;
import com.metsci.glimpse.docking.TileFactories.TileFactoryStandard;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class DockingGroup
{
    private static final Logger logger = Logger.getLogger( DockingGroup.class.getName( ) );

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
    protected final TileFactory tileFactory;

    protected final List<DockingFrame> framesMod;
    public final List<DockingFrame> frames;
    protected GroupArrangement plan;

    protected final LandingIndicator landingIndicator;

    protected final Set<DockingGroupListener> listeners;

    public DockingGroup( DockingTheme theme, DockingFrameCloseOperation frameCloseOperation )
    {
        this.theme = theme;
        this.frameCloseOperation = frameCloseOperation;
        this.tileFactory = new TileFactoryStandard( this );

        this.framesMod = new ArrayList<>( );
        this.frames = unmodifiableList( framesMod );
        this.plan = new GroupArrangement( );

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

    public void setArrangement( GroupArrangement groupArr )
    {
        // WIP: Rearrange existing stuff

        this.plan = groupArr;
    }

    public GroupArrangement captureArrangement( )
    {
        // WIP: Need to include info that is in the plan only (not in the current frames)
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

    public void addViews( View... views )
    {
        // Bookkeeping for frame order
        Set<Window> preExistingFrames = new LinkedHashSet<>( this.frames );
        Map<FrameArrangement,Window> plannedNewFrames = new LinkedHashMap<>( );
        List<Window> unplannedNewFrames = new ArrayList<>( );

        for ( View view : views )
        {
            ViewDestination viewDest = this.doAddView( view );

            // Bookkeeping for frame order
            if ( !preExistingFrames.contains( viewDest.window ) )
            {
                if ( viewDest.frameArr != null )
                {
                    plannedNewFrames.put( viewDest.frameArr, viewDest.window );
                }
                else
                {
                    unplannedNewFrames.add( viewDest.window );
                }
            }
        }

        // Order pre-existing frames
        for ( Window w : reversed( preExistingFrames ) )
        {
            w.toFront( );
        }

        // Order planned new frames (in front of pre-existing)
        for ( FrameArrangement frameArr : reversed( this.plan.frameArrs ) )
        {
            Window w = plannedNewFrames.get( frameArr );
            if ( w != null )
            {
                w.toFront( );
            }
        }

        // Order unplanned new frames (in front of planned)
        for ( Window w : unplannedNewFrames )
        {
            w.toFront( );
        }
    }

    public void addView( View view )
    {
        this.doAddView( view );
    }

    /**
     * Used to inform the caller of {@link DockingGroup#doAddView(View)}
     * about where the view ended up (e.g. which frame).
     */
    protected static class ViewDestination
    {
        /**
         * Null if the added view was not in the planned arrangement
         */
        public final FrameArrangement frameArr;

        /**
         * Always non-null
         */
        public final Window window;

        public ViewDestination( FrameArrangement frameArr, Window window )
        {
            this.frameArr = frameArr;
            this.window = window;
        }
    }

    protected ViewDestination doAddView( View view )
    {
        Map<DockerArrangementNode,Set<String>> planSubtreeViewIds = buildPlanSubtreeViewIdsMap( this.plan );
        Map<MultiSplitPane.Node,Set<String>> guiSubtreeViewIds = buildGuiSubtreeViewIdsMap( this.frames );

        FrameArrangement planFrame = findFrameArrContaining( this.plan, view.viewId );
        if ( planFrame != null )
        {
            // Add to an existing tile
            DockerArrangementTile planTile = findArrTileContaining( planFrame.dockerArr, planSubtreeViewIds, view.viewId );
            Set<String> planTileViewIds = planSubtreeViewIds.get( planTile );
            MultiSplitPane.Leaf guiLeaf = findSimilarGuiLeaf( guiSubtreeViewIds, planTileViewIds );
            if ( guiLeaf != null )
            {
                Tile tile = ( Tile ) guiLeaf.component;
                tile.addView( view, tile.numViews( ) );
                return new ViewDestination( planFrame, getAncestorOfClass( Window.class, tile ) );
            }

            // Create a new tile that neighbors an existing tile or split
            DockerArrangementSplit planParent = findArrNodeParent( planFrame.dockerArr, planTile );
            if ( planParent != null )
            {
                boolean newIsChildA = ( planTile == planParent.childA );
                DockerArrangementNode planNeighbor = ( newIsChildA ? planParent.childB : planParent.childA );
                Set<String> planNeighborViewIds = planSubtreeViewIds.get( planNeighbor );
                MultiSplitPane.Node guiNeighbor = findSimilarGuiNode( guiSubtreeViewIds, planNeighborViewIds );
                if ( guiNeighbor != null )
                {
                    Component neighbor = guiNeighbor.component;
                    Side sideOfNeighbor = ( planParent.arrangeVertically ? ( newIsChildA ? TOP : BOTTOM ) : ( newIsChildA ? LEFT : RIGHT ) );
                    double extentFrac = ( newIsChildA ? planParent.splitFrac : 1.0 - planParent.splitFrac );
                    MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, neighbor );

                    Tile newTile = this.tileFactory.newTile( );
                    newTile.addView( view, 0 );
                    docker.addNeighborLeaf( newTile, neighbor, sideOfNeighbor, extentFrac );

                    return new ViewDestination( planFrame, getAncestorOfClass( Window.class, docker ) );
                }
            }

            // Create a new frame, with size and position from the planned arrangement
            Tile newTile = this.tileFactory.newTile( );
            newTile.addView( view, 0 );
            DockingFrame newFrame = this.addNewFrame( );
            newFrame.docker.addInitialLeaf( newTile );
            newFrame.setBounds( planFrame.x, planFrame.y, planFrame.width, planFrame.height );
            newFrame.setNormalBounds( planFrame.x, planFrame.y, planFrame.width, planFrame.height );
            newFrame.setExtendedState( getFrameExtendedState( planFrame ) );
            newFrame.setVisible( true );
            return new ViewDestination( planFrame, newFrame );
        }

        // Create a new frame, with default size and position
        Tile newTile = this.tileFactory.newTile( );
        newTile.addView( view, 0 );
        DockingFrame newFrame = this.addNewFrame( );
        newFrame.docker.addInitialLeaf( newTile );
        newFrame.setLocationByPlatform( true );
        newFrame.setSize( 1024, 768 );
        newFrame.setVisible( true );
        return new ViewDestination( planFrame, newFrame );
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

    protected static Set<Tile> findTiles( MultiSplitPane docker )
    {
        Set<Tile> tiles = new LinkedHashSet<>( );
        for ( Component c : docker.leaves( ) )
        {
            if ( c instanceof Tile )
            {
                tiles.add( ( Tile ) c );
            }
        }
        return tiles;
    }

    protected static Map<DockerArrangementNode,Set<String>> buildPlanSubtreeViewIdsMap( GroupArrangement groupArr )
    {
        Map<DockerArrangementNode,Set<String>> result = new LinkedHashMap<>( );
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            putPlanSubtreeViewIds( frameArr.dockerArr, result );
        }
        return result;
    }

    protected static Set<String> putPlanSubtreeViewIds( DockerArrangementNode arrNode, Map<DockerArrangementNode,Set<String>> viewIds_INOUT )
    {
        if ( arrNode instanceof DockerArrangementTile )
        {
            DockerArrangementTile arrTile = ( DockerArrangementTile ) arrNode;
            Set<String> result = unmodifiableSet( new LinkedHashSet<>( arrTile.viewIds ) );
            viewIds_INOUT.put( arrTile, result );
            return result;
        }
        else if ( arrNode instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit arrSplit = ( DockerArrangementSplit ) arrNode;
            Set<String> resultA = putPlanSubtreeViewIds( arrSplit.childA, viewIds_INOUT );
            Set<String> resultB = putPlanSubtreeViewIds( arrSplit.childB, viewIds_INOUT );
            Set<String> result = unmodifiableSet( union( resultA, resultB ) );
            viewIds_INOUT.put( arrSplit, result );
            return result;
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

    protected static Map<MultiSplitPane.Node,Set<String>> buildGuiSubtreeViewIdsMap( List<DockingFrame> frames )
    {
        Map<MultiSplitPane.Node,Set<String>> result = new LinkedHashMap<>( );
        for ( DockingFrame frame : frames )
        {
            putGuiSubtreeViewIds( frame.docker.snapshot( ), result );
        }
        return result;
    }

    protected static Set<String> putGuiSubtreeViewIds( MultiSplitPane.Node node, Map<MultiSplitPane.Node,Set<String>> viewIds_INOUT )
    {
        if ( node instanceof MultiSplitPane.Leaf )
        {
            MultiSplitPane.Leaf leaf = ( MultiSplitPane.Leaf ) node;

            Set<String> viewIds = new LinkedHashSet<>( );
            Component c = leaf.component;
            if ( c instanceof Tile )
            {
                Tile tile = ( Tile ) c;
                for ( int i = 0; i < tile.numViews( ); i++ )
                {
                    String viewId = tile.view( i ).viewId;
                    viewIds.add( viewId );
                }
            }

            Set<String> result = unmodifiableSet( viewIds );
            viewIds_INOUT.put( leaf, result );
            return result;
        }
        else if ( node instanceof MultiSplitPane.Split )
        {
            MultiSplitPane.Split split = ( MultiSplitPane.Split ) node;
            Set<String> resultA = putGuiSubtreeViewIds( split.childA, viewIds_INOUT );
            Set<String> resultB = putGuiSubtreeViewIds( split.childB, viewIds_INOUT );
            Set<String> result = unmodifiableSet( union( resultA, resultB ) );
            viewIds_INOUT.put( split, result );
            return result;
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

    protected static FrameArrangement findFrameArrContaining( GroupArrangement groupArr, String viewId )
    {
        for ( FrameArrangement frameArr : groupArr.frameArrs )
        {
            if ( containsView( frameArr.dockerArr, viewId ) )
            {
                return frameArr;
            }
        }

        return null;
    }

    protected static boolean containsView( DockerArrangementNode node, String viewId )
    {
        if ( node instanceof DockerArrangementTile )
        {
            DockerArrangementTile tile = ( DockerArrangementTile ) node;
            return tile.viewIds.contains( viewId );
        }
        else if ( node instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit split = ( DockerArrangementSplit ) node;
            return ( containsView( split.childA, viewId ) || containsView( split.childB, viewId ) );
        }
        else if ( node == null )
        {
            return false;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + node.getClass( ).getName( ) );
        }
    }

    protected static DockerArrangementTile findArrTileContaining( DockerArrangementNode node, Map<DockerArrangementNode,Set<String>> subtreeViewIds, String viewId )
    {
        if ( node instanceof DockerArrangementTile )
        {
            DockerArrangementTile tile = ( DockerArrangementTile ) node;
            if ( subtreeViewIds.get( tile ).contains( viewId ) )
            {
                return tile;
            }

            return null;
        }
        else if ( node instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit split = ( DockerArrangementSplit ) node;
            if ( subtreeViewIds.get( split ).contains( viewId ) )
            {
                DockerArrangementTile resultA = findArrTileContaining( split.childA, subtreeViewIds, viewId );
                if ( resultA != null )
                {
                    return resultA;
                }

                DockerArrangementTile resultB = findArrTileContaining( split.childB, subtreeViewIds, viewId );
                if ( resultB != null )
                {
                    return resultB;
                }
            }

            return null;
        }
        else if ( node == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + node.getClass( ).getName( ) );
        }
    }

    protected static DockerArrangementSplit findArrNodeParent( DockerArrangementNode root, DockerArrangementNode child )
    {
        if ( root instanceof DockerArrangementTile )
        {
            return null;
        }
        else if ( root instanceof DockerArrangementSplit )
        {
            DockerArrangementSplit split = ( DockerArrangementSplit ) root;

            if ( child == split.childA || child == split.childB )
            {
                return split;
            }

            DockerArrangementSplit resultA = findArrNodeParent( split.childA, child );
            if ( resultA != null )
            {
                return resultA;
            }

            DockerArrangementSplit resultB = findArrNodeParent( split.childB, child );
            if ( resultB != null )
            {
                return resultB;
            }

            return null;
        }
        else if ( root == null )
        {
            return null;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + DockerArrangementNode.class.getName( ) + ": " + root.getClass( ).getName( ) );
        }
    }

    protected static MultiSplitPane.Leaf findSimilarGuiLeaf( Map<MultiSplitPane.Node,Set<String>> subtreeViewIds, Set<String> viewIds )
    {
        return ( MultiSplitPane.Leaf ) findSimilarGuiNode( subtreeViewIds, viewIds, true );
    }

    protected static MultiSplitPane.Node findSimilarGuiNode( Map<MultiSplitPane.Node,Set<String>> subtreeViewIds, Set<String> viewIds )
    {
        return findSimilarGuiNode( subtreeViewIds, viewIds, false );
    }

    protected static MultiSplitPane.Node findSimilarGuiNode( Map<MultiSplitPane.Node,Set<String>> subtreeViewIds, Set<String> viewIds, boolean requireLeaf )
    {
        MultiSplitPane.Node bestNode = null;
        int bestCommonCount = 0;
        int bestExtraneousCount = 0;

        for ( Entry<MultiSplitPane.Node,Set<String>> en : subtreeViewIds.entrySet( ) )
        {
            MultiSplitPane.Node node = en.getKey( );
            Set<String> nodeViewIds = en.getValue( );

            if ( !requireLeaf || node instanceof MultiSplitPane.Leaf )
            {
                int commonCount = intersection( nodeViewIds, viewIds ).size( );
                int extraneousCount = nodeViewIds.size( ) - commonCount;
                if ( commonCount > bestCommonCount || ( commonCount == bestCommonCount && extraneousCount < bestExtraneousCount ) )
                {
                    bestNode = node;
                    bestCommonCount = commonCount;
                    bestExtraneousCount = extraneousCount;
                }
            }
        }

        return bestNode;
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

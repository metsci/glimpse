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
package com.metsci.glimpse.docking.group.frame;

import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.allViewsAreAutoCloseable;
import static com.metsci.glimpse.docking.DockingUtils.findViews;
import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;
import static com.metsci.glimpse.docking.LandingRegions.landingInExistingDocker;
import static com.metsci.glimpse.docking.MiscUtils.convertPointFromScreen;
import static com.metsci.glimpse.docking.MiscUtils.convertPointToScreen;
import static com.metsci.glimpse.docking.MiscUtils.reversed;
import static com.metsci.glimpse.docking.group.ArrangementUtils.removeView;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.attachMulticastDockerListener;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyAddedFrame;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyClosedViews;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyClosingViews;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyDisposedFrame;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyDisposingAllFrames;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyDisposingFrame;
import static com.metsci.glimpse.docking.group.DockingGroupListenerUtils.notifyUserRequestingDisposeFrame;
import static com.metsci.glimpse.docking.group.DockingGroupUtils.pruneEmpty;
import static com.metsci.glimpse.docking.group.DockingGroupUtils.restoreSelectedViewsInNewTiles;
import static com.metsci.glimpse.docking.group.DockingGroupUtils.toArrNode;
import static com.metsci.glimpse.docking.group.ViewPlacementUtils.futureViewIds;
import static com.metsci.glimpse.docking.group.frame.DockingGroupMultiframeUtils.placeView;
import static com.metsci.glimpse.docking.group.frame.DockingGroupMultiframeUtils.restoreMaximizedTilesInNewFrames;
import static com.metsci.glimpse.docking.group.frame.DockingGroupMultiframeUtils.showNewFrames;
import static java.awt.Frame.ICONIFIED;
import static java.awt.Frame.MAXIMIZED_HORIZ;
import static java.awt.Frame.MAXIMIZED_VERT;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.metsci.glimpse.docking.DockingFrameCloseOperation;
import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.LandingRegions.LandingRegion;
import com.metsci.glimpse.docking.MultiSplitPane;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.TileFactory;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.group.DockingGroupBase;
import com.metsci.glimpse.docking.group.ViewPlacementRule;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class DockingGroupMultiframe extends DockingGroupBase
{
    private static final Logger logger = Logger.getLogger( DockingGroup.class.getName( ) );


    protected final List<DockingFrame> frames;
    protected GroupArrangement planArr;


    public DockingGroupMultiframe( DockingFrameCloseOperation frameCloseOperation )
    {
        this( frameCloseOperation, defaultDockingTheme( ) );
    }

    public DockingGroupMultiframe( DockingFrameCloseOperation frameCloseOperation, DockingTheme theme )
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
                DockingGroupMultiframe.this.onWindowRaised( frame );
            }

            @Override
            public void windowClosing( WindowEvent ev )
            {
                DockingGroupMultiframe.this.onWindowCloseButton( frame );
            }

            @Override
            public void windowClosed( WindowEvent ev )
            {
                DockingGroupMultiframe.this.onWindowClosed( frame );
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
                Collection<View> views = findViews( frame.docker ).values( );
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
        ViewPlacerMultiframeArr placer = new ViewPlacerMultiframeArr( newPlanArr, viewId );
        rule.placeView( newPlanArr, existingViewIds, placer );

        this.planArr = newPlanArr;

        if ( view != null )
        {
            this.addView( view );
        }
    }

    @Override
    public void addViews( Collection<View> views )
    {
        Collection<ViewDestinationMultiframe> viewDestinations = new ArrayList<>( );
        for ( View view : views )
        {
            Map<DockerArrangementNode,Component> componentsMap = new LinkedHashMap<>( );
            GroupArrangement existingArr = this.getExistingArr( componentsMap );
            ViewPlacerMultiframeGroup placer = new ViewPlacerMultiframeGroup( this, componentsMap, view );
            ViewDestinationMultiframe destination = placeView( existingArr, this.planArr, view.viewId, placer );
            viewDestinations.add( destination );
        }

        restoreSelectedViewsInNewTiles( viewDestinations );
        restoreMaximizedTilesInNewFrames( viewDestinations );
        showNewFrames( viewDestinations, this.planArr.frameArrs );
    }

    @Override
    public Map<String,View> views( )
    {
        return findViews( this.frames );
    }

    @Override
    public void setArrangement( GroupArrangement groupArr )
    {
        Collection<View> views = this.views( ).values( );
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
        GroupArrangement newPlanArr = this.getExistingArr( null );
        for ( String futureViewId : futureViewIds( this, this.planArr ) )
        {
            GroupArrangement existingArr = this.getExistingArr( null );
            ViewPlacerMultiframeArr placer = new ViewPlacerMultiframeArr( newPlanArr, futureViewId );
            placeView( existingArr, this.planArr, futureViewId, placer );
        }
        return newPlanArr;
    }

    /**
     * Returns a {@link GroupArrangement} that reflects only existing components,
     * <em>not</em> the planned arrangement of potential future components.
     * <p>
     * If {@code componentsMap} is non-null, it will be populated with mappings
     * from {@link DockerArrangementNode}s to corresponding {@link Component}s.
     */
    protected GroupArrangement getExistingArr( Map<DockerArrangementNode,Component> componentsMap )
    {
        GroupArrangement groupArr = new GroupArrangement( );
        for ( DockingFrame frame : this.frames )
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

            frameArr.dockerArr = toArrNode( frame.docker.snapshot( ), componentsMap );
            groupArr.frameArrs.add( frameArr );
        }
        return groupArr;
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

                DockingFrame frame = DockingGroupMultiframe.this.addNewFrame( );
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

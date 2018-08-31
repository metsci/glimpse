package com.metsci.glimpse.docking.group.frame;

import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;
import static com.metsci.glimpse.docking.DockingUtils.getFrameExtendedState;
import static com.metsci.glimpse.docking.group.frame.DockingGroupMultiframeUtils.fallbackFrameBounds;

import java.awt.Component;
import java.awt.Rectangle;

import com.metsci.glimpse.docking.MultiSplitPane;
import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;

class ViewPlacerMultiframeGroup implements ViewPlacerMultiframe<ViewDestinationMultiframe>
{

    protected final DockingGroupMultiframe group;
    protected final View newView;


    public ViewPlacerMultiframeGroup( DockingGroupMultiframe group, View newView )
    {
        this.group = group;
        this.newView = newView;
    }

    @Override
    public ViewDestinationMultiframe addToTile( DockerArrangementTile existingTile, int newViewNum )
    {
        Tile tile = ( Tile ) componentsMap.get( existingTile );
        tile.addView( this.newView, newViewNum );
        return new ViewDestinationMultiframe( null, null, null, null );
    }

    @Override
    public ViewDestinationMultiframe addBesideNeighbor( DockerArrangementTile planTile, DockerArrangementNode existingNeighbor, Side sideOfNeighbor, double extentFrac )
    {
        Tile newTile = this.group.createNewTile( );
        newTile.addView( this.newView, 0 );

        Component neighbor = componentsMap.get( existingNeighbor );

        MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, neighbor );
        docker.addNeighborLeaf( newTile, neighbor, sideOfNeighbor, extentFrac );

        return new ViewDestinationMultiframe( null, null, newTile, planTile );
    }

    @Override
    public ViewDestinationMultiframe addInNewFrame( FrameArrangement planFrame, DockerArrangementTile planTile )
    {
        Tile newTile = this.group.createNewTile( );
        newTile.addView( this.newView, 0 );

        DockingFrame newFrame = this.group.addNewFrame( );
        newFrame.docker.addInitialLeaf( newTile );

        newFrame.setBounds( planFrame.x, planFrame.y, planFrame.width, planFrame.height );
        newFrame.setNormalBounds( planFrame.x, planFrame.y, planFrame.width, planFrame.height );
        newFrame.setExtendedState( getFrameExtendedState( planFrame.isMaximizedHoriz, planFrame.isMaximizedVert ) );

        return new ViewDestinationMultiframe( newFrame, planFrame, newTile, planTile );
    }

    @Override
    public ViewDestinationMultiframe addInNewFallbackFrame( )
    {
        Tile newTile = this.group.createNewTile( );
        newTile.addView( this.newView, 0 );

        DockingFrame newFrame = this.group.addNewFrame( );
        newFrame.docker.addInitialLeaf( newTile );

        Rectangle newFrameBounds = fallbackFrameBounds( );
        newFrame.setBounds( newFrameBounds );
        newFrame.setNormalBounds( newFrameBounds );
        newFrame.setExtendedState( getFrameExtendedState( false, false ) );

        return new ViewDestinationMultiframe( newFrame, null, newTile, null );
    }

}

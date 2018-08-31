package com.metsci.glimpse.docking.group.frame;

import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.TOP;
import static com.metsci.glimpse.docking.group.ArrangementUtils.replaceArrNode;
import static com.metsci.glimpse.docking.group.frame.DockingGroupMultiframeUtils.fallbackFrameBounds;

import java.awt.Rectangle;

import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

class ViewPlacerMultiframeArr implements ViewPlacerMultiframe<Void>
{

    protected final GroupArrangement groupArr;
    protected final String newViewId;


    public ViewPlacerMultiframeArr( GroupArrangement groupArr, String newViewId )
    {
        this.groupArr = groupArr;
        this.newViewId = newViewId;
    }

    @Override
    public Void addToTile( DockerArrangementTile existingTile, int viewNum )
    {
        existingTile.viewIds.add( viewNum, this.newViewId );
        return null;
    }

    @Override
    public Void addBesideNeighbor( DockerArrangementTile planTile, DockerArrangementNode existingNeighbor, Side sideOfNeighbor, double extentFrac )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( this.newViewId );
        newTile.selectedViewId = this.newViewId;
        newTile.isMaximized = false;

        DockerArrangementSplit newSplit = new DockerArrangementSplit( );
        newSplit.arrangeVertically = ( sideOfNeighbor == TOP || sideOfNeighbor == BOTTOM );
        boolean newIsChildA = ( sideOfNeighbor == LEFT || sideOfNeighbor == TOP );
        newSplit.childA = ( newIsChildA ? newTile : existingNeighbor );
        newSplit.childB = ( newIsChildA ? existingNeighbor : newTile );
        newSplit.splitFrac = ( newIsChildA ? extentFrac : 1.0 - extentFrac );

        replaceArrNode( this.groupArr, existingNeighbor, newSplit );

        return null;
    }

    @Override
    public Void addInNewFrame( FrameArrangement planFrame, DockerArrangementTile planTile )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( newViewId );
        newTile.selectedViewId = newViewId;
        newTile.isMaximized = false;

        FrameArrangement newFrame = new FrameArrangement( );
        newFrame.dockerArr = newTile;

        newFrame.x = planFrame.x;
        newFrame.y = planFrame.y;
        newFrame.width = planFrame.width;
        newFrame.height = planFrame.height;
        newFrame.isMaximizedHoriz = planFrame.isMaximizedHoriz;
        newFrame.isMaximizedVert = planFrame.isMaximizedVert;

        this.groupArr.frameArrs.add( newFrame );

        return null;
    }

    @Override
    public Void addInNewFallbackFrame( )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( newViewId );
        newTile.selectedViewId = newViewId;
        newTile.isMaximized = false;

        FrameArrangement newFrame = new FrameArrangement( );
        newFrame.dockerArr = newTile;

        Rectangle newFrameBounds = fallbackFrameBounds( );
        newFrame.x = newFrameBounds.x;
        newFrame.y = newFrameBounds.y;
        newFrame.width = newFrameBounds.width;
        newFrame.height = newFrameBounds.height;
        newFrame.isMaximizedHoriz = false;
        newFrame.isMaximizedVert = false;

        this.groupArr.frameArrs.add( newFrame );

        return null;
    }

}

package com.metsci.glimpse.docking.group;

import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.TOP;
import static com.metsci.glimpse.docking.group.ArrangementUtils.replaceArrNode;

import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class ViewPlacerBaseArr implements ViewPlacer<Void>
{

    protected final GroupArrangement groupArr;
    protected final String newViewId;


    public ViewPlacerBaseArr( GroupArrangement groupArr, String newViewId )
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

}

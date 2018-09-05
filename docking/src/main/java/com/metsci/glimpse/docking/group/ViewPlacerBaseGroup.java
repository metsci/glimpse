package com.metsci.glimpse.docking.group;

import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;

import java.awt.Component;
import java.util.Map;

import com.metsci.glimpse.docking.MultiSplitPane;
import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;

public class ViewPlacerBaseGroup implements ViewPlacer<ViewDestination>
{

    protected final DockingGroupBase group;
    protected final Map<DockerArrangementNode,Component> existingComponents;
    protected final View newView;


    public ViewPlacerBaseGroup( DockingGroupBase group, Map<DockerArrangementNode,Component> existingComponents, View newView )
    {
        this.group = group;
        this.existingComponents = existingComponents;
        this.newView = newView;
    }

    @Override
    public ViewDestination addToTile( DockerArrangementTile existingTile, int newViewNum )
    {
        Tile tile = ( Tile ) this.existingComponents.get( existingTile );
        tile.addView( this.newView, newViewNum );
        return new ViewDestination( null, null, null, null );
    }

    @Override
    public ViewDestination addBesideNeighbor( DockerArrangementTile planTile, DockerArrangementNode existingNeighbor, Side sideOfNeighbor, double extentFrac )
    {
        Tile newTile = this.group.tileFactory( ).newTile( );
        newTile.addView( this.newView, 0 );

        Component neighbor = this.existingComponents.get( existingNeighbor );

        MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, neighbor );
        docker.addNeighborLeaf( newTile, neighbor, sideOfNeighbor, extentFrac );

        return new ViewDestination( null, null, newTile, planTile );
    }

}

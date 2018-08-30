package com.metsci.glimpse.docking.group;

import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;

public class ViewDestinationBase
{

    public final Tile createdTile;
    public final DockerArrangementTile planTile;


    public ViewDestinationBase( Tile createdTile, DockerArrangementTile planTile )
    {
        this.createdTile = createdTile;
        this.planTile = planTile;
    }

}

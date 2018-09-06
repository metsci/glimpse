package com.metsci.glimpse.docking.group;

import com.metsci.glimpse.docking.DockingWindow;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;

public class ViewDestination
{

    public final DockingWindow createdWindow;
    public final FrameArrangement planWindow;

    public final Tile createdTile;
    public final DockerArrangementTile planTile;


    public ViewDestination( DockingWindow createdWindow,
                            FrameArrangement planWindow,

                            Tile createdTile,
                            DockerArrangementTile planTile )
    {
        this.createdWindow = createdWindow;
        this.planWindow = planWindow;

        this.createdTile = createdTile;
        this.planTile = planTile;
    }

}

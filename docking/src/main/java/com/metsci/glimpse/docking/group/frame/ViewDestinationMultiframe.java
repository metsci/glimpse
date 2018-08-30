package com.metsci.glimpse.docking.group.frame;

import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.group.ViewDestinationBase;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;

public class ViewDestinationMultiframe extends ViewDestinationBase
{

    public final DockingFrame createdFrame;
    public final FrameArrangement planFrame;


    public ViewDestinationMultiframe( DockingFrame createdFrame,
                                      FrameArrangement planFrame,
                                      Tile createdTile,
                                      DockerArrangementTile planTile )
    {
        super( createdTile, planTile );

        this.createdFrame = createdFrame;
        this.planFrame = planFrame;
    }

}

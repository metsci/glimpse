package com.metsci.glimpse.docking.group;

import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;

public interface ViewPlacer<R>
{

    R addToTile( DockerArrangementTile existingTile, int viewNum );

    R addBesideNeighbor( DockerArrangementTile planTile, DockerArrangementNode existingNeighbor, Side sideOfNeighbor, double extentFrac );

}

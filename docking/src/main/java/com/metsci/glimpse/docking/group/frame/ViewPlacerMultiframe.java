package com.metsci.glimpse.docking.group.frame;

import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;

public interface ViewPlacerMultiframe<R>
{

    R addToTile( DockerArrangementTile existingTile, int viewNum );

    R addBesideNeighbor( DockerArrangementTile planTile, DockerArrangementNode existingNeighbor, Side sideOfNeighbor, double extentFrac );

    R addInNewFrame( FrameArrangement planFrame, DockerArrangementTile planTile );

    R addInNewFallbackFrame( );

}

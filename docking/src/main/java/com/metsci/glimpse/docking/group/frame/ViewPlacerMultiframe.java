package com.metsci.glimpse.docking.group.frame;

import java.awt.Component;
import java.util.Map;

import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;

public interface ViewPlacerMultiframe<R>
{

    R addToTile( DockerArrangementTile existingTile, int viewNum, Map<DockerArrangementNode,Component> componentsMap );

    R addBesideNeighbor( DockerArrangementNode existingNeighbor, Side sideOfNeighbor, double extentFrac, Map<DockerArrangementNode,Component> componentsMap );

    R addInNewFrame( FrameArrangement planFrame );

    R addInNewFallbackFrame( );

}

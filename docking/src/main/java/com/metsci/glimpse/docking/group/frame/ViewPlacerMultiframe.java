package com.metsci.glimpse.docking.group.frame;

import com.metsci.glimpse.docking.group.ViewPlacer;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;

public interface ViewPlacerMultiframe<R> extends ViewPlacer<R>
{

    R createNewFrame( FrameArrangement planWindow, DockerArrangementTile planTile );

    R createFallbackNewFrame( );

}

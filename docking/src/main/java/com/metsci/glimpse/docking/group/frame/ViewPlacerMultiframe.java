package com.metsci.glimpse.docking.group.frame;

import com.metsci.glimpse.docking.group.ViewPlacer;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;

public interface ViewPlacerMultiframe<R> extends ViewPlacer<R>
{

    R addInNewFrame( FrameArrangement planFrame, DockerArrangementTile planTile );

    R addInNewFallbackFrame( );

}

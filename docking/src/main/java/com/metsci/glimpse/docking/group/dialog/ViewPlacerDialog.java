package com.metsci.glimpse.docking.group.dialog;

import com.metsci.glimpse.docking.group.ViewPlacer;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;

public interface ViewPlacerDialog<R> extends ViewPlacer<R>
{

    R addInInitialTile( );

    R addInNewWindow( FrameArrangement planDialog, DockerArrangementTile planTile );

    R addInNewFallbackWindow( );

}

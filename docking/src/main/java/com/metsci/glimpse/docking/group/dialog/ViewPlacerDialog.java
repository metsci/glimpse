package com.metsci.glimpse.docking.group.dialog;

import com.metsci.glimpse.docking.group.ViewPlacer;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;

public interface ViewPlacerDialog<R> extends ViewPlacer<R>
{

    R addInNewDialog( FrameArrangement planDialog, DockerArrangementTile planTile );

}

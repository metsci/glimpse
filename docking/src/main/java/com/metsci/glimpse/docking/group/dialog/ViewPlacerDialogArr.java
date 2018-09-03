package com.metsci.glimpse.docking.group.dialog;

import com.metsci.glimpse.docking.group.ViewPlacerBaseArr;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class ViewPlacerDialogArr extends ViewPlacerBaseArr implements ViewPlacerDialog<Void>
{

    public ViewPlacerDialogArr( GroupArrangement groupArr, String newViewId )
    {
        super( groupArr, newViewId );
    }

    @Override
    public Void addInNewDialog( FrameArrangement planDialog, DockerArrangementTile planTile )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( newViewId );
        newTile.selectedViewId = newViewId;
        newTile.isMaximized = false;

        FrameArrangement newDialog = new FrameArrangement( );
        newDialog.dockerArr = newTile;

        newDialog.x = planDialog.x;
        newDialog.y = planDialog.y;
        newDialog.width = planDialog.width;
        newDialog.height = planDialog.height;
        newDialog.isMaximizedHoriz = false;
        newDialog.isMaximizedVert = false;

        this.groupArr.frameArrs.add( newDialog );

        return null;
    }

}

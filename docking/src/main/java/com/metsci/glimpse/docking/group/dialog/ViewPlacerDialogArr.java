package com.metsci.glimpse.docking.group.dialog;

import static com.metsci.glimpse.docking.DockingUtils.fractionOfScreenBounds;

import java.awt.Rectangle;

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
    public Void addInInitialTile( )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( newViewId );
        newTile.selectedViewId = newViewId;
        newTile.isMaximized = false;

        FrameArrangement existingWindow = this.groupArr.frameArrs.get( 0 );
        existingWindow.dockerArr = newTile;

        return null;
    }

    @Override
    public Void addInNewWindow( FrameArrangement planWindow, DockerArrangementTile planTile )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( newViewId );
        newTile.selectedViewId = newViewId;
        newTile.isMaximized = false;

        FrameArrangement newWindow = new FrameArrangement( );
        newWindow.dockerArr = newTile;

        newWindow.x = planWindow.x;
        newWindow.y = planWindow.y;
        newWindow.width = planWindow.width;
        newWindow.height = planWindow.height;
        newWindow.isMaximizedHoriz = false;
        newWindow.isMaximizedVert = false;

        this.groupArr.frameArrs.add( newWindow );

        return null;
    }

    @Override
    public Void addInNewFallbackWindow( )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( newViewId );
        newTile.selectedViewId = newViewId;
        newTile.isMaximized = false;

        FrameArrangement newWindow = new FrameArrangement( );
        newWindow.dockerArr = newTile;

        Rectangle newFrameBounds = fractionOfScreenBounds( 0.40f );
        newWindow.x = newFrameBounds.x;
        newWindow.y = newFrameBounds.y;
        newWindow.width = newFrameBounds.width;
        newWindow.height = newFrameBounds.height;
        newWindow.isMaximizedHoriz = false;
        newWindow.isMaximizedVert = false;

        this.groupArr.frameArrs.add( newWindow );

        return null;
    }

}

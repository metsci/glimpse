package com.metsci.glimpse.docking.group.frame;

import static com.metsci.glimpse.docking.group.DockingGroupUtils.fallbackWindowBounds;

import java.awt.Rectangle;

import com.metsci.glimpse.docking.group.ViewPlacerBaseArr;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class ViewPlacerMultiframeArr extends ViewPlacerBaseArr implements ViewPlacerMultiframe<Void>
{

    public ViewPlacerMultiframeArr( GroupArrangement groupArr, String newViewId )
    {
        super( groupArr, newViewId );
    }

    @Override
    public Void createNewFrame( FrameArrangement planWindow, DockerArrangementTile planTile )
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
        newWindow.isMaximizedHoriz = planWindow.isMaximizedHoriz;
        newWindow.isMaximizedVert = planWindow.isMaximizedVert;

        this.groupArr.frameArrs.add( newWindow );

        return null;
    }

    @Override
    public Void createFallbackNewFrame( )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( newViewId );
        newTile.selectedViewId = newViewId;
        newTile.isMaximized = false;

        FrameArrangement newWindow = new FrameArrangement( );
        newWindow.dockerArr = newTile;

        Rectangle newFrameBounds = fallbackWindowBounds( );
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

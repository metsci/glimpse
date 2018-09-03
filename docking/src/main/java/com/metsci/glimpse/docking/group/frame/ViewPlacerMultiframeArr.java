package com.metsci.glimpse.docking.group.frame;

import static com.metsci.glimpse.docking.group.frame.ViewPlacerMultiframeUtils.fallbackFrameBounds;

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
    public Void addInNewFrame( FrameArrangement planFrame, DockerArrangementTile planTile )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( newViewId );
        newTile.selectedViewId = newViewId;
        newTile.isMaximized = false;

        FrameArrangement newFrame = new FrameArrangement( );
        newFrame.dockerArr = newTile;

        newFrame.x = planFrame.x;
        newFrame.y = planFrame.y;
        newFrame.width = planFrame.width;
        newFrame.height = planFrame.height;
        newFrame.isMaximizedHoriz = planFrame.isMaximizedHoriz;
        newFrame.isMaximizedVert = planFrame.isMaximizedVert;

        this.groupArr.frameArrs.add( newFrame );

        return null;
    }

    @Override
    public Void addInNewFallbackFrame( )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( newViewId );
        newTile.selectedViewId = newViewId;
        newTile.isMaximized = false;

        FrameArrangement newFrame = new FrameArrangement( );
        newFrame.dockerArr = newTile;

        Rectangle newFrameBounds = fallbackFrameBounds( );
        newFrame.x = newFrameBounds.x;
        newFrame.y = newFrameBounds.y;
        newFrame.width = newFrameBounds.width;
        newFrame.height = newFrameBounds.height;
        newFrame.isMaximizedHoriz = false;
        newFrame.isMaximizedVert = false;

        this.groupArr.frameArrs.add( newFrame );

        return null;
    }

}

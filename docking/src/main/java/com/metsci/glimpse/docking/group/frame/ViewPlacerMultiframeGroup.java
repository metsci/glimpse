package com.metsci.glimpse.docking.group.frame;

import static com.metsci.glimpse.docking.DockingUtils.getFrameExtendedState;
import static com.metsci.glimpse.docking.group.frame.ViewPlacerMultiframeUtils.fallbackFrameBounds;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.Map;

import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.group.ViewDestination;
import com.metsci.glimpse.docking.group.ViewPlacerBaseGroup;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;

public class ViewPlacerMultiframeGroup extends ViewPlacerBaseGroup implements ViewPlacerMultiframe<ViewDestination>
{

    protected final DockingGroupMultiframe group;


    public ViewPlacerMultiframeGroup( DockingGroupMultiframe group, Map<DockerArrangementNode,Component> componentsMap, View newView )
    {
        super( group, componentsMap, newView );
        this.group = group;
    }

    @Override
    public ViewDestination addInNewFrame( FrameArrangement planFrame, DockerArrangementTile planTile )
    {
        Tile newTile = this.group.createNewTile( );
        newTile.addView( this.newView, 0 );

        DockingFrame newFrame = this.group.addNewFrame( );
        newFrame.docker( ).addInitialLeaf( newTile );

        newFrame.setBounds( planFrame.x, planFrame.y, planFrame.width, planFrame.height );
        newFrame.setNormalBounds( planFrame.x, planFrame.y, planFrame.width, planFrame.height );
        newFrame.setExtendedState( getFrameExtendedState( planFrame.isMaximizedHoriz, planFrame.isMaximizedVert ) );

        return new ViewDestination( newFrame, planFrame, newTile, planTile );
    }

    @Override
    public ViewDestination addInNewFallbackFrame( )
    {
        Tile newTile = this.group.createNewTile( );
        newTile.addView( this.newView, 0 );

        DockingFrame newFrame = this.group.addNewFrame( );
        newFrame.docker( ).addInitialLeaf( newTile );

        Rectangle newFrameBounds = fallbackFrameBounds( );
        newFrame.setBounds( newFrameBounds );
        newFrame.setNormalBounds( newFrameBounds );
        newFrame.setExtendedState( getFrameExtendedState( false, false ) );

        return new ViewDestination( newFrame, null, newTile, null );
    }

}

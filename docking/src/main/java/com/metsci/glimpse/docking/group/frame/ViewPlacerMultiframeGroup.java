package com.metsci.glimpse.docking.group.frame;

import static com.metsci.glimpse.docking.DockingUtils.fractionOfScreenBounds;
import static com.metsci.glimpse.docking.DockingUtils.getFrameExtendedState;

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


    public ViewPlacerMultiframeGroup( DockingGroupMultiframe group, Map<DockerArrangementNode,Component> existingComponents, View newView )
    {
        super( group, existingComponents, newView );
        this.group = group;
    }

    @Override
    public ViewDestination addInNewWindow( FrameArrangement planWindow, DockerArrangementTile planTile )
    {
        Tile newTile = this.group.tileFactory( ).newTile( );
        newTile.addView( this.newView, 0 );

        DockingFrame newWindow = this.group.addNewFrame( );
        newWindow.docker( ).addInitialLeaf( newTile );

        newWindow.setBounds( planWindow.x, planWindow.y, planWindow.width, planWindow.height );
        newWindow.setNormalBounds( planWindow.x, planWindow.y, planWindow.width, planWindow.height );
        newWindow.setExtendedState( getFrameExtendedState( planWindow.isMaximizedHoriz, planWindow.isMaximizedVert ) );

        return new ViewDestination( newWindow, planWindow, newTile, planTile );
    }

    @Override
    public ViewDestination addInNewFallbackWindow( )
    {
        Tile newTile = this.group.tileFactory( ).newTile( );
        newTile.addView( this.newView, 0 );

        DockingFrame newWindow = this.group.addNewFrame( );
        newWindow.docker( ).addInitialLeaf( newTile );

        Rectangle newFrameBounds = fractionOfScreenBounds( 0.85f );
        newWindow.setBounds( newFrameBounds );
        newWindow.setNormalBounds( newFrameBounds );
        newWindow.setExtendedState( getFrameExtendedState( false, false ) );

        return new ViewDestination( newWindow, null, newTile, null );
    }

}

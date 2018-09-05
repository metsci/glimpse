package com.metsci.glimpse.docking.group.dialog;

import static com.metsci.glimpse.docking.DockingUtils.fractionOfScreenBounds;

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

public class ViewPlacerDialogGroup extends ViewPlacerBaseGroup implements ViewPlacerDialog<ViewDestination>
{

    protected final DockingGroupDialog group;


    public ViewPlacerDialogGroup( DockingGroupDialog group, Map<DockerArrangementNode,Component> existingComponents, View newView )
    {
        super( group, existingComponents, newView );
        this.group = group;
    }

    @Override
    public ViewDestination addInInitialTile( )
    {
        Tile newTile = this.group.tileFactory( ).newTile( );
        newTile.addView( this.newView, 0 );

        DockingDialog newDialog = this.group.requireDialog( );
        newDialog.docker( ).addInitialLeaf( newTile );

        return new ViewDestination( newDialog, null, newTile, null );
    }

    @Override
    public ViewDestination addInNewWindow( FrameArrangement planDialog, DockerArrangementTile planTile )
    {
        Tile newTile = this.group.tileFactory( ).newTile( );
        newTile.addView( this.newView, 0 );

        DockingDialog newDialog = this.group.initDialog( );
        newDialog.docker( ).addInitialLeaf( newTile );

        newDialog.setBounds( planDialog.x, planDialog.y, planDialog.width, planDialog.height );

        return new ViewDestination( newDialog, planDialog, newTile, planTile );
    }

    @Override
    public ViewDestination addInNewFallbackWindow( )
    {
        Tile newTile = this.group.tileFactory( ).newTile( );
        newTile.addView( this.newView, 0 );

        DockingDialog newDialog = this.group.initDialog( );
        newDialog.docker( ).addInitialLeaf( newTile );

        Rectangle newDialogBounds = fractionOfScreenBounds( 0.40f );
        newDialog.setBounds( newDialogBounds );

        return new ViewDestination( newDialog, null, newTile, null );
    }

}

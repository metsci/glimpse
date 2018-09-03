package com.metsci.glimpse.docking.group.dialog;

import java.awt.Component;
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


    public ViewPlacerDialogGroup( DockingGroupDialog group, Map<DockerArrangementNode,Component> componentsMap, View newView )
    {
        super( group, componentsMap, newView );
        this.group = group;
    }

    @Override
    public ViewDestination addInNewDialog( FrameArrangement planDialog, DockerArrangementTile planTile )
    {
        Tile newTile = this.group.createNewTile( );
        newTile.addView( this.newView, 0 );

        // FIXME
        DockingDialog dialog = this.group.ensureDialog( );
        dialog.docker( ).addInitialLeaf( newTile );

        dialog.setBounds( planFrame.x, planFrame.y, planFrame.width, planFrame.height );

        return new ViewDestination( newFrame, planFrame, newTile, planTile );
    }

}

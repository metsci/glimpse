/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.docking.group.dialog;

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
    protected final Rectangle fallbackWindowBounds;


    public ViewPlacerDialogGroup( DockingGroupDialog group, Map<DockerArrangementNode,Component> existingComponents, View newView, Rectangle fallbackWindowBounds )
    {
        super( group, existingComponents, newView );
        this.group = group;
        this.fallbackWindowBounds = new Rectangle( fallbackWindowBounds );
    }

    @Override
    public ViewDestination createInitialTile( )
    {
        Tile newTile = this.group.tileFactory( ).newTile( );
        newTile.addView( this.newView, 0 );

        DockingDialog newDialog = this.group.requireDialog( );
        newDialog.docker( ).addInitialLeaf( newTile );

        return new ViewDestination( newDialog, null, newTile, null );
    }

    @Override
    public ViewDestination createSoleDialog( FrameArrangement planWindow, DockerArrangementTile planTile )
    {
        ViewDestination d = this.createSoleDialog( new Rectangle( planWindow.x, planWindow.y, planWindow.width, planWindow.height ) );
        return new ViewDestination( d.createdWindow, planWindow, d.createdTile, planTile );
    }

    @Override
    public ViewDestination createFallbackSoleDialog( )
    {
        return this.createSoleDialog( this.fallbackWindowBounds );
    }

    @Override
    public ViewDestination createSoleDialog( Rectangle bounds )
    {
        Tile newTile = this.group.tileFactory( ).newTile( );
        newTile.addView( this.newView, 0 );

        DockingDialog newDialog = this.group.initDialog( );
        newDialog.docker( ).addInitialLeaf( newTile );
        newDialog.setBounds( bounds );

        return new ViewDestination( newDialog, null, newTile, null );
    }

}

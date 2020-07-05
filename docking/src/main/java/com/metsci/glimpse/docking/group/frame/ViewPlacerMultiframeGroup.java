/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.docking.group.frame;

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
    protected final Rectangle fallbackWindowBounds;


    public ViewPlacerMultiframeGroup( DockingGroupMultiframe group, Map<DockerArrangementNode,Component> existingComponents, View newView, Rectangle fallbackWindowBounds )
    {
        super( group, existingComponents, newView );
        this.group = group;
        this.fallbackWindowBounds = new Rectangle( fallbackWindowBounds );
    }

    @Override
    public ViewDestination createNewFrame( FrameArrangement planWindow, DockerArrangementTile planTile )
    {
        ViewDestination d = this.createNewFrame( new Rectangle( planWindow.x, planWindow.y, planWindow.width, planWindow.height ),
                                                 planWindow.isMaximizedHoriz,
                                                 planWindow.isMaximizedVert );

        return new ViewDestination( d.createdWindow, planWindow, d.createdTile, planTile );
    }

    @Override
    public ViewDestination createFallbackNewFrame( )
    {
        return this.createNewFrame( this.fallbackWindowBounds, false, false );
    }

    @Override
    public ViewDestination createNewFrame( Rectangle bounds, boolean isMaximizedHoriz, boolean isMaximizedVert )
    {
        Tile newTile = this.group.tileFactory( ).newTile( );
        newTile.addView( this.newView, 0 );

        DockingFrame newWindow = this.group.addNewFrame( );
        newWindow.docker( ).addInitialLeaf( newTile );

        newWindow.setBounds( bounds );
        newWindow.setNormalBounds( bounds );
        newWindow.setExtendedState( getFrameExtendedState( isMaximizedHoriz, isMaximizedVert ) );

        return new ViewDestination( newWindow, null, newTile, null );
    }

}

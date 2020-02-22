/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.docking.group;

import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;

import java.awt.Component;
import java.util.Map;

import com.metsci.glimpse.docking.MultiSplitPane;
import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;

public class ViewPlacerBaseGroup implements ViewPlacer<ViewDestination>
{

    protected final DockingGroupBase group;
    protected final Map<DockerArrangementNode,Component> existingComponents;
    protected final View newView;


    public ViewPlacerBaseGroup( DockingGroupBase group, Map<DockerArrangementNode,Component> existingComponents, View newView )
    {
        this.group = group;
        this.existingComponents = existingComponents;
        this.newView = newView;
    }

    @Override
    public ViewDestination addToTile( DockerArrangementTile existingTile, int newViewNum )
    {
        Tile tile = ( Tile ) this.existingComponents.get( existingTile );
        tile.addView( this.newView, newViewNum );
        return new ViewDestination( null, null, null, null );
    }

    @Override
    public ViewDestination addBesideNeighbor( DockerArrangementTile planTile, DockerArrangementNode existingNeighbor, Side sideOfNeighbor, double extentFrac )
    {
        Tile newTile = this.group.tileFactory( ).newTile( );
        newTile.addView( this.newView, 0 );

        Component neighbor = this.existingComponents.get( existingNeighbor );

        MultiSplitPane docker = getAncestorOfClass( MultiSplitPane.class, neighbor );
        docker.addNeighborLeaf( newTile, neighbor, sideOfNeighbor, extentFrac );

        return new ViewDestination( null, null, newTile, planTile );
    }

}

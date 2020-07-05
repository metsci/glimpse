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
package com.metsci.glimpse.docking.group;

import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.TOP;
import static com.metsci.glimpse.docking.group.ArrangementUtils.replaceArrNode;

import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementSplit;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class ViewPlacerBaseArr implements ViewPlacer<Void>
{

    protected final GroupArrangement groupArr;
    protected final String newViewId;


    public ViewPlacerBaseArr( GroupArrangement groupArr, String newViewId )
    {
        this.groupArr = groupArr;
        this.newViewId = newViewId;
    }

    @Override
    public Void addToTile( DockerArrangementTile existingTile, int viewNum )
    {
        existingTile.viewIds.add( viewNum, this.newViewId );
        return null;
    }

    @Override
    public Void addBesideNeighbor( DockerArrangementTile planTile, DockerArrangementNode existingNeighbor, Side sideOfNeighbor, double extentFrac )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( this.newViewId );
        newTile.selectedViewId = this.newViewId;
        newTile.isMaximized = false;

        DockerArrangementSplit newSplit = new DockerArrangementSplit( );
        newSplit.arrangeVertically = ( sideOfNeighbor == TOP || sideOfNeighbor == BOTTOM );
        boolean newIsChildA = ( sideOfNeighbor == LEFT || sideOfNeighbor == TOP );
        newSplit.childA = ( newIsChildA ? newTile : existingNeighbor );
        newSplit.childB = ( newIsChildA ? existingNeighbor : newTile );
        newSplit.splitFrac = ( newIsChildA ? extentFrac : 1.0 - extentFrac );

        replaceArrNode( this.groupArr, existingNeighbor, newSplit );

        return null;
    }

}

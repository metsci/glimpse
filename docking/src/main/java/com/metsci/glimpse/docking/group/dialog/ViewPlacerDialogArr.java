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
package com.metsci.glimpse.docking.group.dialog;

import java.awt.Rectangle;

import com.metsci.glimpse.docking.group.ViewPlacerBaseArr;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.FrameArrangement;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class ViewPlacerDialogArr extends ViewPlacerBaseArr implements ViewPlacerDialog<Void>
{

    protected final Rectangle fallbackWindowBounds;


    public ViewPlacerDialogArr( GroupArrangement groupArr, String newViewId, Rectangle fallbackWindowBounds )
    {
        super( groupArr, newViewId );
        this.fallbackWindowBounds = new Rectangle( fallbackWindowBounds );
    }

    @Override
    public Void createInitialTile( )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( newViewId );
        newTile.selectedViewId = newViewId;
        newTile.isMaximized = false;

        FrameArrangement existingWindow = this.groupArr.frameArrs.get( 0 );
        existingWindow.dockerArr = newTile;

        return null;
    }

    @Override
    public Void createSoleDialog( FrameArrangement planWindow, DockerArrangementTile planTile )
    {
        return this.createSoleDialog( new Rectangle( planWindow.x, planWindow.y, planWindow.width, planWindow.height ) );
    }

    @Override
    public Void createFallbackSoleDialog( )
    {
        return this.createSoleDialog( this.fallbackWindowBounds );
    }

    @Override
    public Void createSoleDialog( Rectangle bounds )
    {
        DockerArrangementTile newTile = new DockerArrangementTile( );
        newTile.viewIds.add( newViewId );
        newTile.selectedViewId = newViewId;
        newTile.isMaximized = false;

        FrameArrangement newWindow = new FrameArrangement( );
        newWindow.dockerArr = newTile;

        newWindow.x = bounds.x;
        newWindow.y = bounds.y;
        newWindow.width = bounds.width;
        newWindow.height = bounds.height;
        newWindow.isMaximizedHoriz = false;
        newWindow.isMaximizedVert = false;

        this.groupArr.frameArrs.add( newWindow );

        return null;
    }

}

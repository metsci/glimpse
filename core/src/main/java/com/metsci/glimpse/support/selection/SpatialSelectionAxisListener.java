/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.support.selection;

import java.util.Collection;
import java.util.Collections;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.RateLimitedAxisListener2D;
import com.metsci.glimpse.painter.track.Point;
import com.metsci.glimpse.painter.track.TrackPainter;

public class SpatialSelectionAxisListener extends RateLimitedAxisListener2D
{
    protected TrackPainter painter;
    protected SpatialSelectionListener<Point> listener;
    
    public SpatialSelectionAxisListener( TrackPainter painter, SpatialSelectionListener<Point> listener )
    {
        this.painter = painter;
        this.listener = listener;
    }

    @Override
    public void axisUpdatedRateLimited( Axis2D axis )
    {
        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        double centerX = axisX.getSelectionCenter( );
        double sizeX = axisX.getSelectionSize( ) / 2.0f;

        double centerY = axisY.getSelectionCenter( );
        double sizeY = axisY.getSelectionSize( ) / 2.0f;

        double minX = centerX - sizeX;
        double maxX = centerX + sizeX;

        double minY = centerY - sizeY;
        double maxY = centerY + sizeY;

        Collection<Point> selection = Collections.unmodifiableCollection( painter.getTimeGeoRange( minX, maxX, minY, maxY ) );

        listener.selectionChanged( selection );
    }

}

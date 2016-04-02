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
package com.metsci.glimpse.axis.painter.label;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.format.Util;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * Interprets axis values as latitude or longitude values under a
 * {@link com.metsci.glimpse.util.geo.projection.GeoProjection}.
 *
 * @author ulman
 */
public class LatLonAxisLabelHandler extends GridAxisLabelHandler
{
    protected GeoProjection geoProjection;
    protected boolean longitude;

    public LatLonAxisLabelHandler( GeoProjection geoProjection, boolean longitude )
    {
        super( );

        this.geoProjection = geoProjection;
        this.longitude = longitude;
    }

    @Override
    public String[] getTickLabels( Axis1D axis, double[] tickPositions )
    {
        String[] tickLabels = new String[tickPositions.length];
        for ( int i = 0; i < tickPositions.length; i++ )
        {
            double tickPosition = tickPositions[i];

            String tickString;

            if ( longitude )
            {
                LatLonGeo latLon = geoProjection.unproject( tickPosition, 0 );
                tickString = Util.toDegreesMinutesSeconds( latLon.getLonDeg( ), 0, true );
            }
            else
            {
                LatLonGeo latLon = geoProjection.unproject( 0, tickPosition );
                tickString = Util.toDegreesMinutesSeconds( latLon.getLatDeg( ), 0, false );
            }

            tickLabels[i] = tickString;
        }

        return tickLabels;
    }

    @Override
    public String getAxisLabel( Axis1D axis )
    {
        return axisLabel;
    }
}

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
package com.metsci.glimpse.util.geo.util;

import com.metsci.glimpse.util.units.Azimuth;

/**
 * @author moskowitz
 */
public class DistanceAzimuth
{
    private final double distance;
    private final double azimuth;

    public DistanceAzimuth( double distance, double azimuth )
    {
        this.distance = distance;
        this.azimuth = azimuth;
    }

    /**
     * Factory method constructs DistanceAzimuth equivalent to combination of given distances East
     * and North. (Note: Negative values represent distances West and South respectively.)  Azimuth
     * is based on directions at the start, meaning for instance if one passes a pole the directions
     * do not switch midway through.
     *
     * @param   distanceEast
     * @param   distanceNorth
     * @return  equivalent DistanceAzimuth
     */
    public static DistanceAzimuth fromEastNorth( double distanceEast, double distanceNorth )
    {
        double dist = Math.sqrt( ( distanceEast * distanceEast ) + ( distanceNorth * distanceNorth ) );
        double mathRad = Math.atan2( distanceNorth, distanceEast );
        double azim = Azimuth.fromMathRad( mathRad );

        return new DistanceAzimuth( dist, azim );
    }

    public final double getDistance( )
    {
        return distance;
    }

    public final double getAzimuth( )
    {
        return azimuth;
    }

    @Override
    public String toString( )
    {
        return String.format( "<dist %.5f, azim %.5f>", distance, azimuth );
    }
}

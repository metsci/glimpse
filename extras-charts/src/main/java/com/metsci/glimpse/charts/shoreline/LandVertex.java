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
package com.metsci.glimpse.charts.shoreline;

import com.metsci.glimpse.util.geo.datum.DatumWgs84;
import com.metsci.glimpse.util.units.Angle;

/**
 * LandVertex is a bare-bones lat/lon class, lacking most nice features
 * of LatLon. It is necessary because LatLons normalize themselves, which
 * causes land polygons to wrap poorly around the 180 degree longitude line.
 *
 * Lat and lon are stored in unnormalized degrees.
 *
 * @author hogye
 */
public class LandVertex
{
    private static final double distancePerDegree_SU = Math.PI * DatumWgs84.Constants.meanRadius / 180.0;

    public final double lat;
    public final double lon;

    public LandVertex(double lat_DEG, double lon_DEG)
    {
        lat = lat_DEG;
        lon = lon_DEG;
    }

    /**
     * Returns signed east-west distance from this vertex to the given
     * longitude.
     *
     * The latitude of this vertex is used in converting longitudinal
     * degrees to distance.
     */
    public double getDistanceX_SU(double longitude)
    {
        return (longitude - lon) * Math.cos(Angle.degreesToRadians(lat)) * distancePerDegree_SU;
    }

    public double getDistanceX_SU(LandVertex vertex)
    {
        return getDistanceX_SU(vertex.lon);
    }

    /**
     * Returns signed north-south distance from this vertex to the given
     * latitude.
     */
    public double getDistanceY_SU(double latitude)
    {
        return (latitude - lat) *  distancePerDegree_SU;
    }

    public double getDistanceY_SU(LandVertex vertex)
    {
        return getDistanceY_SU(vertex.lat);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;

        if (!(o instanceof LandVertex)) return false;

        LandVertex v = (LandVertex) o;
        return v.lat == lat && v.lon == lon;
    }

    @Override
    public int hashCode()
    {
        long a = Double.doubleToLongBits(lat);
        int a1 = (int) (a & 0xffffff);
        int a2 = (int) ((a >> 32) & 0xffffff);

        long b = Double.doubleToLongBits(lon);
        int b1 = (int) (b & 0xffffff);
        int b2 = (int) ((b >> 32) & 0xffffff);

        int hash = getClass().getName().hashCode();
        hash = 31*hash + a1;
        hash = 31*hash + a2;
        hash = 31*hash + b1;
        hash = 31*hash + b2;
        return hash;
    }

}

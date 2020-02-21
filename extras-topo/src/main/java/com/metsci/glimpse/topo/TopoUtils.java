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
package com.metsci.glimpse.topo;

import static com.metsci.glimpse.util.units.Angle.radiansToDegrees;
import static java.lang.Math.max;
import static java.lang.Math.min;

import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.topo.io.TopoDataType;
import com.metsci.glimpse.topo.proj.NormalCylindricalProjection;

public class TopoUtils
{

    public static LatLonBox axisBounds( Axis2D axis, NormalCylindricalProjection proj )
    {
        double lonA_DEG = radiansToDegrees( proj.xToLon_RAD( axis.getMinX( ) ) );
        double lonB_DEG = radiansToDegrees( proj.xToLon_RAD( axis.getMaxX( ) ) );
        double latA_DEG = radiansToDegrees( proj.yToLat_RAD( axis.getMinY( ) ) );
        double latB_DEG = radiansToDegrees( proj.yToLat_RAD( axis.getMaxY( ) ) );

        double northLat_DEG = max( latA_DEG, latB_DEG );
        double southLat_DEG = min( latA_DEG, latB_DEG );
        double eastLon_DEG = max( lonA_DEG, lonB_DEG );
        double westLon_DEG = min( lonA_DEG, lonB_DEG );

        return new LatLonBox( northLat_DEG, southLat_DEG, eastLon_DEG, westLon_DEG );
    }

    public static LatLonBox intersect( LatLonBox box, TopoTileBounds tile )
    {
        double northLat_DEG = max( box.northLat_DEG, tile.northLat_DEG );
        double southLat_DEG = min( box.southLat_DEG, tile.southLat_DEG );
        double eastLon_DEG = max( box.eastLon_DEG, tile.eastLon_DEG );
        double westLon_DEG = min( box.westLon_DEG, tile.westLon_DEG );

        return new LatLonBox( northLat_DEG, southLat_DEG, eastLon_DEG, westLon_DEG );
    }

    public static float dataDenormFactor( TopoDataType dataType )
    {
        switch ( dataType )
        {
            case TOPO_I2: return 32767f;
            case TOPO_F4: return 1f;
            default: throw new RuntimeException( "Unrecognized data type: " + dataType );
        }
    }

}

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
package com.metsci.glimpse.dnc.proj;

import static com.metsci.glimpse.util.units.Angle.degreesToRadians;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class DncEquirectProjection implements DncProjection
{

    public final double originLon_DEG;


    public DncEquirectProjection( double originLon_DEG )
    {
        this.originLon_DEG = originLon_DEG;
    }

    @Override
    public String configString( )
    {
        return "Equirect[ " + this.originLon_DEG + " ]";
    }

    @Override
    public double suggestedPpvMultiplier( )
    {
        return 1.0;
    }

    @Override
    public boolean canProjectLibrary( int databaseNum, String libraryName, double minLat_DEG, double maxLat_DEG, double minLon_DEG, double maxLon_DEG )
    {
        return true;
    }

    @Override
    public void projectPos( double lat_DEG, double lon_DEG, float[] result, int resultOffset )
    {
        result[ resultOffset + 0 ] = ( float ) ( lon_DEG - this.originLon_DEG );
        result[ resultOffset + 1 ] = ( float ) lat_DEG;
    }

    @Override
    public double projectAzimuth_MATHRAD( double x, double y, double azimuth_MATHRAD )
    {
        double cos_LOCAL = cos( azimuth_MATHRAD );
        double sin_LOCAL = sin( azimuth_MATHRAD );

        double lat_DEG = y;
        double cosLat = cos( degreesToRadians( lat_DEG ) );
        double cos_PROJ = cos_LOCAL / cosLat;
        double sin_PROJ = sin_LOCAL;

        return atan2( sin_PROJ, cos_PROJ );
    }

}

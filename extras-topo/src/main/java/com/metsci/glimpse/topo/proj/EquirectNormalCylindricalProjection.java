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
package com.metsci.glimpse.topo.proj;

import static com.metsci.glimpse.util.math.MathConstants.HALF_PI;
import static com.metsci.glimpse.util.units.Angle.degreesToRadians;
import static com.metsci.glimpse.util.units.Angle.radiansToDegrees;

public class EquirectNormalCylindricalProjection implements NormalCylindricalProjection
{

    public static final EquirectNormalCylindricalProjection plateCarreeProj_DEG = new EquirectNormalCylindricalProjection( 0.0, true );
    public static final EquirectNormalCylindricalProjection plateCarreeProj_RAD = new EquirectNormalCylindricalProjection( 0.0, false );


    public final double originLon_RAD;
    public final boolean xyInDegrees;


    public EquirectNormalCylindricalProjection( double originLon_RAD, boolean xyInDegrees )
    {
        this.originLon_RAD = originLon_RAD;
        this.xyInDegrees = xyInDegrees;
    }

    @Override
    public double xToLon_RAD( double x )
    {
        double x_RAD = ( this.xyInDegrees ? degreesToRadians( x ) : x );
        return ( this.originLon_RAD + x_RAD );
    }

    @Override
    public double lonToX( double lon_RAD )
    {
        double x_RAD = lon_RAD - this.originLon_RAD;
        return ( this.xyInDegrees ? radiansToDegrees( x_RAD ) : x_RAD );
    }

    @Override
    public double yToLat_RAD( double y )
    {
        double y_RAD = ( this.xyInDegrees ? degreesToRadians( y ) : y );
        return y_RAD;
    }

    @Override
    public double latToY( double lat_RAD )
    {
        double y_RAD = lat_RAD;
        return ( this.xyInDegrees ? radiansToDegrees( y_RAD ) : y_RAD );
    }

    @Override
    public double dLatDy_RAD( double y )
    {
        return ( this.xyInDegrees ? degreesToRadians( 1.0 ) : 1.0 );
    }

    @Override
    public double maxDlatDy_RAD( double yMin, double yMax )
    {
        return ( this.xyInDegrees ? degreesToRadians( 1.0 ) : 1.0 );
    }

    @Override
    public double minUsableY( )
    {
        return ( this.xyInDegrees ? -90.0 : -HALF_PI );
    }

    @Override
    public double maxUsableY( )
    {
        return ( this.xyInDegrees ? +90.0 : +HALF_PI );
    }

}

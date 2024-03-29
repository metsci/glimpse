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
package com.metsci.glimpse.topo.proj;

import static com.metsci.glimpse.util.math.MathConstants.HALF_PI;
import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.sin;

public class MercatorNormalCylindricalProjection implements NormalCylindricalProjection
{

    public static final MercatorNormalCylindricalProjection standardMercatorProj = new MercatorNormalCylindricalProjection( 0.0 );


    public final double originLon_RAD;

    protected final double yCutoff;


    public MercatorNormalCylindricalProjection( double originLon_RAD )
    {
        // yCutoff of PI is a reasonable default, and happens to make the bounds square
        this( originLon_RAD, PI );
    }

    public MercatorNormalCylindricalProjection( double originLon_RAD, double yCutoff )
    {
        this.originLon_RAD = originLon_RAD;
        this.yCutoff = yCutoff;
    }

    @Override
    public double xToLon_RAD( double x )
    {
        return ( originLon_RAD + x );
    }

    @Override
    public double lonToX( double lon_RAD )
    {
        return ( lon_RAD - originLon_RAD );
    }

    @Override
    public double yToLat_RAD( double y )
    {
        return ( ( 2.0 * atan( exp( y ) ) ) - HALF_PI );
    }

    @Override
    public double latToY( double lat_RAD )
    {
        return log( ( sin( lat_RAD ) + 1.0 ) / cos( lat_RAD ) );
    }

    @Override
    public double dLatDy_RAD( double y )
    {
        double expY = exp( y );
        return ( ( 2.0 * expY ) / ( 1.0 + expY*expY ) );
    }

    @Override
    public double maxDlatDy_RAD( double yMin, double yMax )
    {
        // dlat/dy has a global maximum at y=0, and decreases monotonically
        // as y gets farther from 0 -- so we want to find dlat/dy at the y value
        // within [yMin,yMax] that is as close as possible to y=0

        if ( yMin <= 0.0 && 0.0 <= yMax )
        {
            // 0 ∊ [yMin,yMax], so use y=0
            //return this.dyToDlat_DEG( 0.0 );
            return 1.0;
        }
        else if ( yMin > 0.0 )
        {
            // The closest we can get to y=0 is yMin
            return this.dLatDy_RAD( yMin );
        }
        else
        {
            // The closest we can get to y=0 is yMax
            return this.dLatDy_RAD( yMax );
        }
    }

    @Override
    public double minUsableY( )
    {
        return ( -1.0 * this.yCutoff );
    }

    @Override
    public double maxUsableY( )
    {
        return ( +1.0 * this.yCutoff );
    }

}

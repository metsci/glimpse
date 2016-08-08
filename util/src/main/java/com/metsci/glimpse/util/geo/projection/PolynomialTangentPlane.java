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
/*
 *
 *  SBIR Data Rights
 *  Contract No.:  N00024-10-C-5222
 *  Contractor Name:  Metron, Inc.
 *  Address:  1818 Library Street, Suite 600, Reston, VA 20190-5631
 *  Expiration of SBIR Data Rights Period:  August 2, 2019
 *
 *  The Government's rights to use, modify, reproduce, release, perform, display,
 *  or disclose technical data or computer software marked with this legend are
 *  restricted during the period shown as provided in paragraph b)(4) of the Rights
 *  in Noncommercial Technical Data and Computer Software-Small Business
 *  Innovative Research (SBIR) Program clause contained in the above identified
 *  contract.  No restrictions apply after the expiration date shown above.  Any
 *  reproduction of technical data, computer software, or portions thereof marked
 *  with this legend must also reproduce the markings.
 *
 *  (End of legend)
 *   Metron Incorporated.  All rights reserved.
 * /
 */

package com.metsci.glimpse.util.geo.projection;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.math.fast.PolynomialApprox;

/**
 * Implementation of GeoProjection via a plane which is tangent to the Earth and maps x, y
 * coordinates on the plane to/from lat/lon pairs.  Class instances are immutable.
 *
 * This version uses a polynomial approximation to arctangent instead of Math.atan2 for
 * improved performance at some cost to accuracy.
 *
 * Created by B. Moskowitz on 6/17/2015.
 */
public class PolynomialTangentPlane extends TangentPlane
{
    public PolynomialTangentPlane( LatLonGeo latLon )
    {
        super( latLon );
    }

    public PolynomialTangentPlane( LatLonGeo latLon, double tangentPointOnPlaneX, double tangentPointOnPlaneY )
    {
        super( latLon, tangentPointOnPlaneX, tangentPointOnPlaneY );
    }

    @Override
    protected double calcAtan2( double y, double x )
    {
        return PolynomialApprox.atan2( y, x );
    }
}

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
    public PolynomialTangentPlane(LatLonGeo latLon)
    {
        super(latLon);
    }

    public PolynomialTangentPlane(LatLonGeo latLon, double tangentPointOnPlaneX, double tangentPointOnPlaneY)
    {
        super(latLon, tangentPointOnPlaneX, tangentPointOnPlaneY);
    }

    @Override
    protected double calcAtan2(double y, double x)
    {
        double polyValue = PolynomialApprox.atan2(y, x);

//        double slowValue = Math.atan2(y, x);
//        double deltaPoly = polyValue - slowValue;

//        if (Math.abs(deltaPoly) > 1.0e-6)
//        {
//            logWarning(logger, "deltaPoly %4g at y %.4f, x %.4f, slow %.8f", deltaPoly, y, x, slowValue);
//        }

        return polyValue;
    }
}

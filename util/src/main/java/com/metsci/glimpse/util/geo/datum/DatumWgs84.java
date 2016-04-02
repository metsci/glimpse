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
package com.metsci.glimpse.util.geo.datum;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.units.Length;

/**
 * @author osborn
 */
public class DatumWgs84 extends Datum
{
    // IODH idiom avoids cycles in class initialization
    public static final class Constants
    {
        public static final double meanRadius = Length.fromMeters( 6371008.7714d ); // arithmetic mean
        public static final double equatorialRadius = Length.fromMeters( 6378137.0d );
        public static final double flattening = 1.0 / 298.257223563;
    }

    public DatumWgs84( )
    {
        super( Constants.equatorialRadius, Constants.flattening );
    }

    @Override
    public LatLonGeo toWgs84( LatLonGeo ll )
    {
        return ll;
    }

    @Override
    public DatumSphere getSphereApproximation( )
    {
        return Datum.wgs84sphere;
    }
}

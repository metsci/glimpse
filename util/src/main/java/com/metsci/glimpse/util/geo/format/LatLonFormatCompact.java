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
package com.metsci.glimpse.util.geo.format;

import com.metsci.glimpse.util.geo.LatLonGeo;

/**
 * LatLonFormat which formats a LatLonGeo as a String containing lat and lon as degrees in decimal
 * form separated by a comma.
 *
 * @author osborn
 */
public class LatLonFormatCompact implements LatLonFormat
{
    private final String formatString;

    /**
     * Simple constructor.
     *
     * @param  nDecimals  number of decimal places to be shown for each of lat and lon (equivalent
     *                    to using formatString "%.[nDecimals]f")
     */
    public LatLonFormatCompact( int nDecimals )
    {
        formatString = "%." + nDecimals + "f";
    }

    @Override
    public String format( LatLonGeo latLon )
    {
        String result;
        if ( latLon == null )
        {
            result = null;
        }
        else
        {
            double latDeg = latLon.getLatDeg( );
            double lonDeg = latLon.getLonDeg( );
            result = String.format( formatString + "," + formatString, latDeg, lonDeg );
        }

        return result;
    }

    @Override
    public LatLonGeo parse( String s ) throws LatLonFormatParseException
    {
        return parseToLatLonGeo( s );
    }

    public static LatLonGeo parseToLatLonGeo( String s ) throws LatLonFormatParseException
    {
        String[] latLonString = s.split( "," );
        if ( latLonString.length < 2 )
        {
            throw new LatLonFormatParseException( s, "fewer than 2 components" );
        }

        try
        {
            double lat = Double.parseDouble( latLonString[0] );
            double lon = Double.parseDouble( latLonString[1] );

            return new LatLonGeo( lat, lon );
        }
        catch ( NumberFormatException nfe )
        {
            throw new LatLonFormatParseException( s, nfe );
        }
    }
}

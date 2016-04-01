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

import java.text.NumberFormat;

/**
 * @author osborn
 */
public class Util
{
    public static final String degreeSign = "\u00b0";
    public static final String minuteSign = "'";
    public static final String secondSign = "\"";

    public final static String toDegreesMinutesSeconds( double degrees, int nDecimals, boolean longitude )
    {
        double[] dms = toDegreesMinutesSeconds( degrees );
        int deg = ( int ) dms[0];
        int min = ( int ) dms[1];
        double sec = dms[2];

        NumberFormat secondsFormatter = NumberFormat.getInstance( );
        secondsFormatter.setMinimumFractionDigits( nDecimals );
        secondsFormatter.setMaximumFractionDigits( nDecimals );
        secondsFormatter.setMinimumIntegerDigits( 2 );

        String hemisphere = longitude ? ( degrees >= 0.0 ? "E" : "W" ) : ( degrees >= 0.0 ? "N" : "S" );

        String degString = Integer.toString( Math.abs( deg ) );
        String zeroes = "00".substring( 0, ( longitude ? 3 : 2 ) - degString.length( ) );
        degString = zeroes + degString;

        return degString + degreeSign + ( min < 10 ? "0" : "" ) + min + minuteSign + secondsFormatter.format( sec ) + secondSign + hemisphere;
    }

    public final static String toDegreesMinutes( double degrees, int nDecimals, boolean longitude )
    {
        double[] dms = toDegreesMinutes( degrees );
        int deg = ( int ) dms[0];
        double min = dms[1];

        NumberFormat minutesFormatter = NumberFormat.getInstance( );
        minutesFormatter.setMinimumFractionDigits( nDecimals );
        minutesFormatter.setMaximumFractionDigits( nDecimals );
        minutesFormatter.setMinimumIntegerDigits( 2 );

        String hemisphere = longitude ? ( degrees >= 0.0 ? "E" : "W" ) : ( degrees >= 0.0 ? "N" : "S" );

        String degString = Integer.toString( Math.abs( deg ) );
        String zeroes = "00".substring( 0, ( longitude ? 3 : 2 ) - degString.length( ) );
        degString = zeroes + degString;

        return degString + degreeSign + minutesFormatter.format( min ) + minuteSign + hemisphere;
    }

    public final static String toDegrees( double degrees, int nDecimals, boolean longitude )
    {
        NumberFormat degreesFormatter = NumberFormat.getInstance( );
        degreesFormatter.setMinimumFractionDigits( nDecimals );
        degreesFormatter.setMaximumFractionDigits( nDecimals );
        degreesFormatter.setMinimumIntegerDigits( longitude ? 3 : 2 );

        String hemisphere = longitude ? ( degrees >= 0.0 ? "E" : "W" ) : ( degrees >= 0.0 ? "N" : "S" );

        String degString = Integer.toString( ( int ) Math.abs( Math.floor( degrees ) ) );
        String zeroes = "00".substring( 0, ( longitude ? 3 : 2 ) - degString.length( ) );
        degString = zeroes + degString;

        return degreesFormatter.format( degrees ) + degreeSign + hemisphere;
    }

    public final static double[] toDegreesMinutesSeconds( double degrees )
    {
        double sign = ( degrees >= 0.0 ) ? 1.0 : -1.0;
        degrees = Math.abs( degrees );
        double[] dms = new double[3];
        dms[0] = Math.floor( degrees );
        double minutes = ( degrees - dms[0] ) * 60.0;
        dms[1] = Math.floor( minutes );
        dms[2] = ( minutes - dms[1] ) * 60.0;
        dms[0] *= sign;
        return dms;
    }

    public final static double[] toDegreesMinutes( double degrees )
    {
        double sign = ( degrees >= 0.0 ) ? 1.0 : -1.0;
        degrees = Math.abs( degrees );
        double[] dms = new double[2];
        dms[0] = Math.floor( degrees );
        double minutes = ( degrees - dms[0] ) * 60.0;
        dms[1] = minutes;
        dms[0] *= sign;
        return dms;
    }
}

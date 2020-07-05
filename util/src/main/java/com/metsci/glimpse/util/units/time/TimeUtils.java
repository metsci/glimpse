/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.util.units.time;

import static com.metsci.glimpse.util.units.time.format.TimeStampFormat.*;

import java.math.BigDecimal;

import com.metsci.glimpse.util.units.time.format.TimeStampFormat;

public class TimeUtils
{

    public static long parseTime_PMILLIS( String s_ISO8601 )
    {
        return parseTime_PMILLIS( s_ISO8601, iso8601 );
    }

    public static long parseTime_PMILLIS( String s, TimeStampFormat format )
    {
        BigDecimal t_PSEC = format.parse( s );
        BigDecimal t_PMILLIS = t_PSEC.scaleByPowerOfTen( 3 );
        return t_PMILLIS.setScale( 0, BigDecimal.ROUND_HALF_UP ).longValue( );
    }

    public static String formatTime_ISO8601( long t_PMILLIS )
    {
        return formatTime( t_PMILLIS, iso8601 );
    }

    public static String formatTime( long t_PMILLIS, TimeStampFormat format )
    {
        BigDecimal t_PSEC = BigDecimal.valueOf( t_PMILLIS, 3 );
        return format.format( t_PSEC );
    }

}

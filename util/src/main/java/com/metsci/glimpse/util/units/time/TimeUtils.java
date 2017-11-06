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

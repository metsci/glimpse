package com.metsci.glimpse.util.geo.format;

import static com.metsci.glimpse.util.geo.format.Util.toDegreesMinutes;
import static com.metsci.glimpse.util.geo.format.Util.toDegreesMinutesSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GeoFormatUtilTest
{

    @Test
    void dmsFormattingShouldBasicallyWork( )
    {
        double lat_DEG = 12 + 34/60d + 56.789/3600d;
        assertEquals( "12°34'56.789\"N", toDegreesMinutesSeconds( lat_DEG, 3, false ) );
        assertEquals( "12°34'56.79\"N", toDegreesMinutesSeconds( lat_DEG, 2, false ) );
        assertEquals( "12°34'56.8\"N", toDegreesMinutesSeconds( lat_DEG, 1, false ) );
        assertEquals( "12°34'57\"N", toDegreesMinutesSeconds( lat_DEG, 0, false ) );
    }

    @Test
    void dmsFormattingShouldPropagateRoundUps( )
    {
        double lat_DEG = 1 + 59/60d + 59.999/3600d;
        assertEquals( "01°59'59.999\"N", toDegreesMinutesSeconds( lat_DEG, 3, false ) );
        assertEquals( "02°00'00.00\"N", toDegreesMinutesSeconds( lat_DEG, 2, false ) );
        assertEquals( "02°00'00.0\"N", toDegreesMinutesSeconds( lat_DEG, 1, false ) );
        assertEquals( "02°00'00\"N", toDegreesMinutesSeconds( lat_DEG, 0, false ) );
    }

    @Test
    void dmFormattingShouldBasicallyWork( )
    {
        double lat_DEG = 12 + 34.56789/60d;
        assertEquals( "12°34.56789'N", toDegreesMinutes( lat_DEG, 5, false ) );
        assertEquals( "12°34.5679'N", toDegreesMinutes( lat_DEG, 4, false ) );
        assertEquals( "12°34.568'N", toDegreesMinutes( lat_DEG, 3, false ) );
        assertEquals( "12°34.57'N", toDegreesMinutes( lat_DEG, 2, false ) );
        assertEquals( "12°34.6'N", toDegreesMinutes( lat_DEG, 1, false ) );
        assertEquals( "12°35'N", toDegreesMinutes( lat_DEG, 0, false ) );
    }

    @Test
    void dmFormattingShouldPropagateRoundUps( )
    {
        double lat_DEG = 1 + 59.999/60d;
        assertEquals( "01°59.999'N", toDegreesMinutes( lat_DEG, 3, false ) );
        assertEquals( "02°00.00'N", toDegreesMinutes( lat_DEG, 2, false ) );
        assertEquals( "02°00.0'N", toDegreesMinutes( lat_DEG, 1, false ) );
        assertEquals( "02°00'N", toDegreesMinutes( lat_DEG, 0, false ) );
    }

}

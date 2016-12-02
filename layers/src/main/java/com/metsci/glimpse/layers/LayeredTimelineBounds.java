package com.metsci.glimpse.layers;

import com.metsci.glimpse.util.units.time.TimeStamp;

public class LayeredTimelineBounds
{

    public final long min_PMILLIS;
    public final long max_MILLIS;

    public LayeredTimelineBounds( TimeStamp min, TimeStamp max )
    {
        this( min.toPosixMillis( ), max.toPosixMillis( ) );
    }

    public LayeredTimelineBounds( long min_PMILLIS, long max_MILLIS )
    {
        this.min_PMILLIS = min_PMILLIS;
        this.max_MILLIS = max_MILLIS;
    }

}

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
package com.metsci.glimpse.util.units.time;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import com.metsci.glimpse.util.GeneralUtils;
import com.metsci.glimpse.util.units.time.format.TimeStampFormat;
import com.metsci.glimpse.util.units.time.format.TimeStampParseException;

/**
 * A TimeStamp implementation that stores the number of milliseconds since the
 * epoch (1970-01-01 00:00:00 UTC) as a long.
 * <p>
 * Advantages of this implementation:
 * <ul>
 * <li>In many cases, it is desirable to store time as POSIX milliseconds because
 * existing systems or libraries do (java.lang.Date and MySQL, for example).
 * By storing POSIX milliseconds, we avoid conversion costs.
 * <li>Storing an integral representation of a time allows reversible arithmetic
 * and sensible comparison.
 * </ul>
 * <p>
 * Disadvantanges of this implementation:
 * <ul>
 * <li>Storing microseconds would give better precision without requiring more
 * storage or introducing more complexity. The max range of POSIX microseconds
 * (300,000 years) is sufficient for any imaginable application.
 * <li>This class's methods are subject to arithmetic overflow. However, this does
 * happen until the distance from the epoch reaches about 300 million years, so we
 * make no effort to detect overflow.
 * </ul>
 *
 * @author hogye
 */
public class TimeStampPosixMillisInt64 extends TimeStamp implements Serializable
{
    private static final long serialVersionUID = 2651020962415316423L;

    protected static final double millisToSeconds = 1e-3;
    protected static final int millisDecimalScale = 3;
    protected static final long millisToMicros = 1000L;
    protected static final long millisToNanos = 1000000L;

    protected static final double microsToMillis = 1e-3;
    protected static final double nanosToMillis = 1e-6;

    // IODH idiom avoids cycles in class initialization
    private static class Instances
    {
        public static final TimeStampPosixMillisInt64 posixEpoch = new TimeStampPosixMillisInt64( 0 );
    }

    public static final TimeStampFactory<TimeStampPosixMillisInt64> factory = new TimeStampFactory<TimeStampPosixMillisInt64>( )
    {
        public TimeStampPosixMillisInt64 fromPosixSeconds( double posixSeconds )
        {
            long posixMillis = Math.round( Time.secondsToMilliseconds( posixSeconds ) );
            return new TimeStampPosixMillisInt64( posixMillis );
        }

        public TimeStampPosixMillisInt64 fromPosixSeconds( BigDecimal posixSeconds )
        {
            long posixMillis = posixSeconds.scaleByPowerOfTen( millisDecimalScale ).longValue( );
            return new TimeStampPosixMillisInt64( posixMillis );
        }

        public TimeStampPosixMillisInt64 fromPosixMillis( long posixMillis )
        {
            return new TimeStampPosixMillisInt64( posixMillis );
        }

        public TimeStampPosixMillisInt64 fromPosixMicros( long posixMicros )
        {
            long posixMillis = Math.round( posixMicros * microsToMillis );
            return new TimeStampPosixMillisInt64( posixMillis );
        }

        public TimeStampPosixMillisInt64 fromPosixNanos( long posixNanos )
        {
            long posixMillis = Math.round( posixNanos * nanosToMillis );
            return new TimeStampPosixMillisInt64( posixMillis );
        }

        public TimeStampPosixMillisInt64 fromTimeStamp( TimeStamp timeStamp )
        {
            long posixMillis = timeStamp.toPosixMillis( );
            return new TimeStampPosixMillisInt64( posixMillis );
        }

        public TimeStampPosixMillisInt64 fromDate( Date date )
        {
            long posixMillis = date.getTime( );
            return new TimeStampPosixMillisInt64( posixMillis );
        }

        public TimeStampPosixMillisInt64 fromCalendar( Calendar calendar )
        {
            long posixMillis = calendar.getTimeInMillis( );
            return new TimeStampPosixMillisInt64( posixMillis );
        }

        public TimeStampPosixMillisInt64 fromString( String string, TimeStampFormat format ) throws TimeStampParseException
        {
            long posixMillis = secondsToMillis( format.parse( string ) );
            return new TimeStampPosixMillisInt64( posixMillis );
        }

        public TimeStampPosixMillisInt64 currentTime( )
        {
            return new TimeStampPosixMillisInt64( System.currentTimeMillis( ) );
        }

        public TimeStampPosixMillisInt64 posixEpoch( )
        {
            return Instances.posixEpoch;
        }
    };

    protected final long posixMillis;

    protected TimeStampPosixMillisInt64( long posixMillis )
    {
        this.posixMillis = posixMillis;
    }

    @Override
    public TimeStampPosixMillisInt64 add( double duration )
    {
        return new TimeStampPosixMillisInt64( posixMillis + Math.round( Time.toMilliseconds( duration ) ) );
    }

    @Override
    public TimeStampPosixMillisInt64 subtract( double duration )
    {
        return new TimeStampPosixMillisInt64( posixMillis - Math.round( Time.toMilliseconds( duration ) ) );
    }

    @Override
    public double durationBefore( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixMillisInt64 ) return Time.fromMilliseconds( ( ( TimeStampPosixMillisInt64 ) o ).posixMillis - posixMillis );

        return super.durationBefore( o );
    }

    @Override
    public double durationAfter( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixMillisInt64 ) return Time.fromMilliseconds( posixMillis - ( ( TimeStampPosixMillisInt64 ) o ).posixMillis );

        return super.durationAfter( o );
    }

    @Override
    public double toPosixSeconds( )
    {
        return posixMillis * millisToSeconds;
    }

    @Override
    public long toPosixMillis( )
    {
        return posixMillis;
    }

    @Override
    public long toPosixMicros( )
    {
        return posixMillis * millisToMicros;
    }

    @Override
    public long toPosixNanos( )
    {
        return posixMillis * millisToNanos;
    }

    @Override
    public Date toDate( )
    {
        return new Date( posixMillis );
    }

    @Override
    public Calendar toCalendar( )
    {
        Calendar calendar = Calendar.getInstance( );
        calendar.setTimeInMillis( posixMillis );
        return calendar;
    }

    @Override
    public BigDecimal toPosixSecondsExact( )
    {
        return BigDecimal.valueOf( posixMillis, millisDecimalScale );
    }

    @Override
    public boolean isBefore( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixMillisInt64 ) return posixMillis < ( ( TimeStampPosixMillisInt64 ) o ).posixMillis;

        return super.isBefore( o );
    }

    @Override
    public boolean isAfter( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixMillisInt64 ) return posixMillis > ( ( TimeStampPosixMillisInt64 ) o ).posixMillis;

        return super.isAfter( o );
    }

    @Override
    public int compareTo( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixMillisInt64 ) return compareLongs( posixMillis, ( ( TimeStampPosixMillisInt64 ) o ).posixMillis );

        return super.compareTo( o );
    }

    protected static final int compareLongs( long a, long b )
    {
        if ( a < b ) return -1;
        if ( a > b ) return +1;
        return 0;
    }

    protected static final long secondsToMillis( BigDecimal seconds )
    {
        BigDecimal millis = seconds.scaleByPowerOfTen( millisDecimalScale );
        return millis.setScale( 0, BigDecimal.ROUND_HALF_UP ).longValue( );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;

        if ( o instanceof TimeStampPosixMillisInt64 ) return ( posixMillis == ( ( TimeStampPosixMillisInt64 ) o ).posixMillis );

        return super.equals( o );
    }

    @Override
    public int hashCode( )
    {
        return 31 + GeneralUtils.hashCode( posixMillis );
    }

}

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
 * A TimeStamp implementation that stores the number of microseconds since the
 * epoch (1970-01-01 00:00:00 UTC) as a long.
 * <p>
 * Advantages of this implementation:
 * <ul>
 * <li>More precise than milliseconds, while still having a a large enough max
 * range (300,000 years) for any imaginable application.
 * <li>Storing an integral representation of a time allows reversible arithmetic
 * and sensible comparison.
 * </ul>
 * <p>
 * Disadvantanges of this implementation:
 * <ul>
 * <li>Many existing systems and libraries (java.lang.Date, MySQL) store time in
 * POSIX milliseconds. Interoperation between such libraries and this class
 * introduces small conversion costs, and more significantly, the possibility of
 * rounding error.
 * <li>This class's methods are subject to arithmetic overflow. However, this does
 * happen until the distance from the epoch reaches about 300,000 years, so we make
 * no effort to detect overflow.
 * </ul>
 *
 * @author hogye
 */
public class TimeStampPosixMicrosInt64 extends TimeStamp implements Serializable
{

    private static final long serialVersionUID = 5473538773507187917L;

    protected static final double microsToSeconds = 1e-6;
    protected static final int microsDecimalScale = 6;
    protected static final long millisToMicros = 1000L;
    protected static final long microsToNanos = 1000L;

    protected static final double microsToMillis = 1e-3;
    protected static final double nanosToMicros = 1e-6;

    // IODH idiom avoids cycles in class initialization
    private static class Instances
    {
        public static final TimeStampPosixMicrosInt64 posixEpoch = new TimeStampPosixMicrosInt64( 0 );
    }

    public static final TimeStampFactory<TimeStampPosixMicrosInt64> factory = new TimeStampFactory<TimeStampPosixMicrosInt64>( )
    {
        public TimeStampPosixMicrosInt64 fromPosixSeconds( double posixSeconds )
        {
            long posixMicros = Math.round( Time.secondsToMicroseconds( posixSeconds ) );
            return new TimeStampPosixMicrosInt64( posixMicros );
        }

        public TimeStampPosixMicrosInt64 fromPosixSeconds( BigDecimal posixSeconds )
        {
            long posixMicros = posixSeconds.scaleByPowerOfTen( microsDecimalScale ).longValue( );
            return new TimeStampPosixMicrosInt64( posixMicros );
        }

        public TimeStampPosixMicrosInt64 fromPosixMillis( long posixMillis )
        {
            return new TimeStampPosixMicrosInt64( posixMillis * millisToMicros );
        }

        public TimeStampPosixMicrosInt64 fromPosixMicros( long posixMicros )
        {
            return new TimeStampPosixMicrosInt64( posixMicros );
        }

        public TimeStampPosixMicrosInt64 fromPosixNanos( long posixNanos )
        {
            long posixMicros = Math.round( posixNanos * nanosToMicros );
            return new TimeStampPosixMicrosInt64( posixMicros );
        }

        public TimeStampPosixMicrosInt64 fromTimeStamp( TimeStamp timeStamp )
        {
            long posixMicros = timeStamp.toPosixMicros( );
            return new TimeStampPosixMicrosInt64( posixMicros );
        }

        public TimeStampPosixMicrosInt64 fromDate( Date date )
        {
            long posixMicros = date.getTime( ) * millisToMicros;
            return new TimeStampPosixMicrosInt64( posixMicros );
        }

        public TimeStampPosixMicrosInt64 fromCalendar( Calendar calendar )
        {
            long posixMicros = calendar.getTimeInMillis( ) * millisToMicros;
            return new TimeStampPosixMicrosInt64( posixMicros );
        }

        public TimeStampPosixMicrosInt64 fromString( String string, TimeStampFormat format ) throws TimeStampParseException
        {
            long posixMicros = secondsToMicros( format.parse( string ) );
            return new TimeStampPosixMicrosInt64( posixMicros );
        }

        public TimeStampPosixMicrosInt64 currentTime( )
        {
            return new TimeStampPosixMicrosInt64( System.currentTimeMillis( ) * millisToMicros );
        }

        public TimeStampPosixMicrosInt64 posixEpoch( )
        {
            return Instances.posixEpoch;
        }
    };

    protected final long posixMicros;

    protected TimeStampPosixMicrosInt64( long posixMicros )
    {
        this.posixMicros = posixMicros;
    }

    @Override
    public TimeStampPosixMicrosInt64 add( double duration )
    {
        return new TimeStampPosixMicrosInt64( posixMicros + Math.round( Time.toMicroseconds( duration ) ) );
    }

    @Override
    public TimeStampPosixMicrosInt64 subtract( double duration )
    {
        return new TimeStampPosixMicrosInt64( posixMicros - Math.round( Time.toMicroseconds( duration ) ) );
    }

    @Override
    public double durationBefore( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixMicrosInt64 ) return Time.fromMicroseconds( ( ( TimeStampPosixMicrosInt64 ) o ).posixMicros - posixMicros );

        return super.durationBefore( o );
    }

    @Override
    public double durationAfter( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixMicrosInt64 ) return Time.fromMicroseconds( posixMicros - ( ( TimeStampPosixMicrosInt64 ) o ).posixMicros );

        return super.durationAfter( o );
    }

    @Override
    public double toPosixSeconds( )
    {
        return posixMicros * microsToSeconds;
    }

    @Override
    public long toPosixMillis( )
    {
        return Math.round( posixMicros * microsToMillis );
    }

    @Override
    public long toPosixMicros( )
    {
        return posixMicros;
    }

    @Override
    public long toPosixNanos( )
    {
        return posixMicros * microsToNanos;
    }

    @Override
    public Date toDate( )
    {
        long posixMillis = Math.round( posixMicros * microsToMillis );
        return new Date( posixMillis );
    }

    @Override
    public Calendar toCalendar( )
    {
        long posixMillis = Math.round( posixMicros * microsToMillis );
        Calendar calendar = Calendar.getInstance( );
        calendar.setTimeInMillis( posixMillis );

        return calendar;
    }

    @Override
    public BigDecimal toPosixSecondsExact( )
    {
        return BigDecimal.valueOf( posixMicros, microsDecimalScale );
    }

    @Override
    public boolean isBefore( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixMicrosInt64 ) return posixMicros < ( ( TimeStampPosixMicrosInt64 ) o ).posixMicros;

        return super.isBefore( o );
    }

    @Override
    public boolean isAfter( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixMicrosInt64 ) return posixMicros > ( ( TimeStampPosixMicrosInt64 ) o ).posixMicros;

        return super.isAfter( o );
    }

    @Override
    public int compareTo( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixMicrosInt64 ) return compareLongs( posixMicros, ( ( TimeStampPosixMicrosInt64 ) o ).posixMicros );

        return super.compareTo( o );
    }

    protected static final int compareLongs( long a, long b )
    {
        if ( a < b ) return -1;
        if ( a > b ) return +1;
        return 0;
    }

    protected static final long secondsToMicros( BigDecimal seconds )
    {
        BigDecimal micros = seconds.scaleByPowerOfTen( microsDecimalScale );
        return micros.setScale( 0, BigDecimal.ROUND_HALF_UP ).longValue( );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;

        if ( o instanceof TimeStampPosixMicrosInt64 ) return ( posixMicros == ( ( TimeStampPosixMicrosInt64 ) o ).posixMicros );

        return super.equals( o );
    }

    @Override
    public int hashCode( )
    {
        return 1000031 + GeneralUtils.hashCode( posixMicros );
    }

}

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
 * A TimeStamp implementation that stores the number of seconds since the
 * epoch (1970-01-01 00:00:00 UTC) as a double.
 *
 *
 * Advantages of this implementation:
 *
 * Methods are not subject to overflow. When arithmetic results in values
 * outside the bounds of double, the special values Double.POSITIVE_INFINITY
 * and Double.NEGATIVE_INFINITY kick in.
 *
 * Requires little work to convert to and from system-units.
 *
 *
 * Disadvantanges of this implementation:
 *
 * Arithmetic is not exactly reversible, because of precision loss.
 *
 * Precision changes as the value changes.
 *
 * Many existing systems and libraries store time as POSIX milliseconds. To
 * communicate with those systems, this class must perform conversions.
 *
 * @author hogye
 */
public class TimeStampPosixSecondsFloat64 extends TimeStamp implements Serializable
{
    private static final long serialVersionUID = 4906763034483788418L;

    protected static final double secondsToMillis = 1e3;
    protected static final double secondsToMicros = 1e6;
    protected static final double secondsToNanos = 1e9;

    protected static final double millisToSeconds = 1e-3;
    protected static final double microsToSeconds = 1e-6;
    protected static final double nanosToSeconds = 1e-9;

    // IODH idiom avoids cycles in class initialization
    private static class Instances
    {
        public static final TimeStampPosixSecondsFloat64 posixEpoch = new TimeStampPosixSecondsFloat64( 0 );
    }

    public static final TimeStampFactory<TimeStampPosixSecondsFloat64> factory = new TimeStampFactory<TimeStampPosixSecondsFloat64>( )
    {
        public TimeStampPosixSecondsFloat64 fromPosixSeconds( double posixSeconds )
        {
            return new TimeStampPosixSecondsFloat64( posixSeconds );
        }

        public TimeStampPosixSecondsFloat64 fromPosixSeconds( BigDecimal posixSeconds )
        {
            return new TimeStampPosixSecondsFloat64( posixSeconds.doubleValue( ) );
        }

        public TimeStampPosixSecondsFloat64 fromPosixMillis( long posixMillis )
        {
            double posixSeconds = posixMillis * millisToSeconds;
            return new TimeStampPosixSecondsFloat64( posixSeconds );
        }

        public TimeStampPosixSecondsFloat64 fromPosixMicros( long posixMicros )
        {
            double posixSeconds = posixMicros * microsToSeconds;
            return new TimeStampPosixSecondsFloat64( posixSeconds );
        }

        public TimeStampPosixSecondsFloat64 fromPosixNanos( long posixNanos )
        {
            double posixSeconds = posixNanos * nanosToSeconds;
            return new TimeStampPosixSecondsFloat64( posixSeconds );
        }

        public TimeStampPosixSecondsFloat64 fromTimeStamp( TimeStamp timeStamp )
        {
            double posixSeconds = timeStamp.toPosixSeconds( );
            return new TimeStampPosixSecondsFloat64( posixSeconds );
        }

        public TimeStampPosixSecondsFloat64 fromDate( Date date )
        {
            double posixSeconds = date.getTime( ) * millisToSeconds;
            return new TimeStampPosixSecondsFloat64( posixSeconds );
        }

        public TimeStampPosixSecondsFloat64 fromCalendar( Calendar calendar )
        {
            double posixSeconds = calendar.getTimeInMillis( ) * millisToSeconds;
            return new TimeStampPosixSecondsFloat64( posixSeconds );
        }

        public TimeStampPosixSecondsFloat64 fromString( String string, TimeStampFormat format ) throws TimeStampParseException
        {
            double posixSeconds = format.parse( string ).doubleValue( );
            return new TimeStampPosixSecondsFloat64( posixSeconds );
        }

        public TimeStampPosixSecondsFloat64 currentTime( )
        {
            double posixSeconds = System.currentTimeMillis( ) * millisToSeconds;
            return new TimeStampPosixSecondsFloat64( posixSeconds );
        }

        public TimeStampPosixSecondsFloat64 posixEpoch( )
        {
            return Instances.posixEpoch;
        }
    };

    protected final double posixSeconds;

    protected TimeStampPosixSecondsFloat64( double posixSeconds )
    {
        this.posixSeconds = posixSeconds;
    }

    @Override
    public TimeStampPosixSecondsFloat64 add( double duration )
    {
        return new TimeStampPosixSecondsFloat64( posixSeconds + Time.toSeconds( duration ) );
    }

    @Override
    public TimeStampPosixSecondsFloat64 subtract( double duration )
    {
        return new TimeStampPosixSecondsFloat64( posixSeconds - Time.toSeconds( duration ) );
    }

    @Override
    public double durationBefore( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixSecondsFloat64 ) return Time.fromSeconds( ( ( TimeStampPosixSecondsFloat64 ) o ).posixSeconds - posixSeconds );

        return super.durationBefore( o );
    }

    @Override
    public double durationAfter( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixSecondsFloat64 ) return Time.fromSeconds( posixSeconds - ( ( TimeStampPosixSecondsFloat64 ) o ).posixSeconds );

        return super.durationAfter( o );
    }

    @Override
    public double toPosixSeconds( )
    {
        return posixSeconds;
    }

    @Override
    public long toPosixMillis( )
    {
        return Math.round( posixSeconds * secondsToMillis );
    }

    @Override
    public long toPosixMicros( )
    {
        return Math.round( posixSeconds * secondsToMicros );
    }

    @Override
    public long toPosixNanos( )
    {
        return Math.round( posixSeconds * secondsToNanos );
    }

    @Override
    public Date toDate( )
    {
        return new Date( toPosixMillis( ) );
    }

    @Override
    public Calendar toCalendar( )
    {
        Calendar calendar = Calendar.getInstance( );
        calendar.setTimeInMillis( toPosixMillis( ) );
        return calendar;
    }

    @Override
    public BigDecimal toPosixSecondsExact( )
    {
        return new BigDecimal( posixSeconds );
    }

    @Override
    public boolean isBefore( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixSecondsFloat64 ) return posixSeconds < ( ( TimeStampPosixSecondsFloat64 ) o ).posixSeconds;

        return super.isBefore( o );
    }

    @Override
    public boolean isAfter( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixSecondsFloat64 ) return posixSeconds > ( ( TimeStampPosixSecondsFloat64 ) o ).posixSeconds;

        return super.isAfter( o );
    }

    @Override
    public int compareTo( TimeStamp o )
    {
        if ( o instanceof TimeStampPosixSecondsFloat64 ) return Double.compare( posixSeconds, ( ( TimeStampPosixSecondsFloat64 ) o ).posixSeconds );

        return super.compareTo( o );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;

        if ( o instanceof TimeStampPosixSecondsFloat64 ) return ( posixSeconds == ( ( TimeStampPosixSecondsFloat64 ) o ).posixSeconds );

        return super.equals( o );
    }

    @Override
    public int hashCode( )
    {
        return 211 + 337 * GeneralUtils.hashCode( posixSeconds );
    }

}

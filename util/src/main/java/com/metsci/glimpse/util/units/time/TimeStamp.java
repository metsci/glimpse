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
 * @author hogye
 */
public abstract class TimeStamp implements Comparable<TimeStamp>, Serializable
{
    private static final long serialVersionUID = -2541071763636490805L;

    /**
     * The format used by the convenience methods {@link #fromString(String)} and
     * {@link #toString()}. It refers to {@link TimeStampFormat#iso8601}.
     */
    public static final TimeStampFormat defaultFormat = TimeStampFormat.iso8601;

    // IODH idiom avoids cycles in class initialization
    private static class Instances
    {
        public static final TimeStampFactory<?> factory = TimeStampPosixMillisInt64.factory;
    }

    /**
     * Converts a timestamp in posix seconds to a TimeStamp.
     *
     * @param posixSeconds seconds since the epoch
     */
    public static TimeStamp fromPosixSeconds( double posixSeconds )
    {
        return Instances.factory.fromPosixSeconds( posixSeconds );
    }

    /**
     * Converts a timestamp in posix seconds to a TimeStamp.
     *
     * @param posixSeconds seconds since the epoch
     */
    public static TimeStamp fromPosixSeconds( BigDecimal posixSeconds )
    {
        return Instances.factory.fromPosixSeconds( posixSeconds );
    }

    /**
     * Converts a timestamp in posix milliseconds to a TimeStamp.
     *
     * @param posixMillis milliseconds since the epoch
     */
    public static TimeStamp fromPosixMillis( long posixMillis )
    {
        return Instances.factory.fromPosixMillis( posixMillis );
    }

    /**
     * Converts a timestamp in posix microseconds to a TimeStamp.
     *
     * @param posixMicros microseconds since the epoch
     */
    public static TimeStamp fromPosixMicros( long posixMicros )
    {
        return Instances.factory.fromPosixMicros( posixMicros );
    }

    /**
     * Converts a timestamp in posix nanoseconds to a TimeStamp.
     *
     * @param posixNanos nanoseconds since the epoch
     */
    public static TimeStamp fromPosixNanos( long posixNanos )
    {
        return Instances.factory.fromPosixNanos( posixNanos );
    }

    /**
     * Converts a TimeStamp of any subclass to a TimeStamp of the default subclass.
     *
     * @throws NullPointerException if argument is null
     */
    public static TimeStamp fromTimeStamp( TimeStamp timeStamp )
    {
        return Instances.factory.fromTimeStamp( timeStamp );
    }

    /**
     * Converts a {@link Date} to a TimeStamp.
     *
     * @throws NullPointerException if argument is null
     */
    public static TimeStamp fromDate( Date date )
    {
        return Instances.factory.fromDate( date );
    }

    /**
     * Converts a {@link Calendar} to a TimeStamp.
     *
     * @throws NullPointerException if argument is null
     */
    public static TimeStamp fromCalendar( Calendar calendar )
    {
        return Instances.factory.fromCalendar( calendar );
    }

    /**
     * Converts a {@link String} to a TimeStamp.
     *
     * @throws NullPointerException if either argument is null
     * @throws TimeStampParseException if string parsing fails
     */
    public static TimeStamp fromString( String string, TimeStampFormat format ) throws TimeStampParseException
    {
        return Instances.factory.fromString( string, format );
    }

    /**
     * Converts a {@link String} to a TimeStamp, using {@link #defaultFormat}.
     *
     * @throws NullPointerException if either argument is null
     * @throws TimeStampParseException if string parsing fails
     */
    public static TimeStamp fromString( String string ) throws TimeStampParseException
    {
        return Instances.factory.fromString( string, defaultFormat );
    }

    /**
     * Returns a TimeStamp that represents the posix epoch.
     */
    public static TimeStamp posixEpoch( )
    {
        return Instances.factory.posixEpoch( );
    }

    /**
     * Creates a TimeStamp that represents the current system time. The precision
     * may be limited by the precision of Java's access to the system clock.
     */
    public static TimeStamp currentTime( )
    {
        return Instances.factory.currentTime( );
    }

    protected TimeStamp( )
    {
    }

    /**
     * Creates a new TimeStamp instance offset from this timestamp by the given
     * amount.
     *
     * The returned instance may or may not be of the same dynamic type as this
     * instance.
     *
     * @param duration the amount of time to add in system-units
     */
    public abstract TimeStamp add( double duration );

    /**
     * Creates a new TimeStamp instance offset from this timestamp by the given
     * amount.
     *
     * The returned instance may or may not be of the same dynamic type as this
     * instance.
     *
     * @param duration the amount of time to subtract in system-units
     */
    public abstract TimeStamp subtract( double duration );

    /**
     * Determines how long before the given timestamp this timestamp is. That is:
     *
     *     durationBefore = other - this
     *
     * In the base-class implementation, arithmetic is done with BigDecimal. This
     * is quite effective at minimizing rounding error, but may be slow compared
     * to primitive arithmetic.
     *
     * @throws NullPointerException if argument is null
     * @return the difference, in system-units
     */
    public double durationBefore( TimeStamp o )
    {
        BigDecimal secondsBefore = o.toPosixSecondsExact( ).subtract( toPosixSecondsExact( ) );
        return Time.fromSeconds( secondsBefore.doubleValue( ) );
    }

    /**
     * Determines how long after the given timestamp this timestamp is. That is:
     *
     *     durationAfter = this - other
     *
     * In the base-class implementation, arithmetic is done with BigDecimal. This
     * is quite effective at minimizing rounding error, but may be slow compared
     * to primitive arithmetic.
     *
     * @throws NullPointerException if argument is null
     * @return the difference, in system-units
     */
    public double durationAfter( TimeStamp o )
    {
        BigDecimal secondsAfter = toPosixSecondsExact( ).subtract( o.toPosixSecondsExact( ) );
        return Time.fromSeconds( secondsAfter.doubleValue( ) );
    }

    /**
     * Converts this TimeStamp to posix seconds.
     *
     * @return seconds since the epoch
     */
    public abstract double toPosixSeconds( );

    /**
     * Converts this TimeStamp to posix milliseconds.
     *
     * @return milliseconds since the epoch
     */
    public abstract long toPosixMillis( );

    /**
     * Converts this TimeStamp to posix microseconds.
     *
     * @return microseconds since the epoch
     */
    public abstract long toPosixMicros( );

    /**
     * Converts this TimeStamp to posix nanoseconds.
     *
     * @return nanoseconds since the epoch
     */
    public abstract long toPosixNanos( );

    /**
     * Converts this TimeStamp to a {@link Date}.
     */
    public abstract Date toDate( );

    /**
     * Converts this TimeStamp to a {@link Calendar}.
     */
    public abstract Calendar toCalendar( );

    /**
     * Converts this TimeStamp to posix seconds. The returned representation is
     * exact (see note).
     *
     *
     * NOTE: In theory, it is possible to have a subclass that stores its value
     * in a form that cannot be exactly converted to a BigDecimal (e.g., thirds
     * of a second). Such subclasses are discouraged.
     *
     * If such a subclass is unavoidable, its documentation must note prominently
     * that it breaks the contract of the TimeStamp interface, and its implementation
     * of this method must return values as exact as is practical.
     *
     *
     * @return seconds since the epoch
     */
    public abstract BigDecimal toPosixSecondsExact( );

    /**
     * Returns true iff this timestamp is before the given timestamp.
     *
     * @throws NullPointerException if argument is null
     */
    public boolean isBefore( TimeStamp o )
    {
        return compareTo( o ) < 0;
    }

    /**
     * Returns true iff this timestamp is before or equal to the given timestamp.
     *
     * @throws  NullPointerException  if argument is null
     */
    public boolean isBeforeOrEquals( TimeStamp o )
    {
        return compareTo( o ) <= 0;
    }

    /**
     * Returns true iff this timestamp is after the given timestamp.
     *
     * @throws NullPointerException if argument is null
     */
    public boolean isAfter( TimeStamp o )
    {
        return compareTo( o ) > 0;
    }

    /**
     * Returns true iff this timestamp is after or equal to the given timestamp.
     *
     * @throws  NullPointerException  if argument is null
     */
    public boolean isAfterOrEquals( TimeStamp o )
    {
        return compareTo( o ) >= 0;
    }

    @Override
    public int compareTo( TimeStamp o )
    {
        if ( o == this ) return 0;

        return toPosixSecondsExact( ).compareTo( o.toPosixSecondsExact( ) );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o instanceof TimeStamp ) return compareTo( ( TimeStamp ) o ) == 0;

        return false;
    }

    @Override
    public int hashCode( )
    {
        return GeneralUtils.stripTrailingZeros( toPosixSecondsExact( ) ).hashCode( );
    }

    /**
     * Converts this timestamp to a {@link String}, according to the given format.
     */
    public String toString( TimeStampFormat format )
    {
        return format.format( toPosixSecondsExact( ) );
    }

    /**
     * Converts this timestamp to a {@link String}, according to {@link #defaultFormat}.
     */
    @Override
    public String toString( )
    {
        return toString( defaultFormat );
    }

}

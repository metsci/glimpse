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
package com.metsci.glimpse.axis.painter.label.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import com.google.common.collect.Lists;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.units.time.Time;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.metsci.glimpse.util.units.time.format.TimeStampFormat;
import com.metsci.glimpse.util.units.time.format.TimeStampFormatStandard;

public class AbsoluteTimeAxisLabelHandler implements TimeAxisLabelHandler
{
    protected static final TimeZone defaultTimeZone = TimeZone.getTimeZone( "UTC" );

    protected static final String defaultMinuteSecondFormat = "%m:%S";
    protected static final String defaultHourDayMonthFormat = "%d %3N %H:00 ";

    protected static final String defaultHourMinuteFormat = "%H:%m";
    protected static final String defaultDayMonthYearFormat = "%d %3N %y";

    protected static final String defaultDayFormat = "%d";
    protected static final String defaultMonthYearFormat = "%3N %y";

    protected static final String defaultMonthFormat = "%3N";
    protected static final String defaultYearFormat = "%y";

    protected static final TimeStructFactory hourStructFactory = new HourStructFactory( );
    protected static final TimeStructFactory dayStructFactory = new DayStructFactory( );
    protected static final TimeStructFactory monthStructFactory = new MonthStructFactory( );
    protected static final TimeStructFactory yearStructFactory = new YearStructFactory( );

    protected TimeStampFormat minuteSecondFormat;
    protected TimeStampFormat hourDayMonthFormat;
    protected TimeStampFormat hourMinuteFormat;
    protected TimeStampFormat dayMonthYearFormat;
    protected TimeStampFormat dayFormat;
    protected TimeStampFormat monthFormat;
    protected TimeStampFormat monthYearFormat;
    protected TimeStampFormat yearFormat;

    protected final String minuteSecondString;
    protected final String hourDayMonthString;
    protected final String hourMinuteString;
    protected final String dayMonthYearString;
    protected final String dayString;
    protected final String monthString;
    protected final String monthYearString;
    protected final String yearString;

    protected Epoch epoch;
    protected TimeZone timeZone;

    protected int pixelsBetweenTicks = 60;
    protected double yearOrderFactor = 6.0;

    protected AxisUnitConverter converter;

    public AbsoluteTimeAxisLabelHandler( Epoch epoch )
    {
        this( defaultTimeZone, epoch );
    }

    public AbsoluteTimeAxisLabelHandler( TimeZone timeZone, Epoch epoch )
    {
        this( defaultMinuteSecondFormat, defaultHourDayMonthFormat, defaultHourMinuteFormat, defaultDayMonthYearFormat, defaultDayFormat, defaultMonthFormat, defaultMonthYearFormat, defaultYearFormat, defaultTimeZone, epoch );
    }

    //@formatter:off
    public AbsoluteTimeAxisLabelHandler(
            String minuteSecondString,
            String hourDayMonthString,
            String hourMinuteString,
            String dayMonthYearString,
            String dayString,
            String monthString,
            String monthYearString,
            String yearString,
            TimeZone timeZone, Epoch epoch )
    {
        this.timeZone = timeZone;

        this.minuteSecondString = minuteSecondString;
        this.hourDayMonthString = hourDayMonthString;

        this.hourMinuteString = hourMinuteString;
        this.dayMonthYearString = dayMonthYearString;

        this.dayString = dayString;
        this.monthString = monthString;
        
        this.monthYearString = monthYearString;
        this.yearString = yearString;
        
        this.updateFormatters( );

        this.epoch = epoch;
        
        this.converter = new AxisUnitConverter( )
        {
            @Override
            public double fromAxisUnits( double value )
            {
                return value;
            }

            @Override
            public double toAxisUnits( double value )
            {
                return value;
            }
        };
    }
    //@formatter:on

    protected void updateFormatters( )
    {
        this.minuteSecondFormat = new TimeStampFormatStandard( minuteSecondString, timeZone );
        this.hourDayMonthFormat = new TimeStampFormatStandard( hourDayMonthString, timeZone );
        this.hourMinuteFormat = new TimeStampFormatStandard( hourMinuteString, timeZone );
        this.dayMonthYearFormat = new TimeStampFormatStandard( dayMonthYearString, timeZone );
        this.dayFormat = new TimeStampFormatStandard( dayString, timeZone );
        this.monthFormat = new TimeStampFormatStandard( monthString, timeZone );
        this.monthYearFormat = new TimeStampFormatStandard( monthYearString, timeZone );
        this.yearFormat = new TimeStampFormatStandard( yearString, timeZone );
    }

    public TimeZone getTimeZone( )
    {
        return timeZone;
    }

    public void setTimeZone( TimeZone timeZone )
    {
        this.timeZone = timeZone;
        this.updateFormatters( );
    }

    public TimeStructFactory getHourStructFactory( )
    {
        return hourStructFactory;
    }

    public TimeStructFactory getDayStructFactory( )
    {
        return dayStructFactory;
    }

    public TimeStructFactory getMonthStructFactory( )
    {
        return monthStructFactory;
    }

    public TimeStructFactory getYearStructFactory( )
    {
        return yearStructFactory;
    }

    public TimeStampFormat getYearFormat( )
    {
        return yearFormat;
    }

    public TimeStampFormat getMonthYearFormat( )
    {
        return monthYearFormat;
    }

    public TimeStampFormat getMonthFormat( )
    {
        return monthFormat;
    }

    public TimeStampFormat getDayFormat( )
    {
        return dayFormat;
    }

    public TimeStampFormat getDayMonthYearFormat( )
    {
        return dayMonthYearFormat;
    }

    public TimeStampFormat getHourMinuteFormat( )
    {
        return hourMinuteFormat;
    }

    public TimeStampFormat getHourDayMonthFormat( )
    {
        return hourDayMonthFormat;
    }

    public TimeStampFormat getSecondMinuteFormat( )
    {
        return minuteSecondFormat;
    }

    public void setPixelsBetweenTicks( int pixels )
    {
        this.pixelsBetweenTicks = pixels;
    }

    public void setEpoch( Epoch epoch )
    {
        this.epoch = epoch;
    }

    public Epoch getEpoch( )
    {
        return epoch;
    }

    public TimeStamp toTimeStamp( double time )
    {
        return epoch.toTimeStamp( time );
    }

    public double fromTimeStamp( TimeStamp time )
    {
        return epoch.fromTimeStamp( time );
    }

    public static int getYearStep( double spanYears )
    {
        double log10 = Math.log10( spanYears );
        int order = ( int ) Math.floor( log10 );
        if ( ( log10 - order ) > ( 1.0 - 1e-12 ) ) order++;

        return ( int ) Math.max( 1, Math.pow( 10, order ) );
    }

    public static int getRoundedYear( int currentYear, int yearStep )
    {
        int numSteps = currentYear / yearStep;
        return numSteps * yearStep;
    }
    
    @Override
    public List<String> getTickLabels( Axis1D axis, List<TimeStamp> tickPositions )
    {
        double tickInterval = getTickInterval( tickPositions );
        TimeStampFormat format = getTickFormat( tickInterval );
        
        List<String> labels = Lists.newArrayList( );
        for ( TimeStamp tick : tickPositions )
        {
            String label = tick.toString( format );
            labels.add( label );
        }
        
        return labels;
    }

    @Override
    public List<TimeStruct> getTimeStructs( Axis1D axis, List<TimeStamp> tickTimes )
    {
        double tickInterval = getTickInterval( tickTimes );
        
        if ( tickInterval <= Time.fromMinutes( 1 ) )
        {
            return timeStructs( axis, tickTimes, getHourStructFactory( ), getHourDayMonthFormat( ) );
        }
        else if ( tickInterval <= Time.fromHours( 12 ) )
        {
            return timeStructs( axis, tickTimes, getDayStructFactory( ), getDayMonthYearFormat( ) );
        }
        else if ( tickInterval <= Time.fromDays( 10 ) )
        {
            return timeStructs( axis, tickTimes, getMonthStructFactory( ), getMonthYearFormat( ) );
        }
        else if ( tickInterval <= Time.fromDays( 60 ) )
        {
            return timeStructs( axis, tickTimes, getYearStructFactory( ), getYearFormat( ) );
        }
        else
        {
            return Collections.emptyList( );
        }
    }
    
    protected TimeStampFormat getTickFormat( double tickInterval )
    {
        if ( tickInterval < Time.fromMinutes( 1 ) )
        {
            return getSecondMinuteFormat( );
        }
        else if ( tickInterval <= Time.fromHours( 12 ) )
        {
            return getHourMinuteFormat( );
        }
        else if ( tickInterval <= Time.fromDays( 10 ) )
        {
            return getDayFormat( );
        }
        else if ( tickInterval <= Time.fromDays( 60 ) )
        {
            return getMonthFormat( );
        }
        else
        {
            return getYearFormat( );
        }
    }

    @Override
    public double getTickInterval( List<TimeStamp> list )
    {
        if ( list == null || list.size( ) < 2 )
        {
            return Time.fromSeconds( 1 );
        }
        else
        {
            TimeStamp t1 = list.get( 0 );
            TimeStamp t2 = list.get( 1 );
            return t2.durationAfter( t1 );
        }
    }
    
    @Override
    public List<TimeStamp> getTickPositions( Axis1D axis, double axisLengthPixels )
    {
        TimeStamp t0 = toTimeStamp( axis.getMin( ) );
        TimeStamp t1 = toTimeStamp( axis.getMax( ) );
        double approxTickInterval_SU = pixelsBetweenTicks * t1.durationAfter( t0 ) / axisLengthPixels;

        // ticks are placed differently depending on the span of time between each tick:
        //
        // * for spans exceeding a few months, ticks are placed at year, decade, century, etc.. boundaries
        // * for spans exceeding a few weeks, ticks are placed at month boundaries
        // * for spans exceeding a day, ticks are placed at day boundaries, with a guarantee that the first
        //                              day of each month will receive a tick
        // * for smaller spans, ticks are placed at even hour, minute, or second boundaries
        //
        if ( approxTickInterval_SU > Time.fromDays( 60 ) )
        {
            Calendar cal = t0.toCalendar( );
            cal.setTimeZone( defaultTimeZone );

            int currentYear = cal.get( Calendar.YEAR );
            double daysPerYear = 365.25; // assume 365.25 days in every year as a heuristic
            double approxTickInterval_Years = Time.toDays( approxTickInterval_SU ) / daysPerYear;

            int stepYears = getYearStep( approxTickInterval_Years * yearOrderFactor );
            int startYear = getRoundedYear( currentYear, stepYears );

            cal.set( Calendar.YEAR, startYear );
            cal.set( Calendar.MONTH, 0 );
            cal.set( Calendar.DAY_OF_MONTH, 1 );
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0 );
            cal.set( Calendar.SECOND, 0 );
            cal.set( Calendar.MILLISECOND, 0 );

            // calculate end time and number of minutes between ticks
            long endTime = t1.toPosixMillis( );

            List<TimeStamp> times = new ArrayList<TimeStamp>( );
            while ( cal.getTimeInMillis( ) <= endTime )
            {
                times.add( TimeStamp.fromCalendar( cal ) );
                cal.add( Calendar.YEAR, stepYears );
            }

            return times;

        }
        else if ( approxTickInterval_SU > Time.fromDays( 10 ) )
        {
            Calendar cal = t0.toCalendar( );
            cal.setTimeZone( defaultTimeZone );
            cal.set( Calendar.DAY_OF_MONTH, 1 );
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0 );
            cal.set( Calendar.SECOND, 0 );
            cal.set( Calendar.MILLISECOND, 0 );

            // calculate end time and number of minutes between ticks
            long endTime = t1.toPosixMillis( );

            List<TimeStamp> times = new ArrayList<TimeStamp>( );
            while ( cal.getTimeInMillis( ) <= endTime )
            {
                times.add( TimeStamp.fromCalendar( cal ) );
                cal.add( Calendar.MONTH, 1 );
            }

            return times;
        }
        else if ( approxTickInterval_SU > Time.fromDays( 1 ) )
        {
            int tickInterval_Days = tickInterval_Days( approxTickInterval_SU );

            // initialize calendar off start time and reset fields less than month
            Calendar cal = t0.toCalendar( );
            cal.setTimeZone( defaultTimeZone );
            cal.set( Calendar.DAY_OF_MONTH, 1 );
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0 );
            cal.set( Calendar.SECOND, 0 );
            cal.set( Calendar.MILLISECOND, 0 );

            // calculate end time and number of minutes between ticks
            long endTime = t1.toPosixMillis( ) + ( long ) Time.daysToMilliseconds( tickInterval_Days );
            int currentMonth = cal.get( Calendar.MONTH );

            List<TimeStamp> times = new ArrayList<TimeStamp>( );
            while ( cal.getTimeInMillis( ) <= endTime )
            {
                // ensure ticks always fall on the first day of the month
                int newMonth = cal.get( Calendar.MONTH );
                if ( newMonth != currentMonth )
                {
                    cal.set( Calendar.DAY_OF_MONTH, 1 );
                    currentMonth = newMonth;
                }

                // don't display ticks too close to the end of the month
                int max_day = cal.getActualMaximum( Calendar.DAY_OF_MONTH );
                int day = cal.get( Calendar.DAY_OF_MONTH );
                if ( max_day - day + 1 >= tickInterval_Days / 2 )
                {
                    times.add( TimeStamp.fromCalendar( cal ) );
                }

                cal.add( Calendar.DAY_OF_MONTH, tickInterval_Days );
            }

            return times;
        }
        else
        {
            double tickInterval_SU = tickInterval_SU( approxTickInterval_SU );

            // Put ticks on nice round numbers in _local_ time
            double zoneOffset_SU = Time.fromMilliseconds( timeZone.getOffset( t0.toPosixMillis( ) ) );

            double ticksSinceEpoch = Math.floor( ( t0.toPosixSeconds( ) + zoneOffset_SU ) / tickInterval_SU );
            TimeStamp firstTick = TimeStamp.fromPosixSeconds( tickInterval_SU * ticksSinceEpoch - zoneOffset_SU );
            int numTicks = ( int ) Math.ceil( 1 + ( t1.durationAfter( firstTick ) / tickInterval_SU ) );

            List<TimeStamp> times = new ArrayList<TimeStamp>( numTicks );
            for ( int i = 0; i < numTicks; i++ )
                times.add( firstTick.add( i * tickInterval_SU ) );
            return times;
        }
    }

    //@formatter:off
    public static int[] rungs_days_SU = { 2, 3, 4, 5, 8, 10 };
    //@formatter:on

    public static int tickInterval_Days( double approxTickInterval_SU )
    {
        double approxTickInterval_Days = Time.toDays( approxTickInterval_SU );

        for ( int r : rungs_days_SU )
            if ( approxTickInterval_Days <= r ) return r;

        return 10;
    }

    //@formatter:off
    public static double[] rungs_SU =
      { Time.fromSeconds( 1 ),
        Time.fromSeconds( 2 ),
        Time.fromSeconds( 5 ),
        Time.fromSeconds( 10 ),
        Time.fromSeconds( 15 ),
        Time.fromSeconds( 20 ),
        Time.fromSeconds( 30 ),
        Time.fromMinutes( 1 ),
        Time.fromMinutes( 2 ),
        Time.fromMinutes( 5 ),
        Time.fromMinutes( 10 ),
        Time.fromMinutes( 15 ),
        Time.fromMinutes( 20 ),
        Time.fromMinutes( 30 ),
        Time.fromHours( 1 ),
        Time.fromHours( 2 ),
        Time.fromHours( 3 ),
        Time.fromHours( 6 ),
        Time.fromHours( 12 ),
        Time.fromDays( 1 )};
    //@formatter:on

    public static double tickInterval_SU( double approxTickInterval_SU )
    {
        for ( double r : rungs_SU )
            if ( approxTickInterval_SU <= r ) return r;

        return Time.fromDays( 1 );
    }

    public static class YearStruct extends TimeStruct
    {

        @Override
        public void setCalendar( TimeStamp time, Calendar calendar )
        {
            calendar.setTimeInMillis( time.toPosixMillis( ) );
            calendar.set( Calendar.MONTH, 0 );
            calendar.set( Calendar.DAY_OF_MONTH, 0 );
            calendar.set( Calendar.HOUR_OF_DAY, 0 );
            calendar.set( Calendar.MINUTE, 0 );
            calendar.set( Calendar.SECOND, 0 );
        }

        @Override
        public void incrementCalendar( Calendar calendar )
        {
            calendar.add( Calendar.YEAR, 1 );
        }

    }

    public static class MonthStruct extends TimeStruct
    {

        @Override
        public void setCalendar( TimeStamp time, Calendar calendar )
        {
            calendar.setTimeInMillis( time.toPosixMillis( ) );
            calendar.set( Calendar.DAY_OF_MONTH, 0 );
            calendar.set( Calendar.HOUR_OF_DAY, 0 );
            calendar.set( Calendar.MINUTE, 0 );
            calendar.set( Calendar.SECOND, 0 );
        }

        @Override
        public void incrementCalendar( Calendar calendar )
        {
            calendar.add( Calendar.MONTH, 1 );
        }

    }

    public static class DayStruct extends TimeStruct
    {

        @Override
        public void setCalendar( TimeStamp time, Calendar calendar )
        {
            calendar.setTimeInMillis( time.toPosixMillis( ) );
            calendar.set( Calendar.HOUR_OF_DAY, 0 );
            calendar.set( Calendar.MINUTE, 0 );
            calendar.set( Calendar.SECOND, 0 );
        }

        @Override
        public void incrementCalendar( Calendar calendar )
        {
            calendar.add( Calendar.DATE, 1 );
        }

    }

    public static class HourStruct extends TimeStruct
    {

        @Override
        public void setCalendar( TimeStamp time, Calendar calendar )
        {
            calendar.setTimeInMillis( time.toPosixMillis( ) );
            calendar.set( Calendar.MINUTE, 0 );
            calendar.set( Calendar.SECOND, 0 );
        }

        @Override
        public void incrementCalendar( Calendar calendar )
        {
            calendar.add( Calendar.HOUR_OF_DAY, 1 );
        }

    }

    public static interface TimeStructFactory
    {
        public TimeStruct newTimeStruct( );
    }

    public static class DayStructFactory implements TimeStructFactory
    {
        @Override
        public TimeStruct newTimeStruct( )
        {
            return new DayStruct( );
        }
    }

    public static class MonthStructFactory implements TimeStructFactory
    {
        @Override
        public TimeStruct newTimeStruct( )
        {
            return new MonthStruct( );
        }
    }

    public static class YearStructFactory implements TimeStructFactory
    {
        @Override
        public TimeStruct newTimeStruct( )
        {
            return new YearStruct( );
        }
    }

    public static class HourStructFactory implements TimeStructFactory
    {
        @Override
        public TimeStruct newTimeStruct( )
        {
            return new HourStruct( );
        }
    }

    public static <T extends Comparable<T>> T min( T a, T b )
    {
        return ( a.compareTo( b ) < 0 ? a : b );
    }

    public static <T extends Comparable<T>> T max( T a, T b )
    {
        return ( a.compareTo( b ) > 0 ? a : b );
    }

    protected List<TimeStruct> timeStructs( Axis1D axis, List<TimeStamp> tickTimes, TimeStructFactory factory, TimeStampFormat format )
    {
        TimeStamp viewStart = toTimeStamp( axis.getMin( ) );
        TimeStamp viewEnd = toTimeStamp( axis.getMax( ) );

        List<TimeStruct> days = new ArrayList<TimeStruct>( );
        double maxDayViewDuration = Double.NEGATIVE_INFINITY;
        Calendar calendar = Calendar.getInstance( timeZone );

        TimeStamp previousStart = null;

        for ( TimeStamp t : tickTimes )
        {
            TimeStruct day = factory.newTimeStruct( );

            day.setCalendar( t, calendar );
            day.start = TimeStamp.fromPosixMillis( calendar.getTimeInMillis( ) );

            if ( previousStart != null && previousStart.equals( day.start ) ) continue;
            previousStart = day.start;

            day.incrementCalendar( calendar );
            day.end = TimeStamp.fromPosixMillis( calendar.getTimeInMillis( ) );

            day.viewStart = min( day.end, max( day.start, viewStart ) );
            day.viewEnd = min( day.end, max( day.start, viewEnd ) );

            maxDayViewDuration = Math.max( maxDayViewDuration, day.viewEnd.durationAfter( day.viewStart ) );

            days.add( day );
        }

        for ( TimeStruct day : days )
        {
            double duration = day.viewEnd.durationAfter( day.viewStart );
            TimeStamp midpoint = day.viewStart.add( 0.5 * duration );
            TimeStamp edge = ( day.viewStart.equals( day.start ) ? day.viewEnd : day.viewStart );
            double edginess = 1 - Math.max( 0, Math.min( 1, duration / maxDayViewDuration ) );
            day.textCenter = midpoint.add( edginess * edge.durationAfter( midpoint ) );
        }
        
        for ( TimeStruct day : days )
        {
            day.text = day.textCenter.toString( format );
        }

        return days;
    }

    @Override
    public double[] getTickPositions( Axis1D axis )
    {
        List<TimeStamp> tickList = getTickPositions( axis, axis.getSizePixels( ) );
        double[] tickArray = new double[tickList.size( )];

        for ( int i = 0; i < tickList.size( ); i++ )
        {
            tickArray[i] = fromTimeStamp( tickList.get( i ) );
        }

        return tickArray;
    }

    @Override
    public String[] getTickLabels( Axis1D axis, double[] tickPositions )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public double[] getMinorTickPositions( double[] tickPositions )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public String getAxisLabel( Axis1D axis )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public void setAxisLabel( String label )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public AxisUnitConverter getAxisUnitConverter( )
    {
        return this.converter;
    }

    @Override
    public void setAxisUnitConverter( AxisUnitConverter converter )
    {
        this.converter = converter;
    }
}

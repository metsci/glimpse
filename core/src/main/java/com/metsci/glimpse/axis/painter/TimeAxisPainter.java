/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.axis.painter;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter1D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.util.units.time.Time;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.metsci.glimpse.util.units.time.format.TimeStampFormat;
import com.metsci.glimpse.util.units.time.format.TimeStampFormatStandard;
import com.sun.opengl.util.j2d.TextRenderer;

/**
 * A painter for displaying timeline axes. Axis values are interpreted as offsets
 * from an epoch. Both dates and times are displayed with a configurable format
 * and time zone.
 *
 * @author ulman
 */
public abstract class TimeAxisPainter extends GlimpsePainter1D
{
    protected static final TimeZone defaultTimeZone = TimeZone.getTimeZone( "UTC" );

    protected static final TimeStampFormat defaultMinuteSecondFormat = new TimeStampFormatStandard( "%m:%S", defaultTimeZone );
    protected static final TimeStampFormat defaultHourDayMonthFormat = new TimeStampFormatStandard( "%d %3N %H:00 ", defaultTimeZone );

    protected static final TimeStampFormat defaultHourMinuteFormat = new TimeStampFormatStandard( "%H:%m", defaultTimeZone );
    protected static final TimeStampFormat defaultDayMonthYearFormat = new TimeStampFormatStandard( "%d %3N %y", defaultTimeZone );

    protected static final TimeStampFormat defaultDayFormat = new TimeStampFormatStandard( "%d", defaultTimeZone );
    protected static final TimeStampFormat defaultMonthYearFormat = new TimeStampFormatStandard( "%3N %y", defaultTimeZone );

    protected static final TimeStampFormat defaultMonthFormat = new TimeStampFormatStandard( "%3N", defaultTimeZone );
    protected static final TimeStampFormat defaultYearFormat = new TimeStampFormatStandard( "%y", defaultTimeZone );

    protected static final TimeStructFactory hourStructFactory = new HourStructFactory( );
    protected static final TimeStructFactory dayStructFactory = new DayStructFactory( );
    protected static final TimeStructFactory monthStructFactory = new MonthStructFactory( );
    protected static final TimeStructFactory yearStructFactory = new YearStructFactory( );

    protected float[] tickColor;
    protected float[] textColor;

    protected TextRenderer textRenderer;
    protected volatile Font newFont = null;
    protected volatile boolean antialias = false;

    protected Epoch epoch;
    protected final TimeZone timeZone;
    protected final TimeStampFormat minuteSecondFormat;
    protected final TimeStampFormat hourDayMonthFormat;
    protected final TimeStampFormat hourMinuteFormat;
    protected final TimeStampFormat dayMonthYearFormat;
    protected final TimeStampFormat dayFormat;
    protected final TimeStampFormat monthFormat;
    protected final TimeStampFormat monthYearFormat;
    protected final TimeStampFormat yearFormat;

    protected boolean showCurrentTimeLabel = false;
    protected float[] currentTimeTextColor;
    protected float[] currentTimeTickColor;
    protected float currentTimeLineThickness;

    protected int hoverLabelOffset = 4;
    protected int tickLineLength = 4;

    protected int pixelsBetweenTicks = 60;
    protected double yearOrderFactor = 6.0;
    
    protected boolean fontSet = false;
    protected boolean tickColorSet = false;
    protected boolean labelColorSet = false;

    //@formatter:off
    public TimeAxisPainter( TimeStampFormat minuteSecondFormat,
                            TimeStampFormat hourDayMonthFormat,
                            TimeStampFormat hourMinuteFormat,
                            TimeStampFormat dayMonthYearFormat,
                            TimeStampFormat dayFormat,
                            TimeStampFormat monthFormat,
                            TimeStampFormat monthYearFormat,
                            TimeStampFormat yearFormat,
                            TimeZone timeZone, Epoch epoch )
    {
        this.newFont = FontUtils.getBitstreamVeraSansPlain( 12.0f );

        this.timeZone = timeZone;

        this.minuteSecondFormat = minuteSecondFormat;
        this.hourDayMonthFormat = hourDayMonthFormat;

        this.hourMinuteFormat = hourMinuteFormat;
        this.dayMonthYearFormat = dayMonthYearFormat;

        this.dayFormat = dayFormat;
        this.monthYearFormat = monthYearFormat;

        this.monthFormat = monthFormat;
        this.yearFormat = yearFormat;

        this.epoch = epoch;

        this.tickColor = GlimpseColor.getBlack( );
        this.textColor = GlimpseColor.getBlack( );

        this.setCurrentTimeTextColor( GlimpseColor.getGreen( 0.5f ) );
        this.setCurrentTimeTickColor( GlimpseColor.getGreen( 1.0f ) );
        this.currentTimeLineThickness = 3;
    }
    //@formatter:on

    public void setTickLineLength( int pixels )
    {
        this.tickLineLength = pixels;
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

    public void setCurrentTimeTickColor( float[] color )
    {
        this.currentTimeTickColor = color;
    }

    public void setCurrentTimeTextColor( float[] color )
    {
        this.currentTimeTextColor = color;
    }

    public void showCurrentTimeLabel( boolean show )
    {
        this.showCurrentTimeLabel = show;
    }

    public void setFont( Font font )
    {
        setFont( font, true );
    }
    
    public void setTickColor( float[] color )
    {
        this.tickColor = color;
        this.tickColorSet = true;
    }

    public void setTextColor( float[] color )
    {
        this.textColor = color;
        this.labelColorSet = true;
    }

    public void setFont( Font font, boolean antialias )
    {
        this.newFont = font;
        this.antialias = antialias;
        this.fontSet = true;
    }

    protected double tickInterval( List<TimeStamp> list )
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

    protected static int getYearStep( double spanYears )
    {
        double log10 = Math.log10( spanYears );
        int order = ( int ) Math.floor( log10 );
        if ( ( log10 - order ) > ( 1.0 - 1e-12 ) ) order++;

        return (int) Math.max( 1, Math.pow( 10, order ) );
    }

    protected static int getRoundedYear( int currentYear, int yearStep )
    {
        int numSteps = currentYear / yearStep;
        return numSteps * yearStep;
    }

    protected List<TimeStamp> tickTimes( Axis1D axis, double axisLengthPixels )
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
            while( cal.getTimeInMillis( ) <= endTime )
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
            while( cal.getTimeInMillis( ) <= endTime )
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
            long endTime = t1.toPosixMillis( ) + (long) Time.daysToMilliseconds( tickInterval_Days );
            int currentMonth = cal.get( Calendar.MONTH );

            List<TimeStamp> times = new ArrayList<TimeStamp>( );
            while( cal.getTimeInMillis( ) <= endTime )
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
            TimeStamp epoch = TimeStamp.posixEpoch( );
            TimeStamp firstTick = epoch.add( tickInterval_SU * Math.floor( ( t0.durationAfter( epoch ) + zoneOffset_SU ) / tickInterval_SU ) - zoneOffset_SU );
            double numTicks = 1 + ( t1.durationAfter( firstTick ) / tickInterval_SU );

            List<TimeStamp> times = new ArrayList<TimeStamp>( );
            for ( int i = 0; i < numTicks; i++ )
                times.add( firstTick.add( i * tickInterval_SU ) );
            return times;
        }
    }

    protected static int tickInterval_Days( double approxTickInterval_SU )
    {
        double approxTickInterval_Days = Time.secondsToDays( approxTickInterval_SU );

        //@formatter:off
        int[] rungs_SU = { 2, 3, 4, 5, 8, 10 };
        //@formatter:on

        for ( int r : rungs_SU )
            if ( approxTickInterval_Days <= r ) return r;

        return 10;
    }

    protected static double tickInterval_SU( double approxTickInterval_SU )
    {
        //@formatter:off
        double[] rungs_SU = { Time.fromSeconds( 1 ),
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

        for ( double r : rungs_SU )
            if ( approxTickInterval_SU <= r ) return r;

        return Time.fromDays( 1 );
    }

    protected static abstract class TimeStruct
    {
        public TimeStamp start;
        public TimeStamp end;
        public TimeStamp viewStart;
        public TimeStamp viewEnd;
        public TimeStamp textCenter;

        public abstract void setCalendar( TimeStamp time, Calendar cal );
        public abstract void incrementCalendar( Calendar cal );
    }

    protected static class YearStruct extends TimeStruct
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

    protected static class MonthStruct extends TimeStruct
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

    protected static class DayStruct extends TimeStruct
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

    protected static class HourStruct extends TimeStruct
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

    protected static interface TimeStructFactory
    {
        public TimeStruct newTimeStruct( );
    }

    protected static class DayStructFactory implements TimeStructFactory
    {
        @Override
        public TimeStruct newTimeStruct( )
        {
            return new DayStruct( );
        }
    }

    protected static class MonthStructFactory implements TimeStructFactory
    {
        @Override
        public TimeStruct newTimeStruct( )
        {
            return new MonthStruct( );
        }
    }

    protected static class YearStructFactory implements TimeStructFactory
    {
        @Override
        public TimeStruct newTimeStruct( )
        {
            return new YearStruct( );
        }
    }

    protected static class HourStructFactory implements TimeStructFactory
    {
        @Override
        public TimeStruct newTimeStruct( )
        {
            return new HourStruct( );
        }
    }


    protected static <T extends Comparable<T>> T min( T a, T b )
    {
        return ( a.compareTo( b ) < 0 ? a : b );
    }

    protected static <T extends Comparable<T>> T max( T a, T b )
    {
        return ( a.compareTo( b ) > 0 ? a : b );
    }

    protected List<TimeStruct> timeStructs( Axis1D axis, List<TimeStamp> tickTimes, TimeStructFactory factory )
    {
        TimeStamp viewStart = toTimeStamp( axis.getMin( ) );
        TimeStamp viewEnd = toTimeStamp( axis.getMax( ) );

        List<TimeStruct> days = new ArrayList<TimeStruct>( );
        double maxDayViewDuration = Double.NEGATIVE_INFINITY;
        Calendar calendar = Calendar.getInstance( timeZone );

        for ( TimeStamp t : tickTimes )
        {
            TimeStruct day = factory.newTimeStruct( );
            days.add( day );

            day.setCalendar( t, calendar );
            day.start = TimeStamp.fromPosixMillis( calendar.getTimeInMillis( ) );

            day.incrementCalendar( calendar );
            day.end = TimeStamp.fromPosixMillis( calendar.getTimeInMillis( ) );

            day.viewStart = min( day.end, max( day.start, viewStart ) );
            day.viewEnd = min( day.end, max( day.start, viewEnd ) );

            maxDayViewDuration = Math.max( maxDayViewDuration, day.viewEnd.durationAfter( day.viewStart ) );
        }

        for ( TimeStruct day : days )
        {
            double duration = day.viewEnd.durationAfter( day.viewStart );
            TimeStamp midpoint = day.viewStart.add( 0.5 * duration );
            TimeStamp edge = ( day.viewStart.equals( day.start ) ? day.viewEnd : day.viewStart );
            double edginess = 1 - Math.max( 0, Math.min( 1, duration / maxDayViewDuration ) );
            day.textCenter = midpoint.add( edginess * edge.durationAfter( midpoint ) );

        }

        return days;
    }
    
    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        if ( laf == null ) return;
        
        // ignore the look and feel if a font has been manually set
        if ( !fontSet )
        {
            setFont( laf.getFont( AbstractLookAndFeel.AXIS_FONT ), false );
            fontSet = false;
        }
        
        if ( !labelColorSet )
        {
            setTextColor( laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR ) );
            labelColorSet = false;
        }
        
        if ( !tickColorSet )
        {
            setTickColor( laf.getColor( AbstractLookAndFeel.AXIS_TICK_COLOR ) );
            tickColorSet = false;
        }
    }

    @Override
    public void dispose( GLContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;
    }
    
    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        if ( newFont != null )
        {
            if ( textRenderer != null ) textRenderer.dispose( );
            textRenderer = new TextRenderer( newFont, antialias, false );
            newFont = null;
        }
    }
}

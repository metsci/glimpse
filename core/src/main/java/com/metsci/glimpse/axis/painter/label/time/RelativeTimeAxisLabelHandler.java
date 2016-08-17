package com.metsci.glimpse.axis.painter.label.time;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.units.time.Time;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.metsci.glimpse.util.units.time.format.TimeStampFormat;
import com.metsci.glimpse.util.units.time.format.TimeStampParseException;

public class RelativeTimeAxisLabelHandler implements TimeAxisLabelHandler
{
    protected TimeStamp referenceTime;

    protected Epoch epoch;
    protected int pixelsBetweenTicks = 60;
    protected boolean isFuturePositive;


    public RelativeTimeAxisLabelHandler( TimeStamp referenceTime )
    {
        this( referenceTime, true );
    }
    
    public RelativeTimeAxisLabelHandler( TimeStamp referenceTime, boolean isFuturePositive )
    {
        this.isFuturePositive = isFuturePositive;
        this.referenceTime = referenceTime;
        this.epoch = new Epoch( this.referenceTime );
    }
    
    public void setFuturePositive( boolean isFuturePositive )
    {
        this.isFuturePositive = isFuturePositive;
    }
    
    public boolean setFuturePositive( )
    {
        return this.isFuturePositive;
    }
    
    public void setReferenceTime( TimeStamp referenceTime )
    {
        this.referenceTime = referenceTime;
    }
    
    public TimeStamp getReferenceTime( )
    {
        return this.referenceTime;
    }
    
    public TimeStamp toTimeStamp( double time )
    {
        return epoch.toTimeStamp( time );
    }

    public double fromTimeStamp( TimeStamp time )
    {
        return epoch.fromTimeStamp( time );
    }

    @Override
    public void setEpoch( Epoch epoch )
    {
        this.epoch = epoch;
    }

    @Override
    public Epoch getEpoch( )
    {
        return epoch;
    }

    @Override
    public void setTimeZone( TimeZone timeZone )
    {
        // do nothing
    }

    @Override
    public TimeZone getTimeZone( )
    {
        // do nothing
        return null;
    }

    @Override
    public void setPixelsBetweenTicks( int pixels )
    {
        this.pixelsBetweenTicks = pixels;
    }

    @Override
    public List<TimeStamp> getTickPositions( Axis1D axis, double axisLengthPixels )
    {
        TimeStamp minTime = epoch.toTimeStamp( axis.getMin( ) );
        TimeStamp maxTime = epoch.toTimeStamp( axis.getMax( ) );

        double approxTickInterval = pixelsBetweenTicks * ( maxTime.durationAfter( minTime ) ) / axisLengthPixels;

        if ( approxTickInterval < Time.fromDays( 1 ) )
        {
            return getTickPositionsHour( minTime, maxTime, referenceTime, approxTickInterval );
        }
        else
        {
            return getTickPositionsDay( minTime, maxTime, referenceTime, axisLengthPixels, pixelsBetweenTicks );
        }
    }

    protected List<TimeStamp> getTickPositionsHour( TimeStamp minTime, TimeStamp maxTime, TimeStamp referenceTime, double approxTickInterval )
    {
        double tickInterval_su = AbsoluteTimeAxisLabelHandler.tickInterval_SU( approxTickInterval );
        double referenceTime_su = referenceTime.toPosixSeconds( );
        double minTime_su = minTime.toPosixSeconds( ) - referenceTime_su;
        double maxTime_su = maxTime.toPosixSeconds( ) - referenceTime_su;

        int ticksSinceReference = (int) Math.floor( minTime_su / tickInterval_su );
        double firstTick_su = ticksSinceReference * tickInterval_su;
        int numTicks = ( int ) Math.ceil( 1 + ( maxTime_su - firstTick_su ) / tickInterval_su );

        List<TimeStamp> tickTimes = new ArrayList<>( numTicks );
        for ( int n = 0; n < numTicks; n++ )
        {
            tickTimes.add( TimeStamp.fromPosixSeconds( firstTick_su + n * tickInterval_su + referenceTime_su ) );
        }
        return tickTimes;
    }

    protected List<TimeStamp> getTickPositionsDay( TimeStamp minTime, TimeStamp maxTime, TimeStamp referenceTime, double axisLengthPixels, int pixelsBetweenTicks )
    {
        double referenceTime_su = referenceTime.toPosixSeconds( );
        double minTime_su = minTime.toPosixSeconds( ) - referenceTime_su;
        double maxTime_su = maxTime.toPosixSeconds( ) - referenceTime_su;

        GridAxisLabelHandler handler = new GridAxisLabelHandler( );
        handler.setTickSpacing( pixelsBetweenTicks );
        Axis1D axis = new Axis1D( );
        axis.setMin( Time.toDays( minTime_su ) );
        axis.setMax( Time.toDays( maxTime_su ) );
        axis.setSizePixels( ( int ) axisLengthPixels );

        double[] ticks = handler.getTickPositions( axis );

        List<TimeStamp> tickTimes = new ArrayList<>( ticks.length );
        for ( int n = 0; n < ticks.length; n++ )
        {
            tickTimes.add( TimeStamp.fromPosixSeconds( Time.fromDays( ticks[n] ) + referenceTime_su ) );
        }

        return tickTimes;
    }

    @Override
    public List<String> getTickLabels( Axis1D axis, List<TimeStamp> tickPositions )
    {
        TimeStampFormat format = getTickFormat( getTickInterval( tickPositions ), referenceTime );

        List<String> tickLabels = new ArrayList<>( tickPositions.size( ) );
        for ( int n = 0; n < tickPositions.size( ); n++ )
        {
            tickLabels.add( tickPositions.get( n ).toString( format ) );
        }

        return tickLabels;
    }

    protected TimeStampFormat getTickFormat( double tickInterval, final TimeStamp referenceTime )
    {
        if ( tickInterval <= Time.fromMinutes( 1 ) )
        {
            return new TimeStampFormat( )
            {
                @Override
                public BigDecimal parse( String string ) throws TimeStampParseException
                {
                    throw new UnsupportedOperationException( );
                }

                @Override
                public String format( BigDecimal posixSeconds )
                {
                    double elapsedTime_SU = Math.abs( posixSeconds.doubleValue( ) - referenceTime.toPosixSeconds( ) );
                    double elapsedTime_DAYS = Time.toDays( elapsedTime_SU );
                    int elapsedTime_DAYS_WHOLE = (int) Math.floor( elapsedTime_DAYS );
                    double elapsedTime_HOURS = ( elapsedTime_DAYS - elapsedTime_DAYS_WHOLE ) * 24;
                    int elapsedTime_HOURS_WHOLE = (int) Math.floor( elapsedTime_HOURS );
                    double elapsedTime_MIN = ( elapsedTime_HOURS - elapsedTime_HOURS_WHOLE ) * 60;
                    int elapsedTime_MIN_WHOLE = (int) Math.floor( elapsedTime_MIN );
                    double elapsedTime_SEC = ( elapsedTime_MIN - elapsedTime_MIN_WHOLE ) * 60;
                    // use round() here instead of floor() because we always expect ticks to be on even second
                    // boundaries but rounding error will cause us to be somewhat unpredictably above or below
                    // the nearest even second boundary
                    int elapsedTime_SEC_WHOLE = (int) Math.round( elapsedTime_SEC );
                    // however the above fails when we round up to a whole minute, so special case that
                    if ( elapsedTime_SEC_WHOLE >= 60 )
                    {
                        elapsedTime_SEC_WHOLE -= 60;
                        elapsedTime_MIN_WHOLE += 1;
                    }
                    if ( elapsedTime_MIN_WHOLE >= 60 )
                    {
                        elapsedTime_HOURS_WHOLE = 0;
                    }
                    
                    String min = elapsedTime_MIN_WHOLE < 10 ? "0" + elapsedTime_MIN_WHOLE : "" + elapsedTime_MIN_WHOLE;
                    String sec = elapsedTime_SEC_WHOLE < 10 ? "0" + elapsedTime_SEC_WHOLE : "" + elapsedTime_SEC_WHOLE;

                    return min + ':' + sec;
                }
            };
        }
        else if ( tickInterval <= Time.fromHours( 12 ) )
        {
            return new TimeStampFormat( )
            {
                @Override
                public BigDecimal parse( String string ) throws TimeStampParseException
                {
                    throw new UnsupportedOperationException( );
                }

                @Override
                public String format( BigDecimal posixSeconds )
                {
                    double elapsedTime_SU = Math.abs( posixSeconds.doubleValue( ) - referenceTime.toPosixSeconds( ) );
                    double elapsedTime_DAYS = Time.toDays( elapsedTime_SU );
                    int elapsedTime_DAYS_WHOLE = (int) Math.floor( elapsedTime_DAYS );
                    double elapsedTime_HOURS = ( elapsedTime_DAYS - elapsedTime_DAYS_WHOLE ) * 24;
                    int elapsedTime_HOURS_WHOLE = (int) Math.floor( elapsedTime_HOURS );
                    double elapsedTime_MIN = ( elapsedTime_HOURS - elapsedTime_HOURS_WHOLE ) * 60;
                    // use round() here instead of floor() because we always expect ticks to be on even minute
                    // boundaries but rounding error will cause us to be somewhat unpredictably above or below
                    // the nearest even minute boundary
                    int elapsedTime_MIN_WHOLE = (int) Math.round( elapsedTime_MIN );
                    // however the above fails when we round up to a whole hour, so special case that
                    if ( elapsedTime_MIN_WHOLE >= 60 )
                    {
                        elapsedTime_MIN_WHOLE -= 60;
                        elapsedTime_HOURS_WHOLE += 1;
                    }
                    if ( elapsedTime_HOURS_WHOLE >= 24 )
                    {
                        elapsedTime_HOURS_WHOLE = 0;
                    }

                    String hour = elapsedTime_HOURS_WHOLE < 10 ? "0" + elapsedTime_HOURS_WHOLE : "" + elapsedTime_HOURS_WHOLE;
                    String min = elapsedTime_MIN_WHOLE < 10 ? "0" + elapsedTime_MIN_WHOLE : "" + elapsedTime_MIN_WHOLE;

                    return hour + ':' + min;
                }
            };
        }
        else
        {
            return new TimeStampFormat( )
            {
                @Override
                public BigDecimal parse( String string ) throws TimeStampParseException
                {
                    throw new UnsupportedOperationException( );
                }

                @Override
                public String format( BigDecimal posixSeconds )
                {
                    double elapsedTime_SU = posixSeconds.doubleValue( ) - referenceTime.toPosixSeconds( );
                    
                    boolean negative = ( elapsedTime_SU < 0 );
                    String signString = negative ^ !isFuturePositive ? "-" : "";
                    elapsedTime_SU = Math.abs( elapsedTime_SU );

                    int elapsedTime_DAYS = (int) Math.floor( Time.toDays( elapsedTime_SU ) );
                    return elapsedTime_DAYS == 0 ? String.valueOf( elapsedTime_DAYS ) : signString + elapsedTime_DAYS;
                }
            };
        }
    }

    @Override
    public List<TimeStruct> getTimeStructs( Axis1D axis, List<TimeStamp> tickTimes )
    {
        double tickInterval_su = getTickInterval( tickTimes );

        if ( tickInterval_su <= Time.fromMinutes( 1 ) )
        {
            return createTimeStructsRelativeHours( axis, referenceTime, tickTimes, 0.5 );
        }
        else if ( tickInterval_su <= Time.fromHours( 12 ) )
        {
            return createTimeStructsRelativeDays( axis, referenceTime, tickTimes, 0.5 );
        }
        else
        {
            return Collections.emptyList( );
        }
    }

    protected List<TimeStruct> createTimeStructsRelativeHours( Axis1D axis, TimeStamp referenceTime, List<TimeStamp> tickTimes, double labelAlign )
    {
        List<TimeStruct> list = new ArrayList<>( tickTimes.size( ) );

        TimeStamp minTime = epoch.toTimeStamp( axis.getMin( ) );
        TimeStamp maxTime = epoch.toTimeStamp( axis.getMax( ) );
        double referenceTime_su = referenceTime.toPosixSeconds( );

        double maxViewDuration_SU = Double.NEGATIVE_INFINITY;
        Integer previous_HOURS = null;
        Boolean previous_SIGN = null;

        for ( int n = 0; n < tickTimes.size( ); n++ )
        {
            double elapsedTime_SU = tickTimes.get( n ).toPosixSeconds( ) - referenceTime_su;
            
            boolean negative = ( elapsedTime_SU < 0 );
            String signString = negative ^ !isFuturePositive ? "-" : "";
            
            elapsedTime_SU = Math.abs( elapsedTime_SU );
            double elapsedTime_DAYS = Time.toDays( elapsedTime_SU );
            int elapsedTime_DAYS_WHOLE = (int) Math.floor( elapsedTime_DAYS );
            double elapsedTime_HOURS = ( elapsedTime_DAYS - elapsedTime_DAYS_WHOLE ) * 24;
            int elapsedTime_HOURS_WHOLE = (int) Math.floor( elapsedTime_HOURS );
            int elapsedTime_HOURS_TOTAL = elapsedTime_DAYS_WHOLE * 24 + elapsedTime_HOURS_WHOLE;
            
            if ( previous_HOURS != null && elapsedTime_HOURS_TOTAL == previous_HOURS && negative == previous_SIGN ) continue;
            previous_HOURS = elapsedTime_HOURS_TOTAL;
            previous_SIGN = negative;

            TimeStruct timeStruct = new TimeStructRelative( );

            if ( negative )
            {
                timeStruct.end = TimeStamp.fromPosixSeconds( Time.fromHours( -elapsedTime_HOURS_TOTAL ) + referenceTime_su );
                timeStruct.start = timeStruct.end.subtract( Time.fromHours( 1 ) );    
            }
            else
            {
                timeStruct.start = TimeStamp.fromPosixSeconds( Time.fromHours( elapsedTime_HOURS_TOTAL ) + referenceTime_su );
                timeStruct.end = timeStruct.start.add( Time.fromHours( 1 ) );                   
            }
            
            timeStruct.viewStart = clamp( timeStruct.start, timeStruct.end, minTime );
            timeStruct.viewEnd = clamp( timeStruct.start, timeStruct.end, maxTime );
            timeStruct.text = "Day " + signString + elapsedTime_DAYS_WHOLE + " Hour " + signString + elapsedTime_HOURS_WHOLE;

            maxViewDuration_SU = Math.max( maxViewDuration_SU, timeStruct.viewEnd.durationAfter( timeStruct.viewStart ) );

            list.add( timeStruct );
        }

        setTimeStructTextCenter( list, labelAlign, maxViewDuration_SU );

        return list;

    }

    protected List<TimeStruct> createTimeStructsRelativeDays( Axis1D axis, TimeStamp referenceTime, List<TimeStamp> tickTimes, double labelAlign )
    {
        List<TimeStruct> list = new ArrayList<>( tickTimes.size( ) );

        TimeStamp minTime = epoch.toTimeStamp( axis.getMin( ) );
        TimeStamp maxTime = epoch.toTimeStamp( axis.getMax( ) );
        double referenceTime_su = referenceTime.toPosixSeconds( );

        double maxViewDuration_SU = Double.NEGATIVE_INFINITY;
        Integer previous_DAYS = null;
        Boolean previous_SIGN = null;

        for ( int n = 0; n < tickTimes.size( ); n++ )
        {
            double elapsedTime_SU = tickTimes.get( n ).toPosixSeconds( ) - referenceTime_su;
            
            boolean negative = ( elapsedTime_SU < 0 );
            String signString = negative ^ !isFuturePositive ? "-" : "";

            elapsedTime_SU = Math.abs( elapsedTime_SU );
            
            double elapsedTime_DAYS = Time.toDays( elapsedTime_SU );
            int elapsedTime_DAYS_WHOLE = (int) Math.floor( elapsedTime_DAYS );

            if ( previous_DAYS != null && elapsedTime_DAYS_WHOLE == previous_DAYS && negative == previous_SIGN ) continue;
            previous_DAYS = elapsedTime_DAYS_WHOLE;
            previous_SIGN = negative;

            TimeStruct timeStruct = new TimeStructRelative( );

            if ( negative )
            {
                timeStruct.end = TimeStamp.fromPosixSeconds( Time.fromDays( -elapsedTime_DAYS_WHOLE ) + referenceTime_su );
                timeStruct.start = timeStruct.end.subtract( Time.fromDays( 1 ) );    
            }
            else
            {
                timeStruct.start = TimeStamp.fromPosixSeconds( Time.fromDays( elapsedTime_DAYS_WHOLE ) + referenceTime_su );
                timeStruct.end = timeStruct.start.add( Time.fromDays( 1 ) );                   
            }

            timeStruct.viewStart = clamp( timeStruct.start, timeStruct.end, minTime );
            timeStruct.viewEnd = clamp( timeStruct.start, timeStruct.end, maxTime );
            timeStruct.text = "Day " + signString + elapsedTime_DAYS_WHOLE;
            
            maxViewDuration_SU = Math.max( maxViewDuration_SU, timeStruct.viewEnd.durationAfter( timeStruct.viewStart ) );

            list.add( timeStruct );
        }

        setTimeStructTextCenter( list, labelAlign, maxViewDuration_SU );
        
        return list;
    }

    protected TimeStamp clamp( TimeStamp min, TimeStamp max, TimeStamp value )
    {
        if ( value.isBefore( min ) )
            return min;
        else if ( value.isAfter( max ) )
            return max;
        else
            return value;
    }

    protected double clamp( double min, double max, double value )
    {
        if ( value < min )
            return min;
        else if ( value > max )
            return max;
        else
            return value;
    }

    protected void setTimeStructTextCenter( List<TimeStruct> list, double labelAlign, double maxViewDuration_SU )
    {
        for ( int n = 0; n < list.size( ); n++ )
        {
            TimeStruct timeStruct = list.get( n );
            double duration_su = timeStruct.viewEnd.durationAfter( timeStruct.viewStart );
            TimeStamp midpoint = timeStruct.viewStart.add( labelAlign * duration_su );
            TimeStamp edge = ( timeStruct.viewStart.equals( timeStruct.start ) ? timeStruct.viewEnd : timeStruct.viewStart );
            double edginess = 1 - clamp( 0, 1, duration_su / maxViewDuration_SU );
            timeStruct.textCenter = midpoint.add( edginess * ( edge.durationAfter( midpoint ) ) );
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
        return null;
    }

    @Override
    public void setAxisUnitConverter( AxisUnitConverter converter )
    {
        // do nothing
    }

    protected class TimeStructRelative extends TimeStruct
    {
        @Override
        public void setCalendar( TimeStamp time, Calendar cal )
        {
        }

        @Override
        public void incrementCalendar( Calendar cal )
        {
        }
    }
}

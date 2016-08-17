package com.metsci.glimpse.axis.painter.label.time;

import java.util.List;
import java.util.TimeZone;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.units.time.TimeStamp;

public interface TimeAxisLabelHandler extends AxisLabelHandler
{
    public void setEpoch( Epoch epoch );
    public Epoch getEpoch( );

    public void setTimeZone( TimeZone timeZone );
    public TimeZone getTimeZone( );

    public void setPixelsBetweenTicks( int pixels );
    
    public List<TimeStamp> getTickPositions( Axis1D axis, double axisLengthPixels );
    public List<String> getTickLabels( Axis1D axis, List<TimeStamp> tickPositions );
    public List<TimeStruct> getTimeStructs( Axis1D axis, List<TimeStamp> tickTimes );
    public double getTickInterval( List<TimeStamp> tickTimes );

}

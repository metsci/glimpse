package com.metsci.glimpse.plot.timeline.data;

import com.metsci.glimpse.util.units.time.TimeStamp;

public class TimeSpan
{
    TimeStamp startTime;
    TimeStamp endTime;
    
    public TimeSpan( TimeStamp startTime, TimeStamp endTime )
    {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public double getDuration( )
    {
        return endTime.durationAfter( startTime );
    }

    public TimeStamp getStartTime( )
    {
        return startTime;
    }

    public void setStartTime( TimeStamp startTime )
    {
        this.startTime = startTime;
    }

    public TimeStamp getEndTime( )
    {
        return endTime;
    }

    public void setEndTime( TimeStamp endTime )
    {
        this.endTime = endTime;
    }
}

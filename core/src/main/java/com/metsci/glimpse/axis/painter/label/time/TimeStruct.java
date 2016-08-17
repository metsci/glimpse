package com.metsci.glimpse.axis.painter.label.time;

import java.util.Calendar;

import com.metsci.glimpse.util.units.time.TimeStamp;

public abstract class TimeStruct
{
    public TimeStamp start;
    public TimeStamp end;
    public TimeStamp viewStart;
    public TimeStamp viewEnd;
    public TimeStamp textCenter;
    public String text;

    public abstract void setCalendar( TimeStamp time, Calendar cal );

    public abstract void incrementCalendar( Calendar cal );
}

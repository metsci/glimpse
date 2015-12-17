package com.metsci.glimpse.plot.timeline.event.paint;

import java.util.Collection;

import javax.media.opengl.GL2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;

/**
 * An adapter class which utilizes a {@link EventPainter} to (inefficiently)
 * perform the function of a {@link GroupedEventPainter}.
 * 
 * This class should generally not be used. Instead, a {@link GroupedEventPainter}
 * should be implemented which efficiently draws all events simultaneously.
 * 
 * @author ulman
 */
public class GroupedEventPainterAdapter implements GroupedEventPainter
{
    protected EventPainter painter;
    
    public GroupedEventPainterAdapter( EventPainter painter )
    {
        this.painter = painter;
    }

    @Override
    public void paint( GL2 gl, EventPlotInfo info, GlimpseBounds bounds, Axis1D timeAxis, Collection<EventDrawInfo> events )
    {
        for ( EventDrawInfo eventInfo : events )
        {
            painter.paint( gl, eventInfo.event, eventInfo.nextEvent, info, bounds, timeAxis, eventInfo.posMin, eventInfo.posMax );
        }
    }
}
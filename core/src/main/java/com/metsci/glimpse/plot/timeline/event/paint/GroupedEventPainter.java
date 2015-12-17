package com.metsci.glimpse.plot.timeline.event.paint;

import java.util.Collection;

import javax.media.opengl.GL2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;

/**
 * A painter responsible for making OpenGL calls to visualize a collection of {@code Event}.
 * 
 * @author ulman
 */
public interface GroupedEventPainter
{
    /**
     * Draw all the events in the provided collection using the GL2 handle.
     * 
     * @param gl OpenGL handle
     * @param info parent EventPlotInfo of Event to be painted
     * @param bounds width, height, and position of GlimpseLayout containing EventPlotInfo
     * @param timeAxis the plot time axis
     * @param events the event objects to draw
     */
    public void paint( GL2 gl, EventPlotInfo info, GlimpseBounds bounds, Axis1D timeAxis, Collection<EventDrawInfo> events );
}
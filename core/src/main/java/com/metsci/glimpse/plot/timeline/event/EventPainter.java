package com.metsci.glimpse.plot.timeline.event;

import javax.media.opengl.GL;

import com.metsci.glimpse.context.GlimpseBounds;

public interface EventPainter
{
    /**
     * <p>Renders the provided Event (potentially displaying its icon, label, time extents, etc...).<p>
     * 
     * <p>Both the Event to be painted and the next Event in the row (the event with the next largest
     * start time) are provided. Only event should be rendered by this call. The nextEvent argument
     * is provided only as context to allow the EventPainter to modify its rendering to ensure that
     * it does not overlap with nextEvent.</p>
     * 
     * @param gl OpenGL handle
     * @param Event the Event to be painted
     * @param nextEvent the next Event to be painted (as ordered by start time) 
     * @param info parent EventPlotInfo of Event to be painted
     * @param bounds width, height, and position of GlimpseLayout containing EventPlotInfo
     * @param posMin the min y (or x, depending on orientation) in pixel coordinates of the Event
     * @param posMax the max y (or x, depending on orientation) in pixel coordinates of the Event
     */
    public void paint( GL gl, Event event, Event nextEvent, EventPlotInfo info, GlimpseBounds bounds, int posMin, int posMax );
}

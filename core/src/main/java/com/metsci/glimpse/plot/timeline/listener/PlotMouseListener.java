package com.metsci.glimpse.plot.timeline.listener;

import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.plot.stacked.PlotInfo;

public interface PlotMouseListener
{
    public static enum PlotLocation
    {
        Label,
        Plot;
    }
    
    public void mousePressed( GlimpseMouseEvent event, PlotInfo info, PlotLocation location );
    public void mouseReleased( GlimpseMouseEvent event, PlotInfo info, PlotLocation location );
    
    public void mouseExited( GlimpseMouseEvent event, PlotInfo info, PlotLocation location );
    public void mouseEntered( GlimpseMouseEvent event, PlotInfo info, PlotLocation location );
    
    public void mouseWheelMoved( GlimpseMouseEvent event, PlotInfo info, PlotLocation location );
    public void mouseMoved( GlimpseMouseEvent event, PlotInfo info, PlotLocation location );
}

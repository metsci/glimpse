package com.metsci.glimpse.plot.timeline.layout;

import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.painter.info.TooltipPainter;

public interface TimeToolTipHandler
{
    public void setToolTip( GlimpseMouseEvent e, TooltipPainter tooltipPainter );
}
package com.metsci.glimpse.plot.timeline.event;

import com.metsci.glimpse.painter.info.TooltipPainter;
import com.metsci.glimpse.plot.timeline.data.EventSelection;

public interface EventToolTipHandler
{
    public void setToolTip( EventSelection selection, TooltipPainter tooltipPainter );
}

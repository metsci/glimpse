package com.metsci.glimpse.layers;

import com.metsci.glimpse.context.GlimpseContext;

public interface TimelineLayer extends Layer
{

    void installToTimeline( LayeredTimeline timeline );

    void uninstallFromTimeline( LayeredTimeline timeline, GlimpseContext context );

}

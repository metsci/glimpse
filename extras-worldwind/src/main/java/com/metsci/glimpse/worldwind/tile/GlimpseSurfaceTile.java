package com.metsci.glimpse.worldwind.tile;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseLayout;

public interface GlimpseSurfaceTile
{
    public GlimpseLayout getGlimpseLayout( );

    public GlimpseCanvas getGlimpseCanvas( );

    public GlimpseTargetStack getTargetStack( );
}

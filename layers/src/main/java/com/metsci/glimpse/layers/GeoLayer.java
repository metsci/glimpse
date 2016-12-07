package com.metsci.glimpse.layers;

import com.metsci.glimpse.context.GlimpseContext;

public interface GeoLayer extends Layer
{

    void installToGeo( LayeredGeo geo );

    void uninstallFromGeo( LayeredGeo geo, GlimpseContext context, boolean reinstalling );

}

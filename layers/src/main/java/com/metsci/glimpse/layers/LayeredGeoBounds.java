package com.metsci.glimpse.layers;

import com.metsci.glimpse.util.geo.LatLonGeo;

public class LayeredGeoBounds
{

    public final LatLonGeo center;
    public final double ewExtent_SU;
    public final double nsExtent_SU;

    public LayeredGeoBounds( LatLonGeo center, double ewExtent_SU, double nsExtent_SU )
    {
        this.center = center;
        this.ewExtent_SU = ewExtent_SU;
        this.nsExtent_SU = nsExtent_SU;
    }

}

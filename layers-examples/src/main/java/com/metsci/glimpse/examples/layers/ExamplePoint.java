package com.metsci.glimpse.examples.layers;

import com.metsci.glimpse.util.geo.LatLonGeo;

public class ExamplePoint
{

    public final long time_PMILLIS;
    public final LatLonGeo latlon;
    public final double z_SU;

    public ExamplePoint( long time_PMILLIS, LatLonGeo latlon, double z_SU )
    {
        this.time_PMILLIS = time_PMILLIS;
        this.latlon = latlon;
        this.z_SU = z_SU;
    }

}
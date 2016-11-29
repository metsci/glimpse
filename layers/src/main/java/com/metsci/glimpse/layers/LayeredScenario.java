package com.metsci.glimpse.layers;

import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

public class LayeredScenario
{

    public static class Builder
    {
        public GeoProjection geoProj;
        public Epoch timelineEpoch;

        public void setFrom( LayeredScenario scenario )
        {
            this.geoProj = scenario.geoProj;
            this.timelineEpoch = scenario.timelineEpoch;
        }

        public LayeredScenario build( )
        {
            return new LayeredScenario( this.geoProj, this.timelineEpoch );
        }
    }


    public final GeoProjection geoProj;

    public final Epoch timelineEpoch;


    public LayeredScenario( GeoProjection geoProj, Epoch timelineEpoch )
    {
        this.geoProj = geoProj;
        this.timelineEpoch = timelineEpoch;
    }

}

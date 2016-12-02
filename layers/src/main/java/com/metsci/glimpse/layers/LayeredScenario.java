package com.metsci.glimpse.layers;

import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

public class LayeredScenario
{

    public static class Builder
    {
        public GeoProjection geoProj;
        public LayeredGeoBounds geoInitBounds;

        public Epoch timelineEpoch;
        public LayeredTimelineBounds timelineInitBounds;

        public void setFrom( LayeredScenario scenario )
        {
            this.geoProj = scenario.geoProj;
            this.geoInitBounds = scenario.geoInitBounds;

            this.timelineEpoch = scenario.timelineEpoch;
            this.timelineInitBounds = scenario.timelineInitBounds;
        }

        public LayeredScenario build( )
        {
            return new LayeredScenario( this.geoProj,
                                        this.geoInitBounds,

                                        this.timelineEpoch,
                                        this.timelineInitBounds );
        }
    }


    public final GeoProjection geoProj;
    public final LayeredGeoBounds geoInitBounds;

    public final Epoch timelineEpoch;
    public final LayeredTimelineBounds timelineInitBounds;

    public LayeredScenario( GeoProjection geoProj,
                            LayeredGeoBounds geoInitBounds,

                            Epoch timelineEpoch,
                            LayeredTimelineBounds timelineInitBounds )
    {
        this.geoProj = geoProj;
        this.geoInitBounds = geoInitBounds;

        this.timelineEpoch = timelineEpoch;
        this.timelineInitBounds = timelineInitBounds;
    }

}

package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static com.metsci.glimpse.util.PredicateUtils.require;

import java.util.ArrayList;
import java.util.List;

import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisListener1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layers.GeoLayer;
import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.layers.LayeredGeo;
import com.metsci.glimpse.layers.LayeredScenario;
import com.metsci.glimpse.layers.LayeredTimeline;
import com.metsci.glimpse.layers.TimelineLayer;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

public class ExampleLayer implements Layer, GeoLayer, TimelineLayer
{

    public class CanonicalPoint
    {
        public final long time_PMILLIS;
        public final LatLonGeo latlon;
        public final double z_SU;

        public CanonicalPoint( long time_PMILLIS, LatLonGeo latlon, double z_SU )
        {
            this.time_PMILLIS = time_PMILLIS;
            this.latlon = latlon;
            this.z_SU = z_SU;
        }
    }

    protected static class ProjectedPoint
    {
        public final float t;
        public final float x;
        public final float y;
        public final float z;

        public ProjectedPoint( float t, float x, float y, float z )
        {
            this.t = t;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    protected static ProjectedPoint toProjectedPoint( Epoch timelineEpoch, GeoProjection geoProj, CanonicalPoint p )
    {
        float t = ( float ) timelineEpoch.fromPosixMillis( p.time_PMILLIS );

        Vector2d xy_SU = geoProj.project( p.latlon );
        float x_SU = ( float ) xy_SU.getX( );
        float y_SU = ( float ) xy_SU.getY( );

        float z_SU = ( float ) p.z_SU;

        return new ProjectedPoint( t, x_SU, y_SU, z_SU );
    }


    protected final List<CanonicalPoint> canonicalPoints;

    protected GeoProjection geoProj;
    protected Epoch timelineEpoch;
    protected final List<ProjectedPoint> projectedPoints;

    protected ExampleGeoPainter geoPainter;

    protected TimePlotInfo timelineRow;
    protected TaggedAxisListener1D timeAxisListener;
    protected ExampleTimelinePainter timelinePainter;


    public ExampleLayer( )
    {
        this.canonicalPoints = new ArrayList<>( );

        this.geoProj = null;
        this.timelineEpoch = null;
        this.projectedPoints = new ArrayList<>( );

        this.geoPainter = null;

        this.timelineRow = null;
        this.timeAxisListener = null;
        this.timelinePainter = null;
    }

    public void addPoint( long time_PMILLIS, LatLonGeo latlon, double z_SU )
    {
        CanonicalPoint canonicalPoint = new CanonicalPoint( time_PMILLIS, latlon, z_SU );
        this.canonicalPoints.add( canonicalPoint );

        // If init() hasn't been called yet, this point will get projected in init()
        if ( this.geoProj != null && this.timelineEpoch != null )
        {
            ProjectedPoint projectedPoint = toProjectedPoint( this.timelineEpoch, this.geoProj, canonicalPoint );
            this.projectedPoints.add( projectedPoint );

            // If installToGeo() hasn't been called yet, this point will get added to geoPainter in installToGeo()
            if ( this.geoPainter != null )
            {
                this.geoPainter.addPoint( projectedPoint.t, projectedPoint.x, projectedPoint.y, projectedPoint.z );
            }

            // If installToTimeline() hasn't been called yet, this point will get added to timelinePainter in installToTimeline()
            if ( this.timelinePainter != null )
            {
                this.timelinePainter.addPoint( projectedPoint.t, projectedPoint.x, projectedPoint.y, projectedPoint.z );
            }
        }
    }

    @Override
    public void init( LayeredScenario scenario )
    {
        this.geoProj = require( scenario.geoProj, notNull );
        this.timelineEpoch = require( scenario.timelineEpoch, notNull );

        // Project any points we already have
        this.projectedPoints.clear( );
        for ( CanonicalPoint canonicalPoint : this.canonicalPoints )
        {
            ProjectedPoint projectedPoint = toProjectedPoint( this.timelineEpoch, this.geoProj, canonicalPoint );
            this.projectedPoints.add( projectedPoint );
        }
    }

    @Override
    public void installToGeo( LayeredGeo geo )
    {
        this.geoPainter = new ExampleGeoPainter( this.geoProj, this.timelineEpoch );
        geo.dataPainter.addPainter( this.geoPainter );

        // Add points we already have
        for ( ProjectedPoint p : this.projectedPoints )
        {
            this.geoPainter.addPoint( p.t, p.x, p.y, p.z );
        }
    }

    @Override
    public void uninstallFromGeo( LayeredGeo geo, GlimpseContext context )
    {
        geo.dataPainter.removePainter( this.geoPainter );
        this.geoPainter.dispose( context );
        this.geoPainter = null;
    }

    @Override
    public void installToTimeline( LayeredTimeline timeline )
    {
        this.timelinePainter = new ExampleTimelinePainter( this.timelineEpoch );

        this.timelineRow = timeline.addPlotRow( "Example" );
        this.timelineRow.addPainter( this.timelinePainter );

        this.timeAxisListener = new TaggedAxisListener1D( )
        {
            @Override
            public void tagsUpdated( TaggedAxis1D axis )
            {
                float tMin = ( float ) timelineEpoch.fromTimeStamp( timeline.plot.getTimeSelectionMin( ) );
                float tMax = ( float ) timelineEpoch.fromTimeStamp( timeline.plot.getTimeSelectionMax( ) );
                geoPainter.setTimeSelection( tMin, tMax );
                timelinePainter.setTimeSelection( tMin, tMax );
            }
        };
        timeline.plot.getTimeAxis( ).addAxisListener( this.timeAxisListener );

        // Add points we already have
        for ( ProjectedPoint p : this.projectedPoints )
        {
            this.timelinePainter.addPoint( p.t, p.x, p.y, p.z );
        }
    }

    @Override
    public void uninstallFromTimeline( LayeredTimeline timeline, GlimpseContext context )
    {
        timeline.plot.getTimeAxis( ).removeAxisListener( this.timeAxisListener );
        this.timeAxisListener = null;

        this.timelineRow.removePainter( this.timelinePainter );
        this.timelinePainter.dispose( context );
        this.timelinePainter = null;

        timeline.plot.removePlot( this.timelineRow.getId( ) );
        this.timelineRow = null;
    }

}

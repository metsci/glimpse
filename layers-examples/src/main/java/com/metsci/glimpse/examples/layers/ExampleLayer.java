package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.layers.AxisSelection2D.axisSelection2D;
import static com.metsci.glimpse.layers.AxisUtils.addAxisListener2D;
import static com.metsci.glimpse.layers.AxisUtils.addTaggedAxisListener1D;
import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static com.metsci.glimpse.util.PredicateUtils.require;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.axis.tagged.TaggedAxisListener1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layers.AxisSelection2D;
import com.metsci.glimpse.layers.GeoLayer;
import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.layers.LayeredGeo;
import com.metsci.glimpse.layers.LayeredScenario;
import com.metsci.glimpse.layers.LayeredTimeline;
import com.metsci.glimpse.layers.TimeAxisSelection;
import com.metsci.glimpse.layers.TimelineLayer;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.util.GeneralUtils;
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


    protected final String title;
    protected final ExampleStyle style;

    protected final List<CanonicalPoint> canonicalPoints;

    protected GeoProjection geoProj;
    protected Epoch timelineEpoch;
    protected final List<ProjectedPoint> projectedPoints;

    protected ExampleGeoPainter geoPainter;
    protected AxisListener2D geoAxisListener;
    protected AxisSelection2D geoAxisSelection;

    protected TimePlotInfo timelineRow;
    protected ExampleTimelinePainter timelinePainter;
    protected TaggedAxisListener1D timeAxisListener;
    protected TimeAxisSelection timeAxisSelection;


    public ExampleLayer( String title, float[] rgba )
    {
        this.title = title;

        this.style = new ExampleStyle( );
        this.style.rgbaInsideTWindow = Arrays.copyOf( rgba, 4 );
        this.style.rgbaOutsideTWindow = GeneralUtils.floats( 0.4f + 0.6f*rgba[0], 0.4f + 0.6f*rgba[1], 0.4f + 0.6f*rgba[2], 0.4f*rgba[3] );

        this.canonicalPoints = new ArrayList<>( );

        this.geoProj = null;
        this.timelineEpoch = null;
        this.projectedPoints = new ArrayList<>( );

        this.geoPainter = null;
        this.geoAxisListener = null;
        this.geoAxisSelection = null;

        this.timelineRow = null;
        this.timelinePainter = null;
        this.timeAxisListener = null;
        this.timeAxisSelection = null;
    }

    @Override
    public String title( )
    {
        return this.title;
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
        this.geoPainter = new ExampleGeoPainter( this.style );
        geo.dataPainter.addPainter( this.geoPainter );

        // Initialize the new painter's T window
        this.setTimeAxisSelection( this.timeAxisSelection );

        // Initialize both painters' XY windows, and update them when geo selection changes
        this.geoAxisListener = addAxisListener2D( geo.plot.getCenterAxis( ), true, ( axis ) ->
        {
            this.setGeoAxisSelection( axisSelection2D( axis ) );
        } );

        // Add points we already have
        for ( ProjectedPoint p : this.projectedPoints )
        {
            this.geoPainter.addPoint( p.t, p.x, p.y, p.z );
        }
    }

    protected void setGeoAxisSelection( AxisSelection2D geoAxisSelection )
    {
        if ( geoAxisSelection != null )
        {
            this.geoAxisSelection = geoAxisSelection;

            float xMin = ( float ) this.geoAxisSelection.xSelection.min;
            float xMax = ( float ) this.geoAxisSelection.xSelection.max;
            float yMin = ( float ) this.geoAxisSelection.ySelection.min;
            float yMax = ( float ) this.geoAxisSelection.ySelection.max;

            if ( this.geoPainter != null )
            {
                this.geoPainter.setXyWindow( xMin, xMax, yMin, yMax );
            }

            if ( this.timelinePainter != null )
            {
                this.timelinePainter.setXyWindow( xMin, xMax, yMin, yMax );
            }
        }
    }

    @Override
    public void uninstallFromGeo( LayeredGeo geo, GlimpseContext context, boolean reinstalling )
    {
        geo.plot.getCenterAxis( ).removeAxisListener( this.geoAxisListener );
        this.geoAxisListener = null;

        geo.dataPainter.removePainter( this.geoPainter );
        this.geoPainter.dispose( context );
        this.geoPainter = null;
    }

    @Override
    public void installToTimeline( LayeredTimeline timeline )
    {
        // Use the same timelineRowId for all instances of ExampleLayer, so they all share a single plot
        String timelineRowId = "ExampleLayer.timelineRow";
        this.timelineRow = timeline.acquirePlotRow( timelineRowId, "Example" );

        this.timelinePainter = new ExampleTimelinePainter( this.style );
        this.timelineRow.addPainter( this.timelinePainter );

        // Initialize the new painter's XY window
        this.setGeoAxisSelection( this.geoAxisSelection );

        // Initialize both painters' T windows, and update them when time selection changes
        this.timeAxisListener = addTaggedAxisListener1D( timeline.timeAxis( ), true, ( axis ) ->
        {
            this.setTimeAxisSelection( timeline.selection( ) );
        } );

        // Add points we already have
        for ( ProjectedPoint p : this.projectedPoints )
        {
            this.timelinePainter.addPoint( p.t, p.x, p.y, p.z );
        }
    }

    protected void setTimeAxisSelection( TimeAxisSelection timeAxisSelection )
    {
        if ( timeAxisSelection != null )
        {
            this.timeAxisSelection = timeAxisSelection;

            float tMin = ( float ) this.timelineEpoch.fromPosixMillis( timeAxisSelection.min_PMILLIS );
            float tMax = ( float ) this.timelineEpoch.fromPosixMillis( timeAxisSelection.max_PMILLIS );

            if ( this.geoPainter != null )
            {
                this.geoPainter.setTWindow( tMin, tMax );
            }

            if ( this.timelinePainter != null )
            {
                this.timelinePainter.setTWindow( tMin, tMax );
            }
        }
    }

    @Override
    public void uninstallFromTimeline( LayeredTimeline timeline, GlimpseContext context, boolean reinstalling )
    {
        timeline.timeAxis( ).removeAxisListener( this.timeAxisListener );
        this.timeAxisListener = null;

        this.timelineRow.removePainter( this.timelinePainter );
        this.timelinePainter.dispose( context );
        this.timelinePainter = null;

        timeline.releaseRow( this.timelineRow.getId( ), reinstalling );
        this.timelineRow = null;
    }

}

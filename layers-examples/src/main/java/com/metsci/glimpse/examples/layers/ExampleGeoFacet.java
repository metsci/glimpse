package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.layers.AxisUtils.addAxisListener2D;
import static com.metsci.glimpse.layers.AxisUtils.addTaggedAxisListener1D;
import static com.metsci.glimpse.layers.geo.GeoExtension.requireGeoExtension;
import static com.metsci.glimpse.layers.time.TimeExtension.requireTimeExtension;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisListener1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layers.geo.GeoExtension;
import com.metsci.glimpse.layers.geo.GeoView;
import com.metsci.glimpse.layers.time.TimeExtension;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

public class ExampleGeoFacet extends ExampleFacet
{

    protected final ExampleLayer layer;

    protected final GeoView view;
    protected final GeoExtension geoExtension;
    protected final TimeExtension timeExtension;

    protected final ExampleGeoPainter painter;

    protected final AxisListener2D geoAxisListener;
    protected final TaggedAxisListener1D timeAxisListener;


    public ExampleGeoFacet( ExampleLayer layer, GeoView view, ExampleStyle style )
    {
        this.layer = layer;

        this.view = view;
        this.geoExtension = requireGeoExtension( this.view );
        this.timeExtension = requireTimeExtension( this.view );

        this.painter = new ExampleGeoPainter( style );
        this.view.dataPainter.addPainter( this.painter );

        Axis2D geoAxis = this.geoExtension.axis;
        this.geoAxisListener = addAxisListener2D( geoAxis, true, ( axis ) ->
        {
            Axis1D xAxis = geoAxis.getAxisX( );
            Axis1D yAxis = geoAxis.getAxisY( );

            float xMin = ( float ) ( xAxis.getSelectionCenter( ) - 0.5*xAxis.getSelectionSize( ) );
            float xMax = ( float ) ( xAxis.getSelectionCenter( ) + 0.5*xAxis.getSelectionSize( ) );
            float yMin = ( float ) ( yAxis.getSelectionCenter( ) - 0.5*yAxis.getSelectionSize( ) );
            float yMax = ( float ) ( yAxis.getSelectionCenter( ) + 0.5*yAxis.getSelectionSize( ) );

            this.painter.setXyWindow( xMin, xMax, yMin, yMax );
        } );

        TaggedAxis1D timeAxis = this.timeExtension.axis;
        this.timeAxisListener = addTaggedAxisListener1D( timeAxis, true, ( axis ) ->
        {
            Epoch epoch = this.timeExtension.epoch;

            float tMin = ( float ) epoch.fromPosixMillis( this.timeExtension.selectionMin_PMILLIS( ) );
            float tMax = ( float ) epoch.fromPosixMillis( this.timeExtension.selectionMax_PMILLIS( ) );

            this.painter.setTWindow( tMin, tMax );
        } );

        this.isVisible.addListener( true, this::refreshVisibility );
        this.layer.isVisible.addListener( true, this::refreshVisibility );
    }

    protected void refreshVisibility( )
    {
        boolean visible = ( this.layer.isVisible.v( ) && this.isVisible.v( ) );
        this.painter.setVisible( visible );
    }

    @Override
    public void addPoint( ExamplePoint point )
    {
        Epoch epoch = this.timeExtension.epoch;
        float t = ( float ) epoch.fromPosixMillis( point.time_PMILLIS );

        GeoProjection geoProj = this.geoExtension.proj;
        Vector2d xy_SU = geoProj.project( point.latlon );
        float x = ( float ) xy_SU.getX( );
        float y = ( float ) xy_SU.getY( );

        float z = ( float ) point.z_SU;

        this.painter.addPoint( t, x, y, z );
    }

    @Override
    public void dispose( boolean reinstalling )
    {
        this.layer.isVisible.removeListener( this::refreshVisibility );
        this.isVisible.removeListener( this::refreshVisibility );

        TaggedAxis1D timeAxis = this.timeExtension.axis;
        timeAxis.removeAxisListener( this.timeAxisListener );

        Axis2D geoAxis = this.geoExtension.axis;
        geoAxis.removeAxisListener( this.geoAxisListener );

        this.view.dataPainter.removePainter( this.painter );
        this.view.canvas.getGLDrawable( ).invoke( true, ( glDrawable ) ->
        {
            GlimpseContext context = this.view.canvas.getGlimpseContext( );
            this.painter.dispose( context );
            return false;
        } );
    }

}

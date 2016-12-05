package com.metsci.glimpse.layers;

import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static com.metsci.glimpse.util.PredicateUtils.require;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

import java.awt.Component;
import java.util.Collection;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.units.Azimuth;
import com.metsci.glimpse.util.vector.Vector2d;

public class LayeredGeo
{

    public final NewtSwingEDTGlimpseCanvas canvas;
    public final Collection<Component> toolbarComponents;

    public final MultiAxisPlot2D plot;
    public final GridPainter gridPainter;
    public final DelegatePainter dataPainter;
    public final CrosshairPainter crosshairPainter;
    public final BorderPainter borderPainter;


    public LayeredGeo( )
    {
        this.plot = new MultiAxisPlot2D( );
        this.plot.getCenterAxis( ).lockAspectRatioXY( 1.0 );
        Axis1D xAxis = this.plot.getCenterAxisX( );
        Axis1D yAxis = this.plot.getCenterAxisY( );
        AxisInfo xAxisInfo = this.plot.createAxisBottom( "xBottom", xAxis, new AxisMouseListener1D( ) );
        AxisInfo yAxisInfo = this.plot.createAxisLeft( "yLeft", yAxis, new AxisMouseListener1D( ) );

        this.gridPainter = new GridPainter( xAxisInfo.getTickHandler( ), yAxisInfo.getTickHandler( ) );
        this.plot.addPainter( this.gridPainter );

        this.dataPainter = new DelegatePainter( );
        this.plot.addPainter( this.dataPainter );

        this.crosshairPainter = new CrosshairPainter( );
        this.plot.addPainter( this.crosshairPainter );

        this.borderPainter = new BorderPainter( );
        this.plot.addPainter( this.borderPainter );

        this.canvas = new NewtSwingEDTGlimpseCanvas( );
        this.canvas.addLayout( this.plot );

        this.toolbarComponents = unmodifiableCollection( asList( ) );
    }

    public void init( LayeredScenario scenario )
    {
        GeoProjection proj = require( scenario.geoProj, notNull );
        LayeredGeoBounds bounds = require( scenario.geoInitBounds, notNull );

        Vector2d west = proj.project( bounds.center.displacedBy( 0.5*bounds.ewExtent_SU, Azimuth.fromNavDeg( -90 ) ) );
        Vector2d east = proj.project( bounds.center.displacedBy( 0.5*bounds.ewExtent_SU, Azimuth.fromNavDeg( +90 ) ) );
        Vector2d north = proj.project( bounds.center.displacedBy( 0.5*bounds.nsExtent_SU, Azimuth.fromNavDeg( 0 ) ) );
        Vector2d south = proj.project( bounds.center.displacedBy( 0.5*bounds.nsExtent_SU, Azimuth.fromNavDeg( 180 ) ) );

        double xMin = min( min( west.getX( ), east.getX( ) ), min( north.getX( ), south.getX( ) ) );
        double xMax = max( max( west.getX( ), east.getX( ) ), max( north.getX( ), south.getX( ) ) );
        double yMin = min( min( west.getY( ), east.getY( ) ), min( north.getY( ), south.getY( ) ) );
        double yMax = max( max( west.getY( ), east.getY( ) ), max( north.getY( ), south.getY( ) ) );

        this.plot.getCenterAxis( ).set( xMin, xMax, yMin, yMax );
    }

}

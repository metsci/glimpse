package com.metsci.glimpse.layers.geo;

import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.layers.geo.GeoTrait.requireGeoTrait;
import static com.metsci.glimpse.layers.misc.UiUtils.addPainter;

import javax.swing.Icon;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layers.GlimpseCanvasView;
import com.metsci.glimpse.layers.misc.CompositeCursorLabelPainter;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.util.var.Disposable;

public class GeoView extends GlimpseCanvasView
{

    protected MultiAxisPlot2D plot;
    protected GridPainter gridPainter;
    protected DelegatePainter dataPainter;
    protected CrosshairPainter crosshairPainter;
    protected CompositeCursorLabelPainter cursorTextPainter;
    protected BorderPainter borderPainter;


    public GeoView( )
    {
        this.title.set( "Geo" );

        this.plot = null;
        this.gridPainter = null;
        this.dataPainter = null;
        this.crosshairPainter = null;
        this.cursorTextPainter = null;
        this.borderPainter = null;
    }

    @Override
    public Icon getIcon( )
    {
        return requireIcon( "fugue-icons/map.png" );
    }

    @Override
    protected void onContextReady( GlimpseContext context )
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

        this.cursorTextPainter = new CompositeCursorLabelPainter( );
        this.cursorTextPainter.setXYLabels( xAxisInfo, yAxisInfo );
        this.plot.addPainter( this.cursorTextPainter );

        this.borderPainter = new BorderPainter( );
        this.plot.addPainter( this.borderPainter );

        this.canvas.addLayout( this.plot );
    }

    @Override
    public void init( )
    {
        GeoTrait geoTrait = requireGeoTrait( this );
        this.plot.getCenterAxis( ).setParent( geoTrait.axis );
    }

    @Override
    protected void onContextDying( GlimpseContext context )
    {
        this.canvas.removeLayout( this.plot );

        this.plot.dispose( context );

        this.plot = null;
        this.gridPainter = null;
        this.dataPainter = null;
        this.crosshairPainter = null;
        this.cursorTextPainter = null;
        this.borderPainter = null;
    }

    @Override
    public GeoView copy( )
    {
        return new GeoView( );
    }

    public Disposable addDataPainter( GlimpsePainter painter )
    {
        return addPainter( this.dataPainter, painter );
    }

}

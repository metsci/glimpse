package com.metsci.glimpse.layers.geo;

import static com.metsci.glimpse.layers.geo.LayeredGeoConfig.requireGeoConfig;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

import java.awt.Component;
import java.util.Collection;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.layers.LayeredView;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;

public class LayeredGeo extends LayeredView
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

    @Override
    public void init( )
    {
        // WIP: Uninstall layers

        LayeredGeoConfig geoConfig = requireGeoConfig( this );
        this.plot.getCenterAxis( ).setParent( geoConfig.axis );

        // WIP: Reinstall appropriate layers
    }

}

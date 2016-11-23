package com.metsci.glimpse.layers;

import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;

import javax.swing.JToolBar;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;

public class LayeredGeo
{

    public final NewtSwingEDTGlimpseCanvas canvas;
    public final JToolBar toolbar;
    public final View view;

    public final MultiAxisPlot2D plot;
    public final GridPainter gridPainter;
    public final DelegatePainter dataPainter;
    public final CrosshairPainter crosshairPainter;
    public final BorderPainter borderPainter;


    public LayeredGeo( )
    {
        this.plot = new MultiAxisPlot2D( );
        Axis1D xAxis = plot.getCenterAxisX( );
        Axis1D yAxis = plot.getCenterAxisY( );
        AxisInfo xAxisInfo = plot.createAxisBottom( "xBottom", xAxis, new AxisMouseListener1D( ) );
        AxisInfo yAxisInfo = plot.createAxisLeft( "yLeft", yAxis, new AxisMouseListener1D( ) );

        this.gridPainter = new GridPainter( xAxisInfo.getTickHandler( ), yAxisInfo.getTickHandler( ) );
        plot.addPainter( gridPainter );

        this.dataPainter = new DelegatePainter( );
        plot.addPainter( dataPainter );

        this.crosshairPainter = new CrosshairPainter( );
        crosshairPainter.showSelectionBox( false );
        plot.addPainter( crosshairPainter );

        this.borderPainter = new BorderPainter( );
        plot.addPainter( borderPainter );

        this.canvas = new NewtSwingEDTGlimpseCanvas( );
        this.canvas.addLayout( this.plot );

        this.toolbar = newToolbar( true );

        this.view = new View( "geoView", this.canvas, "Geo", false, null, requireIcon( "LayeredGeo/fugue-icons/map.png" ), this.toolbar );
    }

}

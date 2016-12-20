package com.metsci.glimpse.layers.geo;

import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.layers.geo.GeoTrait.requireGeoTrait;

import java.awt.Component;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.Icon;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.layers.View;
import com.metsci.glimpse.layers.misc.CompositeCursorLabelPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;

public class GeoView extends View
{

    public final NewtSwingEDTGlimpseCanvas canvas;

    public final MultiAxisPlot2D plot;
    public final GridPainter gridPainter;
    public final DelegatePainter dataPainter;
    public final CrosshairPainter crosshairPainter;
    public final CompositeCursorLabelPainter cursorTextPainter;
    public final BorderPainter borderPainter;


    public GeoView( )
    {
        this.title.set( "Geo" );

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

        this.canvas = new NewtSwingEDTGlimpseCanvas( );
        this.canvas.addLayout( this.plot );
    }

    @Override
    public Icon getIcon( )
    {
        return requireIcon( "fugue-icons/map.png" );
    }

    @Override
    public Component getComponent( )
    {
        return this.canvas;
    }

    @Override
    public GLAutoDrawable getGLDrawable( )
    {
        return this.canvas.getGLDrawable( );
    }

    @Override
    public void init( )
    {
        GeoTrait geoTrait = requireGeoTrait( this );
        this.plot.getCenterAxis( ).setParent( geoTrait.axis );
    }

    @Override
    public GeoView copy( )
    {
        return new GeoView( );
    }

    @Override
    protected void dispose( )
    {
        super.dispose( );
        this.canvas.dispose( );
    }

}

package com.metsci.glimpse.layers.geo;

import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.layers.geo.LayeredGeoConfig.requireGeoConfig;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

import java.awt.Component;
import java.util.Collection;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.Icon;

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

        this.borderPainter = new BorderPainter( );
        this.plot.addPainter( this.borderPainter );

        this.canvas = new NewtSwingEDTGlimpseCanvas( );
        this.canvas.addLayout( this.plot );

        this.toolbarComponents = unmodifiableCollection( asList( ) );
    }

    @Override
    public Icon getIcon( )
    {
        return requireIcon( "LayeredGeo/fugue-icons/map.png" );
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
    public Collection<Component> getToolbarComponents( )
    {
        return this.toolbarComponents;
    }

    @Override
    public void init( )
    {
        LayeredGeoConfig geoConfig = requireGeoConfig( this );
        this.plot.getCenterAxis( ).setParent( geoConfig.axis );
    }

    @Override
    public LayeredGeo createClone( )
    {
        return new LayeredGeo( );
    }

    @Override
    protected void dispose( )
    {
        super.dispose( );
        this.canvas.dispose( );
    }

}

package com.metsci.glimpse.examples.worldwind;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.canvas.SwingGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.examples.charts.bathy.BathymetryExample;
import com.metsci.glimpse.gl.Jogular;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayoutManagerMig;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.MapPlot2D;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.worldwind.event.MouseWrapperWorldwind;
import com.metsci.glimpse.worldwind.projection.PlateCarreeProjection;
import com.metsci.glimpse.worldwind.tile.GlimpseDynamicSurfaceTile;
import com.metsci.glimpse.worldwind.tile.GlimpseResizingSurfaceTile;

public class BaythmetryTileExample
{
    public static void main( String[] args ) throws IOException
    {
        Jogular.initJogl( );

        // create a Worldwind Frame
        JFrame worldwindFrame = new JFrame( "Worldwind" );

        JPanel panel = new JPanel( );
        panel.setLayout( new BorderLayout( ) );
        worldwindFrame.add( panel );

        final WorldWindowGLCanvas wwc = new WorldWindowGLCanvas( );
        wwc.setModel( new BasicModel( ) );

        panel.add( wwc, BorderLayout.CENTER );

        // put any GlimpseLayout here!
        Axis2D wwaxis = new Axis2D( );
        GlimpseAxisLayout2D baseLayout = new GlimpseAxisLayout2D( wwaxis );
        GlimpseLayoutManagerMig manager = new GlimpseLayoutManagerMig( );
        manager.setLayoutConstraints( String.format( "bottomtotop, gapx 0, gapy 0, insets 0 0 0 0" ) );
        baseLayout.setLayoutManager( manager );

        // WorldWind tile images must be project using a PlateCarree projection in
        // order to appear properly geo-registered on the globe
        GeoProjection projection = new PlateCarreeProjection( );

        BathymetryExample example = new BathymetryExample( );
        MapPlot2D plot = example.getLayout( projection );
        Axis2D axis = plot.getAxis( );

        GlimpseAxisLayout2D layout = new GlimpseAxisLayout2D( wwaxis );

        // add a listener to the layout (which will receive mouse events translated from worldwind)
        AxisUtil.attachMouseListener( layout );

        layout.addPainter( example.getBathymetryPainter( ) );
        layout.addPainter( example.getContourPainter( ) );
        layout.addPainter( plot.getCrosshairPainter( ) );

        layout.addPainter( new GlimpseDataPainter2D( )
        {

            @Override
            public void paintTo( GL gl, GlimpseBounds bounds, Axis2D axis )
            {
                //gl.glDisable( GL.GL_LINE_SMOOTH );

                gl.glLineWidth( 3.0f );
                gl.glColor3f( 1.0f, 0.0f, 0.0f );

                gl.glBegin( GL.GL_LINES );
                try
                {
                    gl.glVertex2d( -81.129573, 19.450632 );
                    gl.glVertex2d( -79.911464, 19.598186 );
                }
                finally
                {
                    gl.glEnd( );
                }

                gl.glPointSize( 30.0f );

                gl.glBegin( GL.GL_POINTS );
                try
                {
                    gl.glVertex2d( -81.129573, 19.450632 );
                }
                finally
                {
                    gl.glEnd( );
                }

            }

        } );

        baseLayout.addPainter( new BackgroundPainter( ).setColor( 0, 0, 0, 0 ) );
        baseLayout.addLayout( layout );

        LatLonGeo corner1 = projection.unproject( axis.getAxisX( ).getMin( ), axis.getAxisY( ).getMin( ) );
        LatLonGeo corner2 = projection.unproject( axis.getAxisX( ).getMin( ), axis.getAxisY( ).getMax( ) );
        LatLonGeo corner3 = projection.unproject( axis.getAxisX( ).getMax( ), axis.getAxisY( ).getMax( ) );
        LatLonGeo corner4 = projection.unproject( axis.getAxisX( ).getMax( ), axis.getAxisY( ).getMin( ) );

        List<LatLon> corners = new ArrayList<LatLon>( );
        corners.add( LatLon.fromDegrees( corner1.getLatDeg( ), corner1.getLonDeg( ) ) );
        corners.add( LatLon.fromDegrees( corner2.getLatDeg( ), corner2.getLonDeg( ) ) );
        corners.add( LatLon.fromDegrees( corner3.getLatDeg( ), corner3.getLonDeg( ) ) );
        corners.add( LatLon.fromDegrees( corner4.getLatDeg( ), corner4.getLonDeg( ) ) );

        GlimpseDynamicSurfaceTile glimpseLayer = new GlimpseResizingSurfaceTile( baseLayout, wwaxis, projection, 3000, 3000, corners );
        ApplicationTemplate.insertBeforePlacenames( wwc, glimpseLayer );

        // Create and install the view controls layer and register a controller for it with the World Window.
        ViewControlsLayer viewControlsLayer = new ViewControlsLayer( );
        ApplicationTemplate.insertBeforeCompass( wwc, viewControlsLayer );
        wwc.addSelectListener( new ViewControlsSelectListener( wwc, viewControlsLayer ) );

        worldwindFrame.setSize( 800, 800 );
        worldwindFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        worldwindFrame.setVisible( true );

        // create a Glimpse Frame
        SwingGlimpseCanvas glimpseCanvas = new SwingGlimpseCanvas( true, wwc.getContext( ) );
        glimpseCanvas.addLayout( plot );
        glimpseCanvas.setLookAndFeel( new SwingLookAndFeel( ) );

        JFrame glimpseFrame = new JFrame( "Glimpse" );
        glimpseFrame.add( glimpseCanvas );

        RepaintManager.newRepaintManager( glimpseCanvas );

        glimpseFrame.pack( );
        glimpseFrame.setSize( 800, 800 );
        glimpseFrame.setLocation( 800, 0 );
        glimpseFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        glimpseFrame.setVisible( true );

        // pass mouse events from Worldwind back to Glimpse
        MouseWrapperWorldwind mouseWrapper = new MouseWrapperWorldwind( wwc, projection, glimpseLayer );
        wwc.addMouseListener( mouseWrapper );
        wwc.addMouseMotionListener( mouseWrapper );
        wwc.addMouseWheelListener( mouseWrapper );

        // add a thread to constantly repaint the WorldWind window
        // this isn't an ideal solution, but because Glimpse currently
        // uses busy repainting this is the easiest way to make the integration work
        ( new Thread( )
        {
            public void run( )
            {
                while ( true )
                {
                    wwc.redraw( );
                    try
                    {
                        Thread.sleep( ( long ) ( 1000.0 / 60.0 ) );
                    }
                    catch ( InterruptedException e )
                    {
                    }
                }
            }
        } ).start( );
    }
}

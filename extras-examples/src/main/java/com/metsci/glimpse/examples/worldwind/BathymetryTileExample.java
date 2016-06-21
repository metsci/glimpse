/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.examples.worldwind;

import static com.metsci.glimpse.worldwind.util.WorldWindGlimpseUtils.linkMouseEvents;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisUtil;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.examples.charts.bathy.BathymetryExample;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayoutManagerMig;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.MapPlot2D;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.worldwind.projection.PlateCarreeProjection;
import com.metsci.glimpse.worldwind.tile.GlimpseDynamicSurfaceTile;
import com.metsci.glimpse.worldwind.tile.GlimpseReprojectingSurfaceTile;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

public class BathymetryTileExample
{
    public static void main( String[] args ) throws IOException
    {
        // create a Worldwind Frame
        JFrame worldwindFrame = new JFrame( "Worldwind" );

        JPanel panel = new JPanel( );
        panel.setLayout( new BorderLayout( ) );
        worldwindFrame.add( panel );

        final WorldWindowGLCanvas wwc = new WorldWindowGLCanvas( );
        BasicModel model = new BasicModel( );

        // uncomment this line to use a flat globe
        //model.setGlobe( new EllipsoidalGlobe( Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS, Earth.WGS84_ES, new ZeroElevationModel( ) ) );

        wwc.setModel( model );

        panel.add( wwc, BorderLayout.CENTER );

        // put any GlimpseLayout here!
        Axis2D wwaxis = new Axis2D( );
        GlimpseAxisLayout2D baseLayout = new GlimpseAxisLayout2D( wwaxis );
        GlimpseLayoutManagerMig manager = new GlimpseLayoutManagerMig( );
        manager.setLayoutConstraints( String.format( "bottomtotop, gapx 0, gapy 0, insets 0 0 0 0" ) );
        baseLayout.setLayoutManager( manager );

        // WorldWind tile images must be projected using a PlateCarree projection in
        // order to appear properly geo-registered on the globe
        GeoProjection projectionTo = new PlateCarreeProjection( );

        // however, we can use another GeoProjection to render our graphics,
        // and use GlimpseReprojectingSurfaceTile to automatically fix the projection
        TangentPlane projection = new TangentPlane( LatLonGeo.fromDeg( -30.637005, 65.476074 ) );
        //TangentPlane projection = new TangentPlane( LatLonGeo.fromDeg( 24, -76 ) );

        BathymetryExample example = new BathymetryExample( );
        MapPlot2D plot = example.getLayout( projection );
        Axis2D axis = plot.getAxis( );

        GlimpseAxisLayout2D layout = new GlimpseAxisLayout2D( wwaxis );

        // add a listener to the layout (which will receive mouse events translated from worldwind)
        AxisUtil.attachMouseListener( layout );

        layout.addPainter( example.getBathymetryPainter( ) );
        layout.addPainter( example.getContourPainter( ) );

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

        GlimpseDynamicSurfaceTile glimpseLayer = new GlimpseReprojectingSurfaceTile( baseLayout, wwaxis, projection, projectionTo, 4500, 4500, 3000, 3000, corners );

        ApplicationTemplate.insertBeforePlacenames( wwc, glimpseLayer );

        // Create and install the view controls layer and register a controller for it with the World Window.
        ViewControlsLayer viewControlsLayer = new ViewControlsLayer( );
        ApplicationTemplate.insertBeforeCompass( wwc, viewControlsLayer );
        wwc.addSelectListener( new ViewControlsSelectListener( wwc, viewControlsLayer ) );

        worldwindFrame.setSize( 800, 800 );
        worldwindFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        worldwindFrame.setVisible( true );
        
        // create a Glimpse Frame
        NewtSwingGlimpseCanvas glimpseCanvas = new NewtSwingGlimpseCanvas( wwc.getContext( ) );
        glimpseCanvas.addLayout( plot );
        glimpseCanvas.setLookAndFeel( new SwingLookAndFeel( ) );

        JFrame glimpseFrame = new JFrame( "Glimpse" );
        glimpseFrame.add( glimpseCanvas );

        // attach a repaint manager which repaints the canvas in a loop
        new FPSAnimator( glimpseCanvas.getGLDrawable( ), 120 ).start( );

        glimpseFrame.pack( );
        glimpseFrame.setSize( 800, 800 );
        glimpseFrame.setLocation( 800, 0 );
        glimpseFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        glimpseFrame.setVisible( true );

        // pass mouse events from WorldWind back to Glimpse
        //linkMouseEvents( wwc, projection, glimpseLayer );

        // force the WorldWind and Glimpse windows to pan together
        //linkAxisToWorldWind( wwc, projection, plot.getAxis( ) );
        //linkWorldWindToAxis( wwc, projection, plot.getAxis( ) );

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

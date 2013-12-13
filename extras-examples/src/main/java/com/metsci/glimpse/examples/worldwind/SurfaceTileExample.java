/*
 * Copyright (c) 2012, Metron, Inc.
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

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.examples.basic.HeatMapExample;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.worldwind.tile.GlimpseStaticSurfaceTile;

/**
 * Demonstrates rendering the contents of a Glimpse plot onto
 * the surface of a WorldWind globe.
 * 
 * @author ulman
 */
public class SurfaceTileExample
{
    public static final java.util.List<LatLon> CORNERS = Arrays.asList( LatLon.fromDegrees( 34, -112 ), LatLon.fromDegrees( 34, -104 ), LatLon.fromDegrees( 44, -104 ), LatLon.fromDegrees( 44, -112 ) );

    public static void main( String[] args ) throws IOException
    {
        // create a Worldwind Frame
        JFrame worldwindFrame = new JFrame( "Worldwind" );

        JPanel panel = new JPanel( );
        panel.setLayout( new BorderLayout( ) );
        worldwindFrame.add( panel );

        final WorldWindowGLCanvas wwc = new WorldWindowGLCanvas( );
        wwc.setModel( new BasicModel( ) );

        panel.add( wwc, BorderLayout.CENTER );

        // put any GlimpseLayout here!
        ColorAxisPlot2D layout = new HeatMapExample( ).getLayout( );

        GlimpseStaticSurfaceTile glimpseLayer = new GlimpseStaticSurfaceTile( layout, 1000, 1000, CORNERS );
        ApplicationTemplate.insertBeforePlacenames( wwc, glimpseLayer );

        // create and install the view controls layer and register a controller for it with the World Window.
        ViewControlsLayer viewControlsLayer = new ViewControlsLayer( );
        ApplicationTemplate.insertBeforeCompass( wwc, viewControlsLayer );
        wwc.addSelectListener( new ViewControlsSelectListener( wwc, viewControlsLayer ) );

        worldwindFrame.setSize( 800, 800 );
        worldwindFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        worldwindFrame.setVisible( true );

        // create a Glimpse Frame
        NewtSwingGlimpseCanvas glimpseCanvas = new NewtSwingGlimpseCanvas( wwc.getContext( ) );
        glimpseCanvas.addLayout( layout );
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

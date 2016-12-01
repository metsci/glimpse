package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.platformFixes.PlatformFixes.fixPlatformQuirks;
import static com.metsci.glimpse.tinylaf.TinyLafUtils.initTinyLaf;
import static com.metsci.glimpse.util.logging.LoggerUtils.initializeLogging;

import java.util.Random;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.layers.LayeredGui;
import com.metsci.glimpse.layers.LayeredScenario;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class LayeredExample
{

    public static void main( String[] args )
    {
        SwingUtilities.invokeLater( ( ) ->
        {

            // Typical preamble for Swing/Glimpse
            initializeLogging( "LayeredExample/logging.properties" );
            fixPlatformQuirks( );
            initTinyLaf( );

            // Set up our scenario
            LayeredScenario.Builder scenario = new LayeredScenario.Builder( );
            scenario.geoProj = new TangentPlane( LatLonGeo.fromDeg( 30.0, -75.0 ) );
            scenario.timelineEpoch = new Epoch( TimeStamp.fromString( "2016-01-01T00:00:00Z" ) );

            // Create an empty layered gui
            LayeredGui gui = new LayeredGui( "Layered Example" );
            gui.arrange( "LayeredExample", "LayeredExample/docking-defaults.xml" );
            gui.setScenario( scenario.build( ) );

            // Add a layer to the gui
            ExampleLayer exampleLayer = new ExampleLayer( );
            gui.addLayer( exampleLayer );

            // Add some data to the layer
            long t0_PMILLIS = scenario.timelineEpoch.getPosixMillis( );
            Random r = new Random( 0 );
            for ( int i = 0; i < 1000; i++ )
            {
                long t_PMILLIS = t0_PMILLIS + i*1000;
                float x_SU = -10 + 0.02f*i + 4*r.nextFloat( );
                float y_SU = -10 + 20*r.nextFloat( );
                float z_SU = -10 + 20*r.nextFloat( );
                exampleLayer.addPoint( t_PMILLIS, x_SU, y_SU, z_SU );
            }

        } );
    }

}

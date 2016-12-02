package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.platformFixes.PlatformFixes.fixPlatformQuirks;
import static com.metsci.glimpse.tinylaf.TinyLafUtils.initTinyLaf;
import static com.metsci.glimpse.util.logging.LoggerUtils.initializeLogging;
import static java.lang.Math.PI;
import static java.lang.Math.sin;

import java.util.Random;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.layers.LayeredGeoBounds;
import com.metsci.glimpse.layers.LayeredGui;
import com.metsci.glimpse.layers.LayeredScenario;
import com.metsci.glimpse.layers.LayeredTimelineBounds;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.units.Length;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class LayeredExample
{

    public static void main( String[] args )
    {
        SwingUtilities.invokeLater( ( ) ->
        {

            // Typical preamble for Swing/Glimpse
            //

            initializeLogging( "LayeredExample/logging.properties" );
            fixPlatformQuirks( );
            initTinyLaf( );


            // Set up our scenario
            //

            LayeredScenario.Builder scenario = new LayeredScenario.Builder( );

            scenario.geoProj = new TangentPlane( LatLonGeo.fromDeg( 30.0, -75.0 ) );
            scenario.geoInitBounds = new LayeredGeoBounds( LatLonGeo.fromDeg( 30.0, -75.0 ), Length.fromNauticalMiles( 25.0 ), Length.fromNauticalMiles( 25.0 ) );

            // WIP: Specify axis display units

            scenario.timelineEpoch = new Epoch( TimeStamp.fromString( "2016-01-01T00:00:00Z" ) );
            scenario.timelineInitBounds = new LayeredTimelineBounds( TimeStamp.fromString( "2015-12-31T23:55:00Z" ), TimeStamp.fromString( "2016-01-01T01:05:00Z" ) );

            // WIP: Specify timezone


            // Create the gui
            //

            LayeredGui gui = new LayeredGui( "Layered Example" );
            gui.arrange( "LayeredExample", "LayeredExample/docking-defaults.xml" );
            gui.init( scenario.build( ) );

            // WIP: Need to stop the animator if init() fails, so the Swing thread can terminate


            // Add a layer
            //

            ExampleLayer exampleLayer = new ExampleLayer( );
            gui.addLayer( exampleLayer );

            long t0_PMILLIS = scenario.timelineEpoch.getPosixMillis( );
            Random r = new Random( 0 );
            for ( int i = 0; i < 3600; i++ )
            {
                long t_PMILLIS = t0_PMILLIS + i*1000;
                float x_SU = ( float ) Length.fromNauticalMiles( -11 + 0.005f*i + 4*r.nextFloat( ) );
                float y_SU = ( float ) Length.fromNauticalMiles( -10 + 20*r.nextFloat( ) );
                float z_SU = ( float ) ( 10*sin( 0.001*i * 5*PI ) + 8*r.nextFloat( ) );
                exampleLayer.addPoint( t_PMILLIS, x_SU, y_SU, z_SU );
            }

        } );
    }

}

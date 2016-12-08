package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.layers.geo.LayeredGeoConfig.setDefaultGeoConfigurator;
import static com.metsci.glimpse.layers.timeline.LayeredTimelineConfig.setDefaultTimelineConfigurator;
import static com.metsci.glimpse.platformFixes.PlatformFixes.fixPlatformQuirks;
import static com.metsci.glimpse.tinylaf.TinyLafUtils.initTinyLaf;
import static com.metsci.glimpse.util.logging.LoggerUtils.initializeLogging;
import static com.metsci.glimpse.util.units.Angle.normalizeAngle360;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.layers.LayeredGui;
import com.metsci.glimpse.layers.geo.LayeredGeo;
import com.metsci.glimpse.layers.geo.LayeredGeoConfig;
import com.metsci.glimpse.layers.timeline.LayeredTimeline;
import com.metsci.glimpse.layers.timeline.LayeredTimelineConfig;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.units.Azimuth;
import com.metsci.glimpse.util.units.Length;
import com.metsci.glimpse.util.units.Speed;
import com.metsci.glimpse.util.units.time.Time;
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


            // Set up view-config defaults
            //

            Supplier<LayeredGeoConfig> geoConfigurator = ( ) ->
            {
                GeoProjection proj = new TangentPlane( LatLonGeo.fromDeg( 30.0, -75.0 ) );

                LayeredGeoConfig config = new LayeredGeoConfig( proj );

                config.setProjectedBounds( Length::fromNauticalMiles, -10, +10, -10, +10 );
                // WIP: Initialize selection box
                // WIP: Specify axis units for display

                return config;
            };

            Supplier<LayeredTimelineConfig> timelineConfigurator = ( ) ->
            {
                Epoch epoch = new Epoch( TimeStamp.fromString( "2016-01-01T00:00:00Z" ) );

                LayeredTimelineConfig config = new LayeredTimelineConfig( epoch );

                config.setRelativeBounds( Time::minutesToSeconds, -5, +65 );
                config.setRelativeSelection( Time::minutesToSeconds, 0, +10 );
                // WIP: Specify timezone for display

                return config;
            };


            // Create some layers
            //

            ExampleLayer exampleLayerA = new ExampleLayer( "Truth", GlimpseColor.getRed( ) );
            ExampleLayer exampleLayerB = new ExampleLayer( "Observed", GlimpseColor.getBlack( ) );

            long time_PMILLIS = TimeStamp.fromString( "2016-01-01T00:00:00Z" ).toPosixMillis( );

            LatLonGeo latlonA = LatLonGeo.fromDeg( 30.0, -75.0 );
            double speedA_SU = Speed.fromKnots( 5.0 );
            double courseA_NAVDEG = 45.0;

            double zA_SU = 0;

            Random r = new Random( 0 );
            for ( int i = 0; i < 3600; i++ )
            {
                long timeStep_MILLIS = 1000;

                time_PMILLIS += timeStep_MILLIS;

                if ( r.nextDouble( ) < 0.1 )
                {
                    speedA_SU = Speed.fromKnots( 4.0 + 2.0*r.nextDouble( ) );
                    courseA_NAVDEG = normalizeAngle360( courseA_NAVDEG - 30.0 + 60.0*r.nextDouble( ) );
                }

                if ( r.nextDouble( ) < 0.003 )
                {
                    zA_SU = 100.0 * r.nextDouble( );
                }

                double distanceA_SU = speedA_SU * Time.fromMilliseconds( timeStep_MILLIS );
                double courseA_SU = Azimuth.fromNavDeg( courseA_NAVDEG );
                latlonA = latlonA.displacedBy( distanceA_SU, courseA_SU );

                double errorDistance_SU = Length.fromNauticalMiles( 0.03*r.nextDouble( ) );
                double errorDirection_SU = Azimuth.fromNavDeg( 360.0 * r.nextDouble( ) );
                LatLonGeo latlonB = latlonA.displacedBy( errorDistance_SU, errorDirection_SU );
                double zB_SU = zA_SU - 5.0 + 10.0*r.nextDouble( );

                exampleLayerA.addPoint( new ExamplePoint( time_PMILLIS, latlonA, zA_SU ) );
                exampleLayerB.addPoint( new ExamplePoint( time_PMILLIS, latlonB, zB_SU ) );
            }


            // Create the gui
            //

            LayeredGui gui = new LayeredGui( "Layered Example" );
            gui.arrange( "LayeredExample", "LayeredExample/docking-defaults.xml" );

            setDefaultGeoConfigurator( gui, geoConfigurator );
            setDefaultTimelineConfigurator( gui, timelineConfigurator );

            LayeredGeo geo = new LayeredGeo( );
            gui.addView( geo );

            LayeredTimeline timeline = new LayeredTimeline( );
            gui.addView( timeline );

            gui.addLayer( exampleLayerA );
            gui.addLayer( exampleLayerB );



            ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor( );
            exec.schedule( ( ) ->
            {
                SwingUtilities.invokeLater( ( ) ->
                {

                    // WIP
                    modifyGeoConfig( geo, ( geoConfig ) ->
                    {


                        return newGeoConfig;
                    } );

                    //LayeredGeoConfig.setGeoConfig( geo, geoConfig );
                    //geo.init( );


                } );
            }, 5, TimeUnit.SECONDS );




        } );
    }

}

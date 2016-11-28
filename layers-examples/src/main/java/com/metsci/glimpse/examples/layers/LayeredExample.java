package com.metsci.glimpse.examples.layers;

import java.util.Random;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.layers.LayeredGui;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class LayeredExample
{

    public static void main( String[] args )
    {
        SwingUtilities.invokeLater( ( ) ->
        {

            LayeredGui gui = new LayeredGui( "Layered Example" );
            gui.arrange( LayeredExample.class, "LayeredExample/docking-defaults.xml" );

            ExampleLayer exampleLayer = new ExampleLayer( );
            gui.addLayer( exampleLayer );

            // Add some random data
            long t0_PMILLIS = TimeStamp.fromString( "2016-01-01T00:00:00Z" ).toPosixMillis( );
            Random r = new Random( 0 );
            for ( int i = 0; i < 1000; i++ )
            {
                long t_PMILLIS = t0_PMILLIS + i*1000;
                float x_SU = -10 + 20*r.nextFloat( );
                float y_SU = -10 + 20*r.nextFloat( );
                float z_SU = -10 + 20*r.nextFloat( );
                exampleLayer.addPoint( t_PMILLIS, x_SU, y_SU, z_SU );
            }

        } );
    }

}

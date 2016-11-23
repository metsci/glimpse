package com.metsci.glimpse.examples.layers;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.layers.LayeredGui;

public class LayeredExample
{

    public static void main( String[] args )
    {
        SwingUtilities.invokeLater( ( ) ->
        {
            LayeredGui gui = new LayeredGui( "LayeredExample" );
            gui.arrange( LayeredExample.class, "LayeredExample/docking-defaults.xml" );

            ExampleLayer exampleLayer = new ExampleLayer( );
            gui.addLayer( exampleLayer );
        } );
    }

}

package com.metsci.glimpse.examples.webstart;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class WebstartMain
{
    public static void main( String[] args )
    {
        try
        {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName( ) );
        }
        catch ( Exception e )
        {
            // ignore
        }

        JFrame frame = new JFrame( "Glimpse Examples" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setPreferredSize( new Dimension( 1280, 800 ) );

        ExampleRunner runnerPanel = new ExampleRunner( );
        frame.setContentPane( runnerPanel );

        frame.pack( );
        frame.setLocationRelativeTo( null );
        frame.setVisible( true );

        runnerPanel.populateExamples( );
    }
}

package com.metsci.glimpse.examples.webstart;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

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

        final JFrame runnerFrame = new JFrame( "Glimpse Examples" );
        runnerFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        runnerFrame.setPreferredSize( new Dimension( 1280, 800 ) );

        ExampleRunner runnerPanel = new ExampleRunner( );
        runnerFrame.setContentPane( runnerPanel );

        runnerFrame.pack( );
        runnerFrame.setLocationRelativeTo( null );
        runnerFrame.setVisible( true );

        runnerPanel.populateExamples( );

        // force all child windows to be dispose-on-close so we don't interrupt our example runner
        Toolkit.getDefaultToolkit( ).addAWTEventListener( new AWTEventListener( )
        {
            @Override
            public void eventDispatched( AWTEvent event )
            {
                if ( event instanceof WindowEvent )
                {
                    Window window = ( ( WindowEvent ) event ).getWindow( );
                    if ( window != runnerFrame )
                    {
                        if ( window instanceof JFrame )
                        {
                            JFrame frame = ( JFrame ) window;
                            if ( frame.getDefaultCloseOperation( ) == JFrame.EXIT_ON_CLOSE )
                            {
                                frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
                            }
                        }
                        else if ( window instanceof JDialog )
                        {
                            JDialog dialog = ( JDialog ) window;
                            if ( dialog.getDefaultCloseOperation( ) == JFrame.EXIT_ON_CLOSE )
                            {
                                dialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
                            }
                        }
                    }
                }
            }
        }, AWTEvent.WINDOW_EVENT_MASK );
    }
}

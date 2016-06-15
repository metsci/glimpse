package com.metsci.glimpse.examples.dnc;

import static javax.swing.BorderFactory.createEmptyBorder;

import java.awt.Font;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;

public class ProgressPane extends JPanel
{

    protected final JLabel descLabel;
    protected final JProgressBar progressBar;
    protected final JButton cancelButton;
    protected final JLabel progressLabel;

    protected final CopyOnWriteArrayList<Runnable> cancelListeners;


    public ProgressPane( String desc )
    {
        this.descLabel = new JLabel( desc );
        this.progressBar = new JProgressBar( 0, 1000 );
        this.cancelButton = new JButton( "Cancel" );
        this.progressLabel = new JLabel( );

        setLayout( new MigLayout( ) );
        add( descLabel, "wrap" );
        add( progressBar, "growx" );
        add( cancelButton, "wrap" );
        add( progressLabel, "wrap" );

        // Appearance tweaks
        descLabel.setFont( adjustFontSize( descLabel.getFont( ), +1 ) );
        descLabel.setBorder( createEmptyBorder( 7, 0, 0, 25 ) );
        progressLabel.setBorder( createEmptyBorder( 0, 0, 15, 0 ) );

        this.cancelListeners = new CopyOnWriteArrayList<>( );
        cancelButton.addActionListener( ( ev ) ->
        {
            for ( Runnable listener : cancelListeners )
            {
                listener.run( );
            }
        } );
    }

    public static Font adjustFontSize( Font font, float sizeDelta )
    {
        return font.deriveFont( font.getSize2D( ) + sizeDelta );
    }

    public static String getPercentString( double fractionComplete )
    {
        return ( ( ( int ) ( 100 * fractionComplete ) ) + "%" );
    }

    public void setProgress( double fractionComplete )
    {
        progressBar.setValue( ( int ) ( 1000 * fractionComplete ) );
        progressLabel.setText( getPercentString( fractionComplete ) + " complete" );
    }

    public void addCancelListener( Runnable listener )
    {
        cancelListeners.add( listener );
    }

}

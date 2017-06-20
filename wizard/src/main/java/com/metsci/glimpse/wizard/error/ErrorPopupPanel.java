package com.metsci.glimpse.wizard.error;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.metsci.glimpse.wizard.Wizard;
import com.metsci.glimpse.wizard.WizardError;

public class ErrorPopupPanel<K> extends JFrame
{
    private static final long serialVersionUID = 1L;

    protected ErrorTablePanel<K> panel;
    protected boolean isOpen;

    protected AWTEventListener listener;

    protected Collection<Component> ignore;

    public ErrorPopupPanel( Wizard<K> controller )
    {
        this( controller, Collections.emptyList( ) );
    }

    public ErrorPopupPanel( Wizard<K> controller, Collection<Component> ignore )
    {
        this.ignore = ignore;

        this.setUndecorated( true );
        this.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
        this.setAlwaysOnTop( true );

        this.listener = new AWTEventListener( )
        {
            public void eventDispatched( AWTEvent event )
            {
                // There are unintuitive interactions with the automatic closing of the frame
                // and the component which is clicked to show the frame. The easiest solution
                // (although probably not the best/cleanest) is to simply ignore events from
                // those control components.
                if ( !ignore.contains( event.getSource( ) ) && ( event.getID( ) == MouseEvent.MOUSE_PRESSED || event.getID( ) == FocusEvent.FOCUS_LOST ) )
                {
                    ErrorPopupPanel.this.hideErrorPopup( );
                }
            }
        };

        this.panel = new ErrorTablePanel<K>( controller, false );
        this.panel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( Color.black ), BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ) );
        this.setBackground( Color.white );
        this.setOpacity( 1.0f );
        this.add( this.panel );
    }

    public void showErrorPopup( Component anchor, Dimension size, Collection<WizardError> errors )
    {
        this.panel.setErrors( errors );

        Dimension anchorSize = anchor.getSize( );
        Point point = new Point( 0, ( int ) anchorSize.getHeight( ) );
        SwingUtilities.convertPointToScreen( point, anchor );

        this.setLocation( point );
        this.setSize( size );
        this.setVisible( true );

        this.isOpen = true;

        Toolkit.getDefaultToolkit( ).addAWTEventListener( this.listener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK );
    }

    public void hideErrorPopup( )
    {
        Toolkit.getDefaultToolkit( ).removeAWTEventListener( this.listener );

        this.setVisible( false );

        this.isOpen = false;
    }

    public boolean isOpen( )
    {
        return this.isOpen;
    }
}

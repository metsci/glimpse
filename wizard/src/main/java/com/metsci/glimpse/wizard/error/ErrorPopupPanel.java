/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.wizard.error;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.metsci.glimpse.wizard.Wizard;
import com.metsci.glimpse.wizard.WizardError;

public class ErrorPopupPanel<K> extends JDialog
{
    private static final long serialVersionUID = 1L;

    protected ErrorTablePanel<K> panel;
    protected AWTEventListener listener;
    protected Collection<Component> ignore;

    public ErrorPopupPanel( Window parent, Wizard<K> controller, Collection<Component> ignore )
    {
        super( parent );

        this.ignore = ignore;

        this.setUndecorated( true );
        this.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );

        this.listener = new AWTEventListener( )
        {
            @Override
            public void eventDispatched( AWTEvent event )
            {
                // There are unintuitive interactions with the automatic closing of the frame
                // and the component which is clicked to show the frame. The easiest solution
                // (although probably not the best/cleanest) is to simply ignore events from
                // those control components.
                if ( ignore.contains( event.getSource( ) ) )
                {
                    return;
                }

                // If focus is gained and the new focus owner is not within the ErrorPopupPanel, hide the dialog
                if ( event.getID( ) == FocusEvent.FOCUS_GAINED &&
                        !SwingUtilities.isDescendingFrom( ( Component ) event.getSource( ), ErrorPopupPanel.this ) )
                {
                    hideErrorPopup( );
                }

                // If a mouse press happens outside of the ErrorPopupPanel, hide the dialog
                if ( event.getID( ) == MouseEvent.MOUSE_PRESSED &&
                        !SwingUtilities.isDescendingFrom( ( Component ) event.getSource( ), ErrorPopupPanel.this ) )
                {
                    hideErrorPopup( );
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

        Toolkit.getDefaultToolkit( ).addAWTEventListener( this.listener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK );
    }

    public void hideErrorPopup( )
    {
        Toolkit.getDefaultToolkit( ).removeAWTEventListener( this.listener );
        this.setVisible( false );
        this.dispose( );
    }
}

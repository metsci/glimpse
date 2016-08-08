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
package com.metsci.glimpse.examples.dnc;

import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static java.lang.Boolean.FALSE;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public class DncExampleUtils
{
    private static final Logger logger = getLogger( DncExampleUtils.class );


    public static void initTinyLaf( )
    {
        try
        {
            net.sf.tinylaf.Theme.loadTheme( DncExampleUtils.class.getClassLoader( ).getResource( "tinylaf/radiance.theme" ) );
            UIManager.setLookAndFeel( new net.sf.tinylaf.TinyLookAndFeel( ) );

            // TinyLaf uses text-area foreground color for option-pane foreground, which doesn't look right
            Color fgColor = UIManager.getColor( "Label.foreground" );
            if ( fgColor != null )
            {
                UIManager.put( "OptionPane.messageForeground", fgColor );
            }

            // TinyLaf disables the "new folder" button in some cases ... not sure why
            UIManager.put( "FileChooser.readOnly", FALSE );
        }
        catch ( UnsupportedLookAndFeelException e )
        {
            logWarning( logger, "Failed to init Tiny L&F", e );
        }
    }


    public static void setTreeEnabled( Component root, boolean enabled )
    {
        root.setEnabled( enabled );
        if ( root instanceof Container )
        {
            for ( Component c : ( ( Container ) root ).getComponents( ) )
            {
                setTreeEnabled( c, enabled );
            }
        }
    }


    public static JLabel newLabel( String text, int fontStyle )
    {
        JLabel label = new JLabel( text );
        label.setFont( label.getFont( ).deriveFont( fontStyle ) );
        return label;
    }


    public static void addTextListener( JTextComponent c, Runnable listener )
    {
        DocumentListener docListener = new DocumentListener( )
        {
            public void insertUpdate( DocumentEvent ev ) { listener.run( ); }
            public void removeUpdate( DocumentEvent ev ) { listener.run( ); }
            public void changedUpdate( DocumentEvent ev ) { listener.run( ); }
        };

        c.addPropertyChangeListener( "document", ( ev ) ->
        {
            Document oldDoc = ( Document ) ev.getOldValue( );
            if ( oldDoc != null )
            {
                oldDoc.removeDocumentListener( docListener );
            }

            Document newDoc = ( Document ) ev.getNewValue( );
            if ( newDoc != null )
            {
                newDoc.addDocumentListener( docListener );
            }

            listener.run( );
        } );

        Document doc = c.getDocument( );
        if ( doc != null )
        {
            doc.addDocumentListener( docListener );
        }
    }

}

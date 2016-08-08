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

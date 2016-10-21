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
package com.metsci.glimpse.examples.misc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.examples.layout.SimpleLayoutExample;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;

/**
 * A Glimpse plot with a Swing JPopupMenu which appears when right clicking on the plot.
 *
 * @author ulman
 */
public class PopupMenuExample
{
    private static final Logger logger = Logger.getLogger( PopupMenuExample.class.getName( ) );

    public static void main( String[] args ) throws Exception
    {
        final NewtSwingGlimpseCanvas canvas = new NewtSwingGlimpseCanvas( );
        GlimpseLayout plot = buildPlot( canvas );
        canvas.addLayout( plot );
        canvas.setLookAndFeel( new SwingLookAndFeel( ) );

        // attach a repaint manager which repaints the canvas in a loop
        new FPSAnimator( canvas.getGLDrawable( ), 120 ).start( );

        final JFrame frame = new JFrame( "Glimpse Example (Swing)" );

        frame.addWindowListener( new WindowAdapter( )
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                canvas.disposeAttached( );
            }
        } );

        frame.add( canvas );

        frame.pack( );
        frame.setSize( 800, 800 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );

        return;
    }

    protected static GlimpseLayout buildPlot( final NewtSwingGlimpseCanvas canvas ) throws Exception
    {
        GlimpseLayout layout = new SimpleLayoutExample( ).getLayout( );

        final JPopupMenu _popupMenu = createPopupMenu( );

        canvas.getGLWindow( ).addMouseListener( new MouseAdapter( )
        {
            @Override
            public void mouseClicked( MouseEvent event )
            {
                if ( event.getButton( ) == MouseEvent.BUTTON3 )
                {
                    _popupMenu.show( canvas, event.getX( ), event.getY( ) );
                }
            }
        } );

        return layout;
    }

    private static JPopupMenu createPopupMenu( )
    {
        JPopupMenu popupMenu = new JPopupMenu( );

        final JRadioButtonMenuItem item1 = new JRadioButtonMenuItem( "Item1", false );
        item1.addActionListener( new ActionListener( )
        {
            @Override
            public void actionPerformed( ActionEvent actionEvent )
            {
                logger.info( "clicked item1" );
            }
        } );

        final JRadioButtonMenuItem item2 = new JRadioButtonMenuItem( "Item2", false );
        item2.addActionListener( new ActionListener( )
        {
            @Override
            public void actionPerformed( ActionEvent actionEvent )
            {
                logger.info( "clicked item2" );
            }
        } );

        popupMenu.add( item1 );
        popupMenu.add( item2 );

        return popupMenu;
    }

}

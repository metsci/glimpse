/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.examples.layout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import com.metsci.glimpse.canvas.SwingGlimpseCanvas;
import com.metsci.glimpse.gl.Jogular;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.support.repaint.SwingRepaintManager;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;

/**
 * @author ulman
 */
public class SimpleLayoutExampleWithPopup
{
    private static final Logger logger = Logger.getLogger( SimpleLayoutExampleWithPopup.class.getName( ) );

    public static void main( String[] args ) throws Exception
    {
        Jogular.initJogl( );

        final SwingGlimpseCanvas canvas = new SwingGlimpseCanvas( true );
        GlimpseLayout plot = buildPlot( canvas );
        canvas.addLayout( plot );
        canvas.setLookAndFeel( new SwingLookAndFeel( ) );

        final RepaintManager manager = SwingRepaintManager.newRepaintManager( canvas );

        JFrame frame = new JFrame( "Glimpse Example (Swing)" );
        frame.add( canvas );

        frame.pack( );
        frame.setSize( 800, 800 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );

        Runtime.getRuntime( ).addShutdownHook( new Thread( )
        {
            @Override
            public void run( )
            {
                canvas.dispose( manager );
            }
        } );

        return;
    }

    protected static GlimpseLayout buildPlot( final SwingGlimpseCanvas canvas ) throws Exception
    {
        GlimpseLayout layout = new SimpleLayoutExample( ).getLayout( );

        final JPopupMenu _popupMenu = createPopupMenu( );

        canvas.addMouseListener( new MouseListener( )
        {

            @Override
            public void mouseClicked( MouseEvent arg0 )
            {
                if ( arg0.getButton( ) == MouseEvent.BUTTON3 )
                {
                    _popupMenu.show( canvas, arg0.getX( ), arg0.getY( ) );
                }
            }

            @Override
            public void mouseEntered( MouseEvent arg0 )
            {
            }

            @Override
            public void mouseExited( MouseEvent arg0 )
            {
            }

            @Override
            public void mousePressed( MouseEvent arg0 )
            {
            }

            @Override
            public void mouseReleased( MouseEvent arg0 )
            {
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

/*
 * Copyright (c) 2019, Metron, Inc.
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

import static com.jogamp.opengl.GLProfile.GL3bc;
import static com.metsci.glimpse.support.QuickUtils.initGlimpseOrExitJvm;
import static com.metsci.glimpse.support.QuickUtils.quickGlimpseCanvas;
import static com.metsci.glimpse.support.QuickUtils.quickGlimpseWindow;
import static com.metsci.glimpse.support.QuickUtils.swingInvokeLater;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.GLProfile;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.context.TargetStackUtil;
import com.metsci.glimpse.event.mouse.GlimpseMouseAdapter;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;

/**
 * A Glimpse plot with a Swing JPopupMenu which appears when right clicking on the plot.
 *
 * @author ulman
 */
public class PopupMenuExample
{
    private static final Logger logger = Logger.getLogger( PopupMenuExample.class.getName( ) );

    public static void main( String[] args )
    {
        swingInvokeLater( ( ) ->
        {
            // create a simple plot but disable right click selection locking
            // on the axis listeners, as we will be using right clicking to bring up popup menu
            SimplePlot2D plot = new SimplePlot2D( )
            {
                protected AxisMouseListener createAxisMouseListenerX( )
                {
                    AxisMouseListener1D l = new AxisMouseListener1D( );
                    l.setAllowSelectionLock( false );
                    return l;
                }

                protected AxisMouseListener createAxisMouseListenerY( )
                {
                    AxisMouseListener1D l = new AxisMouseListener1D( );
                    l.setAllowSelectionLock( false );
                    return l;
                }

                protected AxisMouseListener createAxisMouseListenerZ( )
                {
                    AxisMouseListener1D l = new AxisMouseListener1D( );
                    l.setAllowSelectionLock( false );
                    return l;
                }

                protected AxisMouseListener createAxisMouseListenerXY( )
                {
                    AxisMouseListener2D l = new AxisMouseListener2D( );
                    l.setAllowSelectionLock( false );
                    return l;
                }
            };

            // create a window and show the plot
            String appName = "Popup Menu Example";
            GLProfile glProfile = initGlimpseOrExitJvm( appName, GL3bc );
            NewtSwingEDTGlimpseCanvas canvas = quickGlimpseCanvas( glProfile, plot );
            quickGlimpseWindow( appName, canvas );

            // add a popup menu to the plot
            SwingUtilities.invokeLater( ( ) ->
            {
                final JPopupMenu popupMenu = createPopupMenu( );

                plot.getLayoutCenter( ).addGlimpseMouseListener( new GlimpseMouseAdapter( )
                {
                    @Override
                    public void mousePressed( GlimpseMouseEvent event )
                    {
                        if ( event.isButtonDown( MouseButton.Button3 ) )
                        {
                            event = TargetStackUtil.translateCoordinates( event, canvas );
                            popupMenu.show( canvas, event.getX( ), event.getY( ) );
                        }
                    }
                } );
            } );
        } );
    }

    public static JPopupMenu createPopupMenu( )
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
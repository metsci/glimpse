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
package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.LandingIndicator.ReprType.OPAQUE_WINDOW;
import static com.metsci.glimpse.docking.LandingIndicator.ReprType.SHAPED_WINDOW;
import static com.metsci.glimpse.docking.LandingIndicator.ReprType.TRANSLUCENT_WINDOW;
import static java.awt.GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT;
import static java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT;
import static javax.swing.BorderFactory.createLineBorder;

import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.Area;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.metsci.glimpse.docking.DockingThemes.DockingTheme;

public class LandingIndicator
{

    protected static enum ReprType
    {
        OPAQUE_WINDOW,
        TRANSLUCENT_WINDOW,
        SHAPED_WINDOW
    }


    protected final DockingTheme theme;
    protected final JFrame frame;
    protected final JPanel frameContent;

    protected ReprType recentReprType;


    public LandingIndicator( DockingTheme theme )
    {
        this.theme = theme;

        this.frame = new JFrame( );
        frame.setAlwaysOnTop( true );
        frame.setFocusable( false );
        frame.setUndecorated( true );

        this.frameContent = new JPanel( );
        frame.setContentPane( frameContent );

        this.recentReprType = null;
    }

    public void setBounds( Rectangle bounds )
    {
        if ( bounds == null )
        {
            frame.setVisible( false );
        }
        else
        {
            frame.setBounds( bounds );

            GraphicsDevice device = frame.getGraphicsConfiguration( ).getDevice( );
            if ( device.isWindowTranslucencySupported( TRANSLUCENT ) )
            {
                if ( recentReprType != TRANSLUCENT_WINDOW )
                {
                    frame.setShape( null );
                    frameContent.setBackground( null );
                    frameContent.setBorder( createLineBorder( theme.landingIndicatorColor, theme.landingIndicatorThickness ) );
                    frame.setOpacity( 0.5f );
                    this.recentReprType = TRANSLUCENT_WINDOW;
                }
            }
            else if ( device.isWindowTranslucencySupported( PERPIXEL_TRANSPARENT ) )
            {
                if ( recentReprType != SHAPED_WINDOW )
                {
                    // Set the whole pane to the bg color, to minimize flicker
                    frameContent.setBackground( theme.landingIndicatorColor );
                    frameContent.setBorder( null );
                    this.recentReprType = SHAPED_WINDOW;
                }

                int thickness = theme.landingIndicatorThickness;
                Area shape = new Area( new Rectangle( 0, 0, bounds.width, bounds.height ) );
                shape.subtract( new Area( new Rectangle( thickness, thickness, bounds.width - 2*thickness, bounds.height - 2*thickness ) ) );
                frame.setShape( shape );
            }
            else
            {
                if ( recentReprType != OPAQUE_WINDOW )
                {
                    frame.setShape( null );
                    frameContent.setBackground( null );
                    frameContent.setBorder( createLineBorder( theme.landingIndicatorColor, theme.landingIndicatorThickness ) );
                    this.recentReprType = OPAQUE_WINDOW;
                }
            }

            frame.setVisible( true );
        }
    }

    public void dispose( )
    {
        frame.dispose( );
    }

}

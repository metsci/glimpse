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
import static java.lang.Math.max;
import static javax.swing.BorderFactory.createMatteBorder;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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
            // On some platforms, Swing refuses to programmatically move a window beyond the edge of the screen.
            // Instead, Swing translates the window so that it is entirely on the screen. When our landing-region
            // is an existing docker that is partly offscreen, we have a problem: Swing won't allow us to move the
            // indicator partly offscreen, so it ends up in the wrong place.
            //
            // So here we size the indicator so that it will NOT go beyond the edge of the screen, and adjust the
            // border thickness on each side appropriately. The part of the indicator that would be offscreen is
            // simply not drawn.
            //
            // I'm not sure whether this will behave properly for non-rectangular multi-head setups -- it depends
            // on the details of the translation behavior (which varies by platform, and doesn't seem to be documented).
            //
            // There is an alternative approach, using this JFrame indicator for new-window landing-regions, but
            // switching to a GlassPane overlay when the destination is an existing docker. Such an approach would
            // be robust to non-rectangular multi-head setups, although the implementation would be more complicated.
            // The main downside is that it would be difficult (or impossible) to make sure the overlay indicator
            // looked the same as the window indicator, for all combinations of translucency support and lightweight/
            // heavyweight content.
            //

            Rectangle displayBounds = new Rectangle( );
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment( );
            for ( GraphicsDevice screen : env.getScreenDevices( ) )
            {
                for ( GraphicsConfiguration config : screen.getConfigurations( ) )
                {
                    displayBounds = displayBounds.union( config.getBounds( ) );
                }
            }
            Rectangle visibleBounds = bounds.intersection( displayBounds );
            frame.setBounds( visibleBounds );

            Color color = theme.landingIndicatorColor;
            int border = theme.landingIndicatorThickness;
            int topBorder = max( 0, border - ( visibleBounds.y - bounds.y ) );
            int leftBorder = max( 0, border - ( visibleBounds.x - bounds.x ) );
            int bottomBorder = max( 0, border - ( ( bounds.y + bounds.height ) - ( visibleBounds.y + visibleBounds.height ) ) );
            int rightBorder = max( 0, border - ( ( bounds.x + bounds.width ) - ( visibleBounds.x + visibleBounds.width ) ) );

            GraphicsDevice device = frame.getGraphicsConfiguration( ).getDevice( );
            if ( device.isWindowTranslucencySupported( TRANSLUCENT ) )
            {
                if ( recentReprType != TRANSLUCENT_WINDOW )
                {
                    frame.setShape( null );
                    frameContent.setBackground( null );
                    frameContent.setBorder( createMatteBorder( topBorder, leftBorder, bottomBorder, rightBorder, color ) );
                    frame.setOpacity( 0.5f );
                    this.recentReprType = TRANSLUCENT_WINDOW;
                }
            }
            else if ( device.isWindowTranslucencySupported( PERPIXEL_TRANSPARENT ) )
            {
                if ( recentReprType != SHAPED_WINDOW )
                {
                    // Set the whole pane to the bg color, to minimize flicker
                    frameContent.setBackground( color );
                    frameContent.setBorder( null );
                    this.recentReprType = SHAPED_WINDOW;
                }

                Area shape = new Area( new Rectangle( 0, 0, visibleBounds.width, visibleBounds.height ) );
                shape.subtract( new Area( new Rectangle( leftBorder, topBorder, visibleBounds.width - ( leftBorder + rightBorder ), visibleBounds.height - ( topBorder + bottomBorder ) ) ) );
                frame.setShape( shape );
            }
            else
            {
                if ( recentReprType != OPAQUE_WINDOW )
                {
                    frame.setShape( null );
                    frameContent.setBackground( null );
                    frameContent.setBorder( createMatteBorder( topBorder, leftBorder, bottomBorder, rightBorder, color ) );
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

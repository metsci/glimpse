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
package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.LandingIndicator.ReprType.OPAQUE_WINDOW;
import static com.metsci.glimpse.docking.LandingIndicator.ReprType.SHAPED_WINDOW;
import static com.metsci.glimpse.docking.LandingIndicator.ReprType.TRANSLUCENT_WINDOW;
import static java.awt.GraphicsDevice.TYPE_RASTER_SCREEN;
import static java.awt.GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT;
import static java.awt.GraphicsDevice.WindowTranslucency.TRANSLUCENT;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.unmodifiableCollection;
import static javax.swing.BorderFactory.createMatteBorder;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.metsci.glimpse.docking.DockingThemes.DockingTheme;

public class LandingIndicator
{

    protected static enum ReprType
    {
        OPAQUE_WINDOW, TRANSLUCENT_WINDOW, SHAPED_WINDOW
    }

    protected static class ScreenEntry
    {
        public final int xScreen;
        public final int yScreen;
        public final int wScreen;
        public final int hScreen;

        public final JFrame frame;
        public final JPanel content;

        public ScreenEntry( int xScreen, int yScreen, int wScreen, int hScreen, JFrame frame, JPanel content )
        {
            this.xScreen = xScreen;
            this.yScreen = yScreen;
            this.wScreen = wScreen;
            this.hScreen = hScreen;

            this.frame = frame;
            this.content = content;
        }
    }

    protected final DockingTheme theme;
    protected final ReprType reprType;
    protected final Collection<ScreenEntry> screenEntries;

    public LandingIndicator( DockingTheme theme )
    {
        this.theme = theme;

        boolean haveTranslucentWindows = true;
        boolean haveTransparentPixels = true;
        Collection<ScreenEntry> screenEntries = new ArrayList<>( );

        // On some platforms, Swing refuses to programmatically move a window beyond the edge of the screen.
        // Instead, Swing silently translates the window so that it is entirely on the screen. When our landing-
        // region is an existing docker that is partly offscreen, we have a problem: Swing won't allow us to
        // move the indicator partly offscreen, so it ends up in the wrong place.
        //
        // To deal with this, we create one frame for each screen. Each frame always stays entirely on its screen:
        // any part of it that would spill over to another screen is clipped. The part of the indicator for that
        // other screen is drawn by another frame -- the frame for that other screen.
        //
        // There are a couple of alternative approaches, but this one is the best option: it behaves properly for
        // arbitrary multi-head geometries, while also staying decoupled from the rest of the code. Its primary
        // downside is that it can cause multiple windows to appear in the system window list.
        //

        Toolkit toolkit = Toolkit.getDefaultToolkit( );
        for ( GraphicsDevice screen : GraphicsEnvironment.getLocalGraphicsEnvironment( ).getScreenDevices( ) )
        {
            if ( screen.getType( ) == TYPE_RASTER_SCREEN )
            {
                haveTranslucentWindows &= screen.isWindowTranslucencySupported( TRANSLUCENT );
                haveTransparentPixels &= screen.isWindowTranslucencySupported( PERPIXEL_TRANSPARENT );

                GraphicsConfiguration config = screen.getDefaultConfiguration( );
                Rectangle bounds = config.getBounds( );
                Insets insets = toolkit.getScreenInsets( config );
                int x = bounds.x + insets.left;
                int y = bounds.y + insets.top;
                int w = bounds.width - ( insets.left + insets.right );
                int h = bounds.height - ( insets.top + insets.bottom );

                JFrame frame = new JFrame( ".", config );

                @SuppressWarnings("serial")
                JPanel content = new JPanel( )
                {
                    // Custom paint seems to reduce flickering
                    @Override
                    public void paintComponent( Graphics g )
                    {
                        g.setColor( getBackground( ) );
                        g.fillRect( 0, 0, getWidth( ), getHeight( ) );
                    }
                };

                frame.setAutoRequestFocus( false );
                frame.setFocusable( false );
                frame.setFocusableWindowState( false );
                frame.setAlwaysOnTop( true );
                frame.setFocusable( false );
                frame.setUndecorated( true );
                frame.setContentPane( content );

                screenEntries.add( new ScreenEntry( x, y, w, h, frame, content ) );
            }
        }

        if ( haveTranslucentWindows )
            this.reprType = TRANSLUCENT_WINDOW;
        else if ( haveTransparentPixels )
            this.reprType = SHAPED_WINDOW;
        else
            this.reprType = OPAQUE_WINDOW;

        this.screenEntries = unmodifiableCollection( screenEntries );

        for ( ScreenEntry en : screenEntries )
        {
            JFrame frame = en.frame;
            JPanel content = en.content;
            switch ( reprType )
            {
                case TRANSLUCENT_WINDOW:
                    frame.setOpacity( 0.5f );
                    content.setBackground( null );
                    break;

                case SHAPED_WINDOW:
                    content.setBorder( null );
                    content.setBackground( theme.landingIndicatorColor );
                    break;

                default:
                    content.setBackground( null );
                    break;
            }
        }
    }

    public void setBounds( Rectangle bounds )
    {
        if ( bounds == null )
        {
            for ( ScreenEntry en : screenEntries )
            {
                JFrame frame = en.frame;
                frame.setVisible( false );
            }
        }
        else
        {
            for ( ScreenEntry en : screenEntries )
            {
                JFrame frame = en.frame;
                JPanel content = en.content;

                int x = max( en.xScreen, bounds.x );
                int y = max( en.yScreen, bounds.y );
                int xEnd = min( en.xScreen + en.wScreen, bounds.x + bounds.width );
                int yEnd = min( en.yScreen + en.hScreen, bounds.y + bounds.height );
                int w = xEnd - x;
                int h = yEnd - y;

                if ( w > 0 && h > 0 )
                {
                    frame.setBounds( x, y, w, h );

                    int border = theme.landingIndicatorThickness;
                    int topBorder = max( 0, border - ( y - bounds.y ) );
                    int leftBorder = max( 0, border - ( x - bounds.x ) );
                    int bottomBorder = max( 0, border - ( ( bounds.y + bounds.height ) - yEnd ) );
                    int rightBorder = max( 0, border - ( ( bounds.x + bounds.width ) - xEnd ) );

                    switch ( reprType )
                    {
                        case TRANSLUCENT_WINDOW:
                            content.setBorder( createMatteBorder( topBorder, leftBorder, bottomBorder, rightBorder, theme.landingIndicatorColor ) );
                            break;

                        case SHAPED_WINDOW:
                            Area shape = new Area( new Rectangle( 0, 0, w, h ) );
                            shape.subtract( new Area( new Rectangle( leftBorder, topBorder, w - ( leftBorder + rightBorder ), h - ( topBorder + bottomBorder ) ) ) );
                            frame.setShape( shape );
                            break;

                        default:
                            content.setBorder( createMatteBorder( topBorder, leftBorder, bottomBorder, rightBorder, theme.landingIndicatorColor ) );
                            break;
                    }

                    frame.setVisible( true );
                }
                else
                {
                    frame.setVisible( false );
                }
            }
        }
    }

    public void dispose( )
    {
        for ( ScreenEntry en : screenEntries )
        {
            JFrame frame = en.frame;
            frame.dispose( );
        }
    }

}

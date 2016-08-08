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
package com.metsci.glimpse.support;

import static com.metsci.glimpse.util.GeneralUtils.newArrayList;
import static java.awt.GraphicsDevice.TYPE_RASTER_SCREEN;
import static java.lang.Math.round;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JFrame;

import com.metsci.glimpse.canvas.GlimpseCanvas;

public class FrameUtils
{

    public static JFrame newFrame( String title, Container contentPane, int closeOperation )
    {
        JFrame frame = new JFrame( title );
        frame.setDefaultCloseOperation( closeOperation );
        frame.setContentPane( contentPane );
        return frame;
    }

    /**
     * Adds a window-closing listener that stops the animator. This is helpful
     * for programs that end when a window gets closed. Typical setup sequence is:
     *
     *     JFrame frame = newFrame( "Title", canvas, DISPOSE_ON_CLOSE );
     *     stopOnWindowClosing( frame, animator );
     *     disposeOnWindowClosing( frame, canvas );
     *     destroyOnWindowClosing( frame, sharedDrawable );
     *     showFrameCentered( frame );
     *
     */
    public static WindowListener stopOnWindowClosing( Window window, final GLAnimatorControl animator )
    {
        WindowListener listener = new WindowAdapter( )
        {
            public void windowClosing( WindowEvent ev )
            {
                animator.stop( );
            }
        };
        window.addWindowListener( listener );
        return listener;
    }

    /**
     * Adds a window-closing listener that disposes of the canvas. This is helpful
     * for programs that end when a window gets closed. Typical setup sequence is:
     *
     *     JFrame frame = newFrame( "Title", canvas, DISPOSE_ON_CLOSE );
     *     stopOnWindowClosing( frame, animator );
     *     disposeOnWindowClosing( frame, canvas );
     *     destroyOnWindowClosing( frame, sharedDrawable );
     *     showFrameCentered( frame );
     *
     */
    public static WindowListener disposeOnWindowClosing( Window window, final GlimpseCanvas canvas )
    {
        WindowListener listener = new WindowAdapter( )
        {
            public void windowClosing( WindowEvent ev )
            {
                canvas.disposeAttached( );
            }
        };
        window.addWindowListener( listener );
        return listener;
    }

    /**
     * Adds a window-closing listener that destroys the drawable. This is helpful
     * for programs that end when a window gets closed. Typical setup sequence is:
     *
     *     JFrame frame = newFrame( "Title", canvas, DISPOSE_ON_CLOSE );
     *     stopOnWindowClosing( frame, animator );
     *     disposeOnWindowClosing( frame, canvas );
     *     destroyOnWindowClosing( frame, sharedDrawable );
     *     showFrameCentered( frame );
     *
     */
    public static WindowListener destroyOnWindowClosing( Window window, final GLAutoDrawable drawable )
    {
        WindowListener listener = new WindowAdapter( )
        {
            public void windowClosing( WindowEvent ev )
            {
                drawable.destroy( );
            }
        };
        window.addWindowListener( listener );
        return listener;
    }

    /**
     * Size the frame, center it, and make it visible.
     *
     * The frame is sized to take up 85% of available horizontal and vertical space.
     *
     * @see #centerFrame(Frame)
     */
    public static void showFrameCentered( Frame frame )
    {
        centerFrame( frame );
        frame.setVisible( true );
    }

    /**
     * Size the frame, center it, and make it visible.
     *
     * @see #centerFrame(Frame, double)
     */
    public static void showFrameCentered( Frame frame, double screenExtentFraction )
    {
        centerFrame( frame, screenExtentFraction );
        frame.setVisible( true );
    }

    /**
     * Size the frame, center it, and make it visible.
     *
     * @see #centerFrame(Frame, int, int)
     */
    public static void showFrameCentered( Frame frame, int width, int height )
    {
        centerFrame( frame, width, height );
        frame.setVisible( true );
    }

    /**
     * Size the frame, and center it on the screen (usually the screen of the primary monitor,
     * but may vary based on the platform's window manager). Does not make the frame visible.
     *
     * The frame is sized to take up 85% of available horizontal and vertical space.
     *
     * @see #centerFrame(Frame, double)
     */
    public static void centerFrame( Frame frame )
    {
        centerFrame( frame, 0.85 );
    }

    /**
     * Size the frame, and center it on the screen (usually the screen of the primary monitor,
     * but may vary based on the platform's window manager). Does not make the frame visible.
     *
     * The frame is sized to take up the given fraction of available horizontal and vertical
     * space. In most cases, available space will not include system bars and such, but it is
     * ultimately up to the platform's window manager.
     */
    public static void centerFrame( Frame frame, double screenExtentFraction )
    {
        Rectangle maxWindowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment( ).getMaximumWindowBounds( );
        float frac = ( float ) screenExtentFraction;
        int width = round( frac * maxWindowBounds.width );
        int height = round( frac * maxWindowBounds.height );
        centerFrame( frame, width, height );
    }

    /**
     * Size the frame, and center it on the screen (usually the screen of the primary monitor,
     * but may vary based on the platform's window manager). Does not make the frame visible.
     */
    public static void centerFrame( Frame frame, int width, int height )
    {
        frame.setPreferredSize( new Dimension( width, height ) );
        frame.pack( );
        frame.setSize( width, height );
        frame.setLocationRelativeTo( null );
    }

    /**
     * Make the frame fullscreen across all screens, and make it visible.
     *
     * NOTE: This functionality is subject to the whims of the platform's window manager.
     */
    public static void showFrameFullscreen( Frame frame )
    {
        showFrameFullscreen( frame, true );
    }

    /**
     * Make the frame fullscreen, and make it visible. The allScreens flag indicates whether all
     * screens should be used, or only the primary monitor.
     *
     * NOTE: This functionality is subject to the whims of the platform's window manager.
     */
    public static void showFrameFullscreen( Frame frame, boolean allScreens )
    {
        frame.setUndecorated( true );

        if ( allScreens )
        {
            frame.setAlwaysOnTop( true );
            frame.setSize( Toolkit.getDefaultToolkit( ).getScreenSize( ) );
            frame.setResizable( false );
            frame.setVisible( true );
        }
        else
        {
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment( ).getDefaultScreenDevice( );
            device.setFullScreenWindow( frame );
        }
    }

    public static List<GraphicsDevice> screens( )
    {
        List<GraphicsDevice> screens = newArrayList( );
        for ( GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment( ).getScreenDevices( ) )
        {
            if ( device.getType( ) == TYPE_RASTER_SCREEN )
            {
                screens.add( device );
            }
        }
        return screens;
    }

    /**
     * Make the frame fullscreen on the screen indicated by screenIndex, and make it visible.
     *
     * NOTE: This functionality is subject to the whims of the platform's window manager.
     */
    public static void showFrameFullscreen( Frame frame, GraphicsDevice screen )
    {
        frame.setUndecorated( true );
        screen.setFullScreenWindow( frame );
    }

}

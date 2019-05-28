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
package com.metsci.glimpse.support;

import static com.google.common.base.Objects.equal;
import static com.metsci.glimpse.platformFixes.PlatformFixes.fixPlatformQuirks;
import static com.metsci.glimpse.support.DisposableUtils.onWindowClosing;
import static com.metsci.glimpse.support.FrameUtils.screenFracSize;
import static com.metsci.glimpse.util.GeneralUtils.array;
import static javax.swing.JOptionPane.VALUE_PROPERTY;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import com.jogamp.newt.Screen;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.platformFixes.PlatformFixes;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

/**
 * A collection of functions for quickly creating plots and showing them in windows.
 * <p>
 * The goal of this class is not to be polished, but to make it extremely easy to pop
 * up a quick data plot. This can be useful during development -- e.g. to examine data
 * for debugging purposes, or to plot timing results.
 * <p>
 * In terms of design aesthetics, the core module might not be the ideal place for this
 * class. However, keeping it in core allows it to be used without adding an extra
 * dependency, which is a practically significant improvement in ease of use.
 * <p>
 * @author hogye
 */
public class QuickUtils
{

    /**
     * Performs several init operations that are desirable for most Glimpse applications:
     * <ul>
     * <li>{@link PlatformFixes#fixPlatformQuirks()}
     * <li>Use heavyweight popup menus, which are visible over top of glimpse canvases
     * <li>Show tooltips reliably, even if the focus manager gets confused by a glimpse canvas
     * </ul>
     * <p>
     * This method is for convenience only. It is perfectly acceptable for an application
     * to perform some or all of these init operations piecemeal, instead of calling this
     * method.
     * <p>
     * <strong>NOTE:</strong> This method should be called near the beginning of main,
     * after the Swing {@link LookAndFeel} has been set, but before any UI components get
     * created.
     */
    public static void initStandardGlimpseApp( )
    {
        fixPlatformQuirks( );

        ToolTipManager.sharedInstance( ).setLightWeightPopupEnabled( false );
        JPopupMenu.setDefaultLightWeightPopupEnabled( false );

        UIManager.put( "ToolTipManager.enableToolTipMode", "allWindows" );
    }

    public static void requireSwingThread( )
    {
        if ( !SwingUtilities.isEventDispatchThread( ) )
        {
            throw new RuntimeException( "This operation is only allowed on the Swing/AWT event-dispatch thread" );
        }
    }

    public static GLProfile glProfileOrNull( String glProfileName )
    {
        try
        {
            return GLProfile.get( glProfileName );
        }
        catch ( GLException e )
        {
            return null;
        }
    }

    public static boolean showGLWarningDialog( String progName )
    {
        String dialogTitle = progName + " Warning";
        String text = "Graphics capabilities on this computer are not sufficient.\nYou can run the program anyway, but you may encounter rendering problems.";
        String runAnywayOption = "Run Anyway";
        String quitOption = "Quit";
        JOptionPane optionPane = new JOptionPane( text, WARNING_MESSAGE, YES_NO_OPTION, null, array( runAnywayOption, quitOption ), runAnywayOption );

        AtomicBoolean shouldRunAnyway = new AtomicBoolean( false );
        optionPane.addPropertyChangeListener( ( ev ) ->
        {
            if ( equal( ev.getPropertyName( ), VALUE_PROPERTY ) && equal( ev.getNewValue( ), runAnywayOption ) )
            {
                shouldRunAnyway.set( true );
            }
        } );

        // setVisible(true) blocks until we get an answer
        JDialog dialog = optionPane.createDialog( null, dialogTitle );
        dialog.setModal( true );
        dialog.setVisible( true );
        dialog.dispose( );

        return shouldRunAnyway.get( );
    }

    public static MultiAxisPlot2D quickXyPlot( GlimpsePainter... painters )
    {
        MultiAxisPlot2D plot = new MultiAxisPlot2D( );

        Axis1D xAxis = plot.getCenterAxisX( );
        Axis1D yAxis = plot.getCenterAxisY( );
        AxisInfo xAxisInfo = plot.createAxisBottom( "xAxis", xAxis, new AxisMouseListener1D( ) );
        AxisInfo yAxisInfo = plot.createAxisLeft( "yAxis", yAxis, new AxisMouseListener1D( ) );

        GridPainter gridPainter = new GridPainter( xAxisInfo.getTickHandler( ), yAxisInfo.getTickHandler( ) );

        CrosshairPainter crosshairPainter = new CrosshairPainter( );
        crosshairPainter.showSelectionBox( false );

        BorderPainter borderPainter = new BorderPainter( );

        plot.addPainter( gridPainter );
        for ( GlimpsePainter painter : painters )
        {
            plot.addPainter( painter );
        }
        plot.addPainter( crosshairPainter );
        plot.addPainter( borderPainter );

        return plot;
    }

    /**
     * Similar to {@link #quickGlimpseWindow(String, String, double, GlimpseLayout)},
     * but with behavior suitable for a single-window application. In particular, starts
     * by calling {@link #initStandardGlimpseApp()}, and warns the user if the named
     * {@link GLProfile} is not available.
     * <p>
     * <strong>NOTE:</strong> If the named {@link GLProfile} is not available, and the
     * user chooses to quit rather than continue, this method calls {@link System#exit(int)}!
     */
    public static void quickGlimpseApp( String appName, String glProfileName, double screenFrac, GlimpseLayout layout )
    {
        initStandardGlimpseApp( );

        GLProfile glProfile = glProfileOrNull( glProfileName );
        if ( glProfile == null && !showGLWarningDialog( appName ) )
        {
            System.exit( 1 );
        }

        quickGlimpseWindow( appName, glProfile, screenFrac, layout );
    }

    /**
     * See {@link #quickGlimpseWindow(String, GLProfile, double, GlimpseLayout)}.
     * <p>
     * <strong>NOTE:</strong> Throws a runtime exception if the named {@link GLProfile}
     * is not available.
     */
    public static void quickGlimpseWindow( String title, String glProfileName, double screenFrac, GlimpseLayout layout )
    {
        quickGlimpseWindow( title, GLProfile.get( glProfileName ), screenFrac, layout );
    }

    /**
     * Creates and shows a new window displaying the specified {@code layout}.
     * <p>
     * <strong>NOTE:</strong> Must be called on the Swing EDT.
     */
    public static void quickGlimpseWindow( String title, GLProfile glProfile, double screenFrac, GlimpseLayout layout )
    {
        requireSwingThread( );

        NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( glProfile );
        canvas.addLayout( layout );

        GLAnimatorControl animator = new SwingEDTAnimator( 60 );
        animator.add( canvas.getGLDrawable( ) );
        animator.start( );

        JFrame frame = new JFrame( );
        frame.setTitle( title );

        // This listener must run before NewtCanvasAWT's built-in window-closing
        // listener does -- so add it before we add the canvas to the frame
        onWindowClosing( frame, ( ev ) ->
        {
            animator.stop( );
            tearDownCanvas( canvas );
        } );

        frame.getContentPane( ).add( canvas );
        frame.setSize( screenFracSize( screenFrac ) );
        frame.setLocationRelativeTo( null );
        frame.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        frame.setVisible( true );
    }

    /**
     * When used in a window-closing listener, this method <strong>MUST</strong> run
     * before NewtCanvasAWT's built-in window-closing listener.
     */
    public static void tearDownCanvas( NewtSwingGlimpseCanvas canvas )
    {
        // Hold a reference to the screen so that JOGL's auto-cleanup doesn't destroy
        // and then recreate resources (like the NEDT thread) while we're still working
        Screen screen = canvas.getGLWindow( ).getScreen( );
        screen.addReference( );
        try
        {
            // Canvas destruction is kludgy -- the relevant JOGL code is complicated,
            // the relevant AWT code is platform-dependent native code, and the relevant
            // AWT behavior is affected by quirks and mysteries of the window manager
            // and/or OS. Debugging problems directly would take a long time (weeks or
            // months).
            //
            // The following call sequence seems to work reliably. It was arrived at by
            // trying various sequences until one worked for the platforms and situations
            // we care about.
            //
            // Notes:
            //
            //  * Without setVisible(false), the screen area formerly occupied by the
            //    canvas ends up unusable -- it appears blank or continues to show the
            //    canvas's final frame, and it does not respond to resize events.
            //
            //  * On Windows 10, without the explicit getGLWindow().destroy(), the AWT
            //    thread begins receiving WM_TIMER events, and continues to receive them
            //    indefinitely. This prevents the AWT thread from exiting, which in turn
            //    can prevent the JVM from exiting. This is particularly strange because
            //    getCanvas().destroy() makes it own call to getGLWindow().destroy(). Not
            //    sure whether the difference is in the timing (due to a race), or simply
            //    in the ordering of the calls.
            //
            //  * In the past, the getCanvas().destroy() call has sometimes resulted in
            //    segfaults. However, without that call, we get the WM_TIMER issue. Not
            //    sure what to do about this, except hope that the timing and threading
            //    have been perturbed enough over the years that segfaults are no longer
            //    an issue in practice. FIXME: Test thoroughly, on many machines.
            //
            //  * If we call setNEWTChild(null) instead of setVisible(false), we get the
            //    WM_TIMER issue.
            //
            //  * If we call parent.remove(canvas) instead of setVisible(false), we get
            //    the WM_TIMER issue.
            //
            canvas.setVisible( false );
            canvas.getGLWindow( ).destroy( );
            canvas.getCanvas( ).destroy( );
        }
        finally
        {
            screen.removeReference( );
        }
    }

}

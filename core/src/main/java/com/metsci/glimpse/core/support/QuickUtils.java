/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.support;

import static com.google.common.base.Objects.equal;
import static com.metsci.glimpse.core.support.DisposableUtils.onWindowClosing;
import static com.metsci.glimpse.platformFixes.PlatformFixes.fixPlatformQuirks;
import static com.metsci.glimpse.util.GeneralUtils.array;
import static javax.swing.JOptionPane.VALUE_PROPERTY;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.core.layout.GlimpseLayout;
import com.metsci.glimpse.core.painter.base.GlimpsePainter;
import com.metsci.glimpse.core.painter.decoration.BorderPainter;
import com.metsci.glimpse.core.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.core.painter.decoration.GridPainter;
import com.metsci.glimpse.core.plot.MultiAxisPlot2D;
import com.metsci.glimpse.core.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.core.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.core.support.settings.LookAndFeel;
import com.metsci.glimpse.core.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.core.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.core.support.swing.SwingEDTAnimator;
import com.metsci.glimpse.platformFixes.PlatformFixes;
import com.metsci.glimpse.util.ThrowingRunnable;

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
     * after the Swing LookAndFeel has been set, but before any UI components get created.
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

    /**
     * Like {@link SwingUtilities#invokeLater(Runnable)}, but allows the runnable to
     * throw checked exceptions. If a checked exception is thrown, it will be caught
     * and wrapped in a new {@link RuntimeException}, which will then be thrown.
     */
    public static void swingInvokeLater( ThrowingRunnable runnable )
    {
        // We could make this more like ExecutorService.submit() by returning a Future.
        // That would allow the caller to (1) block, (2) retrieve the value returned by
        // the callable, and (3) handle exceptions thrown by the callable. However, the
        // caller would ALWAYS have to call Future.get(), or else exceptions would be
        // silently swallowed.
        //
        // In practice, we usually want Swing EDT calls to behave like Executor.execute()
        // or SwingUtilities.invokeLater(). The caller can't retrieve the value or handle
        // exceptions, but also isn't REQUIRED to handle exceptions. This matches the way
        // SwingUtilities.invokeLater() already deals with RuntimeExceptions; we're just
        // expanding it to do the same with all Exceptions.
        //
        SwingUtilities.invokeLater( ( ) ->
        {
            try
            {
                runnable.run( );
            }
            catch ( RuntimeException e )
            {
                throw e;
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }
        } );
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

    /**
     * Creates a {@link MultiAxisPlot2D} layout with contents that are desirable for most
     * XY plots:
     * <ul>
     * <li>X and Y axes
     * <li>GridPainter
     * <li>CrosshairPainter (with selection box disabled)
     * <li>BorderPainter.
     * </ul>
     * <p>
     * This method is for convenience only. It is perfectly acceptable for an application
     * to perform some or all of these init operations piecemeal, instead of calling this
     * method.
     */
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

    public static AbstractLookAndFeel quickDefaultLaf( )
    {
        return new SwingLookAndFeel( );
    }

    public static SwingEDTAnimator quickDefaultAnimator( )
    {
        return new SwingEDTAnimator( 60 );
    }

    public static Dimension quickDefaultSize( )
    {
        return new Dimension( 800, 800 );
    }

    public static JFrame quickGlimpseApp( String appName, String glProfileName, GlimpseLayout layout )
    {
        return quickGlimpseApp( appName, glProfileName, layout, quickDefaultSize( ) );
    }

    public static JFrame quickGlimpseApp( String appName, String glProfileName, GlimpseLayout layout, LookAndFeel laf )
    {
        return quickGlimpseApp( appName, glProfileName, layout, quickDefaultSize( ), laf );
    }

    public static JFrame quickGlimpseApp( String appName, String glProfileName, GlimpseLayout layout, int width, int height )
    {
        return quickGlimpseApp( appName, glProfileName, layout, width, height, quickDefaultLaf( ) );
    }

    public static JFrame quickGlimpseApp( String appName, String glProfileName, GlimpseLayout layout, int width, int height, LookAndFeel laf )
    {
        return quickGlimpseApp( appName, glProfileName, layout, new Dimension( width, height ) );
    }

    public static JFrame quickGlimpseApp( String appName, String glProfileName, GlimpseLayout layout, Dimension size )
    {
        return quickGlimpseApp( appName, glProfileName, layout, size, quickDefaultLaf( ) );
    }

    /**
     * Similar to {@link #quickGlimpseWindow(String, String, double, GlimpseLayout)},
     * but with behavior suitable for a single-window application. In particular, starts
     * by calling {@link #initStandardGlimpseApp()}, and warns the user if the named
     * {@link GLProfile} is not available.
     * <p>
     * This method is for convenience only. It is perfectly acceptable for an application
     * to perform some or all of these init operations piecemeal, instead of calling this
     * method.
     * <p>
     * <strong>NOTE:</strong> If the named {@link GLProfile} is not available, and the
     * user chooses to quit rather than continue, this method calls {@link System#exit(int)}!
     */
    public static JFrame quickGlimpseApp( String appName, String glProfileName, GlimpseLayout layout, Dimension size, LookAndFeel laf )
    {
        GLProfile glProfile = initGlimpseOrExitJvm( appName, glProfileName );
        NewtSwingEDTGlimpseCanvas canvas = quickGlimpseCanvas( glProfile, layout, laf );
        return quickGlimpseWindow( appName, canvas, size );
    }

    /**
     * Does several things that are typically done at application startup:
     * <ol>
     * <li>Calls {@link #initStandardGlimpseApp()}
     * <li>Warns the user if the named {@link GLProfile} is not available
     * <li>If the user chooses to continue anyway, returns null
     * <li><strong>If the user chooses NOT to continue, calls {@link System#exit(int)}</strong>
     * </ol>
     */
    public static GLProfile initGlimpseOrExitJvm( String appName, String glProfileName )
    {
        initStandardGlimpseApp( );

        GLProfile glProfile = glProfileOrNull( glProfileName );
        if ( glProfile == null && !showGLWarningDialog( appName ) )
        {
            System.exit( 1 );
        }

        return glProfile;
    }

    public static NewtSwingEDTGlimpseCanvas quickGlimpseCanvas( String glProfileName, GlimpseLayout layout )
    {
        return quickGlimpseCanvas( GLProfile.get( glProfileName ), layout );
    }

    public static NewtSwingEDTGlimpseCanvas quickGlimpseCanvas( String glProfileName, GlimpseLayout layout, LookAndFeel laf )
    {
        return quickGlimpseCanvas( GLProfile.get( glProfileName ), layout, laf );
    }

    public static NewtSwingEDTGlimpseCanvas quickGlimpseCanvas( GLProfile glProfile, GlimpseLayout layout )
    {
        return quickGlimpseCanvas( glProfile, layout, quickDefaultLaf( ) );
    }

    public static NewtSwingEDTGlimpseCanvas quickGlimpseCanvas( GLProfile glProfile, GlimpseLayout layout, LookAndFeel laf )
    {
        return quickGlimpseCanvas( glProfile, layout, laf, quickDefaultAnimator( ) );
    }

    public static NewtSwingEDTGlimpseCanvas quickGlimpseCanvas( GLContext glContext, GlimpseLayout layout )
    {
        return quickGlimpseCanvas( glContext, layout, quickDefaultLaf( ) );
    }

    public static NewtSwingEDTGlimpseCanvas quickGlimpseCanvas( GLContext glContext, GlimpseLayout layout, LookAndFeel laf )
    {
        return quickGlimpseCanvas( glContext, layout, laf, quickDefaultAnimator( ) );
    }

    public static NewtSwingEDTGlimpseCanvas quickGlimpseCanvas( GLProfile glProfile, GlimpseLayout layout, LookAndFeel laf, GLAnimatorControl animator )
    {
        NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( glProfile );
        initGlimpseCanvas( canvas, layout, laf, animator );
        return canvas;
    }

    public static NewtSwingEDTGlimpseCanvas quickGlimpseCanvas( GLContext glContext, GlimpseLayout layout, LookAndFeel laf, GLAnimatorControl animator )
    {
        NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( glContext );
        initGlimpseCanvas( canvas, layout, laf, animator );
        return canvas;
    }

    public static void initGlimpseCanvas( NewtSwingEDTGlimpseCanvas canvas, GlimpseLayout layout, LookAndFeel laf, GLAnimatorControl animator )
    {
        requireSwingThread( );

        canvas.addLayout( layout );

        // setLaf() only affects existing contents, so call it AFTER adding everything
        canvas.setLookAndFeel( laf );

        animator.add( canvas.getGLDrawable( ) );
        animator.start( );
    }

    public static JFrame quickGlimpseWindow( String title, NewtSwingEDTGlimpseCanvas canvas )
    {
        return quickGlimpseWindow( title, canvas, quickDefaultSize( ) );
    }

    public static JFrame quickGlimpseWindow( String title, NewtSwingEDTGlimpseCanvas canvas, int width, int height )
    {
        return quickGlimpseWindow( title, canvas, new Dimension( width, height ) );
    }

    public static JFrame quickGlimpseWindow( String title, NewtSwingEDTGlimpseCanvas canvas, Dimension size )
    {
        requireSwingThread( );

        JFrame frame = new JFrame( );
        frame.setTitle( title );

        // This listener must run before NewtCanvasAWT's built-in window-closing
        // listener does -- so add it before we add the canvas to the frame
        onWindowClosing( frame, ev ->
        {
            // FIXME: Should we call canvas.disposeAttached() here?
            canvas.destroy( );
        } );

        frame.getContentPane( ).add( canvas );
        frame.setSize( size );
        frame.setLocationRelativeTo( null );
        frame.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        frame.setVisible( true );

        return frame;
    }

}

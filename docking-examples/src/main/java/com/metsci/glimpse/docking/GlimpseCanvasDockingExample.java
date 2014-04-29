package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingPane.Arrangement.*;
import static com.metsci.glimpse.docking.DockingThemes.*;
import static com.metsci.glimpse.docking.DockingUtils.*;
import static java.util.logging.Level.*;
import static javax.swing.JFrame.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.logging.Logger;

import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.tinylaf.Theme;
import net.sf.tinylaf.TinyLookAndFeel;

import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.docking.DockingPane.Arrangement.ArrangementNode;
import com.metsci.glimpse.examples.basic.HeatMapExample;
import com.metsci.glimpse.examples.basic.ScatterplotExample;
import com.metsci.glimpse.gl.util.GLUtils;

/**
 * Example demonstrating using the glimpse-docking framework with two GlimpseCanvas.
 * 
 * @author ulman
 */
public class GlimpseCanvasDockingExample
{
    protected static final Logger logger = Logger.getLogger( GlimpseCanvasDockingExample.class.getName( ) );

    public static void main( String[] args ) throws Exception
    {
        Theme.loadTheme( SimpleDockingExample.class.getClassLoader( ).getResource( "tinylaf/radiance.theme" ) );
        UIManager.setLookAndFeel( new TinyLookAndFeel( ) );
        DockingTheme dockingTheme = tinyLafDockingTheme( );

        final JPanel aPanel = new JPanel( );
        aPanel.setLayout( new BorderLayout( ) );
        
        final JPanel bPanel = new JPanel( );
        bPanel.setLayout( new BorderLayout( ) );
        
        // set up two docking panes
        final DockingPane dockingPane = new DockingPane( dockingTheme );
        dockingPane.addView( new View( "aView", "View A", requireIcon( "icons/ViewA.png" ), null, aPanel, null ) );
        dockingPane.addView( new View( "bView", "View B", requireIcon( "icons/ViewB.png" ), null, bPanel, null ) );

        final JFrame frame = new JFrame( "Docking Example" );
        frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
        
        // create a shared OpenGL context
        final GLOffscreenAutoDrawable glDrawable = GLUtils.newOffscreenDrawable( );
        
        // create two GlimpseCanvas
        final NewtSwingGlimpseCanvas aCanvas = new NewtSwingGlimpseCanvas( glDrawable.getContext( ) );
        aCanvas.addLayout( new HeatMapExample( ).getLayout( ) );
        
        final NewtSwingGlimpseCanvas bCanvas = new NewtSwingGlimpseCanvas( glDrawable.getContext( ) );
        bCanvas.addLayout( new ScatterplotExample( ).getLayout( ) );
        
        // add the GlimpseCanvas to an animator so they are repainted
        FPSAnimator animator = new FPSAnimator( 60 );
        animator.add( aCanvas.getGLDrawable( ) );
        animator.add( bCanvas.getGLDrawable( ) );
        animator.start( );

        // add the GlimpseCanvas to the docking pane and the docking pane to the JFrame
        SwingUtilities.invokeAndWait( new Runnable( )
        {
            @Override
            public void run( )
            {
                aPanel.add( aCanvas, BorderLayout.CENTER );
                bPanel.add( bCanvas, BorderLayout.CENTER );
                
                frame.setContentPane( dockingPane );
            
                frame.setPreferredSize( new Dimension( 1600, 900 ) );
                frame.pack( );
            }
        } );

        // restore the saved arrangement of docking panes
        swingRun( dockingPane.restoreArrangement, loadDockingArrangement( "glimpse-docking-example" ) );

        // save the arrangement of docking panes when the window closes
        frame.addWindowListener( new WindowAdapter( )
        {
            public void windowClosing( WindowEvent ev )
            {
                saveDockingArrangement( "glimpse-docking-example", dockingPane.captureArrangement( ) );
            }
        } );

        frame.setVisible( true );
    }

    public static void saveDockingArrangement( String appName, ArrangementNode arrangement )
    {
        try
        {
            File arrangementFile = new File( createAppDir( appName ), "arrangement.xml" );
            writeDockingArrangementXml( arrangement, arrangementFile );
        }
        catch ( Exception e )
        {
            logger.log( WARNING, "Failed to write docking arrangement to file", e );
        }
    }

    public static ArrangementNode loadDockingArrangement( String appName )
    {
        try
        {
            File arrangementFile = new File( createAppDir( appName ), "arrangement.xml" );
            if ( arrangementFile.exists( ) )
            {
                return readDockingArrangementXml( arrangementFile );
            }
        }
        catch ( Exception e )
        {
            logger.log( WARNING, "Failed to load docking arrangement from file", e );
        }

        try
        {
            return readDockingArrangementXml( SimpleDockingExample.class.getClassLoader( ).getResourceAsStream( "docking/glimpse-arrangement-default.xml" ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}
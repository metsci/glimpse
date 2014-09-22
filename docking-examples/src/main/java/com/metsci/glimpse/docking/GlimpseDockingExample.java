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

import static com.metsci.glimpse.docking.DockingGroup.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingThemes.tinyLafDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.createAppDir;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.swingRun;
import static com.metsci.glimpse.docking.DockingXmlUtils.readArrangementXml;
import static com.metsci.glimpse.docking.DockingXmlUtils.writeArrangementXml;
import static com.metsci.glimpse.docking.DockingFrameTitlers.createDefaultFrameTitler;
import static com.metsci.glimpse.gl.util.GLUtils.newOffscreenDrawable;
import static com.metsci.glimpse.platformFixes.PlatformFixes.fixPlatformQuirks;
import static com.metsci.glimpse.support.colormap.ColorGradients.greenBone;
import static com.metsci.glimpse.support.colormap.ColorGradients.jet;
import static java.util.logging.Level.WARNING;

import java.io.File;
import java.util.logging.Logger;

import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.swing.UIManager;

import net.sf.tinylaf.Theme;
import net.sf.tinylaf.TinyLookAndFeel;

import com.jogamp.opengl.util.FPSAnimator;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.docking.DockingGroup.DockingGroupAdapter;
import com.metsci.glimpse.docking.DockingThemes.DockingTheme;
import com.metsci.glimpse.docking.TileFactories.TileFactory;
import com.metsci.glimpse.docking.TileFactories.TileFactoryStandard;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.examples.basic.TaggedHeatMapExample;

public class GlimpseDockingExample
{
    protected static final Logger logger = Logger.getLogger( GlimpseDockingExample.class.getName( ) );


    public static void main( String[] args ) throws Exception
    {
        fixPlatformQuirks( );

        Theme.loadTheme( GlimpseDockingExample.class.getClassLoader( ).getResource( "tinylaf/radiance.theme" ) );
        UIManager.setLookAndFeel( new TinyLookAndFeel( ) );
        DockingTheme dockingTheme = tinyLafDockingTheme( );


        final String appName = "glimpse-docking-example";
        final DockingGroup dockingGroup = new DockingGroup( dockingTheme, DISPOSE_ALL_FRAMES );
        dockingGroup.addListener( createDefaultFrameTitler( "Docking Example" ) );
        final TileFactory tileFactory = new TileFactoryStandard( dockingGroup );


        GLOffscreenAutoDrawable glDrawable = newOffscreenDrawable( );

        NewtSwingGlimpseCanvas aCanvas = new NewtSwingGlimpseCanvas( glDrawable.getContext( ) );
        aCanvas.addLayout( new TaggedHeatMapExample( ).getLayout( greenBone ) );

        NewtSwingGlimpseCanvas bCanvas = new NewtSwingGlimpseCanvas( glDrawable.getContext( ) );
        bCanvas.addLayout( new TaggedHeatMapExample( ).getLayout( jet ) );

        final FPSAnimator animator = new FPSAnimator( 30 );
        animator.add( aCanvas.getGLDrawable( ) );
        animator.add( bCanvas.getGLDrawable( ) );
        animator.start( );


        final View[] views =
        {
            new View( "aView", aCanvas, "View A", false, null, requireIcon( "icons/ViewA.png" ) ),
            new View( "bView", bCanvas, "View B", false, null, requireIcon( "icons/ViewB.png" ) )
        };


        // Certain components are picky about being added to a frame from the Swing thread
        // (e.g. NewtCanvasAWT, which otherwise crashes the JVM when removed). It's a good
        // idea to call dockingGroup.restoreArrangement() on the Swing thread, whether you
        // are using such picky components or not.
        //
        swingRun( new Runnable( )
        {
            public void run( )
            {
                GroupArrangement groupArr = loadDockingArrangement( appName );
                dockingGroup.restoreArrangement( groupArr, tileFactory, views );
                dockingGroup.addListener( new DockingGroupAdapter( )
                {
                    public void disposingAllFrames( DockingGroup group )
                    {
                        saveDockingArrangement( appName, dockingGroup.captureArrangement( ) );
                        animator.stop( );
                    }
                } );
            }
        } );
    }


    public static void saveDockingArrangement( String appName, GroupArrangement groupArr )
    {
        try
        {
            File arrFile = new File( createAppDir( appName ), "arrangement.xml" );
            writeArrangementXml( groupArr, arrFile );
        }
        catch ( Exception e )
        {
            logger.log( WARNING, "Failed to write docking arrangement to file", e );
        }
    }


    public static GroupArrangement loadDockingArrangement( String appName )
    {
        try
        {
            File arrFile = new File( createAppDir( appName ), "arrangement.xml" );
            if ( arrFile.exists( ) )
            {
                return readArrangementXml( arrFile );
            }
        }
        catch ( Exception e )
        {
            logger.log( WARNING, "Failed to load docking arrangement from file", e );
        }

        try
        {
            return readArrangementXml( GlimpseDockingExample.class.getClassLoader( ).getResourceAsStream( "docking/glimpse-arrangement-default.xml" ) );
        }
        catch ( Exception e )
        {
            logger.log( WARNING, "Failed to load default docking arrangement from resource", e );
        }

        return null;
    }

}

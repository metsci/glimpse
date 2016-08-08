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

import static com.metsci.glimpse.docking.DockingFrameTitlers.createDefaultFrameTitler;
import static com.metsci.glimpse.docking.DockingGroup.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingThemes.tinyLafDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.loadDockingArrangement;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.saveDockingArrangement;
import static com.metsci.glimpse.docking.DockingUtils.swingRun;
import static com.metsci.glimpse.gl.util.GLUtils.newOffscreenDrawable;
import static com.metsci.glimpse.platformFixes.PlatformFixes.fixPlatformQuirks;
import static com.metsci.glimpse.support.colormap.ColorGradients.greenBone;
import static com.metsci.glimpse.support.colormap.ColorGradients.jet;

import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.swing.UIManager;

import com.metsci.glimpse.docking.DockingGroup.DockingGroupAdapter;
import com.metsci.glimpse.docking.DockingThemes.DockingTheme;
import com.metsci.glimpse.docking.TileFactories.TileFactory;
import com.metsci.glimpse.docking.TileFactories.TileFactoryStandard;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.examples.basic.TaggedHeatMapExample;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

import net.sf.tinylaf.Theme;
import net.sf.tinylaf.TinyLookAndFeel;

public class GlimpseDockingExample
{

    public static void main( String[] args ) throws Exception
    {
        fixPlatformQuirks( );
        Theme.loadTheme( GlimpseDockingExample.class.getClassLoader( ).getResource( "tinylaf/radiance.theme" ) );
        UIManager.setLookAndFeel( new TinyLookAndFeel( ) );
        final DockingTheme dockingTheme = tinyLafDockingTheme( );

        // Initialize the GUI on the Swing thread, to avoid graphics-driver coredumps on shutdown
        swingRun( new Runnable( )
        {
            public void run( )
            {

                // Create view components
                //

                GLOffscreenAutoDrawable glDrawable = newOffscreenDrawable( );

                NewtSwingEDTGlimpseCanvas aCanvas = new NewtSwingEDTGlimpseCanvas( glDrawable.getContext( ) );
                aCanvas.addLayout( new TaggedHeatMapExample( ).getLayout( greenBone ) );

                NewtSwingEDTGlimpseCanvas bCanvas = new NewtSwingEDTGlimpseCanvas( glDrawable.getContext( ) );
                bCanvas.addLayout( new TaggedHeatMapExample( ).getLayout( jet ) );

                final SwingEDTAnimator glAnimator = new SwingEDTAnimator( 30 );
                glAnimator.add( aCanvas.getGLDrawable( ) );
                glAnimator.add( bCanvas.getGLDrawable( ) );
                glAnimator.start( );


                // Create views
                //

                View[] views = { new View( "aView", aCanvas, "View A", false, null, requireIcon( "icons/ViewA.png" ) ),
                                 new View( "bView", bCanvas, "View B", false, null, requireIcon( "icons/ViewB.png" ) ) };


                // Create and show the docking group
                //

                final String appName = "glimpse-docking-example";
                final DockingGroup dockingGroup = new DockingGroup( dockingTheme, DISPOSE_ALL_FRAMES );
                dockingGroup.addListener( createDefaultFrameTitler( "Docking Example" ) );
                TileFactory tileFactory = new TileFactoryStandard( dockingGroup );

                GroupArrangement groupArr = loadDockingArrangement( appName, GlimpseDockingExample.class.getClassLoader( ).getResource( "docking/glimpse-arrangement-default.xml" ) );
                dockingGroup.restoreArrangement( groupArr, tileFactory, views );
                dockingGroup.addListener( new DockingGroupAdapter( )
                {
                    public void disposingAllFrames( DockingGroup group )
                    {
                        saveDockingArrangement( appName, dockingGroup.captureArrangement( ) );
                        glAnimator.stop( );
                    }
                } );

            }
        } );
    }

}

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
package com.metsci.glimpse.docking.examples;

import static com.metsci.glimpse.docking.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.setArrangementAndSaveOnDispose;
import static com.metsci.glimpse.docking.DockingWindowTitlers.createDefaultWindowTitler;
import static com.metsci.glimpse.docking.ViewCloseOption.VIEW_NOT_CLOSEABLE;
import static com.metsci.glimpse.gl.util.GLUtils.newOffscreenDrawable;
import static com.metsci.glimpse.support.QuickUtils.initStandardGlimpseApp;
import static com.metsci.glimpse.support.colormap.ColorGradients.greenBone;
import static com.metsci.glimpse.support.colormap.ColorGradients.jet;
import static com.metsci.glimpse.tinylaf.TinyLafUtils.initTinyLaf;

import javax.swing.SwingUtilities;

import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingGroupAdapter;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.group.frame.DockingGroupMultiframe;
import com.metsci.glimpse.examples.heatmap.TaggedHeatMapExample;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class GlimpseDockingExample
{

    public static void main( String[] args ) throws Exception
    {
        // Initialize the GUI on the Swing thread, to avoid graphics-driver coredumps on shutdown
        SwingUtilities.invokeLater( ( ) ->
        {
            initTinyLaf( );
            initStandardGlimpseApp( );


            // Create view components
            //

            GLOffscreenAutoDrawable glDrawable = newOffscreenDrawable( );

            NewtSwingEDTGlimpseCanvas aCanvas = new NewtSwingEDTGlimpseCanvas( glDrawable.getContext( ) );
            aCanvas.addLayout( TaggedHeatMapExample.newPlot( greenBone ) );

            NewtSwingEDTGlimpseCanvas bCanvas = new NewtSwingEDTGlimpseCanvas( glDrawable.getContext( ) );
            bCanvas.addLayout( TaggedHeatMapExample.newPlot( jet ) );

            SwingEDTAnimator glAnimator = new SwingEDTAnimator( 30 );
            glAnimator.add( aCanvas.getGLDrawable( ) );
            glAnimator.add( bCanvas.getGLDrawable( ) );
            glAnimator.start( );


            // Create views
            //

            View[] views =
            {
                new View( "aView", aCanvas, "View A", VIEW_NOT_CLOSEABLE, null, requireIcon( GlimpseDockingExample.class.getResource( "icons/ViewA.png" ) ) ),
                new View( "bView", bCanvas, "View B", VIEW_NOT_CLOSEABLE, null, requireIcon( GlimpseDockingExample.class.getResource( "icons/ViewB.png" ) ) )
            };


            // Create and show the docking group
            //

            String appName = "glimpse-docking-example";
            DockingGroup dockingGroup = new DockingGroupMultiframe( DISPOSE_ALL_FRAMES );
            dockingGroup.addListener( createDefaultWindowTitler( "Docking Example" ) );
            setArrangementAndSaveOnDispose( dockingGroup, appName, GlimpseDockingExample.class.getResource( "docking/glimpse-arrangement-default.xml" ) );

            dockingGroup.addListener( new DockingGroupAdapter( )
            {
                @Override
                public void disposingAllWindows( DockingGroup group )
                {
                    aCanvas.destroy( );
                    bCanvas.destroy( );
                }
            } );

            dockingGroup.addViews( views );
            dockingGroup.setVisible( true );

        } );
    }

}

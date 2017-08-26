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
package com.metsci.glimpse.examples.dnc;

import static com.metsci.glimpse.dnc.DncDataPaths.glimpseDncFlatDir;
import static com.metsci.glimpse.dnc.DncProjections.dncTangentPlane;
import static com.metsci.glimpse.dnc.geosym.DncGeosymThemes.DNC_THEME_STANDARD;
import static com.metsci.glimpse.support.FrameUtils.disposeOnWindowClosing;
import static com.metsci.glimpse.support.FrameUtils.newFrame;
import static com.metsci.glimpse.support.FrameUtils.showFrameCentered;
import static com.metsci.glimpse.support.FrameUtils.stopOnWindowClosing;
import static com.metsci.glimpse.util.GlimpseDataPaths.requireExistingDir;
import static com.metsci.glimpse.util.logging.LoggerUtils.initializeLogging;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.io.IOException;

import com.jogamp.opengl.GLAnimatorControl;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.dnc.DncPainter;
import com.metsci.glimpse.dnc.DncPainterSettings;
import com.metsci.glimpse.dnc.DncPainterSettingsImpl;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCache;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCacheConfig;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.info.FpsPainter;
import com.metsci.glimpse.plot.Plot2D;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class DncPainterExample
{

    public static void main( String[] args ) throws IOException
    {
        initializeLogging( "dnc-examples/logging.properties" );



        // Render config
        //

        RenderCacheConfig renderConfig = new RenderCacheConfig( );
        renderConfig.flatParentDir = requireExistingDir( glimpseDncFlatDir );
        renderConfig.proj = dncTangentPlane( 40.6892, -74.0444 ); // New York

        RenderCache renderCache = new RenderCache( renderConfig, 4 );



        // Create plot
        //

        Plot2D plot = new Plot2D( "" );
        plot.lockAspectRatioXY( 1 );
        plot.setShowMinorTicksX( true );
        plot.setShowMinorTicksY( true );

        DncPainterSettings dncPainterSettings = new DncPainterSettingsImpl( renderConfig.proj );
        DncPainter dncPainter = new DncPainter( renderCache, dncPainterSettings, DNC_THEME_STANDARD );
        dncPainter.activateCoverages( renderCache.coverages );
        dncPainter.addAxis( plot.getAxis( ) );

        plot.getLayoutCenter( ).addPainter( dncPainter );
        plot.getLayoutCenter( ).addPainter( new FpsPainter( ) );
        plot.getLayoutCenter( ).addPainter( new BorderPainter( ) );



        // Show
        //

        SwingUtilities.invokeLater( ( ) ->
        {
            NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( );
            canvas.addLayout( plot );
            canvas.setLookAndFeel( new SwingLookAndFeel( ) );

            GLAnimatorControl animator = new SwingEDTAnimator( 30 );
            animator.add( canvas.getGLDrawable( ) );
            animator.start( );

            JFrame frame = newFrame( "DNC Example", canvas, DISPOSE_ON_CLOSE );
            stopOnWindowClosing( frame, animator );
            disposeOnWindowClosing( frame, canvas );
            showFrameCentered( frame );
        } );
    }

}

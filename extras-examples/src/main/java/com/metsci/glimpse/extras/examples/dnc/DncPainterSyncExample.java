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
package com.metsci.glimpse.extras.examples.dnc;

import static com.jogamp.opengl.GLProfile.GL3;
import static com.metsci.glimpse.core.support.QuickUtils.swingInvokeLater;
import static com.metsci.glimpse.dnc.DncDataPaths.glimpseDncFlatDir;
import static com.metsci.glimpse.dnc.DncProjections.dncTangentPlane;
import static com.metsci.glimpse.dnc.geosym.DncGeosymThemes.DNC_THEME_STANDARD;
import static com.metsci.glimpse.util.GlimpseDataPaths.requireExistingDir;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.initLogging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jogamp.opengl.GLProfile;
import com.metsci.glimpse.core.canvas.FBOGlimpseCanvas;
import com.metsci.glimpse.core.painter.decoration.BorderPainter;
import com.metsci.glimpse.core.plot.Plot2D;
import com.metsci.glimpse.core.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.dnc.DncPainter;
import com.metsci.glimpse.dnc.DncPainterSettings;
import com.metsci.glimpse.dnc.DncPainterSettingsImpl;
import com.metsci.glimpse.dnc.DncPainterSync;
import com.metsci.glimpse.dnc.DncPainterSync.RenderCacheSync;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCacheConfig;

/**
 * Renders DNC charts to an image file.
 */
public class DncPainterSyncExample
{
    private static final Logger logger = getLogger( DncPainterSyncExample.class );

    public static void main( String[] args )
    {
        initLogging( DncPainterSyncExample.class.getResource( "logging.properties" ) );
        swingInvokeLater( ( ) ->
        {
            // Render config
            //

            RenderCacheConfig renderConfig = new RenderCacheConfig( );
            renderConfig.flatParentDir = requireExistingDir( glimpseDncFlatDir );
            renderConfig.proj = dncTangentPlane( 40.6892, -74.0444 ); // New York

            RenderCacheSync renderCache = new RenderCacheSync( renderConfig );


            // Create plot
            //

            Plot2D plot = new Plot2D( "" );
            plot.getAxis( ).set( -10000, +110000, -110000, 0 );
            plot.lockAspectRatioXY( 1 );
            plot.setShowMinorTicksX( true );
            plot.setShowMinorTicksY( true );

            DncPainterSettings dncPainterSettings = new DncPainterSettingsImpl( renderConfig.proj );
            DncPainter dncPainter = new DncPainterSync( renderCache, dncPainterSettings, DNC_THEME_STANDARD );
            dncPainter.activateCoverages( renderCache.coverages );
            dncPainter.addAxis( plot.getAxis( ) );

            plot.getLayoutCenter( ).addPainter( dncPainter );
            plot.getLayoutCenter( ).addPainter( new BorderPainter( ) );


            // Create image
            //

            FBOGlimpseCanvas canvas = new FBOGlimpseCanvas( GLProfile.get( GL3 ), 1000, 1000 );
            canvas.addLayout( plot );
            canvas.setLookAndFeel( new SwingLookAndFeel( ) );
            BufferedImage image = canvas.toBufferedImage( );


            // Write image to file
            //

            File outFile = new File( "DncPainterSyncExample.png" );
            ImageIO.write( image, "PNG", outFile );
            logger.info( "Wrote image to " + outFile.getAbsolutePath( ) );
        } );
    }

}

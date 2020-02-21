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
package com.metsci.glimpse.extras.examples.topo;

import static com.jogamp.opengl.GLProfile.GL3;
import static com.metsci.glimpse.core.axis.UpdateMode.CenterScale;
import static com.metsci.glimpse.core.support.FrameUtils.screenFracSize;
import static com.metsci.glimpse.core.support.QuickUtils.quickGlimpseApp;
import static com.metsci.glimpse.topo.io.TopoCache.topoCacheDataset;
import static com.metsci.glimpse.topo.io.TopoDataPaths.requireTopoDataFile;
import static com.metsci.glimpse.topo.io.TopoReader.readTopoLevel;
import static com.metsci.glimpse.topo.proj.MercatorNormalCylindricalProjection.standardMercatorProj;
import static com.metsci.glimpse.util.GeneralUtils.floats;
import static com.metsci.glimpse.util.GeneralUtils.require;
import static com.metsci.glimpse.util.logging.LoggerUtils.initLogging;
import static com.metsci.glimpse.util.math.MathConstants.HALF_PI;
import static java.lang.Math.PI;

import java.io.File;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.axis.WrappedAxis1D;
import com.metsci.glimpse.core.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.core.painter.decoration.BorderPainter;
import com.metsci.glimpse.core.painter.group.WrappedPainter;
import com.metsci.glimpse.core.painter.info.FpsPainter;
import com.metsci.glimpse.core.plot.MultiAxisPlot2D;
import com.metsci.glimpse.topo.MercatorTopoPainter;
import com.metsci.glimpse.topo.io.TopoDataFile;
import com.metsci.glimpse.topo.io.TopoDataset;

public class MercatorTopoPainterExample
{

    public static void main( String[] args )
    {
        initLogging( MercatorTopoPainterExample.class.getResource( "logging.properties" ) );
        SwingUtilities.invokeLater( ( ) ->
        {
            MultiAxisPlot2D plot = new MultiAxisPlot2D( )
            {
                @Override
                protected void initializeCenterAxis( )
                {
                    this.centerAxisX = new WrappedAxis1D( -PI, +PI );
                    this.centerAxisY = new Axis1D( );
                }
            };

            plot.setShowTitle( false );
            plot.setBorderSize( 5 );

            Axis2D axis = plot.getCenterAxis( );
            axis.lockAspectRatioXY( 1.0 );
            axis.getAxisX( ).setUpdateMode( CenterScale );
            axis.getAxisY( ).setUpdateMode( CenterScale );
            axis.set( -PI, +PI, -HALF_PI, +HALF_PI );
            axis.validate( );

            BackgroundPainter backgroundPainter = new BackgroundPainter( );
            backgroundPainter.setColor( floats( 0.7f, 0.7f, 0.7f, 1 ) );

            File topoDataFile = requireTopoDataFile( );
            TopoDataFile topoBase = require( ( ) -> readTopoLevel( topoDataFile ) );
            TopoDataset topoDataset = require( ( ) -> topoCacheDataset( topoBase ) );
            MercatorTopoPainter topoPainter = new MercatorTopoPainter( topoDataset, standardMercatorProj );

            WrappedPainter wrappedPainter = new WrappedPainter( true );
            wrappedPainter.addPainter( backgroundPainter );
            wrappedPainter.addPainter( topoPainter );

            plot.getLayoutCenter( ).addPainter( wrappedPainter );
            plot.getLayoutCenter( ).addPainter( new FpsPainter( ) );
            plot.getLayoutCenter( ).addPainter( new BorderPainter( ) );

            quickGlimpseApp( "MercatorTopoPainterExample", GL3, plot, screenFracSize( 0.8 ) );
        } );
    }

}

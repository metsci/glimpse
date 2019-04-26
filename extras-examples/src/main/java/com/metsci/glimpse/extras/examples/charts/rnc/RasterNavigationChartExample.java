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
package com.metsci.glimpse.extras.examples.charts.rnc;

import java.io.IOException;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.charts.raster.BsbRasterData;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.info.CursorTextZPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.shader.colormap.ColorMapIntegerProgram;
import com.metsci.glimpse.support.texture.ByteTextureProjected2D;
import com.metsci.glimpse.util.geo.projection.MercatorProjection;
import com.metsci.glimpse.util.io.StreamOpener;

/**
 * Glimpse has preliminary support for displaying Electronic Navigation Chart
 * raster images available in the BSB Raster format directly from NOAA.<p>
 *
 * Chart files which can be directly loaded and displayed by Glimpse are
 * available from the following website: http://www.charts.noaa.gov/RNCs/RNCs.shtml<p>
 *
 * Note that currently Glimpse does not automatically project the image correctly
 * (it is being displayed here as a flat image). This capability is in development.
 *
 * @author ulman
 * @see com.metsci.glimpse.charts.raster.BsbRasterData
 */
public class RasterNavigationChartExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new RasterNavigationChartExample( ), GLUtils.getDefaultGLProfile( ) );
    }

    protected int plotHeight = 200;
    protected int plotWidth = 200;

    protected double plotMinX = 0.0;
    protected double plotMinY = 0.0;

    @Override
    public ColorAxisPlot2D getLayout( )
    {
        final ColorAxisPlot2D plot = new ColorAxisPlot2D( );

        // create a color scale axis for the heat maps created below
        Axis1D colorAxis = new Axis1D( );
        colorAxis.setMin( 0.0 );
        colorAxis.setMax( 1000.0 );

        plot.getCrosshairPainter( ).showSelectionCrosshairs( false );

        ShadedTexturePainter painter = new ShadedTexturePainter( );
        plot.addPainter( painter );

        // hide axes
        plot.setTitleHeight( 0 );
        plot.setAxisSizeX( 0 );
        plot.setAxisSizeY( 0 );
        plot.setAxisSizeZ( 0 );

        BsbRasterData data;
        MercatorProjection mercatorProjection;
        try
        {
            ColorMapIntegerProgram fragShader = new ColorMapIntegerProgram( plot.getAxisZ( ), 0, 1 );
            painter.setProgram( fragShader );

            data = BsbRasterData.readImage( StreamOpener.fileThenResource.openForRead( "data/ENCSample.bsb" ) );
            mercatorProjection = new MercatorProjection( );
        }
        catch ( IOException e )
        {
            e.printStackTrace( );
            throw new RuntimeException( e );
        }

        ByteTextureProjected2D dataTexture = data.getDataTexture( );
        Projection textureProjection = data.getProjection( mercatorProjection );
        dataTexture.setProjection( textureProjection );

        ColorTexture1D colorTexture = data.getColorTexture( );
        plot.setColorScale( colorTexture );

        painter.addDrawableTexture( dataTexture, 0 );
        painter.addNonDrawableTexture( colorTexture, 1 );

        plot.lockAspectRatioXY( 1.0 );

        plot.setMinZ( 0.0 );
        plot.setMaxZ( colorTexture.getDimensionSize( 0 ) );

        CursorTextZPainter z = new CursorTextZPainter( );
        z.setOffsetBySelectionSize( false );
        z.setTexture( dataTexture );
        plot.addPainter( z );

        float[] xy00 = new float[2];
        float[] xy11 = new float[2];
        textureProjection.getVertexXY( 0, 0, xy00 );
        textureProjection.getVertexXY( 1, 1, xy11 );

        plot.setMinX( Math.min( xy00[0], xy11[0] ) );
        plot.setMaxX( Math.max( xy00[0], xy11[0] ) );
        plot.setMinY( Math.min( xy00[1], xy11[1] ) );
        plot.setMaxY( Math.max( xy00[1], xy11[1] ) );

        return plot;
    }
}

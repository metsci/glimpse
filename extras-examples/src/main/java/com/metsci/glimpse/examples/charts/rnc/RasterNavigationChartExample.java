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
package com.metsci.glimpse.examples.charts.rnc;

import java.io.IOException;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.axis.painter.NumericAxisPainter;
import com.metsci.glimpse.axis.painter.NumericRotatedYAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.charts.raster.BsbRasterData;
import com.metsci.glimpse.charts.vector.MercatorProjection;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.examples.basic.LinePlotExample;
import com.metsci.glimpse.gl.shader.Pipeline;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.LegendPainter.LegendPlacement;
import com.metsci.glimpse.painter.decoration.LegendPainter.LineLegendPainter;
import com.metsci.glimpse.painter.plot.XYLinePainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.plot.SimplePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.shader.SampledColorScaleShaderInteger;
import com.metsci.glimpse.support.texture.ByteTextureProjected2D;
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
        Example.showWithSwing( new RasterNavigationChartExample( ) );
    }

    protected int plotHeight = 200;
    protected int plotWidth = 200;

    protected double plotMinX = 0.0;
    protected double plotMinY = 0.0;
    
    @Override
    public ColorAxisPlot2D getLayout( )
    {
        final ColorAxisPlot2D plot = new ColorAxisPlot2D( );

        // create a new plot for the floating layout area
        // it uses a vertical text orientation for its y axis painter to save space
        final SimplePlot2D floatingPlot = new SimplePlot2D( )
        {
            @Override
            protected NumericAxisPainter createAxisPainterY( AxisLabelHandler tickHandler )
            {
                return new NumericRotatedYAxisPainter( tickHandler );
            }
        };

        // add an axis listener which adjusts the position of the floating layout painter as the axis changes
        // (the layout painter is tied to a fixed axis value)
        plot.addAxisListener( new AxisListener2D( )
        {
            @Override
            public void axisUpdated( Axis2D axis )
            {
                int minX = plot.getAxisX( ).valueToScreenPixel( plotMinX );
                int minY = plot.getAxisY( ).valueToScreenPixel( plotMinY );

                floatingPlot.setLayoutData( String.format( "pos %d %d %d %d", minX, minY, minX + plotWidth, minY + plotHeight ) );
                plot.invalidateLayout( );
            }
        } );

        // add a mouse listener which listens for middle mouse button (mouse wheel) clicks
        // and moves the floating plot in response
        plot.getLayoutCenter( ).addGlimpseMouseListener( new GlimpseMouseListener( )
        {
            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                if ( event.isButtonDown( MouseButton.Button2 ) )
                {
                    plotMinX = plot.getAxisX( ).screenPixelToValue( event.getX( ) );
                    plotMinY = plot.getAxisY( ).screenPixelToValue( plot.getAxisY( ).getSizePixels( ) - event.getY( ) );
                }
            }

            @Override
            public void mouseEntered( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseExited( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent event )
            {
            }
        } );

        // the floating plot is quite small, so use a smaller font, tighter tick spacing, and smaller bounds for the axes
        floatingPlot.setAxisFont( FontUtils.getSilkscreen( ), false );
        floatingPlot.setAxisSizeX( 25 );
        floatingPlot.setAxisSizeY( 25 );
        floatingPlot.setTickSpacingX( 35 );
        floatingPlot.setTickSpacingY( 35 );
        floatingPlot.setBorderSize( 4 );

        // don't show crosshairs in the floating plot or the main plot
        floatingPlot.getCrosshairPainter( ).setVisible( false );
        plot.getCrosshairPainter( ).setVisible( false );

        // don't provide any space for a title in the floating plot
        floatingPlot.setTitleHeight( 0 );

        // add a border to the outside of the floating plot
        floatingPlot.addPainterOuter( new BorderPainter( ).setLineWidth( 4 ) );

        // create a color scale axis for the heat maps created below
        Axis1D colorAxis = new Axis1D( );
        colorAxis.setMin( 0.0 );
        colorAxis.setMax( 1000.0 );

//        // add a heat map painter to the floating plot
//        floatingPlot.addPainter( HeatMapExample.newHeatMapPainter( colorAxis ) );
//        floatingPlot.getAxis( ).set( 0, 1000, 0, 1000 );
//
//        // add a heat map painter to the outer plot
//        plot.addPainter( HeatMapExample.newHeatMapPainter( colorAxis ) );
//        plot.getAxis( ).set( 0, 1000, 0, 1000 );

        
        
        // creating a data series painter, passing it the lineplot frame
        // this constructor will have the painter draw according to the lineplot x and y axis
        XYLinePainter series1 = LinePlotExample.createXYLinePainter1( );
        floatingPlot.addPainter( series1 );

        // in order for our second data series to use the right hand
        // axis as its y axis, we must manually specify the axes which it should use
        XYLinePainter series2 = LinePlotExample.createXYLinePainter2( );
        floatingPlot.addPainter( series2 );

        LineLegendPainter legend = new LineLegendPainter( LegendPlacement.SE );

        //Move the legend further away from the right side;
        legend.setOffsetY( 10 );
        legend.setOffsetX( 10 );
        legend.addItem( "Series 1", GlimpseColor.fromColorRgba( 1.0f, 0.0f, 0.0f, 0.8f ) );
        legend.addItem( "Series 2", GlimpseColor.fromColorRgba( 0.0f, 0.0f, 1.0f, 0.8f ), true );
        
        plot.addPainter( legend );
        
        
  
        
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
            SampledColorScaleShaderInteger fragShader = new SampledColorScaleShaderInteger( plot.getAxisZ( ), 0, 1 );
            painter.setPipeline( new Pipeline( "colormap", null, null, fragShader ) );

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

        float[] xy00 = new float[2];
        float[] xy11 = new float[2];
        textureProjection.getVertexXY( 0, 0, xy00 );
        textureProjection.getVertexXY( 1, 1, xy11 );

        plot.setMinX( Math.min( xy00[0], xy11[0] ) );
        plot.setMaxX( Math.max( xy00[0], xy11[0] ) );
        plot.setMinY( Math.min( xy00[1], xy11[1] ) );
        plot.setMaxY( Math.max( xy00[1], xy11[1] ) );
        
        
        // add the floating plot to the main plot
        plot.getLayoutCenter( ).addLayout( floatingPlot );
        plot.getLayoutCenter( ).invalidateLayout( );

        return plot;
    }
}

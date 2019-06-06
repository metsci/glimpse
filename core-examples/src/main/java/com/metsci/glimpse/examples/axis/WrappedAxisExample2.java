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
package com.metsci.glimpse.examples.axis;

import static com.jogamp.opengl.GLProfile.GL3;
import static com.metsci.glimpse.axis.tagged.Tag.TEX_COORD_ATTR;
import static com.metsci.glimpse.support.QuickUtils.quickGlimpseApp;
import static com.metsci.glimpse.support.colormap.ColorGradientUtils.newColorTable;
import static com.metsci.glimpse.support.colormap.ColorGradients.inferno;
import static com.metsci.glimpse.support.colormap.ColorGradients.jet;
import static com.metsci.glimpse.support.shader.line.LineJoinType.JOIN_MITER;
import static com.metsci.glimpse.util.GeneralUtils.floats;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.axis.WrappedAxis1D;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.axis.tagged.painter.TaggedPartialColorYAxisPainter;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.WrappedPainter;
import com.metsci.glimpse.painter.info.AnnotationPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.painter.shape.LineSetPainter;
import com.metsci.glimpse.painter.shape.PointSetPainter;
import com.metsci.glimpse.painter.shape.PolygonPainter;
import com.metsci.glimpse.painter.texture.HeatMapPainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.colormap.ColorMapLinear;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.projection.FlatProjection;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.settings.DefaultLookAndFeel;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;

// FIXME Example not working NewtSwingEDTUtils
public class WrappedAxisExample2
{

    public static void main( String args[] ) throws Exception
    {
        SwingUtilities.invokeLater( ( ) -> {

            // PolygonPainter
            //

            PolygonPainter polygonPainter = new PolygonPainter( );
            {
                polygonPainter.addPolygon( "PolygonGroupA",
                        "Polygon1",
                        floats( 8.5f, 11.0f, 13.0f, 9.5f, 8.0f ),
                        floats( 2.5f, -1.0f, 4.0f, 5.0f, 4.0f ),
                        0 );

                polygonPainter.setShowLines( "PolygonGroupA", true );

                LineStyle lineStyle = new LineStyle( );
                lineStyle.rgba = GlimpseColor.getWhite( );
                lineStyle.joinType = JOIN_MITER;
                lineStyle.thickness_PX = 4;
                polygonPainter.setLineStyle( "PolygonGroupA", lineStyle );

                polygonPainter.setFill( "PolygonGroupA", true );
                polygonPainter.setFillColor( "PolygonGroupA", GlimpseColor.getWhite( 0.4f ) );
            }

            // AnnotationPainter
            //

            AnnotationPainter annotationPainter = new AnnotationPainter( new TextRenderer( FontUtils.getDefaultPlain( 28 ), true, true ) );
            annotationPainter.addAnnotation( "Fairly loooooong string", 8.5f, 2.5f, 0, 7, HorizontalPosition.Center, VerticalPosition.Center, GlimpseColor.getRed( ) );

            // HeatMapPainter
            //

            int heatmapWidth = 100;
            int heatmapHeight = 100;
            double[][] heatmapValues = new double[heatmapWidth][heatmapHeight];
            for ( int i = 0; i < heatmapWidth; i++ )
            {
                for ( int j = 0; j < heatmapHeight; j++ )
                {
                    heatmapValues[i][j] = 0.8 * ( i * j ) / ( heatmapWidth * heatmapHeight ) + 0.2 * Math.random( );
                }
            }

            Projection heatmapProj = new FlatProjection( +5, +12, 0, +9 );

            FloatTextureProjected2D heatmapData = new FloatTextureProjected2D( heatmapWidth, heatmapHeight );
            heatmapData.setProjection( heatmapProj );
            heatmapData.setData( heatmapValues );

            TaggedAxis1D colorAxis = new TaggedAxis1D( );
            colorAxis.setMin( -1.0 );
            colorAxis.setMax( +2.0 );
            Tag colorMinTag = colorAxis.addTag( "ColorTagA", 0.0 ).setAttribute( TEX_COORD_ATTR, 0.0f );
            Tag colorMaxTag = colorAxis.addTag( "ColorTagB", 1.0 ).setAttribute( TEX_COORD_ATTR, 1.0f );
            colorAxis.validate( );

            HeatMapPainter heatmapPainter = new HeatMapPainter( colorMinTag, colorMaxTag );
            heatmapPainter.setColorScale( newColorTable( jet, 16 ) );
            heatmapPainter.setDiscardAbove( true );
            heatmapPainter.setDiscardBelow( true );
            heatmapPainter.setData( heatmapData );

            // LineSetPainter
            //

            LineSetPainter linesPainter = new LineSetPainter( );
            {
                linesPainter.setData( floats( 11.0f, 12.0f, 9.5f, 10.6f ),
                        floats( 4.0f, 2.2f, 1.5f, 4.0f ) );

                LineStyle lineStyle = new LineStyle( );
                lineStyle.rgba = GlimpseColor.getBlack( );
                lineStyle.joinType = JOIN_MITER;
                lineStyle.thickness_PX = 5;
                linesPainter.setLineStyle( lineStyle );
            }

            // PointSetPainter
            //

            PointSetPainter pointsPainter = new PointSetPainter( false );
            {
                pointsPainter.setData( floats( 8.5f, 11.0f, 13.0f, 9.5f, 8.0f ),
                        floats( 2.5f, -1.0f, 4.0f, 5.0f, 4.0f ) );

                pointsPainter.setColor( floats( 1.0f, 0.25f, 0.5f, 0.75f, 0.0f ),
                        new ColorMapLinear( 0, 1, inferno ) );

                pointsPainter.setPointSize( 40 );
                pointsPainter.setFeatherSize( 3 );
            }

            // WrappedPainter
            //

            WrappedPainter wrappedPainter = new WrappedPainter( );
            wrappedPainter.addPainter( new BackgroundPainter( ) );
            wrappedPainter.addPainter( new GridPainter( ) );
            wrappedPainter.addPainter( new BorderPainter( ) );
            wrappedPainter.addPainter( heatmapPainter );
            wrappedPainter.addPainter( polygonPainter );
            wrappedPainter.addPainter( linesPainter );
            wrappedPainter.addPainter( pointsPainter );
            wrappedPainter.addPainter( annotationPainter );

            // Show
            //

            MultiAxisPlot2D layout = new MultiAxisPlot2D( )
            {
                @Override
                protected void initializeCenterAxis( )
                {
                    this.centerAxisX = new WrappedAxis1D( +8.5, +20 );
                    this.centerAxisY = new WrappedAxis1D( +2.5, +31 );
                }
            };

            layout.createAxisBottom( "X" ).getAxis( ).setParent( layout.getCenterAxisX( ) );
            layout.createAxisLeft( "Y" ).getAxis( ).setParent( layout.getCenterAxisY( ) );

            AxisInfo colorAxisInfo = layout.createAxisRight( "Z", colorAxis, new TaggedAxisMouseListener1D( ) );
            colorAxisInfo.setAxisPainter( new TaggedPartialColorYAxisPainter( new GridAxisLabelHandler( ) ) );

            layout.getLayoutCenter( ).addPainter( wrappedPainter );
            layout.getLayoutCenter( ).addPainter( new BorderPainter( ) );

            // Make sure the BackgroundPainter gets assigned a color -- otherwise blending turns out weird
            layout.setLookAndFeel( new DefaultLookAndFeel( ) );

            quickGlimpseApp( "WrappedAxisExample2", GL3, 0.8, layout );

        } );
    }

}

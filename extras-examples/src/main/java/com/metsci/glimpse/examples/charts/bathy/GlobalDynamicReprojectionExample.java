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
package com.metsci.glimpse.examples.charts.bathy;

import static com.metsci.glimpse.axis.UpdateMode.CenterScale;
import static com.metsci.glimpse.axis.tagged.Tag.TEX_COORD_ATTR;
import static java.lang.Math.PI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.axis.tagged.NamedConstraint;
import com.metsci.glimpse.axis.tagged.OrderedConstraint;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.axis.tagged.painter.TaggedPartialColorYAxisPainter;
import com.metsci.glimpse.charts.bathy.BathymetryData;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.geo.ScalePainter;
import com.metsci.glimpse.painter.texture.TaggedHeatMapPainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.projection.LatLonProjection;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;
import com.metsci.glimpse.support.texture.mutator.ColorGradientConcatenator;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.io.StreamOpener;
import com.metsci.glimpse.util.units.Length;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * Data displayed was downloaded from the NOAA/NGDC Bathymetry
 * tool at http://www.ngdc.noaa.gov/mgg/gdas/gd_designagrid.html</p>
 *
 * @author hogye
 */
public class GlobalDynamicReprojectionExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new GlobalDynamicReprojectionExample( ) );
    }

    protected static InputStream openBathyFile( ) throws IOException
    {
        File file = new File( "world.bathy" );
        try
        {
            return StreamOpener.file.openForRead( file.getPath( ) );
        }
        catch ( IOException e )
        {
            throw new IOException( "Bathy file is not included in the Glimpse distribution because of its size. We are working to correct this example." );
        }
    }

    @Override
    public MultiAxisPlot2D getLayout( )
    {
        MultiAxisPlot2D plot = new MultiAxisPlot2D( );
        plot.getCenterAxis( ).lockAspectRatioXY( 1.0 );
        plot.getCenterAxis( ).getAxisX( ).setUpdateMode( CenterScale );
        plot.getCenterAxis( ).getAxisX( ).setUpdateMode( CenterScale );

        plot.setBorderSize( 2 );
        plot.setShowTitle( false );

        Axis2D xyAxis = plot.getCenterAxis( );

        AxisUnitConverter unitConverter = new AxisUnitConverter( )
        {
            public double fromAxisUnits( double value )
            {
                return Length.fromNauticalMiles( value );
            }

            public double toAxisUnits( double value )
            {
                return Length.toNauticalMiles( value );
            }
        };

        GridAxisLabelHandler xTicks = new GridAxisLabelHandler( );
        GridAxisLabelHandler yTicks = new GridAxisLabelHandler( );
        xTicks.setAxisUnitConverter( unitConverter );
        yTicks.setAxisUnitConverter( unitConverter );

        // color axis
        TaggedAxis1D colorAxis = new TaggedAxis1D( );
        AxisMouseListener colorMouseListener = new TaggedAxisMouseListener1D( );
        final AxisInfo colorAxisInfo = plot.createAxisRight( "color", colorAxis, colorMouseListener );

        final GridAxisLabelHandler colorTickHandler = new GridAxisLabelHandler( );
        final TaggedPartialColorYAxisPainter colorTagPainter = new TaggedPartialColorYAxisPainter( colorTickHandler );
        colorAxisInfo.setAxisPainter( colorTagPainter );

        ColorTexture1D colorMapTexture = new ColorTexture1D( 1024 );
        colorMapTexture.mutate( new ColorGradientConcatenator( ColorGradients.bathymetry, ColorGradients.topography ) );
        colorTagPainter.setColorScale( colorMapTexture );

        colorAxis.addTag( "Max", 10000.0 ).setAttribute( TEX_COORD_ATTR, 1.0f );
        colorAxis.addTag( "Sea Level", 0.0 ).setAttribute( TEX_COORD_ATTR, 0.5f );
        colorAxis.addTag( "Min", -8000.0 ).setAttribute( TEX_COORD_ATTR, 0.0f );

        List<String> constraints = new ArrayList<String>();
        for(Tag tag: colorAxis.getSortedTags())
        	constraints.add(tag.getName());
        
        colorAxis.addConstraint( new OrderedConstraint( "OrderingConstraint" , 200, constraints) );
        
        colorAxis.setMin( -10000 );
        colorAxis.setMax( 12000 );

        // miscellaneous painters

        plot.addPainter( new BackgroundPainter( ).setColor( GlimpseColor.getBlack( ) ) );

        GridPainter grid = new GridPainter( xTicks, yTicks );
        grid.setLineColor( GlimpseColor.getGray( 0.2f ) );
        grid.setMinorLineColor( GlimpseColor.getGray( 0.1f ) );
        plot.addPainter( grid );

        // heatmap painter

        final TaggedHeatMapPainter heatmap = new TaggedHeatMapPainter( colorAxis );
        heatmap.setColorScale( colorMapTexture );
        plot.addPainter( heatmap );

        final TangentPlane initPlane = new TangentPlane( LatLonGeo.fromDeg( 0, 0 ) );

        // load a bathemetry data set from a data file obtained from
        // http://www.ngdc.noaa.gov/mgg/gdas/gd_designagrid.html
        BathymetryData bathyData;
        try
        {
            bathyData = new BathymetryData( openBathyFile( ), initPlane );
        }
        catch ( IOException e )
        {
            e.printStackTrace( );
            throw new RuntimeException( e );
        }
        bathyData.setAxisBounds( xyAxis );

        final FloatTextureProjected2D texture = new FloatTextureProjected2D( bathyData.getImageWidth( ), bathyData.getImageHeight( ), true );
        texture.setData( bathyData.getData( ) );

        final double startLat = bathyData.getStartLat( );
        final double startLon = bathyData.getStartLon( );
        final double endLat = startLat + bathyData.getHeightStep( ) * bathyData.getImageHeight( );
        final double endLon = startLon + bathyData.getWidthStep( ) * bathyData.getImageWidth( );

        final RunnableOn<TangentPlane> reprojectHeatmap = new RunnableOn<TangentPlane>( )
        {
            // Clip stuff so far from tangent point that distortion is bad
            double earthCircumference = 2 * PI * LatLonGeo.defaultDatum.getRadius( );
            double oneOverDistanceThreshold = 1.0 / ( 0.4 * earthCircumference );

            public void run( TangentPlane plane )
            {
                LatLonProjection newProjection = new LatLonProjection( plane, startLat, endLat, startLon, endLon, false )
                {
                    public void getVertexXYZ( double textureFractionX, double textureFractionY, float[] resultXYZ )
                    {
                        Vector2d xy = project( textureFractionX, textureFractionY );
                        resultXYZ[0] = ( float ) xy.getX( );
                        resultXYZ[1] = ( float ) xy.getY( );

                        // We could set the z-clipping plane, but dividing by the threshold is
                        // easier, and gives the same effect because ShadedTexturePainter sets
                        // the z-clipping plane at 1
                        //
                        Vector2d xyTangent = ( ( TangentPlane ) projection ).getTangentPointOnPlane( );
                        resultXYZ[2] = ( float ) ( xy.minus( xyTangent ).norm( ) * oneOverDistanceThreshold );
                    }
                };

                texture.setProjection( newProjection );
                heatmap.setData( texture );
            }
        };
        reprojectHeatmap.run( initPlane );

        xyAxis.addAxisListener( new AxisListener2D( )
        {
            TangentPlane currentPlane = initPlane;

            public void axisUpdated( Axis2D axis )
            {
                // Using the center of the viewport would be more
                // practical, but this makes for a more exciting demo
                //
                double xRef = axis.getAxisX( ).getSelectionCenter( );
                double yRef = axis.getAxisY( ).getSelectionCenter( );
                LatLonGeo ref = currentPlane.unproject( xRef, yRef );

                currentPlane = new TangentPlane( ref, xRef, yRef );
                reprojectHeatmap.run( currentPlane );
            }
        } );

        // more miscellaneous painters

        CrosshairPainter crosshairs = new CrosshairPainter( );
        crosshairs.showSelectionBox( false );
        crosshairs.setLineWidth( 1.0f );
        crosshairs.setCursorColor( GlimpseColor.getGreen( 0.3f ) );
        plot.addPainter( crosshairs );

        ScalePainter scale = new ScalePainter( );
        scale.setUnitConverter( unitConverter );
        scale.setPixelBufferX( 5 );
        scale.setPixelBufferY( 5 );
        scale.setPrimaryColor( GlimpseColor.getGray( 0.75f ) );
        scale.setSecondaryColor( GlimpseColor.getGray( 0.5f ) );
        scale.setBorderColor( GlimpseColor.getWhite( ) );
        scale.setTextColor( GlimpseColor.getWhite( ) );
        plot.addPainter( scale );

        plot.addPainter( new BorderPainter( ) );

        return plot;
    }

    static abstract class RunnableOn<T>
    {

        public abstract void run( T t );

        public Runnable runnable( final T t )
        {
            return new Runnable( )
            {
                public void run( )
                {
                    RunnableOn.this.run( t );
                }
            };
        }

        public static <T> RunnableOn<T> runnable1( final Runnable runnable )
        {
            return new RunnableOn<T>( )
            {
                public void run( T t )
                {
                    runnable.run( );
                }
            };
        }
    }
}

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
package com.metsci.glimpse.examples.charts.dnc;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.metsci.glimpse.axis.UpdateMode;
import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.canvas.SwingGlimpseCanvas;
import com.metsci.glimpse.charts.raster.BsbRasterData;
import com.metsci.glimpse.charts.shoreline.LandShapePainter;
import com.metsci.glimpse.charts.vector.MercatorProjection;
import com.metsci.glimpse.charts.vector.display.BasicSkin;
import com.metsci.glimpse.charts.vector.display.DNCBasicSkinHelper;
import com.metsci.glimpse.charts.vector.display.examplesupport.BoundingBox;
import com.metsci.glimpse.charts.vector.display.examplesupport.MapInfo;
import com.metsci.glimpse.charts.vector.display.examplesupport.ResourceBasedMapInfo;
import com.metsci.glimpse.charts.vector.iteration.GeoObjectIterator;
import com.metsci.glimpse.charts.vector.painter.EncChartPainter;
import com.metsci.glimpse.charts.vector.parser.objects.DNCFeatureCode;
import com.metsci.glimpse.charts.vector.parser.objects.DNCObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShape;
import com.metsci.glimpse.gl.Jogular;
import com.metsci.glimpse.gl.shader.Pipeline;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.painter.decoration.CopyrightPainter;
import com.metsci.glimpse.painter.info.MeasurementPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.plot.MapPlot2D;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.support.shader.SampledColorScaleShaderInteger;
import com.metsci.glimpse.support.texture.ByteTextureProjected2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.io.StreamOpener;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * Displays a DNC map on a glimpse plot
 * 
 * @author Cunningham
 */
public class DNCMapViewerExample
{

    private static Logger logger = Logger.getLogger( DNCMapViewerExample.class.toString( ) );

    private MapPlot2D geoplot;

    private EncChartPainter<DNCObject> dncPainter;

    private ShadedTexturePainter bsbPainter;
    private LandShapePainter ndgcPainter;

    private DNCMapViewerExample( GlimpseCanvas panel, MercatorProjection projection, MapInfo<DNCObject> mapInfo ) throws Exception
    {
        buildPlot( projection );
        loadMaps( mapInfo );
        panel.addLayout( geoplot );
    }

    private void loadMaps( MapInfo<DNCObject> mapInfo ) throws Exception
    {
        loadDNC( mapInfo );
        loadNdgc( mapInfo );
        loadBsb( mapInfo );
    }

    private void loadDNC( MapInfo<DNCObject> mapInfo ) throws Exception
    {
        dncPainter.clearAll( );

        if ( mapInfo.hasGeoIterator( ) )
        {
            GeoProjection projection = geoplot.getGeoProjection( );

            BoundingBox box = new BoundingBox( );
            GeoObjectIterator<DNCObject> dncIterator = mapInfo.getGeoIterator( );
            while ( dncIterator.hasNext( ) )
            {
                DNCObject dncObject = dncIterator.nextGeo( );
                if ( dncObject.getFeatureCode( ) == DNCFeatureCode.Coastline || dncObject.getFeatureCode( ) == DNCFeatureCode.CoastlineShoreline )
                {
                    addToBoundingBox( box, dncObject );
                }
                dncPainter.addENCObject( dncObject );
            }

            if ( !box.isEmpty( ) )
            {
                Vector2d minXy = projection.project( LatLonGeo.fromDeg( box.getMinY( ), box.getMinX( ) ) );
                Vector2d maxXy = projection.project( LatLonGeo.fromDeg( box.getMaxY( ), box.getMaxX( ) ) );
                geoplot.getAxisX( ).setMin( minXy.getX( ) );
                geoplot.getAxisY( ).setMin( minXy.getY( ) );
                geoplot.getAxisX( ).setMax( maxXy.getX( ) );
                geoplot.getAxisY( ).setMax( maxXy.getY( ) );
                geoplot.validate( );
            }
        }
    }

    private void loadNdgc( MapInfo<DNCObject> mapInfo ) throws Exception
    {
        ndgcPainter.deleteAll( );
        String ndgcResource = mapInfo.getNdgcResourceName( );
        if ( ndgcResource != null )
        {
            GeoProjection projection = geoplot.getGeoProjection( );
            ndgcPainter.loadNdgcLandFile( StreamOpener.fileThenResource.openForRead( ndgcResource ), projection );
        }
    }

    private void loadBsb( MapInfo<DNCObject> mapInfo ) throws Exception
    {
        bsbPainter.removeAllDrawableTextures( );
        bsbPainter.removeAllNonDrawableTextures( );

        GeoProjection projection = geoplot.getGeoProjection( );
        if ( mapInfo.hasBsbRasterData( ) && projection instanceof MercatorProjection )
        {
            BsbRasterData data = mapInfo.getBsbRasterData( );

            ByteTextureProjected2D dataTexture = data.getDataTexture( );
            Projection dataTextureProjection = data.getProjection( ( MercatorProjection ) projection );
            dataTexture.setProjection( dataTextureProjection );

            ColorTexture1D colorTexture = data.getColorTexture( );

            bsbPainter.addDrawableTexture( dataTexture, 0 );
            bsbPainter.addNonDrawableTexture( colorTexture, 1 );

            geoplot.setMinZ( 0.0 );
            geoplot.setMaxZ( colorTexture.getDimensionSize( 0 ) );
            geoplot.validate( );
        }
    }

    private void buildPlot( MercatorProjection projection ) throws Exception
    {
        geoplot = new MapPlot2D( projection );

        // lock the plot to a 1 to 1 aspect ratio
        geoplot.lockAspectRatioXY( 1.0f );
        geoplot.setUpdateModeXY( UpdateMode.CenterScale );

        geoplot.validate( );

        // add a Metron copywrite notice to the plot
        geoplot.addPainter( new CopyrightPainter( ) );

        // Create ndgc data files using the using the NOAA/NGDC Coastline Extractor
        // at http://www.ngdc.noaa.gov/mgg_coastline/
        ndgcPainter = new LandShapePainter( );
        geoplot.addPainter( ndgcPainter );

        bsbPainter = new ShadedTexturePainter( );
        geoplot.addPainter( bsbPainter );

        SampledColorScaleShaderInteger fragShader = new SampledColorScaleShaderInteger( geoplot.getAxisZ( ), 0, 1 );
        bsbPainter.setPipeline( new Pipeline( "colormap", null, null, fragShader ) );

        BasicSkin<DNCObject> skin = new BasicSkin<DNCObject>( new DNCBasicSkinHelper( ), "data/dncskin.xml" );
        dncPainter = new EncChartPainter<DNCObject>( projection, skin );

        geoplot.addPainter( dncPainter );

        geoplot.addPainter( new MeasurementPainter( "m" ) );

        geoplot.getCrosshairPainter( ).setVisible( false );
    }

    private void addToBoundingBox( BoundingBox box, DNCObject encObject )
    {
        for ( GeoShape shape : encObject.getShapeList( ) )
        {
            for ( int i = 0; i < shape.getNumCoordinates( ); i++ )
            {
                double currLon = shape.getVertex( 0, i );
                double currLat = shape.getVertex( 1, i );

                box.applyPoint( currLon, currLat );
            }
        }
    }

    private static void containInFrame( GlimpseCanvas canvas )
    {
        JFrame frame = new JFrame( "DNC Map Viewer Example" );
        frame.add( ( SwingGlimpseCanvas ) canvas );

        frame.pack( );
        frame.setSize( 800, 800 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );
    }

    public static void main( String[] args )
    {
        Jogular.initJogl( );

        try
        {
            String root = null;
            String dir = "/path/to/dnc_dir/";
            
            if ( args.length > 0 )
            {
                root = args[0];
                if ( root.indexOf( '/' ) >= 0 ) dir = null;
            }
            else
            {
                root = "dnc15-general";
            }
            

            GlimpseCanvas panel = new SwingGlimpseCanvas( true );
            MercatorProjection projection = new MercatorProjection( );

            MapInfo<DNCObject> mapInfo = ResourceBasedMapInfo.createDNCMapInfo( root, dir );
            new DNCMapViewerExample( panel, projection, mapInfo );

            RepaintManager.newRepaintManager( panel );
            containInFrame( panel );
        }
        catch ( Throwable t )
        {
            logger.log( Level.SEVERE, "", t );
        }
    }

}

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
package com.metsci.glimpse.charts.vector.display;

import com.metsci.glimpse.charts.vector.display.xmlgen.ColorSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.FeatureSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.Geoskinspec;
import com.metsci.glimpse.charts.vector.display.xmlgen.PolygonPainterSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.ShapeSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.ShapeTypeSpec;
import com.metsci.glimpse.charts.vector.display.xmlgen.TrackPainterSpec;
import com.metsci.glimpse.charts.vector.painter.EncPainterUtils;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObjectType;
import java.util.List;
import java.util.logging.Logger;

public class HardCodedENCSkinSpecFactory
{
    private static Logger logger = Logger.getLogger( HardCodedENCSkinSpecFactory.class.toString( ) );

    private ColorPalette colorPalette;

    public HardCodedENCSkinSpecFactory( ColorPalette colorPalette )
    {
        this.colorPalette = colorPalette;
    }

    public Geoskinspec createSkinSpec( )
    {
        Geoskinspec skinSpec = new Geoskinspec( );
        List<FeatureSpec> encList = skinSpec.getFeature();
        encList.add( addCoastlineHandlers( ) );
        encList.add( addDepthContourHandlers( ) );
        encList.add( addDepthAreaHandlers( ) );
        encList.add( addLandAreaHandlers( ) );
        encList.add( addSeaAreaNamedWaterAreaHandler( ) );
        encList.add( addCoverageSpec( ) );
        return skinSpec;
    }

    private FeatureSpec addCoastlineHandlers( )
    {
        TrackPainterSpec trackSpec = createDefaultTrackHandlerForLinestringType( );
        trackSpec.setShowPoints( false );
        trackSpec.setLineColor( EncPainterUtils.convertFloatArrayToColorSpec( colorPalette.getCoastlineLineColor( .5f ) ) );
        ShapeSpec shape = EncPainterUtils.createShape( ShapeTypeSpec.LINESTRING, trackSpec );

        return EncPainterUtils.createEnc( ENCObjectType.Coastline.name( ), shape );
        //debugWarningMap.add(ENCObjectType.Coastline);
    }

    private FeatureSpec addDepthContourHandlers( )
    {
        TrackPainterSpec trackSpec = createDefaultTrackHandlerForLinestringType( );
        trackSpec.setShowPoints( false );
        trackSpec.setDotted( true );
        trackSpec.setStipplePattern( Short.valueOf( ( short ) 21845 ) );
        trackSpec.setStippleFactor( Integer.valueOf( 1 ) );
        trackSpec.setLineColor( EncPainterUtils.convertFloatArrayToColorSpec( colorPalette.getDepthLineColor( .5f ) ) );
        ShapeSpec shape = EncPainterUtils.createShape( ShapeTypeSpec.LINESTRING, trackSpec );
        return EncPainterUtils.createEnc( ENCObjectType.DepthContour.name( ), shape );
        //debugWarningMap.add(ENCObjectType.DepthContour);
    }

    private FeatureSpec addDepthAreaHandlers( )
    {
        PolygonPainterSpec polygonSpec = createDefaultPolygonHandlerForPolygonType( );
        polygonSpec.setPolyDotted( Boolean.TRUE );
        polygonSpec.setLineColor( EncPainterUtils.convertFloatArrayToColorSpec( colorPalette.getDepthLineColor( .5f ) ) );
        ShapeSpec shape = EncPainterUtils.createShape( ShapeTypeSpec.POLYGON, polygonSpec );

        TrackPainterSpec trackSpec = createDefaultTrackHandlerForLinestringType( );
        trackSpec.setDotted( Boolean.TRUE );
        trackSpec.setLineColor( EncPainterUtils.convertFloatArrayToColorSpec( colorPalette.getDepthLineColor( .5f ) ) );
        trackSpec.setShowPoints( Boolean.FALSE );
        ShapeSpec shape2 = EncPainterUtils.createShape( ShapeTypeSpec.LINESTRING, trackSpec );

        return EncPainterUtils.createEnc( ENCObjectType.DepthArea.name(), shape, shape2 );
        //debugWarningMap.add(ENCObjectType.DepthArea);
    }

    private FeatureSpec addLandAreaHandlers( )
    {
        PolygonPainterSpec polySpec = createDefaultPolygonHandlerForPolygonType( );
        float[] landColorRgba = colorPalette.getLandColor( .2f );
        ColorSpec landColor = EncPainterUtils.convertFloatArrayToColorSpec( landColorRgba );
        polySpec.setFill( Boolean.TRUE );
        polySpec.setFillColor( landColor );
        polySpec.setShowLines( Boolean.FALSE );
        ShapeSpec polyShape = EncPainterUtils.createShape( ShapeTypeSpec.POLYGON, polySpec );

        TrackPainterSpec trackSpec = createDefaultTrackHandlerForPointType( );
        trackSpec.setPointColor( landColor );
        ShapeSpec pointShape = EncPainterUtils.createShape( ShapeTypeSpec.POINT, trackSpec );

        FeatureSpec enc = EncPainterUtils.createEnc( ENCObjectType.LandArea.name(), polyShape, pointShape );
        enc.getFeaturetype().add( ENCObjectType.LandRegion.name() );
        return enc;
        //debugWarningMap.add(ENCObjectType.LandArea);
        //debugWarningMap.add(ENCObjectType.LandRegion);
    }

    private FeatureSpec addSeaAreaNamedWaterAreaHandler( )
    {
        PolygonPainterSpec polySpec = createDefaultPolygonHandlerForPolygonType( );
        polySpec.setFill( true );
        float[] waterBlueRgba = colorPalette.getWaterColor( .2f );
        ColorSpec waterBlueColor = EncPainterUtils.convertFloatArrayToColorSpec( waterBlueRgba );
        polySpec.setFillColor( waterBlueColor );
        polySpec.setShowLines( Boolean.FALSE );

        ShapeSpec polyShape = EncPainterUtils.createShape( ShapeTypeSpec.POLYGON, polySpec );
        return EncPainterUtils.createEnc( ENCObjectType.SeaAreaNamedWaterArea.name(), polyShape );

        //debugWarningMap.add(ENCObjectType.SeaAreaNamedWaterArea);
    }

    private FeatureSpec addCoverageSpec( )
    {
        PolygonPainterSpec polySpec = createDefaultPolygonHandlerForPolygonType( );
        polySpec.setLineColor( EncPainterUtils.convertFloatArrayToColorSpec( colorPalette.getCoverageLineColor( .8f ) ) );
        polySpec.setLineDotted( Boolean.TRUE );

        ShapeSpec polyShape = EncPainterUtils.createShape( ShapeTypeSpec.POLYGON, polySpec );
        return EncPainterUtils.createEnc( ENCObjectType.Coverage.name(), polyShape );

        //debugWarningMap.add(ENCObjectType.Coverage);
    }

    private PolygonPainterSpec createDefaultPolygonHandlerForPolygonType( )
    {
        return new PolygonPainterSpec( );
    }

    private TrackPainterSpec createDefaultTrackHandlerForLinestringType( )
    {
        TrackPainterSpec trackPainterSpec = new TrackPainterSpec( );
        trackPainterSpec.setShowLines( Boolean.TRUE );
        return trackPainterSpec;
    }

    private TrackPainterSpec createDefaultTrackHandlerForMultiPointType( )
    {
        return createDefaultTrackHandlerForPointType( );
    }

    private TrackPainterSpec createDefaultTrackHandlerForPointType( )
    {
        TrackPainterSpec trackPainterSpec = new TrackPainterSpec( );
        trackPainterSpec.setShowLines( Boolean.FALSE );
        return trackPainterSpec;
    }

}

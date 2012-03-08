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

import com.metsci.glimpse.charts.vector.display.xmlgen.TrackPainterSpec;
import com.metsci.glimpse.charts.vector.painter.EncPainterUtils;
import com.metsci.glimpse.charts.vector.parser.objects.GeoFeatureType;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShape;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShapeType;
import com.metsci.glimpse.painter.track.Point;
import com.metsci.glimpse.painter.track.TrackPainter;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

import java.util.List;


/**
 * This content handler renders GeoObjects by drawing points and lines. It
 * delegates the painting of the geo objects to the
 * com.metsci.glimpse.painter.track.TrackPainter. The drawing characteristics of
 * the rendered points and lines are determined by the feature type of the geoobject
 * being rendered and the TrackPainterSpec passed to this class in the
 * constructor. The values in the TrackPainterSpec are determined by the skin
 * xml config file. In a nutshell, this class gets a GeoObject to render, looks
 * up the drawing specs from the TrackPainterSpec, configures the TrackPainter
 * to those specs, and invokes the TrackPainter to render the geoobject.
 * 
 * @author Cunningham
 */
public class TrackHandler <V extends GeoObject> implements ContentHandler<V>
{
    private static final GeoShapeType[] shapeTypes = GeoShapeType.values( );
    
    private static final int defaultStippleFactor = 1;
    private static final short defaultStipplePattern = ( short ) 0x00FF;

    private TrackPainterSpec spec;
    private int trackIdOffset = 0;
    private int trackIdFactor = -1;// Integer.MAX_VALUE / ( encObjectTypes.length * shapeTypes.length );

    public TrackHandler( )
    {
        this( new TrackPainterSpec( ) );
    }

    public TrackHandler( TrackPainterSpec spec )
    {
        this.spec = spec;
    }

    private int determineGroupId( GeoFeatureType objectType, GeoShapeType shapeType )
    {
        return objectType.ordinal( ) + ( shapeType.ordinal( ) + shapeTypes.length );
    }

    @Override
    public Integer getScaleMin( V object )
    {
        return null;
    }

    @Override
    public void paintEnc( GeoContext geoContext, V object )
    {
        GeoShape shape = object.getGeoShapes( ).iterator().next();
        int groupId = determineGroupId( object.getGeoFeatureType( ), shape.getShapeType( ) );
        if (trackIdFactor < 0)
            trackIdFactor = Integer.MAX_VALUE / ( object.getGeoFeatureType( ).getNumFeatures() * shapeTypes.length );
        int trackId = trackIdFactor * groupId + trackIdOffset++;

        TrackPainter trackPainter = geoContext.getTrackPainter( );
        initializeGroupPainterSettings( trackId, trackPainter );

        GeoProjection projection = geoContext.getProjection( );
        List<Point> points = EncPainterUtils.convertGeoVertexesToPoints( projection, shape, trackId );
        if ( !points.isEmpty( ) )
        {
            trackPainter.addPoints( trackId, points );
        }
    }

    @Override
    public void reset( )
    {
        trackIdOffset = 0;
    }

    private void initializeGroupPainterSettings( int trackId, TrackPainter trackPainter )
    {
        if ( spec.isShowLines( ) != null ) trackPainter.setShowLines( trackId, spec.isShowLines( ).booleanValue( ) );

        if ( spec.getLineColor( ) != null ) trackPainter.setLineColor( trackId, EncPainterUtils.convertColorSpecToFloatArray( spec.getLineColor( ) ) );

        if ( spec.getLineWidth( ) != null ) trackPainter.setLineWidth( trackId, spec.getLineWidth( ).floatValue( ) );

        if ( spec.isShowPoints( ) != null ) trackPainter.setShowPoints( trackId, spec.isShowPoints( ).booleanValue( ) );

        if ( spec.getPointColor( ) != null ) trackPainter.setPointColor( trackId, EncPainterUtils.convertColorSpecToFloatArray( spec.getPointColor( ) ) );

        if ( spec.getPointSize( ) != null ) trackPainter.setPointSize( trackId, spec.getPointSize( ).floatValue( ) );

        if ( spec.isDotted( ) != null )
        {
            boolean isDotted = spec.isDotted( ).booleanValue( );
            trackPainter.setDotted( trackId, isDotted );
            if ( isDotted )
            {
                int stippleFactor = defaultStippleFactor;
                if ( spec.getStippleFactor( ) != null ) stippleFactor = spec.getStippleFactor( ).intValue( );
                short stipplePattern = defaultStipplePattern;
                if ( spec.getStipplePattern( ) != null ) stipplePattern = spec.getStipplePattern( ).shortValue( );
                trackPainter.setDotted( trackId, stippleFactor, stipplePattern );
            }
        }
        if ( spec.isShowLabel( ) != null )
        {
            boolean isShowLabel = spec.isShowLabel( ).booleanValue( );
            trackPainter.setShowLabel( trackId, isShowLabel );
            if ( isShowLabel )
            {
                if ( spec.getLabel( ) != null ) trackPainter.setLabel( trackId, spec.getLabel( ) );
                if ( spec.getLabelColor( ) != null ) trackPainter.setLabelColor( trackId, EncPainterUtils.convertColorSpecToFloatArray( spec.getLabelColor( ) ) );
            }
        }
        if ( spec.isShowLabelLine( ) != null )
        {
            boolean showLabelLine = spec.isShowLabelLine( ).booleanValue( );
            trackPainter.setShowLabelLine( trackId, showLabelLine );
            if ( showLabelLine && spec.getLabelLineColor( ) != null )
            {
                trackPainter.setLabelLineColor( trackId, EncPainterUtils.convertColorSpecToFloatArray( spec.getLabelLineColor( ) ) );
            }
        }
        if ( spec.isShowHeadPoint( ) != null )
        {
            boolean showHeadPoint = spec.isShowHeadPoint( ).booleanValue( );
            trackPainter.setShowHeadPoint( trackId, showHeadPoint );
            if ( showHeadPoint )
            {
                if ( spec.getHeadPointSize( ) != null ) trackPainter.setHeadPointSize( trackId, spec.getHeadPointSize( ).floatValue( ) );
                if ( spec.getHeadPointColor( ) != null ) trackPainter.setHeadPointColor( trackId, EncPainterUtils.convertColorSpecToFloatArray( spec.getHeadPointColor( ) ) );
            }
        }
    }

    public void setHeadPointColor( float r, float g, float b, float a )
    {
        spec.setHeadPointColor( EncPainterUtils.convertFloatsToColorSpec( r, g, b, a ) );
    }

    public void setHeadPointSize( float size )
    {
        spec.setHeadPointSize( Float.valueOf( size ) );
    }

    public void setShowHeadPoint( boolean show )
    {
        spec.setShowHeadPoint( Boolean.valueOf( show ) );
    }

    public void setPointColor( float r, float g, float b, float a )
    {
        spec.setPointColor( EncPainterUtils.convertFloatsToColorSpec( r, g, b, a ) );
    }

    public void setPointColor( float[] color )
    {
        spec.setPointColor( EncPainterUtils.convertFloatArrayToColorSpec( color ) );
    }

    public void setPointSize( float size )
    {
        spec.setPointSize( Float.valueOf( size ) );
    }

    public void setShowPoints( boolean show )
    {
        spec.setShowPoints( Boolean.valueOf( show ) );
    }

    public void setLineColor( float r, float g, float b, float a )
    {
        spec.setLineColor( EncPainterUtils.convertFloatsToColorSpec( r, g, b, a ) );
    }

    public void setLineColor( float[] color )
    {
        spec.setLineColor( EncPainterUtils.convertFloatArrayToColorSpec( color ) );
    }

    public void setLineWidth( float width )
    {
        spec.setLineWidth( Float.valueOf( width ) );
    }

    public void setShowLines( boolean show )
    {
        spec.setShowLines( Boolean.valueOf( show ) );
    }

    public void setDotted( boolean activate )
    {
        spec.setDotted( Boolean.valueOf( activate ) );
    }

    public void setDotted( int stippleFactor, short stipplePattern )
    {
        spec.setDotted( Boolean.TRUE );
        spec.setStippleFactor( Integer.valueOf( stippleFactor ) );
        spec.setStipplePattern( Short.valueOf( stipplePattern ) );
    }

    public void setLabelColor( float r, float g, float b, float a )
    {
        spec.setLabelColor( EncPainterUtils.convertFloatsToColorSpec( r, g, b, a ) );
    }

    public void setLabelLineColor( float r, float g, float b, float a )
    {
        spec.setLabelLineColor( EncPainterUtils.convertFloatsToColorSpec( r, g, b, a ) );
    }

    public void setShowLabelLine( boolean show )
    {
        spec.setShowLabelLine( Boolean.valueOf( show ) );
    }

    public void setLabel( String label )
    {
        spec.setLabel( label );
    }

    public void setShowLabel( boolean show )
    {
        spec.setShowLabel( Boolean.valueOf( show ) );
    }
}

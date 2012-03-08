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
package com.metsci.glimpse.charts.vector.painter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.charts.vector.display.ContentHandler;
import com.metsci.glimpse.charts.vector.display.GeoContext;
import com.metsci.glimpse.charts.vector.display.Skin;
import com.metsci.glimpse.charts.vector.parser.GeoReader;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.painter.base.GlimpsePainterImpl;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.AnnotationPainter;
import com.metsci.glimpse.painter.shape.PolygonPainter;
import com.metsci.glimpse.painter.track.TrackPainter;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;


/**
 * Draws encobjects.
 *
 * @author cunningham
 */
public class EncChartPainter<V extends GeoObject> extends GlimpsePainter2D
{
    private static final double metersToNauticalMiles = 0.000539;

    /**
     * Default zoom ranges in nautical miles.  { 0, 10 } means range {0-9} and range {10+}
     */
    private static final double[] defaultAnnotationZoomRangesInNM = new double[] { 0., 40., 60., 80., 100. };

    /**
     * Projection
     */
    private GeoProjection projection;

    /**
     * Painter to draw polygons
     */
    private PolygonPainter polygonPainter;
    /**
     * Painter to draw points and lines
     */
    private TrackPainter trackPainter;
    /**
     * Painters to draw text, based on zoom level
     */
    private AnnotationPaintersByZoomLevel annotationPainters;

    /**
     * The class that will do the actual rendering.
     * For any given enc object and shape, skins.get(0) gets first crack.  Only if it
     * can't handle does skins.get(1) get a chance, and so on until skins.get(n).
     */
    private List<Skin<V>> skins;

    private GeoContext encContext;

    /**
     * Flag on whether to consider zoom level when displaying enc objects.  Should only
     * be set to false when using debug tools.
     */
    private boolean zoomAdjustDisplay = true;

    private DelegatePainter delegate;


    public EncChartPainter( GeoProjection projection, Skin<V> skin )
    {
        this( projection, defaultAnnotationZoomRangesInNM, Collections.<Skin<V>>singletonList( skin ) );
    }

    public EncChartPainter( GeoProjection projection, final Skin<V> skin1, final Skin<V> skin2 )
    {
        // Please excuse the double brace workaround to push the generic array creation off into the java list class
        this( projection, defaultAnnotationZoomRangesInNM, new ArrayList<Skin<V>>( ) {{ add(skin1); add(skin2); }} );
    }

    public EncChartPainter( GeoProjection projection, List<Skin<V>> skins )
    {
        this( projection, defaultAnnotationZoomRangesInNM, skins );
    }

    public EncChartPainter( GeoProjection projection, double[] annotationZoomRangesInNM, List<Skin<V>> skins )
    {
        this.skins = skins;

        this.delegate = new DelegatePainter( );


        this.polygonPainter = new PolygonPainter( );
        addPainter( polygonPainter );

        this.trackPainter = new TrackPainter( );
        addPainter( trackPainter );

        this.annotationPainters = new AnnotationPaintersByZoomLevel( delegate, annotationZoomRangesInNM );

        this.encContext = new GeoContext( projection, polygonPainter, trackPainter, null );

        this.projection = projection;
    }

    public void addPainter( GlimpsePainter painter )
    {
        delegate.addPainter( painter );
    }

    public void removePainter( GlimpsePainter painter )
    {
        delegate.removePainter( painter );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        super.setLookAndFeel( laf );
        delegate.setLookAndFeel( laf );
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        // Quick, dirty and crude.
        // Zoom level will be defined by the great circle distance (in nautical miles) between min and
        // max lat lons.  That value will be multiplied by 100K and then used as the query value to a
        // navigable map of Painters.  The Painters are mapped by "ScaledMinimum" range values.
        // ScaledMinimum is an optional int attribute in an encobject.  There is no relationship between
        // distance in nautical miles and "ScaledMinimum" range values.  They appear to be somewhat
        // correlated.  Yes, it is kludgy...

        double viewLevel = determineViewLevel( axis );
        annotationPainters.setPainterVisibilityAtViewLevel( viewLevel );

        delegate.paintTo( context );
//        super.paintTo( context );
    }

    public void clearAll( )
    {
        polygonPainter.deleteAll( );
        trackPainter.deleteAll( );
        annotationPainters.clearPainterData( );

        for ( Skin<V> skin : skins )
        {
            skin.reset( );
        }
    }

    public void changeSkins( Iterator<V> encIterator, List<Skin<V>> skins ) throws Exception
    {
        clearAll( );
        this.skins = skins;
        addENCObjects( encIterator );
    }

    public void addENCObjects( GeoReader<V> reader )
    {
        Collection<V> encCollection = reader.getCollection( );
        for ( V encObject : encCollection )
        {
            addENCObject( encObject );
        }
    }

    public void addENCObjects( Iterator<V> encIterator ) throws Exception
    {
        while ( encIterator.hasNext( ) )
        {
            V encObject = encIterator.next( );
            addENCObject( encObject );
        }
    }

    public void addENCObject( V encObject )
    {
        List<ContentHandler<V>> contentHandlers = Collections.<ContentHandler<V>> emptyList( );
        for ( int skinIndex = 0; skinIndex < skins.size() && contentHandlers.isEmpty( ); skinIndex++ )
        {
            contentHandlers = skins.get( skinIndex ).getHandlersForGeoObject( encObject );
        }

        for ( ContentHandler<V> contentHandler : contentHandlers )
        {
            Integer scaledMin = contentHandler.getScaleMin( encObject );
            Double scaledMinDouble = null;
            if ( scaledMin != null ) scaledMinDouble = Double.valueOf( scaledMin.doubleValue( ) / 100000. );

            AnnotationPainter annotationPainter = annotationPainters.getPainterAtViewLevel( scaledMinDouble );
            if ( annotationPainter != null )
            {
                encContext.setAnnotationPainter( annotationPainter );
                contentHandler.paintEnc( encContext, encObject );
            }
        }
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        super.dispose( context );

        delegate.dispose( context );
        polygonPainter.dispose( context );
        trackPainter.dispose( context );
        annotationPainters.dispose( context );
    }

    public void setZoomAdjustedDisplay( boolean enable )
    {
        this.zoomAdjustDisplay = enable;
    }

    private double determineViewLevel( Axis2D axis )
    {
        if ( !zoomAdjustDisplay )
        {
            return 0.00001;
        }
        else
        {
            LatLonGeo maxLL = projection.unproject( axis.getAxisX( ).getMax( ), axis.getAxisY( ).getMax( ) );
            LatLonGeo minLL = projection.unproject( axis.getAxisX( ).getMin( ), axis.getAxisY( ).getMin( ) );
            double distNM = maxLL.getDistanceTo( minLL ) * metersToNauticalMiles;
            return distNM;
        }
    }

    private static class AnnotationPaintersByZoomLevel extends PaintersByZoomLevel<AnnotationPainter>
    {
        public AnnotationPaintersByZoomLevel( DelegatePainter parent, double[] viewLevelsInNM )
        {
            super( parent, viewLevelsInNM );
        }

        @Override
        protected AnnotationPainter instantiatePainter( )
        {
            return new AnnotationPainter( );
        }

        @Override
        protected void clearPainterData( AnnotationPainter annotationPainter )
        {
            annotationPainter.clearAnnotations( );
        }
    }

    private static abstract class PaintersByZoomLevel<V extends GlimpsePainterImpl>
    {
        private NavigableMap<Double, V> painterMap;

        public PaintersByZoomLevel( DelegatePainter parent, double[] viewLevelsInNM )
        {
            painterMap = new TreeMap<Double, V>( );
            for ( double lowerZoomRange : viewLevelsInNM )
            {
                V painter = instantiatePainter( );
                parent.addPainter( painter );
                painterMap.put( Double.valueOf( lowerZoomRange ), painter );
            }
        }

        public void setPainterVisibilityAtViewLevel( double viewLevelInNM )
        {
            Double visibleThreshold = painterMap.lowerKey( viewLevelInNM );
            NavigableMap<Double, V> visiblePainterMap = painterMap.tailMap( visibleThreshold, true );
            internalSetVisibility( visiblePainterMap.values( ), true );

            NavigableMap<Double, V> nonVisiblePainterMap = painterMap.headMap( visibleThreshold, false );
            internalSetVisibility( nonVisiblePainterMap.values( ), false );
        }

        public V getPainterAtViewLevel( Double viewLevelInNM )
        {
            Entry<Double, V> entry = null;
            if ( viewLevelInNM == null )
            {
                entry = painterMap.lastEntry( );
            }
            else
            {
                entry = painterMap.lowerEntry( viewLevelInNM );
            }
            V painter = null;
            if ( entry != null ) painter = entry.getValue( );
            return painter;
        }

        public void clearPainterData( )
        {
            for ( V painter : painterMap.values( ) )
            {
                clearPainterData( painter );
            }
        }

        public void dispose( GlimpseContext context )
        {
            for ( V painter : painterMap.values( ) )
            {
                painter.dispose( context );
            }
        }

        private void internalSetVisibility( Collection<V> painters, boolean visible )
        {
            for ( GlimpsePainterImpl painter : painters )
            {
                painter.setVisible( visible );
            }
        }

        protected abstract V instantiatePainter( );

        protected abstract void clearPainterData( V painter );
    }
}

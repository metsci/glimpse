package com.metsci.glimpse.worldwind.tile;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.DrawContext;

import java.util.List;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.RateLimitedEventDispatcher;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

public class GlimpseResizingSurfaceTile extends GlimpseDynamicSurfaceTile
{
    protected int maxWidth;
    protected int maxHeight;
    
    protected int previousWidth;
    protected int previousHeight;
    
    protected double preferredPixelCount;
    
    protected RateLimitedEventDispatcher<?> dispatcher;
    protected volatile boolean updateSize;
    
    public GlimpseResizingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection projection, int preferredWidth, int preferredHeight, double minLat, double maxLat, double minLon, double maxLon )
    {
        this( layout, axes, projection, 8192, 8192, preferredWidth, preferredHeight, minLat, maxLat, minLon, maxLon );
    }
    
    
    public GlimpseResizingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection projection, int preferredWidth, int preferredHeight, List<LatLon> corners )
    {
        this( layout, axes, projection, 8192, 8192, preferredWidth, preferredHeight, corners );
    }
    
    
    public GlimpseResizingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection projection, int maxWidth, int maxHeight, int preferredWidth, int preferredHeight, double minLat, double maxLat, double minLon, double maxLon )
    {
        super( layout, axes, projection, preferredWidth, preferredHeight, minLat, maxLat, minLon, maxLon );
        
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.preferredPixelCount = width * height;
    }

    public GlimpseResizingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection projection, int maxWidth, int maxHeight, int preferredWidth, int preferredHeight, List<LatLon> corners )
    {
        super( layout, axes, projection, preferredWidth, preferredHeight, corners );
        
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.preferredPixelCount =  width * height;
        this.dispatcher = newRateLimitedEventDispatcher( );
    }
    
    protected RateLimitedEventDispatcher<?> newRateLimitedEventDispatcher( )
    {
        return new RateLimitedEventDispatcher<Object>( 200 )
        {
            @Override
            public void eventDispatch( Object data )
            {
                updateSize = true;
            }
        };
    }
    
    // modify the pixel dimensions of the canvas so that they approximately match the
    // dimensions of the tile which the texture will be drawn onto
    // this makes sure that things drawn in pixel space (like GL_POINTS) will
    // not appear distorted (much) when tiled onto WorldWind
    @Override
    protected void updateGeometry( DrawContext dc )
    {
        super.updateGeometry( dc );
        
        dispatcher.eventOccurred( null );
        
        if ( updateSize )
        {
            updateSize = false;
            
            double latSpan = bounds.maxLat - bounds.minLat;
            double lonSpan = bounds.maxLon - bounds.minLon;
            
            double ratio = ( latSpan / lonSpan );
            
            double fcalculatedHeight = Math.sqrt( preferredPixelCount * ratio );
            double fcalculatedWidth = preferredPixelCount / fcalculatedHeight;
            
            int calculatedHeight = (int) fcalculatedHeight;
            int calculatedWidth = (int) fcalculatedWidth;
            
            if ( calculatedHeight > maxHeight )
            {
                calculatedHeight = maxHeight;
                calculatedWidth = (int) Math.min( maxWidth, calculatedHeight * ratio );
            }
            else if ( calculatedWidth > maxWidth )
            {
                calculatedWidth = maxWidth;
                calculatedHeight = (int) Math.min( maxHeight, calculatedWidth * ( 1 / ratio ) );
            }
            
            if ( previousWidth != calculatedWidth || previousHeight != calculatedHeight )
            {
                previousWidth = calculatedWidth;
                previousHeight = calculatedHeight;
                offscreenCanvas.resize( calculatedWidth, calculatedHeight );
            }
        }
    }
}

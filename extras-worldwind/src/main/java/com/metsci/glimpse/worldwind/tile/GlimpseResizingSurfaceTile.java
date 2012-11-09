package com.metsci.glimpse.worldwind.tile;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.DrawContext;

import java.util.List;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

public class GlimpseResizingSurfaceTile extends GlimpseDynamicSurfaceTile
{
    protected int maxWidth;
    protected int maxHeight;
    
    protected int previousWidth;
    protected int previousHeight;
    
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
    }

    public GlimpseResizingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection projection, int maxWidth, int maxHeight, int preferredWidth, int preferredHeight, List<LatLon> corners )
    {
        super( layout, axes, projection, preferredWidth, preferredHeight, corners );
        
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }
    
    // modify the pixel dimensions of the canvas so that they approximately match the
    // dimensions of the tile which the texture will be drawn onto
    // this makes sure that things drawn in pixel space (like GL_POINTS) will
    // not appear distorted (much) when tiled onto WorldWind
    @Override
    protected void updateGeometry( DrawContext dc )
    {
        super.updateGeometry( dc );
        
        double latSpan = bounds.maxLat - bounds.minLat;
        double lonSpan = bounds.maxLon - bounds.minLon;
        
        int calculatedHeight = (int) ( width * ( latSpan / lonSpan ) );
        int calculatedWidth = (int) ( height * ( lonSpan / latSpan ) );
        
        if ( calculatedHeight < maxHeight )
        {
            if ( previousWidth != width || previousHeight != calculatedHeight )
            {
                previousWidth = width;
                previousHeight = calculatedHeight;
                offscreenCanvas.resize( width, calculatedHeight );
            }
        }
        else if ( calculatedWidth < maxWidth )
        {
            if ( previousWidth != calculatedWidth || previousHeight != height )
            {
                previousWidth = calculatedWidth;
                previousHeight = height;
                offscreenCanvas.resize( calculatedWidth, height );
            }
        }
        else if ( previousWidth != width || previousHeight != height )
        {
            previousWidth = width;
            previousHeight = height;
            offscreenCanvas.resize( width, height );
        }
    }
}

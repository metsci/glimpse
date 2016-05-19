/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.worldwind.tile;

import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;

import java.util.List;
import java.util.logging.Logger;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.DrawContext;

public class GlimpseResizingSurfaceTile extends GlimpseDynamicSurfaceTile
{
    private static final Logger logger = Logger.getLogger( GlimpseResizingSurfaceTile.class.getName( ) );

    // the maximum allowed size for the offscreen canvas
    protected int maxWidth;
    protected int maxHeight;

    // the current size of the offscreen canvas (all of which might not be being used)
    protected int currentWidth;
    protected int currentHeight;

    // the dimensions of the offscreen canvas currently being used
    // invariant: maxWidth >= currentWidth >= requestedWidth 
    protected int calculatedWidth;
    protected int calculatedHeight;

    // the percentage of the offscreen buffer filled by the texture
    protected float scaleX = 1.0f;
    protected float scaleY = 1.0f;

    protected double preferredPixelCount;

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
        this.preferredPixelCount = width * height;
    }
    
    public void setPreferredDimensions( int width, int height )
    {
        this.width = width;
        this.height = height;
        this.preferredPixelCount = width * height;
    }

    // modify the pixel dimensions of the canvas so that they approximately match the
    // dimensions of the tile which the texture will be drawn onto
    // this makes sure that things drawn in pixel space (like GL_POINTS) will
    // not appear distorted (much) when tiled onto WorldWind
    @Override
    protected void updateGeometry( DrawContext dc )
    {
        super.updateGeometry( dc );

        if ( bounds != null && tile != null )
        {
            double latSpan = bounds.maxLat - bounds.minLat;
            double lonSpan = bounds.maxLon - bounds.minLon;
    
            double ratio = ( latSpan / lonSpan );
    
            double fcalculatedHeight = Math.sqrt( preferredPixelCount * ratio );
            double fcalculatedWidth = preferredPixelCount / fcalculatedHeight;
    
            calculatedHeight = ( int ) fcalculatedHeight;
            calculatedWidth = ( int ) fcalculatedWidth;
    
            if ( calculatedHeight > maxHeight )
            {
                calculatedHeight = maxHeight;
                calculatedWidth = ( int ) Math.min( maxWidth, calculatedHeight * ratio );
            }
            else if ( calculatedWidth > maxWidth )
            {
                calculatedWidth = maxWidth;
                calculatedHeight = ( int ) Math.min( maxHeight, calculatedWidth * ( 1 / ratio ) );
            }
    
            if ( currentWidth < calculatedWidth || currentHeight < calculatedHeight )
            {
                // when we have to resize the canvas, do a bit more than necessary
                // to leave room for possible future growth
                int bufferedWidth = ( int ) ( Math.max( currentWidth, calculatedWidth ) * 1.1 );
                int bufferedHeight = ( int ) ( Math.max( currentHeight, calculatedHeight ) * 1.1 );
    
                currentWidth = Math.min( maxWidth, bufferedWidth );
                currentHeight = Math.min( maxHeight, bufferedHeight );
                resizeCanvas( currentWidth, currentHeight );
            }
    
            resizeLayout( calculatedWidth, calculatedHeight );
    
            scaleX = ( float ) calculatedWidth / ( float ) currentWidth;
            scaleY = ( float ) calculatedHeight / ( float ) currentHeight;
    
            setTextureScale( tile );
        }
    }

    @Override
    protected TextureSurfaceTile newTextureSurfaceTile( int textureHandle, Iterable<? extends LatLon> corners )
    {
        TextureSurfaceTile tile = new TextureSurfaceTile( textureHandle, corners );
        setTextureScale( tile );
        tile.setAlpha( alpha );
        return tile;
    }

    protected void setTextureScale( TextureSurfaceTile tile )
    {
        setTextureScale( tile, scaleX, scaleY );
    }

    protected void setTextureScale( TextureSurfaceTile tile, float scaleX, float scaleY )
    {
        if ( tile != null ) tile.setTextureScale( scaleX, scaleY );
    }

    protected void resizeLayout( int width, int height )
    {
        mask.setLayoutData( String.format( "pos 0 0 %d %d", width, height ) );
        background.invalidateLayout( );
    }

    protected void resizeCanvas( int width, int height )
    {
        logInfo( logger, "Resizing Offscreen Renderbuffer (%d x %d)", width, height );

        offscreenCanvas.resize( width, height );
    }
}

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
package com.metsci.glimpse.dnc;

import static com.metsci.glimpse.dnc.util.DncMiscUtils.nextPowerOfTwo;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static java.util.Collections.unmodifiableMap;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jogamp.opengl.util.packrect.Rect;
import com.jogamp.opengl.util.packrect.RectVisitor;
import com.jogamp.opengl.util.packrect.RectanglePacker;
import com.metsci.glimpse.dnc.util.AnchoredImage;
import com.metsci.glimpse.dnc.util.NullBackingStoreManager;
import com.metsci.glimpse.dnc.util.TexturableImage;

public class DncAtlases
{

    public static final int imagePadding = 1;


    public static <K> DncHostAtlas<K> createHostAtlas( final Map<K,AnchoredImage> anchoredImages, int maxTextureDim ) throws IOException
    {
        double totalArea = 0;
        for ( AnchoredImage anchoredImage : anchoredImages.values( ) )
        {
            int wPadded = anchoredImage.image.getWidth( ) + 2*imagePadding;
            int hPadded = anchoredImage.image.getHeight( ) + 2*imagePadding;
            totalArea += ( wPadded * hPadded );
        }

        // Initial dims must be at least 2, or RectanglePacker will hang
        int initialWidth = max( 2, min( maxTextureDim, nextPowerOfTwo( ( int ) ceil( sqrt( totalArea ) ) ) ) );
        int initialHeight = max( 2, ( int ) ceil( totalArea / initialWidth ) );
        RectanglePacker rectPacker = new RectanglePacker( new NullBackingStoreManager( ), initialWidth, initialHeight );
        rectPacker.setMaxSize( maxTextureDim, maxTextureDim );
        for ( Entry<K,AnchoredImage> en : anchoredImages.entrySet( ) )
        {
            K key = en.getKey( );
            AnchoredImage anchoredImage = en.getValue( );
            int wPadded = anchoredImage.image.getWidth( ) + 2*imagePadding;
            int hPadded = anchoredImage.image.getHeight( ) + 2*imagePadding;
            rectPacker.add( new Rect( 0, 0, wPadded, hPadded, key ) );
        }

        final Map<K,DncAtlasEntry> atlasEntries = new HashMap<>( );
        Dimension totalSize = ( Dimension ) rectPacker.getBackingStore( );
        final int wTotal = nextPowerOfTwo( totalSize.width );
        final int hTotal = nextPowerOfTwo( totalSize.height );
        TexturableImage atlasImage = new TexturableImage( wTotal, hTotal );
        final Graphics2D g = atlasImage.createGraphics( );
        rectPacker.visit( new RectVisitor( )
        {
            public void visit( Rect rect )
            {
                @SuppressWarnings( "unchecked" )
                K key = ( K ) rect.getUserData( );
                AnchoredImage anchoredImage = anchoredImages.get( key );
                int xPadded = rect.x( );
                int yPadded = rect.y( );
                int wPadded = rect.w( );
                int hPadded = rect.h( );

                int x = xPadded + imagePadding;
                int y = yPadded + imagePadding;
                int w = wPadded - 2*imagePadding;
                int h = hPadded - 2*imagePadding;
                g.drawImage( anchoredImage.image, x, y, w, h, null );

                float sMin = ( xPadded )           / ( float ) wTotal;
                float sMax = ( xPadded + wPadded ) / ( float ) wTotal;
                float tMin = ( yPadded )           / ( float ) hTotal;
                float tMax = ( yPadded + hPadded ) / ( float ) hTotal;

                // XXX: Off by half a pixel?
                float xAlign = ( anchoredImage.iAnchor + imagePadding ) / ( float ) wPadded;
                float yAlign = ( anchoredImage.jAnchor + imagePadding ) / ( float ) hPadded;

                atlasEntries.put( key, new DncAtlasEntry( wPadded, hPadded, sMin, sMax, tMin, tMax, xAlign, yAlign ) );
            }
        } );
        g.dispose( );

        return new DncHostAtlas<K>( atlasEntries, atlasImage );
    }


    public static class DncHostAtlas<K>
    {
        public final Map<K,DncAtlasEntry> entries;
        public final TexturableImage textureImage;

        public DncHostAtlas( Map<K,DncAtlasEntry> entries, TexturableImage textureImage )
        {
            this.entries = unmodifiableMap( entries );
            this.textureImage = textureImage;
        }
    }


    public static class DncAtlasEntry
    {
        // Subimage size, in pixels
        public final int w;
        public final int h;

        // Subimage bounds, as a fraction of atlas-texture dimensions
        public final float sMin;
        public final float sMax;
        public final float tMin;
        public final float tMax;

        // Alignment: 0 = bottom/left, 1 = top/right
        public final float xAlign;
        public final float yAlign;

        public DncAtlasEntry( int w, int h, float sMin, float sMax, float tMin, float tMax, float xAlign, float yAlign )
        {
            this.w = w;
            this.h = h;

            this.sMin = sMin;
            this.sMax = sMax;
            this.tMin = tMin;
            this.tMax = tMax;

            this.xAlign = xAlign;
            this.yAlign = yAlign;
        }
    }

}

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

import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.jogamp.common.nio.Buffers.newDirectFloatBuffer;
import static com.metsci.glimpse.dnc.DncAtlases.createHostAtlas;
import static com.metsci.glimpse.dnc.geosym.DncGeosymLabelUtils.newLabelImage;
import static com.metsci.glimpse.dnc.geosym.DncGeosymLabelUtils.toTextAttributes;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.poslim;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.sum;
import static com.metsci.glimpse.gl.util.GLUtils.genTexture;
import static com.metsci.glimpse.util.GeneralUtils.ints;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;

import java.awt.Color;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.opengl.GL;

import com.metsci.glimpse.dnc.DncAtlases.DncAtlasEntry;
import com.metsci.glimpse.dnc.DncAtlases.DncHostAtlas;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.dnc.DncChunks.DncGroup;
import com.metsci.glimpse.dnc.DncChunks.DncHostChunk;
import com.metsci.glimpse.dnc.geosym.DncGeosymLabelMaker;
import com.metsci.glimpse.dnc.geosym.DncGeosymLabelMaker.DncGeosymLabelMakerEntry;
import com.metsci.glimpse.dnc.util.AnchoredImage;
import com.metsci.glimpse.dnc.util.TexturableImage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class DncLabelAtlases
{

    // xAlign, yAlign
    public static final int coordsPerLabelAtlasAlign = 2;

    // sMin, tMin, sMax, tMax
    public static final int coordsPerLabelAtlasBounds = 4;


    public static DncHostLabelAtlas createHostLabelAtlas( DncHostChunk hChunk, CharBuffer charsBuf, IntBuffer lengthsBuf, Int2ObjectMap<Color> colors, int maxTextureDim, double screenDpi ) throws IOException
    {
        Map<Object,AnchoredImage> anchoredImages = new LinkedHashMap<>( );
        poslim( charsBuf, 0, 0, 1 );
        for ( DncGroup group : hChunk.groups )
        {
            List<DncGeosymLabelMaker> labelMakers = group.geosymAssignment.labelMakers;

            poslim( charsBuf, group.labelsCharFirst, 0, 1 );
            poslim( lengthsBuf, group.labelsLengthFirst, group.labelsLengthCount, 1 );
            while ( lengthsBuf.hasRemaining( ) )
            {
                for ( DncGeosymLabelMaker labelMaker : labelMakers )
                {
                    int numEntries = labelMaker.entries.size( );
                    int[] entryLengths = new int[ numEntries ];
                    lengthsBuf.get( entryLengths );

                    int charCount = sum( entryLengths );
                    if ( charCount > 0 )
                    {
                        int charFirst = charsBuf.limit( );
                        poslim( charsBuf, charFirst, charCount, 1 );
                        AttributedString text = new AttributedString( charsBuf.toString( ) );

                        int i = 0;
                        int entryStart = 0;
                        for ( DncGeosymLabelMakerEntry entry : labelMaker.entries )
                        {
                            int entryLength = entryLengths[ ( i++ ) ];
                            if ( entryLength > 0 )
                            {
                                int entryEnd = entryStart + entryLength;
                                Map<Attribute,Object> textAttrs = toTextAttributes( entry.textStyle, colors );
                                text.addAttributes( textAttrs, entryStart, entryEnd );
                                entryStart = entryEnd;
                            }
                        }

                        Object key = new Object( );
                        anchoredImages.put( key, newLabelImage( text, labelMaker.labelLocation, screenDpi ) );
                    }
                }
            }
        }
        if ( anchoredImages.isEmpty( ) ) return null;

        DncHostAtlas<Object> basicAtlas = createHostAtlas( anchoredImages, maxTextureDim );

        FloatBuffer entriesAlignBuf = newDirectFloatBuffer( coordsPerLabelAtlasAlign * basicAtlas.entries.size( ) );
        FloatBuffer entriesBoundsBuf = newDirectFloatBuffer( coordsPerLabelAtlasBounds * basicAtlas.entries.size( ) );
        for ( Object key : anchoredImages.keySet( ) )
        {
            DncAtlasEntry entry = basicAtlas.entries.get( key );

            entriesAlignBuf.put( entry.xAlign )
                           .put( entry.yAlign );

            entriesBoundsBuf.put( entry.sMin )
                            .put( entry.tMin )
                            .put( entry.sMax )
                            .put( entry.tMax );
        }
        entriesAlignBuf.flip( );
        entriesBoundsBuf.flip( );

        return new DncHostLabelAtlas( hChunk.chunkKey, entriesAlignBuf, entriesBoundsBuf, basicAtlas.textureImage );
    }


    public static DncDeviceLabelAtlas xferLabelAtlasToDevice( DncHostLabelAtlas hAtlas, GL gl )
    {
        TexturableImage textureImage = hAtlas.textureImage;
        int textureHandle = genTexture( gl );
        gl.glBindTexture( GL_TEXTURE_2D, textureHandle );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        textureImage.pushToTexture( gl, GL_TEXTURE_2D );

        int[] bufferHandles = new int[ 2 ];
        gl.glGenBuffers( 2, bufferHandles, 0 );

        int entriesAlignHandle = bufferHandles[ 0 ];
        gl.glBindBuffer( GL_ARRAY_BUFFER, entriesAlignHandle );
        gl.glBufferData( GL_ARRAY_BUFFER, hAtlas.entriesAlignBuf.remaining( ) * SIZEOF_FLOAT, hAtlas.entriesAlignBuf, GL_STATIC_DRAW );

        int entriesBoundsHandle = bufferHandles[ 1 ];
        gl.glBindBuffer( GL_ARRAY_BUFFER, entriesBoundsHandle );
        gl.glBufferData( GL_ARRAY_BUFFER, hAtlas.entriesBoundsBuf.remaining( ) * SIZEOF_FLOAT, hAtlas.entriesBoundsBuf, GL_STATIC_DRAW );

        return new DncDeviceLabelAtlas( hAtlas.chunkKey, textureImage.getWidth( ), textureImage.getHeight( ), entriesAlignHandle, entriesBoundsHandle, textureHandle );
    }


    public static class DncHostLabelAtlas
    {
        public final DncChunkKey chunkKey;
        public final FloatBuffer entriesAlignBuf;
        public final FloatBuffer entriesBoundsBuf;
        public final TexturableImage textureImage;

        public DncHostLabelAtlas( DncChunkKey chunkKey, FloatBuffer entriesAlignBuf, FloatBuffer entriesBoundsBuf, TexturableImage textureImage )
        {
            this.chunkKey = chunkKey;
            this.entriesAlignBuf = entriesAlignBuf;
            this.entriesBoundsBuf = entriesBoundsBuf;
            this.textureImage = textureImage;
        }
    }


    public static class DncDeviceLabelAtlas
    {
        public final DncChunkKey chunkKey;
        public final int textureWidth;
        public final int textureHeight;
        public final int entriesAlignHandle;
        public final int entriesBoundsHandle;
        public final int textureHandle;

        public DncDeviceLabelAtlas( DncChunkKey chunkKey, int textureWidth, int textureHeight, int entriesAlignHandle, int entriesBoundsHandle, int textureHandle )
        {
            this.chunkKey = chunkKey;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
            this.entriesAlignHandle = entriesAlignHandle;
            this.entriesBoundsHandle = entriesBoundsHandle;
            this.textureHandle = textureHandle;
        }

        public void dispose( GL gl )
        {
            gl.glDeleteTextures( 1, ints( textureHandle ), 0 );
            gl.glDeleteBuffers( 2, ints( entriesAlignHandle, entriesBoundsHandle ), 0 );
        }
    }

}

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

import static com.metsci.glimpse.dnc.DncAtlases.createHostAtlas;
import static com.metsci.glimpse.dnc.geosym.DncGeosymImageUtils.loadGeosymImage;
import static com.metsci.glimpse.gl.util.GLUtils.genTexture;
import static com.metsci.glimpse.util.GeneralUtils.ints;
import static java.util.Collections.unmodifiableMap;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.jogamp.opengl.GL;

import com.kitfox.svg.SVGException;
import com.metsci.glimpse.dnc.DncAtlases.DncAtlasEntry;
import com.metsci.glimpse.dnc.DncAtlases.DncHostAtlas;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.dnc.DncChunks.DncGroup;
import com.metsci.glimpse.dnc.DncChunks.DncHostChunk;
import com.metsci.glimpse.dnc.util.AnchoredImage;
import com.metsci.glimpse.dnc.util.TexturableImage;

public class DncIconAtlases
{

    public static DncHostIconAtlas createHostIconAtlas( DncHostChunk hChunk, String cgmDir, String svgDir, int maxTextureDim, double screenDpi ) throws IOException, SVGException
    {
        Map<String,AnchoredImage> anchoredImages = new LinkedHashMap<>( );
        for ( DncGroup group : hChunk.groups )
        {
            String pointSymbolId = group.geosymAssignment.pointSymbolId;
            if ( pointSymbolId != null && !pointSymbolId.isEmpty( ) && !anchoredImages.containsKey( pointSymbolId ) )
            {
                anchoredImages.put( pointSymbolId, loadGeosymImage( pointSymbolId, cgmDir, svgDir, screenDpi ) );
            }
        }
        if ( anchoredImages.isEmpty( ) ) return null;

        DncHostAtlas<String> basicAtlas = createHostAtlas( anchoredImages, maxTextureDim );
        return new DncHostIconAtlas( hChunk.chunkKey, basicAtlas.entries, basicAtlas.textureImage );
    }


    public static DncDeviceIconAtlas xferIconAtlasToDevice( DncHostIconAtlas hAtlas, GL gl )
    {
        int textureHandle = genTexture( gl );
        gl.glBindTexture( GL_TEXTURE_2D, textureHandle );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        hAtlas.textureImage.pushToTexture( gl, GL_TEXTURE_2D );
        return new DncDeviceIconAtlas( hAtlas.chunkKey, hAtlas.entries, textureHandle );
    }


    public static class DncHostIconAtlas
    {
        public final DncChunkKey chunkKey;
        public final Map<String,DncAtlasEntry> entries;
        public final TexturableImage textureImage;

        public DncHostIconAtlas( DncChunkKey chunkKey, Map<String,DncAtlasEntry> entries, TexturableImage textureImage )
        {
            this.chunkKey = chunkKey;
            this.entries = unmodifiableMap( entries );
            this.textureImage = textureImage;
        }
    }


    public static class DncDeviceIconAtlas
    {
        public final DncChunkKey chunkKey;
        public final Map<String,DncAtlasEntry> entries;
        public final int textureHandle;

        public DncDeviceIconAtlas( DncChunkKey chunkKey, Map<String,DncAtlasEntry> entries, int textureHandle )
        {
            this.chunkKey = chunkKey;
            this.entries = unmodifiableMap( entries );
            this.textureHandle = textureHandle;
        }

        public void dispose( GL gl )
        {
            gl.glDeleteTextures( 1, ints( textureHandle ), 0 );
        }
    }

}

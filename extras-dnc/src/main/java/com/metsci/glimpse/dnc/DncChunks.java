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

import static com.google.common.base.Objects.equal;
import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.metsci.glimpse.gl.util.GLUtils.genBuffer;
import static com.metsci.glimpse.util.GeneralUtils.ints;
import static java.util.Collections.unmodifiableCollection;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;

import com.jogamp.opengl.GL;

import com.google.common.base.Objects;
import com.metsci.glimpse.dnc.geosym.DncGeosymAssignment;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class DncChunks
{


    public static DncHostChunk createHostChunk( DncChunkKey chunkKey, int featureCount, IntBuffer groupsBuf, FloatBuffer verticesBuf, Int2ObjectMap<DncGeosymAssignment> geosymAssignments )
    {
        Collection<DncGroup> groups = new ArrayList<>( );
        while ( groupsBuf.hasRemaining( ) )
        {
            int geosymAssignmentId = groupsBuf.get( );

            int labelFirst = groupsBuf.get( );
            int labelCharFirst = groupsBuf.get( );
            int labelCharCount = groupsBuf.get( );
            int labelLengthFirst = groupsBuf.get( );
            int labelLengthCount = groupsBuf.get( );

            int vertexFirst = groupsBuf.get( );
            int trianglesCoordCount = groupsBuf.get( );
            int linesCoordCount = groupsBuf.get( );
            int iconsCoordCount = groupsBuf.get( );
            int labelsCoordCount = groupsBuf.get( );

            DncGeosymAssignment geosymAssignment = geosymAssignments.get( geosymAssignmentId );
            int trianglesCoordFirst = vertexFirst;
            int linesCoordFirst = trianglesCoordFirst + trianglesCoordCount;
            int iconsCoordFirst = linesCoordFirst + linesCoordCount;
            int labelsCoordFirst = iconsCoordFirst + iconsCoordCount;

            groups.add( new DncGroup( chunkKey,
                                      geosymAssignment,

                                      labelFirst,
                                      labelCharFirst,
                                      labelCharCount,
                                      labelLengthFirst,
                                      labelLengthCount,

                                      trianglesCoordFirst,
                                      trianglesCoordCount,
                                      linesCoordFirst,
                                      linesCoordCount,
                                      iconsCoordFirst,
                                      iconsCoordCount,
                                      labelsCoordFirst,
                                      labelsCoordCount ) );
        }

        return new DncHostChunk( chunkKey, featureCount, unmodifiableCollection( groups ), verticesBuf );
    }


    public static DncDeviceChunk xferChunkToDevice( DncHostChunk hChunk, GL gl )
    {
        int verticesHandle = genBuffer( gl );
        gl.glBindBuffer( GL_ARRAY_BUFFER, verticesHandle );
        gl.glBufferData( GL_ARRAY_BUFFER, hChunk.verticesBuf.remaining( ) * SIZEOF_FLOAT, hChunk.verticesBuf, GL_STATIC_DRAW );
        return new DncDeviceChunk( hChunk.chunkKey, hChunk.featureCount, hChunk.groups, verticesHandle );
    }


    public static class DncHostChunk
    {
        public final DncChunkKey chunkKey;
        public final int featureCount;
        public final Collection<DncGroup> groups;
        public final FloatBuffer verticesBuf;

        public DncHostChunk( DncChunkKey chunkKey, int featureCount, Collection<DncGroup> groups, FloatBuffer verticesBuf )
        {
            this.chunkKey = chunkKey;
            this.featureCount = featureCount;
            this.groups = unmodifiableCollection( groups );
            this.verticesBuf = verticesBuf;
        }
    }


    public static class DncDeviceChunk
    {
        public final DncChunkKey chunkKey;
        public final int featureCount;
        public final Collection<DncGroup> groups;
        public final int verticesHandle;

        public DncDeviceChunk( DncChunkKey chunkKey, int featureCount, Collection<DncGroup> groups, int verticesHandle )
        {
            this.chunkKey = chunkKey;
            this.featureCount = featureCount;
            this.groups = unmodifiableCollection( groups );
            this.verticesHandle = verticesHandle;
        }

        public void dispose( GL gl )
        {
            gl.glDeleteBuffers( 1, ints( verticesHandle ), 0 );
        }
    }


    public static class DncChunkKey
    {
        public final DncLibrary library;
        public final DncCoverage coverage;

        public DncChunkKey( DncLibrary library, DncCoverage coverage )
        {
            this.library = library;
            this.coverage = coverage;
        }

        @Override
        public int hashCode( )
        {
            return Objects.hashCode( library, coverage );
        }

        @Override
        public boolean equals( Object o )
        {
            if ( o == this ) return true;
            if ( o == null ) return false;
            if ( o.getClass( ) != getClass( ) ) return false;

            DncChunkKey other = ( DncChunkKey ) o;
            return ( equal( other.library, library ) && equal( other.coverage, coverage ) );
        }
    }


    public static class DncGroup
    {
        public final DncChunkKey chunkKey;
        public final DncGeosymAssignment geosymAssignment;

        public final int labelFirst;
        public final int labelsCharFirst;
        public final int labelsCharCount;
        public final int labelsLengthFirst;
        public final int labelsLengthCount;

        public final int trianglesCoordFirst;
        public final int trianglesCoordCount;
        public final int linesCoordFirst;
        public final int linesCoordCount;
        public final int iconsCoordFirst;
        public final int iconsCoordCount;
        public final int labelsCoordFirst;
        public final int labelsCoordCount;

        public DncGroup( DncChunkKey chunkKey,
                         DncGeosymAssignment geosymAssignment,

                         int labelFirst,
                         int labelsCharFirst,
                         int labelsCharCount,
                         int labelsLengthFirst,
                         int labelsLengthCount,

                         int trianglesCoordFirst,
                         int trianglesCoordCount,
                         int linesCoordFirst,
                         int linesCoordCount,
                         int iconsCoordFirst,
                         int iconsCoordCount,
                         int labelsCoordFirst,
                         int labelsCoordCount )
        {
            this.chunkKey = chunkKey;
            this.geosymAssignment = geosymAssignment;

            this.labelFirst = labelFirst;
            this.labelsCharFirst = labelsCharFirst;
            this.labelsCharCount = labelsCharCount;
            this.labelsLengthFirst = labelsLengthFirst;
            this.labelsLengthCount = labelsLengthCount;

            this.trianglesCoordFirst = trianglesCoordFirst;
            this.trianglesCoordCount = trianglesCoordCount;
            this.linesCoordFirst = linesCoordFirst;
            this.linesCoordCount = linesCoordCount;
            this.iconsCoordFirst = iconsCoordFirst;
            this.iconsCoordCount = iconsCoordCount;
            this.labelsCoordFirst = labelsCoordFirst;
            this.labelsCoordCount = labelsCoordCount;
        }
    }


}

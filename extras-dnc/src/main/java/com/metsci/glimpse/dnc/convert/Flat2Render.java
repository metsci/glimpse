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
package com.metsci.glimpse.dnc.convert;

import static com.google.common.base.Charsets.US_ASCII;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Objects.equal;
import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.jogamp.common.nio.Buffers.SIZEOF_INT;
import static com.jogamp.common.nio.Buffers.SIZEOF_LONG;
import static com.metsci.glimpse.dnc.DncDataPaths.glimpseDncFlatDir;
import static com.metsci.glimpse.dnc.DncDataPaths.glimpseDncRenderDir;
import static com.metsci.glimpse.dnc.DncPainterUtils.coverageSignificanceComparator;
import static com.metsci.glimpse.dnc.DncPainterUtils.libraryRenderingOrder;
import static com.metsci.glimpse.dnc.DncProjections.dncPlateCarree;
import static com.metsci.glimpse.dnc.convert.Flat.doublesPerFlatVertex;
import static com.metsci.glimpse.dnc.convert.Flat.flatChildDirs;
import static com.metsci.glimpse.dnc.convert.Flat.flatDatabaseNum;
import static com.metsci.glimpse.dnc.convert.Flat.flatFeatureDelineation;
import static com.metsci.glimpse.dnc.convert.Flat.intsPerFlatFeature;
import static com.metsci.glimpse.dnc.convert.Flat.intsPerFlatRing;
import static com.metsci.glimpse.dnc.convert.Flat.memmapFlatAttrsBuf;
import static com.metsci.glimpse.dnc.convert.Flat.memmapFlatLibrariesBuf;
import static com.metsci.glimpse.dnc.convert.Flat.memmapFlatRingsBuf;
import static com.metsci.glimpse.dnc.convert.Flat.memmapFlatStringsBuf;
import static com.metsci.glimpse.dnc.convert.Flat.memmapFlatVerticesBuf;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatAttrNames;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatAttrs;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatCharset;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatChecksum;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatChunks;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatCoverageNames;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatFcodeNames;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatLibraryNames;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_AREA_FEATURE;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_LINE_FEATURE;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_POINT_FEATURE;
import static com.metsci.glimpse.dnc.convert.Render.coordsPerRenderLabelVertex;
import static com.metsci.glimpse.dnc.convert.Render.intsPerRenderGroup;
import static com.metsci.glimpse.dnc.convert.Render.longsPerRenderChunk;
import static com.metsci.glimpse.dnc.convert.Render.readRenderCharset;
import static com.metsci.glimpse.dnc.convert.Render.readRenderChunks;
import static com.metsci.glimpse.dnc.convert.Render.readRenderConfig;
import static com.metsci.glimpse.dnc.convert.Render.readRenderCoveragesFile;
import static com.metsci.glimpse.dnc.convert.Render.readRenderLibrariesFile;
import static com.metsci.glimpse.dnc.convert.Render.renderChunksFilename;
import static com.metsci.glimpse.dnc.convert.Render.renderCoveragesFilename;
import static com.metsci.glimpse.dnc.convert.Render.renderCursorFilename;
import static com.metsci.glimpse.dnc.convert.Render.renderFormatVersion;
import static com.metsci.glimpse.dnc.convert.Render.renderGroupsFilename;
import static com.metsci.glimpse.dnc.convert.Render.renderLabelCharsFilename;
import static com.metsci.glimpse.dnc.convert.Render.renderLabelLengthsFilename;
import static com.metsci.glimpse.dnc.convert.Render.renderLibrariesFilename;
import static com.metsci.glimpse.dnc.convert.Render.renderMutexFilename;
import static com.metsci.glimpse.dnc.convert.Render.renderVerticesFilename;
import static com.metsci.glimpse.dnc.convert.Render.writeRenderCharset;
import static com.metsci.glimpse.dnc.convert.Render.writeRenderConfig;
import static com.metsci.glimpse.dnc.geosym.DncGeosymIo.geosymFullAssignmentsFile;
import static com.metsci.glimpse.dnc.geosym.DncGeosymIo.readDncSymbolAssignments;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.GiB;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.MiB;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.constFunc;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.invertIdsMap;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.invertList;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.memmapReadWrite;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.newAttrsFunc;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.newAttrsMap;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.newThreadFactory;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.poslim;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.sorted;
import static com.metsci.glimpse.dnc.util.FileSync.lockFile;
import static com.metsci.glimpse.dnc.util.FileSync.unlockFile;
import static com.metsci.glimpse.util.GeneralUtils.compareInts;
import static com.metsci.glimpse.util.GeneralUtils.compareLongs;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static com.metsci.glimpse.util.units.Angle.degreesToRadians;
import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.hash.Hashing;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.dnc.DncCoverage;
import com.metsci.glimpse.dnc.DncLibrary;
import com.metsci.glimpse.dnc.DncProjections.DncProjection;
import com.metsci.glimpse.dnc.convert.Flat.FlatChunkKey;
import com.metsci.glimpse.dnc.convert.Render.RenderChunk;
import com.metsci.glimpse.dnc.geosym.DncGeosymAssignment;
import com.metsci.glimpse.dnc.geosym.DncGeosymLabelMaker;
import com.metsci.glimpse.dnc.geosym.DncGeosymLabelMaker.DncGeosymLabelMakerEntry;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.support.polygon.Polygon.Interior;
import com.metsci.glimpse.support.polygon.Polygon.Loop.LoopBuilder;
import com.metsci.glimpse.support.polygon.PolygonTessellator;
import com.metsci.glimpse.support.polygon.PolygonTessellator.TessellationException;
import com.metsci.glimpse.support.polygon.VertexAccumulator;
import com.metsci.glimpse.util.primitives.CharsArray;
import com.metsci.glimpse.util.primitives.FloatsArray;
import com.metsci.glimpse.util.primitives.IntsArray;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;

public class Flat2Render
{

    protected static final Logger logger = getLogger( Flat2Render.class );



    public static class RenderCacheConfig
    {
        public File flatParentDir = glimpseDncFlatDir;
        public File renderParentDir = glimpseDncRenderDir;

        public DncProjection proj = dncPlateCarree;
        public int projPointsPerBoundsEdge = 2;

        public String geosymAssignmentsFilename = geosymFullAssignmentsFile;
        public Map<String,Object> externalAttrs = newAttrsMap( "isdm", 0,
                                                               "idsm", 1,
                                                               "ssdc", 18,
                                                               "msdc", 30,
                                                               "mssc", 2 );

        public long groupsFileSize = 25 * MiB;
        public long labelCharsFileSize = 100 * MiB;
        public long labelLengthsFileSize = 100 * MiB;
        public long verticesFileSize = 20 * GiB;
        public boolean reloadChunksTableBeforeConverting = false;
        public Charset charset = UTF_8;
    }



    public static class RenderCache
    {
        public final Int2ObjectMap<DncGeosymAssignment> geosymAssignments;
        public final List<DncLibrary> libraries;
        public final List<DncCoverage> coverages;

        protected final ExecutorService conversionExec;

        protected final Map<DncChunkKey,RenderChunk> chunks;
        protected final Int2ObjectMap<TransitionalDatabase> databases;
        protected final Object2IntMap<DncLibrary> libraryNums;
        protected final Object2IntMap<DncCoverage> coverageNums;
        protected final File renderDir;

        protected final boolean reloadChunksTableBeforeConverting;

        protected final File mutexFile;

        // Safe only for calling force()
        protected final MappedByteBuffer cursorMapped;
        protected final MappedByteBuffer chunksMapped;
        protected final MappedByteBuffer groupsMapped;
        protected final MappedByteBuffer labelCharsMapped;
        protected final MappedByteBuffer labelLengthsMapped;

        // Safe to access while holding mutexFile
        protected final IntBuffer cursorBuf;
        protected final LongBuffer chunksBuf;

        // Safe to duplicate while synced (on master buffer itself)
        protected final IntBuffer groupsBufMaster;
        protected final CharBuffer labelCharsBufMaster;
        protected final IntBuffer labelLengthsBufMaster;

        protected final FileChannel verticesChannel;


        public RenderCache( RenderCacheConfig config, int numConverterThreads ) throws IOException
        {
            File flatParentDir = config.flatParentDir;
            File renderParentDir = config.renderParentDir;
            long groupsFileSize = config.groupsFileSize;
            long labelCharsFileSize = config.labelCharsFileSize;
            long labelLengthsFileSize = config.labelLengthsFileSize;
            long verticesFileSize = config.verticesFileSize;


            this.conversionExec = newChunkJobsExec( "DncRenderCache", numConverterThreads );

            this.chunks = new HashMap<>( );

            this.geosymAssignments = Int2ObjectMaps.unmodifiable( readDncSymbolAssignments( config.geosymAssignmentsFilename ) );

            this.databases = new Int2ObjectLinkedOpenHashMap<>( );
            {
                DncProjection proj = config.proj;
                ListMultimap<String,DncGeosymAssignment> geosymAssignmentsByFcode = createFcodeToGeosymAssignmentsMap( geosymAssignments.values( ) );
                Function<String,Object> externalAttrs = newAttrsFunc( config.externalAttrs );
                for ( File flatDir : flatChildDirs( flatParentDir ) )
                {
                    int databaseNum = flatDatabaseNum( flatDir );
                    databases.put( databaseNum, new TransitionalDatabase( flatDir, proj, geosymAssignmentsByFcode, externalAttrs ) );
                }
            }


            String configString = renderConfigString( config );
            String configHash = Hashing.md5( ).newHasher( ).putString( configString, US_ASCII ).hash( ).toString( );
            this.renderDir = new File( renderParentDir, "dncRenderCache_" + configHash );
            renderDir.mkdirs( );

            this.mutexFile = new File( renderDir, renderMutexFilename );

            File librariesFile = new File( renderDir, renderLibrariesFilename );
            File coveragesFile = new File( renderDir, renderCoveragesFilename );

            File cursorFile       = new File( renderDir, renderCursorFilename       );
            File chunksFile       = new File( renderDir, renderChunksFilename       );
            File groupsFile       = new File( renderDir, renderGroupsFilename       );
            File labelCharsFile   = new File( renderDir, renderLabelCharsFilename   );
            File labelLengthsFile = new File( renderDir, renderLabelLengthsFilename );
            File verticesFile     = new File( renderDir, renderVerticesFilename     );


            this.reloadChunksTableBeforeConverting = config.reloadChunksTableBeforeConverting;

            mutexFile.createNewFile( );
            lockFile( mutexFile );
            try
            {
                if ( !cursorFile.exists( ) )
                {
                    Charset charset = config.charset;
                    writeRenderCharset( renderDir, charset );
                    writeRenderConfig( renderDir, configString, charset );
                    int libraryCount = writeRenderLibrariesFile( config, librariesFile, databases );
                    int coverageCount = writeRenderCoveragesFile( config, coveragesFile, databases );

                    RandomAccessFile chunksRaf = null;
                    RandomAccessFile groupsRaf = null;
                    RandomAccessFile labelCharsRaf = null;
                    RandomAccessFile labelLengthsRaf = null;
                    RandomAccessFile verticesRaf = null;
                    RandomAccessFile cursorRaf = null;
                    try
                    {
                        chunksRaf = new RandomAccessFile( chunksFile, "rw" );
                        chunksRaf.setLength( libraryCount * coverageCount * longsPerRenderChunk * SIZEOF_LONG );

                        groupsRaf = new RandomAccessFile( groupsFile, "rw" );
                        groupsRaf.setLength( groupsFileSize );

                        labelCharsRaf = new RandomAccessFile( labelCharsFile, "rw" );
                        labelCharsRaf.setLength( labelCharsFileSize );

                        labelLengthsRaf = new RandomAccessFile( labelLengthsFile, "rw" );
                        labelLengthsRaf.setLength( labelLengthsFileSize );

                        verticesRaf = new RandomAccessFile( verticesFile, "rw" );
                        verticesRaf.setLength( verticesFileSize );

                        chunksRaf.getFD( ).sync( );
                        groupsRaf.getFD( ).sync( );
                        labelCharsRaf.getFD( ).sync( );
                        labelLengthsRaf.getFD( ).sync( );
                        verticesRaf.getFD( ).sync( );

                        cursorRaf = new RandomAccessFile( cursorFile, "rw" );
                        cursorRaf.setLength( SIZEOF_INT );
                        cursorRaf.writeInt( 0 );
                        cursorRaf.getFD( ).sync( );
                    }
                    finally
                    {
                        if ( chunksRaf != null ) chunksRaf.close( );
                        if ( groupsRaf != null ) groupsRaf.close( );
                        if ( labelCharsRaf != null ) labelCharsRaf.close( );
                        if ( labelLengthsRaf != null ) labelLengthsRaf.close( );
                        if ( verticesRaf != null ) verticesRaf.close( );
                        if ( cursorRaf != null ) cursorRaf.close( );
                    }
                }

                Charset charset = readRenderCharset( renderDir );

                String existingConfigString = readRenderConfig( renderDir, charset );
                if ( !equal( existingConfigString, configString ) ) throw new RuntimeException( "Two different DNC render configs are in conflict, due to a hash collision -- either delete the existing cache dir (if it's not still in use), or use a different cache parent dir: cache-dir = " + renderDir );

                this.libraries = unmodifiableList( readRenderLibrariesFile( librariesFile, charset ) );
                this.coverages = unmodifiableList( readRenderCoveragesFile( coveragesFile, charset ) );

                this.libraryNums = Object2IntMaps.unmodifiable( invertList( libraries ) );
                this.coverageNums = Object2IntMaps.unmodifiable( invertList( coverages ) );

                this.chunksMapped = memmapReadWrite( chunksFile );
                this.chunksBuf = chunksMapped.asLongBuffer( );

                this.groupsMapped = memmapReadWrite( groupsFile );
                this.groupsBufMaster = groupsMapped.asIntBuffer( );

                this.labelCharsMapped = memmapReadWrite( labelCharsFile );
                this.labelCharsBufMaster = labelCharsMapped.asCharBuffer( );

                this.labelLengthsMapped = memmapReadWrite( labelLengthsFile );
                this.labelLengthsBufMaster = labelLengthsMapped.asIntBuffer( );

                this.verticesChannel = FileChannel.open( verticesFile.toPath( ), READ, WRITE );

                this.cursorMapped = memmapReadWrite( cursorFile );
                this.cursorBuf = cursorMapped.asIntBuffer( );

                synchronized ( chunks )
                {
                    int newChunkFirst = chunks.size( );
                    int newChunkCount = cursorBuf.get( 0 ) - newChunkFirst;
                    poslim( chunksBuf, newChunkFirst, newChunkCount, longsPerRenderChunk );
                    for ( RenderChunk newChunk : readRenderChunks( chunksBuf, libraries, coverages ) )
                    {
                        DncChunkKey newChunkKey = newChunk.chunkKey;
                        logger.finer( "Found externally converted chunk: database = " + newChunkKey.library.databaseNum + ", library = " + newChunkKey.library.libraryName + ", coverage = " + newChunkKey.coverage.coverageName );
                        chunks.put( newChunkKey, newChunk );
                    }
                }
            }
            finally
            {
                unlockFile( mutexFile );
            }
        }

        public IntBuffer sliceChunkGroups( RenderChunk chunk )
        {
            IntBuffer groupsBuf;
            synchronized ( groupsBufMaster )
            {
                groupsBuf = groupsBufMaster.duplicate( );
            }
            poslim( groupsBuf, chunk.groupFirst, chunk.groupCount, intsPerRenderGroup );
            return groupsBuf.slice( );
        }

        public FloatBuffer memmapChunkVertices( RenderChunk chunk ) throws IOException
        {
            MappedByteBuffer verticesMapped = verticesChannel.map( READ_ONLY, chunk.vertexCoordFirst * SIZEOF_FLOAT, chunk.vertexCoordCount * SIZEOF_FLOAT );
            verticesMapped.order( ByteOrder.nativeOrder( ) );
            return verticesMapped.asFloatBuffer( );
        }

        public CharBuffer sliceChunkLabelChars( RenderChunk chunk )
        {
            CharBuffer labelCharsBuf;
            synchronized ( labelCharsBufMaster )
            {
                labelCharsBuf = labelCharsBufMaster.duplicate( );
            }
            poslim( labelCharsBuf, chunk.labelCharFirst, chunk.labelCharCount, 1 );
            return labelCharsBuf.slice( );
        }

        public IntBuffer sliceChunkLabelLengths( RenderChunk chunk )
        {
            IntBuffer labelLengthsBuf;
            synchronized ( labelLengthsBufMaster )
            {
                labelLengthsBuf = labelLengthsBufMaster.duplicate( );
            }
            poslim( labelLengthsBuf, chunk.labelLengthFirst, chunk.labelLengthCount, 1 );
            return labelLengthsBuf.slice( );
        }

        public void getChunk( DncChunkKey chunkKey, Function<DncChunkKey,DncChunkPriority> priorityFunc, Consumer<RenderChunk> callback )
        {
            // Maybe it's already in the cache
            RenderChunk chunk;
            synchronized ( chunks )
            {
                chunk = chunks.get( chunkKey );
            }
            if ( chunk != null )
            {
                callback.accept( chunk );
                return;
            }

            // Not in the cache, so we have to convert it
            DncChunkPriority earlyPriority = priorityFunc.apply( chunkKey );
            if ( earlyPriority == DncChunkPriority.SKIP )
            {
                logger.finer( "Skipping chunk conversion: early-priority = " + earlyPriority + ", database = " + chunkKey.library.databaseNum + ", library = " + chunkKey.library.libraryName + ", coverage = " + chunkKey.coverage.coverageName );
            }
            else
            {
                long time_PMILLIS = System.currentTimeMillis( );
                logger.finer( "Enqueueing chunk for conversion: early-priority = " + earlyPriority + ", database = " + chunkKey.library.databaseNum + ", library = " + chunkKey.library.libraryName + ", coverage = " + chunkKey.coverage.coverageName );
                enqueueConversion( chunkKey, priorityFunc, callback, time_PMILLIS, earlyPriority, 0 );
            }
        }

        protected void enqueueConversion( final DncChunkKey chunkKey, final Function<DncChunkKey,DncChunkPriority> priorityFunc, final Consumer<RenderChunk> callback, final long origTime_PMILLIS, final DncChunkPriority earlyPriority, final int numDeferrals )
        {
            conversionExec.execute( new DncChunkJob( chunkKey, origTime_PMILLIS, earlyPriority )
            {
                public void runThrows( ) throws IOException
                {
                    // Maybe it's already in the cache
                    RenderChunk chunk;
                    synchronized ( chunks )
                    {
                        chunk = chunks.get( chunkKey );
                    }
                    if ( chunk != null )
                    {
                        callback.accept( chunk );
                        return;
                    }

                    // Not in the cache, so we have to convert it
                    DncChunkPriority latePriority = priorityFunc.apply( chunkKey );
                    long wait_MILLIS = System.currentTimeMillis( ) - origTime_PMILLIS;
                    if ( latePriority == DncChunkPriority.SKIP )
                    {
                        logger.finer( "Skipping chunk conversion: new-priority = " + latePriority + ", old-priority = " + earlyPriority + ", deferrals = " + numDeferrals + ", total-wait = " + wait_MILLIS + " ms, database = " + chunkKey.library.databaseNum + ", library = " + chunkKey.library.libraryName + ", coverage = " + chunkKey.coverage.coverageName );
                    }
                    else
                    {
                        if ( latePriority.priority < earlyPriority.priority )
                        {
                            logger.finer( "Deferring chunk conversion: new-priority = " + latePriority + ", old-priority = " + earlyPriority + ", prior-deferrals = " + numDeferrals + ", wait-so-far = " + wait_MILLIS + " ms, database = " + chunkKey.library.databaseNum + ", library = " + chunkKey.library.libraryName + ", coverage = " + chunkKey.coverage.coverageName );
                            enqueueConversion( chunkKey, priorityFunc, callback, origTime_PMILLIS, latePriority, numDeferrals + 1 );
                        }
                        else
                        {
                            logger.finer( "Converting chunk: late-priority = " + latePriority + ", early-priority = " + earlyPriority + ", deferrals = " + numDeferrals + ", total-wait = " + wait_MILLIS + " ms, database = " + chunkKey.library.databaseNum + ", library = " + chunkKey.library.libraryName + ", coverage = " + chunkKey.coverage.coverageName );
                            convertChunk( chunkKey, callback );
                        }
                    }
                }
            } );
        }

        protected void convertChunk( DncChunkKey chunkKey, Consumer<RenderChunk> callback ) throws IOException
        {
            DncLibrary library = chunkKey.library;
            DncCoverage coverage = chunkKey.coverage;

            int databaseNum = library.databaseNum;
            String libraryName = library.libraryName;
            String coverageName = coverage.coverageName;



            // Is this chunk already in the cache?
            //
            // The chunk we need may already have been cached by some other thread or process.
            // We can check here whether that's the case. However, in most cases, it's not worth
            // the synchronization that would be required -- the probability that some other
            // process has just written exactly the chunk we need is low, and the cost of redoing
            // the conversion here is not prohibitive.
            //
            // It's tempting to do this without the lockFile/unlockFile -- the data files only
            // ever get appended to, and here we're only reading data that's been written already.
            // However, the cursor file gets overwritten, so lockFile/unlockFile are required, in
            // case we're reading the cursor file at the same moment it's being written to.
            //
            if ( reloadChunksTableBeforeConverting )
            {
                lockFile( mutexFile );
                try
                {
                    RenderChunk chunk;
                    synchronized ( chunks )
                    {
                        int newChunkFirst = chunks.size( );
                        int newChunkCount = cursorBuf.get( 0 ) - newChunkFirst;
                        poslim( chunksBuf, newChunkFirst, newChunkCount, longsPerRenderChunk );
                        for ( RenderChunk newChunk : readRenderChunks( chunksBuf, libraries, coverages ) )
                        {
                            DncChunkKey newChunkKey = newChunk.chunkKey;
                            logger.finer( "Found externally converted chunk: database = " + newChunkKey.library.databaseNum + ", library = " + newChunkKey.library.libraryName + ", coverage = " + newChunkKey.coverage.coverageName );
                            chunks.put( newChunkKey, newChunk );
                        }

                        chunk = chunks.get( chunkKey );
                    }
                    if ( chunk != null )
                    {
                        callback.accept( chunk );
                        return;
                    }
                }
                finally
                {
                    unlockFile( mutexFile );
                }
            }



            // Load features, and convert to vertices
            //

            TransitionalDatabase database = databases.get( databaseNum );
            TransitionalChunk tChunk = database.createChunk( libraryName, coverageName );
            int featureCount = tChunk.featureCount;
            Collection<TransitionalGroup> groups = tChunk.groups;

            int labelCharCount = 0;
            int labelLengthCount = 0;
            int vertexCoordCount = 0;
            for ( TransitionalGroup group : groups )
            {
                labelCharCount += group.labelChars.n;
                labelLengthCount += group.labelLengths.n;
                vertexCoordCount += ( group.triangleCoords.n + group.lineCoords.n + group.iconCoords.n + group.labelCoords.n );
            }



            // Write to cache files
            //

            IntBuffer groupsBuf;
            synchronized ( groupsBufMaster )
            {
                groupsBuf = groupsBufMaster.duplicate( );
            }

            CharBuffer labelCharsBuf;
            synchronized ( labelCharsBufMaster )
            {
                labelCharsBuf = labelCharsBufMaster.duplicate( );
            }

            IntBuffer labelLengthsBuf;
            synchronized ( labelLengthsBufMaster )
            {
                labelLengthsBuf = labelLengthsBufMaster.duplicate( );
            }

            lockFile( mutexFile );
            try
            {

                // Is this chunk already in the cache?
                //

                RenderChunk chunk;
                int newChunkFirst;
                int newChunkCount;
                synchronized ( chunks )
                {
                    newChunkFirst = chunks.size( );
                    newChunkCount = cursorBuf.get( 0 ) - newChunkFirst;
                    poslim( chunksBuf, newChunkFirst, newChunkCount, longsPerRenderChunk );
                    for ( RenderChunk newChunk : readRenderChunks( chunksBuf, libraries, coverages ) )
                    {
                        DncChunkKey newChunkKey = newChunk.chunkKey;
                        logger.finer( "Found externally converted chunk: database = " + newChunkKey.library.databaseNum + ", library = " + newChunkKey.library.libraryName + ", coverage = " + newChunkKey.coverage.coverageName );
                        chunks.put( newChunkKey, newChunk );
                    }

                    chunk = chunks.get( chunkKey );
                }
                if ( chunk != null )
                {
                    callback.accept( chunk );
                    return;
                }



                // Position output buffers
                //

                int chunkNext = newChunkFirst + newChunkCount;
                int groupNext = 0;
                int labelCharNext = 0;
                int labelLengthNext = 0;
                long vertexCoordNext = 0;

                if ( chunkNext > 0 )
                {
                    chunksBuf.position( ( chunkNext - 1 ) * longsPerRenderChunk );
                    chunksBuf.get( ); // lastChunkLibraryNum
                    chunksBuf.get( ); // lastChunkCoverageNum
                    chunksBuf.get( ); // lastChunkFeatureCount
                    int lastChunkGroupFirst = ( int ) chunksBuf.get( );
                    int lastChunkGroupCount = ( int ) chunksBuf.get( );
                    int lastChunkLabelCharFirst = ( int ) chunksBuf.get( );
                    int lastChunkLabelCharCount = ( int ) chunksBuf.get( );
                    int lastChunkLabelLengthFirst = ( int ) chunksBuf.get( );
                    int lastChunkLabelLengthCount = ( int ) chunksBuf.get( );
                    long lastChunkVertexCoordFirst = chunksBuf.get( );
                    int lastChunkVertexCoordCount = ( int ) chunksBuf.get( );

                    groupNext = lastChunkGroupFirst + lastChunkGroupCount;
                    labelCharNext = lastChunkLabelCharFirst + lastChunkLabelCharCount;
                    labelLengthNext = lastChunkLabelLengthFirst + lastChunkLabelLengthCount;
                    vertexCoordNext = lastChunkVertexCoordFirst + lastChunkVertexCoordCount;
                }

                chunksBuf.limit( chunksBuf.capacity( ) );
                chunksBuf.position( chunkNext * longsPerRenderChunk );

                groupsBuf.limit( groupsBuf.capacity( ) );
                groupsBuf.position( groupNext * intsPerRenderGroup );

                labelCharsBuf.limit( labelCharsBuf.capacity( ) );
                labelCharsBuf.position( labelCharNext );

                labelLengthsBuf.limit( labelLengthsBuf.capacity( ) );
                labelLengthsBuf.position( labelLengthNext );


                int labelCharFirst = labelCharNext;
                int labelLengthFirst = labelLengthNext;
                long vertexCoordFirst = vertexCoordNext;


                // Memmap a section of the vertex file
                //

                MappedByteBuffer verticesMapped = verticesChannel.map( READ_WRITE, vertexCoordFirst * SIZEOF_FLOAT, vertexCoordCount * SIZEOF_FLOAT );
                verticesMapped.order( ByteOrder.nativeOrder( ) );
                FloatBuffer verticesBuf = verticesMapped.asFloatBuffer( );



                // Write group and vertex data to buffers
                //

                int groupFirst = groupNext;
                int groupCount = groups.size( );

                int nextGroupLabelFirst = 0;
                int nextGroupLabelCharFirst = 0;
                int nextGroupLabelLengthFirst = 0;
                int nextGroupVertexCoordFirst = 0;
                for ( TransitionalGroup group : groups )
                {
                    int groupLabelFirst = nextGroupLabelFirst;
                    int groupLabelCharFirst = nextGroupLabelCharFirst;
                    int groupLabelLengthFirst = nextGroupLabelLengthFirst;
                    int groupVertexCoordFirst = nextGroupVertexCoordFirst;

                    labelCharsBuf.put( group.labelChars.a, 0, group.labelChars.n );

                    labelLengthsBuf.put( group.labelLengths.a, 0, group.labelLengths.n );

                    verticesBuf.put( group.triangleCoords.a, 0, group.triangleCoords.n );
                    verticesBuf.put( group.lineCoords.a, 0, group.lineCoords.n );
                    verticesBuf.put( group.iconCoords.a, 0, group.iconCoords.n );
                    verticesBuf.put( group.labelCoords.a, 0, group.labelCoords.n );

                    groupsBuf.put( group.geosymAssignment.id )
                             .put( groupLabelFirst )
                             .put( groupLabelCharFirst )
                             .put( group.labelChars.n )
                             .put( groupLabelLengthFirst )
                             .put( group.labelLengths.n )
                             .put( groupVertexCoordFirst )
                             .put( group.triangleCoords.n )
                             .put( group.lineCoords.n )
                             .put( group.iconCoords.n )
                             .put( group.labelCoords.n );

                    nextGroupLabelFirst += ( group.labelCoords.n / coordsPerRenderLabelVertex );
                    nextGroupLabelCharFirst += group.labelChars.n;
                    nextGroupLabelLengthFirst += group.labelLengths.n;
                    nextGroupVertexCoordFirst += ( group.triangleCoords.n + group.lineCoords.n + group.iconCoords.n + group.labelCoords.n );
                }



                // Write chunk data to buffer
                //

                int libraryNum = libraryNums.get( library );
                int coverageNum = coverageNums.get( coverage );

                chunksBuf.put( libraryNum )
                         .put( coverageNum )
                         .put( featureCount )
                         .put( groupFirst )
                         .put( groupCount )
                         .put( labelCharFirst )
                         .put( labelCharCount )
                         .put( labelLengthFirst )
                         .put( labelLengthCount )
                         .put( vertexCoordFirst )
                         .put( vertexCoordCount );

                chunk = new RenderChunk( chunkKey, featureCount, groupFirst, groupCount, labelCharFirst, labelCharCount, labelLengthFirst, labelLengthCount, vertexCoordFirst, vertexCoordCount );
                synchronized ( chunks )
                {
                    logger.finer( "Finished converting chunk: database = " + library.databaseNum + ", library = " + library.libraryName + ", coverage = " + coverage.coverageName );
                    chunks.put( chunkKey, chunk );
                }



                // Run callback
                //

                callback.accept( chunk );



                // Flush buffered writes
                //
                // The writes we've made above don't necessarily become visible to other processes
                // in the order we wrote them. That's a problem, because a new row can appear in a
                // table-of-contents file before the corresponding data appears in the data file.
                //
                // We could enforce strict ordering by calling force() frequently, but that isn't
                // necessary, and would be slow. Instead, we can just guarantee that the top-level
                // table-of-contents write isn't visible until after all data writes are visible.
                // This is accomplished with the force-put-force sequence below.
                //

                verticesMapped.force( );
                groupsMapped.force( );
                labelCharsMapped.force( );
                labelLengthsMapped.force( );
                chunksMapped.force( );

                cursorBuf.put( 0, chunkNext + 1 );
                cursorMapped.force( );
            }
            finally
            {
                unlockFile( mutexFile );
            }
        }
    }



    public static class TransitionalChunk
    {
        public final int featureCount;
        public final Collection<TransitionalGroup> groups;

        public TransitionalChunk( int featureCount, Collection<TransitionalGroup> groups )
        {
            this.featureCount = featureCount;
            this.groups = unmodifiableCollection( groups );
        }
    }



    public static class TransitionalGroup
    {
        public final DncGeosymAssignment geosymAssignment;

        public final FloatsArray triangleCoords;

        public final FloatsArray lineCoords;

        public final FloatsArray iconCoords;

        public final FloatsArray labelCoords;
        public final IntsArray labelLengths;
        public final CharsArray labelChars;

        public TransitionalGroup( DncGeosymAssignment geosymAssignment )
        {
            this.geosymAssignment = geosymAssignment;

            this.triangleCoords = new FloatsArray( );

            this.lineCoords = new FloatsArray( );

            this.iconCoords = new FloatsArray( );

            this.labelCoords = new FloatsArray( );
            this.labelLengths = new IntsArray( );
            this.labelChars = new CharsArray( );
        }
    }



    public static class TransitionalDatabase
    {
        public final File flatDir;

        protected final DncProjection proj;
        protected final ListMultimap<String,DncGeosymAssignment> geosymAssignments;
        protected final Function<String,Object> externalAttrs;

        protected final Charset charset;
        protected final Int2ObjectMap<String> fcodeNames;
        protected final Int2ObjectMap<String> attrNames;
        public final Object2IntMap<String> flatLibraryNums;
        public final Object2IntMap<String> flatCoverageNums;

        protected final Object bufMutex;
        protected final Map<FlatChunkKey,IntBuffer> featuresBufMasters;
        protected final IntBuffer ringsBufMaster;
        protected final DoubleBuffer verticesBufMaster;
        protected final LongBuffer attrsBufMaster;
        protected final ByteBuffer stringsBufMaster;

        public TransitionalDatabase( File flatDir, DncProjection proj, ListMultimap<String,DncGeosymAssignment> geosymAssignments, Function<String,Object> externalAttrs ) throws IOException
        {
            this.flatDir = flatDir;
            this.proj = proj;
            this.geosymAssignments = geosymAssignments;
            this.externalAttrs = externalAttrs;

            this.charset = readFlatCharset( flatDir );
            this.fcodeNames = Int2ObjectMaps.unmodifiable( readFlatFcodeNames( flatDir, charset ) );
            this.attrNames = Int2ObjectMaps.unmodifiable( readFlatAttrNames( flatDir, charset ) );
            this.flatLibraryNums = Object2IntMaps.unmodifiable( invertIdsMap( readFlatLibraryNames( flatDir, charset ) ) );
            this.flatCoverageNums = Object2IntMaps.unmodifiable( invertIdsMap( readFlatCoverageNames( flatDir, charset ) ) );

            this.bufMutex = new Object( );
            this.featuresBufMasters = unmodifiableMap( readFlatChunks( flatDir ) );
            this.ringsBufMaster = memmapFlatRingsBuf( flatDir );
            this.verticesBufMaster = memmapFlatVerticesBuf( flatDir );
            this.attrsBufMaster = memmapFlatAttrsBuf( flatDir );
            this.stringsBufMaster = memmapFlatStringsBuf( flatDir );
        }

        public TransitionalChunk createChunk( String libraryName, String coverageName )
        {
            int flatLibraryNum = flatLibraryNums.get( libraryName );
            int flatCoverageNum = flatCoverageNums.get( coverageName );

            IntBuffer featuresBufMaster = featuresBufMasters.get( new FlatChunkKey( flatLibraryNum, flatCoverageNum ) );
            if ( featuresBufMaster == null )
            {
                return new TransitionalChunk( 0, emptyList( ) );
            }
            else
            {
                IntBuffer featuresBuf;
                IntBuffer ringsBuf;
                DoubleBuffer verticesBuf;
                LongBuffer attrsBuf;
                ByteBuffer stringsBuf;
                synchronized ( bufMutex )
                {
                    featuresBuf = featuresBufMaster.duplicate( );
                    ringsBuf = ringsBufMaster.duplicate( );
                    verticesBuf = verticesBufMaster.duplicate( );
                    attrsBuf = attrsBufMaster.duplicate( );
                    stringsBuf = stringsBufMaster.duplicate( );
                }

                int featureCount = featuresBuf.remaining( ) / intsPerFlatFeature;

                Int2ObjectMap<TransitionalGroup> groups = new Int2ObjectLinkedOpenHashMap<>( );
                for ( int featureNum = 0; featureNum < featureCount; featureNum++ )
                {
                    int fcodeId = featuresBuf.get( );
                    int featureTypeId = featuresBuf.get( );
                    int attrFirst = featuresBuf.get( );
                    int attrCount = featuresBuf.get( );
                    int featureItemFirst = featuresBuf.get( );
                    int featureItemCount = featuresBuf.get( );

                    String fcode = fcodeNames.get( fcodeId );
                    String featureDelineation = flatFeatureDelineation( featureTypeId );
                    Function<String,Object> featureAttrs = newAttrsFunc( readFlatAttrs( attrsBuf, attrFirst, attrCount, attrNames, stringsBuf, charset ) );

                    Set<TransitionalGroup> featureGroups = new HashSet<>( );
                    for ( DncGeosymAssignment geosymAssignment : geosymAssignments.get( fcode ) )
                    {
                        if ( geosymAssignment.matches( fcode, featureDelineation, coverageName, featureAttrs, externalAttrs ) )
                        {
                            if ( !groups.containsKey( geosymAssignment.id ) )
                            {
                                groups.put( geosymAssignment.id, new TransitionalGroup( geosymAssignment ) );
                            }
                            featureGroups.add( groups.get( geosymAssignment.id ) );
                        }
                    }

                    switch ( featureTypeId )
                    {
                        case FLAT_AREA_FEATURE: appendAreaFeature( featureNum, featureAttrs, ringsBuf, featureItemFirst, featureItemCount, verticesBuf, proj, featureGroups ); break;
                        case FLAT_LINE_FEATURE: appendLineFeature( featureNum, featureAttrs, verticesBuf, featureItemFirst, featureItemCount, proj, featureGroups ); break;
                        case FLAT_POINT_FEATURE: appendPointFeature( featureNum, featureAttrs, verticesBuf, featureItemFirst, proj, featureGroups ); break;
                        default: throw new RuntimeException( "Unrecognized feature-type ID: " + featureTypeId );
                    }
                }

                return new TransitionalChunk( featureCount, groups.values( ) );
            }
        }
    }



    // Misc I/O

    public static ListMultimap<String,DncGeosymAssignment> createFcodeToGeosymAssignmentsMap( Collection<DncGeosymAssignment> geosymAssignments ) throws IOException
    {
        ListMultimap<String,DncGeosymAssignment> map = LinkedListMultimap.create( );
        for ( DncGeosymAssignment a : geosymAssignments ) map.put( a.fcode, a );
        return map;
    }

    public static String renderConfigString( RenderCacheConfig config ) throws IOException
    {
        StringBuilder configString = new StringBuilder( );

        configString.append( "formatVersion = " ).append( renderFormatVersion ).append( "\n" );
        configString.append( "\n" );

        configString.append( "flatDirs =" ).append( "\n" );
        for ( File flatDir : flatChildDirs( config.flatParentDir ) )
        {
            configString.append( "    " ).append( flatDir.getName( ) ).append( " : " ).append( readFlatChecksum( flatDir ) ).append( "\n" );
        }
        configString.append( "\n" );

        configString.append( "proj = " ).append( config.proj.configString( ) ).append( "\n" );
        configString.append( "geosymAssignments = " ).append( config.geosymAssignmentsFilename ).append( "\n" );
        configString.append( "\n" );

        for ( Entry<String,Object> en : config.externalAttrs.entrySet( ) )
        {
            String attrName = en.getKey( );
            Object attrValue = en.getValue( );
            String attrString = ( attrValue == null ? "null" : attrValue.toString( ) );
            String attrType = ( attrValue == null ? "null" : attrValue.getClass( ).getName( ) );

            configString.append( attrName ).append( " = " ).append( attrString ).append( " (" ).append( attrType ).append( ")\n" );
        }

        return configString.toString( );
    }

    public static int writeRenderLibrariesFile( RenderCacheConfig config, File file, Int2ObjectMap<TransitionalDatabase> databases ) throws IOException
    {
        PrintStream stream = null;
        try
        {
            stream = new PrintStream( file, config.charset.name( ) );

            DncProjection proj = config.proj;
            int projPointsPerEdge = config.projPointsPerBoundsEdge;

            boolean alreadyHaveBrowseLibrary = false;
            float[] xyMinMaxTemp = new float[ 4 ];

            int libraryCount = 0;
            for ( Int2ObjectMap.Entry<TransitionalDatabase> en : databases.int2ObjectEntrySet( ) )
            {
                int databaseNum = en.getIntKey( );
                TransitionalDatabase database = en.getValue( );
                DoubleBuffer flatLibrariesBuf = memmapFlatLibrariesBuf( database.flatDir );
                for ( String libraryName : database.flatLibraryNums.keySet( ) )
                {
                    // We don't always use the library bounds, but we do always need to advance the buffer position
                    double minLat_DEG = flatLibrariesBuf.get( );
                    double maxLat_DEG = flatLibrariesBuf.get( );
                    double minLon_DEG = flatLibrariesBuf.get( );
                    double maxLon_DEG = flatLibrariesBuf.get( );

                    boolean canProject = proj.canProjectLibrary( databaseNum, libraryName, minLat_DEG, maxLat_DEG, minLon_DEG, maxLon_DEG );
                    if ( !canProject )
                    {
                        logger.fine( "Skipping a library that the projection cannot handle: database = " + databaseNum + ", library = " + libraryName );
                        continue;
                    }

                    boolean isBrowseLibrary = libraryName.equalsIgnoreCase( "BROWSE" );
                    if ( isBrowseLibrary && alreadyHaveBrowseLibrary ) continue;
                    alreadyHaveBrowseLibrary |= isBrowseLibrary;

                    computeXyMinMax( proj, projPointsPerEdge, minLat_DEG, maxLat_DEG, minLon_DEG, maxLon_DEG, xyMinMaxTemp, 0 );
                    float xMin = xyMinMaxTemp[ 0 ];
                    float xMax = xyMinMaxTemp[ 1 ];
                    float yMin = xyMinMaxTemp[ 2 ];
                    float yMax = xyMinMaxTemp[ 3 ];

                    stream.println( databaseNum + " " + libraryName + " " + xMin + " " + xMax + " " + yMin + " " + yMax );
                    libraryCount++;
                }
            }

            stream.flush( );
            return libraryCount;
        }
        finally
        {
            if ( stream != null ) stream.close( );
        }
    }

    public static int writeRenderCoveragesFile( RenderCacheConfig config, File file, Int2ObjectMap<TransitionalDatabase> databases ) throws IOException
    {
        PrintStream stream = null;
        try
        {
            stream = new PrintStream( file, config.charset.name( ) );

            Collection<String> coverageNames = new LinkedHashSet<>( );
            for ( Int2ObjectMap.Entry<TransitionalDatabase> en : databases.int2ObjectEntrySet( ) )
            {
                TransitionalDatabase database = en.getValue( );
                coverageNames.addAll( database.flatCoverageNums.keySet( ) );
            }
            for ( String coverageName : coverageNames )
            {
                stream.println( coverageName );
            }

            stream.flush( );

            return coverageNames.size( );
        }
        finally
        {
            if ( stream != null ) stream.close( );
        }
    }



    // Prioritization, async-exec, etc.

    public static enum DncChunkPriority
    {
        SKIP      ( -2, false ),
        NICE      ( -1, false ),
        DEFAULT   (  0, false ),
        SOON      (  1, true  ),
        IMMEDIATE (  2, true  );

        public final int priority;
        public final boolean lifo;

        private DncChunkPriority( int priority, boolean lifo )
        {
            this.priority = priority;
            this.lifo = lifo;
        }
    }

    public static abstract class DncChunkJob implements Runnable
    {
        public final DncChunkKey chunkKey;
        public final long submitTime_PMILLIS;
        public final DncChunkPriority priority;

        public DncChunkJob( DncChunkKey chunkKey, long submitTime_PMILLIS, DncChunkPriority priority )
        {
            this.chunkKey = chunkKey;
            this.submitTime_PMILLIS = submitTime_PMILLIS;
            this.priority = priority;
        }

        public abstract void runThrows( ) throws IOException;

        @Override
        public final void run( )
        {
            try
            {
                runThrows( );
            }
            catch ( IOException e )
            {
                throw new RuntimeException( e );
            }
        }
    }

    protected static final Comparator<Runnable> chunkJobComparator = new Comparator<Runnable>( )
    {
        public int compare( Runnable a0, Runnable b0 )
        {
            DncChunkJob a = ( a0 instanceof DncChunkJob ? ( DncChunkJob ) a0 : null );
            DncChunkJob b = ( b0 instanceof DncChunkJob ? ( DncChunkJob ) b0 : null );

            // High-priority before low-priority
            DncChunkPriority aPriority = ( a != null ? a.priority : DncChunkPriority.DEFAULT );
            DncChunkPriority bPriority = ( b != null ? b.priority : DncChunkPriority.DEFAULT );
            int priorityComparison = -compareInts( aPriority.priority, bPriority.priority );
            if ( priorityComparison != 0 ) return priorityComparison;

            if ( a != null && b != null )
            {
                // Some coverages are more visually important than others
                DncCoverage aCoverage = a.chunkKey.coverage;
                DncCoverage bCoverage = b.chunkKey.coverage;
                int coverageComparison = coverageSignificanceComparator.compare( aCoverage, bCoverage );
                if ( coverageComparison != 0 ) return coverageComparison;

                // If lifo, newer before older -- otherwise, older before newer
                int timeComparison = compareLongs( a.submitTime_PMILLIS, b.submitTime_PMILLIS );
                return ( aPriority.lifo ? -timeComparison : timeComparison );
            }
            else
            {
                return 0;
            }
        }
    };

    public static ExecutorService newChunkJobsExec( String threadNamePrefix, int numThreads )
    {
        return new ThreadPoolExecutor( numThreads,
                                       numThreads,
                                       0, MILLISECONDS,
                                       new PriorityBlockingQueue<Runnable>( 11, chunkJobComparator ),
                                       newThreadFactory( threadNamePrefix, true ) );
    }

    public static void convertAllChunks( RenderCache cache )
    {
        convertAllChunks( cache, 1, DncChunkPriority.NICE );
    }

    public static void convertAllChunks( RenderCache cache, int numSimultaneousConversions, DncChunkPriority priority )
    {
        // The first N chunks will find permits immediately available; after that, we have to
        // wait for a chunk to be finished (and its permit released) before starting a new one
        if ( numSimultaneousConversions < 1 ) throw new IllegalArgumentException( "Illegal number of simultaneous conversions: " + numSimultaneousConversions );
        final Semaphore permits = new Semaphore( numSimultaneousConversions );

        Function<DncChunkKey,DncChunkPriority> priorityFunc = constFunc( priority );
        List<DncCoverage> coverages = sorted( cache.coverages, coverageSignificanceComparator );
        List<DncLibrary> libraries = sorted( cache.libraries, libraryRenderingOrder );

        for ( DncCoverage coverage : coverages )
        {
            for ( DncLibrary library : libraries )
            {
                permits.acquireUninterruptibly( );

                DncChunkKey chunkKey = new DncChunkKey( library, coverage );
                cache.getChunk( chunkKey, priorityFunc, new Consumer<RenderChunk>( )
                {
                    public void accept( RenderChunk renderChunk )
                    {
                        permits.release( );
                    }
                } );
            }
        }
    }



    // Projection, tessellation, etc.

    public static final int coordsPerXy = 2;



    public static float[][] projectAreaFeatureRings( IntBuffer ringsBuf, int ringFirst, int ringCount, DoubleBuffer verticesBuf, DncProjection proj )
    {
        float[][] xyRings = new float[ ringCount ][];
        poslim( ringsBuf, ringFirst, ringCount, intsPerFlatRing );
        for ( int r = 0; r < ringCount; r++ )
        {
            int vertexFirst = ringsBuf.get( );
            int vertexCount = ringsBuf.get( );

            float[] xys = new float[ vertexCount * coordsPerXy ];
            poslim( verticesBuf, vertexFirst, vertexCount, doublesPerFlatVertex );
            for ( int v = 0; v < vertexCount; v++ )
            {
                double lat_DEG = verticesBuf.get( );
                double lon_DEG = verticesBuf.get( );
                proj.projectPos( lat_DEG, lon_DEG, xys, v * coordsPerXy );
            }
            xyRings[ r ] = xys;
        }
        return xyRings;
    }

    public static FloatsArray computeAreaFeatureTriangleCoords( int featureNum, float[][] xyRings )
    {
        try
        {
            Polygon polygon = new Polygon( );
            for ( float[] xys : xyRings )
            {
                LoopBuilder loop = new LoopBuilder( );
                int xyCount = xys.length / coordsPerXy;
                loop.addVertices( xys, xyCount );

                // XXX: loop.complete() does some data copying that could probably be avoided
                polygon.add( loop.complete( Interior.onLeft ) );
            }

            final FloatsArray triangleCoords = new FloatsArray( );
            PolygonTessellator tessellator = new PolygonTessellator( );
            tessellator.tessellate( polygon, new VertexAccumulator( )
            {
                public void addVertices( float[] xys, int xyCount )
                {
                    for ( int i = 0; i < ( xyCount * coordsPerXy ); i += coordsPerXy )
                    {
                        float x = xys[ i + 0 ];
                        float y = xys[ i + 1 ];

                        triangleCoords.append( x );
                        triangleCoords.append( y );
                        triangleCoords.append( featureNum );
                    }
                }

                public void addVertices( double[] xys, int xyCount )
                {
                    for ( int i = 0; i < ( xyCount * coordsPerXy ); i += coordsPerXy )
                    {
                        float x = ( float ) xys[ i + 0 ];
                        float y = ( float ) xys[ i + 1 ];

                        triangleCoords.append( x );
                        triangleCoords.append( y );
                        triangleCoords.append( featureNum );
                    }
                }
            } );
            return triangleCoords;
        }
        catch ( TessellationException e )
        {
            logWarning( logger, "Failed to tessellate area-feature fill; this feature's area will not be filled", e );
            return new FloatsArray( );
        }
    }

    public static FloatsArray computeAreaFeatureLineCoords( int featureNum, float[][] xyRings )
    {
        FloatsArray lineCoords = new FloatsArray( );
        for ( float[] xys : xyRings )
        {
            appendLineCoords( featureNum, xys, lineCoords );
        }
        return lineCoords;
    }

    public static void appendAreaFeature( int featureNum, Function<String,Object> attrs, IntBuffer ringsBuf, int ringFirst, int ringCount, DoubleBuffer verticesBuf, DncProjection proj, Set<TransitionalGroup> groups )
    {
        boolean needTriangles = haveAreaSymbol( groups );
        boolean needLines = haveLineSymbol( groups );
        boolean needIcons = havePointSymbol( groups );
        boolean needLabels = haveLabelMakers( groups );
        if ( needTriangles || needLines || needIcons || needLabels )
        {
            // Project
            float[][] xyRings = projectAreaFeatureRings( ringsBuf, ringFirst, ringCount, verticesBuf, proj );

            // Triangles
            if ( needTriangles )
            {
                FloatsArray triangleCoords = computeAreaFeatureTriangleCoords( featureNum, xyRings );
                for ( TransitionalGroup group : groups )
                {
                    if ( group.geosymAssignment.hasAreaSymbol( ) )
                    {
                        group.triangleCoords.append( triangleCoords );
                    }
                }
            }

            // Lines
            if ( needLines )
            {
                FloatsArray lineCoords = computeAreaFeatureLineCoords( featureNum, xyRings );
                for ( TransitionalGroup group : groups )
                {
                    if ( group.geosymAssignment.hasLineSymbol( ) )
                    {
                        group.lineCoords.append( lineCoords );
                    }
                }
            }

            // Icons, Labels
            if ( ( needIcons || needLabels ) && xyRings.length > 0 && xyRings[ 0 ].length >= coordsPerXy )
            {
                // Find the average xy of the outer ring
                float[] xys = xyRings[ 0 ];

                float xRef = xys[ 0 ];
                float yRef = xys[ 1 ];

                double xOffsetSum = 0;
                double yOffsetSum = 0;
                for ( int i = 0; i < xys.length; i += coordsPerXy )
                {
                    xOffsetSum += ( xys[ i + 0 ] - xRef );
                    yOffsetSum += ( xys[ i + 1 ] - yRef );
                }
                float xyCount = ( xys.length / coordsPerXy );
                float x = ( float ) ( xRef + ( xOffsetSum / xyCount ) );
                float y = ( float ) ( yRef + ( yOffsetSum / xyCount ) );

                // Icons
                if ( needIcons )
                {
                    for ( TransitionalGroup group : groups )
                    {
                        if ( group.geosymAssignment.hasPointSymbol( ) )
                        {
                            float rotation_CCWRAD = featureRotation_CCWRAD( group.geosymAssignment, attrs, proj, x, y );
                            group.iconCoords.append( x );
                            group.iconCoords.append( y );
                            group.iconCoords.append( featureNum );
                            group.iconCoords.append( rotation_CCWRAD );
                        }
                    }
                }

                // Labels
                if ( needLabels )
                {
                    for ( TransitionalGroup group : groups )
                    {
                        for ( DncGeosymLabelMaker labelMaker : group.geosymAssignment.labelMakers )
                        {
                            int charCount = 0;
                            for ( DncGeosymLabelMakerEntry entry : labelMaker.entries )
                            {
                                String text = entry.getLabelText( attrs );
                                if ( text == null )
                                {
                                    group.labelLengths.append( 0 );
                                }
                                else
                                {
                                    group.labelLengths.append( text.length( ) );
                                    group.labelChars.append( text );
                                    charCount += text.length( );
                                }
                            }
                            if ( charCount > 0 )
                            {
                                group.labelCoords.append( x );
                                group.labelCoords.append( y );
                                group.labelCoords.append( featureNum );
                            }
                        }
                    }
                }
            }
        }
    }



    public static float[] projectLineFeatureVertices( DoubleBuffer verticesBuf, int vertexFirst, int vertexCount, DncProjection proj )
    {
        float[] xys = new float[ vertexCount * coordsPerXy ];
        poslim( verticesBuf, vertexFirst, vertexCount, doublesPerFlatVertex );
        for ( int v = 0; v < vertexCount; v++ )
        {
            double lat_DEG = verticesBuf.get( );
            double lon_DEG = verticesBuf.get( );
            proj.projectPos( lat_DEG, lon_DEG, xys, v * coordsPerXy );
        }
        return xys;
    }

    public static FloatsArray computeLineFeatureLineCoords( int featureNum, float[] xys )
    {
        FloatsArray lineCoords = new FloatsArray( );
        appendLineCoords( featureNum, xys, lineCoords );
        return lineCoords;
    }

    public static void appendLineCoords( int featureNum, float[] xys, FloatsArray lineCoords )
    {
        double cumulativeDistance = 0;
        for ( int i = 0; true; i += coordsPerXy )
        {
            float xA = xys[ i + 0 ];
            float yA = xys[ i + 1 ];

            lineCoords.append( xA );
            lineCoords.append( yA );
            lineCoords.append( featureNum );
            lineCoords.append( ( float ) cumulativeDistance );

            if ( i + 3 >= xys.length )
            {
                break;
            }

            float xB = xys[ i + 2 ];
            float yB = xys[ i + 3 ];

            double dx = xB - xA;
            double dy = yB - yA;
            cumulativeDistance += sqrt( dx*dx + dy*dy );
        }
    }

    public static void appendLineFeature( int featureNum, Function<String,Object> attrs, DoubleBuffer verticesBuf, int vertexFirst, int vertexCount, DncProjection proj, Set<TransitionalGroup> groups )
    {
        boolean needLines = haveLineSymbol( groups );
        boolean needIcons = havePointSymbol( groups );
        boolean needLabels = haveLabelMakers( groups );
        if ( needLines || needIcons || needLabels )
        {
            // Project
            float[] xys = projectLineFeatureVertices( verticesBuf, vertexFirst, vertexCount, proj );

            // Lines
            if ( needLines )
            {
                FloatsArray lineCoords = computeLineFeatureLineCoords( featureNum, xys );
                for ( TransitionalGroup group : groups )
                {
                    if ( group.geosymAssignment.hasLineSymbol( ) )
                    {
                        group.lineCoords.append( lineCoords );
                    }
                }
            }

            // Icons, Labels
            if ( ( needIcons || needLabels ) && xys.length >= coordsPerXy )
            {
                // Find the xy halfway along the length of the line
                float x = xys[ 0 ];
                float y = xys[ 1 ];

                float dTotal = 0;
                for ( int i = 0; i < ( xys.length - coordsPerXy ); i += coordsPerXy )
                {
                    float x0 = xys[ i + 0 ];
                    float y0 = xys[ i + 1 ];
                    float x1 = xys[ i + 2 ];
                    float y1 = xys[ i + 3 ];

                    float dx = ( x1 - x0 );
                    float dy = ( y1 - y0 );
                    float dStep = ( float ) sqrt( dx*dx + dy*dy );

                    dTotal += dStep;
                }

                float dRemaining = 0.5f * dTotal;
                for ( int i = 0; i < ( xys.length - coordsPerXy ); i += coordsPerXy )
                {
                    float x0 = xys[ i + 0 ];
                    float y0 = xys[ i + 1 ];
                    float x1 = xys[ i + 2 ];
                    float y1 = xys[ i + 3 ];

                    float dx = ( x1 - x0 );
                    float dy = ( y1 - y0 );
                    float dStep = ( float ) sqrt( dx*dx + dy*dy );

                    if ( dStep >= dRemaining )
                    {
                        float alpha = dRemaining / dStep;
                        x = x0 + alpha*dx;
                        y = y0 + alpha*dy;
                        break;
                    }

                    dRemaining -= dStep;
                }

                // Icons
                if ( needIcons )
                {
                    for ( TransitionalGroup group : groups )
                    {
                        if ( group.geosymAssignment.hasPointSymbol( ) )
                        {
                            float rotation_CCWRAD = featureRotation_CCWRAD( group.geosymAssignment, attrs, proj, x, y );
                            group.iconCoords.append( x );
                            group.iconCoords.append( y );
                            group.iconCoords.append( featureNum );
                            group.iconCoords.append( rotation_CCWRAD );
                        }
                    }
                }

                // Labels
                if ( needLabels )
                {
                    for ( TransitionalGroup group : groups )
                    {
                        for ( DncGeosymLabelMaker labelMaker : group.geosymAssignment.labelMakers )
                        {
                            int charCount = 0;
                            for ( DncGeosymLabelMakerEntry entry : labelMaker.entries )
                            {
                                String text = entry.getLabelText( attrs );
                                if ( text == null )
                                {
                                    group.labelLengths.append( 0 );
                                }
                                else
                                {
                                    group.labelLengths.append( text.length( ) );
                                    group.labelChars.append( text );
                                    charCount += text.length( );
                                }
                            }
                            if ( charCount > 0 )
                            {
                                group.labelCoords.append( x );
                                group.labelCoords.append( y );
                                group.labelCoords.append( featureNum );
                            }
                        }
                    }
                }
            }
        }
    }



    public static float[] projectPointFeatureVertex( DoubleBuffer verticesBuf, int vertexIndex, DncProjection proj )
    {
        float[] xy = new float[ coordsPerXy ];
        poslim( verticesBuf, vertexIndex, 1, doublesPerFlatVertex );
        double lat_DEG = verticesBuf.get( );
        double lon_DEG = verticesBuf.get( );
        proj.projectPos( lat_DEG, lon_DEG, xy, 0 );
        return xy;
    }

    public static void appendPointFeature( int featureNum, Function<String,Object> attrs, DoubleBuffer verticesBuf, int vertexIndex, DncProjection proj, Set<TransitionalGroup> groups )
    {
        boolean needIcons = havePointSymbol( groups );
        boolean needLabels = haveLabelMakers( groups );
        if ( needIcons || needLabels )
        {
            // Project
            float[] xy = projectPointFeatureVertex( verticesBuf, vertexIndex, proj );
            float x = xy[ 0 ];
            float y = xy[ 1 ];

            // Icons
            if ( needIcons )
            {
                for ( TransitionalGroup group : groups )
                {
                    if ( group.geosymAssignment.hasPointSymbol( ) )
                    {
                        float rotation_CCWRAD = featureRotation_CCWRAD( group.geosymAssignment, attrs, proj, x, y );
                        group.iconCoords.append( x );
                        group.iconCoords.append( y );
                        group.iconCoords.append( featureNum );
                        group.iconCoords.append( rotation_CCWRAD );
                    }
                }
            }

            // Labels
            if ( needLabels )
            {
                for ( TransitionalGroup group : groups )
                {
                    for ( DncGeosymLabelMaker labelMaker : group.geosymAssignment.labelMakers )
                    {
                        int charCount = 0;
                        for ( DncGeosymLabelMakerEntry entry : labelMaker.entries )
                        {
                            String text = entry.getLabelText( attrs );
                            if ( text == null )
                            {
                                group.labelLengths.append( 0 );
                            }
                            else
                            {
                                group.labelLengths.append( text.length( ) );
                                group.labelChars.append( text );
                                charCount += text.length( );
                            }
                        }
                        if ( charCount > 0 )
                        {
                            group.labelCoords.append( x );
                            group.labelCoords.append( y );
                            group.labelCoords.append( featureNum );
                        }
                    }
                }
            }
        }
    }



    public static boolean haveAreaSymbol( Iterable<TransitionalGroup> groups )
    {
        for ( TransitionalGroup group : groups )
        {
            if ( group.geosymAssignment.hasAreaSymbol( ) ) return true;
        }
        return false;
    }

    public static boolean haveLineSymbol( Iterable<TransitionalGroup> groups )
    {
        for ( TransitionalGroup group : groups )
        {
            if ( group.geosymAssignment.hasLineSymbol( ) ) return true;
        }
        return false;
    }

    public static boolean havePointSymbol( Iterable<TransitionalGroup> groups )
    {
        for ( TransitionalGroup group : groups )
        {
            if ( group.geosymAssignment.hasPointSymbol( ) ) return true;
        }
        return false;
    }

    public static boolean haveLabelMakers( Iterable<TransitionalGroup> groups )
    {
        for ( TransitionalGroup group : groups )
        {
            if ( !group.geosymAssignment.labelMakers.isEmpty( ) ) return true;
        }
        return false;
    }

    public static float featureRotation_CCWRAD( DncGeosymAssignment geosymAssignment, Function<String,Object> featureAttrs, DncProjection proj, float x, float y )
    {
        Object orientationValue = featureAttrs.apply( geosymAssignment.orientationAttr );
        if ( orientationValue instanceof Number )
        {
            double unprojRotation_CWDEG = ( ( Number ) orientationValue ).doubleValue( );
            double unprojRotation_CCWRAD = degreesToRadians( -unprojRotation_CWDEG );
            return ( float ) proj.projectAzimuth_MATHRAD( x, y, unprojRotation_CCWRAD );
        }
        else
        {
            return 0;
        }
    }



    // Library Bounds

    public static void computeXyMinMax( DncProjection proj, int projPointsPerEdge, double minLat_DEG, double maxLat_DEG, double minLon_DEG, double maxLon_DEG, float[] result, int resultOffset )
    {
        float xMin = POSITIVE_INFINITY;
        float xMax = NEGATIVE_INFINITY;
        float yMin = POSITIVE_INFINITY;
        float yMax = NEGATIVE_INFINITY;

        projPointsPerEdge = max( 1, projPointsPerEdge );
        float[] xyTemp = new float[ 2 ];

        // North edge
        for ( int i = 0; i < projPointsPerEdge; i++ )
        {
            double a = ( ( double ) i ) / ( ( double ) projPointsPerEdge );
            double lon_DEG = ( 1 - a )*minLon_DEG + ( a )*maxLon_DEG;
            proj.projectPos( maxLat_DEG, lon_DEG, xyTemp, 0 );
            xMin = min( xMin, xyTemp[ 0 ] );
            xMax = max( xMax, xyTemp[ 0 ] );
            yMin = min( yMin, xyTemp[ 1 ] );
            yMax = max( yMax, xyTemp[ 1 ] );
        }

        // East edge
        for ( int i = 0; i < projPointsPerEdge; i++ )
        {
            double a = ( ( double ) i ) / ( ( double ) projPointsPerEdge );
            double lat_DEG = ( 1 - a )*maxLat_DEG + ( a )*minLat_DEG;
            proj.projectPos( lat_DEG, maxLon_DEG, xyTemp, 0 );
            xMin = min( xMin, xyTemp[ 0 ] );
            xMax = max( xMax, xyTemp[ 0 ] );
            yMin = min( yMin, xyTemp[ 1 ] );
            yMax = max( yMax, xyTemp[ 1 ] );
        }

        // South edge
        for ( int i = 0; i < projPointsPerEdge; i++ )
        {
            double a = ( ( double ) i ) / ( ( double ) projPointsPerEdge );
            double lon_DEG = ( 1 - a )*maxLon_DEG + ( a )*minLon_DEG;
            proj.projectPos( minLat_DEG, lon_DEG, xyTemp, 0 );
            xMin = min( xMin, xyTemp[ 0 ] );
            xMax = max( xMax, xyTemp[ 0 ] );
            yMin = min( yMin, xyTemp[ 1 ] );
            yMax = max( yMax, xyTemp[ 1 ] );
        }

        // West edge
        for ( int i = 0; i < projPointsPerEdge; i++ )
        {
            double a = ( ( double ) i ) / ( ( double ) projPointsPerEdge );
            double lat_DEG = ( 1 - a )*minLat_DEG + ( a )*maxLat_DEG;
            proj.projectPos( lat_DEG, minLon_DEG, xyTemp, 0 );
            xMin = min( xMin, xyTemp[ 0 ] );
            xMax = max( xMax, xyTemp[ 0 ] );
            yMin = min( yMin, xyTemp[ 1 ] );
            yMax = max( yMax, xyTemp[ 1 ] );
        }

        result[ resultOffset + 0 ] = xMin;
        result[ resultOffset + 1 ] = xMax;
        result[ resultOffset + 2 ] = yMin;
        result[ resultOffset + 3 ] = yMax;
    }

}

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
import static com.jogamp.common.nio.Buffers.SIZEOF_INT;
import static com.jogamp.common.nio.Buffers.SIZEOF_LONG;
import static com.metsci.glimpse.dnc.DncDataPaths.glimpseDncFlatDir;
import static com.metsci.glimpse.dnc.DncDataPaths.glimpseDncQueryDir;
import static com.metsci.glimpse.dnc.DncProjections.dncPlateCarree;
import static com.metsci.glimpse.dnc.convert.Flat.flatChildDirs;
import static com.metsci.glimpse.dnc.convert.Flat.flatDatabaseNum;
import static com.metsci.glimpse.dnc.convert.Flat.intsPerFlatFeature;
import static com.metsci.glimpse.dnc.convert.Flat.memmapFlatAttrsBuf;
import static com.metsci.glimpse.dnc.convert.Flat.memmapFlatLibrariesBuf;
import static com.metsci.glimpse.dnc.convert.Flat.memmapFlatRingsBuf;
import static com.metsci.glimpse.dnc.convert.Flat.memmapFlatStringsBuf;
import static com.metsci.glimpse.dnc.convert.Flat.memmapFlatVerticesBuf;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatAreaRings;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatAttrNames;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatAttrs;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatCharset;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatChecksum;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatChunks;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatCoverageNames;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatFcodeNames;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatLibraryNames;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatLineVertices;
import static com.metsci.glimpse.dnc.convert.Flat.readFlatPointVertex;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_AREA_FEATURE;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_LINE_FEATURE;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_POINT_FEATURE;
import static com.metsci.glimpse.dnc.convert.Flat2Render.computeAreaFeatureTriangleCoords;
import static com.metsci.glimpse.dnc.convert.Flat2Render.computeXyMinMax;
import static com.metsci.glimpse.dnc.convert.Flat2Render.coordsPerXy;
import static com.metsci.glimpse.dnc.convert.Flat2Render.newChunkJobsExec;
import static com.metsci.glimpse.dnc.convert.Flat2Render.projectAreaFeatureRings;
import static com.metsci.glimpse.dnc.convert.Flat2Render.projectLineFeatureVertices;
import static com.metsci.glimpse.dnc.convert.Flat2Render.projectPointFeatureVertex;
import static com.metsci.glimpse.dnc.convert.Flat2Render.DncChunkPriority.SKIP;
import static com.metsci.glimpse.dnc.convert.Query.intsPerQueryInteriorNode;
import static com.metsci.glimpse.dnc.convert.Query.intsPerQueryLeafNode;
import static com.metsci.glimpse.dnc.convert.Query.intsPerQueryLineItem;
import static com.metsci.glimpse.dnc.convert.Query.intsPerQueryPointItem;
import static com.metsci.glimpse.dnc.convert.Query.intsPerQueryTriangleItem;
import static com.metsci.glimpse.dnc.convert.Query.longsPerQueryChunk;
import static com.metsci.glimpse.dnc.convert.Query.queryChunksFilename;
import static com.metsci.glimpse.dnc.convert.Query.queryCoveragesFilename;
import static com.metsci.glimpse.dnc.convert.Query.queryCursorFilename;
import static com.metsci.glimpse.dnc.convert.Query.queryFormatVersion;
import static com.metsci.glimpse.dnc.convert.Query.queryLibrariesFilename;
import static com.metsci.glimpse.dnc.convert.Query.queryMutexFilename;
import static com.metsci.glimpse.dnc.convert.Query.queryTreesFilename;
import static com.metsci.glimpse.dnc.convert.Query.readQueryCharset;
import static com.metsci.glimpse.dnc.convert.Query.readQueryChunks;
import static com.metsci.glimpse.dnc.convert.Query.readQueryConfig;
import static com.metsci.glimpse.dnc.convert.Query.readQueryCoveragesFile;
import static com.metsci.glimpse.dnc.convert.Query.readQueryLibrariesFile;
import static com.metsci.glimpse.dnc.convert.Query.writeQueryCharset;
import static com.metsci.glimpse.dnc.convert.Query.writeQueryConfig;
import static com.metsci.glimpse.dnc.convert.Render.coordsPerRenderTriangleVertex;
import static com.metsci.glimpse.dnc.convert.Render.longsPerRenderChunk;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.GiB;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.MiB;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.invertIdsMap;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.invertList;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.memmapReadWrite;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.poslim;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.repchar;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.requireResult;
import static com.metsci.glimpse.dnc.util.FileSync.lockFile;
import static com.metsci.glimpse.dnc.util.FileSync.unlockFile;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static java.lang.Float.floatToIntBits;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.ulp;
import static java.lang.System.currentTimeMillis;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Arrays.asList;
import static java.util.Arrays.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.google.common.hash.Hashing;
import com.metsci.glimpse.dnc.DncAreaFeature;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.dnc.DncCoverage;
import com.metsci.glimpse.dnc.DncFeature;
import com.metsci.glimpse.dnc.DncLibrary;
import com.metsci.glimpse.dnc.DncLineFeature;
import com.metsci.glimpse.dnc.DncPointFeature;
import com.metsci.glimpse.dnc.DncProjections.DncProjection;
import com.metsci.glimpse.dnc.DncQuery;
import com.metsci.glimpse.dnc.DncTree;
import com.metsci.glimpse.dnc.convert.Flat.FlatChunkKey;
import com.metsci.glimpse.dnc.convert.Flat2Render.DncChunkJob;
import com.metsci.glimpse.dnc.convert.Flat2Render.DncChunkPriority;
import com.metsci.glimpse.dnc.convert.Query.QueryChunk;
import com.metsci.glimpse.dnc.util.ToFloatFunction;
import com.metsci.glimpse.util.primitives.FloatsArray;
import com.metsci.glimpse.util.primitives.IntsArray;
import com.metsci.glimpse.util.primitives.IntsModifiable;
import com.metsci.glimpse.util.primitives.sorted.SortedFloats;
import com.metsci.glimpse.util.primitives.sorted.SortedFloatsArray;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;

public class Flat2Query
{

    protected static final Logger logger = getLogger( Flat2Query.class );



    public static class QueryCacheConfig
    {
        public File flatParentDir = glimpseDncFlatDir;
        public File queryParentDir = glimpseDncQueryDir;

        public DncProjection proj = dncPlateCarree;
        public int projPointsPerBoundsEdge = 2;

        public long chunksFileSize = 25 * MiB;
        public long treesFileSize = 50 * GiB;
        public boolean reloadChunksTableBeforeConverting = false;
        public Charset charset = UTF_8;
    }



    public static class QueryCache
    {
        public final List<DncLibrary> libraries;
        public final List<DncCoverage> coverages;

        protected final ExecutorService conversionExec;

        protected final Map<DncChunkKey,DncTree> trees;
        protected final Int2ObjectMap<QueryDatabase> databases;
        protected final Object2IntMap<DncLibrary> libraryNums;
        protected final Object2IntMap<DncCoverage> coverageNums;

        protected final boolean reloadChunksTableBeforeConverting;

        protected final File mutexFile;

        // Safe only for calling force()
        protected final MappedByteBuffer cursorMapped;
        protected final MappedByteBuffer chunksMapped;

        // Safe to access while holding mutexFile
        protected final IntBuffer cursorBuf;
        protected final LongBuffer chunksBuf;

        protected final FileChannel treesChannel;


        public QueryCache( QueryCacheConfig config, int numConverterThreads ) throws IOException
        {
            File flatParentDir = config.flatParentDir;
            File queryParentDir = config.queryParentDir;


            this.conversionExec = newChunkJobsExec( "DncQueryCache", numConverterThreads );

            this.trees = new HashMap<>( );

            this.databases = new Int2ObjectLinkedOpenHashMap<>( );
            {
                // Using the same thread-pool could cause deadlock, so use a separate one
                ExecutorService treeBuilderExec = newChunkJobsExec( "DncTreeBuilder", numConverterThreads );

                DncProjection proj = config.proj;
                for ( File flatDir : flatChildDirs( flatParentDir ) )
                {
                    int databaseNum = flatDatabaseNum( flatDir );
                    databases.put( databaseNum, new QueryDatabase( flatDir, proj, treeBuilderExec ) );
                }
            }


            String configString = queryConfigString( config );
            String configHash = Hashing.md5( ).newHasher( ).putString( configString, US_ASCII ).hash( ).toString( );
            File queryDir = new File( queryParentDir, "dncQueryCache_" + configHash );
            queryDir.mkdirs( );

            this.mutexFile = new File( queryDir, queryMutexFilename );

            File librariesFile = new File( queryDir, queryLibrariesFilename );
            File coveragesFile = new File( queryDir, queryCoveragesFilename );

            File cursorFile = new File( queryDir, queryCursorFilename );
            File chunksFile = new File( queryDir, queryChunksFilename );
            File treesFile  = new File( queryDir, queryTreesFilename  );


            this.reloadChunksTableBeforeConverting = config.reloadChunksTableBeforeConverting;

            mutexFile.createNewFile( );
            lockFile( mutexFile );
            try
            {
                if ( !cursorFile.exists( ) )
                {
                    Charset charset = config.charset;
                    writeQueryCharset( queryDir, charset );
                    writeQueryConfig( queryDir, configString, charset );
                    int libraryCount = writeQueryLibrariesFile( config, librariesFile, databases );
                    int coverageCount = writeQueryCoveragesFile( config, coveragesFile, databases );

                    RandomAccessFile chunksRaf = null;
                    RandomAccessFile treesRaf = null;
                    RandomAccessFile cursorRaf = null;
                    try
                    {
                        chunksRaf = new RandomAccessFile( chunksFile, "rw" );
                        chunksRaf.setLength( libraryCount * coverageCount * longsPerRenderChunk * SIZEOF_LONG );

                        treesRaf = new RandomAccessFile( treesFile, "rw" );
                        treesRaf.setLength( config.treesFileSize );

                        chunksRaf.getFD( ).sync( );
                        treesRaf.getFD( ).sync( );

                        cursorRaf = new RandomAccessFile( cursorFile, "rw" );
                        cursorRaf.setLength( SIZEOF_INT );
                        cursorRaf.writeInt( 0 );
                        cursorRaf.getFD( ).sync( );
                    }
                    finally
                    {
                        if ( chunksRaf != null ) chunksRaf.close( );
                        if ( treesRaf != null ) treesRaf.close( );
                        if ( cursorRaf != null ) cursorRaf.close( );
                    }
                }

                Charset charset = readQueryCharset( queryDir );

                String existingConfigString = readQueryConfig( queryDir, charset );
                if ( !equal( existingConfigString, configString ) ) throw new RuntimeException( "Two different DNC query configs are in conflict, due to a hash collision -- either delete the existing cache dir (if it's not still in use), or use a different cache parent dir: cache-dir = " + queryDir );

                this.libraries = unmodifiableList( readQueryLibrariesFile( librariesFile, charset ) );
                this.coverages = unmodifiableList( readQueryCoveragesFile( coveragesFile, charset ) );

                this.libraryNums = Object2IntMaps.unmodifiable( invertList( libraries ) );
                this.coverageNums = Object2IntMaps.unmodifiable( invertList( coverages ) );

                this.chunksMapped = memmapReadWrite( chunksFile );
                this.chunksBuf = chunksMapped.asLongBuffer( );

                this.treesChannel = FileChannel.open( treesFile.toPath( ), READ, WRITE );

                this.cursorMapped = memmapReadWrite( cursorFile );
                this.cursorBuf = cursorMapped.asIntBuffer( );

                synchronized ( trees )
                {
                    int newChunkFirst = trees.size( );
                    int newChunkCount = cursorBuf.get( 0 ) - newChunkFirst;
                    poslim( chunksBuf, newChunkFirst, newChunkCount, longsPerQueryChunk );
                    for ( QueryChunk newChunk : readQueryChunks( chunksBuf, libraries, coverages ) )
                    {
                        DncChunkKey newChunkKey = newChunk.chunkKey;
                        logger.finer( "Found externally converted chunk: database = " + newChunkKey.library.databaseNum + ", library = " + newChunkKey.library.libraryName + ", coverage = " + newChunkKey.coverage.coverageName );
                        DncTree newTree = createTree( newChunk );
                        trees.put( newChunkKey, newTree );
                    }
                }
            }
            finally
            {
                unlockFile( mutexFile );
            }
        }

        protected DncTree createTree( QueryChunk chunk ) throws IOException
        {
            MappedByteBuffer chunkMapped = treesChannel.map( READ_ONLY, chunk.treeWordFirst * SIZEOF_INT, chunk.treeWordCount * SIZEOF_INT );
            chunkMapped.order( ByteOrder.nativeOrder( ) );
            IntBuffer chunkBuf = chunkMapped.asIntBuffer( );

            int interiorNodeWordFirst = 0;
            int interiorNodeWordCount = chunk.interiorNodeCount * intsPerQueryInteriorNode;
            poslim( chunkBuf, interiorNodeWordFirst, interiorNodeWordCount, 1 );
            IntBuffer interiorNodesBuf = chunkBuf.slice( );

            int leafNodeWordFirst = interiorNodeWordFirst + interiorNodeWordCount;
            int leafNodeWordCount = chunk.leafNodeCount * intsPerQueryLeafNode;
            poslim( chunkBuf, leafNodeWordFirst, leafNodeWordCount, 1 );
            IntBuffer leafNodesBuf = chunkBuf.slice( );

            int pointItemWordFirst = leafNodeWordFirst + leafNodeWordCount;
            int pointItemWordCount = chunk.pointItemCount * intsPerQueryPointItem;
            poslim( chunkBuf, pointItemWordFirst, pointItemWordCount, 1 );
            IntBuffer pointsBuf = chunkBuf.slice( );

            int lineItemWordFirst = pointItemWordFirst + pointItemWordCount;
            int lineItemWordCount = chunk.lineItemCount * intsPerQueryLineItem;
            poslim( chunkBuf, lineItemWordFirst, lineItemWordCount, 1 );
            IntBuffer linesBuf = chunkBuf.slice( );

            int triangleItemWordFirst = lineItemWordFirst + lineItemWordCount;
            int triangleItemWordCount = chunk.triangleItemCount * intsPerQueryTriangleItem;
            poslim( chunkBuf, triangleItemWordFirst, triangleItemWordCount, 1 );
            IntBuffer trianglesBuf = chunkBuf.slice( );

            return new DncTree( interiorNodesBuf, leafNodesBuf, pointsBuf, linesBuf, trianglesBuf );
        }

        public void runQuery( DncQuery query, Function<DncChunkKey,DncChunkPriority> priorityFn, BiConsumer<DncChunkKey,Collection<DncFeature>> callback )
        {
            for ( DncChunkKey chunkKey : query.chunkKeys )
            {
                getChunk( chunkKey, priorityFn, ( tree ) ->
                {
                    IntSet featureNums = tree.search( query.xMin, query.xMax, query.yMin, query.yMax );
                    Int2ObjectMap<DncFeature> features = loadFeatures( chunkKey, featureNums );
                    callback.accept( chunkKey, features.values( ) );
                } );
            }
        }

        public Int2ObjectMap<DncFeature> loadFeatures( DncChunkKey chunkKey, IntCollection featureNums )
        {
            // XXX: Try caching DncFeature instances (might not make much difference, though)
            int databaseNum = chunkKey.library.databaseNum;
            QueryDatabase database = databases.get( databaseNum );
            return database.loadFeatures( chunkKey, featureNums );
        }

        public void getChunk( DncChunkKey chunkKey, Function<DncChunkKey,DncChunkPriority> priorityFunc, Consumer<DncTree> callback )
        {
            // Maybe it's already in the cache
            DncTree tree;
            synchronized ( trees )
            {
                tree = trees.get( chunkKey );
            }
            if ( tree != null )
            {
                callback.accept( tree );
                return;
            }

            // Not in the cache, so we have to convert it
            DncChunkPriority earlyPriority = priorityFunc.apply( chunkKey );
            if ( earlyPriority == SKIP )
            {
                logger.finer( "Skipping chunk conversion: early-priority = " + earlyPriority + ", database = " + chunkKey.library.databaseNum + ", library = " + chunkKey.library.libraryName + ", coverage = " + chunkKey.coverage.coverageName );
            }
            else
            {
                long time_PMILLIS = currentTimeMillis( );
                logger.finer( "Enqueueing chunk for conversion: early-priority = " + earlyPriority + ", database = " + chunkKey.library.databaseNum + ", library = " + chunkKey.library.libraryName + ", coverage = " + chunkKey.coverage.coverageName );
                enqueueConversion( chunkKey, priorityFunc, callback, time_PMILLIS, earlyPriority, 0 );
            }
        }

        protected void enqueueConversion( DncChunkKey chunkKey, Function<DncChunkKey,DncChunkPriority> priorityFunc, Consumer<DncTree> callback, long origTime_PMILLIS, DncChunkPriority earlyPriority, int numDeferrals )
        {
            conversionExec.execute( new DncChunkJob( chunkKey, origTime_PMILLIS, earlyPriority )
            {
                public void runThrows( ) throws IOException
                {
                    // Maybe it's already in the cache
                    DncTree tree;
                    synchronized ( trees )
                    {
                        tree = trees.get( chunkKey );
                    }
                    if ( tree != null )
                    {
                        callback.accept( tree );
                        return;
                    }

                    // Not in the cache, so we have to convert it
                    DncChunkPriority latePriority = priorityFunc.apply( chunkKey );
                    long wait_MILLIS = currentTimeMillis( ) - origTime_PMILLIS;
                    if ( latePriority == SKIP )
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

        protected void convertChunk( DncChunkKey chunkKey, Consumer<DncTree> callback ) throws IOException
        {
            DncLibrary library = chunkKey.library;
            DncCoverage coverage = chunkKey.coverage;

            int databaseNum = library.databaseNum;



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
                    DncTree tree;
                    synchronized ( trees )
                    {
                        int newChunkFirst = trees.size( );
                        int newChunkCount = cursorBuf.get( 0 ) - newChunkFirst;
                        poslim( chunksBuf, newChunkFirst, newChunkCount, longsPerQueryChunk );
                        for ( QueryChunk newChunk : readQueryChunks( chunksBuf, libraries, coverages ) )
                        {
                            DncChunkKey newChunkKey = newChunk.chunkKey;
                            logger.finer( "Found externally converted chunk: database = " + newChunkKey.library.databaseNum + ", library = " + newChunkKey.library.libraryName + ", coverage = " + newChunkKey.coverage.coverageName );
                            DncTree newTree = createTree( newChunk );
                            trees.put( newChunkKey, newTree );
                        }

                        tree = trees.get( chunkKey );
                    }
                    if ( tree != null )
                    {
                        callback.accept( tree );
                        return;
                    }
                }
                finally
                {
                    unlockFile( mutexFile );
                }
            }



            // Load features, and convert to tree
            //

            QueryDatabase database = databases.get( databaseNum );
            Tree protoTree = database.createTree( library, coverage );

            int interiorNodeCount = protoTree.interiorNodesBuf.n( ) / intsPerQueryInteriorNode;
            int leafNodeCount = protoTree.leafNodesBuf.n( ) / intsPerQueryLeafNode;
            int pointItemCount = protoTree.pointsBuf.n( ) / intsPerQueryPointItem;
            int lineItemCount = protoTree.linesBuf.n( ) / intsPerQueryLineItem;
            int triangleItemCount = protoTree.trianglesBuf.n( ) / intsPerQueryTriangleItem;

            int treeWordCount = ( interiorNodeCount * intsPerQueryInteriorNode )
                              + ( leafNodeCount * intsPerQueryLeafNode )
                              + ( pointItemCount * intsPerQueryPointItem )
                              + ( lineItemCount * intsPerQueryLineItem )
                              + ( triangleItemCount * intsPerQueryTriangleItem );



            // Write to cache files
            //

            lockFile( mutexFile );
            try
            {

                // Is this chunk already in the cache?
                //

                DncTree tree;
                int newChunkFirst;
                int newChunkCount;
                synchronized ( trees )
                {
                    newChunkFirst = trees.size( );
                    newChunkCount = cursorBuf.get( 0 ) - newChunkFirst;
                    poslim( chunksBuf, newChunkFirst, newChunkCount, longsPerQueryChunk );
                    for ( QueryChunk newChunk : readQueryChunks( chunksBuf, libraries, coverages ) )
                    {
                        DncChunkKey newChunkKey = newChunk.chunkKey;
                        logger.finer( "Found externally converted chunk: database = " + newChunkKey.library.databaseNum + ", library = " + newChunkKey.library.libraryName + ", coverage = " + newChunkKey.coverage.coverageName );
                        DncTree newTree = createTree( newChunk );
                        trees.put( newChunkKey, newTree );
                    }

                    tree = trees.get( chunkKey );
                }
                if ( tree != null )
                {
                    callback.accept( tree );
                    return;
                }



                // Position output buffers
                //

                int chunkNext = newChunkFirst + newChunkCount;
                long treeWordNext = 0;

                if ( chunkNext > 0 )
                {
                    chunksBuf.position( ( chunkNext - 1 ) * longsPerQueryChunk );
                    chunksBuf.get( ); // lastChunkLibraryNum
                    chunksBuf.get( ); // lastChunkCoverageNum
                    long lastChunkWordFirst = chunksBuf.get( );
                    int lastChunkInteriorNodeCount = ( int ) chunksBuf.get( );
                    int lastChunkLeafNodeCount = ( int ) chunksBuf.get( );
                    int lastChunkPointItemCount = ( int ) chunksBuf.get( );
                    int lastChunkLineItemCount = ( int ) chunksBuf.get( );
                    int lastChunkTriangleItemCount = ( int ) chunksBuf.get( );

                    long lastChunkWordCount = ( lastChunkInteriorNodeCount * intsPerQueryInteriorNode )
                                            + ( lastChunkLeafNodeCount * intsPerQueryLeafNode )
                                            + ( lastChunkPointItemCount * intsPerQueryPointItem )
                                            + ( lastChunkLineItemCount * intsPerQueryLineItem )
                                            + ( lastChunkTriangleItemCount * intsPerQueryTriangleItem );

                    treeWordNext = lastChunkWordFirst + lastChunkWordCount;
                }

                chunksBuf.limit( chunksBuf.capacity( ) );
                chunksBuf.position( chunkNext * longsPerQueryChunk );


                long treeWordFirst = treeWordNext;


                // Memmap a section of the trees file
                //

                MappedByteBuffer treesMapped = treesChannel.map( READ_WRITE, treeWordFirst * SIZEOF_INT, treeWordCount * SIZEOF_INT );
                treesMapped.order( ByteOrder.nativeOrder( ) );
                IntBuffer treesBuf = treesMapped.asIntBuffer( );



                // Write tree data to buffer
                //

                protoTree.interiorNodesBuf.copyTo( treesBuf );
                protoTree.leafNodesBuf.copyTo( treesBuf );
                protoTree.pointsBuf.copyTo( treesBuf );
                protoTree.linesBuf.copyTo( treesBuf );
                protoTree.trianglesBuf.copyTo( treesBuf );



                // Write chunk data to buffer
                //

                int libraryNum = libraryNums.get( library );
                int coverageNum = coverageNums.get( coverage );

                chunksBuf.put( libraryNum );
                chunksBuf.put( coverageNum );
                chunksBuf.put( treeWordFirst );
                chunksBuf.put( interiorNodeCount );
                chunksBuf.put( leafNodeCount );
                chunksBuf.put( pointItemCount );
                chunksBuf.put( lineItemCount );
                chunksBuf.put( triangleItemCount );

                QueryChunk chunk = new QueryChunk( chunkKey, treeWordFirst, interiorNodeCount, leafNodeCount, pointItemCount, lineItemCount, triangleItemCount );
                tree = createTree( chunk );
                synchronized ( trees )
                {
                    logger.finer( "Finished converting chunk: database = " + library.databaseNum + ", library = " + library.libraryName + ", coverage = " + coverage.coverageName );
                    trees.put( chunkKey, tree );
                }



                // Run callback
                //

                callback.accept( tree );



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

                treesMapped.force( );
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



    public static String queryConfigString( QueryCacheConfig config ) throws IOException
    {
        StringBuilder configString = new StringBuilder( );

        configString.append( "formatVersion = " ).append( queryFormatVersion ).append( "\n" );
        configString.append( "\n" );

        configString.append( "flatDirs =" ).append( "\n" );
        for ( File flatDir : flatChildDirs( config.flatParentDir ) )
        {
            configString.append( "    " ).append( flatDir.getName( ) ).append( " : " ).append( readFlatChecksum( flatDir ) ).append( "\n" );
        }
        configString.append( "\n" );

        configString.append( "proj = " ).append( config.proj.configString( ) ).append( "\n" );

        return configString.toString( );
    }

    public static int writeQueryLibrariesFile( QueryCacheConfig config, File file, Int2ObjectMap<QueryDatabase> databases ) throws IOException
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
            for ( Int2ObjectMap.Entry<QueryDatabase> en : databases.int2ObjectEntrySet( ) )
            {
                int databaseNum = en.getIntKey( );
                QueryDatabase database = en.getValue( );
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

    public static int writeQueryCoveragesFile( QueryCacheConfig config, File file, Int2ObjectMap<QueryDatabase> databases ) throws IOException
    {
        PrintStream stream = null;
        try
        {
            stream = new PrintStream( file, config.charset.name( ) );

            Collection<String> coverageNames = new LinkedHashSet<>( );
            for ( Int2ObjectMap.Entry<QueryDatabase> en : databases.int2ObjectEntrySet( ) )
            {
                QueryDatabase database = en.getValue( );
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



    public static class Tree
    {
        public final IntsModifiable interiorNodesBuf;
        public final IntsModifiable leafNodesBuf;
        public final IntsModifiable pointsBuf;
        public final IntsModifiable linesBuf;
        public final IntsModifiable trianglesBuf;

        public Tree( )
        {
            this.interiorNodesBuf = new IntsArray( );
            this.leafNodesBuf = new IntsArray( );
            this.pointsBuf = new IntsArray( );
            this.linesBuf = new IntsArray( );
            this.trianglesBuf = new IntsArray( );
        }
    }



    public static class QueryDatabase
    {
        public final File flatDir;

        protected final ExecutorService exec;
        protected final DncProjection proj;

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

        public QueryDatabase( File flatDir, DncProjection proj, ExecutorService exec ) throws IOException
        {
            this.flatDir = flatDir;
            this.proj = proj;

            this.exec = exec;
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

        public Int2ObjectMap<DncFeature> loadFeatures( DncChunkKey chunkKey, IntCollection featureNums )
        {
            int flatLibraryNum = flatLibraryNums.get( chunkKey.library.libraryName );
            int flatCoverageNum = flatCoverageNums.get( chunkKey.coverage.coverageName );

            Int2ObjectMap<DncFeature> features = new Int2ObjectOpenHashMap<>( );

            IntBuffer featuresBufMaster = featuresBufMasters.get( new FlatChunkKey( flatLibraryNum, flatCoverageNum ) );
            if ( featuresBufMaster != null )
            {
                IntBuffer featuresBuf;
                synchronized ( bufMutex )
                {
                    featuresBuf = featuresBufMaster.duplicate( );
                }

                for ( IntIterator it = featureNums.iterator( ); it.hasNext( ); )
                {
                    int featureNum = it.nextInt( );

                    poslim( featuresBuf, featureNum, 1, intsPerFlatFeature );

                    int fcodeId = featuresBuf.get( );
                    int featureTypeId = featuresBuf.get( );
                    int attrFirst = featuresBuf.get( );
                    int attrCount = featuresBuf.get( );
                    int featureItemFirst = featuresBuf.get( );
                    int featureItemCount = featuresBuf.get( );

                    String fcode = fcodeNames.get( fcodeId );

                    Supplier<Map<String,Object>> attrsLoader = ( ) ->
                    {
                        LongBuffer attrsBuf;
                        ByteBuffer stringsBuf;
                        synchronized ( bufMutex )
                        {
                            attrsBuf = attrsBufMaster.duplicate( );
                            stringsBuf = stringsBufMaster.duplicate( );
                        }
                        return readFlatAttrs( attrsBuf, attrFirst, attrCount, attrNames, stringsBuf, charset );
                    };

                    DncFeature feature;
                    switch ( featureTypeId )
                    {
                        case FLAT_AREA_FEATURE:
                        {
                            feature = new DncAreaFeature( chunkKey, featureNum, fcode, attrsLoader, ( ) ->
                            {
                                IntBuffer ringsBuf;
                                DoubleBuffer verticesBuf;
                                synchronized ( bufMutex )
                                {
                                    ringsBuf = ringsBufMaster.duplicate( );
                                    verticesBuf = verticesBufMaster.duplicate( );
                                }
                                int ringFirst = featureItemFirst;
                                int ringCount = featureItemCount;
                                return readFlatAreaRings( ringsBuf, ringFirst, ringCount, verticesBuf );
                            } );
                        }
                        break;

                        case FLAT_LINE_FEATURE:
                        {
                            feature = new DncLineFeature( chunkKey, featureNum, fcode, attrsLoader, ( ) ->
                            {
                                DoubleBuffer verticesBuf;
                                synchronized ( bufMutex )
                                {
                                    verticesBuf = verticesBufMaster.duplicate( );
                                }
                                int vertexFirst = featureItemFirst;
                                int vertexCount = featureItemCount;
                                return readFlatLineVertices( verticesBuf, vertexFirst, vertexCount );
                            } );
                        }
                        break;

                        case FLAT_POINT_FEATURE:
                        {
                            feature = new DncPointFeature( chunkKey, featureNum, fcode, attrsLoader, ( ) ->
                            {
                                DoubleBuffer verticesBuf;
                                synchronized ( bufMutex )
                                {
                                    verticesBuf = verticesBufMaster.duplicate( );
                                }
                                int vertexIndex = featureItemFirst;
                                return readFlatPointVertex( verticesBuf, vertexIndex );
                            } );
                        }
                        break;

                        default: throw new RuntimeException( "Unrecognized feature-type ID: " + featureTypeId );
                    }
                    features.put( featureNum, feature );
                }
            }

            return features;
        }

        public Tree createTree( DncLibrary library, DncCoverage coverage )
        {
            int flatLibraryNum = flatLibraryNums.get( library.libraryName );
            int flatCoverageNum = flatCoverageNums.get( coverage.coverageName );

            TreeBuilder tree = new TreeBuilder( library.xMin, library.xMax, library.yMin, library.yMax, exec );

            IntBuffer featuresBufMaster = featuresBufMasters.get( new FlatChunkKey( flatLibraryNum, flatCoverageNum ) );
            if ( featuresBufMaster != null )
            {
                IntBuffer featuresBuf;
                IntBuffer ringsBuf;
                DoubleBuffer verticesBuf;
                synchronized ( bufMutex )
                {
                    featuresBuf = featuresBufMaster.duplicate( );
                    ringsBuf = ringsBufMaster.duplicate( );
                    verticesBuf = verticesBufMaster.duplicate( );
                }

                for ( int featureNum = 0; featuresBuf.hasRemaining( ); featureNum++ )
                {
                    featuresBuf.get( ); // fcodeId
                    int featureTypeId = featuresBuf.get( );
                    featuresBuf.get( ); // attrFirst
                    featuresBuf.get( ); // attrCount
                    int featureItemFirst = featuresBuf.get( );
                    int featureItemCount = featuresBuf.get( );

                    switch ( featureTypeId )
                    {
                        case FLAT_POINT_FEATURE:
                        {
                            float[] xy = projectPointFeatureVertex( verticesBuf, featureItemFirst, proj );
                            float x = xy[ 0 ];
                            float y = xy[ 1 ];
                            tree.addPoint( featureNum, x, y );
                        }
                        break;

                        case FLAT_LINE_FEATURE:
                        {
                            float[] xys = projectLineFeatureVertices( verticesBuf, featureItemFirst, featureItemCount, proj );
                            for ( int i = 0; i < ( xys.length - coordsPerXy ); i += coordsPerXy )
                            {
                                float xA = xys[ i + 0 ];
                                float yA = xys[ i + 1 ];
                                float xB = xys[ i + 2 ];
                                float yB = xys[ i + 3 ];
                                tree.addLine( featureNum, xA, yA, xB, yB );
                            }
                        }
                        break;

                        case FLAT_AREA_FEATURE:
                        {
                            float[][] xyRings = projectAreaFeatureRings( ringsBuf, featureItemFirst, featureItemCount, verticesBuf, proj );
                            FloatsArray triangleCoords = computeAreaFeatureTriangleCoords( featureNum, xyRings );
                            for ( int i = 0; i < triangleCoords.n( ); i += 3*coordsPerRenderTriangleVertex )
                            {
                                float xA = triangleCoords.v( i + 0 );
                                float yA = triangleCoords.v( i + 1 );
                                //float featureNumA = triangleCoords.v( i + 2 );

                                float xB = triangleCoords.v( i + 3 );
                                float yB = triangleCoords.v( i + 4 );
                                //float featureNumB = triangleCoords.v( i + 5 );

                                float xC = triangleCoords.v( i + 6 );
                                float yC = triangleCoords.v( i + 7 );
                                //float featureNumC = triangleCoords.v( i + 8 );

                                tree.addTriangle( featureNum, xA, yA, xB, yB, xC, yC );
                            }
                        }
                        break;

                        default:
                        {
                            throw new RuntimeException( "Unrecognized feature-type ID: " + featureTypeId );
                        }
                    }
                }
            }

            return tree.build( );
        }
    }



    public static class TreeBuilder
    {
        protected final float xRootMin;
        protected final float xRootMax;
        protected final float yRootMin;
        protected final float yRootMax;
        protected final Collection<PointItem> points;
        protected final Collection<LineItem> lines;
        protected final Collection<TriangleItem> triangles;
        protected final ExecutorService exec;

        public TreeBuilder( float xRootMin, float xRootMax, float yRootMin, float yRootMax, ExecutorService exec )
        {
            this.xRootMin = xRootMin;
            this.xRootMax = xRootMax;
            this.yRootMin = yRootMin;
            this.yRootMax = yRootMax;
            this.points = new ArrayList<>( );
            this.lines = new ArrayList<>( );
            this.triangles = new ArrayList<>( );
            this.exec = exec;
        }

        public void addPoint( int featureNum, float x, float y )
        {
            points.add( new PointItem( featureNum, x, y ) );
        }

        public void addLine( int featureNum, float xA, float yA, float xB, float yB )
        {
            lines.add( new LineItem( featureNum, xA, yA, xB, yB ) );
        }

        public void addTriangle( int featureNum, float xA, float yA, float xB, float yB, float xC, float yC )
        {
            triangles.add( new TriangleItem( featureNum, xA, yA, xB, yB, xC, yC ) );
        }

        public Tree build( )
        {
            Node root = createNode( xRootMin, xRootMax, yRootMin, yRootMax, points, lines, triangles, exec );

            Tree tree = new Tree( );
            appendQueryTree( root, tree.interiorNodesBuf, tree.leafNodesBuf, tree.pointsBuf, tree.linesBuf, tree.trianglesBuf );
            return tree;
        }
    }



    public static interface Node
    { }

    public static class InteriorNode implements Node
    {
        public final float xDivider;
        public final float yDivider;
        public final Node child0;
        public final Node child1;
        public final Node child2;
        public final Node child3;

        public InteriorNode( float xDivider, float yDivider, Node child0, Node child1, Node child2, Node child3 )
        {
            this.xDivider = xDivider;
            this.yDivider = yDivider;
            this.child0 = child0;
            this.child1 = child1;
            this.child2 = child2;
            this.child3 = child3;
        }
    }

    public static class LeafNode implements Node
    {
        public final float xMin;
        public final float xMax;
        public final float yMin;
        public final float yMax;
        public final Collection<PointItem> points;
        public final Collection<LineItem> lines;
        public final Collection<TriangleItem> triangles;

        public LeafNode( float xMin, float xMax, float yMin, float yMax, Collection<PointItem> points, Collection<LineItem> lines, Collection<TriangleItem> triangles )
        {
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
            this.points = points;
            this.lines = lines;
            this.triangles = triangles;
        }
    }

    public static interface Item
    {
        float xMin( );
        float xMax( );
        float yMin( );
        float yMax( );
    }

    public static class PointItem implements Item
    {
        public final int featureNum;
        public final float x;
        public final float y;

        public PointItem( int featureNum, float x, float y )
        {
            this.featureNum = featureNum;
            this.x = x;
            this.y = y;
        }

        public float xMin( ) { return x; }
        public float yMin( ) { return y; }
        public float xMax( ) { return x; }
        public float yMax( ) { return y; }
    }

    public static class LineItem implements Item
    {
        public final int featureNum;
        public final float xA;
        public final float yA;
        public final float xB;
        public final float yB;

        public LineItem( int featureNum, float xA, float yA, float xB, float yB )
        {
            this.featureNum = featureNum;
            this.xA = xA;
            this.yA = yA;
            this.xB = xB;
            this.yB = yB;
        }

        public float xMin( ) { return min( xA, xB ); }
        public float yMin( ) { return min( yA, yB ); }
        public float xMax( ) { return max( xA, xB ); }
        public float yMax( ) { return max( yA, yB ); }
    }

    public static class TriangleItem implements Item
    {
        public final int featureNum;
        public final float xA;
        public final float yA;
        public final float xB;
        public final float yB;
        public final float xC;
        public final float yC;

        public TriangleItem( int featureNum, float xA, float yA, float xB, float yB, float xC, float yC )
        {
            this.featureNum = featureNum;
            this.xA = xA;
            this.yA = yA;
            this.xB = xB;
            this.yB = yB;
            this.xC = xC;
            this.yC = yC;
        }

        public float xMin( ) { return min3f( xA, xB, xC ); }
        public float yMin( ) { return min3f( yA, yB, yC ); }
        public float xMax( ) { return max3f( xA, xB, xC ); }
        public float yMax( ) { return max3f( yA, yB, yC ); }
    }

    public static float chooseDivider( float nodeMin, float nodeMax, SortedFloats itemMins, SortedFloats itemMaxs )
    {
        int numItems = itemMins.n( );

        float bestDivider = nodeMin + 0.5f*( nodeMax - nodeMin );
        int bestWorstCost = Integer.MAX_VALUE;
        double bestAvgCost = Double.MAX_VALUE;

        for ( int i = 0; i < numItems; i++ )
        {
            float itemMin = itemMins.v( i );

            // Don't try the same divider position twice
            if ( i-1 >= 0 && itemMin == itemMins.v( i-1 ) )
            {
                continue;
            }

            // Since itemMin and itemMax are both inclusive, we don't want the
            // divider right at itemMin or itemMax -- it would mean keeping the
            // item both above and below
            float divider = itemMin - ulp( itemMin );

            double fracBelow = ( divider - nodeMin ) / ( nodeMax - nodeMin );
            double fracAbove = 1.0 - fracBelow;
            int numBelow = itemMins.indexAfter( divider );
            int numAbove = numItems - itemMaxs.indexAtOrAfter( divider );

            int worstCost = max( numBelow, numAbove );
            double avgCost = fracBelow*numBelow + fracAbove*numAbove;
            if ( worstCost < bestWorstCost || ( worstCost == bestWorstCost && avgCost < bestAvgCost ) )
            {
                bestDivider = divider;
                bestWorstCost = worstCost;
                bestAvgCost = avgCost;
            }
        }

        return bestDivider;
    }

    public static Node createNode( float xNodeMin, float xNodeMax, float yNodeMin, float yNodeMax, Collection<PointItem> points, Collection<LineItem> lines, Collection<TriangleItem> triangles, ExecutorService exec )
    {
        int numItems = points.size( ) + lines.size( ) + triangles.size( );
        if ( numItems < 100 )
        {
            return new LeafNode( xNodeMin, xNodeMax, yNodeMin, yNodeMax, points, lines, triangles );
        }


        // Choose dividers
        //

        Collection<Collection<? extends Item>> itemColls = asList( points, lines, triangles );

        Future<SortedFloats> xItemMinsFuture = exec.submit( ( ) ->
        {
            return createSortedFloatsArray( itemColls, Item::xMin );
        } );

        Future<SortedFloats> xItemMaxsFuture = exec.submit( ( ) ->
        {
            return createSortedFloatsArray( itemColls, Item::xMax );
        } );

        Future<SortedFloats> yItemMinsFuture = exec.submit( ( ) ->
        {
            return createSortedFloatsArray( itemColls, Item::yMin );
        } );

        Future<SortedFloats> yItemMaxsFuture = exec.submit( ( ) ->
        {
            return createSortedFloatsArray( itemColls, Item::yMax );
        } );

        SortedFloats xItemMins = requireResult( xItemMinsFuture );
        SortedFloats xItemMaxs = requireResult( xItemMaxsFuture );
        SortedFloats yItemMins = requireResult( yItemMinsFuture );
        SortedFloats yItemMaxs = requireResult( yItemMaxsFuture );

        float xDivider = chooseDivider( xNodeMin, xNodeMax, xItemMins, xItemMaxs );
        float yDivider = chooseDivider( yNodeMin, yNodeMax, yItemMins, yItemMaxs );


        // Divvy up items into quadrants
        //
        // An item may be included in more than one quadrant
        //
        // Treating both min and max as inclusive simplifies handling of degenerate items (e.g. points)
        //

        float xMin0 = xNodeMin;
        float xMax0 = xDivider;
        float yMin0 = yNodeMin;
        float yMax0 = yDivider;

        float xMin1 = xDivider;
        float xMax1 = xNodeMax;
        float yMin1 = yNodeMin;
        float yMax1 = yDivider;

        float xMin2 = xNodeMin;
        float xMax2 = xDivider;
        float yMin2 = yDivider;
        float yMax2 = yNodeMax;

        float xMin3 = xDivider;
        float xMax3 = xNodeMax;
        float yMin3 = yDivider;
        float yMax3 = yNodeMax;

        Collection<PointItem> points0 = new ArrayList<>( );
        Collection<PointItem> points1 = new ArrayList<>( );
        Collection<PointItem> points2 = new ArrayList<>( );
        Collection<PointItem> points3 = new ArrayList<>( );
        for ( PointItem point : points )
        {
            float x = point.x;
            float y = point.y;

            if ( boxContainsPoint( xMin0, yMin0, xMax0, yMax0, x, y ) ) points0.add( point );
            if ( boxContainsPoint( xMin1, yMin1, xMax1, yMax1, x, y ) ) points1.add( point );
            if ( boxContainsPoint( xMin2, yMin2, xMax2, yMax2, x, y ) ) points2.add( point );
            if ( boxContainsPoint( xMin3, yMin3, xMax3, yMax3, x, y ) ) points3.add( point );
        }

        Collection<LineItem> lines0 = new ArrayList<>( );
        Collection<LineItem> lines1 = new ArrayList<>( );
        Collection<LineItem> lines2 = new ArrayList<>( );
        Collection<LineItem> lines3 = new ArrayList<>( );
        for ( LineItem line : lines )
        {
            float xA = line.xA;
            float yA = line.yA;
            float xB = line.xB;
            float yB = line.yB;

            if ( boxIntersectsLine( xMin0, yMin0, xMax0, yMax0, xA, yA, xB, yB ) ) lines0.add( line );
            if ( boxIntersectsLine( xMin1, yMin1, xMax1, yMax1, xA, yA, xB, yB ) ) lines1.add( line );
            if ( boxIntersectsLine( xMin2, yMin2, xMax2, yMax2, xA, yA, xB, yB ) ) lines2.add( line );
            if ( boxIntersectsLine( xMin3, yMin3, xMax3, yMax3, xA, yA, xB, yB ) ) lines3.add( line );
        }

        Collection<TriangleItem> triangles0 = new ArrayList<>( );
        Collection<TriangleItem> triangles1 = new ArrayList<>( );
        Collection<TriangleItem> triangles2 = new ArrayList<>( );
        Collection<TriangleItem> triangles3 = new ArrayList<>( );
        for ( TriangleItem triangle : triangles )
        {
            float xA = triangle.xA;
            float yA = triangle.yA;
            float xB = triangle.xB;
            float yB = triangle.yB;
            float xC = triangle.xC;
            float yC = triangle.yC;

            if ( boxIntersectsTriangle( xMin0, yMin0, xMax0, yMax0, xA, yA, xB, yB, xC, yC ) ) triangles0.add( triangle );
            if ( boxIntersectsTriangle( xMin1, yMin1, xMax1, yMax1, xA, yA, xB, yB, xC, yC ) ) triangles1.add( triangle );
            if ( boxIntersectsTriangle( xMin2, yMin2, xMax2, yMax2, xA, yA, xB, yB, xC, yC ) ) triangles2.add( triangle );
            if ( boxIntersectsTriangle( xMin3, yMin3, xMax3, yMax3, xA, yA, xB, yB, xC, yC ) ) triangles3.add( triangle );
        }


        // Recurse if appropriate
        //

        int numItems0 = points0.size( ) + lines0.size( ) + triangles0.size( );
        int numItems1 = points1.size( ) + lines1.size( ) + triangles1.size( );
        int numItems2 = points2.size( ) + lines2.size( ) + triangles2.size( );
        int numItems3 = points3.size( ) + lines3.size( ) + triangles3.size( );
        int worstCost = max4i( numItems0, numItems1, numItems2, numItems3 );

        double xFracBelow = ( xDivider - xNodeMin ) / ( xNodeMax - xNodeMin );
        double xFracAbove = 1.0 - xFracBelow;
        double yFracBelow = ( yDivider - yNodeMin ) / ( yNodeMax - yNodeMin );
        double yFracAbove = 1.0 - yFracBelow;
        double avgCost = xFracBelow * yFracBelow * numItems0
                       + xFracAbove * yFracBelow * numItems1
                       + xFracBelow * yFracAbove * numItems2
                       + xFracAbove * yFracAbove * numItems3;


        int oldCost = numItems;
        if ( worstCost < 0.85*oldCost || ( worstCost <= oldCost && avgCost < 0.85*oldCost ) )
        {
            return new InteriorNode( xDivider,
                                     yDivider,
                                     createNode( xNodeMin, xDivider, yNodeMin, yDivider, points0, lines0, triangles0, exec ),
                                     createNode( xDivider, xNodeMax, yNodeMin, yDivider, points1, lines1, triangles1, exec ),
                                     createNode( xNodeMin, xDivider, yDivider, yNodeMax, points2, lines2, triangles2, exec ),
                                     createNode( xDivider, xNodeMax, yDivider, yNodeMax, points3, lines3, triangles3, exec ) );
        }
        else
        {
            return new LeafNode( xNodeMin, xNodeMax, yNodeMin, yNodeMax, points, lines, triangles );
        }
    }

    public static <T> SortedFloatsArray createSortedFloatsArray( Collection<? extends Collection<? extends T>> colls, ToFloatFunction<T> valueFn )
    {
        int n = 0;
        for ( Collection<? extends T> coll : colls )
        {
            n += coll.size( );
        }
        float[] values = new float[ n ];

        int i = 0;
        for ( Collection<? extends T> coll : colls )
        {
            for ( T obj : coll )
            {
                values[ i++ ] = valueFn.applyAsFloat( obj );
            }
        }

        sort( values );

        return new SortedFloatsArray( values );
    }

    public static boolean boxContainsPoint( float xMin, float yMin, float xMax, float yMax, float x, float y )
    {
        return ( xMin <= x && x <= xMax && yMin <= y && y <= yMax );
    }

    public static boolean boxIntersectsLine( float xMin, float yMin, float xMax, float yMax, float xA, float yA, float xB, float yB )
    {
        if ( boxContainsPoint( xMin, yMin, xMax, yMax, xA, yA ) ) return true;
        if ( boxContainsPoint( xMin, yMin, xMax, yMax, xB, yB ) ) return true;

        if ( xA < xMin && xB < xMin ) return false;
        if ( xA > xMax && xB > xMax ) return false;
        if ( yA < yMin && yB < yMin ) return false;
        if ( yA > yMax && yB > yMax ) return false;

        if ( lineIntersectsHorizontal( xA, yA, xB, yB, xMin, xMax, yMin ) ) return true;
        if ( lineIntersectsHorizontal( xA, yA, xB, yB, xMin, xMax, yMax ) ) return true;
        if ( lineIntersectsVertical( xA, yA, xB, yB, xMin, yMin, yMax ) ) return true;
        if ( lineIntersectsVertical( xA, yA, xB, yB, xMax, yMin, yMax ) ) return true;

        return false;
    }

    public static boolean boxIntersectsTriangle( float xMin, float yMin, float xMax, float yMax, float xA, float yA, float xB, float yB, float xC, float yC )
    {
        if ( boxContainsPoint( xMin, yMin, xMax, yMax, xA, yA ) ) return true;
        if ( boxContainsPoint( xMin, yMin, xMax, yMax, xB, yB ) ) return true;
        if ( boxContainsPoint( xMin, yMin, xMax, yMax, xC, yC ) ) return true;

        if ( xA < xMin && xB < xMin && xC < xMin ) return false;
        if ( xA > xMax && xB > xMax && xC > xMax ) return false;
        if ( yA < yMin && yB < yMin && yC < yMin ) return false;
        if ( yA > yMax && yB > yMax && yC > yMax ) return false;

        if ( triangleContainsPoint( xA, yA, xB, yB, xC, yC, xMin, yMin ) ) return true;
        if ( triangleContainsPoint( xA, yA, xB, yB, xC, yC, xMax, yMin ) ) return true;
        if ( triangleContainsPoint( xA, yA, xB, yB, xC, yC, xMin, yMax ) ) return true;
        if ( triangleContainsPoint( xA, yA, xB, yB, xC, yC, xMax, yMax ) ) return true;

        if ( lineIntersectsHorizontal( xA, yA, xB, yB, xMin, xMax, yMin ) ) return true;
        if ( lineIntersectsHorizontal( xA, yA, xB, yB, xMin, xMax, yMax ) ) return true;
        if ( lineIntersectsVertical( xA, yA, xB, yB, xMin, yMin, yMax ) ) return true;
        if ( lineIntersectsVertical( xA, yA, xB, yB, xMax, yMin, yMax ) ) return true;

        if ( lineIntersectsHorizontal( xB, yB, xC, yC, xMin, xMax, yMin ) ) return true;
        if ( lineIntersectsHorizontal( xB, yB, xC, yC, xMin, xMax, yMax ) ) return true;
        if ( lineIntersectsVertical( xB, yB, xC, yC, xMin, yMin, yMax ) ) return true;
        if ( lineIntersectsVertical( xB, yB, xC, yC, xMax, yMin, yMax ) ) return true;

        if ( lineIntersectsHorizontal( xC, yC, xA, yA, xMin, xMax, yMin ) ) return true;
        if ( lineIntersectsHorizontal( xC, yC, xA, yA, xMin, xMax, yMax ) ) return true;
        if ( lineIntersectsVertical( xC, yC, xA, yA, xMin, yMin, yMax ) ) return true;
        if ( lineIntersectsVertical( xC, yC, xA, yA, xMax, yMin, yMax ) ) return true;

        return false;
    }

    public static boolean triangleContainsPoint( float xA, float yA, float xB, float yB, float xC, float yC, float x, float y )
    {
        double crossAB = cross( x-xA, y-yA, xB-xA, yB-yA );
        double crossBC = cross( x-xB, y-yB, xC-xB, yC-yB );
        double crossCA = cross( x-xC, y-yC, xA-xC, yA-yC );

        return ( ( crossAB <= 0 && crossBC <= 0 && crossCA <= 0 )
              || ( crossAB >= 0 && crossBC >= 0 && crossCA >= 0 ) );
    }

    public static float cross( float xA, float yA, float xB, float yB )
    {
        return ( xA*yB - yA*xB );
    }

    public static boolean lineIntersectsHorizontal( float xA, float yA, float xB, float yB, float xMin, float xMax, float y )
    {
        float dy = yB - yA;
        if ( dy == 0f )
        {
            return ( yA == y && ( xA < xB ? ( xA <= xMax && xB >= xMin ) : ( xB <= xMax && xA >= xMin ) ) );
        }
        else
        {
            double alpha = ( y - yA ) / dy;
            if ( alpha < 0f || alpha > 1f )
            {
                return false;
            }
            else
            {
                double x = xA + alpha*( xB - xA );
                return ( xMin <= x && x <= xMax );
            }
        }
    }

    public static boolean lineIntersectsVertical( float xA, float yA, float xB, float yB, float x, float yMin, float yMax )
    {
        float dx = xB - xA;
        if ( dx == 0f )
        {
            return ( xA == x && ( yA < yB ? ( yA <= yMax && yB >= yMin ) : ( yB <= yMax && yA >= yMin ) ) );
        }
        else
        {
            double alpha = ( x - xA ) / dx;
            if ( alpha < 0f || alpha > 1f )
            {
                return false;
            }
            else
            {
                double y = yA + alpha*( yB - yA );
                return ( yMin <= y && y <= yMax );
            }
        }
    }

    public static int max4i( int a, int b, int c, int d )
    {
        return max( max( a, b ), max( c, d ) );
    }

    public static float min3f( float a, float b, float c )
    {
        return min( min( a, b ), c );
    }

    public static float max3f( float a, float b, float c )
    {
        return max( max( a, b ), c );
    }

    public static int appendQueryTree( Node node, IntsModifiable interiorNodesBuf, IntsModifiable leafNodesBuf, IntsModifiable pointsBuf, IntsModifiable linesBuf, IntsModifiable trianglesBuf )
    {
        if ( node instanceof LeafNode )
        {
            LeafNode leaf = ( LeafNode ) node;

            int leafNodeNum = leafNodesBuf.n( ) / intsPerQueryLeafNode;

            int pointFirst = pointsBuf.n( ) / intsPerQueryPointItem;
            int pointCount = leaf.points.size( );

            int lineFirst = linesBuf.n( ) / intsPerQueryLineItem;
            int lineCount = leaf.lines.size( );

            int triangleFirst = trianglesBuf.n( ) / intsPerQueryTriangleItem;
            int triangleCount = leaf.triangles.size( );

            leafNodesBuf.append( floatToIntBits( leaf.xMin ) );
            leafNodesBuf.append( floatToIntBits( leaf.xMax ) );
            leafNodesBuf.append( floatToIntBits( leaf.yMin ) );
            leafNodesBuf.append( floatToIntBits( leaf.yMax ) );
            leafNodesBuf.append( pointFirst );
            leafNodesBuf.append( pointCount );
            leafNodesBuf.append( lineFirst );
            leafNodesBuf.append( lineCount );
            leafNodesBuf.append( triangleFirst );
            leafNodesBuf.append( triangleCount );

            for ( PointItem point : leaf.points )
            {
                pointsBuf.append( point.featureNum );
                pointsBuf.append( floatToIntBits( point.x ) );
                pointsBuf.append( floatToIntBits( point.y ) );
            }

            for ( LineItem line : leaf.lines )
            {
                linesBuf.append( line.featureNum );
                linesBuf.append( floatToIntBits( line.xA ) );
                linesBuf.append( floatToIntBits( line.yA ) );
                linesBuf.append( floatToIntBits( line.xB ) );
                linesBuf.append( floatToIntBits( line.yB ) );
            }

            for ( TriangleItem triangle : leaf.triangles )
            {
                trianglesBuf.append( triangle.featureNum );
                trianglesBuf.append( floatToIntBits( triangle.xA ) );
                trianglesBuf.append( floatToIntBits( triangle.yA ) );
                trianglesBuf.append( floatToIntBits( triangle.xB ) );
                trianglesBuf.append( floatToIntBits( triangle.yB ) );
                trianglesBuf.append( floatToIntBits( triangle.xC ) );
                trianglesBuf.append( floatToIntBits( triangle.yC ) );
            }

            // A negative node number gets bitwise NOT-ed and used as an index into the leaves list
            return ( ~leafNodeNum );
        }
        else if ( node instanceof InteriorNode )
        {
            InteriorNode interior = ( InteriorNode ) node;

            // Interior nodes are written post-order depth-first, so children are already in the list
            int childNum0 = appendQueryTree( interior.child0, interiorNodesBuf, leafNodesBuf, pointsBuf, linesBuf, trianglesBuf );
            int childNum1 = appendQueryTree( interior.child1, interiorNodesBuf, leafNodesBuf, pointsBuf, linesBuf, trianglesBuf );
            int childNum2 = appendQueryTree( interior.child2, interiorNodesBuf, leafNodesBuf, pointsBuf, linesBuf, trianglesBuf );
            int childNum3 = appendQueryTree( interior.child3, interiorNodesBuf, leafNodesBuf, pointsBuf, linesBuf, trianglesBuf );

            int interiorNodeNum = interiorNodesBuf.n( ) / intsPerQueryInteriorNode;

            interiorNodesBuf.append( floatToIntBits( interior.xDivider ) );
            interiorNodesBuf.append( floatToIntBits( interior.yDivider ) );
            interiorNodesBuf.append( childNum0 );
            interiorNodesBuf.append( childNum1 );
            interiorNodesBuf.append( childNum2 );
            interiorNodesBuf.append( childNum3 );

            // A non-negative node number is interpreted as an index into the interiorNodes list
            return interiorNodeNum;
        }
        else
        {
            throw new RuntimeException( "Unexpected node type: " + node.getClass( ).getName( ) );
        }
    }



    public static void printTree( PrintStream out, Node root )
    {
        printTree0( out, root, 0 );
    }

    protected static void printTree0( PrintStream out, Node node, int depth )
    {
        String indent = repchar( ' ', 4*depth );
        if ( node instanceof LeafNode )
        {
            LeafNode leaf = ( LeafNode ) node;
            int numItems = leaf.points.size( ) + leaf.lines.size( ) + leaf.triangles.size( );
            out.println( indent + "Leaf { " + numItems + " }" );
        }
        else if ( node instanceof InteriorNode )
        {
            InteriorNode interior = ( InteriorNode ) node;
            out.println( indent + "Interior" );
            out.println( indent + "{" );
            printTree0( out, interior.child0, depth + 1 );
            printTree0( out, interior.child1, depth + 1 );
            printTree0( out, interior.child2, depth + 1 );
            printTree0( out, interior.child3, depth + 1 );
            out.println( indent + "}" );
        }
        else
        {
            out.println( indent + node.getClass( ).getName( ) );
        }
    }

}

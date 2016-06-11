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
import static com.metsci.glimpse.dnc.DncChunks.createHostChunk;
import static com.metsci.glimpse.dnc.DncChunks.xferChunkToDevice;
import static com.metsci.glimpse.dnc.DncIconAtlases.createHostIconAtlas;
import static com.metsci.glimpse.dnc.DncIconAtlases.xferIconAtlasToDevice;
import static com.metsci.glimpse.dnc.DncLabelAtlases.coordsPerLabelAtlasAlign;
import static com.metsci.glimpse.dnc.DncLabelAtlases.coordsPerLabelAtlasBounds;
import static com.metsci.glimpse.dnc.DncLabelAtlases.createHostLabelAtlas;
import static com.metsci.glimpse.dnc.DncLabelAtlases.xferLabelAtlasToDevice;
import static com.metsci.glimpse.dnc.DncPainterUtils.coverageSignificanceComparator;
import static com.metsci.glimpse.dnc.DncPainterUtils.groupRenderingOrder;
import static com.metsci.glimpse.dnc.convert.Render.coordsPerRenderIconVertex;
import static com.metsci.glimpse.dnc.convert.Render.coordsPerRenderLabelVertex;
import static com.metsci.glimpse.dnc.convert.Render.coordsPerRenderLineVertex;
import static com.metsci.glimpse.dnc.convert.Render.coordsPerRenderTriangleVertex;
import static com.metsci.glimpse.dnc.geosym.DncGeosymIo.readGeosymColors;
import static com.metsci.glimpse.dnc.geosym.DncGeosymIo.readGeosymLineAreaStyles;
import static com.metsci.glimpse.dnc.geosym.DncGeosymThemes.DNC_THEME_STANDARD;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.newWorkerDaemon;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.sorted;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.timeSince_MILLIS;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.sort;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_LINE_STRIP;
import static javax.media.opengl.GL.GL_MAX_TEXTURE_SIZE;
import static javax.media.opengl.GL.GL_ONE;
import static javax.media.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static javax.media.opengl.GL.GL_POINTS;
import static javax.media.opengl.GL.GL_TEXTURE0;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TRIANGLES;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.awt.Color;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.dnc.DncAreaShaders.DncAreaProgram;
import com.metsci.glimpse.dnc.DncAtlases.DncAtlasEntry;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.dnc.DncChunks.DncDeviceChunk;
import com.metsci.glimpse.dnc.DncChunks.DncGroup;
import com.metsci.glimpse.dnc.DncChunks.DncHostChunk;
import com.metsci.glimpse.dnc.DncIconAtlases.DncDeviceIconAtlas;
import com.metsci.glimpse.dnc.DncIconAtlases.DncHostIconAtlas;
import com.metsci.glimpse.dnc.DncIconShaders.DncIconProgram;
import com.metsci.glimpse.dnc.DncLabelAtlases.DncDeviceLabelAtlas;
import com.metsci.glimpse.dnc.DncLabelAtlases.DncHostLabelAtlas;
import com.metsci.glimpse.dnc.DncLabelShaders.DncLabelProgram;
import com.metsci.glimpse.dnc.DncLineShaders.DncLineProgram;
import com.metsci.glimpse.dnc.convert.Flat2Render.DncChunkPriority;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCache;
import com.metsci.glimpse.dnc.convert.Render.RenderChunk;
import com.metsci.glimpse.dnc.geosym.DncGeosymAssignment;
import com.metsci.glimpse.dnc.geosym.DncGeosymLineAreaStyle;
import com.metsci.glimpse.dnc.geosym.DncGeosymTheme;
import com.metsci.glimpse.dnc.util.DncMiscUtils.ThrowingRunnable;
import com.metsci.glimpse.dnc.util.RateLimitedAxisLimitsListener1D;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;

public class DncPainter extends GlimpsePainter2D
{

    protected static final Logger logger = getLogger( DncPainter.class );


    // XXX: Maybe move these to settings

    protected static final long chunkDisposeTimeLimit_MILLIS = 3;
    protected static final int guaranteedChunkDisposalsPerFrame = 1;

    protected static final long highlightSetDisposeTimeLimit_MILLIS = 3;
    protected static final int guaranteedHighlightSetDisposalsPerFrame = 1;

    protected static final long iconAtlasDisposeTimeLimit_MILLIS = 3;
    protected static final int guaranteedIconAtlasDisposalsPerFrame = 1;

    protected static final long labelAtlasDisposeTimeLimit_MILLIS = 3;
    protected static final int guaranteedLabelAtlasDisposalsPerFrame = 1;

    protected static final long chunkXferTimeLimit_MILLIS = 5;
    protected static final int guaranteedChunkXfersPerFrame = 1;

    protected static final long iconAtlasXferTimeLimit_MILLIS = 3;
    protected static final int guaranteedIconAtlasXfersPerFrame = 1;

    protected static final long labelAtlasXferTimeLimit_MILLIS = 1;
    protected static final int guaranteedLabelAtlasXfersPerFrame = 1;


    protected static class RasterizeArgs
    {
        public final int maxTextureDim;
        public final double screenDpi;

        public RasterizeArgs( int maxTextureDim, double screenDpi )
        {
            this.maxTextureDim = maxTextureDim;
            this.screenDpi = screenDpi;
        }
    }


    protected final Object mutex;
    protected final ExecutorService asyncExec;
    protected final ExecutorService iconsExec;
    protected final ExecutorService labelsExec;

    protected final RenderCache cache;
    protected final DncPainterSettings settings;
    protected final Collection<DncLibrary> allLibraries;

    protected final Map<DncChunkKey,DncHostChunk> hChunks;
    protected final Map<DncChunkKey,DncDeviceChunk> dChunks;
    protected final List<DncDeviceChunk> dChunksToDispose;

    protected final Map<DncChunkKey,DncHostIconAtlas> hIconAtlases;
    protected final Map<DncChunkKey,DncDeviceIconAtlas> dIconAtlases;
    protected final List<DncDeviceIconAtlas> dIconAtlasesToDispose;

    protected final Map<DncChunkKey,DncHostLabelAtlas> hLabelAtlases;
    protected final Map<DncChunkKey,DncDeviceLabelAtlas> dLabelAtlases;
    protected final List<DncDeviceLabelAtlas> dLabelAtlasesToDispose;

    protected final Map<DncChunkKey,IndexSetTexture> highlightSets;
    protected final List<IndexSetTexture> highlightSetsToDispose;

    protected DncAreaProgram areaProgram;
    protected DncLineProgram lineProgram;
    protected DncIconProgram iconProgram;
    protected DncLabelProgram labelProgram;

    protected final Set<Axis2D> axes;
    protected final AxisListener1D axisListener;
    protected final Set<DncLibrary> activeLibraries;
    protected final Set<DncCoverage> activeCoverages;
    public final Function<DncChunkKey,DncChunkPriority> chunkPriorityFunc;

    // Accessed while holding mutex
    protected DncGeosymTheme theme;
    protected Map<String,DncGeosymLineAreaStyle> lineAreaStyles;
    protected RasterizeArgs rasterizeArgs;

    // Accessed only on the labels thread
    protected Int2ObjectMap<Color> labelColors;
    protected String labelColorsFile;



    public DncPainter( RenderCache cache, DncPainterSettings settings )
    {
        this( cache, settings, DNC_THEME_STANDARD );
    }

    public DncPainter( RenderCache cache, DncPainterSettings settings, DncGeosymTheme theme )
    {
        this.mutex = new Object( );
        this.asyncExec = newWorkerDaemon( "DncPainter.Async." );
        this.iconsExec = newWorkerDaemon( "DncPainter.Icons." );
        this.labelsExec = newWorkerDaemon( "DncPainter.Labels." );

        this.cache = cache;
        this.settings = settings;
        this.allLibraries = cache.libraries;

        this.hChunks = new HashMap<>( );
        this.dChunks = new HashMap<>( );
        this.dChunksToDispose = new ArrayList<>( );

        this.hIconAtlases = new HashMap<>( );
        this.dIconAtlases = new HashMap<>( );
        this.dIconAtlasesToDispose = new ArrayList<>( );

        this.hLabelAtlases = new HashMap<>( );
        this.dLabelAtlases = new HashMap<>( );
        this.dLabelAtlasesToDispose = new ArrayList<>( );

        this.highlightSets = new HashMap<>( );
        this.highlightSetsToDispose = new ArrayList<>( );

        this.areaProgram = null;
        this.lineProgram = null;
        this.iconProgram = null;
        this.labelProgram = null;

        this.axes = new HashSet<>( );
        this.axisListener = new RateLimitedAxisLimitsListener1D( )
        {
            public void axisLimitsUpdatedRateLimited( Axis1D axis )
            {
                updateActiveLibraries( allLibraries );
            }
        };
        this.activeLibraries = new HashSet<>( );
        this.activeCoverages = new HashSet<>( );
        this.chunkPriorityFunc = new Function<DncChunkKey,DncChunkPriority>( )
        {
            public DncChunkPriority apply( DncChunkKey chunkKey )
            {
                synchronized ( mutex )
                {
                    boolean isActive = ( activeLibraries.contains( chunkKey.library ) && activeCoverages.contains( chunkKey.coverage ) );
                    return ( isActive ? DncChunkPriority.IMMEDIATE : DncChunkPriority.SOON );
                }
            }
        };

        this.theme = null;
        this.lineAreaStyles = emptyMap( );
        this.rasterizeArgs = null;

        this.labelColors = new Int2ObjectOpenHashMap<>( );
        this.labelColorsFile = null;

        setTheme( theme );
    }

    public void addAxis( Axis2D axis )
    {
        synchronized ( mutex )
        {
            // Don't allow axes to be re-enabled after disposal
            if ( asyncExec.isShutdown( ) ) return;

            if ( axes.add( axis ) )
            {
                axis.getAxisX( ).addAxisListener( axisListener );
                axis.getAxisY( ).addAxisListener( axisListener );
                updateActiveLibraries( allLibraries );
            }
        }
    }

    public void removeAxis( Axis2D axis )
    {
        synchronized ( mutex )
        {
            // Not necessary, since axes would already be empty
            //if ( asyncExec.isShutdown( ) ) return;

            if ( axes.remove( axis ) )
            {
                axis.getAxisX( ).removeAxisListener( axisListener );
                axis.getAxisY( ).removeAxisListener( axisListener );
                updateActiveLibraries( allLibraries );
            }
        }
    }

    public boolean isChunkActive( DncChunkKey chunkKey )
    {
        synchronized ( mutex )
        {
            return ( activeLibraries.contains( chunkKey.library ) && activeCoverages.contains( chunkKey.coverage ) );
        }
    }

    public Collection<DncChunkKey> activeChunkKeys( )
    {
        synchronized ( mutex )
        {
            Collection<DncChunkKey> chunkKeys = new ArrayList<>( );
            for ( DncLibrary library : activeLibraries )
            {
                for ( DncCoverage coverage : activeCoverages )
                {
                    DncChunkKey chunkKey = new DncChunkKey( library, coverage );
                    chunkKeys.add( chunkKey );
                }
            }
            return chunkKeys;
        }
    }

    public void setTheme( DncGeosymTheme newTheme )
    {
        // If asyncExec is currently in a call to activateCoverages, it's possible for
        // this block to run AFTER asyncExec reads this.theme, but BEFORE it populates
        // maps with newly loaded chunk data.
        //
        // That feels wrong, and makes the whole thing tricky to reason about. However,
        // it turns out okay, because after writing this.theme we re-activate everything,
        // which gives eventual consistency.
        //
        synchronized ( mutex )
        {
            if ( !equal( newTheme, theme ) )
            {
                // Drop everything that was created using the old theme
                deactivateChunks( activeLibraries, activeCoverages );
                this.lineAreaStyles = emptyMap( );

                // Store the theme
                this.theme = newTheme;

                // Reload everything using the new theme
                activateChunks( activeLibraries, activeCoverages );
                final String newLineAreaStylesFile = newTheme.lineAreaStylesFile;
                asyncExec.execute( new ThrowingRunnable( )
                {
                    public void runThrows( ) throws Exception
                    {
                        Map<String,DncGeosymLineAreaStyle> newLineAreaStyles = readGeosymLineAreaStyles( newLineAreaStylesFile );
                        synchronized ( mutex )
                        {
                            lineAreaStyles = newLineAreaStyles;
                        }
                    }
                } );
            }
        }
    }

    public void highlightFeatures( DncChunkKey chunkKey, IntCollection featureNums )
    {
        synchronized ( mutex )
        {
            if ( !highlightSets.containsKey( chunkKey ) )
            {
                highlightSets.put( chunkKey, new IndexSetTexture( ) );
            }
            highlightSets.get( chunkKey ).set( featureNums );
        }
    }

    public void setCoverageActive( DncCoverage coverage, boolean active )
    {
        setCoveragesActive( singleton( coverage ), active );
    }

    public void setCoveragesActive( Collection<DncCoverage> coverages, boolean active )
    {
        if ( active )
        {
            activateCoverages( coverages );
        }
        else
        {
            deactivateCoverages( coverages );
        }
    }

    public void activateCoverages( String... coverageNames )
    {
        Collection<DncCoverage> coverages = new ArrayList<>( );
        for ( String coverageName : coverageNames )
        {
            coverages.add( new DncCoverage( coverageName ) );
        }
        activateCoverages( coverages );
    }

    public void activateCoverage( DncCoverage coverage )
    {
        activateCoverages( singleton( coverage ) );
    }

    public void activateCoverages( Collection<DncCoverage> coverages )
    {
        synchronized ( mutex )
        {
            // Don't allow coverages to be re-activated after disposal
            if ( asyncExec.isShutdown( ) ) return;

            if ( activeCoverages.addAll( coverages ) )
            {
                activateChunks( activeLibraries, coverages );
            }
        }
    }

    public void deactivateCoverage( DncCoverage coverage )
    {
        deactivateCoverages( singleton( coverage ) );
    }

    public void deactivateCoverages( Collection<DncCoverage> coverages )
    {
        synchronized ( mutex )
        {
            // Not necessary, since activeCoverages would already be empty
            //if ( asyncExec.isShutdown( ) ) return;

            if ( activeCoverages.removeAll( coverages ) )
            {
                deactivateChunks( activeLibraries, coverages );
            }
        }
    }

    protected void updateActiveLibraries( Collection<DncLibrary> librariesToUpdate )
    {
        synchronized ( mutex )
        {
            // Not usually necessary, since axes would be empty, but guards against strange impls of isLibraryActive
            if ( asyncExec.isShutdown( ) ) return;

            Set<DncLibrary> librariesToActivate = new HashSet<>( );
            Set<DncLibrary> librariesToDeactivate = new HashSet<>( );

            for ( DncLibrary library : librariesToUpdate )
            {
                if ( settings.isLibraryActive( library, axes ) )
                {
                    if ( activeLibraries.add( library ) )
                    {
                        librariesToActivate.add( library );
                    }
                }
                else
                {
                    if ( activeLibraries.remove( library ) )
                    {
                        librariesToDeactivate.add( library );
                    }
                }
            }

            deactivateChunks( librariesToDeactivate, activeCoverages );
            activateChunks( librariesToActivate, activeCoverages );
        }
    }

    protected void activateChunks( Collection<DncLibrary> libraries, Collection<DncCoverage> coverages )
    {
        coverages = sorted( coverages, coverageSignificanceComparator );
        synchronized ( mutex )
        {
            // Not strictly necessary, but avoids submitting useless requests to the cache
            if ( asyncExec.isShutdown( ) ) return;

            for ( DncLibrary library : libraries )
            {
                for ( DncCoverage coverage : coverages )
                {
                    final DncChunkKey chunkKey = new DncChunkKey( library, coverage );
                    if ( !dChunks.containsKey( chunkKey ) && !hChunks.containsKey( chunkKey ) )
                    {
                        cache.getChunk( chunkKey, chunkPriorityFunc, new Consumer<RenderChunk>( )
                        {
                            public void accept( final RenderChunk renderChunk )
                            {
                                // On the async thread ...
                                asyncExec.execute( new ThrowingRunnable( )
                                {
                                    public void runThrows( ) throws Exception
                                    {
                                        // Wait until we have certain display info from our first paint
                                        final RasterizeArgs rasterizeArgs = waitForRasterizeArgs( );
                                        if ( rasterizeArgs == null )
                                        {
                                            return;
                                        }

                                        // Bail out if chunk is no longer active
                                        synchronized ( mutex )
                                        {
                                            if ( !( activeLibraries.contains( chunkKey.library ) && activeCoverages.contains( chunkKey.coverage ) ) )
                                            {
                                                return;
                                            }
                                        }

                                        // Load and put chunk vertices
                                        int featureCount = renderChunk.featureCount;
                                        IntBuffer groupsBuf = cache.sliceChunkGroups( renderChunk );
                                        FloatBuffer verticesBuf = cache.memmapChunkVertices( renderChunk );
                                        final DncHostChunk hChunk = createHostChunk( chunkKey, featureCount, groupsBuf, verticesBuf, cache.geosymAssignments );
                                        synchronized ( mutex )
                                        {
                                            if ( activeLibraries.contains( chunkKey.library ) && activeCoverages.contains( chunkKey.coverage ) )
                                            {
                                                hChunks.put( chunkKey, hChunk );

                                                if ( !highlightSets.containsKey( chunkKey ) )
                                                {
                                                    highlightSets.put( chunkKey, new IndexSetTexture( ) );
                                                }
                                            }
                                        }

                                        // On the icons thread ...
                                        iconsExec.execute( new ThrowingRunnable( )
                                        {
                                            public void runThrows( ) throws Exception
                                            {
                                                // Bail out if chunk is no longer active
                                                synchronized ( mutex )
                                                {
                                                    if ( !( activeLibraries.contains( chunkKey.library ) && activeCoverages.contains( chunkKey.coverage ) ) )
                                                    {
                                                        return;
                                                    }
                                                }

                                                // Get up-to-date cgmDir and svgDir
                                                String cgmDir;
                                                String svgDir;
                                                synchronized ( mutex )
                                                {
                                                    if ( theme == null )
                                                    {
                                                        return;
                                                    }
                                                    cgmDir = theme.cgmDir;
                                                    svgDir = theme.svgDir;
                                                }

                                                // Load, rasterize, and put chunk icons
                                                DncHostIconAtlas hIconAtlas = createHostIconAtlas( hChunk, cgmDir, svgDir, rasterizeArgs.maxTextureDim, rasterizeArgs.screenDpi );
                                                if ( hIconAtlas != null )
                                                {
                                                    synchronized ( mutex )
                                                    {
                                                        if ( equal( cgmDir, theme.cgmDir ) && equal( svgDir, theme.svgDir ) && activeLibraries.contains( chunkKey.library ) && activeCoverages.contains( chunkKey.coverage ) )
                                                        {
                                                            hIconAtlases.put( chunkKey, hIconAtlas );
                                                        }
                                                    }
                                                }
                                            }
                                        } );

                                        // On the labels thread ...
                                        labelsExec.execute( new ThrowingRunnable( )
                                        {
                                            public void runThrows( ) throws Exception
                                            {
                                                // Bail out if chunk is no longer active
                                                synchronized ( mutex )
                                                {
                                                    if ( !( activeLibraries.contains( chunkKey.library ) && activeCoverages.contains( chunkKey.coverage ) ) )
                                                    {
                                                        return;
                                                    }
                                                }

                                                // Get up-to-date colors map
                                                String colorsFile;
                                                synchronized ( mutex )
                                                {
                                                    if ( theme == null )
                                                    {
                                                        return;
                                                    }
                                                    colorsFile = theme.colorsFile;
                                                }
                                                if ( !equal( colorsFile, labelColorsFile ) )
                                                {
                                                    labelColors = readGeosymColors( colorsFile );
                                                    labelColorsFile = colorsFile;
                                                }

                                                // Load, rasterize, and put chunk labels
                                                CharBuffer labelCharsBuf = cache.sliceChunkLabelChars( renderChunk );
                                                IntBuffer labelLengthsBuf = cache.sliceChunkLabelLengths( renderChunk );
                                                DncHostLabelAtlas hLabelAtlas = createHostLabelAtlas( hChunk, labelCharsBuf, labelLengthsBuf, labelColors, rasterizeArgs.maxTextureDim, rasterizeArgs.screenDpi );
                                                if ( hLabelAtlas != null )
                                                {
                                                    synchronized ( mutex )
                                                    {
                                                        if ( equal( colorsFile, theme.colorsFile ) && activeLibraries.contains( chunkKey.library ) && activeCoverages.contains( chunkKey.coverage ) )
                                                        {
                                                            hLabelAtlases.put( chunkKey, hLabelAtlas );
                                                        }
                                                    }
                                                }
                                            }
                                        } );
                                    }
                                } );
                            }
                        } );
                    }
                }
            }
        }
    }

    /**
     * Wait until either the painter is disposed (in which case return null)
     * or this.rasterizeArgs is set (in which case return this.rasterizeArgs).
     */
    protected RasterizeArgs waitForRasterizeArgs( )
    {
        synchronized ( mutex )
        {
            while ( true )
            {
                // Stop waiting if painter has been disposed
                if ( asyncExec.isShutdown( ) )
                {
                    return null;
                }

                if ( rasterizeArgs != null )
                {
                    return rasterizeArgs;
                }

                try
                {
                    mutex.wait( );
                }
                catch ( InterruptedException e )
                { }
            }
        }
    }

    protected void deactivateChunks( Collection<DncLibrary> libraries, Collection<DncCoverage> coverages )
    {
        synchronized ( mutex )
        {
            // Not necessary, since everything would already be empty
            //if ( asyncExec.isShutdown( ) ) return;

            for ( DncLibrary library : libraries )
            {
                for ( DncCoverage coverage : coverages )
                {
                    DncChunkKey chunkKey = new DncChunkKey( library, coverage );

                    hChunks.remove( chunkKey );
                    hIconAtlases.remove( chunkKey );
                    hLabelAtlases.remove( chunkKey );

                    DncDeviceChunk dChunk = dChunks.remove( chunkKey );
                    if ( dChunk != null ) dChunksToDispose.add( dChunk );

                    DncDeviceIconAtlas dIconAtlas = dIconAtlases.remove( chunkKey );
                    if ( dIconAtlas != null ) dIconAtlasesToDispose.add( dIconAtlas );

                    DncDeviceLabelAtlas dLabelAtlas = dLabelAtlases.remove( chunkKey );
                    if ( dLabelAtlas != null ) dLabelAtlasesToDispose.add( dLabelAtlas );

                    // Keep higlight-set objects in the map, but dispose their device resources
                    IndexSetTexture highlightSet = highlightSets.get( chunkKey );
                    if ( highlightSet != null ) highlightSetsToDispose.add( highlightSet );
                }
            }
        }
    }

    @Override
    protected void dispose( GLContext context )
    {
        GL2 gl = context.getGL( ).getGL2( );
        synchronized ( mutex )
        {
            // Chunks
            for ( DncDeviceChunk dChunk : dChunksToDispose ) dChunk.dispose( gl );
            for ( DncDeviceChunk dChunk : dChunks.values( ) ) dChunk.dispose( gl );
            dChunksToDispose.clear( );
            dChunks.clear( );
            hChunks.clear( );

            // Icon-atlases
            for ( DncDeviceIconAtlas dIconAtlas : dIconAtlasesToDispose ) dIconAtlas.dispose( gl );
            for ( DncDeviceIconAtlas dIconAtlas : dIconAtlases.values( ) ) dIconAtlas.dispose( gl );
            dIconAtlasesToDispose.clear( );
            dIconAtlases.clear( );
            hIconAtlases.clear( );

            // Label-atlases
            for ( DncDeviceLabelAtlas dLabelAtlas : dLabelAtlasesToDispose ) dLabelAtlas.dispose( gl );
            for ( DncDeviceLabelAtlas dLabelAtlas : dLabelAtlases.values( ) ) dLabelAtlas.dispose( gl );
            dLabelAtlasesToDispose.clear( );
            dLabelAtlases.clear( );
            hLabelAtlases.clear( );

            // Highlight-sets
            for ( IndexSetTexture highlightSet : highlightSets.values( ) ) highlightSet.freeDeviceResources( gl );
            highlightSetsToDispose.clear( );
            highlightSets.clear( );

            // Shader programs
            if ( areaProgram != null ) gl.glDeleteProgram( areaProgram.programHandle );
            if ( lineProgram != null ) gl.glDeleteProgram( lineProgram.programHandle );
            if ( iconProgram != null ) gl.glDeleteProgram( iconProgram.programHandle );
            if ( labelProgram != null ) gl.glDeleteProgram( labelProgram.programHandle );

            // Axis listeners
            for ( Axis2D axis : axes )
            {
                axis.getAxisX( ).removeAxisListener( axisListener );
                axis.getAxisY( ).removeAxisListener( axisListener );
            }
            axes.clear( );

            // Mark all chunks as deactivated
            activeLibraries.clear( );
            activeCoverages.clear( );

            // Executors
            //
            // Jobs already submitted will run, but will find that no chunks are active, and so
            // will simply discard whatever they have generated.
            //
            // Jobs submitted later will be silently discarded.
            //
            asyncExec.shutdown( );
            iconsExec.shutdown( );
            labelsExec.shutdown( );

            // Wake up any threads sleeping in waitForRasterizeArgs()
            mutex.notifyAll( );
        }
    }

    @Override
    public void paintTo( GlimpseContext glimpse, GlimpseBounds bounds, Axis2D axis )
    {
        GL2 gl = glimpse.getGL( ).getGL2( );

        gl.glMatrixMode( GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( axis.getMinX( ), axis.getMaxX( ), axis.getMinY( ), axis.getMaxY( ), -1, 1 );

        gl.glEnable( GL_BLEND );

        // Premultiplied alpha
        gl.glBlendFunc( GL_ONE, GL_ONE_MINUS_SRC_ALPHA );

        synchronized ( mutex )
        {
            // Don't try to paint after disposal
            if ( asyncExec.isShutdown( ) ) return;


            // Store values used in rasterizing icons and labels
            if ( rasterizeArgs == null )
            {
                int[] maxTextureDim = new int[ 1 ];
                gl.glGetIntegerv( GL_MAX_TEXTURE_SIZE, maxTextureDim, 0 );
                this.rasterizeArgs = new RasterizeArgs( maxTextureDim[ 0 ], glimpse.getDPI( ) );
                logger.fine( "Rasterization args: max-texture-dim = " + rasterizeArgs.maxTextureDim + ", screen-dpi = " + rasterizeArgs.screenDpi );
                mutex.notifyAll( );
            }


            // Build shader programs
            if ( areaProgram == null ) areaProgram = new DncAreaProgram( gl );
            if ( lineProgram == null ) lineProgram = new DncLineProgram( gl );
            if ( iconProgram == null ) iconProgram = new DncIconProgram( gl );
            if ( labelProgram == null ) labelProgram = new DncLabelProgram( gl );


            // Dispose of deactivated chunks
            int chunkDisposeCount = 0;
            long chunkDisposeStart_PMILLIS = System.currentTimeMillis( );
            while ( !dChunksToDispose.isEmpty( ) )
            {
                boolean allowDispose = ( chunkDisposeCount < guaranteedChunkDisposalsPerFrame || timeSince_MILLIS( chunkDisposeStart_PMILLIS ) <= chunkDisposeTimeLimit_MILLIS );
                if ( !allowDispose ) break;

                DncDeviceChunk dChunk = dChunksToDispose.remove( 0 );
                dChunk.dispose( gl );
                chunkDisposeCount++;
            }


            // Dispose of deactivated icon-atlases
            int iconAtlasDisposeCount = 0;
            long iconAtlasDisposeStart_PMILLIS = System.currentTimeMillis( );
            while ( !dIconAtlasesToDispose.isEmpty( ) )
            {
                boolean allowDispose = ( iconAtlasDisposeCount < guaranteedIconAtlasDisposalsPerFrame || timeSince_MILLIS( iconAtlasDisposeStart_PMILLIS ) <= iconAtlasDisposeTimeLimit_MILLIS );
                if ( !allowDispose ) break;

                DncDeviceIconAtlas dIconAtlas = dIconAtlasesToDispose.remove( 0 );
                dIconAtlas.dispose( gl );
                iconAtlasDisposeCount++;
            }


            // Dispose of deactivated label-atlases
            int labelAtlasDisposeCount = 0;
            long labelAtlasDisposeStart_PMILLIS = System.currentTimeMillis( );
            while ( !dLabelAtlasesToDispose.isEmpty( ) )
            {
                boolean allowDispose = ( labelAtlasDisposeCount < guaranteedLabelAtlasDisposalsPerFrame || timeSince_MILLIS( labelAtlasDisposeStart_PMILLIS ) <= labelAtlasDisposeTimeLimit_MILLIS );
                if ( !allowDispose ) break;

                DncDeviceLabelAtlas dLabelAtlas = dLabelAtlasesToDispose.remove( 0 );
                dLabelAtlas.dispose( gl );
                labelAtlasDisposeCount++;
            }


            // Dispose of deactivated highlight-sets
            int highlightSetDisposeCount = 0;
            long highlightSetDisposeStart_PMILLIS = System.currentTimeMillis( );
            while ( !highlightSetsToDispose.isEmpty( ) )
            {
                boolean allowDispose = ( highlightSetDisposeCount < guaranteedHighlightSetDisposalsPerFrame || timeSince_MILLIS( highlightSetDisposeStart_PMILLIS ) <= highlightSetDisposeTimeLimit_MILLIS );
                if ( !allowDispose ) break;

                IndexSetTexture highlightSet = highlightSetsToDispose.remove( 0 );
                highlightSet.freeDeviceResources( gl );
                highlightSetDisposeCount++;
            }


            // Identify chunks to draw
            Collection<DncChunkKey> chunksToDraw = new ArrayList<>( );
            for ( DncLibrary library : activeLibraries )
            {
                // If this axis is a control axis (i.e. it controls library activation),
                // then paint only libraries that are active for it specifically.
                //
                // Otherwise, paint all libraries, regardless of which control axis they
                // were loaded for.
                //
                if ( !axes.contains( axis ) || settings.isLibraryActive( library, axis ) )
                {
                    for ( DncCoverage coverage : activeCoverages )
                    {
                        chunksToDraw.add( new DncChunkKey( library, coverage ) );
                    }
                }
            }


            // Transfer chunks to the graphics device
            int chunkXferCount = 0;
            long chunkXferStart_PMILLIS = System.currentTimeMillis( );
            for ( DncChunkKey chunkKey : chunksToDraw )
            {
                if ( hChunks.containsKey( chunkKey ) )
                {
                    boolean allowXfer = ( chunkXferCount < guaranteedChunkXfersPerFrame || timeSince_MILLIS( chunkXferStart_PMILLIS ) <= chunkXferTimeLimit_MILLIS );
                    if ( allowXfer )
                    {
                        DncHostChunk hChunk = hChunks.remove( chunkKey );
                        DncDeviceChunk dChunk = xferChunkToDevice( hChunk, gl );
                        dChunks.put( chunkKey, dChunk );
                        chunkXferCount++;
                    }
                }
            }


            // Transfer icon-atlases to the graphics device
            int iconAtlasXferCount = 0;
            long iconAtlasXferStart_PMILLIS = System.currentTimeMillis( );
            for ( DncChunkKey chunkKey : chunksToDraw )
            {
                if ( hIconAtlases.containsKey( chunkKey ) )
                {
                    boolean allowXfer = ( iconAtlasXferCount < guaranteedIconAtlasXfersPerFrame || timeSince_MILLIS( iconAtlasXferStart_PMILLIS ) <= iconAtlasXferTimeLimit_MILLIS );
                    if ( allowXfer )
                    {
                        DncHostIconAtlas hIconAtlas = hIconAtlases.remove( chunkKey );
                        DncDeviceIconAtlas dIconAtlas = xferIconAtlasToDevice( hIconAtlas, gl );
                        dIconAtlases.put( chunkKey, dIconAtlas );
                        iconAtlasXferCount++;
                    }
                }
            }


            // Transfer label-atlases to the graphics device
            int labelAtlasXferCount = 0;
            long labelAtlasXferStart_PMILLIS = System.currentTimeMillis( );
            for ( DncChunkKey chunkKey : chunksToDraw )
            {
                if ( hLabelAtlases.containsKey( chunkKey ) )
                {
                    boolean allowXfer = ( labelAtlasXferCount < guaranteedLabelAtlasXfersPerFrame || timeSince_MILLIS( labelAtlasXferStart_PMILLIS ) <= labelAtlasXferTimeLimit_MILLIS );
                    if ( allowXfer )
                    {
                        DncHostLabelAtlas hLabelAtlas = hLabelAtlases.remove( chunkKey );
                        DncDeviceLabelAtlas dLabelAtlas = xferLabelAtlasToDevice( hLabelAtlas, gl );
                        dLabelAtlases.put( chunkKey, dLabelAtlas );
                        labelAtlasXferCount++;
                    }
                }
            }


            // Do the actual drawing
            boolean areasVisible = settings.areAreasVisible( axis );
            boolean linesVisible = settings.areLinesVisible( axis );
            boolean iconsVisible = settings.areIconsVisible( axis );
            boolean labelsVisible = settings.areLabelsVisible( axis );
            if ( areasVisible || linesVisible || iconsVisible || labelsVisible )
            {
                List<DncGroup> groupsToDraw = new ArrayList<>( );
                for ( DncChunkKey chunkKey : chunksToDraw )
                {
                    DncDeviceChunk dChunk = dChunks.get( chunkKey );
                    if ( dChunk != null ) groupsToDraw.addAll( dChunk.groups );
                }
                sort( groupsToDraw, groupRenderingOrder );

                float iconScale = settings.iconsGlobalScale( axis );

                long pulsatePeriod_MILLIS = 1100;
                long currentTime_PMILLIS = currentTimeMillis( );
                double pulsatePeriodFrac = ( ( double ) ( currentTime_PMILLIS % pulsatePeriod_MILLIS ) ) / ( ( double ) pulsatePeriod_MILLIS );
                double pulsateScale = 0.5*( 1.0 + cos( 2.0 * PI * pulsatePeriodFrac ) );

                float lineHighlightExtraThickness_PX = ( float ) ( 2.0*pulsateScale );
                float iconHighlightScale = ( float ) ( 1.0 + 0.75*pulsateScale );
                float labelHighlightScale = ( float ) ( 1.0 + 0.75*pulsateScale );

                gl.glEnableClientState( GL_VERTEX_ARRAY );


                for ( DncGroup group : groupsToDraw )
                {
                    DncDeviceChunk dChunk = dChunks.get( group.chunkKey );
                    DncDeviceIconAtlas dIconAtlas = dIconAtlases.get( group.chunkKey );
                    DncDeviceLabelAtlas dLabelAtlas = dLabelAtlases.get( group.chunkKey );
                    IndexSetTexture highlightSet = highlightSets.get( group.chunkKey );
                    DncGeosymAssignment geosymAssignment = group.geosymAssignment;

                    boolean drawGroupAreas = ( areasVisible && geosymAssignment.hasAreaSymbol( ) );
                    boolean drawGroupLines = ( linesVisible && geosymAssignment.hasLineSymbol( ) );
                    boolean drawGroupIcons = ( dIconAtlas != null && iconsVisible && geosymAssignment.hasPointSymbol( ) );
                    boolean drawGroupLabels = ( dLabelAtlas != null && labelsVisible && !geosymAssignment.labelMakers.isEmpty( ) );

                    if ( dChunk != null && highlightSet != null && ( drawGroupAreas || drawGroupLines || drawGroupIcons || drawGroupLabels ) )
                    {
                        int highlightSetTextureUnit = 1;
                        gl.glActiveTexture( GL_TEXTURE0 + highlightSetTextureUnit );
                        highlightSet.bind( gl, dChunk.featureCount, rasterizeArgs.maxTextureDim );

                        if ( drawGroupAreas )
                        {
                            DncGeosymLineAreaStyle style = lineAreaStyles.get( geosymAssignment.areaSymbolId );
                            if ( style != null && style.symbolType.equals( "AreaPlain" ) )
                            {
                                gl.glUseProgram( areaProgram.programHandle );

                                gl.glUniform1i( areaProgram.HIGHLIGHT_SET, highlightSetTextureUnit );
                                gl.glUniform4fv( areaProgram.RGBA, 1, style.fillRgba, 0 );

                                gl.glBindBuffer( GL_ARRAY_BUFFER, dChunk.verticesHandle );
                                gl.glVertexPointer( coordsPerRenderTriangleVertex, GL_FLOAT, 0, group.trianglesCoordFirst * SIZEOF_FLOAT );

                                gl.glDrawArrays( GL_TRIANGLES, 0, group.trianglesCoordCount / coordsPerRenderTriangleVertex );
                            }
                        }

                        if ( drawGroupLines )
                        {
                            DncGeosymLineAreaStyle style = lineAreaStyles.get( geosymAssignment.lineSymbolId );
                            if ( style != null )
                            {
                                gl.glUseProgram( lineProgram.programHandle );

                                gl.glUniform1i( lineProgram.HIGHLIGHT_SET, highlightSetTextureUnit );
                                gl.glUniform2f( lineProgram.PPV, ( float ) axis.getAxisX( ).getPixelsPerValue( ), ( float ) axis.getAxisY( ).getPixelsPerValue( ) );
                                gl.glUniform2f( lineProgram.VIEWPORT_SIZE_PX, bounds.getWidth( ), bounds.getHeight( ) );
                                gl.glUniform1f( lineProgram.LINE_THICKNESS_PX, style.lineWidth );
                                gl.glUniform1f( lineProgram.FEATHER_THICKNESS_PX, 1f );
                                gl.glUniform1f( lineProgram.HIGHLIGHT_EXTRA_THICKNESS_PX, lineHighlightExtraThickness_PX );
                                gl.glUniform4fv( lineProgram.RGBA, 1, style.lineRgba, 0 );

                                gl.glUniform1i( lineProgram.STIPPLE_ENABLE, style.hasLineStipple ? 1 : 0 );
                                gl.glUniform1f( lineProgram.STIPPLE_FACTOR, style.lineStippleFactor );
                                gl.glUniform1i( lineProgram.STIPPLE_PATTERN, style.lineStipplePattern );

                                gl.glBindBuffer( GL_ARRAY_BUFFER, dChunk.verticesHandle );
                                gl.glVertexPointer( coordsPerRenderLineVertex, GL_FLOAT, 0, group.linesCoordFirst * SIZEOF_FLOAT );

                                gl.glDrawArrays( GL_LINE_STRIP, 0, group.linesCoordCount / coordsPerRenderLineVertex );
                            }
                        }

                        if ( drawGroupIcons )
                        {
                            DncAtlasEntry atlasEntry = dIconAtlas.entries.get( geosymAssignment.pointSymbolId );
                            if ( atlasEntry != null )
                            {
                                gl.glUseProgram( iconProgram.programHandle );

                                gl.glUniform1i( iconProgram.HIGHLIGHT_SET, highlightSetTextureUnit );
                                gl.glUniform2f( iconProgram.VIEWPORT_SIZE_PX, bounds.getWidth( ), bounds.getHeight( ) );
                                gl.glUniform2f( iconProgram.IMAGE_SIZE_PX, iconScale * atlasEntry.w, iconScale * atlasEntry.h );
                                gl.glUniform2f( iconProgram.IMAGE_ALIGN, atlasEntry.xAlign, atlasEntry.yAlign );
                                gl.glUniform4f( iconProgram.IMAGE_BOUNDS, atlasEntry.sMin, atlasEntry.tMin, atlasEntry.sMax, atlasEntry.tMax );
                                gl.glUniform1f( iconProgram.HIGHLIGHT_SCALE, iconHighlightScale );

                                int iconAtlasTextureUnit = 0;
                                gl.glActiveTexture( GL_TEXTURE0 + iconAtlasTextureUnit );
                                gl.glBindTexture( GL_TEXTURE_2D, dIconAtlas.textureHandle );
                                gl.glUniform1i( iconProgram.ATLAS, iconAtlasTextureUnit );

                                gl.glBindBuffer( GL_ARRAY_BUFFER, dChunk.verticesHandle );
                                gl.glVertexPointer( coordsPerRenderIconVertex, GL_FLOAT, 0, group.iconsCoordFirst * SIZEOF_FLOAT );

                                gl.glDrawArrays( GL_POINTS, 0, group.iconsCoordCount / coordsPerRenderIconVertex );
                            }
                        }

                        if ( drawGroupLabels )
                        {
                            gl.glUseProgram( labelProgram.programHandle );

                            gl.glUniform1i( labelProgram.HIGHLIGHT_SET, highlightSetTextureUnit );
                            gl.glUniform2f( labelProgram.VIEWPORT_SIZE_PX, bounds.getWidth( ), bounds.getHeight( ) );
                            gl.glUniform2f( labelProgram.ATLAS_SIZE_PX, dLabelAtlas.textureWidth, dLabelAtlas.textureHeight );
                            gl.glUniform1f( labelProgram.HIGHLIGHT_SCALE, labelHighlightScale );

                            int labelAtlasTextureUnit = 0;
                            gl.glActiveTexture( GL_TEXTURE0 + labelAtlasTextureUnit );
                            gl.glBindTexture( GL_TEXTURE_2D, dLabelAtlas.textureHandle );
                            gl.glUniform1i( labelProgram.ATLAS, labelAtlasTextureUnit );

                            gl.glEnableVertexAttribArray( labelProgram.aImageAlign );
                            gl.glBindBuffer( GL_ARRAY_BUFFER, dLabelAtlas.entriesAlignHandle );
                            gl.glVertexAttribPointer( labelProgram.aImageAlign, coordsPerLabelAtlasAlign, GL_FLOAT, false, 0, group.labelFirst * coordsPerLabelAtlasAlign * SIZEOF_FLOAT );

                            gl.glEnableVertexAttribArray( labelProgram.aImageBounds );
                            gl.glBindBuffer( GL_ARRAY_BUFFER, dLabelAtlas.entriesBoundsHandle );
                            gl.glVertexAttribPointer( labelProgram.aImageBounds, coordsPerLabelAtlasBounds, GL_FLOAT, false, 0, group.labelFirst * coordsPerLabelAtlasBounds * SIZEOF_FLOAT );

                            gl.glBindBuffer( GL_ARRAY_BUFFER, dChunk.verticesHandle );
                            gl.glVertexPointer( coordsPerRenderLabelVertex, GL_FLOAT, 0, group.labelsCoordFirst * SIZEOF_FLOAT );

                            gl.glDrawArrays( GL_POINTS, 0, group.labelsCoordCount / coordsPerRenderLabelVertex );

                            gl.glDisableVertexAttribArray( labelProgram.aImageAlign );
                            gl.glDisableVertexAttribArray( labelProgram.aImageBounds );
                        }
                    }
                }


                gl.glDisableClientState( GL_VERTEX_ARRAY );
                gl.glUseProgram( 0 );
            }
        }
    }


}

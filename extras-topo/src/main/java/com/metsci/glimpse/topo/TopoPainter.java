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
package com.metsci.glimpse.topo;

import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.metsci.glimpse.gl.util.GLUtils.genTexture;
import static com.metsci.glimpse.support.colormap.ColorGradientUtils.newColorGradient;
import static com.metsci.glimpse.support.colormap.ColorGradientUtils.newColorTable;
import static com.metsci.glimpse.support.colormap.ColorGradientUtils.vc;
import static com.metsci.glimpse.support.wrapped.WrappedGlimpseContext.isFirstWrappedTile;
import static com.metsci.glimpse.topo.TopoLevelSet.createTopoLevels;
import static com.metsci.glimpse.util.concurrent.ConcurrencyUtils.newDaemonThreadFactory;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.logging.Level.FINE;
import static javax.media.opengl.GL.GL_CLAMP_TO_EDGE;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_SHORT;
import static javax.media.opengl.GL.GL_STATIC_DRAW;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_S;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_T;
import static javax.media.opengl.GL.GL_UNPACK_ALIGNMENT;
import static javax.media.opengl.GL2ES2.GL_R32F;
import static javax.media.opengl.GL2ES2.GL_RED;
import static javax.media.opengl.GL2GL3.GL_R16_SNORM;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.media.opengl.GL3;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.colormap.ColorGradient;
import com.metsci.glimpse.topo.io.TopoDataset;

public class TopoPainter extends GlimpsePainterBase
{
    private static final Logger logger = getLogger( TopoPainter.class );

    protected static final int maxRowsPerBand;
    protected static final int maxColsPerTile;
    protected static final int hTileDisposalsPerFrame;
    protected static final int dTileDisposalsPerFrame;
    protected static final int tileXfersPerFrame;
    protected static final boolean preloadLowerResTiles;
    protected static final boolean preloadHigherResTiles;
    static
    {
        int level;

        String s = System.getProperty( "topoPerfLevel" );
        if ( s == null )
        {
            logger.fine( "JVM property 'topoPerfLevel' is not set" );
            level = 0;
        }
        else
        {
            try
            {
                level = Integer.parseInt( s );
            }
            catch ( NumberFormatException e )
            {
                logger.warning( "JVM property 'topoPerfLevel' is not a parseable integer: " + s );
                level = 0;
            }
        }

        if ( level <= -1 )
        {
            maxRowsPerBand = 1024;
            maxColsPerTile = 1024;
            hTileDisposalsPerFrame = 1;
            dTileDisposalsPerFrame = 1;
            tileXfersPerFrame = 1;
            preloadLowerResTiles = false;
            preloadHigherResTiles = false;
        }
        else if ( level >= +1 )
        {
            maxRowsPerBand = 2048;
            maxColsPerTile = 2048;
            hTileDisposalsPerFrame = 1;
            dTileDisposalsPerFrame = 1;
            tileXfersPerFrame = 1;
            preloadLowerResTiles = true;
            preloadHigherResTiles = true;
        }
        else
        {
            maxRowsPerBand = 2048;
            maxColsPerTile = 2048;
            hTileDisposalsPerFrame = 1;
            dTileDisposalsPerFrame = 1;
            tileXfersPerFrame = 1;
            preloadLowerResTiles = true;
            preloadHigherResTiles = false;
        }

        if ( logger.isLoggable( FINE ) )
        {
            logger.fine( "Using topoPerfLevel " + level + ":"
                       + "\n  maxRowsPerBand:         " + maxRowsPerBand
                       + "\n  maxColsPerTile:         " + maxColsPerTile
                       + "\n  hTileDisposalsPerFrame: " + hTileDisposalsPerFrame
                       + "\n  dTileDisposalsPerFrame: " + dTileDisposalsPerFrame
                       + "\n  tileXfersPerFrame:      " + tileXfersPerFrame
                       + "\n  preloadLowerResTiles:   " + preloadLowerResTiles
                       + "\n  preloadHigherResTiles:  " + preloadHigherResTiles
                       + "\n" );
        }
        else
        {
            logger.info( "Using topoPerfLevel " + level );
        }
    }



    public static final ColorGradient bathyColorGradient = newColorGradient( -11000f,
                                                                             -0f,
                                                                             vc( -10000f,  0.00f, 0.00f, 0.00f  ),
                                                                             vc(  -8000f,  0.12f, 0.44f, 0.60f  ),
                                                                             vc(  -7000f,  0.32f, 0.62f, 0.80f  ),
                                                                             vc(  -6000f,  0.40f, 0.72f, 0.90f  ),
                                                                             vc(  -5000f,  0.53f, 0.79f, 0.95f  ),
                                                                             vc(     -0f,  0.84f, 0.92f, 1.00f  ) );

    public static final ColorGradient topoColorGradient = newColorGradient( +0f,
                                                                            +8000f,
                                                                            vc(     +0f,  0.36f, 0.63f, 0.31f  ),
                                                                            vc(    +50f,  0.42f, 0.70f, 0.38f  ),
                                                                            vc(   +750f,  0.49f, 0.76f, 0.45f  ),
                                                                            vc(  +3000f,  0.67f, 0.90f, 0.65f  ),
                                                                            vc(  +5500f,  0.90f, 0.95f, 0.90f  ),
                                                                            vc(  +6500f,  0.99f, 0.99f, 0.99f  ) );

    protected final TopoLevelSet levels;

    protected final ExecutorService async;

    protected final Map<TopoTileKey,TopoHostTile> hTiles;
    protected final Map<TopoTileKey,TopoDeviceTile> dTiles;
    protected final TopoProgram prog;

    protected long frameNum;


    public TopoPainter( TopoDataset dataset )
    {
        this.levels = createTopoLevels( dataset, maxRowsPerBand, maxColsPerTile );

        this.async = newSingleThreadExecutor( newDaemonThreadFactory( "TopoPainter.Async.%d" ) );

        // Create hTiles and dTiles with access ordering, so iteration visits the least recently accessed entry first
        this.hTiles = new LinkedHashMap<>( 16, 0.75f, /* accessOrder */ true );
        this.dTiles = new LinkedHashMap<>( 16, 0.75f, /* accessOrder */ true );

        ColorTexture1D bathyColorTable = newColorTable( bathyColorGradient, 1024 );
        ColorTexture1D topoColorTable = newColorTable( topoColorGradient, 1024 );
        this.prog = new TopoProgram( 2, 3, 4, bathyColorTable, topoColorTable, -11000f, +8000f );

        this.frameNum = 0;
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        if ( isFirstWrappedTile( context ) )
        {
            this.frameNum++;
        }


        // Dispose of unneeded hTiles
        // We created hTiles with access ordering, so iteration visits the least recently accessed entry first
        int hTileDisposeCount = 0;
        for ( Iterator<Entry<TopoTileKey,TopoHostTile>> it = this.hTiles.entrySet( ).iterator( ); it.hasNext( ); )
        {
            Entry<TopoTileKey,TopoHostTile> en = it.next( );
            TopoHostTile hTile = en.getValue( );

            // If we've gone a complete frame without using this hTile, it's elligible for disposal
            if ( hTile != null && this.frameNum >= hTile.frameNumOfLastUse + 2 )
            {
                if ( hTileDisposeCount < hTileDisposalsPerFrame )
                {
                    it.remove( );
                    hTile.dispose( );
                    hTileDisposeCount++;
                }
            }
        }

        // Dispose of unneeded dTiles
        // We created dTiles with access ordering, so iteration visits the least recently accessed entry first
        int dTileDisposeCount = 0;
        for ( Iterator<Entry<TopoTileKey,TopoDeviceTile>> it = this.dTiles.entrySet( ).iterator( ); it.hasNext( ); )
        {
            Entry<TopoTileKey,TopoDeviceTile> en = it.next( );
            TopoDeviceTile dTile = en.getValue( );

            // If we've gone a complete frame without using this dTile, it's elligible for disposal
            if ( this.frameNum >= dTile.frameNumOfLastUse + 2 )
            {
                if ( dTileDisposeCount < dTileDisposalsPerFrame )
                {
                    it.remove( );
                    dTile.dispose( gl );
                    dTileDisposeCount++;
                }
            }
        }


        // Identify visible levelNum
        Axis2D axis = requireAxis2D( context );
        double pixelSize_DEG = 1.0 / axis.getAxisX( ).getPixelsPerValue( );
        int levelNum = min( this.levels.size( ) - 1, this.levels.cellSizes_DEG.indexAtOrAfter( pixelSize_DEG ) );

        // Identify tiles visible on the current level
        Collection<TopoTileKey> tilesToDraw = this.findTiles( axis, levelNum );

        // Identify tiles worth having ready
        List<TopoTileKey> tilesToPrep = new ArrayList<>( );

        tilesToPrep.addAll( tilesToDraw );

        if ( preloadLowerResTiles && levelNum < this.levels.size( ) - 1 )
        {
            tilesToPrep.addAll( this.findTiles( axis, levelNum + 1 ) );
        }

        if ( preloadHigherResTiles && levelNum > 0 )
        {
            tilesToPrep.addAll( this.findTiles( axis, levelNum - 1 ) );
        }


        // Load hTiles
        for ( TopoTileKey tileKey : tilesToPrep )
        {
            if ( !this.dTiles.containsKey( tileKey ) && !this.hTiles.containsKey( tileKey ) )
            {
                // While an hTile load is in progress: containsKey() will return true, but get() will return null
                this.hTiles.put( tileKey, null );

                this.async.submit( ( ) ->
                {
                    // Include a 1-cell border, so that texture interpolation works right across tile boundaries
                    TopoHostTile hTile = createHostTile( this.levels, tileKey, 1 );
                    SwingUtilities.invokeLater( ( ) ->
                    {
                        this.hTiles.put( tileKey, hTile );
                        hTile.frameNumOfLastUse = this.frameNum;
                    } );
                } );
            }
        }


        // Xfer hTiles to device
        // TODO: Check whether GL_UNPACK_SWAP_BYTES is needed
        int tileXferCount = 0;
        for ( TopoTileKey tileKey : tilesToPrep )
        {
            TopoHostTile hTile = this.hTiles.get( tileKey );
            if ( hTile != null && !this.dTiles.containsKey( tileKey ) && tileXferCount < tileXfersPerFrame )
            {
                TopoDeviceTile dTile = xferHostTileToDevice( gl, hTile );
                this.dTiles.put( tileKey, dTile );

                this.hTiles.remove( tileKey );
                hTile.dispose( );

                tileXferCount++;
            }
        }


        // Identify lower-res tiles we can use to fill in for missing tiles
        Set<TopoTileKey> fallbackTiles_UNORDERED = new HashSet<>( );
        for ( TopoTileKey tileKey : tilesToDraw )
        {
            if ( !this.dTiles.containsKey( tileKey ) )
            {
                TopoTileBounds tileBounds = this.levels.get( tileKey.levelNum ).tileBounds( tileKey.bandNum, tileKey.tileNum );

                double northLat_DEG = min( axis.getMaxY( ), tileBounds.northLat_DEG );
                double southLat_DEG = max( axis.getMinY( ), tileBounds.southLat_DEG );
                double westLon_DEG = max( axis.getMinX( ), tileBounds.westLon_DEG );
                double eastLon_DEG = min( axis.getMaxX( ), tileBounds.eastLon_DEG );

                for ( int fallbackLevelNum = tileKey.levelNum + 1; fallbackLevelNum < this.levels.size( ); fallbackLevelNum++ )
                {
                    Collection<TopoTileKey> tileKeys = this.findTiles( northLat_DEG, southLat_DEG, westLon_DEG, eastLon_DEG, fallbackLevelNum );
                    fallbackTiles_UNORDERED.addAll( tileKeys );

                    // If we have the dTiles necessary to completely cover the area of the missing tile,
                    // don't bother checking any more levels. (It's possible that the area is not covered
                    // by dTiles from a single fallbackLevelNum, but IS covered by dTiles from multiple
                    // fallbackLevelNums ... but that's complicated to check for, and at worst we just end
                    // up drawing more tiles than we need to for a frame or two.)
                    if ( this.dTiles.keySet( ).containsAll( tileKeys ) )
                    {
                        break;
                    }
                }
            }
        }

        // Order fallback tiles from low-res to high-res
        List<TopoTileKey> fallbackTilesToDraw = new ArrayList<>( fallbackTiles_UNORDERED );
        fallbackTilesToDraw.sort( ( a, b ) ->
        {
            return ( -1 * Integer.compare( a.levelNum, b.levelNum ) );
        } );


        // Mark used tiles
        Collection<TopoTileKey> tilesToRetain = new LinkedHashSet<>( );
        tilesToRetain.addAll( tilesToPrep );
        tilesToRetain.addAll( fallbackTilesToDraw );
        for ( TopoTileKey tileKey : tilesToRetain )
        {
            if ( this.dTiles.containsKey( tileKey ) )
            {
                TopoDeviceTile dTile = this.dTiles.get( tileKey );
                dTile.frameNumOfLastUse = this.frameNum;
            }

            if ( this.hTiles.containsKey( tileKey ) )
            {
                TopoHostTile hTile = this.hTiles.get( tileKey );
                if ( hTile != null )
                {
                    hTile.frameNumOfLastUse = this.frameNum;
                }
            }
        }


        // Draw visible tiles
        this.prog.begin( context, axis );
        try
        {
            // Draw fallback tiles, from low-res to high-res
            for ( TopoTileKey tileKey : fallbackTilesToDraw )
            {
                TopoDeviceTile dTile = this.dTiles.get( tileKey );
                if ( dTile != null )
                {
                    this.prog.draw( context, dTile );
                }
            }

            // Draw current-level tiles on top
            for ( TopoTileKey tileKey : tilesToDraw )
            {
                TopoDeviceTile dTile = this.dTiles.get( tileKey );
                if ( dTile != null )
                {
                    this.prog.draw( context, dTile );
                }
            }
        }
        finally
        {
            this.prog.end( context );
        }
    }

    protected Collection<TopoTileKey> findTiles( Axis2D axis, int levelNum )
    {
        return findTiles( axis.getMaxY( ), axis.getMinY( ), axis.getMinX( ), axis.getMaxX( ), levelNum );
    }

    protected Collection<TopoTileKey> findTiles( double northLat_DEG, double southLat_DEG, double westLon_DEG, double eastLon_DEG, int levelNum )
    {
        TopoLevel level = this.levels.get( levelNum );

        int minBandNum = ( int ) floor( ( level.northLat_DEG - northLat_DEG ) / level.bandHeight_DEG );
        int maxBandNum = ( int ) ceil( ( level.northLat_DEG - southLat_DEG ) / level.bandHeight_DEG );
        int minTileNum = ( int ) floor( ( westLon_DEG - level.westLon_DEG ) / level.tileWidth_DEG );
        int maxTileNum = ( int ) ceil( ( eastLon_DEG - level.westLon_DEG ) / level.tileWidth_DEG );

        Collection<TopoTileKey> tileKeys = new ArrayList<>( );
        for ( int bandNum = max( 0, minBandNum ); bandNum <= min( level.numBands - 1, maxBandNum ); bandNum++ )
        {
            for ( int tileNum = max( 0, minTileNum ); tileNum <= min( level.numTiles - 1, maxTileNum ); tileNum++ )
            {
                TopoTileKey tileKey = new TopoTileKey( levelNum, bandNum, tileNum );
                tileKeys.add( tileKey );
            }
        }
        return tileKeys;
    }

    protected static TopoHostTile createHostTile( TopoLevelSet levels, TopoTileKey tileKey, int numBorderCells )
    {
        TopoLevel level = levels.get( tileKey.levelNum );
        return level.copyTile( tileKey.bandNum, tileKey.tileNum, numBorderCells );
    }

    protected static TopoDeviceTile xferHostTileToDevice( GL3 gl, TopoHostTile hTile )
    {
        int texture = genTexture( gl );
        gl.glBindTexture( GL_TEXTURE_2D, texture );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );

        gl.glPixelStorei( GL_UNPACK_ALIGNMENT, hTile.dataType.numBytes );

        switch ( hTile.dataType )
        {
            case TOPO_I2:
            {
                ShortBuffer dataShorts = hTile.dataBytes.asShortBuffer( );
                gl.glTexImage2D( GL_TEXTURE_2D, 0, GL_R16_SNORM, hTile.numDataCols, hTile.numDataRows, 0, GL_RED, GL_SHORT, dataShorts );
            }
            break;

            case TOPO_F4:
            {
                FloatBuffer dataFloats = hTile.dataBytes.asFloatBuffer( );
                gl.glTexImage2D( GL_TEXTURE_2D, 0, GL_R32F, hTile.numDataCols, hTile.numDataRows, 0, GL_RED, GL_FLOAT, dataFloats );
            }
            break;

            default:
            {
                throw new RuntimeException( "Unrecognized data type: " + hTile.dataType );
            }
        }

        GLEditableBuffer xyBuffer = new GLEditableBuffer( GL_STATIC_DRAW, 8 * SIZEOF_FLOAT );
        xyBuffer.grow2f( ( float ) hTile.westLon_DEG, ( float ) hTile.northLat_DEG );
        xyBuffer.grow2f( ( float ) hTile.westLon_DEG, ( float ) hTile.southLat_DEG );
        xyBuffer.grow2f( ( float ) hTile.eastLon_DEG, ( float ) hTile.northLat_DEG );
        xyBuffer.grow2f( ( float ) hTile.eastLon_DEG, ( float ) hTile.southLat_DEG );

        double sInset_FRAC = ( ( double ) hTile.numBorderCells ) / ( ( double ) hTile.numDataCols );
        double tInset_FRAC = ( ( double ) hTile.numBorderCells ) / ( ( double ) hTile.numDataRows );
        GLEditableBuffer stBuffer = new GLEditableBuffer( GL_STATIC_DRAW, 8 * SIZEOF_FLOAT );
        stBuffer.grow2f( ( float ) ( 0.0 + sInset_FRAC ), ( float ) ( 0.0 + tInset_FRAC ) );
        stBuffer.grow2f( ( float ) ( 0.0 + sInset_FRAC ), ( float ) ( 1.0 - tInset_FRAC ) );
        stBuffer.grow2f( ( float ) ( 1.0 - sInset_FRAC ), ( float ) ( 0.0 + tInset_FRAC ) );
        stBuffer.grow2f( ( float ) ( 1.0 - sInset_FRAC ), ( float ) ( 1.0 - tInset_FRAC ) );

        int numVertices = xyBuffer.sizeFloats( ) / 2;

        return new TopoDeviceTile( texture, hTile.dataType, xyBuffer.deviceBuffer( gl ), stBuffer.deviceBuffer( gl ), numVertices, 0 );
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        this.levels.dispose( );

        for ( TopoHostTile hTile : this.hTiles.values( ) )
        {
            if ( hTile != null )
            {
                hTile.dispose( );
            }
        }
        this.hTiles.clear( );

        for ( TopoDeviceTile dTile : this.dTiles.values( ) )
        {
            dTile.dispose( context.getGL( ) );
        }
        this.dTiles.clear( );
    }

}

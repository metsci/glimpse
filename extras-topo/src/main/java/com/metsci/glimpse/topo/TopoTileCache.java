/*
 * Copyright (c) 2020, Metron, Inc.
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
import static com.metsci.glimpse.core.gl.util.GLUtils.genTexture;
import static com.metsci.glimpse.core.support.QuickUtils.requireSwingThread;
import static com.metsci.glimpse.topo.TopoLevelSet.createTopoLevels;
import static com.metsci.glimpse.topo.TopoUtils.intersect;
import static com.metsci.glimpse.util.concurrent.ConcurrencyUtils.newDaemonThreadFactory;
import static com.metsci.glimpse.util.math.MathConstants.HALF_PI;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_SHORT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL2ES2.GL_R32F;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2GL3.GL_R16_SNORM;

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

import com.jogamp.opengl.GL3;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.gl.GLEditableBuffer;
import com.metsci.glimpse.topo.io.TopoDataset;
import com.metsci.glimpse.topo.proj.NormalCylindricalProjection;

public class TopoTileCache
{

    public final TopoPainterConfig config;
    public final TopoLevelSet levels;
    public final NormalCylindricalProjection proj;

    protected final ExecutorService async;

    protected final Map<TopoTileKey,TopoHostTile> hTiles;
    protected final Map<TopoTileKey,TopoDeviceTile> dTiles;

    protected long frameNum;

    protected boolean disposed;


    public TopoTileCache( TopoDataset dataset, NormalCylindricalProjection proj, TopoPainterConfig config )
    {
        this.config = config;
        this.levels = createTopoLevels( dataset, this.config.maxRowsPerBand, this.config.maxColsPerTile );
        this.proj = proj;

        this.async = newSingleThreadExecutor( newDaemonThreadFactory( "TopoTileCache.Async.%d" ) );

        // Create hTiles and dTiles with access ordering, so iteration visits the least recently accessed entry first
        this.hTiles = new LinkedHashMap<>( 16, 0.75f, /* accessOrder */ true );
        this.dTiles = new LinkedHashMap<>( 16, 0.75f, /* accessOrder */ true );

        this.frameNum = 0;

        this.disposed = false;
    }

    public List<TopoDeviceTile> update( GL3 gl, long frameNum, LatLonBox viewBounds, int levelNum )
    {
        requireSwingThread( );

        // Update latest frameNum
        this.frameNum = frameNum;

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
                if ( hTileDisposeCount < this.config.hTileDisposalsPerFrame )
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
                if ( dTileDisposeCount < this.config.dTileDisposalsPerFrame )
                {
                    it.remove( );
                    dTile.dispose( gl );
                    dTileDisposeCount++;
                }
            }
        }

        // Identify tiles visible on the current level
        Collection<TopoTileKey> tilesToDraw = this.findTiles( viewBounds, levelNum );

        // Identify tiles worth having ready
        List<TopoTileKey> tilesToPrep = new ArrayList<>( );

        tilesToPrep.addAll( tilesToDraw );

        if ( this.config.preloadLowerResTiles && levelNum < this.levels.size( ) - 1 )
        {
            tilesToPrep.addAll( this.findTiles( viewBounds, levelNum + 1 ) );
        }

        if ( this.config.preloadHigherResTiles && levelNum > 0 )
        {
            tilesToPrep.addAll( this.findTiles( viewBounds, levelNum - 1 ) );
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
                        if ( !this.disposed )
                        {
                            this.hTiles.put( tileKey, hTile );
                            hTile.frameNumOfLastUse = this.frameNum;
                        }
                        else
                        {
                            hTile.dispose( );
                        }
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
            if ( hTile != null && !this.dTiles.containsKey( tileKey ) && tileXferCount < this.config.tileXfersPerFrame )
            {
                TopoDeviceTile dTile = xferHostTileToDevice( gl, hTile, this.proj );
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
                LatLonBox missingBox = intersect( viewBounds, tileBounds );

                for ( int fallbackLevelNum = tileKey.levelNum + 1; fallbackLevelNum < this.levels.size( ); fallbackLevelNum++ )
                {
                    Collection<TopoTileKey> tileKeys = this.findTiles( missingBox, fallbackLevelNum );
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

        // List tiles to be drawn
        List<TopoDeviceTile> dTilesToDraw = new ArrayList<>( );

        // Fallback tiles should be drawn from low-res to high-res
        for ( TopoTileKey tileKey : fallbackTilesToDraw )
        {
            TopoDeviceTile dTile = this.dTiles.get( tileKey );
            if ( dTile != null )
            {
                dTilesToDraw.add( dTile );
            }
        }

        // Current-level tiles should be drawn on top
        for ( TopoTileKey tileKey : tilesToDraw )
        {
            TopoDeviceTile dTile = this.dTiles.get( tileKey );
            if ( dTile != null )
            {
                dTilesToDraw.add( dTile );
            }
        }

        return dTilesToDraw;
    }

    protected Collection<TopoTileKey> findTiles( LatLonBox viewBounds, int levelNum )
    {
        TopoLevel level = this.levels.get( levelNum );

        int minBandNum = ( int ) floor( ( level.northLat_DEG - viewBounds.northLat_DEG ) / level.bandHeight_DEG );
        int maxBandNum = ( int ) ceil( ( level.northLat_DEG - viewBounds.southLat_DEG ) / level.bandHeight_DEG );
        int minTileNum = ( int ) floor( ( viewBounds.westLon_DEG - level.westLon_DEG ) / level.tileWidth_DEG );
        int maxTileNum = ( int ) ceil( ( viewBounds.eastLon_DEG - level.westLon_DEG ) / level.tileWidth_DEG );

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

    protected static TopoDeviceTile xferHostTileToDevice( GL3 gl, TopoHostTile hTile, NormalCylindricalProjection proj )
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



        float yNorth;
        double visibleNorthLat_RAD = hTile.northLat_RAD - hTile.borderSize_RAD;
        if ( visibleNorthLat_RAD >= +HALF_PI )
        {
            yNorth = ( float ) proj.maxUsableY( );
        }
        else
        {
            yNorth = ( float ) min( proj.maxUsableY( ), proj.latToY( visibleNorthLat_RAD ) );
        }

        float ySouth;
        double visibleSouthLat_RAD = hTile.southLat_RAD + hTile.borderSize_RAD;
        if ( visibleSouthLat_RAD <= -HALF_PI )
        {
            ySouth = ( float ) proj.minUsableY( );
        }
        else
        {
            ySouth = ( float ) max( proj.minUsableY( ), proj.latToY( visibleSouthLat_RAD ) );
        }

        float xEast = ( float ) proj.lonToX( hTile.eastLon_RAD - hTile.borderSize_RAD );
        float xWest = ( float ) proj.lonToX( hTile.westLon_RAD + hTile.borderSize_RAD );
        GLEditableBuffer xyBuffer = new GLEditableBuffer( GL_STATIC_DRAW, 8 * SIZEOF_FLOAT );
        xyBuffer.grow2f( xWest, yNorth );
        xyBuffer.grow2f( xWest, ySouth );
        xyBuffer.grow2f( xEast, yNorth );
        xyBuffer.grow2f( xEast, ySouth );

        int numVertices = xyBuffer.sizeFloats( ) / 2;

        return new TopoDeviceTile( hTile.northLat_RAD,
                                   hTile.southLat_RAD,
                                   hTile.eastLon_RAD,
                                   hTile.westLon_RAD,

                                   texture,
                                   hTile.dataType,

                                   xyBuffer.deviceBuffer( gl ),
                                   numVertices,

                                   0 );
    }

    protected void dispose( GlimpseContext context )
    {
        requireSwingThread( );

        this.disposed = true;

        this.async.shutdown( );

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

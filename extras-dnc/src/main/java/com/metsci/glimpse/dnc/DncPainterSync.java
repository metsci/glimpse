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
package com.metsci.glimpse.dnc;

import static com.jogamp.opengl.GL.GL_MAX_TEXTURE_SIZE;
import static com.metsci.glimpse.core.painter.base.GlimpsePainterBase.requireAxis2D;
import static com.metsci.glimpse.dnc.DncChunks.xferChunkToDevice;
import static com.metsci.glimpse.dnc.DncIconAtlases.xferIconAtlasToDevice;
import static com.metsci.glimpse.dnc.DncLabelAtlases.xferLabelAtlasToDevice;
import static com.metsci.glimpse.dnc.convert.Flat2Render.DncChunkPriority.NICE;
import static com.metsci.glimpse.dnc.geosym.DncGeosymThemes.DNC_THEME_STANDARD;
import static java.lang.Long.MAX_VALUE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;

import com.jogamp.opengl.GL2ES2;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.dnc.DncChunks.DncDeviceChunk;
import com.metsci.glimpse.dnc.DncChunks.DncHostChunk;
import com.metsci.glimpse.dnc.DncIconAtlases.DncDeviceIconAtlas;
import com.metsci.glimpse.dnc.DncIconAtlases.DncHostIconAtlas;
import com.metsci.glimpse.dnc.DncLabelAtlases.DncDeviceLabelAtlas;
import com.metsci.glimpse.dnc.DncLabelAtlases.DncHostLabelAtlas;
import com.metsci.glimpse.dnc.convert.Flat2Render.DncChunkJob;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCache;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCacheConfig;
import com.metsci.glimpse.dnc.geosym.DncGeosymTheme;

/**
 * A {@link DncPainter} whose paint method blocks until relevant background
 * tasks have finished. Intended for non-interactive use (e.g. painting to an
 * offscreen image).
 * <p>
 * <strong>WARNING:</strong> this implementation is quite kludgy. A better
 * impl will require changes to RenderCache and DncPainter.
 * <p>
 * TODO: Re-implement less kludgily
 */
public class DncPainterSync extends DncPainter
{

    public static class RenderCacheSync extends RenderCache
    {
        public RenderCacheSync( RenderCacheConfig config ) throws IOException
        {
            // Always use a single converter thread, so that completePendingAsyncTasks() will work reliably
            super( config, 1 );
        }

        public void completePendingAsyncTasks( Object mutex ) throws InterruptedException
        {
            boolean[] done = { false };

            // Submit a job with chunkKey, submitTime, and priority that cause it to run after all real jobs
            DncChunkKey dummyChunkKey = new DncChunkKey( null, new DncCoverage( "" ) );
            this.conversionExec.execute( new DncChunkJob( dummyChunkKey, MAX_VALUE, NICE )
            {
                @Override
                public void runThrows( )
                {
                    synchronized ( mutex )
                    {
                        done[0] = true;
                        mutex.notifyAll( );
                    }
                }
            } );

            synchronized ( mutex )
            {
                while ( !done[0] )
                {
                    mutex.wait( );
                }
            }
        }
    }


    protected final RenderCacheSync cacheSync;

    public DncPainterSync( RenderCacheSync cacheSync, DncPainterSettings settings )
    {
        this( cacheSync, settings, DNC_THEME_STANDARD );
    }

    public DncPainterSync( RenderCacheSync cacheSync, DncPainterSettings settings, DncGeosymTheme theme )
    {
        super( cacheSync, settings, theme );
        this.cacheSync = cacheSync;
    }

    /**
     * Assumes that {@code fifoExec} executes jobs one at a time, in FIFO order.
     */
    protected static void completePendingAsyncTasks( Executor fifoExec, Object mutex ) throws InterruptedException
    {
        boolean[] done = { false };
        fifoExec.execute( ( ) ->
        {
            synchronized ( mutex )
            {
                done[0] = true;
                mutex.notifyAll( );
            }
        } );

        synchronized ( mutex )
        {
            while ( !done[0] )
            {
                mutex.wait( );
            }
        }
    }

    @Override
    public void paintTo( GlimpseContext context )
    {
        Axis2D axis = requireAxis2D( context );
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        // Set rasterizeArgs and then release the mutex, so that async tasks can proceed
        synchronized ( this.mutex )
        {
            if ( this.rasterizeArgs == null )
            {
                int[] maxTextureDim = new int[ 1 ];
                gl.glGetIntegerv( GL_MAX_TEXTURE_SIZE, maxTextureDim, 0 );
                this.rasterizeArgs = new RasterizeArgs( maxTextureDim[ 0 ], context.getDPI( ) );
                logger.fine( "Rasterization args: max-texture-dim = " + this.rasterizeArgs.maxTextureDim + ", screen-dpi = " + rasterizeArgs.screenDpi );
                this.mutex.notifyAll( );
            }
        }

        // No new conversions will be started while we hold the mutex
        synchronized ( this.mutex )
        {
            if ( !this.visible ) return;

            // Don't try to paint after disposal
            if ( this.asyncExec.isShutdown( ) ) return;

            // Wait for pending async tasks to complete
            try
            {
                this.cacheSync.completePendingAsyncTasks( this.mutex );
                completePendingAsyncTasks( this.asyncExec, this.mutex );
                completePendingAsyncTasks( this.iconsExec, this.mutex );
                completePendingAsyncTasks( this.labelsExec, this.mutex );
            }
            catch ( InterruptedException e )
            {
                logger.warning( "Paint was interrupted while waiting for async tasks to complete" );
                return;
            }

            // Identify chunks to draw
            Collection<DncChunkKey> chunksToDraw = new ArrayList<>( );
            for ( DncLibrary library : this.activeLibraries )
            {
                if ( !this.axes.contains( axis ) || this.settings.isLibraryActive( library, axis ) )
                {
                    for ( DncCoverage coverage : this.activeCoverages )
                    {
                        chunksToDraw.add( new DncChunkKey( library, coverage ) );
                    }
                }
            }

            // Transfer chunks to the graphics device
            for ( DncChunkKey chunkKey : chunksToDraw )
            {
                if ( this.hChunks.containsKey( chunkKey ) )
                {
                    DncHostChunk hChunk = this.hChunks.remove( chunkKey );
                    DncDeviceChunk dChunk = xferChunkToDevice( hChunk, gl );
                    this.dChunks.put( chunkKey, dChunk );
                }
            }

            // Transfer icon-atlases to the graphics device
            for ( DncChunkKey chunkKey : chunksToDraw )
            {
                if ( this.hIconAtlases.containsKey( chunkKey ) )
                {
                    DncHostIconAtlas hIconAtlas = this.hIconAtlases.remove( chunkKey );
                    DncDeviceIconAtlas dIconAtlas = xferIconAtlasToDevice( hIconAtlas, gl );
                    this.dIconAtlases.put( chunkKey, dIconAtlas );
                }
            }

            // Transfer label-atlases to the graphics device
            for ( DncChunkKey chunkKey : chunksToDraw )
            {
                if ( this.hLabelAtlases.containsKey( chunkKey ) )
                {
                    DncHostLabelAtlas hLabelAtlas = this.hLabelAtlases.remove( chunkKey );
                    DncDeviceLabelAtlas dLabelAtlas = xferLabelAtlasToDevice( hLabelAtlas, gl );
                    this.dLabelAtlases.put( chunkKey, dLabelAtlas );
                }
            }

            super.paintTo( context );
        }
    }

}

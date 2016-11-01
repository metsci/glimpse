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
package com.metsci.glimpse.support.texture;

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

/**
 * A texture class which stores 32 bit fixed point values (uncapped).
 *
 * @author ulman
 *
 */
public class FloatTextureProjected2D extends TextureProjected2D
{
    private static final Logger logger = Logger.getLogger( FloatTextureProjected2D.class.getName( ) );

    public FloatTextureProjected2D( int dataSizeX, int dataSizeY )
    {
        this( dataSizeX, dataSizeY, false );
    }

    public FloatTextureProjected2D( int dataSizeX, int dataSizeY, boolean useVertexZCoord )
    {
        super( dataSizeX, dataSizeY, useVertexZCoord );
    }

    protected Buffer prepare_setPixelStore( GL gl, int i )
    {
        gl.glPixelStorei( GL2.GL_UNPACK_ALIGNMENT, 1 );
        gl.glPixelStorei( GL2.GL_UNPACK_SKIP_PIXELS, texStartsX[i] );
        gl.glPixelStorei( GL2.GL_UNPACK_ROW_LENGTH, dataSizeX );

        // for some reason, the following does not work:
        //gl.glPixelStorei( GL2.GL_UNPACK_SKIP_ROWS, texStartsY[i] );
        // however, skipping rows manually using data.position works
        return data.asFloatBuffer( ).position( texStartsY[i] * dataSizeX );
    }

    @Override
    protected void prepare_setData( GL gl )
    {
        for ( int i = 0; i < numTextures; i++ )
        {
            gl.glBindTexture( getTextureType( ), textureHandles[i] );

            prepare_setTexParameters( gl );
            Buffer positionedBuffer = prepare_setPixelStore( gl, i );

            gl.glTexImage2D( GL3.GL_TEXTURE_2D, 0, GL3.GL_R32F, texSizesX[i], texSizesY[i], 0, GL3.GL_RED, GL3.GL_FLOAT, positionedBuffer );
        }

        gl.glPixelStorei( GL2.GL_UNPACK_SKIP_PIXELS, 0 );
        gl.glPixelStorei( GL2.GL_UNPACK_SKIP_ROWS, 0 );
        gl.glPixelStorei( GL2.GL_UNPACK_ROW_LENGTH, 0 );
    }

    @Override
    protected int getRequiredCapacityBytes( )
    {
        return dataSizeX * dataSizeY * BYTES_PER_FLOAT;
    }

    @Override
    protected float getData( int index )
    {
        return data.asFloatBuffer( ).get( index );
    }

    /**
     * Updates this texture with new data values. The dimensions of the data
     * array should be float[n0][n1] where n0 and n1 are the first and second
     * arguments provided to resize( ) or to the Texture constructor.
     *
     * @param data the new data values to load into this texture.
     */
    public void setData( float[][] data )
    {
        mutate( new SetDataMutator( data ) );
    }

    public void setData( float[][] data, boolean flip )
    {
        mutate( new SetDataMutator( data, flip ) );
    }

    /**
     * Updates this texture with new data values. The dimensions of the data
     * array should be float[n0][n1] where n0 and n1 are the first and second
     * arguments provided to resize( ) or to the Texture constructor.
     *
     * @param data the new data values to load into this texture.
     */
    public void setData( double[][] data )
    {
        mutate( new SetDataMutator( data ) );
    }

    public void setData( double[][] data, boolean flip )
    {
        mutate( new SetDataMutator( data, flip ) );
    }

    /**
     * Provides a general, thread-safe mechanism for arbitrarily updating the
     * data values for this Texture. Modifications made to the FloatBuffer
     * passed as an argument to the mutate( ) function will be reflected in
     * the Texture data.
     *
     * @param mutator a class defining the operation which should be applied to
     *        the texture data.
     */
    public void mutate( MutatorFloat2D mutator )
    {
        lock.lock( );
        try
        {
            data.rewind( );
            mutator.mutate( data.asFloatBuffer( ), dataSizeX, dataSizeY );
            makeDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public static interface MutatorFloat2D
    {
        public void mutate( FloatBuffer data, int dataSizeX, int dataSizeY );
    }

    public static class SetDataMutator implements MutatorFloat2D
    {
        public float[][] dataf;
        public double[][] datad;
        public boolean flip;

        public SetDataMutator( float[][] data, boolean flip )
        {
            this.dataf = data;
            this.flip = flip;
        }

        public SetDataMutator( double[][] data, boolean flip )
        {
            this.datad = data;
            this.flip = flip;
        }

        public SetDataMutator( float[][] data )
        {
            this( data, false );
        }

        public SetDataMutator( double[][] data )
        {
            this( data, false );
        }

        @Override
        public void mutate( FloatBuffer buffer, int dataSizeX, int dataSizeY )
        {
            if ( dataf != null )
                mutatef( buffer, dataSizeX, dataSizeY );
            else if ( datad != null ) mutated( buffer, dataSizeX, dataSizeY );
        }

        public void mutatef( FloatBuffer buffer, int dataSizeX, int dataSizeY )
        {
            if ( dataf == null )
            {
                logger.warning( "Null data array passed to SetDataMutator." );
                return;
            }

            buffer.clear( );

            if ( !flip )
            {
                if ( dataf.length != dataSizeX || dataf[0].length != dataSizeY )
                {
                    logWarning( logger, "Incorrectly sized data array passed to SetDataMutator. Expected %d %d. Got %d %d.", dataSizeX, dataSizeY, dataf.length, dataf[0].length );
                    return;
                }

                for ( int y = 0; y < dataSizeY; y++ )
                {
                    for ( int x = 0; x < dataSizeX; x++ )
                    {
                        buffer.put( dataf[x][y] );
                    }
                }
            }
            else
            {
                if ( dataf.length != dataSizeY || dataf[0].length != dataSizeX )
                {
                    logWarning( logger, "Incorrectly sized data array passed to SetDataMutator. Expected %d %d. Got %d %d.", dataSizeX, dataSizeY, dataf[0].length, dataf.length );
                    return;
                }

                for ( int y = 0; y < dataSizeY; y++ )
                {
                    for ( int x = 0; x < dataSizeX; x++ )
                    {
                        buffer.put( dataf[y][x] );
                    }
                }
            }
        }

        public void mutated( FloatBuffer buffer, int dataSizeX, int dataSizeY )
        {
            if ( datad == null )
            {
                logger.warning( "Null data array passed to SetDataMutator." );
                return;
            }

            buffer.clear( );

            if ( !flip )
            {
                if ( datad.length != dataSizeX || datad[0].length != dataSizeY )
                {
                    logWarning( logger, "Incorrectly sized data array passed to SetDataMutator. Expected %d %d. Got %d %d.", dataSizeX, dataSizeY, datad.length, datad[0].length );
                    return;
                }

                for ( int y = 0; y < dataSizeY; y++ )
                {
                    for ( int x = 0; x < dataSizeX; x++ )
                    {
                        buffer.put( ( float ) datad[x][y] );
                    }
                }
            }
            else
            {
                if ( datad.length != dataSizeY || datad[0].length != dataSizeX )
                {
                    logWarning( logger, "Incorrectly sized data array passed to SetDataMutator. Expected %d %d. Got %d %d.", dataSizeX, dataSizeY, datad[0].length, dataf.length );
                    return;
                }

                for ( int y = 0; y < dataSizeY; y++ )
                {
                    for ( int x = 0; x < dataSizeX; x++ )
                    {
                        buffer.put( ( float ) datad[y][x] );
                    }
                }
            }
        }
    }
}

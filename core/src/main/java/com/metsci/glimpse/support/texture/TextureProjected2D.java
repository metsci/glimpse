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

import static com.metsci.glimpse.gl.util.GLUtils.getGLTextureDim;
import static com.metsci.glimpse.gl.util.GLUtils.getGLTextureUnit;
import static java.util.logging.Level.WARNING;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLContext;

import com.jogamp.common.nio.Buffers;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.DrawableTexture;
import com.metsci.glimpse.gl.texture.DrawableTextureProgram;
import com.metsci.glimpse.gl.texture.Texture;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.texture.TextureUnit;
import com.metsci.glimpse.support.projection.InvertibleProjection;
import com.metsci.glimpse.support.projection.Projection;

public abstract class TextureProjected2D implements DrawableTexture
{
    public static final int NUM_DIMENSIONS = 2;
    public static final int VERTICES_PER_QUAD = 6; // quads are made of two triangles
    public static final int BYTES_PER_FLOAT = 4;

    private static final Logger logger = Logger.getLogger( TextureProjected2D.class.getName( ) );

    // projection defining the mapping from texel (texture index) to vertex coordinate
    protected Projection projection;

    // buffer to store texture data
    protected ByteBuffer data;

    // buffer to store vertex and texture coordinate data
    protected FloatBuffer coordBuffer;

    // whether to compute and use a z coordinate for each vertex
    protected boolean useVertexZCoord;

    // 2 or 3, depending on useVertexZCoord
    protected int floatsPerVertex;

    // the number of physical OpenGL textures that this logical texture
    // was split into (because of maximum texture size limits)
    protected int numTextures;
    protected int textureCountX;
    protected int textureCountY;
    protected int maxTextureSize;

    // OpenGL texture data handles (length numTextures)
    protected int[] textureHandles;
    // OpenGL vertex coordinate buffer handles (length numTextures)
    protected int[] vertexCoordHandles;
    // OpenGL texture coordinate buffer handles (length numTextures)
    protected int[] texCoordHandles;

    // the X index into data of the bottom left corner of the ith texture
    protected int[] texStartsX;
    // the Y index into data of the bottom left corner of the ith texture
    protected int[] texStartsY;

    // the X size of the data for the ith texture
    protected int[] texSizesX;
    // the Y size of the data for the ith texture
    protected int[] texSizesY;

    // the total number of quads to draw for the ith texture
    protected int[] texQuadCounts;

    protected ReentrantLock lock = new ReentrantLock( );

    protected boolean glAllocated;

    protected boolean dirty;

    protected boolean projectionDirty;

    protected int dataSizeX;
    protected int dataSizeY;

    public TextureProjected2D( int dataSizeX, int dataSizeY, boolean useVertexZCoord )
    {
        this.useVertexZCoord = useVertexZCoord;
        this.floatsPerVertex = ( useVertexZCoord ? 3 : 2 );

        this.dataSizeX = dataSizeX;
        this.dataSizeY = dataSizeY;
        this.data = newByteBuffer( );
    }

    protected abstract void prepare_setData( GL gl );

    protected abstract int getRequiredCapacityBytes( );

    protected abstract float getData( int index );

    public double getDataValue( double coordX, double coordY )
    {
        lock.lock( );
        try
        {
            Projection projection = getProjection( );

            if ( projection == null ) return 0.0;

            if ( projection instanceof InvertibleProjection )
            {
                InvertibleProjection invProjection = ( InvertibleProjection ) projection;

                double fracX = invProjection.getTextureFractionX( coordX, coordY );
                double fracY = invProjection.getTextureFractionY( coordX, coordY );

                int x = ( int ) Math.floor( fracX * dataSizeX );
                int y = ( int ) Math.floor( fracY * dataSizeY );

                return getDataValue( x, y );
            }
        }
        finally
        {
            lock.unlock( );
        }

        return 0.0;
    }

    public float getDataValue( int indexX, int indexY )
    {
        lock.lock( );
        try
        {
            if ( indexX < 0 || indexY < 0 || indexX >= dataSizeX || indexY >= dataSizeY ) return 0.0f;

            return getData( indexY * dataSizeX + indexX );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void makeProjectionDirty( )
    {
        this.projectionDirty = true;
    }

    @Override
    public int[] getHandles( )
    {
        return textureHandles;
    }

    @Override
    public void makeDirty( )
    {
        this.dirty = true;
    }

    @Override
    public boolean isDirty( )
    {
        return dirty || projectionDirty;
    }

    @Override
    public int getNumDimension( )
    {
        return NUM_DIMENSIONS;
    }

    @Override
    public int getDimensionSize( int n )
    {
        switch ( n )
        {
            case 0:
                return dataSizeX;
            case 1:
                return dataSizeY;
            default:
                return 0;
        }
    }

    @Override
    public boolean prepare( GlimpseContext context, int texUnit )
    {
        GL gl = context.getGL( );

        // should we check for dirtiness and allocation before lock to speed up?
        lock.lock( );
        try
        {
            if ( !glAllocated )
            {
                allocate_calcSizes( gl );
                allocate_genTextureHandles( gl );
                allocate_genBuffers( gl );
            }

            prepare_glState( gl );

            if ( glAllocated && dirty )
            {
                prepare_setData( gl );
                dirty = false;
            }

            if ( glAllocated && projectionDirty )
            {
                prepare_setCoords( gl );
                projectionDirty = false;

            }

            return !isDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void draw( GlimpseContext context, DrawableTextureProgram program, int texUnit )
    {
        draw( context, program, texUnit, Collections.<TextureUnit<Texture>> emptyList( ) );
    }

    @Override
    public void draw( GlimpseContext context, DrawableTextureProgram program, int texUnit, Collection<TextureUnit<Texture>> multiTextureList )
    {
        // prepare our texture
        boolean ready = prepare( context, texUnit );
        if ( !ready )
        {
            logger.log( WARNING, "Unable to make ready." );
            return;
        }

        // prepare all the multitextures
        for ( TextureUnit<Texture> texture : multiTextureList )
        {
            ready = texture.prepare( context );

            if ( !ready )
            {
                logger.log( WARNING, "Unable to make ready." );
                return;
            }
        }

        Axis2D axis = GlimpsePainterBase.requireAxis2D( context );
        GL3 gl = GlimpsePainterBase.getGL3( context );

        program.begin( context, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
        try
        {
            for ( int i = 0; i < numTextures; i++ )
            {
                // this TexturProjected2D defines the quad bounds to draw, but when
                // multitexturing we also need to bind the others textures which
                // the shader will access
                for ( TextureUnit<Texture> multiTexture : multiTextureList )
                {
                    Texture texture = multiTexture.getTexture( );
                    int[] handles = texture.getHandles( );
                    int multiTextureUnit = multiTexture.getTextureUnit( );
                    int type = getGLTextureDim( texture.getNumDimension( ) );

                    gl.glActiveTexture( getGLTextureUnit( multiTextureUnit ) );

                    // there are two common cases which this is intended to handle:
                    // 1) the multitexture is a small texture like a colormap which should be the same for each part of the 2D grid texture
                    // 2) the multitexture is a second 2D grid with the same structure as the first
                    //
                    // this doesn't attempt to catch cases where the two 2D grids don't line up -- undefined behavior will result in this case
                    gl.glBindTexture( type, texture.getHandles( )[i >= handles.length ? 0 : i] );
                }

                int type = getTextureType( );

                gl.glActiveTexture( getGLTextureUnit( texUnit ) );
                gl.glBindTexture( type, textureHandles[i] );

                int vertexCount = VERTICES_PER_QUAD * texQuadCounts[i];

                program.draw( context, GL.GL_TRIANGLES, vertexCoordHandles[i], texCoordHandles[i], 0, vertexCount );
            }
        }
        finally
        {
            program.end( context );
        }
    }

    @Override
    public void dispose( GLContext context )
    {
        GL gl = context.getGL( );

        if ( textureHandles != null && textureHandles.length != 0 )
        {
            gl.glDeleteTextures( textureHandles.length, textureHandles, 0 );
        }

        if ( vertexCoordHandles != null && vertexCoordHandles.length != 0 )
        {
            gl.glDeleteBuffers( vertexCoordHandles.length, vertexCoordHandles, 0 );
        }

        if ( texCoordHandles != null && texCoordHandles.length != 0 )
        {
            gl.glDeleteBuffers( texCoordHandles.length, texCoordHandles, 0 );
        }
    }

    protected int getTextureType( )
    {
        return getGLTextureDim( NUM_DIMENSIONS );
    }

    protected void prepare_glState( GL gl )
    {
        // generally does nothing
        // kept for backwards compatibility for Glimpse-World Wind support
        // which still relies on GL2 functionality
    }

    protected void allocate_calcSizes( GL gl )
    {
        maxTextureSize = getMaxGLTextureSize( gl );

        textureCountX = dataSizeX / maxTextureSize;
        textureCountY = dataSizeY / maxTextureSize;

        if ( dataSizeX % maxTextureSize != 0 ) textureCountX++;
        if ( dataSizeY % maxTextureSize != 0 ) textureCountY++;

        numTextures = textureCountX * textureCountY;
    }

    protected void allocate_genTextureHandles( GL gl )
    {
        if ( textureHandles != null && textureHandles.length != 0 )
        {
            gl.glDeleteTextures( textureHandles.length, textureHandles, 0 );
        }

        if ( numTextures == 0 || projection == null ) return;

        textureHandles = new int[numTextures];
        gl.glGenTextures( numTextures, textureHandles, 0 );

    }

    protected void allocate_genBuffers( GL gl )
    {
        if ( vertexCoordHandles != null && vertexCoordHandles.length != 0 )
        {
            gl.glDeleteBuffers( vertexCoordHandles.length, vertexCoordHandles, 0 );
        }

        if ( texCoordHandles != null && texCoordHandles.length != 0 )
        {
            gl.glDeleteBuffers( texCoordHandles.length, texCoordHandles, 0 );
        }

        if ( numTextures == 0 || projection == null ) return;

        vertexCoordHandles = new int[numTextures];
        gl.glGenBuffers( numTextures, vertexCoordHandles, 0 );

        texCoordHandles = new int[numTextures];
        gl.glGenBuffers( numTextures, texCoordHandles, 0 );

        texStartsX = new int[numTextures];
        texStartsY = new int[numTextures];
        texSizesX = new int[numTextures];
        texSizesY = new int[numTextures];
        texQuadCounts = new int[numTextures];

        int index = 0;
        for ( int x = 0; x < textureCountX; x++ )
        {
            for ( int y = 0; y < textureCountY; y++ )
            {
                int startX = x * maxTextureSize;
                int startY = y * maxTextureSize;
                int endX = Math.min( startX + maxTextureSize, dataSizeX );
                int endY = Math.min( startY + maxTextureSize, dataSizeY );
                int sizeX = endX - startX;
                int sizeY = endY - startY;

                texStartsX[index] = startX;
                texStartsY[index] = startY;

                texSizesX[index] = sizeX;
                texSizesY[index] = sizeY;

                texQuadCounts[index] = getQuadCountForTexture( index, startX, startY, sizeX, sizeY );

                index++;
            }
        }

        glAllocated = true;

        makeDirty( );
        makeProjectionDirty( );
    }

    public static int getMaxGLTextureSize( GL gl )
    {
        int[] result = new int[1];
        gl.glGetIntegerv( GL2.GL_MAX_TEXTURE_SIZE, result, 0 );
        return result[0];
    }

    protected int getQuadCountForTexture( int texIndex, int texStartX, int texStartY, int texSizeX, int texSizeY )
    {
        int quadCountX = projection.getSizeX( texSizeX );
        int quadCountY = projection.getSizeY( texSizeY );
        return quadCountX * quadCountY;
    }

    protected void prepare_setCoords( GL gl )
    {
        float[] temp = new float[floatsPerVertex];

        for ( int i = 0; i < numTextures; i++ )
        {
            int projectFloats = texQuadCounts[i] * VERTICES_PER_QUAD * floatsPerVertex;
            if ( coordBuffer == null || coordBuffer.capacity( ) < projectFloats ) coordBuffer = Buffers.newDirectFloatBuffer( projectFloats );

            coordBuffer.rewind( );
            putVerticesCoords( i, texStartsX[i], texStartsY[i], texSizesX[i], texSizesY[i], temp );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, vertexCoordHandles[i] );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, projectFloats * BYTES_PER_FLOAT, coordBuffer.rewind( ), GL2.GL_STATIC_DRAW );

            coordBuffer.rewind( );
            putVerticesTexCoords( i, texStartsX[i], texStartsY[i], texSizesX[i], texSizesY[i] );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, texCoordHandles[i] );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, projectFloats * BYTES_PER_FLOAT, coordBuffer.rewind( ), GL2.GL_STATIC_DRAW );
        }
    }

    protected void putVerticesCoords( int texIndex, int texStartX, int texStartY, int texSizeX, int texSizeY, float[] temp )
    {
        int quadCountX = projection.getSizeX( texSizeX );
        int quadCountY = projection.getSizeY( texSizeY );

        for ( int x = 0; x < quadCountX; x++ )
        {
            double texFracX0 = x / ( double ) quadCountX;
            double texFracX1 = ( x + 1 ) / ( double ) quadCountX;

            for ( int y = 0; y < quadCountY; y++ )
            {
                double texFracY0 = y / ( double ) quadCountY;
                double texFracY1 = ( y + 1 ) / ( double ) quadCountY;

                // first triangle
                putVertexCoords( texIndex, texFracX0, texFracY0, temp );
                putVertexCoords( texIndex, texFracX1, texFracY0, temp );
                putVertexCoords( texIndex, texFracX1, texFracY1, temp );

                // second triangle
                putVertexCoords( texIndex, texFracX0, texFracY0, temp );
                putVertexCoords( texIndex, texFracX1, texFracY1, temp );
                putVertexCoords( texIndex, texFracX0, texFracY1, temp );
            }
        }
    }

    protected void putVertexCoords( int texIndex, double texFracX, double texFracY, float[] temp )
    {
        double dataFracX = ( texStartsX[texIndex] + texSizesX[texIndex] * texFracX ) / dataSizeX;
        double dataFracY = ( texStartsY[texIndex] + texSizesY[texIndex] * texFracY ) / dataSizeY;

        if ( useVertexZCoord )
        {
            projection.getVertexXYZ( dataFracX, dataFracY, temp );
            coordBuffer.put( temp[0] ).put( temp[1] ).put( temp[2] );
        }
        else
        {
            projection.getVertexXY( dataFracX, dataFracY, temp );
            coordBuffer.put( temp[0] ).put( temp[1] );
        }
    }

    protected void putVerticesTexCoords( int texIndex, int texStartX, int texStartY, int texSizeX, int texSizeY )
    {
        int quadCountX = projection.getSizeX( texSizeX );
        int quadCountY = projection.getSizeY( texSizeY );

        for ( int x = 0; x < quadCountX; x++ )
        {
            double texFracX0 = x / ( double ) quadCountX;
            double texFracX1 = ( x + 1 ) / ( double ) quadCountX;

            for ( int y = 0; y < quadCountY; y++ )
            {
                double texFracY0 = y / ( double ) quadCountY;
                double texFracY1 = ( y + 1 ) / ( double ) quadCountY;

                // first triangle
                putVertexTexCoords( texIndex, texFracX0, texFracY0 );
                putVertexTexCoords( texIndex, texFracX1, texFracY0 );
                putVertexTexCoords( texIndex, texFracX1, texFracY1 );

                // second triangle
                putVertexTexCoords( texIndex, texFracX0, texFracY0 );
                putVertexTexCoords( texIndex, texFracX1, texFracY1 );
                putVertexTexCoords( texIndex, texFracX0, texFracY1 );
            }
        }
    }

    protected void putVertexTexCoords( int texIndex, double texFracX, double texFracY )
    {
        coordBuffer.put( ( float ) texFracX ).put( ( float ) texFracY );
    }

    protected void prepare_setTexParameters( GL gl )
    {
        gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST );
        gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST );

        gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP );
        gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP );
    }

    protected ByteBuffer newByteBuffer( )
    {
        return Buffers.newDirectByteBuffer( getRequiredCapacityBytes( ) );
    }

    public boolean isResident( GL2 gl )
    {
        lock.lock( );
        try
        {
            if ( !glAllocated ) return false;

            byte[] resident = new byte[textureHandles.length];
            gl.glAreTexturesResident( 1, textureHandles, 0, resident, 0 );

            for ( int i = 0; i < resident.length; i++ )
            {
                if ( resident[i] <= 0 ) return false;
            }

            return true;
        }
        finally
        {
            lock.unlock( );
        }
    }

    /**
     * Resizes this two dimensional texture to the given new size. This deallocates
     * any data stored on the graphics card and dirties the texture.
     *
     * If the texture size has been made larger, setData( ) or mutate( ) should be
     * used to provide data for the new larger sections of the data. The dimensions
     * of the data array argument to set data should be float[dataSizeX][dataSizeY].
     *
     * @param dataSizeX the number of texture elements in the 1st, or x, dimension
     * @param dataSizeY the number of texture elements in the 2nd, or y, dimension
     */
    public void resize( int dataSizeX, int dataSizeY )
    {
        lock.lock( );
        try
        {
            this.dataSizeX = dataSizeX;
            this.dataSizeY = dataSizeY;

            this.glAllocated = false;

            if ( this.data == null || this.data.capacity( ) < getRequiredCapacityBytes( ) ) this.data = newByteBuffer( );

            makeDirty( );
            makeProjectionDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setProjection( Projection projection )
    {
        lock.lock( );
        try
        {
            this.projection = projection;
            makeProjectionDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public Projection getProjection( )
    {
        lock.lock( );
        try
        {
            return this.projection;
        }
        finally
        {
            lock.unlock( );
        }
    }
}

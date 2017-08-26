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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLContext;

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
        this.data = this.newByteBuffer( );
    }

    protected abstract void prepare_setData( GL gl );

    protected abstract int getRequiredCapacityBytes( );

    protected abstract float getData( int index );

    public double getDataValue( double coordX, double coordY )
    {
        this.lock.lock( );
        try
        {
            Projection projection = this.getProjection( );

            if ( projection == null ) return 0.0;

            if ( projection instanceof InvertibleProjection )
            {
                InvertibleProjection invProjection = ( InvertibleProjection ) projection;

                double fracX = invProjection.getTextureFractionX( coordX, coordY );
                double fracY = invProjection.getTextureFractionY( coordX, coordY );

                int x = ( int ) Math.floor( fracX * this.dataSizeX );
                int y = ( int ) Math.floor( fracY * this.dataSizeY );

                return this.getDataValue( x, y );
            }
        }
        finally
        {
            this.lock.unlock( );
        }

        return 0.0;
    }

    public float getDataValue( int indexX, int indexY )
    {
        this.lock.lock( );
        try
        {
            if ( indexX < 0 || indexY < 0 || indexX >= this.dataSizeX || indexY >= this.dataSizeY ) return 0.0f;

            return this.getData( indexY * this.dataSizeX + indexX );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void makeProjectionDirty( )
    {
        this.projectionDirty = true;
    }

    @Override
    public int[] getHandles( )
    {
        return this.textureHandles;
    }

    @Override
    public void makeDirty( )
    {
        this.dirty = true;
    }

    @Override
    public boolean isDirty( )
    {
        return this.dirty || this.projectionDirty;
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
                return this.dataSizeX;
            case 1:
                return this.dataSizeY;
            default:
                return 0;
        }
    }

    @Override
    public boolean prepare( GlimpseContext context, int texUnit )
    {
        GL gl = context.getGL( );

        // should we check for dirtiness and allocation before lock to speed up?
        this.lock.lock( );
        try
        {
            if ( !this.glAllocated )
            {
                this.allocate_calcSizes( gl );
                this.allocate_genTextureHandles( gl );
                this.allocate_genBuffers( gl );
            }

            this.prepare_glState( gl );

            if ( this.glAllocated && this.dirty )
            {
                this.prepare_setData( gl );
                this.dirty = false;
            }

            if ( this.glAllocated && this.projectionDirty )
            {
                this.prepare_setCoords( gl );
                this.projectionDirty = false;

            }

            return !this.isDirty( );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void draw( GlimpseContext context, DrawableTextureProgram program, int texUnit )
    {
        this.draw( context, program, texUnit, Collections.<TextureUnit<Texture>> emptyList( ) );
    }

    @Override
    public void draw( GlimpseContext context, DrawableTextureProgram program, int texUnit, Collection<TextureUnit<Texture>> multiTextureList )
    {
        // prepare our texture
        boolean ready = this.prepare( context, texUnit );
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
            for ( int i = 0; i < this.numTextures; i++ )
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

                int type = this.getTextureType( );

                gl.glActiveTexture( getGLTextureUnit( texUnit ) );
                gl.glBindTexture( type, this.textureHandles[i] );

                int vertexCount = VERTICES_PER_QUAD * this.texQuadCounts[i];

                program.draw( context, GL.GL_TRIANGLES, this.vertexCoordHandles[i], this.texCoordHandles[i], 0, vertexCount );
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

        if ( this.textureHandles != null && this.textureHandles.length != 0 )
        {
            gl.glDeleteTextures( this.textureHandles.length, this.textureHandles, 0 );
        }

        if ( this.vertexCoordHandles != null && this.vertexCoordHandles.length != 0 )
        {
            gl.glDeleteBuffers( this.vertexCoordHandles.length, this.vertexCoordHandles, 0 );
        }

        if ( this.texCoordHandles != null && this.texCoordHandles.length != 0 )
        {
            gl.glDeleteBuffers( this.texCoordHandles.length, this.texCoordHandles, 0 );
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
        this.maxTextureSize = getMaxGLTextureSize( gl );

        this.textureCountX = this.dataSizeX / this.maxTextureSize;
        this.textureCountY = this.dataSizeY / this.maxTextureSize;

        if ( this.dataSizeX % this.maxTextureSize != 0 ) this.textureCountX++;
        if ( this.dataSizeY % this.maxTextureSize != 0 ) this.textureCountY++;

        this.numTextures = this.textureCountX * this.textureCountY;
    }

    protected void allocate_genTextureHandles( GL gl )
    {
        if ( this.textureHandles != null && this.textureHandles.length != 0 )
        {
            gl.glDeleteTextures( this.textureHandles.length, this.textureHandles, 0 );
        }

        if ( this.numTextures == 0 || this.projection == null ) return;

        this.textureHandles = new int[this.numTextures];
        gl.glGenTextures( this.numTextures, this.textureHandles, 0 );

    }

    protected void allocate_genBuffers( GL gl )
    {
        if ( this.vertexCoordHandles != null && this.vertexCoordHandles.length != 0 )
        {
            gl.glDeleteBuffers( this.vertexCoordHandles.length, this.vertexCoordHandles, 0 );
        }

        if ( this.texCoordHandles != null && this.texCoordHandles.length != 0 )
        {
            gl.glDeleteBuffers( this.texCoordHandles.length, this.texCoordHandles, 0 );
        }

        if ( this.numTextures == 0 || this.projection == null ) return;

        this.vertexCoordHandles = new int[this.numTextures];
        gl.glGenBuffers( this.numTextures, this.vertexCoordHandles, 0 );

        this.texCoordHandles = new int[this.numTextures];
        gl.glGenBuffers( this.numTextures, this.texCoordHandles, 0 );

        this.texStartsX = new int[this.numTextures];
        this.texStartsY = new int[this.numTextures];
        this.texSizesX = new int[this.numTextures];
        this.texSizesY = new int[this.numTextures];
        this.texQuadCounts = new int[this.numTextures];

        int index = 0;
        for ( int x = 0; x < this.textureCountX; x++ )
        {
            for ( int y = 0; y < this.textureCountY; y++ )
            {
                int startX = x * this.maxTextureSize;
                int startY = y * this.maxTextureSize;
                int endX = Math.min( startX + this.maxTextureSize, this.dataSizeX );
                int endY = Math.min( startY + this.maxTextureSize, this.dataSizeY );
                int sizeX = endX - startX;
                int sizeY = endY - startY;

                this.texStartsX[index] = startX;
                this.texStartsY[index] = startY;

                this.texSizesX[index] = sizeX;
                this.texSizesY[index] = sizeY;

                this.texQuadCounts[index] = this.getQuadCountForTexture( index, startX, startY, sizeX, sizeY );

                index++;
            }
        }

        this.glAllocated = true;

        this.makeDirty( );
        this.makeProjectionDirty( );
    }

    public static int getMaxGLTextureSize( GL gl )
    {
        int[] result = new int[1];
        gl.glGetIntegerv( GL2.GL_MAX_TEXTURE_SIZE, result, 0 );
        return result[0];
    }

    protected int getQuadCountForTexture( int texIndex, int texStartX, int texStartY, int texSizeX, int texSizeY )
    {
        int quadCountX = this.projection.getSizeX( texSizeX );
        int quadCountY = this.projection.getSizeY( texSizeY );
        return quadCountX * quadCountY;
    }

    protected void prepare_setCoords( GL gl )
    {
        float[] temp = new float[this.floatsPerVertex];

        for ( int i = 0; i < this.numTextures; i++ )
        {
            int projectFloats = this.texQuadCounts[i] * VERTICES_PER_QUAD * this.floatsPerVertex;
            if ( this.coordBuffer == null || this.coordBuffer.capacity( ) < projectFloats ) this.coordBuffer = Buffers.newDirectFloatBuffer( projectFloats );

            this.coordBuffer.rewind( );
            this.putVerticesCoords( i, this.texStartsX[i], this.texStartsY[i], this.texSizesX[i], this.texSizesY[i], temp );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, this.vertexCoordHandles[i] );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, projectFloats * BYTES_PER_FLOAT, this.coordBuffer.rewind( ), GL2.GL_STATIC_DRAW );

            this.coordBuffer.rewind( );
            this.putVerticesTexCoords( i, this.texStartsX[i], this.texStartsY[i], this.texSizesX[i], this.texSizesY[i] );
            gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, this.texCoordHandles[i] );
            gl.glBufferData( GL2.GL_ARRAY_BUFFER, projectFloats * BYTES_PER_FLOAT, this.coordBuffer.rewind( ), GL2.GL_STATIC_DRAW );
        }
    }

    protected void putVerticesCoords( int texIndex, int texStartX, int texStartY, int texSizeX, int texSizeY, float[] temp )
    {
        int quadCountX = this.projection.getSizeX( texSizeX );
        int quadCountY = this.projection.getSizeY( texSizeY );

        for ( int x = 0; x < quadCountX; x++ )
        {
            double texFracX0 = x / ( double ) quadCountX;
            double texFracX1 = ( x + 1 ) / ( double ) quadCountX;

            for ( int y = 0; y < quadCountY; y++ )
            {
                double texFracY0 = y / ( double ) quadCountY;
                double texFracY1 = ( y + 1 ) / ( double ) quadCountY;

                // first triangle
                this.putVertexCoords( texIndex, texFracX0, texFracY0, temp );
                this.putVertexCoords( texIndex, texFracX1, texFracY0, temp );
                this.putVertexCoords( texIndex, texFracX1, texFracY1, temp );

                // second triangle
                this.putVertexCoords( texIndex, texFracX0, texFracY0, temp );
                this.putVertexCoords( texIndex, texFracX1, texFracY1, temp );
                this.putVertexCoords( texIndex, texFracX0, texFracY1, temp );
            }
        }
    }

    protected void putVertexCoords( int texIndex, double texFracX, double texFracY, float[] temp )
    {
        double dataFracX = ( this.texStartsX[texIndex] + this.texSizesX[texIndex] * texFracX ) / this.dataSizeX;
        double dataFracY = ( this.texStartsY[texIndex] + this.texSizesY[texIndex] * texFracY ) / this.dataSizeY;

        if ( this.useVertexZCoord )
        {
            this.projection.getVertexXYZ( dataFracX, dataFracY, temp );
            this.coordBuffer.put( temp[0] ).put( temp[1] ).put( temp[2] );
        }
        else
        {
            this.projection.getVertexXY( dataFracX, dataFracY, temp );
            this.coordBuffer.put( temp[0] ).put( temp[1] );
        }
    }

    protected void putVerticesTexCoords( int texIndex, int texStartX, int texStartY, int texSizeX, int texSizeY )
    {
        int quadCountX = this.projection.getSizeX( texSizeX );
        int quadCountY = this.projection.getSizeY( texSizeY );

        for ( int x = 0; x < quadCountX; x++ )
        {
            double texFracX0 = x / ( double ) quadCountX;
            double texFracX1 = ( x + 1 ) / ( double ) quadCountX;

            for ( int y = 0; y < quadCountY; y++ )
            {
                double texFracY0 = y / ( double ) quadCountY;
                double texFracY1 = ( y + 1 ) / ( double ) quadCountY;

                // first triangle
                this.putVertexTexCoords( texIndex, texFracX0, texFracY0 );
                this.putVertexTexCoords( texIndex, texFracX1, texFracY0 );
                this.putVertexTexCoords( texIndex, texFracX1, texFracY1 );

                // second triangle
                this.putVertexTexCoords( texIndex, texFracX0, texFracY0 );
                this.putVertexTexCoords( texIndex, texFracX1, texFracY1 );
                this.putVertexTexCoords( texIndex, texFracX0, texFracY1 );
            }
        }
    }

    protected void putVertexTexCoords( int texIndex, double texFracX, double texFracY )
    {
        this.coordBuffer.put( ( float ) texFracX ).put( ( float ) texFracY );
    }

    protected void prepare_setTexParameters( GL gl )
    {
        GL3 gl3 = gl.getGL3( );
        
        gl3.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST );
        gl3.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST );

        gl3.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE );
        gl3.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE );
    }

    protected ByteBuffer newByteBuffer( )
    {
        return Buffers.newDirectByteBuffer( this.getRequiredCapacityBytes( ) );
    }

    public boolean isResident( GL2 gl )
    {
        this.lock.lock( );
        try
        {
            if ( !this.glAllocated ) return false;

            byte[] resident = new byte[this.textureHandles.length];
            gl.glAreTexturesResident( 1, this.textureHandles, 0, resident, 0 );

            for ( int i = 0; i < resident.length; i++ )
            {
                if ( resident[i] <= 0 ) return false;
            }

            return true;
        }
        finally
        {
            this.lock.unlock( );
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
        this.lock.lock( );
        try
        {
            this.dataSizeX = dataSizeX;
            this.dataSizeY = dataSizeY;

            this.glAllocated = false;

            if ( this.data == null || this.data.capacity( ) < this.getRequiredCapacityBytes( ) ) this.data = this.newByteBuffer( );

            this.makeDirty( );
            this.makeProjectionDirty( );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void setProjection( Projection projection )
    {
        this.lock.lock( );
        try
        {
            this.projection = projection;
            this.makeProjectionDirty( );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public Projection getProjection( )
    {
        this.lock.lock( );
        try
        {
            return this.projection;
        }
        finally
        {
            this.lock.unlock( );
        }
    }
}

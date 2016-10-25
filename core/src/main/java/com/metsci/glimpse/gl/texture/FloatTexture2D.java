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
package com.metsci.glimpse.gl.texture;

import static java.util.logging.Level.*;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.texture.TextureUnit;

/**
 * A two dimensional texture storing float values.
 *
 * @author osborn
 */
public class FloatTexture2D extends AbstractTexture implements DrawableTexture
{
    private static final Logger logger = Logger.getLogger( FloatTexture2D.class.getName( ) );

    protected FloatBuffer data;
    protected GLEditableBuffer xyBuffer;
    protected GLEditableBuffer sBuffer;

    protected double[] min;
    protected double[] max;

    public FloatTexture2D( double[] min, double[] max, int n0, int n1, boolean centers )
    {
        super( n0, n1 );

        this.data = Buffers.newDirectFloatBuffer( n0 * n1 );

        this.min = Arrays.copyOf( min, min.length );
        this.max = Arrays.copyOf( max, max.length );

        double tminmax[][] = computeDrawMinMax( min, max, centers );
        double tmin[] = tminmax[0];
        double tmax[] = tminmax[1];

        this.xyBuffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 12 * Buffers.SIZEOF_FLOAT );
        this.xyBuffer.growQuad2f( ( float ) tmin[0], ( float ) tmin[1], ( float ) tmax[0], ( float ) tmax[1] );

        this.sBuffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 12 * Buffers.SIZEOF_FLOAT );
        this.sBuffer.growQuad2f( 0, 0, 1, 1 );
    }

    public int xyVbo( GlimpseContext context )
    {
        GL gl = context.getGL( );
        return this.xyBuffer.deviceBuffer( gl );
    }

    public int sVbo( GlimpseContext context )
    {
        GL gl = context.getGL( );
        return this.sBuffer.deviceBuffer( gl );
    }

    public int getMode( )
    {
        return GL.GL_TRIANGLES;
    }

    @Override
    public void draw( GlimpseContext context, DrawableTextureProgram program, int texUnit, Collection<TextureUnit<Texture>> multiTextureList )
    {
        boolean ready = true;

        // prepare all the multitextures
        for ( TextureUnit<Texture> texture : multiTextureList )
        {
            if ( !texture.prepare( context ) ) ready = false;
        }

        if ( !prepare( context, texUnit ) ) ready = false;

        if ( !ready )
        {
            logger.log( WARNING, "Unable to make ready." );

            return;
        }

        Axis2D axis = GlimpsePainterBase.requireAxis2D( context );

        program.begin( context, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
        try
        {
            program.draw( context, getMode( ), xyVbo( context ), sVbo( context ), 0, this.xyBuffer.sizeFloats( ) / 2 );
        }
        finally
        {
            program.end( context );
        }
    }

    private final double[][] computeDrawMinMax( double[] min, double[] max, boolean centers )
    {
        double[] texmin = new double[2];
        double[] texmax = new double[2];
        double d0 = ( max[0] - min[0] ) / dim[0];
        double d1 = ( max[1] - min[1] ) / dim[1];

        if ( !centers )
        {
            texmin[0] = min[0];
            texmin[1] = min[1];
            texmax[0] = max[0];
            texmax[1] = max[0];
        }
        else
        {
            texmin[0] = min[0] - d0 / 2;
            texmin[1] = min[1] - d1 / 2;
            texmax[0] = max[0] + d0 / 2;
            texmax[1] = max[0] + d1 / 2;
        }

        return new double[][] { texmin, texmax };
    }

    @Override
    protected void prepare_setTexParameters( GL gl )
    {
        gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST );
        gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST );

        gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE );
        gl.glTexParameteri( GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE );
    }

    @Override
    protected void prepare_setData( GL gl )
    {
        gl.glTexImage2D( GL3.GL_TEXTURE_2D, 0, GL3.GL_R32F, dim[0], dim[1], 0, GL3.GL_RED, GL3.GL_FLOAT, data.rewind( ) );
    }

    @Override
    protected void prepare_setPixelStore( GL gl )
    {
        gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 1 );
    }

    public void mutate( MutatorFloat2D mutator )
    {
        lock.lock( );
        try
        {
            data.rewind( );
            mutator.mutate( data, min, max, dim.clone( ) );
            makeDirty( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public static interface MutatorFloat2D
    {
        public void mutate( FloatBuffer data, double[] min, double[] max, int[] dim );
    }
}

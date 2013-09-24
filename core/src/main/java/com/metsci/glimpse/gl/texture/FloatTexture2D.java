/*
 * Copyright (c) 2012, Metron, Inc.
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

import static java.util.logging.Level.WARNING;

import java.nio.FloatBuffer;
import java.util.logging.Logger;

import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;

/**
 * A two dimensional texture storing float values.
 *
 * @author osborn
 */
public class FloatTexture2D extends AbstractTexture implements DrawableTexture
{
    private static final Logger logger = Logger.getLogger( FloatTexture2D.class.getName() );

    protected FloatBuffer data;
    protected boolean centers;
    protected double[] min;
    protected double[] max;

    public FloatTexture2D( double[] min, double[] max, int n0, int n1, boolean centers )
    {
        super( n0, n1 );

        this.data = BufferUtil.newFloatBuffer( n0 * n1 );

        this.min = min.clone();
        this.max = max.clone();
        this.centers = centers;
    }

    @Override
    public void draw( GL gl, int texUnit )
    {
        boolean ready = prepare( gl, texUnit );

        if( !ready )
        {
            logger.log( WARNING, "Unable to make ready." );
            return;
        }

        gl.glTexEnvf( GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE );
        gl.glPolygonMode( GL.GL_FRONT, GL.GL_FILL );

        double tminmax[][] = computeDrawMinMax();
        double tmin[] = tminmax[0];
        double tmax[] = tminmax[1];

        gl.glBegin( GL.GL_QUADS );
        {
            gl.glTexCoord2f( 0.0f, 0.0f );
            gl.glVertex2d( tmin[0], tmin[1] );

            gl.glTexCoord2f( 1.0f, 0.0f );
            gl.glVertex2d( tmax[0], tmin[1] );

            gl.glTexCoord2f( 1.0f, 1.0f );
            gl.glVertex2d( tmax[0], tmax[1] );

            gl.glTexCoord2f( 0.0f, 1.0f );
            gl.glVertex2d( tmin[0], tmax[1] );
        }
        gl.glEnd();
    }

    private final double[][] computeDrawMinMax( )
    {
        double[] texmin = new double[2];
        double[] texmax = new double[2];
        double d0 = ( max[0] - min[0] ) / dim[0];
        double d1 = ( max[1] - min[1] ) / dim[1];

        if( !centers )
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
        gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST );
        gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST );

        gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP );
        gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP );
    }

    @Override
    protected void prepare_setData( GL gl )
    {
        gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_LUMINANCE32F_ARB, dim[0], dim[1], 0, GL.GL_LUMINANCE, GL.GL_FLOAT,
                         data.rewind() );
    }

    @Override
    protected void prepare_setPixelStore( GL gl )
    {
        gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 1 );
    }


    public void mutate( MutatorFloat2D mutator )
    {
        lock.lock();
        try
        {
            data.rewind();
            mutator.mutate( data, min, max, dim.clone() );
            makeDirty();
        }
        finally
        {
            lock.unlock();
        }
    }

    public static interface MutatorFloat2D
    {
        public void mutate( FloatBuffer data, double[] min, double[] max, int[] dim );
    }
}

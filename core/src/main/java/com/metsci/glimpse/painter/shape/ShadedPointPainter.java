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
package com.metsci.glimpse.painter.shape;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer2D;
import com.metsci.glimpse.gl.attribute.GLVertexAttribute;
import com.metsci.glimpse.gl.shader.Pipeline;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.gl.texture.FloatTexture1D;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.shader.SimplePointShader;

/**
 * A painter, similar to {@link PointSetPainter}, which allows visualization
 * of large clouds of points or particles. Unlike {@link PointSetPainter},
 * {@code ShadedPointPainter} employs shaders set the color and size of the points.
 *
 * @author ulman
 * @see com.metsci.glimpse.examples.basic.ScatterplotExample
 */
public class ShadedPointPainter extends GlimpseDataPainter2D
{
    protected ReentrantLock lock = new ReentrantLock( );

    protected FloatTexture1D sizeTexture;
    protected ColorTexture1D colorTexture;

    protected GLFloatBuffer2D positionBuffer;
    protected GLFloatBuffer colorAttributeBuffer;
    protected GLFloatBuffer sizeAttributeBuffer;

    protected Pipeline pipeline;
    protected int colorAttributeIndex = 6;
    protected int sizeAttributeIndex = 7;

    protected SimplePointShader vertShader;

    protected boolean constantSize = true;
    protected boolean constantColor = true;

    protected float constantPointSize = 5.0f;
    protected float[] constantPointColor = GlimpseColor.getBlack( );

    protected boolean userPipeline = false;

    public ShadedPointPainter( Axis1D colorAxis, Axis1D sizeAxis, Pipeline pipeline ) throws IOException
    {
        this.lock = new ReentrantLock( );
        this.pipeline = pipeline;
        this.userPipeline = true;
    }

    public ShadedPointPainter( Axis1D colorAxis, Axis1D sizeAxis ) throws IOException
    {
        this.lock = new ReentrantLock( );
        this.initShaderPipeline( colorAxis, sizeAxis );
    }

    protected void initShaderPipeline( Axis1D colorAxis, Axis1D sizeAxis ) throws IOException
    {
        vertShader = new SimplePointShader( 0, 1, colorAttributeIndex, sizeAttributeIndex, colorAxis, sizeAxis );
        pipeline = new Pipeline( "pointshader", null, vertShader, null );
    }

    public void useVertexPositionData( GLFloatBuffer2D positionBuffer )
    {
        lock.lock( );
        try
        {
            this.positionBuffer = positionBuffer;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useColorAttribData( GLFloatBuffer attributeBuffer )
    {
        lock.lock( );
        try
        {
            this.colorAttributeBuffer = attributeBuffer;
            this.setVariablePointColor0( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useSizeAttribData( GLFloatBuffer attributeBuffer )
    {
        lock.lock( );
        try
        {
            this.sizeAttributeBuffer = attributeBuffer;
            this.setVariablePointSize0( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useColorScale( ColorTexture1D colorTexture )
    {
        lock.lock( );
        try
        {
            this.colorTexture = colorTexture;
            this.setVariablePointColor0( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useSizeScale( FloatTexture1D sizeTexture )
    {
        lock.lock( );
        try
        {
            this.sizeTexture = sizeTexture;
            this.setVariablePointSize0( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setDiscardAboveSize( boolean discard )
    {
        lock.lock( );
        try
        {
            this.vertShader.setDiscardAboveSize( discard );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setDiscardBelowSize( boolean discard )
    {
        lock.lock( );
        try
        {
            this.vertShader.setDiscardBelowSize( discard );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setDiscardAboveColor( boolean discard )
    {
        lock.lock( );
        try
        {
            this.vertShader.setDiscardAboveColor( discard );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setDiscardBelowColor( boolean discard )
    {
        lock.lock( );
        try
        {
            this.vertShader.setDiscardBelowColor( discard );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setConstantPointSize( float size )
    {
        lock.lock( );
        try
        {
            this.constantPointSize = size;
            this.setConstantPointSize0( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setConstantPointColor( float[] color )
    {
        lock.lock( );
        try
        {
            this.constantPointColor = color;
            this.setConstantPointColor0( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useConstantColor( )
    {
        lock.lock( );
        try
        {
            this.setConstantPointColor0( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useConstantSize( )
    {
        lock.lock( );
        try
        {
            this.setConstantPointSize0( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useVariableSize( )
    {
        lock.lock( );
        try
        {
            this.setVariablePointSize0( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useVariableColor( )
    {
        lock.lock( );
        try
        {
            this.setVariablePointColor0( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected void setConstantPointColor0( )
    {
        this.constantColor = true;
        this.vertShader.setConstantColor( true );
    }

    protected void setConstantPointSize0( )
    {
        this.constantSize = true;
        this.vertShader.setConstantSize( true );
    }

    protected void setVariablePointColor0( )
    {
        if ( this.colorTexture != null && this.colorAttributeBuffer != null )
        {
            this.constantColor = false;
            this.vertShader.setConstantColor( false );
        }
    }

    protected void setVariablePointSize0( )
    {
        if ( this.sizeTexture != null && this.sizeAttributeBuffer != null )
        {
            this.constantSize = false;
            this.vertShader.setConstantSize( false );
        }
    }

    @Override
    public void paintTo( GL2 gl, GlimpseBounds bounds, Axis2D axis )
    {
        lock.lock( );
        try
        {
            if ( positionBuffer == null ) return;

            if ( !constantSize && ( sizeTexture == null || sizeAttributeBuffer == null ) ) return;

            if ( !constantColor && ( colorTexture == null || colorAttributeBuffer == null ) ) return;

            if ( constantSize )
            {
                gl.glPointSize( constantPointSize );
            }
            else
            {
                sizeTexture.prepare( gl, 1 );
                sizeAttributeBuffer.bind( sizeAttributeIndex, gl );

                gl.glEnable( GL2.GL_VERTEX_PROGRAM_POINT_SIZE );
            }

            if ( constantColor )
            {
                GlimpseColor.glColor( gl, constantPointColor );
            }
            else
            {
                colorTexture.prepare( gl, 0 );
                colorAttributeBuffer.bind( colorAttributeIndex, gl );
            }

            positionBuffer.bind( GLVertexAttribute.ATTRIB_POSITION_2D, gl );
            pipeline.beginUse( gl );

            gl.glEnable( GL2.GL_POINT_SMOOTH );
            gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
            gl.glEnable( GL2.GL_BLEND );

            drawArrays( gl );
        }
        finally
        {
            try
            {
                if ( pipeline != null ) pipeline.endUse( gl );
                if ( positionBuffer != null ) positionBuffer.unbind( gl );
                if ( !constantColor && colorAttributeBuffer != null ) colorAttributeBuffer.unbind( gl );
                if ( !constantSize && sizeAttributeBuffer != null ) sizeAttributeBuffer.unbind( gl );
            }
            finally
            {
                lock.unlock( );
            }
        }
    }

    protected void drawArrays( GL gl )
    {
        gl.glDrawArrays( GL2.GL_POINTS, 0, positionBuffer.getNumVertices( ) );
    }

    @Override
    public void dispose( GLContext context )
    {
        // if the user passed in a pipeline it is their responsibility to dispose of it
        if ( !userPipeline && pipeline != null )
        {
            pipeline.dispose( context );
        }
    }
}

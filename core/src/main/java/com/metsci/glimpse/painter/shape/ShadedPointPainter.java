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
import java.nio.FloatBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.gl.texture.FloatTexture1D;
import com.metsci.glimpse.painter.base.GlimpsePainterImpl;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.shader.SimplePointShader;

//XXX see: https://github.com/sgothel/jogl/blob/master/src/test/com/jogamp/opengl/test/junit/jogl/demos/es2/RedSquareES2.java
//XXX see: https://jogamp.org/deployment/webstart/javadoc/jogl/javadoc/com/jogamp/opengl/util/GLArrayDataClient.html
//XXX allowing multithreaded set of attribute data at the same time rendering is occurring on another thread needs to be worked out
//XXX GLBuffer handled this, but GLArrayDataClient requires stateful seal( boolean ) to be called

/**
 * A painter, similar to {@link PointSetPainter}, which allows visualization
 * of large clouds of points or particles. Unlike {@link PointSetPainter},
 * {@code ShadedPointPainter} employs shaders set the color and size of the points.
 *
 * @author ulman
 * @see com.metsci.glimpse.examples.basic.ScatterplotExample
 */
public class ShadedPointPainter extends GlimpsePainterImpl
{
    protected ReentrantLock lock = new ReentrantLock( );

    protected FloatTexture1D sizeTexture;
    protected ColorTexture1D colorTexture;

    protected SimplePointShader program;

    protected boolean constantSize = true;
    protected boolean constantColor = true;

    protected float constantPointSize = 5.0f;
    protected float[] constantPointColor = GlimpseColor.getBlack( );

    protected int vertexCount = 0;

    public ShadedPointPainter( Axis1D colorAxis, Axis1D sizeAxis ) throws IOException
    {
        this.lock = new ReentrantLock( );
        this.program = newShader( colorAxis, sizeAxis );
    }

    public void useVertexPositionData( FloatBuffer positionBuffer )
    {
        lock.lock( );
        try
        {
            this.program.setVertexData( positionBuffer );
            this.vertexCount = positionBuffer.limit( ) / 2;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useColorAttribData( FloatBuffer attributeBuffer )
    {
        lock.lock( );
        try
        {
            this.program.setColorData( attributeBuffer );
            this.setVariablePointColor0( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useSizeAttribData( FloatBuffer attributeBuffer )
    {
        lock.lock( );
        try
        {
            this.program.setSizeData( attributeBuffer );
            this.setVariablePointColor0( );
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
            this.program.setDiscardAboveSize( discard );
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
            this.program.setDiscardBelowSize( discard );
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
            this.program.setDiscardAboveColor( discard );
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
            this.program.setDiscardBelowColor( discard );
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
        this.program.setConstantColor( true );
    }

    protected void setConstantPointSize0( )
    {
        this.constantSize = true;
        this.program.setConstantSize( true );
    }

    protected void setVariablePointColor0( )
    {
        if ( this.colorTexture != null )
        {
            this.constantColor = false;
            this.program.setConstantColor( false );
        }
    }

    protected void setVariablePointSize0( )
    {
        if ( this.sizeTexture != null )
        {
            this.constantSize = false;
            this.program.setConstantSize( false );
        }
    }

    protected SimplePointShader newShader( Axis1D colorAxis, Axis1D sizeAxis ) throws IOException
    {
        return new SimplePointShader( 0, 1, colorAxis, sizeAxis );
    }

    @Override
    public void paintTo( GlimpseContext context )
    {
        Axis2D axis = getAxis2D( context );
        GL2 gl = context.getGL( ).getGL2( );

        lock.lock( );
        try
        {
            if ( vertexCount == 0 ) return;

            if ( !constantSize && ( sizeTexture == null ) ) return;

            if ( !constantColor && ( colorTexture == null ) ) return;

            if ( constantSize )
            {
                gl.glPointSize( constantPointSize );
            }
            else
            {
                sizeTexture.prepare( gl, 1 );

                gl.glEnable( GL2.GL_VERTEX_PROGRAM_POINT_SIZE );
            }

            if ( constantColor )
            {
                GlimpseColor.glColor( gl, constantPointColor );
            }
            else
            {
                colorTexture.prepare( gl, 0 );
            }

            program.setProjectionMatrix( axis );

            program.useProgram( gl, true );

            gl.glEnable( GL2.GL_POINT_SMOOTH );
            gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
            gl.glEnable( GL2.GL_BLEND );

            drawArrays( gl );
        }
        finally
        {
            try
            {
                if ( program != null ) program.useProgram( gl, false );
            }
            finally
            {
                lock.unlock( );
            }
        }
    }

    protected void drawArrays( GL gl )
    {
        gl.glDrawArrays( GL2.GL_POINTS, 0, vertexCount );
    }

    @Override
    protected void disposeOnce( GlimpseContext context )
    {
        if ( program != null )
        {
            program.dispose( context.getGLContext( ) );
        }
    }
}

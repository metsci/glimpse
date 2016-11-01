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

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.gl.texture.FloatTexture1D;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.shader.point.PointAttributeColorSizeProgram;

/**
 * A painter, similar to {@link PointSetPainter}, which allows visualization
 * of large clouds of points or particles. Unlike {@link PointSetPainter},
 * {@code ShadedPointPainter} employs shaders set the color and size of the points.
 *
 * @author ulman
 * @see com.metsci.glimpse.examples.basic.ScatterplotExample
 */
public class ShadedPointPainter extends GlimpsePainterBase
{
    protected FloatTexture1D sizeTexture;
    protected ColorTexture1D colorTexture;

    protected PointAttributeColorSizeProgram program;

    protected boolean constantSize = true;
    protected boolean constantColor = true;

    protected int vertexCount = 0;

    public ShadedPointPainter( Axis1D colorAxis, Axis1D sizeAxis ) throws IOException
    {
        this.program = newShader( colorAxis, sizeAxis );
    }

    public void useVertexPositionData( FloatBuffer positionBuffer )
    {
        painterLock.lock( );
        try
        {
            this.program.setVertexData( positionBuffer );
            this.vertexCount = positionBuffer.limit( ) / 2;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void useColorAttribData( FloatBuffer attributeBuffer )
    {
        painterLock.lock( );
        try
        {
            this.program.setColorData( attributeBuffer );
            this.setVariablePointColor0( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void useSizeAttribData( FloatBuffer attributeBuffer )
    {
        painterLock.lock( );
        try
        {
            this.program.setSizeData( attributeBuffer );
            this.setVariablePointColor0( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void useColorScale( ColorTexture1D colorTexture )
    {
        painterLock.lock( );
        try
        {
            this.colorTexture = colorTexture;
            this.setVariablePointColor0( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void useSizeScale( FloatTexture1D sizeTexture )
    {
        painterLock.lock( );
        try
        {
            this.sizeTexture = sizeTexture;
            this.setVariablePointSize0( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setDiscardAboveSize( boolean discard )
    {
        painterLock.lock( );
        try
        {
            this.program.setDiscardAboveSize( discard );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setDiscardBelowSize( boolean discard )
    {
        painterLock.lock( );
        try
        {
            this.program.setDiscardBelowSize( discard );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setDiscardAboveColor( boolean discard )
    {
        painterLock.lock( );
        try
        {
            this.program.setDiscardAboveColor( discard );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setDiscardBelowColor( boolean discard )
    {
        painterLock.lock( );
        try
        {
            this.program.setDiscardBelowColor( discard );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setConstantPointSize( float size )
    {
        painterLock.lock( );
        try
        {
            this.constantSize = true;
            this.program.setContstantSize( size );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setConstantPointColor( float[] color )
    {
        painterLock.lock( );
        try
        {
            this.constantColor = true;
            this.program.setContstantColor( color );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void useConstantColor( )
    {
        painterLock.lock( );
        try
        {
            this.constantSize = true;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void useConstantSize( )
    {
        painterLock.lock( );
        try
        {
            this.constantColor = true;
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void useVariableSize( )
    {
        painterLock.lock( );
        try
        {
            this.setVariablePointSize0( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void useVariableColor( )
    {
        painterLock.lock( );
        try
        {
            this.setVariablePointColor0( );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    protected void setVariablePointColor0( )
    {
        this.constantColor = false;
        this.program.setConstantColor( false );
    }

    protected void setVariablePointSize0( )
    {
        this.constantSize = false;
        this.program.setConstantSize( false );
    }

    protected PointAttributeColorSizeProgram newShader( Axis1D colorAxis, Axis1D sizeAxis ) throws IOException
    {
        return new PointAttributeColorSizeProgram( 0, 1, colorAxis, sizeAxis );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        Axis2D axis = requireAxis2D( context );
        GL gl = context.getGL( );

        if ( vertexCount == 0 ) return;

        if ( !constantSize && ( sizeTexture == null ) ) return;

        if ( !constantColor && ( colorTexture == null ) ) return;

        if ( !constantSize )
        {
            sizeTexture.prepare( context, 1 );
        }

        if ( !constantColor )
        {
            colorTexture.prepare( context, 0 );
        }

        program.setProjectionMatrix( axis );

        GLUtils.enableStandardBlending( gl );
        program.useProgram( gl, true );
        try
        {
            drawArrays( gl );
        }
        finally
        {
            program.useProgram( gl, false );
            GLUtils.disableBlending( gl );
        }
    }

    protected void drawArrays( GL gl )
    {
        gl.glDrawArrays( GL.GL_POINTS, 0, vertexCount );
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        program.dispose( context.getGLContext( ) );
    }
}

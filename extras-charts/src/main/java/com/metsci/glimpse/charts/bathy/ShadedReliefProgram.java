/*
 * Copyright (c) 2019 Metron, Inc.
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
package com.metsci.glimpse.charts.bathy;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;

import java.nio.FloatBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLUniformData;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.shader.GlimpseShaderProgram;
import com.metsci.glimpse.gl.texture.DrawableTextureProgram;
import com.metsci.glimpse.support.shader.colormap.ColorMapProgram.ProgramHandles;

/**
 * Takes two textures - the elevation data and the hillshade data - and does a nonlinear
 * interpolation on the video card to index into the colormap.
 *
 * @author borkholder
 */
public class ShadedReliefProgram extends GlimpseShaderProgram implements DrawableTextureProgram
{
    public static final int MAX_COLORS = 20;

    protected GLUniformData alpha;
    protected GLUniformData elevationTexUnit;
    protected GLUniformData hillshadeTexUnit;
    protected GLUniformData colors;
    protected GLUniformData nColors;

    protected GLUniformData AXIS_RECT;

    protected ProgramHandles handles;

    public ShadedReliefProgram( int elevTexUnit, int shadeTexUnit )
    {
        this.initialize( elevTexUnit, shadeTexUnit );
    }

    protected void addShaders( )
    {
        this.addVertexShader( "com/metsci/glimpse/core/shaders/colormap/passthrough.vs" );
        this.addFragmentShader( "com/metsci/glimpse/charts/shaders/relief/shaded_relief_shader.fs" );
    }

    protected void initialize( int elevTexUnit, int shadeTexUnit )
    {
        this.addShaders( );

        this.elevationTexUnit = this.addUniformData( new GLUniformData( "elevtex", elevTexUnit ) );
        this.hillshadeTexUnit = this.addUniformData( new GLUniformData( "shadetex", shadeTexUnit ) );
        this.alpha = this.addUniformData( new GLUniformData( "alpha", 1f ) );
        this.colors = this.addUniformData( new GLUniformData( "colors", 4, FloatBuffer.allocate( 4 * MAX_COLORS ) ) );
        this.nColors = this.addUniformData( new GLUniformData( "nColors", 0 ) );

        this.AXIS_RECT = this.addUniformData( GLUniformData.creatEmptyVector( "AXIS_RECT", 4 ) );
        // without setting default data, we will get "javax.media.opengl.GLException: glUniform atom only available for 1i and 1f"
        // if begin( ) is called before setOrtho( )
        this.AXIS_RECT.setData( FloatBuffer.wrap( new float[] { 0, 1, 0, 1 } ) );
    }

    public void setAlpha( float alpha )
    {
        this.alpha.setData( alpha );
    }

    public void setElevationTexUnit( int unit )
    {
        this.elevationTexUnit.setData( unit );
    }

    public void setHillshadeTexUnit( int unit )
    {
        this.hillshadeTexUnit.setData( unit );
    }

    /**
     * The first dimension is the number of colors, the second is {elevation threshold, hue, saturation, brightness}.
     */
    public void setColors( float[][] colors )
    {
        if ( colors.length > MAX_COLORS )
        {
            throw new IllegalArgumentException( "Only " + MAX_COLORS + " allowed" );
        }

        float[] flattened = new float[colors.length * 4];
        for ( int i = 0, k = 0; i < colors.length; i++ )
        {
            float[] ci = colors[i];
            for ( int j = 0; j < ci.length; j++ )
            {
                flattened[k] = ci[j];
                k++;
            }
        }

        this.colors.setData( FloatBuffer.wrap( flattened ) );
        this.nColors.setData( colors.length );
    }

    @Override
    public void begin( GlimpseContext context, float xMin, float xMax, float yMin, float yMax )
    {
        this.setOrtho( context, xMin, xMax, yMin, yMax );

        this.begin( context );
    }

    public void begin( GlimpseContext context )
    {
        this.useProgram( context.getGL( ), true );
    }

    @Override
    public void doUseProgram( GL gl, boolean on )
    {
        GL3 gl3 = gl.getGL3( );

        if ( this.handles == null )
        {
            this.handles = new ProgramHandles( gl3, this.getShaderProgram( ).program( ) );
        }

        if ( on )
        {
            gl3.glEnableVertexAttribArray( this.handles.inXy );
            gl3.glEnableVertexAttribArray( this.handles.inS );
        }
        else
        {
            gl3.glDisableVertexAttribArray( this.handles.inXy );
            gl3.glDisableVertexAttribArray( this.handles.inS );
        }
    }

    public void setAxisOrtho( GlimpseContext context, Axis2D axis )
    {
        this.setOrtho( context, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
    }

    public void setPixelOrtho( GlimpseContext context, GlimpseBounds bounds )
    {
        this.setOrtho( context, 0, bounds.getWidth( ), 0, bounds.getHeight( ) );
    }

    public void setOrtho( GlimpseContext context, float xMin, float xMax, float yMin, float yMax )
    {
        this.AXIS_RECT.setData( FloatBuffer.wrap( new float[] { xMin, xMax, yMin, yMax } ) );
    }

    @Override
    public void draw( GlimpseContext context, int mode, GLEditableBuffer xyVbo, GLEditableBuffer sVbo, int first, int count )
    {
        GL gl = context.getGL( );
        this.draw( context, mode, xyVbo.deviceBuffer( gl ), sVbo.deviceBuffer( gl ), first, count );
    }

    @Override
    public void draw( GlimpseContext context, int mode, int xyVbo, int sVbo, int first, int count )
    {
        GL3 gl = context.getGL( ).getGL3( );

        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, sVbo );
        gl.glVertexAttribPointer( this.handles.inS, 2, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    @Override
    public void end( GlimpseContext context )
    {
        this.useProgram( context.getGL( ), false );
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        this.dispose( context.getGLContext( ) );
    }
}

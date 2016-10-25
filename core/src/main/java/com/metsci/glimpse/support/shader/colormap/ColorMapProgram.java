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
package com.metsci.glimpse.support.shader.colormap;

import static javax.media.opengl.GL.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLUniformData;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.shader.GlimpseShaderProgram;
import com.metsci.glimpse.gl.texture.DrawableTextureProgram;

/**
 * A shader which colors a 2D data texture using values sampled from a color
 * scaled defined by a 1D color texture.
 *
 * @author ulman
 *
 */
public class ColorMapProgram extends GlimpseShaderProgram implements AxisListener1D, DrawableTextureProgram
{
    private static final Logger logger = Logger.getLogger( ColorMapProgram.class.getName( ) );

    protected GLUniformData dataMin;
    protected GLUniformData dataMax;

    protected GLUniformData alpha;
    protected GLUniformData dataTexUnit;
    protected GLUniformData colorTexUnit;
    protected GLUniformData discardNaN;

    protected GLUniformData AXIS_RECT;

    protected Axis1D colorAxis;

    protected ProgramHandles handles;

    public static class ProgramHandles
    {
        // Vertex attributes

        public final int inXy;
        public final int inS;

        public ProgramHandles( GL2ES2 gl, int program )
        {
            this.inXy = gl.glGetAttribLocation( program, "inXy" );
            this.inS = gl.glGetAttribLocation( program, "inS" );
        }
    }

    /**
     * @param colorAxis color axis producing events
     * @param targetTexUnit 2D texture unit which is the target of color-mapping
     * @param colorTexUnit 1D texture unit containing color-map
     * @throws IOException if the shader source file cannot be read
     */
    public ColorMapProgram( Axis1D colorAxis, int targetTexUnit, int colorTexUnit ) throws IOException
    {
        initialize( colorAxis, targetTexUnit, colorTexUnit );
    }

    protected void addShaders( )
    {
        this.addVertexShader( "shaders/colormap/passthrough.vs" );
        this.addFragmentShader( "shaders/colormap/sampled_colorscale_shader.fs" );
    }

    protected void initialize( Axis1D colorAxis, int targetTexUnit, int colorTexUnit )
    {
        this.addShaders( );

        this.dataMin = this.addUniformData( new GLUniformData( "dataMin", getMin( colorAxis ) ) );
        this.dataMax = this.addUniformData( new GLUniformData( "dataMax", getMax( colorAxis ) ) );
        this.alpha = this.addUniformData( new GLUniformData( "alpha", 1f ) );
        this.discardNaN = this.addUniformData( new GLUniformData( "discardNaN", 0 ) );

        this.dataTexUnit = this.addUniformData( new GLUniformData( "datatex", targetTexUnit ) );
        this.colorTexUnit = this.addUniformData( new GLUniformData( "colortex", colorTexUnit ) );

        this.AXIS_RECT = this.addUniformData( GLUniformData.creatEmptyVector( "AXIS_RECT", 4 ) );
        // without setting default data, we will get "javax.media.opengl.GLException: glUniform atom only available for 1i and 1f"
        // if begin( ) is called before setOrtho( )
        this.AXIS_RECT.setData( FloatBuffer.wrap( new float[] { 0, 1, 0, 1 } ) );

        this.colorAxis = colorAxis;
        this.colorAxis.addAxisListener( this );
    }

    public void setDiscardNaN( boolean discard )
    {
        this.discardNaN.setData( discard ? 1 : 0 );
    }

    public void setAlpha( float alpha )
    {
        this.alpha.setData( alpha );
    }

    @Override
    public void axisUpdated( Axis1D axis )
    {
        dataMin.setData( getMin( axis ) );
        dataMax.setData( getMax( axis ) );
    }

    public void setTargetTexUnit( int unit )
    {
        dataTexUnit.setData( unit );
    }

    public void setColorTexUnit( int unit )
    {
        colorTexUnit.setData( unit );
    }

    protected float getMin( Axis1D axis )
    {
        return ( float ) axis.getMin( );
    }

    protected float getMax( Axis1D axis )
    {
        return ( float ) axis.getMax( );
    }

    /// DrawableTextureProgram methods

    @Override
    public void dispose( GLContext context )
    {
        super.dispose( context );
        this.colorAxis.removeAxisListener( this );
    }

    @Override
    public void begin( GlimpseContext context, float xMin, float xMax, float yMin, float yMax )
    {
        setOrtho( context, xMin, xMax, yMin, yMax );

        begin( context );
    }

    public void begin( GlimpseContext context )
    {
        this.useProgram( context.getGL( ), true );
    }

    @Override
    public void useProgram( GL gl, boolean on )
    {
        super.useProgram( gl, on );

        GL3 gl3 = gl.getGL3( );

        if ( this.handles == null )
        {
            this.handles = new ProgramHandles( gl3, getShaderProgram( ).program( ) );
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
        setOrtho( context, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
    }

    public void setPixelOrtho( GlimpseContext context, GlimpseBounds bounds )
    {
        setOrtho( context, 0, bounds.getWidth( ), 0, bounds.getHeight( ) );
    }

    public void setOrtho( GlimpseContext context, float xMin, float xMax, float yMin, float yMax )
    {
        this.AXIS_RECT.setData( FloatBuffer.wrap( new float[] { xMin, xMax, yMin, yMax } ) );
    }

    @Override
    public void draw( GlimpseContext context, int mode, GLEditableBuffer xyVbo, GLEditableBuffer sVbo, int first, int count )
    {
        GL gl = context.getGL( );
        draw( context, mode, xyVbo.deviceBuffer( gl ), sVbo.deviceBuffer( gl ), first, count );
    }

    @Override
    public void draw( GlimpseContext context, int mode, int xyVbo, int sVbo, int first, int count )
    {
        GL3 gl = context.getGL( ).getGL3( );

        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, sVbo );
        gl.glVertexAttribPointer( handles.inS, 2, GL_FLOAT, false, 0, 0 );

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

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
package com.metsci.glimpse.painter.texture;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;
import static com.metsci.glimpse.gl.util.GLUtils.defaultVertexAttributeArray;
import static com.metsci.glimpse.support.wrapped.WrappedGlimpseContext.getWrapper2D;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_FLOAT;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;
import javax.media.opengl.GL3;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.support.wrapped.Wrapper2D;

public class MultiTagHeatMapProgram implements HeatMapProgram
{
    public static final String lineVertShader_GLSL = requireResourceText( "shaders/HeatMapProgram/heatmap.vs" );
    public static final String lineGeomShader_GLSL = requireResourceText( "shaders/HeatMapProgram/heatmap.gs" );
    public static final String lineFragShader_GLSL = requireResourceText( "shaders/HeatMapProgram/heatmap-multitag.fs" );


    public static class Handles
    {
        public final int program;

        public final int AXIS_RECT;
        public final int WRAP_RECT;

        public final int VALUES_TEXUNIT;
        public final int COLORMAP_TEXUNIT;
        public final int TAG_FRACTIONS_TEXUNIT;
        public final int TAG_VALUES_TEXUNIT;

        public final int ALPHA;
        public final int DISCARD_NAN;
        public final int DISCARD_BELOW;
        public final int DISCARD_ABOVE;

        public final int inXy;
        public final int inSt;

        public Handles( GL2ES2 gl )
        {
            this.program = createProgram( gl, lineVertShader_GLSL, lineGeomShader_GLSL, lineFragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( program, "AXIS_RECT" );
            this.WRAP_RECT = gl.glGetUniformLocation( program, "WRAP_RECT" );

            this.VALUES_TEXUNIT = gl.glGetUniformLocation( program, "VALUES_TEXUNIT" );
            this.COLORMAP_TEXUNIT = gl.glGetUniformLocation( program, "COLORMAP_TEXUNIT" );
            this.TAG_FRACTIONS_TEXUNIT = gl.glGetUniformLocation( program, "TAG_FRACTIONS_TEXUNIT" );
            this.TAG_VALUES_TEXUNIT = gl.glGetUniformLocation( program, "TAG_VALUES_TEXUNIT" );

            this.ALPHA = gl.glGetUniformLocation( program, "ALPHA" );
            this.DISCARD_NAN = gl.glGetUniformLocation( program, "DISCARD_NAN" );
            this.DISCARD_BELOW = gl.glGetUniformLocation( program, "DISCARD_BELOW" );
            this.DISCARD_ABOVE = gl.glGetUniformLocation( program, "DISCARD_ABOVE" );

            this.inXy = gl.glGetAttribLocation( program, "inXy" );
            this.inSt = gl.glGetAttribLocation( program, "inSt" );
        }
    }


    protected final int valuesTexUnit;
    protected final int colormapTexUnit;
    protected final int tagFractionsTexUnit;
    protected final int tagValuesTexUnit;

    protected float alpha;
    protected boolean discardNan;
    protected boolean discardBelow;
    protected boolean discardAbove;

    protected Handles handles;


    public MultiTagHeatMapProgram( int valuesTexUnit, int colormapTexUnit, int tagFractionsTexUnit, int tagValuesTexUnit )
    {
        this.valuesTexUnit = valuesTexUnit;
        this.colormapTexUnit = colormapTexUnit;
        this.tagFractionsTexUnit = tagFractionsTexUnit;
        this.tagValuesTexUnit = tagValuesTexUnit;

        this.alpha = 1f;
        this.discardNan = false;
        this.discardBelow = false;
        this.discardAbove = false;

        this.handles = null;
    }

    @Override
    public void setAlpha( float alpha )
    {
        this.alpha = alpha;
    }

    @Override
    public void setDiscardNan( boolean discardNan )
    {
        this.discardNan = discardNan;
    }

    @Override
    public void setDiscardBelow( boolean discardBelow )
    {
        this.discardBelow = discardBelow;
    }

    @Override
    public void setDiscardAbove( boolean discardAbove )
    {
        this.discardAbove = discardAbove;
    }

    /**
     * Returns the raw GL handles for the shader program, uniforms, and attributes. Compiles and
     * links the program, if necessary.
     */
    public Handles handles( GL2ES2 gl )
    {
        if ( this.handles == null )
        {
            this.handles = new Handles( gl );
        }
        return this.handles;
    }

    @Override
    public void begin( GlimpseContext context, float xMin, float xMax, float yMin, float yMax )
    {
        GL2ES3 gl = context.getGL( ).getGL2ES3( );

        if ( this.handles == null )
        {
            this.handles = new Handles( gl );
        }

        gl.glBindVertexArray( defaultVertexAttributeArray( gl ) );
        gl.glUseProgram( this.handles.program );

        gl.glUniform4f( this.handles.AXIS_RECT, xMin, xMax, yMin, yMax );

        Wrapper2D wrapper = getWrapper2D( context );
        gl.glUniform4f( this.handles.WRAP_RECT, ( float ) wrapper.x.wrapMin( ), ( float ) wrapper.x.wrapMax( ), ( float ) wrapper.y.wrapMin( ), ( float ) wrapper.y.wrapMax( ) );

        // The appropriate calls to glActiveTexture() happen in ShadedTexturePainter.prepare()
        gl.glUniform1i( this.handles.VALUES_TEXUNIT, this.valuesTexUnit );
        gl.glUniform1i( this.handles.COLORMAP_TEXUNIT, this.colormapTexUnit );
        gl.glUniform1i( this.handles.TAG_FRACTIONS_TEXUNIT, this.tagFractionsTexUnit );
        gl.glUniform1i( this.handles.TAG_VALUES_TEXUNIT, this.tagValuesTexUnit );

        gl.glUniform1f( this.handles.ALPHA, this.alpha );
        gl.glUniform1i( this.handles.DISCARD_NAN, ( this.discardNan ? 1 : 0 ) );
        gl.glUniform1i( this.handles.DISCARD_BELOW, ( this.discardBelow ? 1 : 0 ) );
        gl.glUniform1i( this.handles.DISCARD_ABOVE, ( this.discardAbove ? 1 : 0 ) );

        gl.glEnableVertexAttribArray( this.handles.inXy );
        gl.glEnableVertexAttribArray( this.handles.inSt );
    }

    @Override
    public void draw( GlimpseContext context, int mode, GLEditableBuffer xyVbo, GLEditableBuffer stVbo, int first, int count )
    {
        GL gl = context.getGL( );
        this.draw( context, mode, xyVbo.deviceBuffer( gl ), stVbo.deviceBuffer( gl ), first, count );
    }

    @Override
    public void draw( GlimpseContext context, int mode, int xyVbo, int stVbo, int first, int count )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glBindBuffer( GL_ARRAY_BUFFER, stVbo );
        gl.glVertexAttribPointer( this.handles.inSt, 2, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( mode, first, count );
    }

    @Override
    public void end( GlimpseContext context )
    {
        GL2ES3 gl = context.getGL( ).getGL2ES3( );

        gl.glDisableVertexAttribArray( this.handles.inXy );
        gl.glDisableVertexAttribArray( this.handles.inSt );

        gl.glUseProgram( 0 );
        gl.glBindVertexArray( 0 );
    }

    /**
     * Deletes the program, and resets this object to the way it was before {@link #begin(GL3)}
     * was first called.
     * <p>
     * This object can be safely reused after being disposed, but in most cases there is no
     * significant advantage to doing so.
     */
    @Override
    public void dispose( GlimpseContext context )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        if ( this.handles != null )
        {
            gl.glDeleteProgram( this.handles.program );
            this.handles = null;
        }
    }

}

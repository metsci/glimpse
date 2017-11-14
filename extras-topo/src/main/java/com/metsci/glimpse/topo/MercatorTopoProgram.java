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
package com.metsci.glimpse.topo;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;
import static com.metsci.glimpse.gl.util.GLUtils.defaultVertexAttributeArray;
import static com.metsci.glimpse.painter.base.GlimpsePainterBase.requireAxis2D;
import static com.metsci.glimpse.topo.TopoUtils.dataDenormFactor;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_TEXTURE0;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TRIANGLE_STRIP;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.topo.proj.MercatorNormalCylindricalProjection;

public class MercatorTopoProgram
{

    public static final String vertShader_GLSL = requireResourceText( "shaders/TopoProgram/topo-mercator.vs" );
    public static final String fragShader_GLSL = requireResourceText( "shaders/TopoProgram/topo-mercator.fs" );


    public static class Handles
    {
        public final int program;

        public final int AXIS_RECT;

        public final int ORIGIN_LON_RAD;

        public final int DATA_TEX_UNIT;
        public final int DATA_DENORM_FACTOR;
        public final int DATA_LAT_MAX_RAD;
        public final int DATA_LAT_SPAN_RAD;
        public final int DATA_LON_MIN_RAD;
        public final int DATA_LON_SPAN_RAD;

        public final int BATHY_COLORMAP_TEX_UNIT;
        public final int BATHY_COLORMAP_MIN_VALUE;
        public final int TOPO_COLORMAP_TEX_UNIT;
        public final int TOPO_COLORMAP_MAX_VALUE;

        public final int inXy;

        public Handles( GL2ES2 gl )
        {
            this.program = createProgram( gl, vertShader_GLSL, null, fragShader_GLSL );

            this.AXIS_RECT = gl.glGetUniformLocation( program, "AXIS_RECT" );

            this.ORIGIN_LON_RAD = gl.glGetUniformLocation( program, "ORIGIN_LON_RAD" );

            this.DATA_TEX_UNIT = gl.glGetUniformLocation( program, "DATA_TEX_UNIT" );
            this.DATA_DENORM_FACTOR = gl.glGetUniformLocation( program, "DATA_DENORM_FACTOR" );
            this.DATA_LAT_MAX_RAD = gl.glGetUniformLocation( program, "DATA_LAT_MAX_RAD" );
            this.DATA_LAT_SPAN_RAD = gl.glGetUniformLocation( program, "DATA_LAT_SPAN_RAD" );
            this.DATA_LON_MIN_RAD = gl.glGetUniformLocation( program, "DATA_LON_MIN_RAD" );
            this.DATA_LON_SPAN_RAD = gl.glGetUniformLocation( program, "DATA_LON_SPAN_RAD" );

            this.BATHY_COLORMAP_TEX_UNIT = gl.glGetUniformLocation( program, "BATHY_COLORMAP_TEX_UNIT" );
            this.BATHY_COLORMAP_MIN_VALUE = gl.glGetUniformLocation( program, "BATHY_COLORMAP_MIN_VALUE" );
            this.TOPO_COLORMAP_TEX_UNIT = gl.glGetUniformLocation( program, "TOPO_COLORMAP_TEX_UNIT" );
            this.TOPO_COLORMAP_MAX_VALUE = gl.glGetUniformLocation( program, "TOPO_COLORMAP_MAX_VALUE" );

            this.inXy = gl.glGetAttribLocation( program, "inXy" );
        }
    }


    protected Handles handles;

    protected final int dataTexUnit;

    protected final int bathyColormapTexUnit;
    protected final ColorTexture1D bathyColormapTexture;
    protected final float bathyColormapMinValue;

    protected final int topoColormapTexUnit;
    protected final ColorTexture1D topoColormapTexture;
    protected final float topoColormapMaxValue;


    public MercatorTopoProgram( int dataTexUnit,
                                int bathyColormapTexUnit,
                                int topoColormapTexUnit,
                                ColorTexture1D bathyColormapTexture,
                                ColorTexture1D topoColormapTexture,
                                float bathyColormapMinValue,
                                float topoColormapMaxValue )
    {
        this.handles = null;

        this.dataTexUnit = dataTexUnit;

        this.bathyColormapTexUnit = bathyColormapTexUnit;
        this.bathyColormapTexture = bathyColormapTexture;
        this.bathyColormapMinValue = bathyColormapMinValue;

        this.topoColormapTexUnit = topoColormapTexUnit;
        this.topoColormapTexture = topoColormapTexture;
        this.topoColormapMaxValue = topoColormapMaxValue;
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

    public void begin( GlimpseContext context, MercatorNormalCylindricalProjection proj )
    {
        GL2ES3 gl = context.getGL( ).getGL2ES3( );

        if ( this.handles == null )
        {
            this.handles = new Handles( gl );
        }

        gl.glBindVertexArray( defaultVertexAttributeArray( gl ) );
        gl.glUseProgram( this.handles.program );

        Axis2D axis = requireAxis2D( context );
        gl.glUniform4f( this.handles.AXIS_RECT, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );

        gl.glUniform1f( this.handles.ORIGIN_LON_RAD, ( float ) proj.originLon_RAD );

        gl.glUniform1i( this.handles.DATA_TEX_UNIT, this.dataTexUnit );

        this.bathyColormapTexture.prepare( context, this.bathyColormapTexUnit );
        gl.glUniform1i( this.handles.BATHY_COLORMAP_TEX_UNIT, this.bathyColormapTexUnit );
        gl.glUniform1f( this.handles.BATHY_COLORMAP_MIN_VALUE, this.bathyColormapMinValue );

        this.topoColormapTexture.prepare( context, this.topoColormapTexUnit );
        gl.glUniform1i( this.handles.TOPO_COLORMAP_TEX_UNIT, this.topoColormapTexUnit );
        gl.glUniform1f( this.handles.TOPO_COLORMAP_MAX_VALUE, this.topoColormapMaxValue );

        gl.glEnableVertexAttribArray( this.handles.inXy );
    }

    public void draw( GlimpseContext context, TopoDeviceTile tile )
    {
        GL2ES2 gl = context.getGL( ).getGL2ES2( );

        gl.glUniform1f( this.handles.DATA_DENORM_FACTOR, dataDenormFactor( tile.textureDataType ) );

        gl.glUniform1f( this.handles.DATA_LAT_MAX_RAD,  ( float ) ( tile.northLat_RAD ) );
        gl.glUniform1f( this.handles.DATA_LAT_SPAN_RAD, ( float ) ( tile.northLat_RAD - tile.southLat_RAD ) );
        gl.glUniform1f( this.handles.DATA_LON_MIN_RAD,  ( float ) ( tile.westLon_RAD ) );
        gl.glUniform1f( this.handles.DATA_LON_SPAN_RAD, ( float ) ( tile.eastLon_RAD - tile.westLon_RAD ) );

        gl.glActiveTexture( GL_TEXTURE0 + this.dataTexUnit );
        gl.glBindTexture( GL_TEXTURE_2D, tile.texture );

        gl.glBindBuffer( GL_ARRAY_BUFFER, tile.xyBuffer );
        gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, 0 );

        gl.glDrawArrays( GL_TRIANGLE_STRIP, 0, tile.numVertices );
    }

    public void end( GlimpseContext context )
    {
        GL2ES3 gl = context.getGL( ).getGL2ES3( );

        gl.glDisableVertexAttribArray( this.handles.inXy );

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

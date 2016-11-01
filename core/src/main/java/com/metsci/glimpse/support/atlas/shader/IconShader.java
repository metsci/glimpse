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
package com.metsci.glimpse.support.atlas.shader;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLUniformData;

import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.GLArrayDataClient;
import com.jogamp.opengl.util.GLArrayDataServer;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.shader.GlimpseShaderProgram;

public class IconShader extends GlimpseShaderProgram
{
    // Fragment Shader

    protected int textureUnit;
    protected GLUniformData textureUnitArg;
    protected GLUniformData isPickModeArg;
    protected boolean enablePicking;

    // Vertex Shader

    protected GLArrayDataClient pixelCoordAttribute;
    protected GLArrayDataClient texCoordAttribute;
    protected GLArrayDataClient colorCoordAttribute;

    protected GLUniformData mvpMatrix;
    protected GLArrayDataClient vertexAttribute;

    // Geometry Shader

    protected GLUniformData viewportWidth;
    protected GLUniformData viewportHeight;
    protected GLUniformData globalScale;

    public IconShader( int textureUnit, boolean enablePicking )
    {
        this.addVertexShader( "shaders/atlas/texture_atlas_icon_shader.vs" );
        this.addGeometryShader( "shaders/atlas/texture_atlas_icon_shader.gs" );
        this.addFragmentShader( "shaders/atlas/texture_atlas_icon_shader.fs" );

        // Fragment Shader

        this.textureUnit = textureUnit;
        this.enablePicking = enablePicking;

        this.textureUnitArg = this.addUniformData( new GLUniformData( "tex", textureUnit ) );
        this.isPickModeArg = this.addUniformData( new GLUniformData( "isPickMode", enablePicking ? 1 : 0 ) );

        // Vertex Shader

        this.vertexAttribute = this.addArrayData( GLArrayDataServer.createGLSL( "a_position", 4, GL.GL_FLOAT, false, 0, GL.GL_DYNAMIC_DRAW ) );
        this.pixelCoordAttribute = this.addArrayData( GLArrayDataServer.createGLSL( "pixelCoords", 4, GL.GL_FLOAT, false, 0, GL.GL_DYNAMIC_DRAW ) );
        this.texCoordAttribute = this.addArrayData( GLArrayDataServer.createGLSL( "texCoords", 4, GL.GL_FLOAT, false, 0, GL.GL_DYNAMIC_DRAW ) );
        this.colorCoordAttribute = this.addArrayData( GLArrayDataServer.createGLSL( "pickColor", 3, GL.GL_BYTE, false, 0, GL.GL_DYNAMIC_DRAW ) );

        this.mvpMatrix = this.addUniformData( GLUniformData.creatEmptyMatrix( "mvpMatrix", 4, 4 ) );

        // Geometry Shader

        this.viewportWidth = this.addUniformData( new GLUniformData( "viewportWidth", ( float ) 1 ) );
        this.viewportHeight = this.addUniformData( new GLUniformData( "viewportHeight", ( float ) 1 ) );
        this.globalScale = this.addUniformData( new GLUniformData( "globalScale", ( float ) 1 ) );
    }

    public void setProjectionMatrix( float minX, float maxX, float minY, float maxY )
    {
        Matrix4 m = new Matrix4( );
        m.makeOrtho( minX, maxX, minY, maxY, -1, 1 );
        this.mvpMatrix.setData( FloatBuffer.wrap( m.getMatrix( ) ) );
    }

    public void setProjectionMatrix( Axis2D axis )
    {
        setProjectionMatrix( ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
    }

    public void setPickMode( boolean pickMode )
    {
        this.enablePicking = pickMode;
        this.isPickModeArg.setData( pickMode ? 1 : 0 );
    }

    public void setPixelCoordData( Buffer b )
    {
        this.pixelCoordAttribute.reset( );
        this.pixelCoordAttribute.put( b.rewind( ) );
        this.pixelCoordAttribute.seal( true );
    }

    public void setTexCoordData( Buffer b )
    {
        this.texCoordAttribute.reset( );
        this.texCoordAttribute.put( b.rewind( ) );
        this.texCoordAttribute.seal( true );
    }

    public void setColorCoordData( Buffer b )
    {
        this.colorCoordAttribute.reset( );
        this.colorCoordAttribute.put( b.rewind( ) );
        this.colorCoordAttribute.seal( true );
    }

    public void setVertexData( Buffer b )
    {
        this.vertexAttribute.reset( );
        this.vertexAttribute.put( b.rewind( ) );
        this.vertexAttribute.seal( true );
    }

    public void updateViewport( GlimpseBounds bounds )
    {
        this.updateViewport( bounds.getWidth( ), bounds.getHeight( ) );
    }

    public void updateViewport( int width, int height )
    {
        this.viewportWidth.setData( ( float ) width );
        this.viewportHeight.setData( ( float ) height );
    }

    public void setGlobalScale( float scale )
    {
        this.globalScale.setData( scale );
    }

    @Override
    public void useProgram( GL gl, boolean on )
    {
        super.useProgram( gl, on );
    }
}
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
package com.metsci.glimpse.support.shader;

import java.io.IOException;
import java.nio.Buffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLUniformData;

import com.jogamp.opengl.util.GLArrayDataClient;
import com.jogamp.opengl.util.GLArrayDataServer;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.gl.joglshader.GlimpseShaderProgram;

public class SimplePointShader extends GlimpseShaderProgram
{
    protected GLUniformData colorTexUnit;
    protected GLUniformData colorMin;
    protected GLUniformData colorMax;

    protected GLUniformData sizeTexUnit;
    protected GLUniformData sizeMin;
    protected GLUniformData sizeMax;

    protected GLUniformData discardBelowColor;
    protected GLUniformData discardAboveColor;

    protected GLUniformData discardBelowSize;
    protected GLUniformData discardAboveSize;

    protected GLUniformData constantSize;
    protected GLUniformData constantColor;

    protected GLArrayDataClient colorAttribute;
    protected GLArrayDataClient sizeAttribute;

    public SimplePointShader( int colorTextureUnit, int sizeTextureUnit, Axis1D colorAxis, Axis1D sizeAxis ) throws IOException
    {
        this.addVertexShader( "shaders/point/point_shader.vs" );

        this.colorTexUnit = this.addUniformData( new GLUniformData( "valTexture_color", colorTextureUnit ) );
        this.colorMin = this.addUniformData( new GLUniformData( "valMin_color", ( float ) colorAxis.getMin( ) ) );
        this.colorMax = this.addUniformData( new GLUniformData( "valMax_color", ( float ) colorAxis.getMax( ) ) );

        this.sizeTexUnit = this.addUniformData( new GLUniformData( "valTexture_size", sizeTextureUnit ) );
        this.sizeMin = this.addUniformData( new GLUniformData( "valMin_size", ( float ) sizeAxis.getMin( ) ) );
        this.sizeMax = this.addUniformData( new GLUniformData( "valMax_size", ( float ) sizeAxis.getMax( ) ) );

        this.discardBelowColor = this.addUniformData( new GLUniformData( "discardAbove_color", 0 ) );
        this.discardAboveColor = this.addUniformData( new GLUniformData( "discardBelow_color", 0 ) );

        this.discardBelowSize = this.addUniformData( new GLUniformData( "discardAbove_size", 0 ) );
        this.discardAboveSize = this.addUniformData( new GLUniformData( "discardBelow_size", 0 ) );

        this.constantSize = this.addUniformData( new GLUniformData( "constant_color", 1 ) );
        this.constantColor = this.addUniformData( new GLUniformData( "constant_size", 1 ) );

        this.colorAttribute = this.addArrayData( GLArrayDataServer.createGLSL( "valColor", 1, GL.GL_FLOAT, false, 0, GL.GL_STATIC_DRAW ) );
        this.sizeAttribute = this.addArrayData( GLArrayDataServer.createGLSL( "valSize", 1, GL.GL_FLOAT, false, 0, GL.GL_STATIC_DRAW ) );

        colorAxis.addAxisListener( new AxisListener1D( )
        {
            @Override
            public void axisUpdated( Axis1D handler )
            {
                colorMin.setData( ( float ) handler.getMin( ) );
                colorMax.setData( ( float ) handler.getMax( ) );
            }
        } );

        sizeAxis.addAxisListener( new AxisListener1D( )
        {
            @Override
            public void axisUpdated( Axis1D handler )
            {
                sizeMin.setData( ( float ) handler.getMin( ) );
                sizeMax.setData( ( float ) handler.getMax( ) );
            }
        } );
    }

    public void setSizeData( Buffer b )
    {
        this.sizeAttribute.reset( );
        this.sizeAttribute.put( b );
    }

    public void setColorData( Buffer b )
    {
        this.colorAttribute.reset( );
        this.colorAttribute.put( b );
    }

    public void setConstantColor( boolean constant )
    {
        constantColor.setData( constant ? 1 : 0 );
    }

    public void setConstantSize( boolean constant )
    {
        constantSize.setData( constant ? 1 : 0 );
    }

    public void setDiscardAboveSize( boolean discard )
    {
        discardAboveSize.setData( discard ? 1 : 0 );
    }

    public void setDiscardBelowSize( boolean discard )
    {
        discardBelowSize.setData( discard ? 1 : 0 );
    }

    public void setDiscardAboveColor( boolean discard )
    {
        discardAboveColor.setData( discard ? 1 : 0 );
    }

    public void setDiscardBelowColor( boolean discard )
    {
        discardBelowColor.setData( discard ? 1 : 0 );
    }
}

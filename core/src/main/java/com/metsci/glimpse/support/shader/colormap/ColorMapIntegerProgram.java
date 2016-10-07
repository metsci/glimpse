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

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.media.opengl.GLUniformData;

import com.metsci.glimpse.axis.Axis1D;

public class ColorMapIntegerProgram extends ColorMapProgram
{
    public ColorMapIntegerProgram( Axis1D colorAxis, int targetTexUnit, int colorTexUnit ) throws IOException
    {
        super( colorAxis, targetTexUnit, colorTexUnit );
    }

    @Override
    protected void addShaders( )
    {
        this.addVertexShader( "shaders/colormap/passthrough.vs" );
        this.addFragmentShader( "shaders/colormap/sampled_colorscale_shader_integer.fs" );
    }

    protected void initialize( Axis1D colorAxis, int targetTexUnit, int colorTexUnit )
    {
        this.addShaders( );

        this.dataMin = this.addUniformData( new GLUniformData( "dataMin", getMin( colorAxis ) ) );
        this.dataMax = this.addUniformData( new GLUniformData( "dataMax", getMax( colorAxis ) ) );
        this.alpha = this.addUniformData( new GLUniformData( "alpha", 1f ) );

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
        // do nothing, shader operates on uint textures
    }
}

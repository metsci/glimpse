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
package com.metsci.glimpse.support.shader;

import java.io.IOException;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.gl.shader.Shader;
import com.metsci.glimpse.gl.shader.ShaderArg;
import com.metsci.glimpse.gl.shader.ShaderSource;
import com.metsci.glimpse.gl.shader.ShaderType;
import com.metsci.glimpse.util.io.StreamOpener;

public class SimplePointShader extends Shader
{
    protected ShaderArg colorTexUnit;
    protected ShaderArg colorMin;
    protected ShaderArg colorMax;

    protected ShaderArg sizeTexUnit;
    protected ShaderArg sizeMin;
    protected ShaderArg sizeMax;

    protected ShaderArg discardBelowColor;
    protected ShaderArg discardAboveColor;

    protected ShaderArg discardBelowSize;
    protected ShaderArg discardAboveSize;

    protected ShaderArg constantSize;
    protected ShaderArg constantColor;

    protected int colorAttributeIndex;
    protected int sizeAttributeIndex;

    public SimplePointShader( int colorTextureUnit, int sizeTextureUnit, int colorAttributeIndex, int sizeAttributeIndex, Axis1D colorAxis, Axis1D sizeAxis ) throws IOException
    {
        this( colorTextureUnit, sizeTextureUnit, colorAttributeIndex, sizeAttributeIndex, colorAxis, sizeAxis, readSource( ) );
    }

    protected SimplePointShader( int colorTextureUnit, int sizeTextureUnit, int colorAttributeIndex, int sizeAttributeIndex, Axis1D colorAxis, Axis1D sizeAxis, ShaderSource... source ) throws IOException
    {
        super( "point_shader", ShaderType.vertex, source );
        this.initializeShaderArgs( );
        this.initializeShaderValues( colorTextureUnit, sizeTextureUnit, colorAttributeIndex, sizeAttributeIndex, colorAxis, sizeAxis );
    }

    protected void initializeShaderArgs( )
    {
        this.colorTexUnit = getArg( "valTexture_color" );
        this.colorMin = getArg( "valMin_color" );
        this.colorMax = getArg( "valMax_color" );

        this.sizeTexUnit = getArg( "valTexture_size" );
        this.sizeMin = getArg( "valMin_size" );
        this.sizeMax = getArg( "valMax_size" );

        this.discardAboveColor = getArg( "discardAbove_color" );
        this.discardBelowColor = getArg( "discardBelow_color" );
        this.discardAboveSize = getArg( "discardAbove_size" );
        this.discardBelowSize = getArg( "discardBelow_size" );

        this.constantColor = getArg( "constant_color" );
        this.constantSize = getArg( "constant_size" );
    }

    protected void initializeShaderValues( int colorTextureUnit, int sizeTextureUnit, int colorAttributeIndex, int sizeAttributeIndex, Axis1D colorAxis, Axis1D sizeAxis )
    {
        this.colorAttributeIndex = colorAttributeIndex;
        this.sizeAttributeIndex = sizeAttributeIndex;

        this.colorTexUnit.setValue( colorTextureUnit );
        this.colorMin.setValue( colorAxis.getMin( ) );
        this.colorMax.setValue( colorAxis.getMax( ) );

        colorAxis.addAxisListener( new AxisListener1D( )
        {
            @Override
            public void axisUpdated( Axis1D handler )
            {
                colorMin.setValue( handler.getMin( ) );
                colorMax.setValue( handler.getMax( ) );
            }
        } );

        this.sizeTexUnit.setValue( sizeTextureUnit );
        this.sizeMin.setValue( sizeAxis.getMin( ) );
        this.sizeMax.setValue( sizeAxis.getMax( ) );

        sizeAxis.addAxisListener( new AxisListener1D( )
        {
            @Override
            public void axisUpdated( Axis1D handler )
            {
                sizeMin.setValue( handler.getMin( ) );
                sizeMax.setValue( handler.getMax( ) );
            }
        } );

        this.discardAboveColor.setValue( false );
        this.discardBelowColor.setValue( false );
        this.discardAboveSize.setValue( false );
        this.discardBelowSize.setValue( false );

        this.constantColor.setValue( true );
        this.constantSize.setValue( true );
    }

    private final static ShaderSource readSource( ) throws IOException
    {
        return new ShaderSource( "shaders/point/point_shader.vs", StreamOpener.fileThenResource );
    }

    @Override
    public boolean preLink( GL gl, int glProgramHandle )
    {
        GL2 gl2 = gl.getGL2( );

        gl2.glBindAttribLocation( glProgramHandle, colorAttributeIndex, "valColor" );
        gl2.glBindAttribLocation( glProgramHandle, sizeAttributeIndex, "valSize" );

        return true;
    }

    @Override
    public void preDisplay( GL gl )
    {
        // empty
    }

    @Override
    public void postDisplay( GL gl )
    {
        // empty
    }

    public void setConstantColor( boolean constant )
    {
        constantColor.setValue( constant );
    }

    public void setConstantSize( boolean constant )
    {
        constantSize.setValue( constant );
    }

    public void setDiscardAboveSize( boolean discard )
    {
        discardAboveSize.setValue( discard );
    }

    public void setDiscardBelowSize( boolean discard )
    {
        discardBelowSize.setValue( discard );
    }

    public void setDiscardAboveColor( boolean discard )
    {
        discardAboveColor.setValue( discard );
    }

    public void setDiscardBelowColor( boolean discard )
    {
        discardBelowColor.setValue( discard );
    }
}

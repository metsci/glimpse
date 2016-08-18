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

import javax.media.opengl.GLUniformData;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.gl.shader.GlimpseShaderProgram;

/**
 * A shader which colors a 2D data texture using values sampled from a color
 * scaled defined by a 1D color texture.
 *
 * @author ulman
 *
 */
public class SampledColorScaleShader extends GlimpseShaderProgram implements AxisListener1D
{
    private GLUniformData dataMin;
    private GLUniformData dataMax;

    private GLUniformData alpha;
    private GLUniformData dataTexUnit;
    private GLUniformData colorTexUnit;

    private Axis1D colorAxis;

    private GLUniformData discardNaN;

    /**
     * @param colorAxis color axis producing events
     * @param targetTexUnit 2D texture unit which is the target of color-mapping
     * @param colorTexUnit 1D texture unit containing color-map
     * @throws IOException if the shader source file cannot be read
     */
    public SampledColorScaleShader( Axis1D colorAxis, int targetTexUnit, int colorTexUnit ) throws IOException
    {
        initialize( colorAxis, targetTexUnit, colorTexUnit );
    }
    
    protected void addShaders( )
    {
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

    @Override
    public void dispose( GLContext context )
    {
        super.dispose( context );
        this.colorAxis.removeAxisListener( this );
    }
}

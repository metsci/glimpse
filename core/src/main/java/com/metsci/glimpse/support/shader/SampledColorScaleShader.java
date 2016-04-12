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

import static com.metsci.glimpse.gl.shader.ShaderType.fragment;

import java.io.IOException;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.gl.shader.Shader;
import com.metsci.glimpse.gl.shader.ShaderArg;
import com.metsci.glimpse.gl.shader.ShaderSource;
import com.metsci.glimpse.util.io.StreamOpener;

/**
 * A shader which colors a 2D data texture using values sampled from a color
 * scaled defined by a 1D color texture.
 *
 * @author ulman
 *
 */
public class SampledColorScaleShader extends Shader implements AxisListener1D
{
    private ShaderArg dataMin;
    private ShaderArg dataMax;

    private ShaderArg alpha;
    private ShaderArg dataTexUnit;
    private ShaderArg colorTexUnit;

    private Axis1D colorAxis;

    private ShaderArg discardNaN;

    /**
     * @param colorAxis color axis producing events
     * @param targetTexUnit 2D texture unit which is the target of color-mapping
     * @param colorTexUnit 1D texture unit containing color-map
     * @throws IOException if the shader source file cannot be read
     */
    public SampledColorScaleShader( Axis1D colorAxis, int targetTexUnit, int colorTexUnit ) throws IOException
    {
        super( "sampled_colorscale_shader", fragment, readSource( "shaders/colormap/sampled_colorscale_shader.fs" ) );

        initialize( colorAxis, targetTexUnit, colorTexUnit );
    }

    protected SampledColorScaleShader( String source ) throws IOException
    {
        super( "sampled_colorscale_shader", fragment, readSource( source ) );
    }

    protected void initialize( Axis1D colorAxis, int targetTexUnit, int colorTexUnit )
    {
        this.colorAxis = colorAxis;

        this.colorAxis.addAxisListener( this );

        this.dataMin = getArg( "dataMin" );
        this.dataMin.setValue( getMin( colorAxis ) );

        this.dataMax = getArg( "dataMax" );
        this.dataMax.setValue( getMax( colorAxis ) );

        this.alpha = getArg( "alpha" );
        this.alpha.setValue( 1f );

        this.dataTexUnit = getArg( "datatex" );
        this.dataTexUnit.setValue( targetTexUnit );

        this.colorTexUnit = getArg( "colortex" );
        this.colorTexUnit.setValue( colorTexUnit );

        this.discardNaN = getArg( "discardNaN" );
        this.discardNaN.setValue( false );
    }

    private final static ShaderSource readSource( String source ) throws IOException
    {
        return new ShaderSource( source, StreamOpener.fileThenResource );
    }

    public void setDiscardNaN( boolean discard )
    {
        discardNaN.setValue( discard );
    }

    public void setAlpha( float alpha )
    {
        this.alpha.setValue( alpha );
    }

    @Override
    public boolean preLink( GL gl, int glProgramHandle )
    {
        // empty
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

    @Override
    public void axisUpdated( Axis1D axis )
    {
        dataMin.setValue( getMin( axis ) );
        dataMax.setValue( getMax( axis ) );
    }

    public void setTargetTexUnit( int unit )
    {
        dataTexUnit.setValue( unit );
    }

    public void setColorTexUnit( int unit )
    {
        colorTexUnit.setValue( unit );
    }

    protected double getMin( Axis1D axis )
    {
        return axis.getMin( );
    }

    protected double getMax( Axis1D axis )
    {
        return axis.getMax( );
    }

    @Override
    public void dispose( GLContext context )
    {
        super.dispose( context );
        this.colorAxis.removeAxisListener( this );
    }
}

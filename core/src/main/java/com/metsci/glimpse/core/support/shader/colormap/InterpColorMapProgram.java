/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.support.shader.colormap;

import static com.metsci.glimpse.core.support.wrapped.WrappedGlimpseContext.getWrapper2D;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.nio.FloatBuffer;
import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GLUniformData;
import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.painter.texture.BasicHeatMapProgram;
import com.metsci.glimpse.core.painter.texture.HeatMapProgram;
import com.metsci.glimpse.core.support.wrapped.Wrapper2D;

/**
 * Does a non-linear interpolation on the GPU and then maps into the colorscale.
 * Works better when the colormap and data have discrete steps.
 *
 * @author borkholder
 */
public class InterpColorMapProgram extends ColorMapProgram implements HeatMapProgram
{
    private static final Logger LOGGER = Logger.getLogger( InterpColorMapProgram.class.getName( ) );

    protected GLUniformData discardAbove;
    protected GLUniformData discardBelow;
    protected GLUniformData overrideAlpha;

    protected GLUniformData WRAP_RECT;

    public static class Handles extends ColorMapProgram.ProgramHandles
    {
        public final int WRAP_RECT;

        public Handles( GL2ES2 gl, int program )
        {
            super( gl, program );

            this.WRAP_RECT = gl.glGetUniformLocation( program, "WRAP_RECT" );
        }
    }

    public InterpColorMapProgram( Axis1D colorAxis, int targetTexUnit, int colorTexUnit )
    {
        super( colorAxis, targetTexUnit, colorTexUnit );
        super.setDiscardNaN( true );
    }

    @Override
    protected void initialize( Axis1D colorAxis, int targetTexUnit, int colorTexUnit )
    {
        super.initialize( colorAxis, targetTexUnit, colorTexUnit );

        this.WRAP_RECT = this.addUniformData( GLUniformData.creatEmptyVector( "WRAP_RECT", 4 ) );

        // without setting default data, we will get "com.jogamp.opengl.GLException: glUniform atom only available for 1i and 1f"
        // if begin( ) is called before setOrtho( )
        this.WRAP_RECT.setData( FloatBuffer.wrap( new float[] { 0, 1, 0, 1 } ) );

        this.discardAbove = this.addUniformData( new GLUniformData( "discardAbove", 0 ) );
        this.discardBelow = this.addUniformData( new GLUniformData( "discardBelow", 0 ) );
        this.overrideAlpha = this.addUniformData( new GLUniformData( "overrideAlpha", 0 ) );
    }

    @Override
    public void begin( GlimpseContext context, float xMin, float xMax, float yMin, float yMax )
    {
        super.begin( context, xMin, xMax, yMin, yMax );

        Wrapper2D wrapper = getWrapper2D( context );
        GL2ES3 gl = context.getGL( ).getGL2ES3( );
        gl.glUniform4f( ( ( Handles ) this.handles ).WRAP_RECT, ( float ) wrapper.x.wrapMin( ), ( float ) wrapper.x.wrapMax( ), ( float ) wrapper.y.wrapMin( ), ( float ) wrapper.y.wrapMax( ) );
    }

    @Override
    public void doUseProgram( GL gl, boolean on )
    {
        if ( this.handles == null )
        {
            this.handles = new Handles( gl.getGL2ES2( ), this.getShaderProgram( ).program( ) );
        }

        super.doUseProgram( gl, on );
    }

    public void setOverrideAlpha( boolean override )
    {
        this.overrideAlpha.setData( override ? 1 : 0 );
    }

    @Override
    protected void addShaders( )
    {
        this.addVertexShader( InterpColorMapProgram.class.getResource( "passthrough.vs" ) );
        this.addGeometryShader( BasicHeatMapProgram.class.getResource( "heatmap/heatmap.gs" ) );
        this.addFragmentShader( InterpColorMapProgram.class.getResource( "interp_colorscale_shader.fs" ) );
    }

    @Override
    public void setDiscardNaN( boolean discard )
    {
        logWarning( LOGGER, "discardNaN has not effect for " + InterpColorMapProgram.class.getSimpleName( ) );
    }

    @Override
    public void setUseColormapAlpha( boolean useColormapAlpha )
    {
        this.overrideAlpha.setData( useColormapAlpha ? 0 : 1 );
    }

    @Override
    public void setDiscardNan( boolean discardNan )
    {
        super.setDiscardNaN( discardNan );
    }

    @Override
    public void setDiscardBelow( boolean discardBelow )
    {
        this.discardBelow.setData( discardBelow ? 1 : 0 );
    }

    @Override
    public void setDiscardAbove( boolean discardAbove )
    {
        this.discardAbove.setData( discardAbove ? 1 : 0 );
    }
}

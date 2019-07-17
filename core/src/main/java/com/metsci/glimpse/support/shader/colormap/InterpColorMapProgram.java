/*
 * Copyright (c) 2019, Metron, Inc.
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

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.io.IOException;
import java.util.logging.Logger;

import com.jogamp.opengl.GLUniformData;

import com.metsci.glimpse.axis.Axis1D;

/**
 * Does a non-linear interpolation on the GPU and then maps into the colorscale.
 * Works better when the colormap and data have discrete steps.
 *
 * @author borkholder
 */
public class InterpColorMapProgram extends ColorMapProgram
{
    private static final Logger LOGGER = Logger.getLogger( InterpColorMapProgram.class.getName( ) );

    protected GLUniformData discardAboveBelow;

    public InterpColorMapProgram( Axis1D colorAxis, int targetTexUnit, int colorTexUnit ) throws IOException
    {
        super( colorAxis, targetTexUnit, colorTexUnit );
        super.setDiscardNaN( true );
    }

    @Override
    protected void initialize( Axis1D colorAxis, int targetTexUnit, int colorTexUnit )
    {
        super.initialize( colorAxis, targetTexUnit, colorTexUnit );

        this.discardAboveBelow = this.addUniformData( new GLUniformData( "discardAboveBelow", 0 ) );
    }

    public void setDiscardAboveBelow( boolean discard )
    {
        this.discardAboveBelow.setData( discard ? 1 : 0 );
    }

    @Override
    protected void addShaders( )
    {
        this.addVertexShader( "com/metsci/glimpse/core/shaders/colormap/passthrough.vs" );
        this.addFragmentShader( "com/metsci/glimpse/core/shaders/colormap/interp_colorscale_shader.fs" );
    }

    @Override
    public void setDiscardNaN( boolean discard )
    {
        logWarning( LOGGER, "discardNaN has not effect for " + InterpColorMapProgram.class.getSimpleName( ) );
    }
}

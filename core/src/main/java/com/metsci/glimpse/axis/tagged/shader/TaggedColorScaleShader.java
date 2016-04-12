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
package com.metsci.glimpse.axis.tagged.shader;

import static com.metsci.glimpse.axis.tagged.Tag.TEX_COORD_ATTR;
import static com.metsci.glimpse.gl.shader.ShaderType.fragment;

import java.io.IOException;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.gl.shader.Shader;
import com.metsci.glimpse.gl.shader.ShaderArg;
import com.metsci.glimpse.gl.shader.ShaderSource;
import com.metsci.glimpse.util.io.StreamOpener;

public class TaggedColorScaleShader extends Shader implements AxisListener1D
{
    public static final String DEFAULT_SHADER_SOURCE = "shaders/colormap/tagged_colorscale_shader.fs";

    private ShaderArg dataTexUnit;
    private ShaderArg colorTexUnit;

    private ShaderArg vertexCoordTexUnit;
    private ShaderArg textureCoordTexUnit;

    private ShaderArg sizeArg;
    private ShaderArg alphaArg;

    private ShaderArg discardNaN;
    private ShaderArg discardAbove;
    private ShaderArg discardBelow;

    private TaggedAxis1D taggedAxis;

    public TaggedColorScaleShader( TaggedAxis1D axis, int dataTexUnit, int colorTexUnit, int vertexTexUnit, int textureTexUnit, String source ) throws IOException
    {
        super( "tagged_colorscale_shader", fragment, readSource( source ) );

        this.taggedAxis = axis;

        this.dataTexUnit = getArg( "datatex" );
        this.dataTexUnit.setValue( dataTexUnit );

        this.colorTexUnit = getArg( "colortex" );
        this.colorTexUnit.setValue( colorTexUnit );

        this.vertexCoordTexUnit = getArg( "vcoordtex" );
        this.vertexCoordTexUnit.setValue( vertexTexUnit );

        this.textureCoordTexUnit = getArg( "tcoordtex" );
        this.textureCoordTexUnit.setValue( textureTexUnit );

        this.alphaArg = getArg( "alpha" );
        this.alphaArg.setValue( 1.0f );

        this.sizeArg = getArg( "size" );
        this.setSizeArgValue( );

        this.discardNaN = getArg( "discardNaN" );
        discardNaN.setValue( false );

        this.discardAbove = getArg( "discardAbove" );
        discardAbove.setValue( false );

        this.discardBelow = getArg( "discardBelow" );
        discardBelow.setValue( false );

        this.taggedAxis.addAxisListener( this );
    }

    public TaggedColorScaleShader( TaggedAxis1D axis, int dataTexUnit, int colorTexUnit, int vertexTexUnit, int textureTexUnit ) throws IOException
    {
        this( axis, dataTexUnit, colorTexUnit, vertexTexUnit, textureTexUnit, DEFAULT_SHADER_SOURCE );
    }

    private final static ShaderSource readSource( String source ) throws IOException
    {
        return new ShaderSource( source, StreamOpener.fileThenResource );
    }

    @Override
    public void axisUpdated( Axis1D axis )
    {
        setSizeArgValue( );
    }

    protected void setSizeArgValue( )
    {
        List<Tag> tags = taggedAxis.getSortedTags( );
        int size = tags.size( );

        int count = 0;
        for ( int i = size - 1; i >= 0; i-- )
        {
            Tag tag = tags.get( i );

            if ( tag.hasAttribute( TEX_COORD_ATTR ) ) count++;
        }

        this.sizeArg.setValue( count );
    }

    public void setAlpha( float alpha )
    {
        this.alphaArg.setValue( alpha );
    }

    public void setDiscardNaN( boolean discard )
    {
        discardNaN.setValue( discard );
    }

    public void setDiscardAbove( boolean discard )
    {
        discardAbove.setValue( discard );
    }

    public void setDiscardBelow( boolean discard )
    {
        discardBelow.setValue( discard );
    }

    @Override
    public boolean preLink( GL gl, int glProgramHandle )
    {
        return true;
    }

    @Override
    public void preDisplay( GL gl )
    {
    }

    @Override
    public void postDisplay( GL gl )
    {
    }

    @Override
    public void dispose( GLContext context )
    {
        super.dispose( context );
        this.taggedAxis.removeAxisListener( this );
    }

}

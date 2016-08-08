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

import static com.metsci.glimpse.axis.tagged.Tag.*;

import java.io.IOException;
import java.util.List;

import javax.media.opengl.GLContext;
import javax.media.opengl.GLUniformData;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.gl.joglshader.GlimpseShaderProgram;

public class TaggedColorScaleShader extends GlimpseShaderProgram implements AxisListener1D
{
    private GLUniformData dataTexUnit;
    private GLUniformData colorTexUnit;

    private GLUniformData vertexCoordTexUnit;
    private GLUniformData textureCoordTexUnit;

    private GLUniformData sizeArg;
    private GLUniformData alphaArg;

    private GLUniformData discardNaN;
    private GLUniformData discardAbove;
    private GLUniformData discardBelow;

    private TaggedAxis1D taggedAxis;

    public TaggedColorScaleShader( TaggedAxis1D axis, int dataTexUnit, int colorTexUnit, int vertexTexUnit, int textureTexUnit ) throws IOException
    {
        this.addFragmentShader( "shaders/colormap/tagged_colorscale_shader.fs" );

        this.taggedAxis = axis;

        this.dataTexUnit = this.addUniformData( new GLUniformData( "datatex", dataTexUnit ) );
        this.colorTexUnit = this.addUniformData( new GLUniformData( "colortex", colorTexUnit ) );
        this.vertexCoordTexUnit = this.addUniformData( new GLUniformData( "vcoordtex", vertexTexUnit ) );
        this.textureCoordTexUnit = this.addUniformData( new GLUniformData( "tcoordtex", textureTexUnit ) );
        this.alphaArg = this.addUniformData( new GLUniformData( "alpha", 1.0f ) );
        this.sizeArg = this.addUniformData( new GLUniformData( "size", 0 ) );
        this.setSizeArgValue( );
        this.discardNaN = this.addUniformData( new GLUniformData( "discardNaN", 0 ) );
        this.discardAbove = this.addUniformData( new GLUniformData( "discardAbove", 0 ) );
        this.discardBelow = this.addUniformData( new GLUniformData( "discardBelow", 0 ) );

        this.taggedAxis.addAxisListener( this );
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

        this.sizeArg.setData( count );
    }

    public void setAlpha( float alpha )
    {
        this.alphaArg.setData( alpha );
    }

    public void setDiscardNaN( boolean discard )
    {
        discardNaN.setData( discard ? 1 : 0 );
    }

    public void setDiscardAbove( boolean discard )
    {
        discardAbove.setData( discard ? 1 : 0 );
    }

    public void setDiscardBelow( boolean discard )
    {
        discardBelow.setData( discard ? 1 : 0 );
    }

    @Override
    public void dispose( GLContext context )
    {
        super.dispose( context );
        this.taggedAxis.removeAxisListener( this );
    }
}

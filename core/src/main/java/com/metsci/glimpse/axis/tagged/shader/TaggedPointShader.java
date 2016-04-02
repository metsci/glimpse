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

import java.io.IOException;
import java.util.List;

import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.gl.shader.ShaderSource;
import com.metsci.glimpse.support.shader.SimplePointShader;

public class TaggedPointShader extends SimplePointShader
{
    protected TaggedAxis1D taggedColorAxis;
    protected TaggedAxis1D taggedSizeAxis;

    protected AxisListener1D colorAxisListener;
    protected AxisListener1D sizeAxisListener;

    public TaggedPointShader( int colorTextureUnit, int sizeTextureUnit, int colorAttributeIndex, int sizeAttributeIndex, TaggedAxis1D colorAxis, TaggedAxis1D sizeAxis ) throws IOException
    {
        super( colorTextureUnit, sizeTextureUnit, colorAttributeIndex, sizeAttributeIndex, colorAxis, sizeAxis );
    }

    protected TaggedPointShader( int colorTextureUnit, int sizeTextureUnit, int colorAttributeIndex, int sizeAttributeIndex, TaggedAxis1D colorAxis, TaggedAxis1D sizeAxis, ShaderSource... source ) throws IOException
    {
        super( colorTextureUnit, sizeTextureUnit, colorAttributeIndex, sizeAttributeIndex, colorAxis, sizeAxis, source );
    }

    @Override
    protected void initializeShaderValues( int colorTextureUnit, int sizeTextureUnit, int colorAttributeIndex, int sizeAttributeIndex, Axis1D colorAxis, Axis1D sizeAxis )
    {
        this.taggedColorAxis = ( TaggedAxis1D ) colorAxis;
        this.taggedSizeAxis = ( TaggedAxis1D ) sizeAxis;

        this.colorAttributeIndex = colorAttributeIndex;
        this.sizeAttributeIndex = sizeAttributeIndex;

        this.colorTexUnit.setValue( colorTextureUnit );
        this.colorMin.setValue( getMinTag( taggedColorAxis ) );
        this.colorMax.setValue( getMaxTag( taggedColorAxis ) );

        this.colorAxisListener = new AxisListener1D( )
        {
            @Override
            public void axisUpdated( Axis1D handler )
            {
                colorMin.setValue( getMinTag( taggedColorAxis ) );
                colorMax.setValue( getMaxTag( taggedColorAxis ) );
            }
        };

        this.taggedColorAxis.addAxisListener( colorAxisListener );

        this.sizeTexUnit.setValue( sizeTextureUnit );
        this.sizeMin.setValue( getMinTag( taggedSizeAxis ) );
        this.sizeMax.setValue( getMaxTag( taggedSizeAxis ) );

        this.sizeAxisListener = new AxisListener1D( )
        {
            @Override
            public void axisUpdated( Axis1D handler )
            {
                sizeMin.setValue( getMinTag( taggedSizeAxis ) );
                sizeMax.setValue( getMaxTag( taggedSizeAxis ) );
            }
        };

        this.taggedSizeAxis.addAxisListener( sizeAxisListener );

        this.discardAboveColor.setValue( false );
        this.discardBelowColor.setValue( false );
        this.discardAboveSize.setValue( false );
        this.discardBelowSize.setValue( false );

        this.constantColor.setValue( true );
        this.constantSize.setValue( true );
    }

    protected double getMinTag( TaggedAxis1D axis )
    {
        List<Tag> tags = axis.getSortedTags( );
        return tags.get( 0 ).getValue( );
    }

    protected double getMaxTag( TaggedAxis1D axis )
    {
        List<Tag> tags = axis.getSortedTags( );
        return tags.get( tags.size( ) - 1 ).getValue( );
    }

    @Override
    public void dispose( GLContext context )
    {
        super.dispose( context );
        this.taggedColorAxis.removeAxisListener( this.colorAxisListener );
        this.taggedSizeAxis.removeAxisListener( this.sizeAxisListener );
    }
}

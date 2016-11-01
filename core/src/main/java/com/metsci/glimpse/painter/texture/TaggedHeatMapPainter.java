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
package com.metsci.glimpse.painter.texture;

import static com.metsci.glimpse.axis.tagged.Tag.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.texture.FloatTexture1D;
import com.metsci.glimpse.gl.texture.FloatTexture1D.MutatorFloat1D;
import com.metsci.glimpse.support.shader.colormap.ColorMapTaggedProgram;

/**
 * A HeatMapPainter whose coloring is controlled via a
 * {@link com.metsci.glimpse.axis.tagged.TaggedAxis1D}.
 *
 * @author ulman
 */
public class TaggedHeatMapPainter extends HeatMapPainter implements AxisListener1D
{
    protected static final int DEFAULT_DATA_COORD_UNIT = 2;
    protected static final int DEFAULT_TEX_COORD_UNIT = 3;

    protected FloatTexture1D vertexCoordTex;
    protected FloatTexture1D textureCoordTex;

    protected TaggedAxis1D taggedAxis;

    public TaggedHeatMapPainter( TaggedAxis1D taggedAxis )
    {
        super( taggedAxis );

        this.taggedAxis = taggedAxis;
        this.updateTextureArrays( );
        this.taggedAxis.addAxisListener( this );
    }

    @Override
    protected void loadDefaultPipeline( Axis1D axis ) throws IOException
    {
        this.program = new ColorMapTaggedProgram( ( TaggedAxis1D ) axis, DEFAULT_DRAWABLE_TEXTURE_UNIT, DEFAULT_NONDRAWABLE_TEXTURE_UNIT, DEFAULT_DATA_COORD_UNIT, DEFAULT_TEX_COORD_UNIT );

        this.setProgram( this.program );
    }

    private ColorMapTaggedProgram getProgram( )
    {
        return ( ColorMapTaggedProgram ) this.program;
    }

    @Override
    public void setAlpha( float alpha )
    {
        painterLock.lock( );
        try
        {
            getProgram( ).setAlpha( alpha );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    @Override
    public void setDiscardNaN( boolean discard )
    {
        painterLock.lock( );
        try
        {
            getProgram( ).setDiscardNaN( discard );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setDiscardAbove( boolean discard )
    {
        painterLock.lock( );
        try
        {
            getProgram( ).setDiscardAbove( discard );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    public void setDiscardBelow( boolean discard )
    {
        painterLock.lock( );
        try
        {
            getProgram( ).setDiscardBelow( discard );
        }
        finally
        {
            painterLock.unlock( );
        }
    }

    @Override
    public void axisUpdated( Axis1D axis )
    {
        updateTextureArrays( );
    }

    protected void updateTextureArrays( )
    {
        final List<Tag> tags = taggedAxis.getSortedTags( );

        int count = 0;
        for ( Tag tag : tags )
        {
            if ( tag.hasAttribute( TEX_COORD_ATTR ) ) count++;
        }

        if ( vertexCoordTex == null || vertexCoordTex.getDimensionSize( 0 ) != count )
        {
            this.removeNonDrawableTexture( vertexCoordTex );
            this.vertexCoordTex = new FloatTexture1D( count );
            this.addNonDrawableTexture( vertexCoordTex, DEFAULT_DATA_COORD_UNIT );
        }

        vertexCoordTex.mutate( new MutatorFloat1D( )
        {
            @Override
            public void mutate( FloatBuffer data, int n0 )
            {
                int size = Math.min( tags.size( ), n0 );

                for ( int i = size - 1; i >= 0; i-- )
                {
                    Tag tag = tags.get( i );

                    if ( tag.hasAttribute( TEX_COORD_ATTR ) )
                    {
                        data.put( ( float ) tag.getValue( ) );
                    }
                }
            }
        } );

        if ( textureCoordTex == null || textureCoordTex.getDimensionSize( 0 ) != count )
        {
            this.removeNonDrawableTexture( textureCoordTex );
            this.textureCoordTex = new FloatTexture1D( count );
            this.addNonDrawableTexture( textureCoordTex, DEFAULT_TEX_COORD_UNIT );
        }

        textureCoordTex.mutate( new MutatorFloat1D( )
        {
            @Override
            public void mutate( FloatBuffer data, int n0 )
            {
                int size = Math.min( tags.size( ), n0 );

                for ( int i = size - 1; i >= 0; i-- )
                {
                    Tag tag = tags.get( i );

                    if ( tag.hasAttribute( TEX_COORD_ATTR ) )
                    {
                        data.put( tag.getAttributeFloat( TEX_COORD_ATTR ) );
                    }
                }
            }
        } );
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        super.doDispose( context );

        this.taggedAxis.removeAxisListener( this );
    }
}

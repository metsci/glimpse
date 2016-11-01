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
package com.metsci.glimpse.axis.tagged.painter;

import static com.metsci.glimpse.axis.tagged.Tag.*;
import static javax.media.opengl.GL.*;

import java.nio.FloatBuffer;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;

/**
 * A vertical (y) axis painter which displays positions of tags in addition
 * to tick marks and labels. A color scale is also displayed which stretches
 * between specified tags.<p>
 *
 * @author ulman
 * @see TaggedPartialColorXAxisPainter
 */
public class TaggedPartialColorYAxisPainter extends TaggedColorYAxisPainter
{
    protected GLEditableBuffer vertexCoords;
    protected GLEditableBuffer textureCoords;

    public TaggedPartialColorYAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );

        this.vertexCoords = new GLEditableBuffer( GL_DYNAMIC_DRAW, 0 );
        this.textureCoords = new GLEditableBuffer( GL_DYNAMIC_DRAW, 0 );
    }

    @Override
    protected void paintColorScale( GlimpseContext context )
    {
        Axis1D axis = getAxis1D( context );

        if ( colorTexture != null && axis instanceof TaggedAxis1D )
        {
            TaggedAxis1D taggedAxis = ( TaggedAxis1D ) axis;
            GlimpseBounds bounds = getBounds( context );
            GL3 gl = context.getGL( ).getGL3( );

            int height = bounds.getHeight( );
            int width = bounds.getWidth( );

            int count = updateCoordinateBuffers( gl, taggedAxis, width, height );

            float x1 = getColorBarMinX( width );
            float x2 = getColorBarMaxX( width );

            float inset_PX = 0.5f * style.thickness_PX;

            pathOutline.clear( );
            pathOutline.addRectangle( x1, inset_PX, x2, height - inset_PX );

            GLUtils.enableStandardBlending( gl );
            try
            {
                if ( count > 0 )
                {
                    // draw color scale
                    progTex.begin( context );
                    try
                    {
                        progTex.setPixelOrtho( context, bounds );

                        progTex.draw( context, GL_TRIANGLES, colorTexture, vertexCoords, textureCoords, 0, count );
                    }
                    finally
                    {
                        progTex.end( context );
                    }
                }

                // draw outline box
                progOutline.begin( gl );
                try
                {
                    progOutline.setPixelOrtho( gl, bounds );
                    progOutline.setViewport( gl, bounds );

                    progOutline.draw( gl, style, pathOutline );
                }
                finally
                {
                    progOutline.end( gl );
                }
            }
            finally
            {
                gl.glDisable( GL.GL_BLEND );
            }
        }
    }

    protected int updateCoordinateBuffers( GL gl, TaggedAxis1D taggedAxis, int width, int height )
    {
        List<Tag> tags = taggedAxis.getSortedTags( );
        int size = tags.size( );

        if ( size <= 1 ) return 0;

        vertexCoords.clear( );
        textureCoords.clear( );
        FloatBuffer v = vertexCoords.growFloats( 12 * ( size - 1 ) );
        FloatBuffer t = textureCoords.growFloats( 6 * ( size - 1 ) );

        float x1 = getColorBarMinX( width );
        float x2 = getColorBarMaxX( width );

        float prevVertexCoord = 0;
        float prevTextureCoord = 0;
        boolean init = false;

        int count = 0;
        for ( Tag tag : tags )
        {
            if ( tag.hasAttribute( TEX_COORD_ATTR ) )
            {
                float textureCoord = tag.getAttributeFloat( TEX_COORD_ATTR );
                float vertexCoord = taggedAxis.valueToScreenPixel( tag.getValue( ) );

                if ( init )
                {
                    v.put( x1 ).put( prevVertexCoord );
                    v.put( x2 ).put( vertexCoord );
                    v.put( x1 ).put( vertexCoord );

                    v.put( x2 ).put( vertexCoord );
                    v.put( x2 ).put( prevVertexCoord );
                    v.put( x1 ).put( prevVertexCoord );

                    t.put( prevTextureCoord );
                    t.put( textureCoord );
                    t.put( textureCoord );

                    t.put( textureCoord );
                    t.put( prevTextureCoord );
                    t.put( prevTextureCoord );

                    count += 6;
                }

                prevVertexCoord = vertexCoord;
                prevTextureCoord = textureCoord;
                init = true;
            }
        }

        return count;
    }
}

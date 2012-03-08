/*
 * Copyright (c) 2012, Metron, Inc.
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

import static com.metsci.glimpse.support.color.GlimpseColor.getBlack;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.ColorXAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * A horizontal (x) axis painter which displays positions of tags on
 * top of a color scale. This axis must be added to a
 * {@link com.metsci.glimpse.layout.GlimpseAxisLayout1D} whose associated
 * axis is a {@link com.metsci.glimpse.axis.tagged.TaggedAxis1D}.
 *
 * @author ulman
 */
public class TaggedColorXAxisPainter extends ColorXAxisPainter
{
    protected static final float DEFAULT_TAG_POINTER_OUTLINE_WIDTH = 2;
    protected static final int DEFAULT_TAG_POINTER_HEIGHT = 7;
    protected static final int DEFAULT_TAG_HEIGHT = 15;
    protected static final int DEFAULT_TAG_HALFBASE = 5;

    protected float[] tagColor = GlimpseColor.fromColorRgba( 0.0f, 0.0f, 0.0f, 0.2f );
    protected int tagHalfWidth = DEFAULT_TAG_HALFBASE;
    protected int tagHeight = DEFAULT_TAG_HEIGHT;
    protected int tagPointerHeight = DEFAULT_TAG_POINTER_HEIGHT;
    protected float tagPointerOutlineWidth = DEFAULT_TAG_POINTER_OUTLINE_WIDTH;

    public TaggedColorXAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );

        this.tickBufferSize = 10;
    }

    public void setTagColor( float[] color )
    {
        this.tagColor = color;
    }

    public void setTagHalfWidth( int halfWidth )
    {
        this.tagHalfWidth = halfWidth;
    }

    public void setTagHeight( int height )
    {
        this.tagHeight = height;
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        if ( axis instanceof TaggedAxis1D )
        {
            TaggedAxis1D taggedAxis = (TaggedAxis1D) axis;

            GL gl = context.getGL( );

            int width = bounds.getWidth( );
            int height = bounds.getHeight( );

            gl.glMatrixMode( GL.GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( -0.5, width - 1 + 0.5f, -0.5, height - 1 + 0.5f, -1, 1 );

            gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
            gl.glEnable( GL.GL_BLEND );

            paintColorScale( gl, taggedAxis, width, height );

            paintTicks( gl, taggedAxis, width, height );
            paintSelectionLine( gl, taggedAxis, width, height );

            paintTags( gl, taggedAxis, width, height );
        }
    }

    protected void paintTags( GL gl, TaggedAxis1D taggedAxis, int width, int height )
    {
        for ( Tag tag : taggedAxis.getSortedTags( ) )
        {
            paintTag( gl, tag, taggedAxis, width, height );
        }
    }

    protected void paintTag( GL gl, Tag tag, TaggedAxis1D taggedAxis, int width, int height )
    {
        int x = taggedAxis.valueToScreenPixel( tag.getValue( ) );
        int yMin = getTagMinY( height );
        int yMid = getTagPointerMaxY( height );
        int yMax = getTagMaxY( height );

        GlimpseColor.glColor( gl, tagColor );
        gl.glBegin( GL.GL_TRIANGLES );
        try
        {
            gl.glVertex2f( x, yMin );
            gl.glVertex2f( x - tagHalfWidth, yMid );
            gl.glVertex2f( x + tagHalfWidth, yMid );
        }
        finally
        {
            gl.glEnd( );
        }

        gl.glBegin( GL.GL_QUADS );
        try
        {
            gl.glVertex2f( x - tagHalfWidth, yMax );
            gl.glVertex2f( x - tagHalfWidth, yMid );
            gl.glVertex2f( x + tagHalfWidth, yMid );
            gl.glVertex2f( x + tagHalfWidth, yMax );
        }
        finally
        {
            gl.glEnd( );
        }

        GlimpseColor.glColor( gl, getBlack( ), 1f );
        gl.glLineWidth( tagPointerOutlineWidth );
        gl.glEnable( GL.GL_LINE_SMOOTH );
        gl.glBegin( GL.GL_LINE_LOOP );
        try
        {
            gl.glVertex2f( x, yMin );
            gl.glVertex2f( x + tagHalfWidth, yMid );
            gl.glVertex2f( x + tagHalfWidth, yMax );
            gl.glVertex2f( x - tagHalfWidth, yMax );
            gl.glVertex2f( x - tagHalfWidth, yMid );
            gl.glVertex2f( x, yMin );
        }
        finally
        {
            gl.glEnd( );
        }
    }

    public int getTagMinY( int height )
    {
        return height - 1 - tickBufferSize - colorBarSize;
    }

    public int getTagMaxY( int height )
    {
        return getTagMinY( height ) + tagHeight;
    }

    public int getTagPointerMaxY( int height )
    {
        return getTagMinY( height ) + tagPointerHeight;
    }
}

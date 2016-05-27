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

import javax.media.opengl.GL2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.ColorXAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

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
    protected boolean tagColorSet = false;

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
        this.tagColorSet = true;
    }

    public void setTagHalfWidth( int halfWidth )
    {
        this.tagHalfWidth = halfWidth;
    }

    public void setTagHeight( int height )
    {
        this.tagHeight = height;
    }

    public void setTagPointerHeight( int height )
    {
        this.tagPointerHeight = height;
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        super.setLookAndFeel( laf );

        if ( !tagColorSet )
        {
            setTagColor( laf.getColor( AbstractLookAndFeel.AXIS_TAG_COLOR ) );
            tagColorSet = false;
        }
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        updateTextRenderer( );
        if ( textRenderer == null ) return;

        if ( axis instanceof TaggedAxis1D )
        {
            TaggedAxis1D taggedAxis = ( TaggedAxis1D ) axis;

            GL2 gl = context.getGL( ).getGL2( );

            int width = bounds.getWidth( );
            int height = bounds.getHeight( );

            gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
            gl.glEnable( GL2.GL_BLEND );

            gl.glMatrixMode( GL2.GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( -0.5, width - 1 + 0.5f, -0.5, height - 1 + 0.5f, -1, 1 );

            paintColorScale( gl, taggedAxis, width, height );

            gl.glMatrixMode( GL2.GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( axis.getMin( ), axis.getMax( ), -0.5, height - 1 + 0.5f, -1, 1 );

            paintTicks( gl, taggedAxis, width, height );
            paintAxisLabel( gl, axis, width, height );
            paintSelectionLine( gl, taggedAxis, width, height );

            gl.glMatrixMode( GL2.GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( -0.5, width - 1 + 0.5f, -0.5, height - 1 + 0.5f, -1, 1 );

            paintTags( gl, taggedAxis, width, height );
        }
    }

    protected void paintTags( GL2 gl, TaggedAxis1D taggedAxis, int width, int height )
    {
        for ( Tag tag : taggedAxis.getSortedTags( ) )
        {
            paintTag( gl, tag, taggedAxis, width, height );
        }
    }

    protected void paintTag( GL2 gl, Tag tag, TaggedAxis1D taggedAxis, int width, int height )
    {
        int x = taggedAxis.valueToScreenPixel( tag.getValue( ) );
        int yMin = getTagMinY( height );
        int yMid = getTagPointerMaxY( height );
        int yMax = getTagMaxY( height );

        float[] color = tagColor;
        Object colorValue = tag.getAttribute( Tag.TAG_COLOR_ATTR );
        if ( colorValue != null && colorValue instanceof float[] )
        {
            color = ( float[] ) colorValue;
        }

        GlimpseColor.glColor( gl, color, 0.2f );
        gl.glBegin( GL2.GL_TRIANGLES );
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

        gl.glBegin( GL2.GL_QUADS );
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

        GlimpseColor.glColor( gl, color, 1f );
        gl.glLineWidth( tagPointerOutlineWidth );
        gl.glEnable( GL2.GL_LINE_SMOOTH );
        gl.glBegin( GL2.GL_LINE_LOOP );
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

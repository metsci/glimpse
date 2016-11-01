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

import static com.jogamp.common.nio.Buffers.*;
import static javax.media.opengl.GL.*;

import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.ColorYAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

/**
 * A vertical (y) axis painter which displays positions of tags on
 * top of a color scale. This axis must be added to a
 * {@link com.metsci.glimpse.layout.GlimpseAxisLayout1D} whose associated
 * axis is a {@link com.metsci.glimpse.axis.tagged.TaggedAxis1D}.
 *
 * @author ulman
 */
public class TaggedColorYAxisPainter extends ColorYAxisPainter
{
    protected static final float DEFAULT_TAG_POINTER_OUTLINE_WIDTH = 2;
    protected static final int DEFAULT_TAG_POINTER_HEIGHT = 7;
    protected static final int DEFAULT_TAG_HEIGHT = 15;
    protected static final int DEFAULT_TAG_HALFBASE = 5;

    protected float[] tagColor;
    protected boolean tagColorSet = false;

    protected FlatColorProgram flatColorProg;
    protected GLEditableBuffer tagXyVbo;
    protected LineStyle tagStyle;

    protected int tagHalfWidth = DEFAULT_TAG_HALFBASE;
    protected int tagHeight = DEFAULT_TAG_HEIGHT;
    protected int tagPointerHeight = DEFAULT_TAG_POINTER_HEIGHT;
    protected float tagPointerOutlineWidth = DEFAULT_TAG_POINTER_OUTLINE_WIDTH;

    public TaggedColorYAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );

        this.tickBufferSize = 10;

        this.tagXyVbo = new GLEditableBuffer( GL_DYNAMIC_DRAW, 18 * SIZEOF_FLOAT );

        this.setTagColor0( GlimpseColor.fromColorRgba( 0.0f, 0.0f, 0.0f, 0.2f ) );

        this.tagStyle = new LineStyle( );
        this.tagStyle.stippleEnable = false;
        this.tagStyle.feather_PX = 0.9f;
    }

    public void setTagColor( float[] color )
    {
        this.setTagColor0( color );
        this.tagColorSet = true;
    }

    protected void setTagColor0( float[] color )
    {
        this.tagColor = Arrays.copyOf( color, 4 );
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
    public void doPaintTo( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );
        Axis1D axis = getAxis1D( context );
        GlimpseBounds bounds = getBounds( context );

        updateTextRenderer( );
        if ( textRenderer == null ) return;

        TickInfo info = getTickInfo( axis, bounds );

        paintColorScale( context );
        paintTicks( gl, axis, bounds, info );
        paintTickLabels( gl, axis, bounds, info );
        paintAxisLabel( gl, axis, bounds );
        paintSelectionLine( gl, axis, bounds );

        if ( axis instanceof TaggedAxis1D )
        {
            TaggedAxis1D taggedAxis = ( TaggedAxis1D ) axis;
            paintTags( gl, taggedAxis, bounds );
        }
    }

    @Override
    protected void initShaders( )
    {
        super.initShaders( );

        this.flatColorProg = new FlatColorProgram( );
    }

    protected void paintTags( GL gl, TaggedAxis1D taggedAxis, GlimpseBounds bounds )
    {
        for ( Tag tag : taggedAxis.getSortedTags( ) )
        {
            paintTag( gl, tag, taggedAxis, bounds );
        }
    }

    protected void paintTag( GL gl, Tag tag, TaggedAxis1D taggedAxis, GlimpseBounds bounds )
    {
        int width = bounds.getWidth( );
        int y = taggedAxis.valueToScreenPixel( tag.getValue( ) );
        int xMin = getTagMinX( width );
        int xMid = getTagPointerMaxX( width );
        int xMax = getTagMaxX( width );

        float[] color = tagColor;
        Object colorValue = tag.getAttribute( Tag.TAG_COLOR_ATTR );
        if ( colorValue != null && colorValue instanceof float[] )
        {
            color = ( float[] ) colorValue;
        }

        GL3 gl3 = gl.getGL3( );

        FloatBuffer xy = tagXyVbo.editFloats( 0, 18 );

        xy.put( xMin ).put( y );
        xy.put( xMid ).put( y - tagHalfWidth );
        xy.put( xMid ).put( y + tagHalfWidth );

        xy.put( xMax ).put( y - tagHalfWidth );
        xy.put( xMid ).put( y - tagHalfWidth );
        xy.put( xMid ).put( y + tagHalfWidth );

        xy.put( xMid ).put( y + tagHalfWidth );
        xy.put( xMax ).put( y + tagHalfWidth );
        xy.put( xMax ).put( y - tagHalfWidth );

        GLUtils.enableStandardBlending( gl );
        try
        {
            flatColorProg.begin( gl3 );
            try
            {
                flatColorProg.setPixelOrtho( gl3, bounds );
                flatColorProg.setColor( gl3, color[0], color[1], color[2], 0.3f );

                flatColorProg.draw( gl3, GL.GL_TRIANGLES, tagXyVbo, 0, 9 );
            }
            finally
            {
                flatColorProg.end( gl3 );
            }

            tagStyle.rgba = color;
            tagStyle.rgba[3] = 1.0f;
            tagStyle.thickness_PX = tagPointerOutlineWidth;

            pathOutline.clear( );
            pathOutline.moveTo( xMin, y );
            pathOutline.lineTo( xMid, y + tagHalfWidth );
            pathOutline.lineTo( xMax, y + tagHalfWidth );
            pathOutline.lineTo( xMax, y - tagHalfWidth );
            pathOutline.lineTo( xMid, y - tagHalfWidth );
            pathOutline.closeLoop( );

            progOutline.begin( gl3 );
            try
            {
                progOutline.setPixelOrtho( gl3, bounds );
                progOutline.setViewport( gl3, bounds );

                progOutline.draw( gl3, tagStyle, pathOutline );
            }
            finally
            {
                progOutline.end( gl3 );
            }
        }
        finally
        {
            GLUtils.disableBlending( gl3 );
        }
    }

    public int getTagMinX( int width )
    {
        return width - tickBufferSize - colorBarSize;
    }

    public int getTagMaxX( int width )
    {
        return getTagMinX( width ) + tagHeight;
    }

    public int getTagPointerMaxX( int width )
    {
        return getTagMinX( width ) + tagPointerHeight;
    }
}

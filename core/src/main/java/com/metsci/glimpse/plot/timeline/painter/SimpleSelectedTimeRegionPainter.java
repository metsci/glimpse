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
package com.metsci.glimpse.plot.timeline.painter;

import java.awt.Font;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter1D;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;

public class SimpleSelectedTimeRegionPainter extends GlimpsePainter1D
{
    protected float[] selectionFillColor = GlimpseColor.fromColorRgba( 0, 153, 204, 40 );
    protected float[] selectionBorderColor = GlimpseColor.fromColorRgba( 0, 51, 255, 255 );
    protected float[] currentTimeMarkerColor = GlimpseColor.fromColorRgba( 0, 51, 255, 255 );

    protected boolean showCurrenTimeMarker = true;
    protected float currentTimeMarkerWidth = 3.0f;

    protected TextRenderer textRenderer;
    protected Font font;

    protected Orientation orientation;

    public SimpleSelectedTimeRegionPainter( Orientation orientation )
    {
        super( );

        this.orientation = orientation;
        this.font = FontUtils.getDefaultBold( 12 );
        this.textRenderer = new TextRenderer( font );
    }

    public void setSelectionFillColor( float[] color )
    {
        this.selectionFillColor = color;
    }

    public void setSelectionBorderColor( float[] color )
    {
        this.selectionBorderColor = color;
    }

    public void setCurrenTimeMarkerColor( float[] color )
    {
        this.currentTimeMarkerColor = color;
    }

    public void setShowCurrentTimeMarker( boolean show )
    {
        this.showCurrenTimeMarker = show;
    }

    public void setCurrentTimeMarkerWidth( float width )
    {
        this.currentTimeMarkerWidth = width;
    }

    protected void paint( GL2 gl, TaggedAxis1D taggedAxis, List<Tag> tags, float min, float max, float current, int width, int height )
    {
        GlimpseColor.glColor( gl, selectionFillColor );

        gl.glBegin( GL2.GL_QUADS );
        try
        {
            if ( orientation == Orientation.VERTICAL )
            {
                gl.glVertex2f( min, 0 );
                gl.glVertex2f( min, height - 1 );
                gl.glVertex2f( max, height - 1 );
                gl.glVertex2f( max, 0 );
            }
            else
            {
                gl.glVertex2f( 0, min );
                gl.glVertex2f( width, min );
                gl.glVertex2f( width, max );
                gl.glVertex2f( 0, max );
            }
        }
        finally
        {
            gl.glEnd( );
        }

        GlimpseColor.glColor( gl, selectionBorderColor );

        gl.glBegin( GL2.GL_LINE_LOOP );
        try
        {
            if ( orientation == Orientation.VERTICAL )
            {
                gl.glVertex2f( min, 0 );
                gl.glVertex2f( min, height - 1 );
                gl.glVertex2f( max, height - 1 );
                gl.glVertex2f( max, 0 );
            }
            else
            {
                gl.glVertex2f( 0, min );
                gl.glVertex2f( width, min );
                gl.glVertex2f( width, max );
                gl.glVertex2f( 0, max );
            }
        }
        finally
        {
            gl.glEnd( );
        }

        if ( this.showCurrenTimeMarker )
        {
            GlimpseColor.glColor( gl, currentTimeMarkerColor );
            gl.glLineWidth( currentTimeMarkerWidth );

            gl.glBegin( GL2.GL_LINES );
            try
            {
                if ( orientation == Orientation.VERTICAL )
                {
                    gl.glVertex2f( current, 0 );
                    gl.glVertex2f( current, height );
                }
                else
                {
                    gl.glVertex2f( 0, current );
                    gl.glVertex2f( width, current );
                }
            }
            finally
            {
                gl.glEnd( );
            }
        }
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        if ( textRenderer == null ) return;

        GL2 gl = context.getGL( ).getGL2( );

        TaggedAxis1D taggedAxis = ( TaggedAxis1D ) axis;

        int height = bounds.getHeight( );
        int width = bounds.getWidth( );

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );

        if ( orientation == Orientation.VERTICAL )
        {
            gl.glOrtho( taggedAxis.getMin( ), taggedAxis.getMax( ), -0.5, height - 1 + 0.5f, -1, 1 );
        }
        else
        {
            gl.glOrtho( -0.5, width - 1 + 0.5f, taggedAxis.getMin( ), taggedAxis.getMax( ), -1, 1 );
        }

        gl.glBlendFunc( GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL2.GL_BLEND );

        Tag minTag = taggedAxis.getTag( StackedTimePlot2D.MIN_TIME );
        Tag maxTag = taggedAxis.getTag( StackedTimePlot2D.MAX_TIME );
        Tag currentTag = taggedAxis.getTag( StackedTimePlot2D.CURRENT_TIME );

        if ( minTag == null || maxTag == null || currentTag == null ) return;

        List<Tag> tags = taggedAxis.getSortedTags( );

        float min = ( float ) minTag.getValue( );
        float current = ( float ) currentTag.getValue( );
        float max = ( float ) maxTag.getValue( );

        paint( gl, taggedAxis, tags, min, max, current, width, height );
    }
}

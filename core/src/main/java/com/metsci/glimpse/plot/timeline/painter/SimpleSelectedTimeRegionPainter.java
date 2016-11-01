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

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

public class SimpleSelectedTimeRegionPainter extends GlimpsePainterBase
{
    protected float[] selectionFillColor = GlimpseColor.fromColorRgba( 0, 153, 204, 40 );
    protected float[] selectionBorderColor = GlimpseColor.fromColorRgba( 0, 51, 255, 255 );
    protected float[] currentTimeMarkerColor = GlimpseColor.fromColorRgba( 0, 51, 255, 255 );

    protected boolean showCurrenTimeMarker = true;
    protected float currentTimeMarkerWidth = 3.0f;

    protected TextRenderer textRenderer;
    protected Font font;

    protected Orientation orientation;

    protected LineProgram lineProg;
    protected LinePath linePath;
    protected LineStyle lineStyle;

    protected FlatColorProgram fillProg;
    protected GLEditableBuffer fillPath;

    public SimpleSelectedTimeRegionPainter( Orientation orientation )
    {
        super( );

        this.orientation = orientation;
        this.font = FontUtils.getDefaultBold( 12 );
        this.textRenderer = new TextRenderer( font );

        this.lineProg = new LineProgram( );
        this.linePath = new LinePath( );
        this.lineStyle = new LineStyle( );
        this.lineStyle.joinType = LineJoinType.JOIN_NONE;
        this.lineStyle.feather_PX = 0;
        this.lineStyle.thickness_PX = 1;
        this.lineStyle.stippleEnable = false;

        this.fillProg = new FlatColorProgram( );
        this.fillPath = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
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

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        if ( textRenderer == null ) return;

        GlimpseBounds bounds = getBounds( context );
        Axis1D axis = requireAxis1D( context );
        GL3 gl = context.getGL( ).getGL3( );

        TaggedAxis1D taggedAxis = ( TaggedAxis1D ) axis;

        int height = bounds.getHeight( );
        int width = bounds.getWidth( );

        Tag minTag = taggedAxis.getTag( StackedTimePlot2D.MIN_TIME );
        Tag maxTag = taggedAxis.getTag( StackedTimePlot2D.MAX_TIME );
        Tag currentTag = taggedAxis.getTag( StackedTimePlot2D.CURRENT_TIME );

        if ( minTag == null || maxTag == null || currentTag == null ) return;

        List<Tag> tags = taggedAxis.getSortedTags( );

        float min = ( float ) minTag.getValue( );
        float current = ( float ) currentTag.getValue( );
        float max = ( float ) maxTag.getValue( );

        GLUtils.enableStandardBlending( gl );
        try
        {
            paint( context, taggedAxis, tags, min, max, current, width, height );
        }
        finally
        {
            GLUtils.disableBlending( gl );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        GL3 gl = context.getGL( ).getGL3( );

        this.fillPath.dispose( gl );
        this.linePath.dispose( gl );
        this.fillProg.dispose( gl );
        this.lineProg.dispose( gl );

        this.textRenderer.dispose( );
    }

    protected void paint( GlimpseContext context, TaggedAxis1D taggedAxis, List<Tag> tags, float min, float max, float current, int width, int height )
    {
        GL3 gl = context.getGL( ).getGL3( );
        GlimpseBounds bounds = getBounds( context );

        fillProg.begin( gl );
        try
        {
            fillPath.clear( );

            if ( orientation == Orientation.VERTICAL )
            {
                fillProg.setOrtho( gl, ( float ) taggedAxis.getMin( ), ( float ) taggedAxis.getMax( ), 0, height );
                fillPath.growQuad2f( min, 0, max, height - 1 );
            }
            else
            {
                fillProg.setOrtho( gl, 0, width, ( float ) taggedAxis.getMin( ), ( float ) taggedAxis.getMax( ) );
                fillPath.growQuad2f( 0, min, width, max );
            }

            fillProg.draw( gl, fillPath, selectionFillColor );
        }
        finally
        {
            fillProg.end( gl );
        }

        lineProg.begin( gl );
        try
        {
            lineProg.setViewport( gl, bounds );

            linePath.clear( );

            if ( orientation == Orientation.VERTICAL )
            {
                lineProg.setOrtho( gl, ( float ) taggedAxis.getMin( ), ( float ) taggedAxis.getMax( ), 0, height );

                linePath.addRectangle( min, 0, max, height - 1 );
            }
            else
            {
                lineProg.setOrtho( gl, ( float ) taggedAxis.getMin( ), ( float ) taggedAxis.getMax( ), 0, height );

                linePath.addRectangle( 0, min, width, max );
            }

            lineStyle.rgba = selectionBorderColor;
            lineStyle.thickness_PX = 1.0f;

            lineProg.draw( gl, lineStyle, linePath );

            if ( this.showCurrenTimeMarker )
            {
                lineStyle.rgba = currentTimeMarkerColor;
                lineStyle.thickness_PX = currentTimeMarkerWidth;

                linePath.clear( );

                if ( orientation == Orientation.VERTICAL )
                {
                    linePath.moveTo( current, 0 );
                    linePath.lineTo( current, height );
                }
                else
                {
                    linePath.moveTo( 0, current );
                    linePath.lineTo( width, current );
                }

                lineProg.draw( gl, lineStyle, linePath );
            }
        }
        finally
        {
            lineProg.end( gl );
        }
    }
}

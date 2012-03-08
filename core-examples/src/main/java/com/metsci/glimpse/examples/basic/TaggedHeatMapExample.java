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
package com.metsci.glimpse.examples.basic;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.painter.NumericAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.tagged.NamedConstraint;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.axis.tagged.painter.TaggedPartialColorYAxisPainter;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.info.CursorTextZPainter;
import com.metsci.glimpse.painter.texture.TaggedHeatMapPainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.projection.FlatProjection;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;

import static com.metsci.glimpse.axis.tagged.Tag.*;

/**
 * A variant of the basic HeatMapExample with tagged axes for controlling the color scale.
 *
 * @author ulman
 * @see HeatMapExample
 */
public class TaggedHeatMapExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new TaggedHeatMapExample( ) );
    }

    TaggedHeatMapPainter heatmap;
    
    @Override
    public ColorAxisPlot2D getLayout( )
    {
        // create a heat map plot with three custom modifications:
        // 1) Use a TaggedAxis1D for the z axis, allowing the addition of custom, draggable tag points
        // 2) Use a MouseAdapter which knows about tagged axes for the z axis
        // 3) Use a painter which knows about tagged axes for the z axis painter
        ColorAxisPlot2D plot = new ColorAxisPlot2D( )
        {
            @Override
            protected Axis1D createAxisZ( )
            {
                return new TaggedAxis1D( );
            }

            @Override
            protected AxisMouseListener createAxisMouseListenerZ( )
            {
                return new TaggedAxisMouseListener1D( );
            }

            @Override
            protected NumericAxisPainter createAxisPainterZ( AxisLabelHandler tickHandler )
            {
                return new TaggedPartialColorYAxisPainter( tickHandler );
            }
        };

        // get the tagged z axis
        TaggedAxis1D axisZ = ( TaggedAxis1D ) plot.getAxisZ( );

        // add some named tags at specific points along the axis
        // also add a custom "attribute" to each tag which specifies the relative (0 to 1)
        // point along the color scale which the tag is attached to
        final Tag t1 = axisZ.addTag( "T1", 50.0 ).setAttribute( TEX_COORD_ATTR, 0.0f );
        final Tag t2 = axisZ.addTag( "T2", 300.0 ).setAttribute( TEX_COORD_ATTR, 0.3f );
        final Tag t3 = axisZ.addTag( "T3", 500.0 ).setAttribute( TEX_COORD_ATTR, 0.6f );
        final Tag t4 = axisZ.addTag( "T4", 600.0 ).setAttribute( TEX_COORD_ATTR, 0.8f );
        final Tag t5 = axisZ.addTag( "T5", 800.0 ).setAttribute( TEX_COORD_ATTR, 1.0f );

        // add a constraint which prevents dragging the tags past one another
        axisZ.addConstraint( new NamedConstraint( "C1" )
        {
            final static double buffer = 1.0;

            @Override
            public void applyConstraint( TaggedAxis1D axis )
            {
                if ( t4.getValue( ) > t5.getValue( ) - buffer ) t4.setValue( t5.getValue( ) - buffer );

                if ( t3.getValue( ) > t4.getValue( ) - buffer ) t3.setValue( t4.getValue( ) - buffer );

                if ( t2.getValue( ) > t3.getValue( ) - buffer ) t2.setValue( t3.getValue( ) - buffer );

                if ( t1.getValue( ) > t2.getValue( ) - buffer ) t1.setValue( t2.getValue( ) - buffer );
            }
        } );

        // set border and offset sizes in pixels
        plot.setBorderSize( 15 );
        plot.setAxisSizeX( 30 );
        plot.setAxisSizeY( 40 );
        plot.setTitleHeight( 0 );
        
        // set the x, y, and z initial axis bounds
        plot.setMinX( 0.0f );
        plot.setMaxX( 1000.0f );

        plot.setMinY( 0.0f );
        plot.setMaxY( 1000.0f );

        plot.setMinZ( 0.0f );
        plot.setMaxZ( 1000.0f );

        // lock the aspect ratio of the x and y axis to 1 to 1
        plot.lockAspectRatioXY( 1.0f );

        // set the size of the selection box to 100.0 units
        plot.setSelectionSize( 100.0f );

        // generate some data to display
        double[][] data = HeatMapExample.generateData( 1000, 1000 );

        // generate a projection indicating how the data should be mapped to plot coordinates
        Projection projection = new FlatProjection( 0, 1000, 0, 1000 );

        // create an OpenGL texture wrapper object
        FloatTextureProjected2D texture = new FloatTextureProjected2D( 1000, 1000 );

        // load the data and projection into the texture
        texture.setProjection( projection );
        texture.setData( data );

        // setup the color map for the painter
        ColorTexture1D colors = new ColorTexture1D( 1024 );
        colors.setColorGradient( ColorGradients.jet );

        // create a painter to display the heatmap data
        // this heatmap painter knows about axis tags
        heatmap = new TaggedHeatMapPainter( axisZ );
        heatmap.setDiscardAbove( true );
        heatmap.setDiscardBelow( true );

        // add the heatmap data and color scale to the painter
        heatmap.setData( texture );
        heatmap.setColorScale( colors );

        // add the painter to the plot
        plot.addPainter( heatmap );

        // load the color map into the plot (so the color scale is displayed on the z axis)
        plot.setColorScale( colors );

        // add the painter to the plot
        plot.addPainter( heatmap );

        // create a painter which displays the cursor position and data value under the cursor
        CursorTextZPainter cursorPainter = new CursorTextZPainter( );
        plot.addPainter( cursorPainter );

        cursorPainter.setOffsetBySelectionSize( false );
        
        // tell the cursor painter what texture to report data values from
        cursorPainter.setTexture( texture );

        return plot;
    }
    
    public GlimpsePainter getPainter( )
    {
        return heatmap;
    }
}

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
package com.metsci.glimpse.examples.axis;

import static com.metsci.glimpse.axis.tagged.Tag.TEX_COORD_ATTR;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.painter.ColorRightYAxisPainter;
import com.metsci.glimpse.axis.painter.ColorXAxisPainter;
import com.metsci.glimpse.axis.painter.ColorYAxisPainter;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.axis.tagged.painter.TaggedColorXAxisPainter;
import com.metsci.glimpse.axis.tagged.painter.TaggedPartialColorXAxisPainter;
import com.metsci.glimpse.axis.tagged.painter.TaggedPartialColorYAxisPainter;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.support.colormap.ColorGradients;

/**
 * MultiAxisPlot2D allows users to create multiple axes above, below, and to the right and left
 * of the main plotting area.
 *
 * @author ulman
 */
public class MultiAxisPlotExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new MultiAxisPlotExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( )
    {
        final MultiAxisPlot2D plot = new MultiAxisPlot2D( );

        // create a number of custom axes on the top, bottom, right, and left of the plot

        // define names for the axes so that we can refer to them later
        String axisT1 = "axisT1";
        String axisB1 = "axisB1";
        String axisB2 = "axisB2";
        String axisB3 = "axisB3";
        String axisB4 = "axisB4";
        String axisR1 = "axisR1";
        String axisR2 = "axisR2";
        String axisR3 = "axisR3";
        String axisL1 = "axisL1";
        String axisL2 = "axisL2";

        // ask the plot to create the axes, indicating whether we want it positioned
        // below, above, to the right, or to the left of the main plot area
        plot.createAxisTop( axisT1 );
        plot.createAxisBottom( axisB1 );
        plot.createAxisBottom( axisB2 );
        AxisInfo axisInfoB3 = plot.createAxisBottom( axisB3 );
        AxisInfo axisInfoR = plot.createAxisRight( axisR1 );
        plot.createAxisRight( axisR2 );
        AxisInfo axisInfoL1 = plot.createAxisLeft( axisL1 );
        plot.createAxisLeft( axisL2 );

        // create an axis with a custom Axis1D and AxisMouseListener (which support tags)
        TaggedAxis1D taggedAxisB4 = new TaggedAxis1D( );
        AxisMouseListener taggedMouseListenerB4 = new TaggedAxisMouseListener1D( );
        AxisInfo axisInfoB4 = plot.createAxisBottom( axisB4, taggedAxisB4, taggedMouseListenerB4 );

        // add a custom painter to display the axis tags
        GridAxisLabelHandler tickHandlerB4 = new GridAxisLabelHandler( );
        TaggedColorXAxisPainter tagPainterB4 = new TaggedPartialColorXAxisPainter( tickHandlerB4 );
        axisInfoB4.setAxisPainter( tagPainterB4 );

        // add some tags to the axis
        taggedAxisB4.addTag( "Tag1", 2.0 ).setAttribute( TEX_COORD_ATTR, 0.0f );
        taggedAxisB4.addTag( "Tag2", 8.0 ).setAttribute( TEX_COORD_ATTR, 1.0f );

        // create an axis with a custom Axis1D and AxisMouseListener (which support tags)
        TaggedAxis1D taggedAxisR3 = new TaggedAxis1D( );
        AxisMouseListener taggedMouseListener = new TaggedAxisMouseListener1D( );
        AxisInfo axisInfoR3 = plot.createAxisRight( axisR3, taggedAxisR3, taggedMouseListener );

        // add a custom painter to display the axis tags
        GridAxisLabelHandler tickHandler = new GridAxisLabelHandler( );
        TaggedPartialColorYAxisPainter tagPainter = new TaggedPartialColorYAxisPainter( tickHandler );
        axisInfoR3.setAxisPainter( tagPainter );

        // add some tags to the axis
        taggedAxisR3.addTag( "Tag1", 2.0 );
        taggedAxisR3.addTag( "Tag2", 8.0 );

        // adjust the order and size of the right hand size axes
        plot.getAxisInfo( axisR2 ).setOrder( 0 );
        plot.getAxisInfo( axisR1 ).setOrder( 1 );
        plot.getAxisInfo( axisR3 ).setOrder( 2 );
        plot.getAxisInfo( axisR3 ).setSize( 30 );

        // link "axisT1" and "axisR1" to the central plot axes, so that they move together
        plot.getAxis( axisT1 ).setParent( plot.getCenterAxisX( ) );
        plot.getAxis( axisR1 ).setParent( plot.getCenterAxisY( ) );

        // set the initial min and max value of one of the named axes
        Axis1D axis = plot.getAxis( axisT1 );
        axis.setMin( -20.0 );
        axis.setMax( 150.0 );

        // get the info object for one of the bottom axes (using its unique string name)
        // and adjust how its painter displays tick marks
        plot.getAxisInfo( axisB1 ).getTickHandler( ).setMinorTickCount( 10 );
        plot.getAxisInfo( axisB1 ).getAxisPainter( ).setShowMinorTicks( true );

        // alternatively, we can use info object we saved when we created the axis
        // here we set a text label and adjust the axis size for one of the left axes
        axisInfoL1.getTickHandler( ).setAxisLabel( "Left Axis 1" );
        axisInfoL1.getAxisPainter( ).setAxisLabelBufferSize( 10 );
        axisInfoL1.setSize( 60 );

        // have one of the axes display a color scale in addition to labels and tick marks

        // create a jet color scale texture
        ColorTexture1D colorScale = new ColorTexture1D( 1024 );
        colorScale.setColorGradient( ColorGradients.jet );

        // create an axis painter to display the jet color scale
        ColorYAxisPainter colorRightPainter = new ColorRightYAxisPainter( axisInfoR.getTickHandler( ) );
        colorRightPainter.setColorScale( colorScale );

        // use the color scale in place of the standard axis painter for one of the right axes
        axisInfoR.setAxisPainter( colorRightPainter );

        // create another color scale texture for the tag axis
        ColorTexture1D colorScale2 = new ColorTexture1D( 1024 );
        colorScale2.setColorGradient( ColorGradients.clearToBlack );
        tagPainter.setColorScale( colorScale2 );

        // create a horizontal color scale painter
        ColorXAxisPainter painterB3 = new ColorXAxisPainter( axisInfoB3.getTickHandler( ) );
        axisInfoB3.setAxisPainter( painterB3 );
        painterB3.setColorScale( colorScale );

        tagPainterB4.setColorScale( colorScale );

        // set a title for the overall plot
        plot.setTitle( "Multi Axis Plot" );

        // create grid lines which are linked to two specific axes (different than those
        // two linked to the central plot mouse controls)

        // to accomplish this, we need to create a new Axis2D from the two Axis1D we wish to use
        Axis2D gridAxis = new Axis2D( plot.getAxis( axisB1 ), plot.getAxis( axisR1 ) );
        // then create a GlimpseAxisLayout2D using the Axis2D
        GlimpseAxisLayout2D gridLayout = new GlimpseAxisLayout2D( gridAxis );
        // this GlimpseAxisLayout2D will sit on top of the plot.getLayoutCenter( )
        // but we want it to be transparent to events, since events are handled by the
        // underlying plot.getLayoutCenter( ) Layout
        gridLayout.setEventConsumer( false );
        gridLayout.setEventGenerator( false );
        // finally, add the GridPainter and add the new GlimpseAxisLayout2D to the plot layout
        gridLayout.addPainter( new GridPainter( ) );
        plot.getLayoutCenter( ).addLayout( gridLayout );

        // add a simple border painter to the main plot area
        plot.addPainter( new BorderPainter( ) );

        return plot;
    }
}

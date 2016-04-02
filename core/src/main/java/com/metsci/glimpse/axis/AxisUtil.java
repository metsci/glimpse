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
package com.metsci.glimpse.axis;

import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;

/**
 * Utility classes for automatically creating axes and assigning
 * them to a {@link com.metsci.glimpse.layout.GlimpseAxisLayout2D}
 * or {@link com.metsci.glimpse.layout.GlimpseAxisLayout1D} and
 * attaching a {@link com.metsci.glimpse.axis.listener.mouse.AxisMouseListener}
 * to provide mouse interaction.
 *
 * @author ulman
 */
public class AxisUtil
{
    /////////////////////////////////////////////////////////////////////////////////////////
    //      Methods for automatically creating GlimpseLayouts and associated Axes          //
    /////////////////////////////////////////////////////////////////////////////////////////

    public static GlimpseAxisLayout2D createLayout2D( )
    {
        GlimpseAxisLayout2D layout = new GlimpseAxisLayout2D( );
        createAxis2D( layout );
        return layout;
    }

    public static GlimpseAxisLayout1D createLayoutX( )
    {
        GlimpseAxisLayout1D layout = new GlimpseAxisLayoutX( );
        createHorizontalAxis( layout );
        return layout;
    }

    public static GlimpseAxisLayout1D createLayoutY( )
    {
        GlimpseAxisLayout1D layout = new GlimpseAxisLayoutY( );
        createVerticalAxis( layout );
        return layout;
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Methods for automatically creating Glimpse Axes and attaching appropriate listeners //
    /////////////////////////////////////////////////////////////////////////////////////////

    public static Axis2D createAxis2D( GlimpseAxisLayout2D layout, Axis2D parentAxis )
    {
        return createAxis2D( layout, parentAxis, UpdateMode.MinMax, false, -1, -1, -1, -1 );
    }

    public static Axis2D createAxis2D( GlimpseAxisLayout2D layout )
    {
        return createAxis2D( layout, null, UpdateMode.MinMax, false, -1, -1, -1, -1 );
    }

    public static Axis2D createAxis2D( GlimpseAxisLayout2D layout, double minX, double maxX, double minY, double maxY )
    {
        return createAxis2D( layout, null, UpdateMode.MinMax, true, minX, maxX, minY, maxY );
    }

    public static Axis2D createAxis2D( GlimpseAxisLayout2D layout, Axis2D parentAxis, UpdateMode mode, double minX, double maxX, double minY, double maxY )
    {
        return createAxis2D( layout, parentAxis, mode, true, minX, maxX, minY, maxY );
    }

    public static Axis1D createHorizontalAxis( GlimpseAxisLayout1D layout, Axis1D parentAxis, UpdateMode mode )
    {
        return createAxis( layout, parentAxis, mode, false, -1, -1 );
    }

    public static Axis1D createVerticalAxis( GlimpseAxisLayout1D layout, Axis1D parentAxis, UpdateMode mode )
    {
        return createAxis( layout, parentAxis, mode, false, -1, -1 );
    }

    public static Axis1D createHorizontalAxis( GlimpseAxisLayout1D layout, Axis1D parentAxis )
    {
        return createHorizontalAxis( layout, parentAxis, UpdateMode.MinMax );
    }

    public static Axis1D createVerticalAxis( GlimpseAxisLayout1D layout, Axis1D parentAxis )
    {
        return createVerticalAxis( layout, parentAxis, UpdateMode.MinMax );
    }

    public static Axis1D createHorizontalAxis( GlimpseAxisLayout1D layout )
    {
        return createHorizontalAxis( layout, UpdateMode.MinMax );
    }

    public static Axis1D createVerticalAxis( GlimpseAxisLayout1D layout )
    {
        return createVerticalAxis( layout, UpdateMode.MinMax );
    }

    public static Axis1D createHorizontalAxis( GlimpseAxisLayout1D layout, UpdateMode mode )
    {
        return createAxis( layout, null, mode, false, -1f, -1f );
    }

    public static Axis1D createVerticalAxis( GlimpseAxisLayout1D layout, UpdateMode mode )
    {
        return createAxis( layout, null, mode, false, -1f, -1f );
    }

    public static Axis1D createHorizontalAxis( GlimpseAxisLayout1D layout, double min, double max )
    {
        return createHorizontalAxis( layout, UpdateMode.MinMax, min, max );
    }

    public static Axis1D createVerticalAxis( GlimpseAxisLayout1D layout, double min, double max )
    {
        return createVerticalAxis( layout, UpdateMode.MinMax, min, max );
    }

    public static Axis1D createHorizontalAxis( GlimpseAxisLayout1D layout, UpdateMode mode, double min, double max )
    {
        return createAxis( layout, null, mode, true, min, max );
    }

    public static Axis1D createVerticalAxis( GlimpseAxisLayout1D layout, UpdateMode mode, double min, double max )
    {
        return createAxis( layout, null, mode, true, min, max );
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    //       Methods for attaching mouse listeners to already created GlimpseLayouts       //
    /////////////////////////////////////////////////////////////////////////////////////////

    public static void attachHorizontalMouseListener( GlimpseAxisLayout1D layout )
    {
        attachMouseListener( layout );
    }

    public static void attachVerticalMouseListener( GlimpseAxisLayout1D layout )
    {
        attachMouseListener( layout );
    }

    public static void attachMouseListener( GlimpseAxisLayout2D layout )
    {
        AxisMouseListener listener = new AxisMouseListener2D( );
        layout.addGlimpseMouseAllListener( listener );
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Private factory helper methods                                                      //
    /////////////////////////////////////////////////////////////////////////////////////////

    private static void attachMouseListener( GlimpseAxisLayout1D layout )
    {
        AxisMouseListener listener = new AxisMouseListener1D( );
        layout.addGlimpseMouseAllListener( listener );
    }

    private static Axis1D createAxis( GlimpseAxisLayout1D layout, Axis1D parentAxis, UpdateMode mode, boolean setMinMax, double min, double max )
    {
        // create the axis object
        Axis1D axis = new Axis1D( );

        // set the axis update mode
        axis.setUpdateMode( mode );

        // set the min and max bounds of the axis
        if ( setMinMax )
        {
            axis.setMin( min );
            axis.setMax( max );
        }

        // set this axis' parent
        axis.setParent( parentAxis );

        // attach a set of mouse listeners which adjust the min/max bounds of the axis based on mouse events
        attachMouseListener( layout );

        // associate the provided axis with the layout
        layout.setAxis( axis );

        return axis;
    }

    private static Axis2D createAxis2D( GlimpseAxisLayout2D layout, Axis2D parentAxis, UpdateMode mode, boolean setMinMax, double minX, double maxX, double minY, double maxY )
    {
        // create the axis object
        Axis2D axis = new Axis2D( );

        // set the axis update mode
        axis.getAxisX( ).setUpdateMode( mode );
        axis.getAxisY( ).setUpdateMode( mode );

        // set the min and max bounds of the axis
        if ( setMinMax )
        {
            axis.getAxisX( ).setMin( minX );
            axis.getAxisX( ).setMax( maxX );
            axis.getAxisY( ).setMin( minY );
            axis.getAxisY( ).setMax( maxY );
        }

        // set this axis' parent
        axis.setParent( parentAxis );

        // attach a set of mouse listeners which adjust the min/max bounds of the axis based on mouse events
        attachMouseListener( layout );

        // associate the provided axis with the layout
        layout.setAxis( axis );

        return axis;
    }
}

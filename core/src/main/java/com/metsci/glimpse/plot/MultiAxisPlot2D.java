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
package com.metsci.glimpse.plot;

import static com.metsci.glimpse.plot.MultiAxisPlot2D.AxisOrientation.Bottom;
import static com.metsci.glimpse.plot.MultiAxisPlot2D.AxisOrientation.Left;
import static com.metsci.glimpse.plot.MultiAxisPlot2D.AxisOrientation.Right;
import static com.metsci.glimpse.plot.MultiAxisPlot2D.AxisOrientation.Top;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.axis.painter.NumericAxisPainter;
import com.metsci.glimpse.axis.painter.NumericRightYAxisPainter;
import com.metsci.glimpse.axis.painter.NumericTopXAxisPainter;
import com.metsci.glimpse.axis.painter.NumericXAxisPainter;
import com.metsci.glimpse.axis.painter.NumericYAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.event.mouse.Mouseable;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutManagerMig;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.base.GlimpsePainterCallback;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.support.settings.DefaultLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * A flexible plotting area with the ability to create an arbitrary number of additional
 * axes above, below, left, or right of the main central plotting area.
 *
 * @author ulman
 */
public class MultiAxisPlot2D extends GlimpseLayout
{
    protected int outerBorder = 10;
    protected int titleSpacing = 50;
    protected int innerSpacing = 0;

    protected Map<String, AxisInfo> axesTopX;
    protected Map<String, AxisInfo> axesBottomX;
    protected Map<String, AxisInfo> axesRightY;
    protected Map<String, AxisInfo> axesLeftY;

    protected Map<String, AxisInfo> axesAll;

    protected Axis1D centerAxisX;
    protected Axis1D centerAxisY;

    protected GlimpseLayoutManagerMig layout;

    protected GlimpseLayout titleLayout;
    protected SimpleTextPainter titlePainter;
    protected String title = null;
    protected boolean showTitle = false;

    protected BackgroundPainter plotBackgroundPainter;
    protected BackgroundPainter backgroundPainter;
    protected LookAndFeel laf;

    protected GlimpseAxisLayout2D axisLayoutXY;
    protected AxisMouseListener mouseListenerXY;

    protected enum AxisOrientation
    {
        Top, Bottom, Left, Right;
    }

    public class AxisInfo
    {
        protected String id;

        protected int order;

        protected Axis1D axis;

        protected AxisMouseListener mouseListener;

        protected GridAxisLabelHandler tickHandler;

        protected GlimpseLayout layout;

        protected int size;

        protected AxisOrientation orient;

        protected DelegatePainter backgroundDelegate;
        protected DelegatePainter axisDelegate;
        protected NumericAxisPainter axisPainter;
        protected DelegatePainter foregroundDelegate;

        public AxisInfo( String id, AxisOrientation orient, Axis1D axis, AxisMouseListener mouseListener, GridAxisLabelHandler tickHandler, NumericAxisPainter painter, GlimpseLayout layout, int order, int size )
        {
            this.id = id;
            this.axis = axis;
            this.mouseListener = mouseListener;
            this.tickHandler = tickHandler;
            this.axisPainter = painter;
            this.layout = layout;
            this.order = order;
            this.size = size;
            this.orient = orient;

            this.backgroundDelegate = new DelegatePainter( );
            this.backgroundDelegate.setVisible( false );

            this.axisDelegate = new DelegatePainter( );

            this.foregroundDelegate = new DelegatePainter( );
            this.foregroundDelegate.setVisible( false );

            this.layout.addPainter( backgroundDelegate );
            this.layout.addPainter( axisDelegate );
            this.layout.addPainter( foregroundDelegate );

            this.axisDelegate.addPainter( axisPainter );
        }

        public String getId( )
        {
            return id;
        }

        public Axis1D getAxis( )
        {
            return axis;
        }

        public AxisMouseListener getMouseListener( )
        {
            return mouseListener;
        }

        public GridAxisLabelHandler getTickHandler( )
        {
            return tickHandler;
        }

        public NumericAxisPainter getAxisPainter( )
        {
            return axisPainter;
        }

        public GlimpseLayout getLayout( )
        {
            return layout;
        }

        public AxisOrientation getOrientation( )
        {
            return orient;
        }

        public int getSize( )
        {
            return size;
        }

        public void setSize( int size )
        {
            this.size = size;

            updatePainterLayout( );
            validate( );
        }

        public int getOrder( )
        {
            return order;
        }

        public void setOrder( int order )
        {
            this.order = order;

            updatePainterLayout( );
            validate( );
        }

        public void addBackgroundPainter( GlimpsePainter painter )
        {
            this.backgroundDelegate.addPainter( painter );
            this.backgroundDelegate.setVisible( true );
        }

        public void removeBackgroundPainter( GlimpsePainter painter )
        {
            this.backgroundDelegate.removePainter( painter );
        }

        public void addForegroundPainter( GlimpsePainter painter )
        {
            this.foregroundDelegate.addPainter( painter );
            this.foregroundDelegate.setVisible( true );
        }

        public void removeForegroundPainter( GlimpsePainter painter )
        {
            this.foregroundDelegate.removePainter( painter );
        }

        public void setAxisPainter( NumericAxisPainter newPainter )
        {
            this.axisDelegate.removePainter( this.axisPainter );
            this.axisPainter = newPainter;
            this.axisDelegate.addPainter( this.axisPainter );
        }

        @Override
        public int hashCode( )
        {
            final int prime = 31;
            int result = 1;
            result = prime + result * ( ( id == null ) ? 0 : id.hashCode( ) );
            result = prime + result * ( ( orient == null ) ? 0 : orient.hashCode( ) );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            AxisInfo other = ( AxisInfo ) obj;
            if ( id == null ) return other.id == null;
            if ( !id.equals( other.id ) ) return false;
            if ( orient != other.orient ) return false;
            return true;
        }
    }

    public MultiAxisPlot2D( )
    {
        initialize( );
    }

    //////////////////////////////////////
    //     Initialization Methods       //
    //////////////////////////////////////

    protected void initialize( )
    {
        initializeArrays( );
        initializeLayout( );
        initializePainters( );
        initializeLookAndFeel( );
        updatePainterLayout( );
    }

    protected void initializeArrays( )
    {
        this.axesTopX = new LinkedHashMap<String, AxisInfo>( );
        this.axesBottomX = new LinkedHashMap<String, AxisInfo>( );
        this.axesRightY = new LinkedHashMap<String, AxisInfo>( );
        this.axesLeftY = new LinkedHashMap<String, AxisInfo>( );

        this.axesAll = new LinkedHashMap<String, AxisInfo>( );

        this.centerAxisX = new Axis1D( );
        this.centerAxisY = new Axis1D( );
    }

    protected void initializeLayout( )
    {
        this.layout = new GlimpseLayoutManagerMig( );
        this.setLayoutManager( layout );
    }

    protected void updatePainterLayout( )
    {
        int t = axesTopX.size( );
        int b = axesBottomX.size( );
        int r = axesRightY.size( );
        int l = axesLeftY.size( );

        this.layout.setLayoutConstraints( String.format( "bottomtotop, gapx 0, gapy 0, insets %d %d %d %d", getTopInset( ), outerBorder, outerBorder, outerBorder ) );

        this.titleLayout.setLayoutData( String.format( "cell %d %d 1 1, pushx, growx, height %d!", l, 0, isTitleVisible( ) ? titleSpacing : 0 ) );

        this.axisLayoutXY.setLayoutData( String.format( "cell %d %d 1 1, push, grow", l, t + 1 ) );

        List<AxisInfo> axisList = getSortedAxes( axesTopX.values( ) );
        for ( int i = 0; i < t; i++ )
        {
            AxisInfo axis = axisList.get( i );
            axis.getLayout( ).setLayoutData( String.format( "cell %d %d 1 1, pushx, growx, height %d!", l, i + 1, axis.getSize( ) ) );
        }

        axisList = getSortedAxes( axesBottomX.values( ) );
        for ( int i = 0; i < b; i++ )
        {
            AxisInfo axis = axisList.get( i );
            axis.getLayout( ).setLayoutData( String.format( "cell %d %d 1 1, pushx, growx, height %d!", l, t + 2 + i, axis.getSize( ) ) );
        }

        axisList = getSortedAxes( axesLeftY.values( ) );
        for ( int i = 0; i < l; i++ )
        {
            AxisInfo axis = axisList.get( i );
            axis.getLayout( ).setLayoutData( String.format( "cell %d %d 1 1, pushy, growy, width %d!", i, t + 1, axis.getSize( ) ) );
        }

        axisList = getSortedAxes( axesRightY.values( ) );
        for ( int i = 0; i < r; i++ )
        {
            AxisInfo axis = axisList.get( i );
            axis.getLayout( ).setLayoutData( String.format( "cell %d %d 1 1, pushy, growy, width %d!", l + 1 + i, t + 1, axis.getSize( ) ) );
        }

        this.invalidateLayout( );
    }

    protected void initializePainters( )
    {
        this.backgroundPainter = new BackgroundPainter( true );
        super.addPainter0( backgroundPainter, null, Integer.MIN_VALUE );

        this.titleLayout = new GlimpseLayout( this, "Title" );
        this.axisLayoutXY = new GlimpseAxisLayout2D( this, "Center", new Axis2D( centerAxisX, centerAxisY ) );

        this.titlePainter = createTitlePainter( );

        if ( titlePainter != null ) titleLayout.addPainter( titlePainter );

        this.plotBackgroundPainter = new BackgroundPainter( false );
        this.axisLayoutXY.addPainter( this.plotBackgroundPainter );

        this.mouseListenerXY = createAxisMouseListenerXY( );
        this.attachAxisMouseListener( axisLayoutXY, mouseListenerXY );
    }

    protected void initializeLookAndFeel( )
    {
        setLookAndFeel( new DefaultLookAndFeel( ) );
    }

    protected List<AxisInfo> getSortedAxes( Collection<AxisInfo> unsorted )
    {
        List<AxisInfo> sortedList = new ArrayList<AxisInfo>( );

        sortedList.addAll( unsorted );

        Collections.sort( sortedList, new Comparator<AxisInfo>( )
        {
            @Override
            public int compare( AxisInfo axis0, AxisInfo axis1 )
            {
                return axis0.getOrder( ) - axis1.getOrder( );
            }
        } );

        return sortedList;
    }

    //////////////////////////////////////
    //         Painter Methods          //
    //////////////////////////////////////

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        super.setLookAndFeel( laf );

        this.laf = laf;
    }

    public void setPlotBackgroundColor( float[] color )
    {
        plotBackgroundPainter.setColor( color );
    }

    public void setBackgroundColor( float[] color )
    {
        backgroundPainter.setColor( color );
    }

    @Override
    public void addPainter( GlimpsePainter painter )
    {
        axisLayoutXY.addPainter( painter );
    }

    @Override
    public void addPainter( GlimpsePainter painter, GlimpsePainterCallback callback )
    {
        axisLayoutXY.addPainter( painter, callback );
    }

    @Override
    public void addPainter( GlimpsePainter painter, int zOrder )
    {
        axisLayoutXY.addPainter( painter, zOrder );
    }

    @Override
    public void addPainter( GlimpsePainter painter, GlimpsePainterCallback callback, int zOrder )
    {
        axisLayoutXY.addPainter( painter, callback, zOrder );
    }

    @Override
    public void removePainter( GlimpsePainter painter )
    {
        axisLayoutXY.removePainter( painter );
    }

    public GlimpseAxisLayout2D getLayoutCenter( )
    {
        return axisLayoutXY;
    }

    //////////////////////////////////////
    //       Axis Getter Methods        //
    //////////////////////////////////////

    public Axis1D getAxis( String label )
    {
        AxisInfo info = axesAll.get( label );
        return info == null ? null : info.getAxis( );
    }

    public AxisInfo getAxisInfo( String label )
    {
        return axesAll.get( label );
    }

    public Axis2D getAxis2D( String axisX, String axisY )
    {
        return new Axis2D( getAxis( axisX ), getAxis( axisY ) );
    }

    public Axis1D getCenterAxisX( )
    {
        return centerAxisX;
    }

    public Axis1D getCenterAxisY( )
    {
        return centerAxisY;
    }

    public Axis2D getCenterAxis( )
    {
        return new Axis2D( centerAxisX, centerAxisY );
    }

    //////////////////////////////////////////////
    //       Title / Font / Color Methods       //
    //////////////////////////////////////////////

    public void setTitleFont( Font font )
    {
        this.titlePainter.setFont( font, true );
    }

    public void setTitleFont( Font font, boolean antialias )
    {
        this.titlePainter.setFont( font, antialias );
    }

    public String getTitle( )
    {
        return this.title;
    }

    public void setTitle( String title )
    {
        this.title = title;
        this.titlePainter.setText( title );
        this.setShowTitle( true );
    }

    public void setShowTitle( boolean show )
    {
        this.showTitle = show;
        this.updatePainterLayout( );
        this.validate( );
    }

    public void setTitleHeight( int height )
    {
        this.titleSpacing = height;
        this.updatePainterLayout( );
        this.validate( );
    }

    //////////////////////////////////////
    //       Layout Size Methods        //
    //////////////////////////////////////

    public void setBorderSize( int size )
    {
        this.outerBorder = size;
        this.updatePainterLayout( );
        this.validate( );
    }

    public void validate( )
    {
        centerAxisX.validate( );
        centerAxisY.validate( );
        for ( AxisInfo axis : axesTopX.values( ) )
            axis.getAxis( ).validate( );
        for ( AxisInfo axis : axesBottomX.values( ) )
            axis.getAxis( ).validate( );
        for ( AxisInfo axis : axesLeftY.values( ) )
            axis.getAxis( ).validate( );
        for ( AxisInfo axis : axesRightY.values( ) )
            axis.getAxis( ).validate( );
    }

    //////////////////////////////////////
    //      Axis Deletion Methods       //
    //////////////////////////////////////

    public void deleteAxisRight( String name )
    {
        AxisInfo info = axesRightY.get( name );

        if ( info == null ) return;

        this.removeLayout( info.getLayout( ) );
        axesRightY.remove( name );
        axesAll.remove( name );

        updatePainterLayout( );
        validate( );
    }

    public void deleteAxisLeft( String name )
    {
        AxisInfo info = axesLeftY.get( name );

        if ( info == null ) return;

        this.removeLayout( info.getLayout( ) );
        axesLeftY.remove( name );
        axesAll.remove( name );

        updatePainterLayout( );
        validate( );
    }

    public void deleteAxisTop( String name )
    {
        AxisInfo info = axesTopX.get( name );

        if ( info == null ) return;

        this.removeLayout( info.getLayout( ) );
        axesTopX.remove( name );
        axesAll.remove( name );

        updatePainterLayout( );
        validate( );
    }

    public void deleteAxisBottom( String name )
    {
        AxisInfo info = axesBottomX.get( name );

        if ( info == null ) return;

        this.removeLayout( info.getLayout( ) );
        axesBottomX.remove( name );
        axesAll.remove( name );

        updatePainterLayout( );
        validate( );
    }

    //////////////////////////////////////
    //      Axis Creation Methods       //
    //////////////////////////////////////

    public AxisInfo createAxisRight( String name, Axis1D axis, AxisMouseListener mouseListener )
    {
        int order = 0;
        int size = defaultAxisSizeRight( );

        GridAxisLabelHandler tickHandler = createTickHandlerRight( );
        NumericAxisPainter painter = createAxisPainterRight( tickHandler );
        painter.setLookAndFeel( laf );

        GlimpseLayout layout = new GlimpseAxisLayoutY( this, name, axis );

        attachAxisMouseListener( layout, mouseListener );

        AxisInfo info = new AxisInfo( name, Right, axis, mouseListener, tickHandler, painter, layout, order, size );

        axesRightY.put( name, info );
        axesAll.put( name, info );

        layout.setLookAndFeel( laf );

        updatePainterLayout( );
        validate( );

        return info;
    }

    public AxisInfo createAxisRight( String name )
    {
        Axis1D axis = createAxisRight( );
        return createAxisRight( name, axis, createAxisMouseListenerRight( ) );
    }

    public AxisInfo createAxisLeft( String name, Axis1D axis, AxisMouseListener mouseListener )
    {
        int order = 0;
        int size = defaultAxisSizeLeft( );
        GridAxisLabelHandler tickHandler = createTickHandlerLeft( );
        NumericAxisPainter painter = createAxisPainterLeft( tickHandler );
        painter.setLookAndFeel( laf );

        GlimpseLayout layout = new GlimpseAxisLayoutY( this, name, axis );

        attachAxisMouseListener( layout, mouseListener );

        AxisInfo info = new AxisInfo( name, Left, axis, mouseListener, tickHandler, painter, layout, order, size );

        axesLeftY.put( name, info );
        axesAll.put( name, info );

        layout.setLookAndFeel( laf );

        updatePainterLayout( );
        validate( );

        return info;
    }

    public AxisInfo createAxisLeft( String name )
    {
        Axis1D axis = createAxisLeft( );
        return createAxisLeft( name, axis, createAxisMouseListenerLeft( ) );
    }

    public AxisInfo createAxisTop( String name, Axis1D axis, AxisMouseListener mouseListener )
    {
        int order = 0;
        int size = defaultAxisSizeTop( );
        GridAxisLabelHandler tickHandler = createTickHandlerTop( );
        NumericAxisPainter painter = createAxisPainterTop( tickHandler );
        painter.setLookAndFeel( laf );

        GlimpseLayout layout = new GlimpseAxisLayoutX( this, name, axis );

        attachAxisMouseListener( layout, mouseListener );

        AxisInfo info = new AxisInfo( name, Top, axis, mouseListener, tickHandler, painter, layout, order, size );

        axesTopX.put( name, info );
        axesAll.put( name, info );

        layout.setLookAndFeel( laf );

        updatePainterLayout( );
        validate( );

        return info;
    }

    public AxisInfo createAxisTop( String name )
    {
        Axis1D axis = createAxisTop( );
        return createAxisTop( name, axis, createAxisMouseListenerTop( ) );
    }

    public AxisInfo createAxisBottom( String name, Axis1D axis, AxisMouseListener mouseListener )
    {
        int order = 0;
        int size = defaultAxisSizeBottom( );
        GridAxisLabelHandler tickHandler = createTickHandlerBottom( );
        NumericAxisPainter painter = createAxisPainterBottom( tickHandler );
        painter.setLookAndFeel( laf );

        GlimpseLayout layout = new GlimpseAxisLayoutX( this, name, axis );

        attachAxisMouseListener( layout, mouseListener );

        AxisInfo info = new AxisInfo( name, Bottom, axis, mouseListener, tickHandler, painter, layout, order, size );

        axesBottomX.put( name, info );
        axesAll.put( name, info );

        layout.setLookAndFeel( laf );

        updatePainterLayout( );
        validate( );

        return info;
    }

    protected void attachAxisMouseListener( Mouseable mouseable, AxisMouseListener listener )
    {
        mouseable.addGlimpseMouseListener( listener );
        mouseable.addGlimpseMouseMotionListener( listener );
        mouseable.addGlimpseMouseWheelListener( listener );
    }

    public AxisInfo createAxisBottom( String name )
    {
        Axis1D axis = createAxisBottom( );
        return createAxisBottom( name, axis, createAxisMouseListenerBottom( ) );
    }

    protected Axis1D createAxisRight( )
    {
        return new Axis1D( );
    }

    protected Axis1D createAxisLeft( )
    {
        return new Axis1D( );
    }

    protected Axis1D createAxisTop( )
    {
        return new Axis1D( );
    }

    protected Axis1D createAxisBottom( )
    {
        return new Axis1D( );
    }

    protected NumericAxisPainter createAxisPainterRight( AxisLabelHandler tickHandler )
    {
        return new NumericRightYAxisPainter( tickHandler );
    }

    protected NumericAxisPainter createAxisPainterTop( AxisLabelHandler tickHandler )
    {
        return new NumericTopXAxisPainter( tickHandler );
    }

    protected NumericAxisPainter createAxisPainterBottom( AxisLabelHandler tickHandler )
    {
        return new NumericXAxisPainter( tickHandler );
    }

    protected NumericAxisPainter createAxisPainterLeft( AxisLabelHandler tickHandler )
    {
        return new NumericYAxisPainter( tickHandler );
    }

    protected GridAxisLabelHandler createTickHandlerRight( )
    {
        return new GridAxisLabelHandler( );
    }

    protected GridAxisLabelHandler createTickHandlerTop( )
    {
        return new GridAxisLabelHandler( );
    }

    protected GridAxisLabelHandler createTickHandlerBottom( )
    {
        return new GridAxisLabelHandler( );
    }

    protected GridAxisLabelHandler createTickHandlerLeft( )
    {
        return new GridAxisLabelHandler( );
    }

    protected AxisMouseListener createAxisMouseListenerRight( )
    {
        return new AxisMouseListener1D( );
    }

    protected AxisMouseListener createAxisMouseListenerTop( )
    {
        return new AxisMouseListener1D( );
    }

    protected AxisMouseListener createAxisMouseListenerBottom( )
    {
        return new AxisMouseListener1D( );
    }

    protected AxisMouseListener createAxisMouseListenerLeft( )
    {
        return new AxisMouseListener1D( );
    }

    protected int defaultAxisSizeRight( )
    {
        return 50;
    }

    protected int defaultAxisSizeTop( )
    {
        return 35;
    }

    protected int defaultAxisSizeBottom( )
    {
        return 40;
    }

    protected int defaultAxisSizeLeft( )
    {
        return 50;
    }

    protected AxisMouseListener createAxisMouseListenerXY( )
    {
        return new AxisMouseListener2D( );
    }

    protected SimpleTextPainter createTitlePainter( )
    {
        SimpleTextPainter painter = new SimpleTextPainter( );
        painter.setHorizontalPosition( HorizontalPosition.Center );
        painter.setVerticalPosition( VerticalPosition.Center );
        return painter;
    }

    protected boolean isTitleVisible( )
    {
        return showTitle && title != null && !title.isEmpty( );
    }

    protected int getTopInset( )
    {
        if ( isTitleVisible( ) )
            return 0;
        else
            return outerBorder;
    }

    @Override
    public String toString( )
    {
        return MultiAxisPlot2D.class.getSimpleName( );
    }
}

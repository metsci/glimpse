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

import java.awt.Font;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.UpdateMode;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.axis.painter.NumericAxisPainter;
import com.metsci.glimpse.axis.painter.NumericXAxisPainter;
import com.metsci.glimpse.axis.painter.NumericYAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.context.TargetStackUtil;
import com.metsci.glimpse.event.mouse.GlimpseMouseAllListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseMotionListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseWheelListener;
import com.metsci.glimpse.event.mouse.Mouseable;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutManagerMig;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.DefaultLookAndFeel;

/**
 * <p>A simple, blank plotting area which divides itself into five regions:</p>
 * <ul>
 *  <li> a central plotting area
 *  <li> a title area above the plot
 *  <li> a labeled horizontal x axis below the plot
 *  <li> a labeled vertical y axis to the left of the plot
 *  <li> a labeled vertical z axis to the right of the plot
 * </ul>
 *
 * <p>Any of the areas above may be shown or hidden (the z axis area is commonly hidden).</p>
 *
 * <p>This plot is commonly used as a template for building new plots, however
 * for most use cases {@link SimplePlot2D} is more useful. It automatically
 * provides useful painters such as grid lines and mouse cursors.</p>
 *
 * @author ulman
 * @see SimplePlot2D
 */
public class Plot2D extends GlimpseAxisLayout2D
{
    /**
     * A constant for use with {@link com.metsci.glimpse.layout.GlimpseLayout#setZOrder(com.metsci.glimpse.painter.base.GlimpsePainter, int)}.
     * GlimpsePainters which should appear behind the plot data should be given this z order value.
     */
    public static int BACKGROUND_LAYER = -100;

    /**
     * A constant for use with {@link com.metsci.glimpse.layout.GlimpseLayout#setZOrder(com.metsci.glimpse.painter.base.GlimpsePainter, int)}.
     * GlimpsePainters which contain plot data should generally be given this z order value. Note, this is the default z order.
     */
    public static int DATA_LAYER = 0;

    /**
     * A constant for use with {@link com.metsci.glimpse.layout.GlimpseLayout#setZOrder(com.metsci.glimpse.painter.base.GlimpsePainter, int)}.
     * GlimpsePainters which contain overlays that should appear in front of plot data should be given this z order value.
     */
    public static int FOREGROUND_LAYER = 100;

    protected int outerBorder = 10;
    protected int axisThicknessX = 40;
    protected int axisThicknessY = 60;
    protected int axisThicknessZ = 65;
    protected int titleSpacing = 50;

    protected String title = null;
    protected boolean showTitle = false;

    protected BackgroundPainter backgroundPainter;

    protected GlimpseLayout titleLayout;
    protected GlimpseAxisLayout1D axisLayoutX;
    protected GlimpseAxisLayout1D axisLayoutY;
    protected GlimpseAxisLayout1D axisLayoutZ;
    protected GlimpseAxisLayout2D axisLayoutXY;

    protected Axis2D axisXY;
    protected Axis1D axisZ;

    protected SimpleTextPainter titlePainter;
    protected NumericAxisPainter painterX;
    protected NumericAxisPainter painterY;
    protected NumericAxisPainter painterZ;

    protected GridAxisLabelHandler tickX;
    protected GridAxisLabelHandler tickY;
    protected GridAxisLabelHandler tickZ;

    protected AxisMouseListener mouseListenerX;
    protected AxisMouseListener mouseListenerY;
    protected AxisMouseListener mouseListenerZ;
    protected AxisMouseListener mouseListenerXY;

    /**
     * Provided for subclasses which want to set fields before initialize is called (although they are
     * then responsible for calling initialize( )).
     */
    protected Plot2D( )
    {
    }

    public Plot2D( String name )
    {
        this.initialize( );
        this.setName( name );
    }

    protected void initialize( )
    {
        initializeAxes( );
        initializeParentLayout( );
        initializePainters( );
        attachAxisMouseListeners( );
        initializeLookAndFeel( );
        updatePainterLayout( );
    }

    protected void initializeAxes( )
    {
        axisXY = new Axis2D( createAxisX( ), createAxisY( ) );
        axisZ = createAxisZ( );
    }

    protected void initializeParentLayout( )
    {
        backgroundPainter = new BackgroundPainter( true );
        super.addPainter0( backgroundPainter, null, Integer.MIN_VALUE );
    }

    protected void initializeLookAndFeel( )
    {
        setLookAndFeel( new DefaultLookAndFeel( ) );
    }

    protected void updatePainterLayout( )
    {
        getLayoutManager( ).setLayoutConstraints( String.format( "bottomtotop, gapx 0, gapy 0, insets %d %d %d %d", getTopInset( ), outerBorder, outerBorder, outerBorder ) );

        titleLayout.setLayoutData( String.format( "cell 1 0 1 1, pushx, growx, height %d!", titleSpacing ) );

        axisLayoutY.setLayoutData( String.format( "cell 0 1 1 1, pushy, growy, width %d!", axisThicknessY ) );

        axisLayoutX.setLayoutData( String.format( "cell 1 2 1 1, pushx, growx, height %d!", axisThicknessX ) );

        axisLayoutXY.setLayoutData( "cell 1 1 1 1, push, grow" );

        axisLayoutZ.setLayoutData( String.format( "cell 2 1 1 1, pushy, growy, width %d!", axisThicknessZ ) );

        invalidateLayout( );
    }

    protected void initializePainters( )
    {
        this.titleLayout = new GlimpseLayout( this, "Title" );
        this.axisLayoutX = new GlimpseAxisLayoutX( this, "AxisX" );
        this.axisLayoutY = new GlimpseAxisLayoutY( this, "AxisY" );
        this.axisLayoutZ = new GlimpseAxisLayoutY( this, "AxisZ" );
        this.axisLayoutXY = new GlimpseAxisLayout2D( this, "Center" );

        this.setAxis( axisXY );
        this.axisLayoutZ.setAxis( axisZ );

        this.tickX = createLabelHandlerX( );
        this.tickY = createLabelHandlerY( );
        this.tickZ = createLabelHandlerZ( );

        this.titlePainter = createTitlePainter( );
        this.painterX = createAxisPainterX( tickX );
        this.painterY = createAxisPainterY( tickY );
        this.painterZ = createAxisPainterZ( tickZ );

        if ( titlePainter != null ) titleLayout.addPainter( titlePainter );
        if ( painterX != null ) axisLayoutX.addPainter( painterX );
        if ( painterY != null ) axisLayoutY.addPainter( painterY );
        if ( painterZ != null ) axisLayoutZ.addPainter( painterZ );

        this.mouseListenerX = createAxisMouseListenerX( );
        this.mouseListenerY = createAxisMouseListenerY( );
        this.mouseListenerZ = createAxisMouseListenerZ( );
        this.mouseListenerXY = createAxisMouseListenerXY( );
    }

    protected void attachAxisMouseListeners( )
    {
        attachAxisMouseListener( axisLayoutX, mouseListenerX );
        attachAxisMouseListener( axisLayoutY, mouseListenerY );
        attachAxisMouseListener( axisLayoutZ, mouseListenerZ );
        attachAxisMouseListener( axisLayoutXY, mouseListenerXY );
    }

    protected void removeAxisMouseListener( Mouseable mouseable, AxisMouseListener listener )
    {
        mouseable.removeGlimpseMouseListener( listener );
        mouseable.removeGlimpseMouseMotionListener( listener );
        mouseable.removeGlimpseMouseWheelListener( listener );
    }

    protected void attachAxisMouseListener( Mouseable mouseable, AxisMouseListener listener )
    {
        mouseable.addGlimpseMouseListener( listener );
        mouseable.addGlimpseMouseMotionListener( listener );
        mouseable.addGlimpseMouseWheelListener( listener );
    }

    protected AxisMouseListener createAxisMouseListenerX( )
    {
        return new AxisMouseListener1D( );
    }

    protected AxisMouseListener createAxisMouseListenerY( )
    {
        return new AxisMouseListener1D( );
    }

    protected AxisMouseListener createAxisMouseListenerZ( )
    {
        return new AxisMouseListener1D( );
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
        painter.setColor( GlimpseColor.getBlack( ) );
        return painter;
    }

    protected Axis1D createAxisX( )
    {
        return new Axis1D( );
    }

    protected Axis1D createAxisY( )
    {
        return new Axis1D( );
    }

    protected Axis1D createAxisZ( )
    {
        return new Axis1D( );
    }

    protected GridAxisLabelHandler createLabelHandlerX( )
    {
        return new GridAxisLabelHandler( );
    }

    protected GridAxisLabelHandler createLabelHandlerY( )
    {
        return new GridAxisLabelHandler( );
    }

    protected GridAxisLabelHandler createLabelHandlerZ( )
    {
        return new GridAxisLabelHandler( );
    }

    protected NumericAxisPainter createAxisPainterX( AxisLabelHandler tickHandler )
    {
        return new NumericXAxisPainter( tickHandler );
    }

    protected NumericAxisPainter createAxisPainterY( AxisLabelHandler tickHandler )
    {
        return new NumericYAxisPainter( tickHandler );
    }

    protected NumericAxisPainter createAxisPainterZ( AxisLabelHandler tickHandler )
    {
        return new NumericYAxisPainter( tickHandler );
    }

    protected int getTopInset( )
    {
        if ( showTitle && title != null && !title.isEmpty( ) )
            return 0;
        else
            return outerBorder;
    }

    public GlimpseLayoutManagerMig getLayoutManager( )
    {
        return ( GlimpseLayoutManagerMig ) super.getLayoutManager( );
    }

    public void setBackgroundColor( float[] color )
    {
        this.backgroundPainter.setColor( color );
    }

    public void addAxisListener( AxisListener2D l )
    {
        this.axis.addAxisListener( l );
    }

    public void setUpdateModeZ( UpdateMode mode )
    {
        this.axisZ.setUpdateMode( mode );
    }

    public void setUpdateModeXY( UpdateMode mode )
    {
        this.axis.getAxisX( ).setUpdateMode( mode );
        this.axis.getAxisY( ).setUpdateMode( mode );
    }

    public void setAxisFont( Font font )
    {
        setAxisFont( font, true );
    }

    public void setAxisFont( Font font, boolean antialias )
    {
        if ( this.painterX != null ) this.painterX.setFont( font, antialias );
        if ( this.painterY != null ) this.painterY.setFont( font, antialias );
        if ( this.painterZ != null ) this.painterZ.setFont( font, antialias );
    }

    public void setAxisColor( float[] color )
    {
        this.setTickColor( color );
        this.setTickLabelColor( color );
        this.setAxisLabelColor( color );
    }

    public void setTitleFont( Font font )
    {
        setTitleFont( font, true );
    }

    public void setTitleFont( Font font, boolean antialias )
    {
        this.titlePainter.setFont( font, antialias );
    }

    public void setTitle( String title )
    {
        this.title = title;
        this.titlePainter.setText( title );
        this.showTitle( true );
    }

    public void setTitleColor( float[] rgba )
    {
        this.titlePainter.setColor( rgba );
        this.showTitle( true );
    }

    public void showTitle( boolean show )
    {
        this.showTitle = show;
        this.updatePainterLayout( );
        this.validate( );
    }

    public void setBorderSize( int size )
    {
        this.outerBorder = size;
        this.updatePainterLayout( );
        this.validate( );
    }

    public void setAxisSizeX( int size )
    {
        this.axisThicknessX = size;
        this.updatePainterLayout( );
        this.validate( );
    }

    public void setAxisSizeY( int size )
    {
        this.axisThicknessY = size;
        this.updatePainterLayout( );
        this.validate( );
    }

    public void setAxisSizeZ( int size )
    {
        this.axisThicknessZ = size;
        this.updatePainterLayout( );
        this.validate( );
    }

    public void setTitleHeight( int size )
    {
        this.titleSpacing = size;
        this.updatePainterLayout( );
        this.validate( );
    }

    public Axis2D getAxis( )
    {
        return axisXY;
    }

    public Axis1D getAxisX( )
    {
        return axisXY.getAxisX( );
    }

    public Axis1D getAxisY( )
    {
        return axisXY.getAxisY( );
    }

    public Axis1D getAxisZ( )
    {
        return axisZ;
    }

    public void setTickSize( int size )
    {
        if ( painterX != null ) painterX.setTickSize( size );
        if ( painterY != null ) painterY.setTickSize( size );
        if ( painterZ != null ) painterZ.setTickSize( size );
    }

    public void setTickColor( float[] color )
    {
        if ( painterX != null ) painterX.setTickColor( color );
        if ( painterY != null ) painterY.setTickColor( color );
        if ( painterZ != null ) painterZ.setTickColor( color );
    }

    public void setTickLabelColor( float[] color )
    {
        if ( painterX != null ) painterX.setTickLabelColor( color );
        if ( painterY != null ) painterY.setTickLabelColor( color );
        if ( painterZ != null ) painterZ.setTickLabelColor( color );
    }

    public void setAxisLabelColor( float[] color )
    {
        if ( painterX != null ) painterX.setAxisLabelColor( color );
        if ( painterY != null ) painterY.setAxisLabelColor( color );
        if ( painterZ != null ) painterZ.setAxisLabelColor( color );
    }

    public void setMinorTickCount( int count )
    {
        if ( tickX != null ) tickX.setMinorTickCount( count );
        if ( tickY != null ) tickY.setMinorTickCount( count );
        if ( tickZ != null ) tickZ.setMinorTickCount( count );
    }

    public void setShowMinorTicks( boolean show )
    {
        if ( painterX != null ) painterX.setShowMinorTicks( show );
        if ( painterY != null ) painterY.setShowMinorTicks( show );
        if ( painterZ != null ) painterZ.setShowMinorTicks( show );
    }

    public void setAxisLabelZ( String label )
    {
        setAxisLabelZ( label, "" );
    }

    public void setAxisLabelZ( String label, String abbreviatedUnits )
    {
        tickZ.setAxisLabel( label );
        tickZ.setAxisUnits( abbreviatedUnits, true );
    }

    public void setAxisLabelZ( String label, String units, boolean abbreviated )
    {
        tickZ.setAxisLabel( label );
        tickZ.setAxisUnits( units, abbreviated );
    }

    public void setShowAxisMarkerZ( boolean show )
    {
        painterZ.setShowMarker( show );
    }

    public void setShowMinorTicksZ( boolean show )
    {
        painterZ.setShowMinorTicks( show );
    }

    public void setMarkerWidthZ( int width )
    {
        painterZ.setMarkerWidth( width );
    }

    public void setTickSpacingZ( int spacing )
    {
        tickZ.setTickSpacing( spacing );
    }

    public void setMinorTickCountZ( int count )
    {
        tickZ.setMinorTickCount( count );
    }

    public void setAxisLabelX( String label )
    {
        setAxisLabelX( label, "" );
    }

    public void setAxisLabelX( String label, String abbreviatedUnits )
    {
        tickX.setAxisLabel( label );
        tickX.setAxisUnits( abbreviatedUnits, true );
    }

    public void setAxisLabelX( String label, String units, boolean abbreviated )
    {
        tickX.setAxisLabel( label );
        tickX.setAxisUnits( units, abbreviated );
    }

    public void setShowAxisMarkerX( boolean show )
    {
        painterX.setShowMarker( show );
    }

    public void setShowMinorTicksX( boolean show )
    {
        painterX.setShowMinorTicks( show );
    }

    public void setMarkerWidthX( int width )
    {
        painterX.setMarkerWidth( width );
    }

    public void setTickSpacingX( int spacing )
    {
        tickX.setTickSpacing( spacing );
    }

    public void setMinorTickCountX( int count )
    {
        tickX.setMinorTickCount( count );
    }

    public void setAxisLabelY( String label )
    {
        setAxisLabelY( label, "" );
    }

    public void setAxisLabelY( String label, String abbreviatedUnits )
    {
        tickY.setAxisLabel( label );
        tickY.setAxisUnits( abbreviatedUnits, true );
    }

    public void setAxisLabelY( String label, String units, boolean abbreviated )
    {
        tickY.setAxisLabel( label );
        tickY.setAxisUnits( units, abbreviated );
    }

    public void setShowAxisMarkerY( boolean show )
    {
        painterY.setShowMarker( show );
    }

    public void setShowMinorTicksY( boolean show )
    {
        painterY.setShowMinorTicks( show );
    }

    public void setMarkerWidthY( int width )
    {
        painterY.setMarkerWidth( width );
    }

    public void setTickSpacingY( int spacing )
    {
        tickY.setTickSpacing( spacing );
    }

    public void setMinorTickCountY( int count )
    {
        tickY.setMinorTickCount( count );
    }

    public void validate( )
    {
        axis.getAxisX( ).validate( );
        axis.getAxisY( ).validate( );
        axisZ.validate( );
    }

    public void setAbsoluteMaxX( double value )
    {
        axis.getAxisX( ).setAbsoluteMax( value );
    }

    public void setAbsoluteMinX( double value )
    {
        axis.getAxisX( ).setAbsoluteMin( value );
    }

    public void setAbsoluteMaxY( double value )
    {
        axis.getAxisY( ).setAbsoluteMax( value );
    }

    public void setAbsoluteMinY( double value )
    {
        axis.getAxisY( ).setAbsoluteMin( value );
    }

    public void setAbsoluteMaxZ( double value )
    {
        axisZ.setAbsoluteMax( value );
    }

    public void setAbsoluteMinZ( double value )
    {
        axisZ.setAbsoluteMin( value );
    }

    public void setMaxX( double value )
    {
        axis.getAxisX( ).setMax( value );
    }

    public void setMinX( double value )
    {
        axis.getAxisX( ).setMin( value );
    }

    public void setMaxY( double value )
    {
        axis.getAxisY( ).setMax( value );
    }

    public void setMinY( double value )
    {
        axis.getAxisY( ).setMin( value );
    }

    public void setMaxZ( double value )
    {
        axisZ.setMax( value );
    }

    public void setMinZ( double value )
    {
        axisZ.setMin( value );
    }

    public void lockMaxX( double value )
    {
        axis.getAxisX( ).lockMax( value );
    }

    public void lockMinX( double value )
    {
        axis.getAxisX( ).lockMin( value );
    }

    public void lockMaxY( double value )
    {
        axis.getAxisY( ).lockMax( value );
    }

    public void lockMinY( double value )
    {
        axis.getAxisY( ).lockMin( value );
    }

    public void lockMaxZ( double value )
    {
        axisZ.lockMax( value );
    }

    public void lockMinZ( double value )
    {
        axisZ.lockMin( value );
    }

    public void unlockMaxX( )
    {
        axis.getAxisX( ).unlockMax( );
    }

    public void unlockMinX( )
    {
        axis.getAxisX( ).unlockMin( );
    }

    public void unlockMaxY( )
    {
        axis.getAxisY( ).unlockMax( );
    }

    public void unlockMinY( )
    {
        axis.getAxisY( ).unlockMin( );
    }

    public void unlockMaxZ( )
    {
        axisZ.unlockMax( );
    }

    public void unlockMinZ( )
    {
        axisZ.unlockMin( );
    }

    public void setSelectionSize( double value )
    {
        axis.getAxisX( ).setSelectionSize( value );
        axis.getAxisY( ).setSelectionSize( value );
    }

    public void lockAspectRatioXY( double x_to_y_ratio )
    {
        axis.lockAspectRatioXY( x_to_y_ratio );
    }

    public NumericAxisPainter getAxisPainterX( )
    {
        return painterX;
    }

    public NumericAxisPainter getAxisPainterY( )
    {
        return painterY;
    }

    public NumericAxisPainter getAxisPainterZ( )
    {
        return painterZ;
    }

    public GridAxisLabelHandler getLabelHandlerX( )
    {
        return tickX;
    }

    public GridAxisLabelHandler getLabelHandlerY( )
    {
        return tickY;
    }

    public GridAxisLabelHandler getLabelHandlerZ( )
    {
        return tickZ;
    }

    public GlimpseLayout getLayoutX( )
    {
        return axisLayoutX;
    }

    public GlimpseLayout getLayoutY( )
    {
        return axisLayoutY;
    }

    public GlimpseLayout getLayoutZ( )
    {
        return axisLayoutZ;
    }

    public GlimpseAxisLayout2D getLayoutCenter( )
    {
        return axisLayoutXY;
    }

    public GlimpseLayout getLayoutTitle( )
    {
        return titleLayout;
    }

    public GlimpseTargetStack getTargetStackX( )
    {
        return TargetStackUtil.newTargetStack( this, axisLayoutX );
    }

    public GlimpseTargetStack getTargetStackY( )
    {
        return TargetStackUtil.newTargetStack( this, axisLayoutY );
    }

    public GlimpseTargetStack getTargetStackZ( )
    {
        return TargetStackUtil.newTargetStack( this, axisLayoutZ );
    }

    public GlimpseTargetStack getTargetStackCenter( )
    {
        return TargetStackUtil.newTargetStack( this, axisLayoutXY );
    }

    public GlimpseTargetStack getTargetStackTitle( )
    {
        return TargetStackUtil.newTargetStack( this, titleLayout );
    }

    @Override
    public void addGlimpseMouseListener( GlimpseMouseListener listener )
    {
        this.axisLayoutXY.addGlimpseMouseListener( listener );
    }

    @Override
    public void addGlimpseMouseMotionListener( GlimpseMouseMotionListener listener )
    {
        this.axisLayoutXY.addGlimpseMouseMotionListener( listener );
    }

    @Override
    public void addGlimpseMouseWheelListener( GlimpseMouseWheelListener listener )
    {
        this.axisLayoutXY.addGlimpseMouseWheelListener( listener );
    }

    @Override
    public void addGlimpseMouseAllListener( GlimpseMouseAllListener listener )
    {
        this.axisLayoutXY.addGlimpseMouseAllListener( listener );
    }

    @Override
    public void removeGlimpseMouseAllListener( GlimpseMouseAllListener listener )
    {
        this.axisLayoutXY.removeGlimpseMouseAllListener( listener );
    }

    @Override
    public void removeGlimpseMouseListener( GlimpseMouseListener listener )
    {
        this.axisLayoutXY.removeGlimpseMouseListener( listener );
    }

    @Override
    public void removeGlimpseMouseMotionListener( GlimpseMouseMotionListener listener )
    {
        this.axisLayoutXY.removeGlimpseMouseMotionListener( listener );
    }

    @Override
    public void removeGlimpseMouseWheelListener( GlimpseMouseWheelListener listener )
    {
        this.axisLayoutXY.removeGlimpseMouseWheelListener( listener );
    }

    @Override
    public String toString( )
    {
        return Plot2D.class.getSimpleName( );
    }
}

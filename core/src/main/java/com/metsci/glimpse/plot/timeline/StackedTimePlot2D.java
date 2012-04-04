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
package com.metsci.glimpse.plot.timeline;

import static com.metsci.glimpse.support.font.FontUtils.getDefaultBold;
import static com.metsci.glimpse.support.font.FontUtils.getDefaultPlain;

import java.awt.Font;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.axis.painter.NumericXYAxisPainter;
import com.metsci.glimpse.axis.painter.TimeAxisPainter;
import com.metsci.glimpse.axis.painter.TimeXAxisPainter;
import com.metsci.glimpse.axis.painter.TimeYAxisPainter;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.axis.tagged.Constraint;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.plot.StackedPlot2D;
import com.metsci.glimpse.plot.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.StackedPlot2D.PlotInfo;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.plot.timeline.listener.TimelineMouseListener1D;
import com.metsci.glimpse.plot.timeline.listener.TimelineMouseListener2D;
import com.metsci.glimpse.plot.timeline.painter.SelectedTimeRegionPainter;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.util.units.time.Time;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * A {@link StackedPlot2D} which automatically creates a timeline axis at the
 * bottom of the stack and uses a
 * {@link com.metsci.glimpse.axis.tagged.TaggedAxis1D} to define a selected time
 * region.
 *
 * @author ulman
 */
public class StackedTimePlot2D extends GlimpseLayout
{
    public static final String MIN_TIME = "min_time";
    public static final String MAX_TIME = "max_time";
    public static final String CURRENT_TIME = "current_time";

    public static final String BACKGROUND = "Timeline Background";
    public static final String TIMELINE = "Timeline";

    // tags representing the minimum and maximum bounds of the selected time window
    protected Tag minTag;
    protected Tag maxTag;
    // tag representing the currently selected time
    protected Tag currentTag;

    // epoch encapsulating the absolute time which maps to value 0 on the timeline
    protected Epoch epoch;
    
    // layout covering all plots and timeline (for drawing
    // which must span multiple plots)
    protected GlimpseAxisLayout1D overlayLayout;

    // time layout painters and listeners
    protected PlotInfo timelineInfo;
    protected GlimpseAxisLayout1D timeLayout;
    protected DelegatePainter timeAxisDelegate;
    protected TimeAxisPainter timeAxisPainter;
    protected StackedPlot2D delegatePlot;
    protected PlotInfo selectedLayout;

    // timeline painters and listeners
    protected AxisMouseListener1D timelineMouseListener;
    protected SimpleTextPainter timeUnitsPainter;
    protected BorderPainter timeAxisBorderPainter;

    // default settings for TimelineMouseListeners of new plots
    protected boolean allowPanX = true;
    protected boolean allowPanY = true;
    protected boolean allowZoomX = true;
    protected boolean allowZoomY = true;
    protected boolean allowSelectionLock = true;
    protected boolean currentTimeLock;

    // a map of TimePlotInfo organized by their unique id
    protected Map<String, TimePlotInfo> stackedTimePlots;
    
    // the size of the label layout area in pixels
    protected int labelLayoutSize;

    public StackedTimePlot2D( )
    {
        this( Orientation.VERTICAL, Epoch.posixEpoch( ) );
    }

    /**
     * Creates a vertical StackedTimePlot2D. The provided epoch determines what
     * absolute timestamp corresponds to value 0.0 on the time Axis1D.
     */
    public StackedTimePlot2D( Epoch epoch )
    {
        this( Orientation.VERTICAL, epoch );
    }

    public StackedTimePlot2D( Orientation orientation )
    {
        this( orientation, Epoch.posixEpoch( ) );
    }

    /**
     * Creates a StackedTimePlot2D with specified orientation. The provided
     * epoch determines what absolute timestamp corresponds to value 0.0 on the
     * time Axis1D.
     */
    public StackedTimePlot2D( Orientation orientation, Epoch epoch )
    {
        super( StackedTimePlot2D.class.getSimpleName( ) );

        this.epoch = epoch;

        this.stackedTimePlots = new LinkedHashMap<String, TimePlotInfo>( );

        this.delegatePlot = new StackedPlot2D( orientation )
        {
            @Override
            protected TaggedAxis1D createCommonAxis( )
            {
                return new TaggedAxis1D( );
            }
        };

        this.initialize( );
    }

    /**
     * StackedTimePlot2D provides a GlimpseAxisLayout1D which stretches over
     * all the underlying plots and timeline. By default, it passes through mouse
     * events to the underlying GlimpseLayouts and is used to display the blue
     * time selection interval box.
     * 
     * However, it can be used to perform arbitrary drawing on the timeline which
     * must stretch across multiple plots.
     */
    public GlimpseAxisLayout1D getOverlayLayout( )
    {
        return this.overlayLayout;
    }

    /**
     * The layout on which the time axis markings are painted. Painters may be added
     * to this layout to draw additional decorations onto the time axis.
     */
    public GlimpseAxisLayout1D getTimelineLayout( )
    {
        return this.timeLayout;
    }

    /**
     * Returns the PlotInfo handles for all plotting areas. This includes both PlotInfo
     * handles created by {@link #createPlot(String)} and TimePlotInfo handles created
     * by {@link #createTimePlot(String)}.
     */
    public Collection<PlotInfo> getAllPlots( )
    {
        return this.delegatePlot.getAllPlots( );
    }

    /**
     * <p>Returns only the TimePlotInfo handles for plotting areas created with 
     * {@link #createTimePlot(String)}.</p>
     * 
     * Note, this may not be all the plotting areas for this StackedPlot2D if some
     * vanilla plots were created using {@link #createPlot(String)}.
     */
    public Collection<TimePlotInfo> getAllTimePlots( )
    {
        return Collections.unmodifiableCollection( this.stackedTimePlots.values( ) );
    }

    /**
     * Removes the provided plot identified via its unique string identifier.
     */
    public void deletePlot( String name )
    {
        this.delegatePlot.deletePlot( name );
        this.stackedTimePlots.remove( name );
    }

    /**
     * Returns the plot handle for the plot identified via its unique string identifier.
     * 
     * @param name a plot unique identifier
     * @return the PlotInfo handle
     */
    public PlotInfo getPlot( String name )
    {
        return this.delegatePlot.getPlot( name );
    }

    /**
     * Returns the time plot handle for the plot identified via its unique string identifier.
     * 
     * @param name a plot unique identifier
     * @return the PlotInfo handle
     */
    public TimePlotInfo getTimePlot( String name )
    {
        return this.stackedTimePlots.get( name );
    }

    public void setSelectedPlot( String name )
    {
        this.setSelectedPlot( getPlot( name ) );
    }

    public void setSelectedPlot( PlotInfo layout )
    {
        this.selectedLayout = layout;
    }

    public PlotInfo getSelectedPlot( )
    {
        return this.selectedLayout;
    }

    public void setTimelineMouseListener1D( AxisMouseListener1D listener )
    {
        if ( this.timelineMouseListener != null )
        {
            this.timeLayout.removeGlimpseMouseAllListener( this.timelineMouseListener );
        }

        this.timelineMouseListener = listener;

        if ( this.timelineMouseListener != null )
        {
            this.timeLayout.addGlimpseMouseAllListener( this.timelineMouseListener );
        }
    }

    public AxisMouseListener1D getTimelineMouseListener1D( )
    {
        return this.timelineMouseListener;
    }

    /**
     * Sets whether or not locking of the selected region is allowed for all
     * timeline and plot axes. This setting will also affect newly created plots.
     * 
     * @param lock whether to allow locking of the selected region
     * @see AxisMouseListener#setAllowSelectionLock(boolean)
     */
    public void setAllowSelectionLock( boolean lock )
    {
        this.allowSelectionLock = lock;

        this.timelineMouseListener.setAllowSelectionLock( lock );

        for ( TimePlotInfo info : getAllTimePlots( ) )
        {
            info.getTimelineMouseListener( ).setAllowSelectionLock( lock );
        }
    }

    /**
     * Sets whether or not zooming of the Y axis is allowed for all
     * timeline and plot axes. This setting will also affect newly created plots.
     * 
     * @param lock whether to allow zooming of the Y axis
     * @see AxisMouseListener#setAllowZoomY(boolean)
     */
    public void setAllowZoomY( boolean lock )
    {
        this.allowZoomY = lock;

        if ( this.getOrientation( ) == Orientation.HORIZONTAL )
        {
            this.timelineMouseListener.setAllowZoom( lock );
        }

        for ( TimePlotInfo info : getAllTimePlots( ) )
        {
            info.getTimelineMouseListener( ).setAllowZoomY( lock );
        }
    }

    /**
     * Sets whether or not zooming of the X axis is allowed for all
     * timeline and plot axes. This setting will also affect newly created plots.
     * 
     * @param lock whether to allow zooming of the X axis
     * @see AxisMouseListener#setAllowZoomX(boolean)
     */
    public void setAllowZoomX( boolean lock )
    {
        this.allowZoomX = lock;

        if ( this.getOrientation( ) == Orientation.VERTICAL )
        {
            this.timelineMouseListener.setAllowZoom( lock );
        }

        for ( TimePlotInfo info : getAllTimePlots( ) )
        {
            info.getTimelineMouseListener( ).setAllowZoomX( lock );
        }
    }

    /**
     * Sets whether or not panning of the Y axis is allowed for all
     * timeline and plot axes. This setting will also affect newly created plots.
     * 
     * @param lock whether to allow panning of the Y axis
     * @see AxisMouseListener#setAllowPanY(boolean)
     */
    public void setAllowPanY( boolean lock )
    {
        this.allowPanY = lock;

        if ( this.getOrientation( ) == Orientation.HORIZONTAL )
        {
            this.timelineMouseListener.setAllowPan( lock );
        }

        for ( TimePlotInfo info : getAllTimePlots( ) )
        {
            info.getTimelineMouseListener( ).setAllowPanY( lock );
        }
    }

    /**
     * Sets whether or not panning of the X axis is allowed for all
     * timeline and plot axes. This setting will also affect newly created plots.
     * 
     * @param lock whether to allow panning of the X axis
     * @see AxisMouseListener#setAllowPanX(boolean)
     */
    public void setAllowPanX( boolean lock )
    {
        this.allowPanX = lock;

        if ( this.getOrientation( ) == Orientation.VERTICAL )
        {
            this.timelineMouseListener.setAllowPan( lock );
        }

        for ( TimePlotInfo info : getAllTimePlots( ) )
        {
            info.getTimelineMouseListener( ).setAllowPanX( lock );
        }
    }

    /**
     * Get the TaggedAxis1D Tag which defines the currently selected time.
     *
     * @return the current time selection Tag
     */
    public Tag getTimeSelection( )
    {
        return this.currentTag;
    }

    /**
     * Get the TaggedAxis1D Tag which defines the earliest endpoint of the
     * selected time region.
     *
     * @return the earliest time selection Tag
     */
    public Tag getTimeSelectionMin( )
    {
        return this.minTag;
    }

    /**
     * Get the TaggedAxis1D Tag which defines the latest endpoint of the
     * selected time region.
     *
     * @return the latest time selection Tag
     */
    public Tag getTimeSelectionMax( )
    {
        return this.maxTag;
    }

    public Orientation getOrientation( )
    {
        return this.delegatePlot.getOrientation( );
    }

    public Epoch getEpoch( )
    {
        return this.epoch;
    }

    public void setEpoch( Epoch epoch )
    {
        this.epoch = epoch;
        this.timeAxisPainter.setEpoch( epoch );
    }

    public TimeStamp toTimeStamp( double value )
    {
        return epoch.toTimeStamp( value );
    }

    public double fromTimeStamp( TimeStamp value )
    {
        return epoch.fromTimeStamp( value );
    }

    public TaggedAxis1D getTimeAxis( )
    {
        return ( TaggedAxis1D ) this.delegatePlot.getCommonAxis( );
    }

    public void setTimeAxisPainter( TimeAxisPainter painter )
    {
        this.timeAxisDelegate.removePainter( this.timeAxisPainter );
        this.timeAxisPainter = painter;
        this.timeAxisDelegate.addPainter( this.timeAxisPainter );
    }

    public TimeAxisPainter getTimeAxisPainter( )
    {
        return this.timeAxisPainter;
    }

    public SimpleTextPainter getTimeUnitsPainter( )
    {
        return this.timeUnitsPainter;
    }

    public BorderPainter getTimeAxisBorderPainter( )
    {
        return this.timeAxisBorderPainter;
    }

    public PlotInfo getTimelinePlotInfo( )
    {
        return this.timelineInfo;
    }

    public void setAxisColor( float[] rgba )
    {
        this.timeAxisPainter.setTextColor( rgba );
        this.timeAxisPainter.setTickColor( rgba );
    }

    public void setAxisFont( Font font )
    {
        this.timeAxisPainter.setFont( font );
    }

    public void setShowCurrentTime( boolean show )
    {
        this.timeAxisPainter.showCurrentTimeLabel( show );
    }

    public void setCurrentTimeColor( float[] rgba )
    {
        this.timeAxisPainter.setCurrentTimeTextColor( rgba );
        this.timeAxisPainter.setCurrentTimeTickColor( rgba );
    }

    public void setBackgroundColor( float[] rgba )
    {
        this.delegatePlot.setBackgroundColor( rgba );
    }

    public void setBorderSize( int size )
    {
        this.delegatePlot.setBorderSize( size );
    }
    
    public void setLabelLayoutSize( int size )
    {
        this.labelLayoutSize = size;
        
        this.delegatePlot.validateLayout( );
    }

    public void validate( )
    {
        this.delegatePlot.validate( );
    }

    public boolean isTimeAxisHorizontal( )
    {
        return delegatePlot.getOrientation( ) == Orientation.VERTICAL;
    }

    /**
     * Pushes the layout stack for the named plot onto the provided
     * GlimpseTargetStack.
     *
     * @param name
     *            the name of the plot
     * @return a relative GlimpseTargetStack for the named plot
     */
    public GlimpseTargetStack pushLayoutTargetStack( GlimpseTargetStack stack, String name )
    {
        stack = pushPlotTargetStack( stack );
        PlotInfo plot = delegatePlot.getPlot( name );
        stack.push( plot.getLayout( ) );
        return stack;
    }

    /**
     * Pushes the layout stack for the base layout of this StackedTimePlot2D
     * onto the provided GlimpseTargetStack.
     *
     * @return a relative GlimpseTargetStack for the timeline plot background
     *         layout
     */
    public GlimpseTargetStack pushPlotTargetStack( GlimpseTargetStack stack )
    {
        stack.push( this );
        stack.push( delegatePlot );
        return stack;
    }

    /**
     * @see #createPlot( String, Axis1D )
     */
    public PlotInfo createPlot( String name )
    {
        return this.createPlot( name, new Axis1D( ) );
    }

    /**
     * Creates a plotting area with one common time axis and attaches a mouse
     * listener which handles properly adjusting the time selection on the time
     * axis. Returns a handle which may be used for adding GlimpsePainter to the
     * plot or adjusting its size, order, and other display characteristics.
     *
     * @param name the unique identifier of the plot to create
     * @param axis the non-shared / non-time data axis for the plot
     * @return a handle to the newly created plot
     */
    public PlotInfo createPlot( String name, Axis1D axis )
    {
        PlotInfo layoutInfo = delegatePlot.createPlot( name, axis );

        attachTimelineMouseListener( layoutInfo );

        return layoutInfo;
    }

    /**
     * @see #createPlot(String, Axis1D )
     */
    public TimePlotInfo createTimePlot( String name )
    {
        return createTimePlot( name, new Axis1D( ) );
    }

    /**
     * Creates a plot similar to {@code createPlot( String, Axis1D )} but with
     * additional plot decorations, including: grid lines, axes labels for the
     * data axis, and a text label describing the plot.
     *
     * @see #createPlot(String, Axis1D )
     */
    public TimePlotInfo createTimePlot( String name, Axis1D axis )
    {
        PlotInfo layoutInfo = delegatePlot.createPlot( name, axis );

        // create a tick handler to calculate Y axis tick marks
        GridAxisLabelHandler labelHandler = new GridAxisLabelHandler( )
        {
            @Override
            protected String tickString( double number, int orderAxis )
            {
                return tickNumberFormatter.format( number );
            }

            @Override
            protected void updateFormatter( int orderAxis, int orderTick )
            {
                tickNumberFormatter.setMaximumFractionDigits( Math.abs( orderTick ) );
            }
        };

        // add a painter for user data
        DelegatePainter dataPainter = new DelegatePainter( );
        layoutInfo.getLayout( ).addPainter( dataPainter );

        // create a painter to display Y axis grid lines
        GridPainter gridPainter = new GridPainter( labelHandler, labelHandler );
        gridPainter.setShowMinorGrid( false );
        layoutInfo.getLayout( ).addPainter( gridPainter );

        // create a painter to display Y axis tick marks along the left edge of
        // the graph
        NumericXYAxisPainter axisPainter = new NumericXYAxisPainter( labelHandler, labelHandler );
        axisPainter.setFont( getDefaultPlain( 9 ), false );
        axisPainter.setShowLabelsNearOrigin( true );
        axisPainter.setShowOriginLabel( true );
        layoutInfo.getLayout( ).addPainter( axisPainter );

        // add a label to display the concept and units being displayed
        SimpleTextPainter labelPainter = new SimpleTextPainter( );
        labelPainter.setHorizontalPosition( HorizontalPosition.Center );
        labelPainter.setVerticalPosition( VerticalPosition.Top );
        labelPainter.setFont( FontUtils.getDefaultBold( 9 ), false );
        labelPainter.setPadding( 2 );
        // don't use the plot unique identifier as the label by default, this makes
        // it too easy to think that the String argument to createPlot() is supposed to be the label
        labelPainter.setText( "" );
        layoutInfo.getLayout( ).addPainter( labelPainter );

        // add a border
        BorderPainter borderPainter = new BorderPainter( );
        layoutInfo.getLayout( ).addPainter( borderPainter );

        // create a custom mouse listener
        TimelineMouseListener2D listener = attachTimelineMouseListener( layoutInfo );

        // create a GlimpseLayout which will appear to the side of the timeline and contain labels/controls
        GlimpseLayout labelLayout = new GlimpseLayout( );
        
        TimePlotInfo timePlotInfo = new TimePlotInfo( this, layoutInfo, labelLayout, listener, gridPainter, axisPainter, labelPainter, borderPainter, dataPainter );

        if ( isTimeAxisHorizontal( ) )
        {
            gridPainter.setShowVerticalLines( false );
            labelHandler.setTickSpacing( 16 );
            axisPainter.setShowVerticalTicks( true );
            axisPainter.setShowHorizontalTicks( false );
            axisPainter.setLockLeft( true );
            labelPainter.setHorizontalPosition( HorizontalPosition.Center );
            labelPainter.setVerticalPosition( VerticalPosition.Top );
        }
        else
        {
            gridPainter.setShowHorizontalLines( false );
            labelHandler.setTickSpacing( 45 );
            axisPainter.setShowVerticalTicks( false );
            axisPainter.setShowHorizontalTicks( true );
            axisPainter.setLockTop( true );
            labelPainter.setHorizontalPosition( HorizontalPosition.Center );
            labelPainter.setVerticalPosition( VerticalPosition.Bottom );
        }

        this.stackedTimePlots.put( name, timePlotInfo );

        return timePlotInfo;
    }

    public boolean isLocked( )
    {
        return isSelectionLocked( ) || isCurrentTimeLocked( );
    }

    public boolean isSelectionLocked( )
    {
        return getTimeAxis( ).isSelectionLocked( );
    }

    /**
     * Fixes the selected time region so that it will no longer follow the mouse
     * cursor.
     *
     * @param lock
     *            whether to lock or unlock the selected time region
     */
    public void setSelectionLocked( boolean lock )
    {
        getTimeAxis( ).setSelectionLock( lock );
        getTimeAxis( ).validate( );
    }

    public boolean isCurrentTimeLocked( )
    {
        return currentTimeLock;
    }

    /**
     * Fixes the selected time region and the timeline bounds with the current
     * maximum of the selected time region at the far right of the timeline.
     *
     * @param lock
     */
    public void setCurrentTimeLocked( boolean lock )
    {
        currentTimeLock = lock;

        if ( lock )
        {
            double maxValue = maxTag.getValue( );

            TimeStamp maxTime = epoch.toTimeStamp( maxValue );

            shiftTimeSelection( maxTime );
            shiftTimeAxisBounds( maxTime );
            getTimeAxis( ).lockMax( maxValue );
        }
        else
        {
            getTimeAxis( ).unlockMax( );
        }

        getTimeAxis( ).validate( );
    }

    public void setTimeSelection( TimeStamp minTime, TimeStamp selectedTime, TimeStamp maxTime )
    {
        minTag.setValue( epoch.fromTimeStamp( minTime ) );
        maxTag.setValue( epoch.fromTimeStamp( maxTime ) );
        currentTag.setValue( epoch.fromTimeStamp( selectedTime ) );

        TaggedAxis1D axis = getTimeAxis( );
        axis.validateTags( );
        axis.validate( );

        if ( isCurrentTimeLocked( ) )
        {
            shiftTimeAxisBounds( maxTime );
        }
    }

    public void setTimeAxisBounds( TimeStamp minTime, TimeStamp maxTime )
    {
        TaggedAxis1D axis = getTimeAxis( );

        axis.setMax( epoch.fromTimeStamp( maxTime ) );
        axis.setMin( epoch.fromTimeStamp( minTime ) );

        axis.validate( );

        if ( isCurrentTimeLocked( ) )
        {
            shiftTimeSelection( maxTime );
        }
    }

    public void shiftTimeAxisBounds( TimeStamp maxTime )
    {
        TaggedAxis1D axis = getTimeAxis( );

        double diff = axis.getMax( ) - axis.getMin( );
        double max = epoch.fromTimeStamp( maxTime );

        axis.setMax( max );
        axis.setMin( max - diff );

        axis.validate( );
    }

    public void setTimeSelection( TimeStamp minTime, TimeStamp maxTime )
    {
        setTimeSelection( minTime, maxTime, maxTime );
    }

    public void shiftTimeSelection( TimeStamp maxTime )
    {
        double diff = Time.fromSeconds( maxTag.getValue( ) - minTag.getValue( ) );
        TimeStamp minTime = maxTime.subtract( diff );
        setTimeSelection( minTime, maxTime, maxTime );
    }

    protected void initialize( )
    {
        TaggedAxis1D timeAxis = getTimeAxis( );

        this.addTimeTags( getTimeAxis( ) );

        this.minTag = timeAxis.getTag( MIN_TIME );
        this.maxTag = timeAxis.getTag( MAX_TIME );
        this.currentTag = timeAxis.getTag( CURRENT_TIME );

        this.timelineInfo = this.delegatePlot.createPlot( TIMELINE );

        if ( isTimeAxisHorizontal( ) )
        {
            this.timelineInfo.setSize( 45 );
            this.timelineInfo.setOrder( Integer.MAX_VALUE );

            this.timeLayout = new GlimpseAxisLayoutX( this.timelineInfo.getLayout( ) );
            
            this.labelLayoutSize = 60;
        }
        else
        {
            this.timelineInfo.setSize( 60 );
            this.timelineInfo.setOrder( Integer.MIN_VALUE );

            this.timeLayout = new GlimpseAxisLayoutY( this.timelineInfo.getLayout( ) );
        
            this.labelLayoutSize = 30;
        }

        this.timelineMouseListener = new TimelineMouseListener1D( this );
        this.timeLayout.addGlimpseMouseAllListener( this.timelineMouseListener );

        this.timeAxisPainter = createTimeAxisPainter( );
        this.timeAxisPainter.setFont( getDefaultPlain( 12 ), false );
        this.timeAxisPainter.showCurrentTimeLabel( false );
        this.timeAxisPainter.setCurrentTimeTickColor( GlimpseColor.getGreen( ) );

        this.delegatePlot.setBorderSize( 0 );

        this.timeAxisDelegate = new DelegatePainter( );
        this.timeAxisDelegate.addPainter( this.timeAxisPainter );

        this.timeLayout.addPainter( this.timeAxisDelegate );

        this.timeUnitsPainter = new SimpleTextPainter( );
        this.timeUnitsPainter.setHorizontalPosition( HorizontalPosition.Right );
        this.timeUnitsPainter.setVerticalPosition( VerticalPosition.Bottom );
        this.timeUnitsPainter.setColor( GlimpseColor.getBlack( ) );
        this.timeUnitsPainter.setFont( getDefaultBold( 12 ) );
        this.timeUnitsPainter.setText( "GMT" );
        this.timeUnitsPainter.setBackgroundColor( GlimpseColor.getYellow( ) );
        this.timeUnitsPainter.setPaintBackground( true );

        this.timeAxisBorderPainter = new BorderPainter( ).setColor( GlimpseColor.fromColorRgba( 0.8f, 0.8f, 0.8f, 1.0f ) );

        this.timeLayout.addPainter( this.timeUnitsPainter );
        this.timeLayout.addPainter( this.timeAxisBorderPainter );

        this.addLayout( this.delegatePlot );

        if ( isTimeAxisHorizontal( ) )
        {
            this.overlayLayout = new GlimpseAxisLayoutX( this, "Overlay", timeAxis );
        }
        else
        {
            this.overlayLayout = new GlimpseAxisLayoutY( this, "Overlay", timeAxis );
        }

        this.overlayLayout.setEventGenerator( true );
        this.overlayLayout.setEventConsumer( false );
        this.overlayLayout.addPainter( new SelectedTimeRegionPainter( this ) );
        this.overlayLayout.setLayoutData( "pos 0 0 container.w container.h" );
    }

    protected TimelineMouseListener2D attachTimelineMouseListener( PlotInfo layoutInfo )
    {
        TimelineMouseListener2D mouseListener = new TimelineMouseListener2D( this, layoutInfo );
        mouseListener.setAllowPanX( this.allowPanX );
        mouseListener.setAllowPanY( this.allowPanY );
        mouseListener.setAllowZoomX( this.allowZoomX );
        mouseListener.setAllowZoomY( this.allowZoomY );
        mouseListener.setAllowSelectionLock( this.allowSelectionLock );

        layoutInfo.getLayout( ).addGlimpseMouseAllListener( mouseListener );
        return mouseListener;
    }

    protected TimeAxisPainter createTimeAxisPainter( )
    {
        TimeAxisPainter painter;
        if ( isTimeAxisHorizontal( ) )
        {
            painter = new TimeXAxisPainter( this.epoch );
        }
        else
        {
            painter = new TimeYAxisPainter( this.epoch );
        }

        painter.setFont( getDefaultPlain( 12 ), false );
        painter.showCurrentTimeLabel( false );
        painter.setCurrentTimeTickColor( GlimpseColor.getGreen( ) );

        return painter;
    }

    protected void addTimeTags( TaggedAxis1D axis )
    {
        axis.addTag( MIN_TIME, 0 );
        axis.addTag( MAX_TIME, 10 );
        axis.addTag( CURRENT_TIME, 10 );

        axis.addConstraint( new Constraint( )
        {
            @Override
            public void applyConstraint( TaggedAxis1D axis )
            {
                Tag minTag = axis.getTag( MIN_TIME );
                Tag maxTag = axis.getTag( MAX_TIME );
                Tag currentTag = axis.getTag( CURRENT_TIME );

                double minValue = minTag.getValue( );
                double maxValue = maxTag.getValue( );
                double currentValue = currentTag.getValue( );

                if ( minValue > maxValue )
                {
                    minTag.setValue( maxValue );
                }

                if ( currentValue < minValue )
                {
                    currentTag.setValue( minValue );
                }
                else if ( currentValue > maxValue )
                {
                    currentTag.setValue( maxValue );
                }
            }

            @Override
            public String getName( )
            {
                return "order";
            }
        } );
    }
}

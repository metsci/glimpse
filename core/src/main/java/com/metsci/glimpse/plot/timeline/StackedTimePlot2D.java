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
package com.metsci.glimpse.plot.timeline;

import static com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation.HORIZONTAL;
import static com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation.VERTICAL;
import static com.metsci.glimpse.support.font.FontUtils.getDefaultPlain;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.axis.painter.NumericXYAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.time.AbsoluteTimeAxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.time.TimeAxisLabelHandler;
import com.metsci.glimpse.axis.tagged.OrderedConstraint;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseAllListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseMotionListener;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.painter.info.TooltipPainter;
import com.metsci.glimpse.plot.stacked.PlotInfo;
import com.metsci.glimpse.plot.stacked.StackedPlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.event.listener.EventSelectionHandler;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfoImpl;
import com.metsci.glimpse.plot.timeline.layout.TimelineInfo;
import com.metsci.glimpse.plot.timeline.listener.DataAxisMouseListener1D;
import com.metsci.glimpse.plot.timeline.listener.PlotMouseListener;
import com.metsci.glimpse.plot.timeline.listener.TimeAxisMouseListener1D;
import com.metsci.glimpse.plot.timeline.painter.SelectedTimeRegionPainter;
import com.metsci.glimpse.support.atlas.TextureAtlas;
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
public class StackedTimePlot2D extends StackedPlot2D
{
    public static final String MIN_TIME = "min_time";
    public static final String MAX_TIME = "max_time";
    public static final String CURRENT_TIME = "current_time";

    // tags representing the minimum and maximum bounds of the selected time window
    protected Tag minTag;
    protected Tag maxTag;
    // tag representing the currently selected time
    protected Tag currentTag;

    // timeline painters and listeners
    protected AxisMouseListener1D timelineMouseListener;
    protected TooltipPainter tooltipPainter;

    // painter which highlights selected time region in blue
    protected SelectedTimeRegionPainter selectedTimePainter;

    // default tick/label handler
    protected TimeAxisLabelHandler timeTickHandler;
    protected TimelineInfo defaultTimelineInfo;

    protected TextureAtlas defaultTextureAtlas;

    // the default selection handler for all EventPlotInfo
    // if client code would like individual EventPlotInfo to maintain
    // their own set of selected events, individual SelectionHandlers
    // can be set for each EventPlotInfo
    protected EventSelectionHandler commonSelectionHandler;

    protected List<PlotMouseListener> plotMouseListeners;

    // default settings for TimelineMouseListeners of new plots
    protected volatile boolean allowPanX = true;
    protected volatile boolean allowPanY = true;
    protected volatile boolean allowZoomX = true;
    protected volatile boolean allowZoomY = true;
    protected volatile boolean allowSelectionLock = true;
    protected volatile boolean currentTimeLock;

    // the size of the label layout area in pixels
    protected volatile int labelLayoutSize = 30;
    protected volatile boolean showLabelLayout = false;
    protected volatile boolean showTimeline = true;

    // epoch encapsulating the absolute time which maps to value 0 on the timeline
    protected volatile Epoch epoch;

    // the selected plot row
    protected volatile PlotInfo selectedLayout;

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

    public StackedTimePlot2D( Orientation orientation, Epoch epoch )
    {
        this( orientation, epoch, new TextureAtlas( ) );
    }

    public StackedTimePlot2D( Orientation orientation, Epoch epoch, TextureAtlas atlas )
    {
        this( orientation, epoch, atlas, null );
    }

    public StackedTimePlot2D( Orientation orientation, Epoch epoch, TaggedAxis1D commonAxis )
    {
        this( orientation, epoch, new TextureAtlas( ), commonAxis );
    }

    /**
     * Creates a StackedTimePlot2D with specified orientation. The provided
     * epoch determines what absolute timestamp corresponds to value 0.0 on the
     * time Axis1D.
     */
    public StackedTimePlot2D( Orientation orientation, Epoch epoch, TextureAtlas atlas, TaggedAxis1D commonAxis )
    {
        super( orientation, commonAxis );

        this.epoch = epoch;
        this.defaultTextureAtlas = atlas;

        this.commonSelectionHandler = new EventSelectionHandler( );

        this.plotMouseListeners = new CopyOnWriteArrayList<PlotMouseListener>( );

        this.setBorderSize( 0 );
        this.timeTickHandler = new AbsoluteTimeAxisLabelHandler( TimeZone.getTimeZone( "GMT-0:00" ), epoch );

        this.initializeTimeAxis( );
        this.defaultTimelineInfo = this.createTimeline( );
        this.initializeOverlayPainters( );
    }

    public void setTimeAxisLabelHandler( TimeAxisLabelHandler handler )
    {
        this.timeTickHandler = handler;
        this.timeTickHandler.setEpoch( this.epoch );
        
        for ( PlotInfo info : getAllPlots( ) )
        {
            if ( info instanceof TimelineInfo )
            {
                ( (TimelineInfo) info ).getAxisPainter( ).setLabelHandler( handler );
            }
        }
    }

    public TimeAxisLabelHandler getTimeAxisLabelHandler( )
    {
        return this.timeTickHandler;
    }

    public TimelineInfo getDefaultTimeline( )
    {
        return this.defaultTimelineInfo;
    }

    public void addPlotMouseListener( PlotMouseListener listener )
    {
        this.plotMouseListeners.add( listener );
    }

    public void removePlotMouseListener( PlotMouseListener listener )
    {
        this.plotMouseListeners.remove( listener );
    }

    /**
     * Returns the common EventSelectionHandler shared between all {@link EventPlotInfo}
     * sub-plots for this StackedTimePlot2D.
     */
    public EventSelectionHandler getEventSelectionHander( )
    {
        return this.commonSelectionHandler;
    }

    public void setShowTimeline( boolean showTimeline )
    {
        this.showTimeline = showTimeline;

        for ( PlotInfo plot : getAllPlots( ) )
        {
            if ( plot instanceof TimelineInfo ) plot.getLayout( ).setVisible( showTimeline );
        }

        if ( this.isAutoValidate( ) ) this.validate( );
    }

    public boolean isShowTimeline( )
    {
        return this.showTimeline;
    }

    @Override
    public int getOverlayLayoutOffsetX( )
    {
        return orient == VERTICAL ? getLabelSize( ) : 0;
    }

    @Override
    public int getOverlayLayoutOffsetY2( )
    {
        return orient == VERTICAL ? 0 : getLabelSize( );
    }

    @Override
    public void setPlotSpacing( int size )
    {
        this.plotSpacing = size;

        for ( PlotInfo info : stackedPlots.values( ) )
        {
            // don't automatically change the timeline info plot spacing
            if ( info instanceof TimelineInfo ) continue;

            info.setPlotSpacing( size );
        }

        if ( this.isAutoValidate( ) ) this.validate( );
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

    public GlimpseLayout getFullOverlayLayout( )
    {
        return this.fullOverlayLayout;
    }

    public GlimpseAxisLayout1D getUnderlayLayout( )
    {
        return this.underlayLayout;
    }

    public TooltipPainter getTooltipPainter( )
    {
        return this.tooltipPainter;
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
        this.lock.lock( );
        try
        {
            List<TimePlotInfo> list = new LinkedList<TimePlotInfo>( );

            for ( PlotInfo plot : getAllPlots( ) )
            {
                if ( plot instanceof TimePlotInfo ) list.add( ( TimePlotInfo ) plot );
            }

            return list;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    /**
     * Returns the timeline handle for the timeline identified via its unique string identifier.
     * 
     * @param id a timeline unique identifier
     * @return the TimelineInfo handle
     */
    public TimelineInfo getTimeline( Object id )
    {
        this.lock.lock( );
        try
        {
            PlotInfo plot = getPlot( id );

            if ( plot instanceof TimelineInfo )
            {
                return ( TimelineInfo ) plot;
            }
            else
            {
                return null;
            }
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    /**
     * Returns the time plot handle for the plot identified via its unique string identifier.
     * 
     * @param id a plot unique identifier
     * @return the TimePlotInfo handle
     */
    public TimePlotInfo getTimePlot( Object id )
    {
        this.lock.lock( );
        try
        {
            PlotInfo plot = getPlot( id );

            if ( plot instanceof TimePlotInfo )
            {
                return ( TimePlotInfo ) plot;
            }
            else
            {
                return null;
            }
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    /**
     * Returns the event plot handle for the plot identified via its unique string identifier.
     * 
     * @param id a plot unique identifier
     * @return the EventPlotInfo handle
     */
    public EventPlotInfo getEventPlot( Object id )
    {
        this.lock.lock( );
        try
        {
            PlotInfo plot = getPlot( id );

            if ( plot instanceof EventPlotInfo )
            {
                return ( EventPlotInfo ) plot;
            }
            else
            {
                return null;
            }
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void setSelectedPlot( Object id )
    {
        this.setSelectedPlot( getPlot( id ) );
    }

    public void setSelectedPlot( PlotInfo layout )
    {
        this.selectedLayout = layout;
    }

    public PlotInfo getSelectedPlot( )
    {
        return this.selectedLayout;
    }

    public void setTimeAxisMouseListener( AxisMouseListener1D listener )
    {
        this.lock.lock( );
        try
        {
            if ( this.timelineMouseListener != null )
            {
                this.underlayLayout.removeGlimpseMouseAllListener( this.timelineMouseListener );
            }

            if ( listener != null )
            {
                this.underlayLayout.addGlimpseMouseAllListener( listener );
            }

            this.timelineMouseListener = listener;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public AxisMouseListener1D getTimeAxisMouseListener( )
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
        this.lock.lock( );
        try
        {
            this.allowSelectionLock = lock;

            this.timelineMouseListener.setAllowSelectionLock( lock );

            for ( TimePlotInfo info : getAllTimePlots( ) )
            {
                info.getDataAxisMouseListener( ).setAllowSelectionLock( lock );
            }
        }
        finally
        {
            this.lock.unlock( );
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
        this.lock.lock( );
        try
        {
            this.allowZoomY = lock;

            if ( this.getOrientation( ) == Orientation.HORIZONTAL )
            {
                this.timelineMouseListener.setAllowZoom( lock );
            }
            else
            {
                for ( TimePlotInfo info : getAllTimePlots( ) )
                {
                    info.getDataAxisMouseListener( ).setAllowZoom( lock );
                }
            }
        }
        finally
        {
            this.lock.unlock( );
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
        this.lock.lock( );
        try
        {
            this.allowZoomX = lock;

            if ( this.getOrientation( ) == Orientation.VERTICAL )
            {
                this.timelineMouseListener.setAllowZoom( lock );
            }
            else
            {
                for ( TimePlotInfo info : getAllTimePlots( ) )
                {
                    info.getDataAxisMouseListener( ).setAllowZoom( lock );
                }
            }
        }
        finally
        {
            this.lock.unlock( );
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
        this.lock.lock( );
        try
        {
            this.allowPanY = lock;

            if ( this.getOrientation( ) == Orientation.HORIZONTAL )
            {
                this.timelineMouseListener.setAllowPan( lock );
            }
            else
            {
                for ( TimePlotInfo info : getAllTimePlots( ) )
                {
                    info.getDataAxisMouseListener( ).setAllowPan( lock );
                }
            }
        }
        finally
        {
            this.lock.unlock( );
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
        this.lock.lock( );
        try
        {
            this.allowPanX = lock;

            if ( this.getOrientation( ) == Orientation.VERTICAL )
            {
                this.timelineMouseListener.setAllowPan( lock );
            }
            else
            {
                for ( TimePlotInfo info : getAllTimePlots( ) )
                {
                    info.getDataAxisMouseListener( ).setAllowPan( lock );
                }
            }
        }
        finally
        {
            this.lock.unlock( );
        }

    }

    /**
     * Get the TaggedAxis1D Tag which defines the currently selected time.
     *
     * @return the current time selection Tag
     */
    public Tag getTimeSelectionTag( )
    {
        return this.currentTag;
    }

    /**
     * Get the TaggedAxis1D Tag which defines the earliest endpoint of the
     * selected time region.
     *
     * @return the earliest time selection Tag
     */
    public Tag getTimeSelectionMinTag( )
    {
        return this.minTag;
    }

    /**
     * Get the TaggedAxis1D Tag which defines the latest endpoint of the
     * selected time region.
     *
     * @return the latest time selection Tag
     */
    public Tag getTimeSelectionMaxTag( )
    {
        return this.maxTag;
    }

    /**
     * Get the currently selected time (usually equal to getTimeSelectionMax()).
     */
    public TimeStamp getTimeSelection( )
    {
        return epoch.toTimeStamp( currentTag.getValue( ) );
    }

    /**
     * Get the TimeStamp of earliest endpoint of the selected time region.
     */
    public TimeStamp getTimeSelectionMin( )
    {
        return epoch.toTimeStamp( minTag.getValue( ) );
    }

    /**
     * Get the TimeStamp of latest endpoint of the selected time region.
     */
    public TimeStamp getTimeSelectionMax( )
    {
        return epoch.toTimeStamp( maxTag.getValue( ) );
    }

    public Epoch getEpoch( )
    {
        return this.epoch;
    }

    public void setEpoch( Epoch epoch )
    {
        this.lock.lock( );
        try
        {
            this.epoch = epoch;

            this.timeTickHandler.setEpoch( epoch );

            for ( PlotInfo info : getAllPlots( ) )
            {
                if ( info instanceof TimelineInfo )
                {
                    ( ( TimelineInfo ) info ).setEpoch( epoch );
                }
            }
        }
        finally
        {
            this.lock.unlock( );
        }
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
        return ( TaggedAxis1D ) this.commonAxis;
    }

    public SelectedTimeRegionPainter getSelectedTimePainter( )
    {
        return this.selectedTimePainter;
    }

    public void setLabelSize( int size )
    {
        this.labelLayoutSize = size;
        this.validateLayout( );
    }

    public void setShowLabels( boolean show )
    {
        this.showLabelLayout = show;
        this.validateLayout( );
    }

    /**
     * Method name changed to be more consistent with other setters.
     * @deprecated {@link #setShowLabels(boolean)}
     */
    public void showLabels( boolean show )
    {
        this.setShowLabels( show );
    }

    public int getLabelSize( )
    {
        return showLabelLayout ? this.labelLayoutSize : 0;
    }

    public boolean isShowLabels( )
    {
        return this.showLabelLayout;
    }

    public boolean isTimeAxisHorizontal( )
    {
        return getOrientation( ) == Orientation.VERTICAL;
    }

    /**
     * Pushes the layout stack for the named plot onto the provided
     * GlimpseTargetStack.
     *
     * @param id unique identifier for the plot
     * @return a relative GlimpseTargetStack for the named plot
     */
    public GlimpseTargetStack pushLayoutTargetStack( GlimpseTargetStack stack, Object id )
    {
        stack = pushPlotTargetStack( stack );
        PlotInfo plot = getPlot( id );
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
        return stack;
    }

    /**
     * @see #createPlot(Object )
     */
    public TimePlotInfo createTimePlot( )
    {
        return createTimePlot( UUID.randomUUID( ) );
    }

    /**
     * @see #createPlot(Object, Axis1D )
     */
    public TimePlotInfo createTimePlot( Object id )
    {
        return createTimePlot( id, new Axis1D( ) );
    }

    /**
     * Creates a plot similar to {@code createPlot( String, Axis1D )} but with
     * additional plot decorations, including: grid lines, axes labels for the
     * data axis, and a text label describing the plot.
     *
     * @see #createPlot(Object, Axis1D )
     */
    public TimePlotInfo createTimePlot( Object id, Axis1D axis )
    {
        this.lock.lock( );
        try
        {
            PlotInfo plotInfo = createPlot0( id, axis );
            TimePlotInfo timePlotInfo = createTimePlot0( plotInfo );
            stackedPlots.put( id, timePlotInfo );
            addTimePlotInfoListeners( timePlotInfo );

            if ( isAutoValidate( ) ) validate( );

            return timePlotInfo;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    /**
     * Creates a labeled timeline (with tick marks and date/time labels). This method may
     * be called multiple times with different time zones if multiple timezone labels are
     * desired.
     */
    public TimelineInfo createTimeline( )
    {
        return createTimeline( UUID.randomUUID( ), TimeZone.getTimeZone( "GMT-0:00" ) );
    }

    public TimelineInfo createTimeline( Object id, TimeZone timeZone )
    {
        this.lock.lock( );
        try
        {
            PlotInfo info = createPlot( id );
            TimelineInfo timelineInfo = new TimelineInfo( this, info );

            timelineInfo.setTimeZone( timeZone );

            this.stackedPlots.put( timelineInfo.getId( ), timelineInfo );
            if ( isAutoValidate( ) ) this.validate( );

            return timelineInfo;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public TextureAtlas getTextureAtlas( )
    {
        return this.defaultTextureAtlas;
    }

    public EventPlotInfo createEventPlot( )
    {
        return createEventPlot( UUID.randomUUID( ) );
    }

    public EventPlotInfo createEventPlot( Object id )
    {
        return createEventPlot( id, defaultTextureAtlas );
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
     * @param lock whether to lock or unlock the selected time region
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

    public TimeStamp getTime( GlimpseMouseEvent e )
    {
        if ( isTimeAxisHorizontal( ) )
        {
            return getEpoch( ).toTimeStamp( e.getAxisCoordinatesX( ) );
        }
        else
        {
            return getEpoch( ).toTimeStamp( e.getAxisCoordinatesY( ) );
        }
    }

    protected void initializeTimeAxis( )
    {
        TaggedAxis1D timeAxis = getTimeAxis( );

        this.addTimeTags( getTimeAxis( ) );

        this.minTag = timeAxis.getTag( MIN_TIME );
        this.maxTag = timeAxis.getTag( MAX_TIME );
        this.currentTag = timeAxis.getTag( CURRENT_TIME );
    }

    protected void initializeOverlayPainters( )
    {
        this.selectedTimePainter = new SelectedTimeRegionPainter( this );

        this.tooltipPainter = new TooltipPainter( this.defaultTextureAtlas );
        this.overlayLayout.addGlimpseMouseMotionListener( new GlimpseMouseMotionListener( )
        {
            @Override
            public void mouseMoved( GlimpseMouseEvent e )
            {
                tooltipPainter.setLocation( e );
            }
        } );

        this.overlayLayout.addPainter( this.selectedTimePainter );
        this.overlayLayout.addPainter( this.tooltipPainter );

        this.timelineMouseListener = createTimeAxisListener( );
        this.underlayLayout.addGlimpseMouseAllListener( this.timelineMouseListener );
    }

    protected DataAxisMouseListener1D createDataAxisListener( PlotInfo plotInfo )
    {
        return new DataAxisMouseListener1D( this, plotInfo );
    }

    protected TaggedAxisMouseListener1D createTimeAxisListener( )
    {
        return new TimeAxisMouseListener1D( this );
    }

    protected void addTimeTags( TaggedAxis1D axis )
    {
        axis.addTag( MIN_TIME, 0 );
        axis.addTag( MAX_TIME, 10 );
        axis.addTag( CURRENT_TIME, 10 );

        axis.addConstraint( new OrderedConstraint( "order", Arrays.asList( MIN_TIME, CURRENT_TIME, MAX_TIME ) ) );
    }

    @Override
    protected TaggedAxis1D createCommonAxis( )
    {
        return new TaggedAxis1D( );
    }

    protected EventPlotInfo createEventPlot( Object id, TextureAtlas atlas )
    {
        this.lock.lock( );
        try
        {
            PlotInfo plotInfo = createPlot0( id, new Axis1D( ) );
            EventPlotInfo eventPlotInfo = createEventPlot0( plotInfo, atlas );
            stackedPlots.put( id, eventPlotInfo );
            addTimePlotInfoListeners( eventPlotInfo );

            if ( isAutoValidate( ) ) validate( );

            return eventPlotInfo;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    protected EventPlotInfo createEventPlot0( PlotInfo plotInfo, TextureAtlas atlas )
    {
        TimePlotInfo timePlot = createTimePlot0( plotInfo );

        // don't show axes
        timePlot.getAxisPainter( ).setVisible( false );
        // don't show grid lines
        timePlot.getGridPainter( ).setVisible( false );
        // center the labels because the plots are so small anyway
        if ( isTimeAxisHorizontal( ) )
        {
            timePlot.getLabelPainter( ).setVerticalPosition( VerticalPosition.Center );
            timePlot.getLabelPainter( ).setHorizontalPosition( HorizontalPosition.Left );
        }
        else
        {
            timePlot.getLabelPainter( ).setVerticalPosition( VerticalPosition.Center );
            timePlot.getLabelPainter( ).setHorizontalPosition( HorizontalPosition.Center );
        }

        // the TimeAxisMouseListener1D for all plots is attached to the underlay layout
        // thus we need to let events fall through if they are not handled
        timePlot.getLayout( ).setEventConsumer( false );

        EventPlotInfo eventPlotInfo = new EventPlotInfo( timePlot, atlas );
        eventPlotInfo.setLookAndFeel( laf );
        eventPlotInfo.setSelectionHandler( commonSelectionHandler );

        return eventPlotInfo;
    }

    protected EventPlotInfo createEventPlot0( PlotInfo plotInfo )
    {
        return createEventPlot0( plotInfo, defaultTextureAtlas );
    }

    protected TimePlotInfo createTimePlot0( PlotInfo plotInfo )
    {
        // create a tick handler to calculate Y axis tick marks
        GridAxisLabelHandler labelHandler = new GridAxisLabelHandler( )
        {
            @Override
            protected String tickString( Axis1D axis, double number, int orderAxis )
            {
                return tickNumberFormatter.format( number );
            }

            @Override
            protected void updateFormatter( int orderAxis, int orderTick )
            {
                tickNumberFormatter.setMaximumFractionDigits( Math.abs( orderTick ) );
            }
        };

        GlimpseAxisLayout2D layout2D = plotInfo.getLayout( );
        layout2D.setEventConsumer( false );

        GlimpseAxisLayout2D plotLayout = new GlimpseAxisLayout2D( layout2D, String.format( "%s-plot", plotInfo.getId( ) ) );
        plotLayout.setEventConsumer( false );

        BackgroundPainter backgroundPainter = new BackgroundPainter( false );
        plotLayout.addPainter( backgroundPainter, Integer.MIN_VALUE );

        // add a painter for user data
        DelegatePainter dataPainter = new DelegatePainter( );
        plotLayout.addPainter( dataPainter );

        AxisLabelHandler xHandler, yHandler;
        if ( orient == HORIZONTAL )
        {
            xHandler = labelHandler;
            yHandler = timeTickHandler;
        }
        else
        {
            yHandler = labelHandler;
            xHandler = timeTickHandler;
        }

        // create a painter to display Y axis grid lines
        GridPainter gridPainter = new GridPainter( xHandler, yHandler );
        gridPainter.setShowMinorGrid( false );
        plotLayout.addPainter( gridPainter );

        // create a painter to display Y axis tick marks along the left edge of the graph
        NumericXYAxisPainter axisPainter = new NumericXYAxisPainter( xHandler, yHandler );
        axisPainter.setFont( getDefaultPlain( 9 ), false );
        axisPainter.setShowLabelsNearOrigin( true );
        axisPainter.setShowOriginLabel( true );
        plotLayout.addPainter( axisPainter );

        // add a border
        BorderPainter borderPainter = new BorderPainter( );
        plotLayout.addPainter( borderPainter );

        // create a custom mouse listener for the data (non-time) axis
        DataAxisMouseListener1D listener = createDataAxisListener( plotInfo );
        GlimpseAxisLayout1D layout1D;
        if ( orient == HORIZONTAL )
        {
            layout1D = new GlimpseAxisLayoutX( plotLayout );
        }
        else
        {
            layout1D = new GlimpseAxisLayoutY( plotLayout );
        }
        layout1D.setEventConsumer( false );
        layout1D.addGlimpseMouseAllListener( listener );

        // the TimeAxisMouseListener1D for all plots is attached to the underlay layout
        // thus we need to let events fall through if they are not handled
        plotLayout.setEventConsumer( false );

        // create a GlimpseLayout which will appear to the side of the timeline and contain labels/controls
        GlimpseLayout labelLayout = new GlimpseLayout( layout2D, String.format( "%s-label", plotInfo.getId( ) ) );

        // add a label to display the plot title
        SimpleTextPainter labelPainter = new SimpleTextPainter( );
        labelPainter.setHorizontalPosition( HorizontalPosition.Center );
        labelPainter.setVerticalPosition( VerticalPosition.Center );
        labelPainter.setFont( FontUtils.getDefaultBold( 9 ), false );
        labelPainter.setPadding( 2 );
        // don't use the plot unique identifier as the label by default, this makes
        // it too easy to think that the String argument to createPlot() is supposed to be the label
        labelPainter.setText( "" );
        labelPainter.setHorizontalLabels( false );
        labelLayout.addPainter( labelPainter );

        // add a border
        BorderPainter labelBorderPainter = new BorderPainter( );
        labelBorderPainter.setVisible( false );
        labelLayout.addPainter( labelBorderPainter );

        //@formatter:off
        TimePlotInfo timePlotInfo = new TimePlotInfoImpl( StackedTimePlot2D.this,
                                                      plotInfo,
                                                      plotLayout,
                                                      labelLayout,
                                                      listener,
                                                      gridPainter,
                                                      axisPainter,
                                                      labelPainter,
                                                      borderPainter,
                                                      labelBorderPainter,
                                                      backgroundPainter,
                                                      dataPainter );
        //@formatter:on

        if ( orient == HORIZONTAL )
        {
            gridPainter.setShowHorizontalLines( false );
            labelHandler.setTickSpacing( 45 );
            axisPainter.setShowVerticalTicks( false );
            axisPainter.setShowHorizontalTicks( true );
            axisPainter.setLockTop( true );
        }
        else
        {
            gridPainter.setShowVerticalLines( false );
            labelHandler.setTickSpacing( 16 );
            axisPainter.setShowVerticalTicks( true );
            axisPainter.setShowHorizontalTicks( false );
            axisPainter.setLockLeft( true );
        }

        timePlotInfo.setLookAndFeel( laf );

        return timePlotInfo;
    }

    @Override
    public PlotInfo createPlot( Object id, Axis1D axis )
    {
        PlotInfo info = super.createPlot( id, axis );
        addPlotInfoListeners( info );
        return info;
    }

    protected void addPlotInfoListeners( PlotInfo info )
    {
        info.getLayout( ).addGlimpseMouseAllListener( createPlotMouseListener( info, PlotMouseListener.PlotLocation.Plot ) );
    }

    protected void addTimePlotInfoListeners( TimePlotInfo info )
    {
        info.getLabelLayout( ).addGlimpseMouseAllListener( createPlotMouseListener( info, PlotMouseListener.PlotLocation.Label ) );
        info.getLayout( ).addGlimpseMouseAllListener( createPlotMouseListener( info, PlotMouseListener.PlotLocation.Plot ) );
    }

    protected GlimpseMouseAllListener createPlotMouseListener( final PlotInfo info, final PlotMouseListener.PlotLocation location )
    {
        return new GlimpseMouseAllListener( )
        {
            @Override
            public void mouseWheelMoved( GlimpseMouseEvent e )
            {
                for ( PlotMouseListener listener : plotMouseListeners )
                {
                    listener.mouseWheelMoved( e, info, location );
                }
            }

            @Override
            public void mouseMoved( GlimpseMouseEvent e )
            {
                for ( PlotMouseListener listener : plotMouseListeners )
                {
                    listener.mouseMoved( e, info, location );
                }
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent e )
            {
                for ( PlotMouseListener listener : plotMouseListeners )
                {
                    listener.mouseReleased( e, info, location );
                }
            }

            @Override
            public void mousePressed( GlimpseMouseEvent e )
            {
                for ( PlotMouseListener listener : plotMouseListeners )
                {
                    listener.mousePressed( e, info, location );
                }
            }

            @Override
            public void mouseExited( GlimpseMouseEvent e )
            {
                for ( PlotMouseListener listener : plotMouseListeners )
                {
                    listener.mouseExited( e, info, location );
                }
            }

            @Override
            public void mouseEntered( GlimpseMouseEvent e )
            {
                for ( PlotMouseListener listener : plotMouseListeners )
                {
                    listener.mouseEntered( e, info, location );
                }
            }
        };
    }
}
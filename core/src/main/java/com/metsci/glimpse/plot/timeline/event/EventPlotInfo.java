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
package com.metsci.glimpse.plot.timeline.event;

import static com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode.Ellipsis;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.event.mouse.GlimpseMouseAllListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.painter.info.TooltipPainter;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.event.Event.OverlapRenderingMode;
import com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode;
import com.metsci.glimpse.plot.timeline.event.listener.DragListener;
import com.metsci.glimpse.plot.timeline.event.listener.EventPlotListener;
import com.metsci.glimpse.plot.timeline.event.listener.EventSelectionHandler;
import com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter;
import com.metsci.glimpse.plot.timeline.event.paint.EventPainterManager;
import com.metsci.glimpse.plot.timeline.event.paint.GroupedEventPainter;
import com.metsci.glimpse.plot.timeline.event.paint.GroupedEventPainterAdapter;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfoWrapper;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.util.io.StreamOpener;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * <p>A handle to one of the plotting areas making up a {@link StackedTimePlot2D}. This
 * is a specialized plotting area which supports display and manipulation of
 * {@link Event} objects.</p>
 *
 * <p>For an example of this plot in use, see
 * {@link com.mcom.metsci.glimpse.examples.stacked.CollapsibleTimelinePlotExample}.</p>
 *
 * @author ulman
 */
public class EventPlotInfo extends TimePlotInfoWrapper implements TimePlotInfo
{
    private static final Logger logger = Logger.getLogger( EventPlotInfo.class.getName( ) );

    public static final int DEFAULT_ROW_SIZE = 26;
    public static final int DEFAULT_BUFFER_SIZE = 2;

    protected EventManager eventManager;
    protected EventPainterManager eventPainterManager;
    protected GlimpseAxisLayout1D layout1D;

    protected int maxRowCount = Integer.MAX_VALUE;
    protected int minRowCount = 0;
    protected int rowSize;
    protected int eventPadding;

    protected List<EventPlotListener> eventListeners;

    protected boolean isHorizontal;

    protected EventToolTipHandler eventToolTipHandler;
    protected DragListener dragListener;
    protected TooltipListener tooltipListener;

    protected TextRenderingMode textRenderingMode = Ellipsis;

    protected EventSelectionHandler selectionHandler;

    protected Object defaultIconId;
    protected int defaultIconSize = 0;
    protected boolean useDefaultIconSize = false;

    public EventPlotInfo( TimePlotInfo delegate )
    {
        this( delegate, new TextureAtlas( ) );
    }

    public EventPlotInfo( TimePlotInfo delegate, TextureAtlas atlas )
    {
        super( delegate );

        try
        {
            defaultIconId = UUID.randomUUID( );
            BufferedImage defaultImage = ImageIO.read( StreamOpener.fileThenResource.openForRead( "icons/timeline/dot.png" ) );
            atlas.loadImage( defaultIconId, defaultImage );
        }
        catch ( IOException e )
        {
            logWarning( logger, "Trouble loading default icon.", e );
        }

        final Epoch epoch = getStackedTimePlot( ).getEpoch( );
        this.isHorizontal = getStackedTimePlot( ).isTimeAxisHorizontal( );

        if ( isHorizontal )
        {
            this.layout1D = new GlimpseAxisLayoutX( getLayout( ), "EventLayout1D" );
        }
        else
        {
            this.layout1D = new GlimpseAxisLayoutY( getLayout( ), "EventLayout1D" );
        }

        // layout1D should completely cover its parent layout
        this.layout1D.setLayoutData( "pos container.x container.y container.x2 container.y2" );
        // un-handled events should be passed on to the parent layout
        this.layout1D.setEventConsumer( false );
        this.eventManager = new EventManager( this );
        this.eventPainterManager = new EventPainterManager( this, eventManager, epoch, atlas );
        this.eventPainterManager.setEventPainter( new GroupedEventPainterAdapter( new DefaultEventPainter( ) ) );
        this.layout1D.addPainter( this.eventPainterManager );

        this.eventListeners = new CopyOnWriteArrayList<EventPlotListener>( );

        this.layout1D.addGlimpseMouseAllListener( new EventListener( ) );

        this.dragListener = new DragListener( this );
        this.addEventPlotListener( dragListener );
        this.layout1D.addGlimpseMouseAllListener( dragListener );

        this.tooltipListener = new TooltipListener( );
        this.addEventPlotListener( tooltipListener );

        this.info.setTimeToolTipHandler( null );
        this.eventToolTipHandler = new EventToolTipHandler( )
        {
            @Override
            public void setToolTip( EventSelection selection, TooltipPainter tooltipPainter )
            {
                Event event = selection.getEvent( );

                if ( event.hasChildren( ) && event.getLabel( ) == null && event.getToolTipText( ) == null )
                {
                    List<Object> icons = new ArrayList<Object>( event.getEventCount( ) );
                    List<float[]> colors = new ArrayList<float[]>( event.getEventCount( ) );
                    StringBuilder b = new StringBuilder( );

                    Iterator<Event> iter = event.iterator( );
                    while ( iter.hasNext( ) )
                    {
                        Event child = iter.next( );

                        Object iconId = child.getIconId( );
                        float[] iconColor = null;
                        if ( iconId == null )
                        {
                            iconId = defaultIconId;
                            iconColor = DefaultEventPainter.getBackgroundColor( child, child.getEventPlotInfo( ), selectionHandler.isEventSelected( child ) );
                        }

                        icons.add( iconId );
                        colors.add( iconColor );

                        b.append( child.getLabel( ) );
                        if ( iter.hasNext( ) ) b.append( "\n" );
                    }

                    tooltipPainter.setText( b.toString( ) );
                    tooltipPainter.setIcons( icons, colors );
                }
                else
                {
                    String label = event.getLabel( ) == null ? "" : event.getLabel( );
                    String tip = event.getToolTipText( ) == null ? "" : event.getToolTipText( );
                    String text = String.format( "%s\n%s", label, tip );
                    tooltipPainter.setText( text );
                    tooltipPainter.setIcon( event.getIconId( ) );
                }
            }
        };

        // by default don't grow event plots
        this.setGrow( false );

        this.rowSize = DEFAULT_ROW_SIZE;
        this.eventPadding = DEFAULT_BUFFER_SIZE;
        this.updateSize( );
    }

    protected class EventListener implements GlimpseMouseAllListener
    {
        Set<EventSelection> hoveredEvents = Collections.emptySet( );

        @Override
        public void mouseEntered( GlimpseMouseEvent e )
        {
        }

        @Override
        public void mouseExited( GlimpseMouseEvent e )
        {
            // exit all currently hovered events
            TimeStamp time = getTime( e );
            for ( EventPlotListener listener : eventListeners )
            {
                listener.eventsExited( e, hoveredEvents, time );
            }

            hoveredEvents = Collections.emptySet( );
        }

        @Override
        public void mousePressed( GlimpseMouseEvent e )
        {
            TimeStamp time = getTime( e );
            Set<EventSelection> tempEvents = Collections.unmodifiableSet( Sets.newHashSet( eventManager.getNearestEvents( e ) ) );

            for ( EventPlotListener listener : eventListeners )
            {
                listener.eventsClicked( e, tempEvents, time );
            }

            EventSelection eventSelection = eventManager.getNearestEvent( tempEvents, e );
            if ( eventSelection != null && selectionHandler.isAllowMouseEventSelection( ) )
            {
                Event event = eventSelection.getEvent( );

                if ( event.isSelectable( ) )
                {
                    if ( e.isKeyDown( ModifierKey.Ctrl ) && selectionHandler.isAllowMultipleEventSelection( ) )
                    {
                        if ( selectionHandler.isEventSelected( event ) )
                        {
                            selectionHandler.removeSelectedEvent( event );
                        }
                        else
                        {
                            selectionHandler.addSelectedEvent( event );
                        }
                    }
                    else
                    {
                        // special case: clicking a single selected event deselects it
                        if ( isDeselectSingleEvent( event ) )
                        {
                            selectionHandler.setSelectedEvents( Collections.<Event> emptySet( ) );
                        }
                        else
                        {
                            selectionHandler.setSelectedEvents( Collections.singleton( event ) );
                        }
                    }
                }
            }
            else if ( selectionHandler.isClearSelectionOnClick( ) )
            {
                selectionHandler.clearSelectedEvents( );
            }
        }

        @Override
        public void mouseReleased( GlimpseMouseEvent e )
        {
        }

        @Override
        public void mouseWheelMoved( GlimpseMouseEvent e )
        {
        }

        @Override
        public void mouseMoved( GlimpseMouseEvent e )
        {
            TimeStamp time = getTime( e );
            Set<EventSelection> newHoveredEvents = Collections.unmodifiableSet( Sets.newHashSet( eventManager.getNearestEvents( e ) ) );

            SetView<EventSelection> eventsExited = Sets.difference( hoveredEvents, newHoveredEvents );
            if ( eventsExited.size( ) > 0 )
            {
                for ( EventPlotListener listener : eventListeners )
                {
                    listener.eventsExited( e, eventsExited, time );
                }
            }

            SetView<EventSelection> eventsEntered = Sets.difference( newHoveredEvents, hoveredEvents );
            if ( eventsEntered.size( ) > 0 )
            {
                for ( EventPlotListener listener : eventListeners )
                {
                    listener.eventsEntered( e, eventsEntered, time );
                }
            }

            if ( newHoveredEvents.size( ) > 0 )
            {
                for ( EventPlotListener listener : eventListeners )
                {
                    listener.eventsHovered( e, newHoveredEvents, time );
                }
            }

            hoveredEvents = newHoveredEvents;
        }
    }

    protected boolean isDeselectSingleEvent( Event event )
    {
        if ( selectionHandler.getSelectedEvents( ).size( ) == 1 )
        {
            Event currentSelection = selectionHandler.getSelectedEvents( ).iterator( ).next( );
            return currentSelection.equals( event );
        }
        else
        {
            return false;
        }
    }

    protected class TooltipListener implements EventPlotListener
    {
        EventSelection selection = null;

        protected void selectEvent( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
        {
            EventSelection bestSelection = null;
            double bestDiff = Double.POSITIVE_INFINITY;
            for ( EventSelection eventSelection : events )
            {
                Event event = eventSelection.getEvent( );

                double diff = Math.abs( event.getEndTime( ).durationAfter( time ) );
                if ( diff < bestDiff )
                {
                    bestSelection = eventSelection;
                    bestDiff = diff;
                }

                diff = Math.abs( event.getStartTime( ).durationAfter( time ) );
                if ( diff < bestDiff )
                {
                    bestSelection = eventSelection;
                    bestDiff = diff;
                }
            }

            if ( bestSelection == null || bestSelection.equals( selection ) ) return;

            selection = bestSelection;

            StackedTimePlot2D plot = getStackedTimePlot( );
            TooltipPainter tooltipPainter = plot.getTooltipPainter( );

            if ( eventToolTipHandler != null )
            {
                eventToolTipHandler.setToolTip( selection, tooltipPainter );
            }
        }

        @Override
        public void eventsExited( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
        {
            if ( eventToolTipHandler != null && events.contains( selection ) )
            {
                StackedTimePlot2D plot = getStackedTimePlot( );
                TooltipPainter tooltipPainter = plot.getTooltipPainter( );
                tooltipPainter.clear( );

                selection = null;
            }
        }

        @Override
        public void eventsEntered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
        {
            selectEvent( e, events, time );
        }

        @Override
        public void eventsHovered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
        {
            selectEvent( e, events, time );
        }

        @Override
        public void eventsClicked( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
        {
        }

        @Override
        public void eventUpdated( GlimpseMouseEvent e, Event event )
        {
        }
    }

    public void setDefaultIconSize( int size )
    {
        this.defaultIconSize = size;
        this.setUseDefaultIconSize( true );
    }

    public int getDefaultIconSize( )
    {
        return this.defaultIconSize;
    }

    public boolean isUseDefaultIconSize( )
    {
        return this.useDefaultIconSize;
    }

    public void setUseDefaultIconSize( boolean useDefaultIconSize )
    {
        this.useDefaultIconSize = useDefaultIconSize;
    }

    public void setAggregateNearbyEvents( boolean aggregate )
    {
        this.eventManager.setAggregateNearbyEvents( aggregate );
    }

    public boolean isAggregateNearbyEvents( )
    {
        return this.eventManager.isAggregateNearbyEvents( );
    }

    public void setEventPainter( GroupedEventPainter painter )
    {
        this.eventPainterManager.setEventPainter( painter );
    }

    public GroupedEventPainter getEventPainter( )
    {
        return this.eventPainterManager.getEventPainter( );
    }

    public void setSelectionHandler( EventSelectionHandler selectionHandler )
    {
        this.selectionHandler = selectionHandler;
    }

    public EventSelectionHandler getEventSelectionHandler( )
    {
        return this.selectionHandler;
    }

    public void setEventToolTipHandler( EventToolTipHandler toolTipHandler )
    {
        this.eventToolTipHandler = toolTipHandler;
    }

    public TimeStamp getTime( GlimpseMouseEvent e )
    {
        return getStackedTimePlot( ).getTime( e );
    }

    public boolean isMouseDragEnabled( )
    {
        return this.dragListener.isEnabled( );
    }

    public void setMouseDragEnabled( boolean enabled )
    {
        this.dragListener.setEnabled( enabled );
    }

    public void addEventPlotListener( EventPlotListener listener )
    {
        this.eventListeners.add( listener );
    }

    public void removeEventPlotListener( EventPlotListener listener )
    {
        this.eventListeners.remove( listener );
    }

    public boolean isStackOverlappingEvents( )
    {
        return eventManager.isStackOverlappingEvents( );
    }

    public void setStackOverlappingEvents( boolean stack )
    {
        eventManager.setStackOverlappingEvents( stack );
    }

    public int getRow( Object eventId )
    {
        return this.eventManager.getRow( eventId );
    }

    /**
     * Sets the size of a single row of events. An EventPlotInfo may contain
     * multiple rows of events if some of those events overlap in time.
     */
    public void setRowSize( int size )
    {
        this.rowSize = size;
        this.updateSize( );
    }

    public int getRowSize( )
    {
        return this.rowSize;
    }

    public double getRowSize( GlimpseBounds bounds )
    {
        if ( isGrow( ) )
        {
            int rowCount = getRowCount( );
            Orientation orient = getStackedPlot( ).getOrientation( );
            int size = orient == Orientation.HORIZONTAL ? bounds.getWidth( ) : bounds.getHeight( );
            int sizeMinusBuffer = size - ( rowCount + 1 ) * getEventPadding( );

            return ( double ) sizeMinusBuffer / ( double ) rowCount;
        }
        else
        {
            return getRowSize( );
        }
    }

    @Override
    public void setGrow( boolean grow )
    {
        super.setGrow( grow );
        this.updateSize( );
    }

    @Override
    public void setSize( int size )
    {
        this.rowSize = size;
        this.updateSize( );
    }

    public void setEventPadding( int size )
    {
        this.eventPadding = size;
        this.updateSize( );
    }

    public int getEventPadding( )
    {
        return this.eventPadding;
    }

    public void setRowMaxCount( int count )
    {
        this.maxRowCount = count;
        this.eventManager.validate( );
    }

    public int getRowMaxCount( )
    {
        return this.maxRowCount;
    }

    public void setRowMinCount( int count )
    {
        this.minRowCount = count;
        this.updateSize( );
    }

    public int getRowMinCount( )
    {
        return this.minRowCount;
    }

    public int getRowCount( )
    {
        return Math.max( minRowCount, this.eventManager.getRowCount( ) );
    }

    public void updateSize( )
    {
        if ( !isGrow( ) )
        {
            int rowCount = getRowCount( );
            super.setSize( rowCount * this.rowSize + ( rowCount + 1 ) * this.eventPadding );
        }
    }

    public boolean isTextColorSet( )
    {
        return this.eventPainterManager.isTextColorSet( );
    }

    public boolean isBackgroundColorSet( )
    {
        return this.eventPainterManager.isBackgroundColorSet( );
    }

    public boolean isBorderColorSet( )
    {
        return this.eventPainterManager.isBorderColorSet( );
    }

    public TextRenderer getTextRenderer( )
    {
        return this.eventPainterManager.getTextRenderer( );
    }

    public Object getDefaultIconId( )
    {
        return this.defaultIconId;
    }

    public TextureAtlas getTextureAtlas( )
    {
        return this.eventPainterManager.getTextureAtlas( );
    }

    public void setDefaultEventBorderThickness( float thickness )
    {
        this.eventPainterManager.setBorderThickness( thickness );
    }

    public float getDefaultEventBorderThickness( )
    {
        return this.eventPainterManager.getBorderThickness( );
    }

    public void setDefaultEventBackgroundColor( float[] backgroundColor )
    {
        this.eventPainterManager.setBackgroundColor( backgroundColor );
    }

    public float[] getDefaultEventBackgroundColor( )
    {
        return this.eventPainterManager.getBackgroundColor( );
    }

    @Override
    public void setDefaultEventBorderColor( float[] borderColor )
    {
        this.eventPainterManager.setBorderColor( borderColor );
    }

    public float[] getDefaultEventBorderColor( )
    {
        return this.eventPainterManager.getBorderColor( );
    }

    public void setDefaultEventTextColor( float[] textColor )
    {
        this.eventPainterManager.setTextColor( textColor );
    }

    public float[] getTextColorNoBackground( )
    {
        return this.eventPainterManager.getTextColorNoBackground( );
    }

    public float[] getTextColor( )
    {
        return this.eventPainterManager.getTextColor( );
    }

    public void setFont( Font font, boolean antialias )
    {
        this.eventPainterManager.setFont( font, antialias );
    }

    public Set<Event> getEvents( )
    {
        return this.eventManager.getEvents( );
    }

    public Event getEvent( Object id )
    {
        return this.eventManager.getEvent( id );
    }

    public EventBounds getEventBounds( Object id )
    {
        return this.eventManager.getOrCreateEventBounds( id );
    }

    public EventManager getEventManager( )
    {
        return this.eventManager;
    }

    public Event addEvent( String label, TimeStamp time )
    {
        return addEvent( UUID.randomUUID( ), label, time );
    }

    public Event addEvent( String label, TimeStamp startTime, TimeStamp endTime )
    {
        return addEvent( UUID.randomUUID( ), label, startTime, endTime );
    }

    public Event addEvent( Object id, String label, TimeStamp time )
    {
        Event event = new Event( id, label, time );
        addEvent( event );
        return event;
    }

    public Event addEvent( Object id, String label, TimeStamp startTime, TimeStamp endTime )
    {
        Event event = new Event( id, label, startTime, endTime );
        addEvent( event );
        return event;
    }

    public void addEvent( Event event )
    {
        event.setEventPlotInfo( this );
        event.setTextRenderingMode( textRenderingMode );

        this.eventManager.addEvent( event );
    }

    public void removeEvent( Event event )
    {
        event.setEventPlotInfo( null );
        this.eventManager.removeEvent( event.getId( ) );
    }

    public void removeEvent( Object id )
    {
        Event event = this.eventManager.removeEvent( id );
        if ( event != null )
        {
            event.setEventPlotInfo( null );
        }
    }

    public void removeAllEvents( )
    {
        this.eventManager.removeAllEvents( );
    }

    /**
     * Sets the TextRenderingMode for all {@link Event} children of this EventPlotInfo.
     * This value will also be the default of any newly created Events. The value can be
     * overridden on a per-Event basis using {@link Event#setTextRenderingMode(OverlapRenderingMode)}.
     * However, the next call to {@link EventPlotInfo#setTextRenderingMode(OverlapRenderingMode)} will
     * reset all Events to the same value.
     *
     * @see Event#setOverlapMode(OverlapRenderingMode)
     */
    public void setTextRenderingMode( TextRenderingMode mode )
    {
        this.textRenderingMode = mode;
    }

    /**
     * @see #setTextRenderingMode(TextRenderingMode)
     */
    public TextRenderingMode getTextRenderingMode( )
    {
        return this.textRenderingMode;
    }

    public void updateEventRow( GlimpseMouseEvent mouseEvent, Event event, int rowIndex )
    {
        this.eventManager.setRow( event.getId( ), rowIndex );

        this.notifyEventUpdated( mouseEvent, event );
    }

    public void updateEvent( Event oldEvent, TimeStamp newStartTime, TimeStamp newEndTime )
    {
        this.updateEvent0( null, oldEvent, newStartTime, newEndTime );
    }

    protected void updateEvent0( GlimpseMouseEvent mouseEvent, Event oldEvent, TimeStamp newStartTime, TimeStamp newEndTime )
    {
        this.eventManager.moveEvent( oldEvent, newStartTime, newEndTime );

        this.notifyEventUpdated( mouseEvent, oldEvent );
    }

    public void validate( )
    {
        this.eventManager.validate( );
    }

    public GlimpseAxisLayout1D getEventLayout( )
    {
        return layout1D;
    }

    protected void notifyEventUpdated( GlimpseMouseEvent mouseEvent, Event event )
    {
        for ( EventPlotListener listener : eventListeners )
        {
            listener.eventUpdated( mouseEvent, event );
        }
    }
}
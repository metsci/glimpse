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
package com.metsci.glimpse.plot.timeline.event;

import static com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode.Ellipsis;

import java.awt.Font;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.metsci.glimpse.event.mouse.GlimpseMouseAllListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.painter.info.TooltipPainter;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.event.Event.OverlapRenderingMode;
import com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfoWrapper;
import com.metsci.glimpse.support.atlas.TextureAtlas;
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
    public static final int DEFAULT_ROW_SIZE = 26;
    public static final int DEFAULT_BUFFER_SIZE = 2;

    protected EventPainter eventPainter;
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

    protected Set<Event> selectedEvents;
    protected float[] selectedBorderColor;
    protected float[] selectedBackgroundColor;
    protected float selectedBorderThickness = 1.8f;
    protected boolean highlightSelectedEvents = false;

    public EventPlotInfo( TimePlotInfo delegate )
    {
        this( delegate, new TextureAtlas( ) );
    }

    public EventPlotInfo( TimePlotInfo delegate, TextureAtlas atlas )
    {
        super( delegate );

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
        this.eventPainter = new EventPainter( this, epoch, atlas, isHorizontal );
        this.layout1D.addPainter( this.eventPainter );

        this.eventListeners = new CopyOnWriteArrayList<EventPlotListener>( );

        this.selectedEvents = new LinkedHashSet<Event>( );

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
                String label = event.getLabel( ) == null ? "" : event.getLabel( );
                String tip = event.getToolTipText( ) == null ? "" : event.getToolTipText( );
                String text = String.format( "%s\n%s", label, tip );
                tooltipPainter.setText( text );
                tooltipPainter.setIcon( event.getIconId( ) );
            }
        };

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
            if ( isHorizontal )
            {
                TimeStamp time = getTime( e );
                Set<EventSelection> tempEvents = Collections.unmodifiableSet( Sets.newHashSet( eventPainter.getNearestEvents( e ) ) );

                for ( EventPlotListener listener : eventListeners )
                {
                    listener.eventsClicked( e, tempEvents, time );
                }

                EventSelection eventSelection = eventPainter.getNearestEvent( tempEvents, e );
                if ( eventSelection != null )
                {
                    Event event = eventSelection.getEvent( );

                    if ( e.isKeyDown( ModifierKey.Ctrl ) )
                    {
                        if ( selectedEvents.contains( event ) )
                        {
                            removeSelectedEvent( event );
                        }
                        else
                        {
                            addSelectedEvent( event );
                        }
                    }
                    else
                    {
                        setSelectedEvents( Collections.singleton( event ) );
                    }
                }
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
            if ( isHorizontal )
            {
                TimeStamp time = getTime( e );
                Set<EventSelection> newHoveredEvents = Collections.unmodifiableSet( Sets.newHashSet( eventPainter.getNearestEvents( e ) ) );

                SetView<EventSelection> eventsExited = Sets.difference( hoveredEvents, newHoveredEvents );
                for ( EventPlotListener listener : eventListeners )
                {
                    listener.eventsExited( e, eventsExited, time );
                }

                SetView<EventSelection> eventsEntered = Sets.difference( newHoveredEvents, hoveredEvents );
                for ( EventPlotListener listener : eventListeners )
                {
                    listener.eventsEntered( e, eventsEntered, time );
                }

                for ( EventPlotListener listener : eventListeners )
                {
                    listener.eventsHovered( e, newHoveredEvents, time );
                }

                hoveredEvents = Sets.newHashSet( newHoveredEvents );
            }
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

            if ( eventToolTipHandler != null ) eventToolTipHandler.setToolTip( selection, tooltipPainter );
        }

        @Override
        public void eventsExited( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
        {
            if ( eventToolTipHandler != null && events.contains( selection ) )
            {
                StackedTimePlot2D plot = getStackedTimePlot( );
                TooltipPainter tooltipPainter = plot.getTooltipPainter( );
                tooltipPainter.setText( null );

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
        public void eventUpdated( Event event )
        {
        }

        @Override
        public void eventsSelected( Set<Event> selectedEvents, Set<Event> deselectedEvents )
        {
        }
    }

    public void setEventToolTipHandler( EventToolTipHandler toolTipHandler )
    {
        this.eventToolTipHandler = toolTipHandler;
    }

    public Set<Event> getSelectedEvents( )
    {
        return Collections.unmodifiableSet( Sets.newHashSet( selectedEvents ) );
    }

    public void setSelectedEvents( Set<Event> events )
    {
        // set of deselected events
        Set<Event> deselectedEvents = Sets.difference( selectedEvents, events ).immutableCopy( );
        // set of newly selected events
        Set<Event> newSelectedEvents = Sets.difference( events, selectedEvents ).immutableCopy( );
        
        selectedEvents.clear( );
        selectedEvents.addAll( events );
        
        notifyEventsSelected( newSelectedEvents, deselectedEvents );
    }

    public void clearSelectedEvents( )
    {
        Set<Event> tempDeselectedEvents = Collections.unmodifiableSet( Sets.newHashSet( selectedEvents ) );

        selectedEvents.clear( );
        
        notifyEventsSelected( Collections.<Event>emptySet( ), tempDeselectedEvents );
    }

    public void addSelectedEvent( Event event )
    {
        selectedEvents.add( event );
        
        notifyEventsSelected( Collections.singleton( event ), Collections.<Event>emptySet( ) );
    }

    public void removeSelectedEvent( Event event )
    {
        selectedEvents.remove( event );
        
        notifyEventsSelected( Collections.<Event>emptySet( ), Collections.singleton( event ) );
    }

    public boolean isEventSelected( Event event )
    {
        return selectedEvents.contains( event );
    }

    public float[] getSelectedEventBorderColor( )
    {
        return selectedBorderColor;
    }

    public float[] getSelectedEventBackgroundColor( )
    {
        return selectedBackgroundColor;
    }

    public float getSelectedEventBorderThickness( )
    {
        return selectedBorderThickness;
    }

    public boolean isHighlightSelectedEvents( )
    {
        return highlightSelectedEvents;
    }

    public void setSelectedEventBorderColor( float[] color )
    {
        selectedBorderColor = color;
        highlightSelectedEvents = true;
    }

    public void setSelectedEventBackgroundColor( float[] color )
    {
        selectedBackgroundColor = color;
        highlightSelectedEvents = true;
    }

    public void setSelectedEventBorderThickness( float thickness )
    {
        selectedBorderThickness = thickness;
        highlightSelectedEvents = true;
    }

    public void setHighlightSelectedEvents( boolean highlight )
    {
        highlightSelectedEvents = highlight;
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
        return eventPainter.isStackOverlappingEvents( );
    }

    public void setStackOverlappingEvents( boolean stack )
    {
        eventPainter.setStackOverlappingEvents( stack );
    }

    public int getRow( Object eventId )
    {
        return this.eventPainter.getRow( eventId );
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
        this.eventPainter.validate( );
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
        return Math.max( minRowCount, this.eventPainter.getRowCount( ) );
    }

    public void updateSize( )
    {
        int rowCount = getRowCount( );

        this.setSize( rowCount * this.rowSize + ( rowCount + 1 ) * this.eventPadding );
    }

    public TextureAtlas getTextureAtlas( )
    {
        return this.eventPainter.getTextureAtlas( );
    }

    public void setBackgroundColor( float[] backgroundColor )
    {
        this.eventPainter.setBackgroundColor( backgroundColor );
    }

    public void setBorderColor( float[] borderColor )
    {
        this.eventPainter.setBorderColor( borderColor );
    }

    public void setTextColor( float[] textColor )
    {
        this.eventPainter.setTextColor( textColor );
    }

    public void setFont( Font font, boolean antialias )
    {
        this.eventPainter.setFont( font, antialias );
    }

    public Set<Event> getEvents( )
    {
        return this.eventPainter.getEvents( );
    }

    public Event getEvent( Object id )
    {
        return this.eventPainter.getEvent( id );
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

        this.eventPainter.addEvent( event );
    }

    public void removeEvent( Event event )
    {
        event.setEventPlotInfo( null );
        this.eventPainter.removeEvent( event.getId( ) );
    }

    public void removeEvent( Object id )
    {
        Event event = this.eventPainter.removeEvent( id );
        if ( event != null )
        {
            event.setEventPlotInfo( null );
        }
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

    public void updateEventRow( Event event, int rowIndex )
    {
        this.eventPainter.setRow( event.getId( ), rowIndex );

        this.notifyEventUpdated( event );
    }

    public void updateEvent( Event oldEvent, TimeStamp newStartTime, TimeStamp newEndTime )
    {
        this.eventPainter.moveEvent( oldEvent, newStartTime, newEndTime );

        this.notifyEventUpdated( oldEvent );
    }

    protected void notifyEventUpdated( Event event )
    {
        for ( EventPlotListener listener : eventListeners )
        {
            listener.eventUpdated( event );
        }
    }

    protected void notifyEventsSelected( Set<Event> selectedEvents, Set<Event> deselectedEvents )
    {
        for ( EventPlotListener listener : eventListeners )
        {
            listener.eventsSelected( selectedEvents, deselectedEvents );
        }
    }
}
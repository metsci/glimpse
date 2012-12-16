package com.metsci.glimpse.plot.timeline.event;

import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Center;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.End;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Start;

import java.awt.Font;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseAllListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.painter.info.TooltipPainter;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.data.EventSelection.Location;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class EventPlotInfo extends TimePlotInfo
{
    public static final int DEFAULT_ROW_SIZE = 26;
    public static final int DEFAULT_BUFFER_SIZE = 2;

    protected EventPainter eventPainter;
    protected GlimpseAxisLayout1D layout1D;

    protected int rowSize;
    protected int bufferSize;

    protected List<EventPlotListener> eventListeners;

    protected boolean isHorizontal;

    protected DragListener dragListener;
    protected TooltipListener tooltipListener;

    public EventPlotInfo( TimePlotInfo delegate )
    {
        this( delegate, new TextureAtlas( ) );
    }

    public EventPlotInfo( TimePlotInfo delegate, TextureAtlas atlas )
    {
        super( delegate );

        final Epoch epoch = parent.getEpoch( );
        this.isHorizontal = parent.isTimeAxisHorizontal( );

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

        this.layout1D.addGlimpseMouseAllListener( new EventListener( ) );

        this.dragListener = new DragListener( );
        this.addEventPlotListener( dragListener );
        this.layout1D.addGlimpseMouseAllListener( dragListener );

        this.tooltipListener = new TooltipListener( );
        this.addEventPlotListener( tooltipListener );
        
        this.rowSize = DEFAULT_ROW_SIZE;
        this.bufferSize = DEFAULT_BUFFER_SIZE;
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
                Set<EventSelection> events = Collections.unmodifiableSet( eventPainter.getNearestEvents( e ) );

                for ( EventPlotListener listener : eventListeners )
                {
                    listener.eventsClicked( e, events, time );
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
                Set<EventSelection> newHoveredEvents = Collections.unmodifiableSet( eventPainter.getNearestEvents( e ) );

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
            if ( events.isEmpty( ) || selection != null ) return;
            
            selection = events.iterator( ).next( );
            
            StackedTimePlot2D plot = getStackedTimePlot( );
            TooltipPainter tooltipPainter = plot.getTooltipPainter( );
            
            Event event = selection.getEvent( );
            
            tooltipPainter.setText( event.getLabel( ) );
            tooltipPainter.setIcon( event.getIconId( ) );
        }
        
        @Override
        public void eventsExited( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
        {
            if ( events.contains( selection ) )
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
        
    }

    protected class DragListener implements EventPlotListener, GlimpseMouseAllListener
    {
        Location dragType = null;
        TimeStamp anchorTime = null;
        TimeStamp eventStart = null;
        TimeStamp eventEnd = null;
        Event dragEvent = null;

        boolean enabled = true;

        public boolean isEnabled( )
        {
            return this.enabled;
        }

        public void setEnabled( boolean enabled )
        {
            this.enabled = enabled;

            reset( );
        }

        public void reset( )
        {
            dragType = null;
            anchorTime = null;
            eventStart = null;
            eventEnd = null;
            dragEvent = null;
        }

        @Override
        public void mouseMoved( GlimpseMouseEvent e )
        {
            if ( !enabled ) return;

            if ( e.isButtonDown( MouseButton.Button1 ) && dragEvent != null )
            {
                TimeStamp time = getTime( e );

                if ( dragType == Location.Center )
                {
                    double diff = time.durationAfter( anchorTime );
                    dragEvent.setTimes( eventStart.add( diff ), eventEnd.add( diff ) );
                }
                else if ( dragType == Location.End && eventStart.isBefore( time ) )
                {
                    dragEvent.setTimes( eventStart, time );
                }
                else if ( dragType == Location.Start && eventEnd.isAfter( time ) )
                {
                    dragEvent.setTimes( time, eventEnd );
                }

                e.setHandled( true );
            }
        }

        @Override
        public void eventsClicked( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time )
        {
            if ( !enabled ) return;

            if ( e.isButtonDown( MouseButton.Button1 ) )
            {
                for ( EventSelection selection : events )
                {
                    if ( selection.isLocation( Center, Start, End ) )
                    {
                        dragEvent = selection.getEvent( );
                        eventStart = dragEvent.getStartTime( );
                        eventEnd = dragEvent.getEndTime( );
                        anchorTime = time;
                        e.setHandled( true );

                        if ( selection.isCenterSelection( ) )
                        {
                            dragType = Center;
                        }
                        else if ( selection.isStartTimeSelection( ) )
                        {
                            dragType = Start;
                        }
                        else if ( selection.isEndTimeSelection( ) )
                        {
                            dragType = End;
                        }

                        return;
                    }
                }
            }
        }

        @Override
        public void mouseReleased( GlimpseMouseEvent event )
        {
            if ( !enabled ) return;

            reset( );
        }

        //@formatter:off
        @Override public void eventsHovered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time ) { }
        @Override public void eventsExited( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time ) { }
        @Override public void eventsEntered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time ) { }
        @Override public void eventUpdated( Event event ) { }
        @Override public void mouseEntered( GlimpseMouseEvent event ) { }
        @Override public void mouseExited( GlimpseMouseEvent event ) { }
        @Override public void mousePressed( GlimpseMouseEvent event ) { }
        @Override public void mouseWheelMoved( GlimpseMouseEvent e ) { }
        //@formatter:on
    }

    public interface EventPlotListener
    {
        public void eventsExited( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

        public void eventsEntered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

        public void eventsHovered( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

        public void eventsClicked( GlimpseMouseEvent e, Set<EventSelection> events, TimeStamp time );

        public void eventUpdated( Event event );
    }

    public TimeStamp getTime( GlimpseMouseEvent e )
    {
        Axis1D axis = e.getAxis1D( );
        double valueX = axis.screenPixelToValue( e.getX( ) );
        return parent.getEpoch( ).toTimeStamp( valueX );
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

    /**
     * Sets the size of a single row of events. An EventPlotInfo may contain
     * multiple rows of events if some of those events overlap in time.
     */
    public void setRowSize( int size )
    {
        this.rowSize = size;
        this.updateSize( );
    }

    public void setBufferSize( int size )
    {
        this.bufferSize = size;
        this.updateSize( );
    }

    public int getBufferSize( )
    {
        return this.bufferSize;
    }

    public int getRowSize( )
    {
        return this.rowSize;
    }

    public void updateSize( )
    {
        int rowCount = this.eventPainter.getRowCount( );

        this.setSize( rowCount * this.rowSize + ( rowCount + 1 ) * this.bufferSize );
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

    public Event addEvent( String name, TimeStamp time )
    {
        return addEvent( UUID.randomUUID( ), name, time );
    }

    public Event addEvent( String name, TimeStamp startTime, TimeStamp endTime )
    {
        return addEvent( UUID.randomUUID( ), name, startTime, endTime );
    }

    public Event addEvent( Object id, String name, TimeStamp time )
    {
        Event event = new Event( id, name, time );
        addEvent( event );
        return event;
    }

    public Event addEvent( Object id, String name, TimeStamp startTime, TimeStamp endTime )
    {
        Event event = new Event( id, name, startTime, endTime );
        addEvent( event );
        return event;
    }

    public void addEvent( Event event )
    {
        event.setEventPlotInfo( this );
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

    void updateEvent( Event oldEvent, TimeStamp newStartTime, TimeStamp newEndTime )
    {
        this.eventPainter.moveEvent0( oldEvent, newStartTime, newEndTime );

        for ( EventPlotListener listener : eventListeners )
        {
            listener.eventUpdated( oldEvent );
        }
    }
}
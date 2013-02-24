package com.metsci.glimpse.plot.timeline.event;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Sets;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;

public class EventSelectionHandler
{
    protected List<EventSelectionListener> eventListeners;
    
    protected Set<Event> selectedEvents;
    protected float[] selectedBorderColor;
    protected float[] selectedBackgroundColor;
    protected float selectedBorderThickness = 1.8f;
    protected boolean highlightSelectedEvents = false;
    protected boolean clearSelectionOnClick = false;
    protected boolean allowMouseEventSelection = true;
    
    public EventSelectionHandler( )
    {
        this.eventListeners = new CopyOnWriteArrayList<EventSelectionListener>( );
        this.selectedEvents = new LinkedHashSet<Event>( );
    }
    

    public void addEventSelectionListener( EventSelectionListener listener )
    {
        this.eventListeners.add( listener );
    }

    public void removeEventSelectionListener( EventSelectionListener listener )
    {
        this.eventListeners.remove( listener );
    }
    
    /**
     * If true, the set of selected events is cleared when the user clicks
     * on an area of the timeline which contains no events. Otherwise, such
     * a click has no effect on the selected events. In this case, the only
     * way to deselect all events is to ctrl-click on all the selected events.
     * 
     * @param clear
     */
    public void setClearSelectionOnClick( boolean clear )
    {
        this.clearSelectionOnClick = clear;
    }
    
    /**
     * @see EventPlotInfo#setClearSelectionOnClick(boolean)
     */
    public boolean isClearSelectionOnClick( )
    {
        return this.clearSelectionOnClick;
    }
    
    /**
     * <p>If true, Events are automatically selected when users click inside or near
     * them with the mouse (unless they are set as unselectable via
     * {@link Event#setSelectable(boolean)}. Clicking one event removes all other
     * selected events unless the ctrl key is held down, in which case the clicked
     * event is added or removed from the set of selected Events.</p>
     * 
     * <p>If custom selection semmantics are required (only selecting events
     * when their icon is clicked, for example), then
     * {@link #setAllowMouseEventSelection(boolean)} can be set to false and client
     * code can attach a custom {@link EventPlotListener} or {@link GlimpseMouseListener}
     * which calls {@link #setSelectedEvents(Set)} as desired based on user clicks.</p>
     * 
     * @param allowSelection
     */
    public void setAllowMouseEventSelection( boolean allowSelection )
    {
        this.allowMouseEventSelection = allowSelection;
    }
    
    public boolean isAllowMouseEventSelection( )
    {
        return this.allowMouseEventSelection;
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
    

    protected void notifyEventsSelected( Set<Event> selectedEvents, Set<Event> deselectedEvents )
    {
        for ( EventSelectionListener listener : eventListeners )
        {
            listener.eventsSelected( selectedEvents, deselectedEvents );
        }
    }
}

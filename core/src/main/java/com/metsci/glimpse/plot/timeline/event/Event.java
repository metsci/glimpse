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

import static com.metsci.glimpse.plot.timeline.event.Event.OverlapRenderingMode.*;
import static com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.plot.timeline.data.EventConstraint;
import com.metsci.glimpse.plot.timeline.data.TimeSpan;
import com.metsci.glimpse.plot.timeline.event.listener.EventPlotListener;
import com.metsci.glimpse.plot.timeline.event.paint.EventPainter;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * Event represents an occurrence with a start and end time and is usually created
 * by an {@link com.metsci.glimpse.plot.timeline.event.EventPlotInfo} (which represents
 * a row or column of a {@link com.metsci.glimpse.plot.timeline.StackedTimePlot2D}.
 *
 * In addition to time bounds, Events can have text labels, icons, and tool tips associated
 * with them. EventPlotInfo allows registering of listeners which report when the mouse
 * interacts with an Event. Events can also be adjusted by the user by click and
 * dragging on their bounds.
 *
 * @author ulman
 */
public class Event implements Iterable<Event>
{
    protected EventPlotInfo info;

    protected Object id;
    protected String label;
    protected Object iconId; // references id in associated TextureAtlas
    protected String toolTipText;

    protected float[] backgroundColor;
    protected float[] borderColor;
    protected float[] textColor;
    protected float borderThickness = 1.8f;

    protected TimeStamp startTime;
    protected TimeStamp endTime;

    protected boolean fixedRow = false;
    protected int fixedRowIndex = 0;

    protected boolean showLabel = true;
    protected boolean showIcon = true;
    protected boolean showBorder = true;
    protected boolean showBackground = true;

    protected TextRenderingMode textRenderingMode = Ellipsis;
    protected OverlapRenderingMode overlapRenderingMode = Overfull;

    protected boolean isSelectable = true;
    protected boolean isEditable = true;
    protected boolean isEndTimeMoveable = true;
    protected boolean isStartTimeMoveable = true;
    protected boolean isResizeable = true;
    protected double maxTimeSpan = Double.MAX_VALUE;
    protected double minTimeSpan = 0;

    protected int iconSize = 0;
    protected boolean useDefaultSize = true;

    protected List<EventConstraint> constraints;

    protected EventPainter painter;

    /**
     * Indicates how text which is too large to fit in the Event box should be shortened.
     *
     * @author ulman
     */
    public enum TextRenderingMode
    {
        /**
         * Don't shorten the text at all. It will simply spill over the event box into adjacent event boxes.
         */
        ShowAll,
        /**
         * If any of the text does not fit, hide all the text.
         */
        HideAll,
        /**
         * Shorten the text to fit in the box and display ellipsis to indicate that the text has been shortened.
         */
        Ellipsis;
    }

    /**
     * Indicates what types of overlaps should be considered when determining whether to shorten Event box text.
     *
     * @author ulman
     */
    public enum OverlapRenderingMode
    {
        /**
         * Don't try to detect overlaps. When this mode is set, text will never be shortened regardless of the {@link ShortenMode}.
         */
        None,
        /**
         * Only shorten text when it overflows the box for this Event. Don't try to detect overlaps with other Events.
         */
        Overfull,
        /**
         * Shorten the text when it overflows the box for this Event or overlaps with another Event.
         */
        Intersecting;
    }

    protected EventConstraint builtInConstraints = new EventConstraint( )
    {
        @Override
        public TimeSpan applyConstraint( Event event, TimeSpan proposedTimeSpan )
        {
            // if the timeline is not editable, don't allow the change
            // if the timeline is not resizable and one or both of the endpoints are fixed,
            //    then it is effectively not editable, don't allow the change
            if ( !isEditable || ( !isResizeable && ( !isEndTimeMoveable || !isStartTimeMoveable ) ) )
            {
                return event.getTimeSpan( );
            }

            TimeStamp oldStart = event.getStartTime( );
            TimeStamp oldEnd = event.getEndTime( );

            TimeStamp newStart = proposedTimeSpan.getStartTime( );
            TimeStamp newEnd = proposedTimeSpan.getEndTime( );

            if ( !isEndTimeMoveable ) newEnd = oldEnd;
            if ( !isStartTimeMoveable ) newStart = oldStart;

            double newDiff = newEnd.durationAfter( newStart );
            double oldDiff = oldEnd.durationAfter( oldStart );

            if ( !isResizeable && newDiff != oldDiff )
            {
                newEnd = oldEnd;
                newStart = oldStart;
            }

            if ( newDiff < minTimeSpan )
            {
                if ( oldEnd.equals( newEnd ) )
                {
                    newStart = newEnd.subtract( minTimeSpan );
                }
                else
                {
                    newEnd = newStart.add( minTimeSpan );
                }
            }

            if ( newDiff > maxTimeSpan )
            {
                if ( oldEnd.equals( newEnd ) )
                {
                    newStart = newEnd.subtract( maxTimeSpan );
                }
                else
                {
                    newEnd = newStart.add( maxTimeSpan );
                }
            }

            return new TimeSpan( newStart, newEnd );
        }
    };

    private Event( TimeStamp time )
    {
        this( ( Object ) null, ( String ) null, time );
    }

    protected Event( Object id, String name, TimeStamp time )
    {
        this.id = id;
        this.label = name;

        this.startTime = time;
        this.endTime = time;

        this.constraints = new LinkedList<EventConstraint>( );
        this.constraints.add( builtInConstraints );
    }

    protected Event( Object id, String name, TimeStamp startTime, TimeStamp endTime )
    {
        this.id = id;
        this.label = name;

        this.startTime = startTime;
        this.endTime = endTime;

        this.constraints = new LinkedList<EventConstraint>( );
        this.constraints.add( builtInConstraints );
    }

    /**
     * @see EventPainter#paint(GL, Event, Event, EventPlotInfo, GlimpseBounds, Axis1D, int, int)
     */
    public void paint( GlimpseContext context, EventPainter defaultPainter, Event nextEvent, EventPlotInfo info, int posMin, int posMax )
    {
        EventPainter eventPainter = painter != null ? painter : defaultPainter;

        if ( eventPainter != null ) eventPainter.paint( context, this, nextEvent, info, posMin, posMax );
    }

    /**
     * Sets the height of the Event's icon (for horizontal timeline layouts) or the width (for vertical timeline layouts).
     *
     * @param iconSize the icon size in pixels
     */
    public void setIconSize( int iconSize )
    {
        this.iconSize = iconSize;
        this.setUseDefaultIconSize( false );
    }

    /**
     * @see #setIconSize(int)
     * @return the icon size in pixels
     */
    public int getIconSize( )
    {
        return this.iconSize;
    }

    /**
     * If a specific icon size has not been set, the default icon size is used. The default size is the minimum of the
     * actual size of the icon and the height of the event row.
     */
    public void setUseDefaultIconSize( boolean useDefaultSize )
    {
        this.useDefaultSize = useDefaultSize;
    }

    /**
     * @see #setUseDefaultIconSize(boolean)
     */
    public boolean isUseDefaultIconSize( )
    {
        return this.useDefaultSize;
    }

    /**
     * Events can be aggregated when many Events are close together in time and the timeline view is zoomed out very far. This
     * method will return true for aggregate events.
     *
     * @return true if the event is an aggregate event, false otherwise
     */
    public boolean hasChildren( )
    {
        return getEventCount( ) > 1;
    }

    /**
     * Gets the number of aggregated events that make up this event.
     */
    public int getEventCount( )
    {
        return 1;
    }

    /**
     * EventPlotInfo can automatically create synthetic groups of Events when the timeline
     * is zoomed out far enough that a bunch of Events are crowded into the same space.
     * The individual constituent Events can be accessed via this method.
     * User created Events never have children.
     */
    @Override
    public Iterator<Event> iterator( )
    {
        return new Iterator<Event>( )
        {
            boolean movedNext;

            @Override
            public boolean hasNext( )
            {
                return !movedNext;
            }

            @Override
            public Event next( )
            {
                movedNext = true;
                return Event.this;
            }

            @Override
            public void remove( )
            {
                throw new UnsupportedOperationException( );
            }
        };
    }

    /**
     * Sets a custom painter for this event. In addition to modifying the look of a single Event,
     * {@code EventPlotInfo#setEventPainter(EventPainter)} can be used to modify the default
     * EventPainter used for all events in the EventPlotInfo. However, the EventPainter set
     * here takes precedence.
     *
     * @param painter the EventPainter to use to render this event on the timeline
     */
    public void setEventPainter( EventPainter painter )
    {
        this.painter = painter;
    }

    /**
     * @see #setEventPainter(EventPainter)
     */
    public EventPainter getEventPainter( )
    {
        return this.painter;
    }

    /**
     * <p>Adds an EventConstraint which determines whether proposed changes to the min
     * and max time bounds of an Event are allowed.</p>
     *
     * <p>This method should be used for specialized constraints. Events support basic
     * constraints by default via setEditable, setResizeable, setEndTimeMoveable,
     * setStartTimeMoveable, setMinTimeSpan, and setMaxTimeSpan.</p>
     *
     * @param constraint the EventConstraint to add
     */
    public void addConstraint( EventConstraint constraint )
    {
        this.constraints.add( constraint );
    }

    /**
     * @see #addConstraint(EventConstraint)
     * @param constraint the EventConstraint to remove
     */
    public void removeConstraint( EventConstraint constraint )
    {
        this.constraints.remove( constraint );
    }

    /**
     * Sets the tooltip text to be displayed when the user mouses over this Event.
     */
    public void setToolTipText( String text )
    {
        this.toolTipText = text;
    }

    /**
     * @see #getToolTipText()
     */
    public String getToolTipText( )
    {
        return this.toolTipText;
    }

    /**
     * Sets whether or not this Event can be selected via mouse clicks. Setting
     * selectable to false does not prevent the event from being selected
     * programmatically via {@link EventPlotInfo#setSelectedEvents(java.util.Set)}.
     *
     * @param isSelectable
     */
    public void setSelectable( boolean isSelectable )
    {
        this.isSelectable = isSelectable;
    }

    /**
     * @see #isSelectable()
     */
    public boolean isSelectable( )
    {
        return this.isSelectable;
    }

    /**
     * Sets whether or not the Event start and end times are modifiable by the user
     * via mouse interaction. This does not prevent programmatically changing the
     * mouse bounds.
     *
     * For finer control over what the user is allowed to do when modifying the
     * start and end time of an Event (without disallowing it completely) see
     * {@link #setStartTimeMoveable(boolean)}, {@link #setResizeable(boolean)},
     * and {@link #setMinTimeSpan(double)}.
     */
    public void setEditable( boolean isEditable )
    {
        this.isEditable = isEditable;
    }

    /**
     * @see #setEditable(boolean)
     * @return
     */
    public boolean isEditable( )
    {
        return isEditable;
    }

    /**
     * @see #setEndTimeMoveable(boolean)
     * @return
     */
    public boolean isEndTimeMoveable( )
    {
        return isEndTimeMoveable;
    }

    /**
     * If true, the endTime of the Event cannot be adjusted by user mouse gestures.
     */
    public void setEndTimeMoveable( boolean isEndTimeMoveable )
    {
        this.isEndTimeMoveable = isEndTimeMoveable;
    }

    /**
     * @see #setStartTimeMoveable(boolean)
     * @return
     */
    public boolean isStartTimeMoveable( )
    {
        return isStartTimeMoveable;
    }

    /**
     * If true, the startTime of the Event cannot be adjusted by user mouse gestures.
     */
    public void setStartTimeMoveable( boolean isStartTimeMoveable )
    {
        this.isStartTimeMoveable = isStartTimeMoveable;
    }

    /**
     * {@link #setStartTimeMoveable(boolean)}
     * @return
     */
    public boolean isResizeable( )
    {
        return isResizeable;
    }

    /**
     * If true, the time span of the Event (the amount of time between the start and
     * end times) cannot be adjusted by user mouse gestures. However, the Event may
     * still be dragged.
     */
    public void setResizeable( boolean isResizeable )
    {
        this.isResizeable = isResizeable;
    }

    /**
     * {@link #setMaxTimeSpan(double)}
     * @return
     */
    public double getMaxTimeSpan( )
    {
        return maxTimeSpan;
    }

    /**
     * Sets the maximum time span between the start and end times. By default the
     * maximum is Double.MAX_VALUE.
     */
    public void setMaxTimeSpan( double maxTimeSpan )
    {
        this.maxTimeSpan = maxTimeSpan;
    }

    /**
     * {@link #setMinTimeSpan(double)}
     * @return
     */
    public double getMinTimeSpan( )
    {
        return minTimeSpan;
    }

    /**
     * Sets the minimum (inclusive) span between the start and end times. By default the
     * minimum is 0.
     */
    public void setMinTimeSpan( double minTimeSpan )
    {
        this.minTimeSpan = minTimeSpan;
    }

    /**
     * @return the text displayed inside the Event box on the timeline.
     */
    public String getLabel( )
    {
        return label;
    }

    /**
     * Sets the text displayed inside the Event box on the timeline.
     * @param name
     */
    public void setLabel( String name )
    {
        this.label = name;
    }

    /**
     * @see #setIconId(Object)
     * @return the identifier for the icon displayed inside the Event box on the timeline.
     */
    public Object getIconId( )
    {
        return iconId;
    }

    /**
     * Sets the icon displayed inside the Event box on the timeline. The iconId corresponds
     * to an icon loaded into the {@link TextureAtlas} associated with the {@link EventPlotInfo}
     * parent of this Event.
     *
     * @param iconId  the identifier for the icon displayed inside the Event box on the timeline.
     */
    public void setIconId( Object iconId )
    {
        this.iconId = iconId;
    }

    /**
     * @param thickness the thickness (in pixels) of the border around the box for this Event on the timeline.
     */
    public void setBorderThickness( float thickness )
    {
        this.borderThickness = thickness;
    }

    /**
     * @see #setBorderThickness(float)
     * @return
     */
    public float getBorderThickness( )
    {
        return this.borderThickness;
    }

    /**
     * {@link #setBackgroundColor(float[])}
     * @return
     */
    public float[] getBackgroundColor( )
    {
        return backgroundColor;
    }

    /**
     * @param backgroundColor the fill color of the box for this Event on the timeline.
     */
    public void setBackgroundColor( float[] backgroundColor )
    {
        this.backgroundColor = backgroundColor;
    }

    /**
     * {@link #setBorderColor(float[])}
     * @return
     */
    public float[] getBorderColor( )
    {
        return borderColor;
    }

    /**
     * @param borderColor the border color of the box for this Event on the timeline.
     */
    public void setBorderColor( float[] borderColor )
    {
        this.borderColor = borderColor;
    }

    /**
     * @see #setLabelColor(float[])
     * @return
     */
    public float[] getLabelColor( )
    {
        return textColor;
    }

    /**
     * @param textColor the color for the label text display for this Event on the timeline.
     */
    public void setLabelColor( float[] textColor )
    {
        this.textColor = textColor;
    }

    /**
     * <p>Sets the start and end time for this Event.</p>
     *
     * <p>If force is false, then the constraints (see {@link #addConstraint(EventConstraint)})
     * are taken into account and the final Event bounds might not be equal to
     * the input arguments.</p>
     *
     * @param startTime
     * @param endTime
     * @param force
     */
    public void setTimes( TimeStamp startTime, TimeStamp endTime, boolean force )
    {
        setTimes0( null, startTime, endTime, force );
    }

    protected void setTimes0( GlimpseMouseEvent mouseEvent, TimeStamp startTime, TimeStamp endTime, boolean force )
    {
        if ( !force )
        {
            TimeSpan newTimes = applyConstraints( new TimeSpan( startTime, endTime ) );
            startTime = newTimes.getStartTime( );
            endTime = newTimes.getEndTime( );
        }

        if ( this.info == null )
        {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        else
        {
            // if we're attached to a plot, delegate the update of our
            // start/end time to it, so that it can update its data structures
            this.info.updateEvent0( mouseEvent, this, startTime, endTime );
        }
    }

    /**
     * {@code EventPlotInfo} plots on a {@code StackedTimePlot2D} can place events into multiple
     * rows to prevent them from overlapping with one another if they overlap in time. This
     * method allows querying for the row that this Event has been assigned.
     */
    public int getRow( )
    {
        if ( this.info != null )
        {
            return this.info.getRow( id );
        }
        else
        {
            return 0;
        }
    }

    /**
     * Events may either be assigned a fixed row to be displayed on, or may float automatically between
     * rows to ensure that no Events overlap in time.
     *
     * @see #getRow()
     */
    public boolean isFixedRow( )
    {
        return this.fixedRow;
    }

    /**
     * @see #getRow()
     * @see #isFixedRow()
     */
    protected int getFixedRow( )
    {
        return this.fixedRowIndex;
    }

    /**
     * The row this Event appears on will be managed by its {@link EventPlotInfo}
     * parent. If {@link EventPlotInfo#setStackOverlappingEvents(boolean)} is set
     * to true, then the row will be set to avoid overlaps with other events, otherwise
     * the Event will be placed in the first row.
     */
    public void setFloatingRow( )
    {
        this.fixedRow = false;
    }

    /**
     * This event will appear on the requested row index in the timeline
     * regardless of whether that causes it to overlap with other Events.
     */
    public void setFixedRow( int rowIndex )
    {
        this.fixedRow = true;
        this.fixedRowIndex = rowIndex;

        if ( this.info != null )
        {
            this.info.updateEventRow( null, this, rowIndex );
        }
    }

    protected TimeSpan applyConstraints( TimeSpan span )
    {
        for ( EventConstraint constraint : constraints )
        {
            span = constraint.applyConstraint( this, span );
        }

        return span;
    }

    /**
     * {@link #setTimes(TimeStamp, TimeStamp, boolean)}
     * @param startTime
     * @param endTime
     */
    public void setTimes( TimeStamp startTime, TimeStamp endTime )
    {
        setTimes( startTime, endTime, true );
    }

    protected void setTimes0( TimeStamp startTime, TimeStamp endTime )
    {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * @return the start / earliest / left-edge TimeStamp for this Event.
     */
    public TimeStamp getStartTime( )
    {
        return startTime;
    }

    /**
     * @see #setTimes(TimeStamp, TimeStamp, boolean)
     * @param startTime
     */
    public void setStartTime( TimeStamp startTime )
    {
        setTimes( startTime, this.endTime, true );
    }

    protected void setStartTime0( TimeStamp startTime )
    {
        this.startTime = startTime;
    }

    /**
     * @return the end / latest / right-edge TimeStamp for this Event.
     */
    public TimeStamp getEndTime( )
    {
        return endTime;
    }

    /**
     * @see #setTimes(TimeStamp, TimeStamp, boolean)
     * @param startTime
     */
    public void setEndTime( TimeStamp endTime )
    {
        setTimes( this.startTime, endTime, true );
    }

    protected void setEndTime0( TimeStamp endTime )
    {
        this.endTime = endTime;
    }

    /**
     * @return the start and end times of the Event packaged in a TimeSpan
     */
    public TimeSpan getTimeSpan( )
    {
        return new TimeSpan( startTime, endTime );
    }

    /**
     * return whether the label associated with this event should be shown when room permits.
     */
    public boolean isShowLabel( )
    {
        return showLabel;
    }

    /**
     * @param showName whether to show the label text in this Event's box on the timeline.
     */
    public void setShowLabel( boolean showName )
    {
        this.showLabel = showName;
    }

    /**
     * Set what types of overlaps should be considered when determining whether to shorten Event box text
     * and whether to display the Event's icon.
     *
     * @param mode
     */
    public void setOverlapMode( OverlapRenderingMode mode )
    {
        this.overlapRenderingMode = mode;
    }

    /**
     * @see #setOverlapMode(OverlapRenderingMode)
     */
    public OverlapRenderingMode getOverlapRenderingMode( )
    {
        return startTime.equals( endTime ) ? Intersecting : this.overlapRenderingMode;
    }

    /**
     * Sets how text and icons should be handled when this Event's box is too small or when it overlaps
     * with another Event's box.
     *
     * @param mode
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

    /**
     * return whether the icon associated with this event should be shown when room permits.
     */
    public boolean isShowIcon( )
    {
        return showIcon;
    }

    /**
     * @param showIcon whether to show the icon associated with this event.
     */
    public void setShowIcon( boolean showIcon )
    {
        this.showIcon = showIcon;
    }

    /**
     * return if true, the event box is filled with the background color. If false, the box is transparent.
     */
    public boolean isShowBackground( )
    {
        return showBackground;
    }

    /**
     * @param showBorder whether to fill the event box with the background color.
     */
    public void setShowBackground( boolean showBorder )
    {
        this.showBackground = showBorder;
    }

    /**
     * @return if true, a line border is drawn around the event box.
     */
    public boolean isShowBorder( )
    {
        return showBorder;
    }

    /**
     * @param showBorder whether to draw a line border around the event box.
     */
    public void setShowBorder( boolean showBorder )
    {
        this.showBorder = showBorder;
    }

    /**
     * <p>An Event's id may be any Object, but its {@link #equals(Object)} and {@link #hashCode()}
     * methods should be properly implemented and it should be unique among the Events of an
     * {@link EventPlotInfo} timeline.</p>
     *
     * <p>{@link EventPlotListener} will use this id when reporting events occurring on this Event.</p>
     *
     * @return the unique id for this Event.
     */
    public Object getId( )
    {
        return id;
    }

    /**
     * @return the timeline which this Event is attached to.
     */
    public EventPlotInfo getEventPlotInfo( )
    {
        return info;
    }

    /**
     * @return the amount of overlap between this event and the given event in system units (minutes)
     */
    public double getOverlapTime( Event event )
    {
        double maxStart = Math.max( event.getStartTime( ).toPosixSeconds( ), getStartTime( ).toPosixSeconds( ) );
        double minEnd = Math.min( event.getEndTime( ).toPosixSeconds( ), getEndTime( ).toPosixSeconds( ) );
        return Math.max( 0, minEnd - maxStart );
    }

    /**
     * @return the duration of the event (time between start and end TimeStamps) in system units (minutes)
     */
    public double getDuration( )
    {
        return startTime.durationBefore( endTime );
    }

    /**
     * <p>Returns whether the provided Timestamp is inside the bounds of this Event. The startTime
     * is treated as inclusive and the endTime is treated as exclusive. This means that instantaneous
     * events (with startTime equal to endTime) will always return false.</p>
     *
     * <p>This method is equivalent to
     * event.getStartTime().isBeforeOrEquals( time ) && event.getEndTime().isAfter( time )</p>
     *
     * @param time the TimeStamp to test
     * @return whether the TimeStamp is inside this Event's time bounds
     */
    public boolean contains( TimeStamp time )
    {
        return startTime.isBeforeOrEquals( time ) && endTime.isAfter( time );
    }

    /**
     * The parent EventPlotInfo of an Event should be modified by
     * calling {@link EventPlotInfo#addEvent(Event)} and
     * {@link EventPlotInfo#removeEvent(Event)}.
     *
     * @param info
     */
    protected void setEventPlotInfo( EventPlotInfo info )
    {
        this.info = info;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode( ) );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        Event other = ( Event ) obj;
        if ( id == null )
        {
            if ( other.id != null ) return false;
        }
        else if ( !id.equals( other.id ) ) return false;
        return true;
    }

    @Override
    public String toString( )
    {
        return String.format( "%s (%s)", label, id );
    }

    public static Event createDummyEvent( Event event )
    {
        TimeStamp startTime = TimeStamp.fromTimeStamp( event.getStartTime( ) );
        TimeStamp endTime = TimeStamp.fromTimeStamp( event.getEndTime( ) );
        return new Event( event.getId( ), null, startTime, endTime );
    }

    public static Event createDummyEvent( TimeStamp time )
    {
        return new Event( time );
    }

    public static Comparator<Event> getStartTimeComparator( )
    {
        return new Comparator<Event>( )
        {
            @Override
            public int compare( Event o1, Event o2 )
            {
                int c_time = o1.getStartTime( ).compareTo( o2.getStartTime( ) );

                if ( c_time == 0 )
                {
                    // if the times are equal but object ids are not, the comparator
                    // should not return 0 to remain consistent with equals
                    // otherwise we make a rather arbitrary decision about ordering
                    if ( o1.getId( ) == null && o2.getId( ) == null )
                    {
                        return 0;
                    }
                    else if ( o1.getId( ) == null )
                    {
                        return -1;
                    }
                    else if ( o2.getId( ) == null )
                    {
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                }
                else
                {
                    return c_time;
                }
            }
        };
    }

    public static Comparator<Event> getEndTimeComparator( )
    {
        return new Comparator<Event>( )
        {
            @Override
            public int compare( Event o1, Event o2 )
            {
                return o1.getEndTime( ).compareTo( o2.getEndTime( ) );
            }
        };
    }
}

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

import static com.metsci.glimpse.plot.timeline.event.Event.OverlapRenderingMode.Intersecting;
import static com.metsci.glimpse.plot.timeline.event.Event.OverlapRenderingMode.Overfull;
import static com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode.Ellipsis;
import static com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode.HideAll;
import static com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode.ShowAll;

import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.EventConstraint;
import com.metsci.glimpse.plot.timeline.data.TimeSpan;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.atlas.support.ImageData;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.sun.opengl.util.j2d.TextRenderer;

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
public class Event
{
    public static final int ARROW_TIP_BUFFER = 2;
    public static final int ARROW_SIZE = 10;
    public static final float[] DEFAULT_COLOR = GlimpseColor.getGray( );

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

    protected boolean isIconVisible;
    protected boolean isTextVisible;
    protected TimeStamp iconStartTime;
    protected TimeStamp iconEndTime;
    protected TimeStamp textStartTime;
    protected TimeStamp textEndTime;

    protected boolean isEditable = true;
    protected boolean isEndTimeMoveable = true;
    protected boolean isStartTimeMoveable = true;
    protected boolean isResizeable = true;
    protected double maxTimeSpan = Double.MAX_VALUE;
    protected double minTimeSpan = 0;

    protected List<EventConstraint> constraints;

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
            if ( !isEditable ) return event.getTimeSpan( );

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
        this( null, null, time );
    }

    public Event( Object id, String name, TimeStamp time )
    {
        this.id = id;
        this.label = name;

        this.startTime = time;
        this.endTime = time;
        this.overlapRenderingMode = Intersecting;

        this.constraints = new LinkedList<EventConstraint>( );
        this.constraints.add( builtInConstraints );
    }

    public Event( Object id, String name, TimeStamp startTime, TimeStamp endTime )
    {
        this.id = id;
        this.label = name;

        this.startTime = startTime;
        this.endTime = endTime;
        this.overlapRenderingMode = startTime.equals( endTime ) ? Intersecting : Overfull;

        this.constraints = new LinkedList<EventConstraint>( );
        this.constraints.add( builtInConstraints );
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
    public void removeConstrain( EventConstraint constraint )
    {
        this.constraints.remove( constraint );
    }

    protected float[] getBackgroundColor( EventPainter painter, boolean isSelected )
    {
        float[] defaultColor = painter.getBackgroundColor( );
        float[] selectedColor = painter.getSelectedEventBackgroundColor( );

        if ( isSelected )
        {
            if ( selectedColor != null )
                return selectedColor;
            else if ( backgroundColor != null )
                return backgroundColor;
            else
                return defaultColor;
        }
        else
        {
            if ( backgroundColor != null )
                return backgroundColor;
            else
                return defaultColor;
        }
    }

    protected float[] getBorderColor( EventPainter painter, boolean isSelected )
    {
        float[] defaultColor = painter.getBorderColor( );
        float[] selectedColor = painter.getSelectedEventBorderColor( );

        if ( isSelected )
        {
            if ( selectedColor != null )
                return selectedColor;
            else if ( borderColor != null )
                return borderColor;
            else
                return defaultColor;
        }
        else
        {
            if ( borderColor != null )
                return borderColor;
            else
                return defaultColor;
        }
    }

    protected float getBorderThickness( EventPainter painter, boolean isSelected )
    {
        if ( isSelected )
        {
            return painter.getSelectedEventBorderThickness( );
        }
        else
        {
            return borderThickness;
        }
    }

    public void paint( GL gl, Axis1D axis, EventPainter painter, Event next, int width, int height, int sizeMin, int sizeMax )
    {
        int size = sizeMax - sizeMin;
        double sizeCenter = sizeMin + size / 2.0;
        int buffer = painter.getRowBufferSize( );
        int arrowSize = Math.min( size, ARROW_SIZE );

        Epoch epoch = painter.getEpoch( );
        double timeMin = epoch.fromTimeStamp( startTime );
        double timeMax = epoch.fromTimeStamp( endTime );

        double arrowBaseMin = timeMin;
        boolean offEdgeMin = false;
        if ( axis.getMin( ) > timeMin )
        {
            offEdgeMin = true;
            timeMin = axis.getMin( ) + ARROW_TIP_BUFFER / axis.getPixelsPerValue( );
            arrowBaseMin = timeMin + arrowSize / axis.getPixelsPerValue( );
        }

        double arrowBaseMax = timeMax;
        boolean offEdgeMax = false;
        if ( axis.getMax( ) < timeMax )
        {
            offEdgeMax = true;
            timeMax = axis.getMax( ) - ARROW_TIP_BUFFER / axis.getPixelsPerValue( );
            arrowBaseMax = timeMax - arrowSize / axis.getPixelsPerValue( );
        }

        arrowBaseMax = Math.max( timeMin, arrowBaseMax );
        arrowBaseMin = Math.min( timeMax, arrowBaseMin );

        double timeSpan = arrowBaseMax - arrowBaseMin;
        double remainingSpaceX = axis.getPixelsPerValue( ) * timeSpan - buffer * 2;

        int pixelX = buffer + ( offEdgeMin ? arrowSize : 0 ) + Math.max( 0, axis.valueToScreenPixel( timeMin ) );

        // start positions of the next event in this row
        double nextStartValue = next != null ? epoch.fromTimeStamp( next.getStartTime( ) ) : axis.getMax( );
        int nextStartPixel = next != null ? axis.valueToScreenPixel( nextStartValue ) : width;

        boolean highlightSelected = painter.getEventPlotInfo( ).isHighlightSelectedEvents( );
        boolean isSelected = highlightSelected ? painter.getEventPlotInfo( ).isEventSelected( this ) : false;

        if ( painter.isHorizontal( ) )
        {
            if ( !offEdgeMin && !offEdgeMax )
            {
                if ( showBackground )
                {
                    GlimpseColor.glColor( gl, getBackgroundColor( painter, isSelected ) );
                    gl.glBegin( GL.GL_QUADS );
                    try
                    {
                        gl.glVertex2d( timeMin, sizeMin );
                        gl.glVertex2d( timeMin, sizeMax );
                        gl.glVertex2d( timeMax, sizeMax );
                        gl.glVertex2d( timeMax, sizeMin );
                    }
                    finally
                    {
                        gl.glEnd( );
                    }
                }

                if ( showBorder )
                {
                    GlimpseColor.glColor( gl, getBorderColor( painter, isSelected ) );
                    gl.glLineWidth( getBorderThickness( painter, isSelected ) );
                    gl.glBegin( GL.GL_LINE_LOOP );
                    try
                    {
                        gl.glVertex2d( timeMin, sizeMin );
                        gl.glVertex2d( timeMin, sizeMax );
                        gl.glVertex2d( timeMax, sizeMax );
                        gl.glVertex2d( timeMax, sizeMin );
                    }
                    finally
                    {
                        gl.glEnd( );
                    }
                }
            }
            else
            {
                if ( showBackground )
                {
                    GlimpseColor.glColor( gl, getBackgroundColor( painter, isSelected ) );
                    gl.glBegin( GL.GL_POLYGON );
                    try
                    {
                        gl.glVertex2d( arrowBaseMin, sizeMax );
                        gl.glVertex2d( arrowBaseMax, sizeMax );
                        gl.glVertex2d( timeMax, sizeCenter );
                        gl.glVertex2d( arrowBaseMax, sizeMin );
                        gl.glVertex2d( arrowBaseMin, sizeMin );
                        gl.glVertex2d( timeMin, sizeCenter );
                    }
                    finally
                    {
                        gl.glEnd( );
                    }
                }

                if ( showBorder )
                {
                    GlimpseColor.glColor( gl, getBorderColor( painter, isSelected ) );
                    gl.glLineWidth( getBorderThickness( painter, isSelected ) );
                    gl.glBegin( GL.GL_LINE_LOOP );
                    try
                    {
                        gl.glVertex2d( arrowBaseMin, sizeMax );
                        gl.glVertex2d( arrowBaseMax, sizeMax );
                        gl.glVertex2d( timeMax, sizeCenter );
                        gl.glVertex2d( arrowBaseMax, sizeMin );
                        gl.glVertex2d( arrowBaseMin, sizeMin );
                        gl.glVertex2d( timeMin, sizeCenter );
                    }
                    finally
                    {
                        gl.glEnd( );
                    }
                }
            }

            isIconVisible = showIcon && iconId != null && !isIconOverlapping( size, buffer, remainingSpaceX, pixelX, nextStartPixel );

            if ( isIconVisible )
            {
                double valueX = axis.screenPixelToValue( pixelX );
                iconStartTime = epoch.toTimeStamp( valueX );
                iconEndTime = iconStartTime.add( size / axis.getPixelsPerValue( ) );

                TextureAtlas atlas = painter.getTextureAtlas( );
                atlas.beginRendering( );
                try
                {
                    ImageData iconData = atlas.getImageData( iconId );
                    double iconScale = size / ( double ) iconData.getHeight( );

                    atlas.drawImageAxisX( gl, iconId, axis, valueX, sizeMin, iconScale, iconScale, 0, iconData.getHeight( ) );
                }
                finally
                {
                    atlas.endRendering( );
                }

                remainingSpaceX -= size + buffer;
                pixelX += size + buffer;
            }

            if ( showLabel )
            {
                TextRenderer textRenderer = painter.getTextRenderer( );
                Rectangle2D labelBounds = textRenderer.getBounds( label );

                boolean isTextOverfull = isTextOverfull( size, buffer, remainingSpaceX, pixelX, nextStartPixel, labelBounds );
                boolean isTextIntersecting = isTextIntersecting( size, buffer, remainingSpaceX, pixelX, nextStartPixel, labelBounds );
                boolean isTextOverlappingAndHidden = ( ( isTextOverfull || isTextIntersecting ) && textRenderingMode == HideAll );
                double availableSpace = getTextAvailableSpace( size, buffer, remainingSpaceX, pixelX, nextStartPixel );

                isTextVisible = !isTextOverlappingAndHidden;

                if ( isTextVisible )
                {
                    Rectangle2D displayBounds = labelBounds;
                    String displayText = label;

                    if ( labelBounds.getWidth( ) > availableSpace && textRenderingMode != ShowAll )
                    {
                        displayText = calculateDisplayText( textRenderer, displayText, availableSpace );
                        displayBounds = textRenderer.getBounds( displayText );
                    }

                    double valueX = axis.screenPixelToValue( pixelX );
                    textStartTime = epoch.toTimeStamp( valueX );
                    textEndTime = textStartTime.add( displayBounds.getWidth( ) / axis.getPixelsPerValue( ) );

                    // use this event's text color if it has been set
                    if ( textColor != null )
                    {
                        GlimpseColor.setColor( textRenderer, textColor );
                    }
                    // otherwise, use the default no background color if the background is not showing
                    // and if a color has not been explicitly set for the EventPainter
                    else if ( !painter.textColorSet && !showBackground )
                    {
                        GlimpseColor.setColor( textRenderer, painter.textColorNoBackground );
                    }
                    // otherwise use the EventPainter's default text color
                    else
                    {
                        GlimpseColor.setColor( textRenderer, painter.textColor );
                    }

                    textRenderer.beginRendering( width, height );
                    try
                    {
                        // use the labelBounds for the height (if the text shortening removed a character which
                        // hangs below the line, we don't want the text position to move)
                        int pixelY = ( int ) ( size / 2.0 - labelBounds.getHeight( ) * 0.3 + sizeMin );
                        textRenderer.draw( displayText, pixelX, pixelY );

                        remainingSpaceX -= displayBounds.getWidth( ) + buffer;
                        pixelX += displayBounds.getWidth( ) + buffer;
                    }
                    finally
                    {
                        textRenderer.endRendering( );
                    }
                }
            }
            else
            {
                isTextVisible = false;
            }
        }
        else
        {
            //TODO handle drawing text and icons in HORIZONTAL orientation

            GlimpseColor.glColor( gl, getBackgroundColor( painter, isSelected ) );
            gl.glBegin( GL.GL_QUADS );
            try
            {
                gl.glVertex2d( sizeMin, timeMin );
                gl.glVertex2d( sizeMax, timeMin );
                gl.glVertex2d( sizeMax, timeMax );
                gl.glVertex2d( sizeMin, timeMax );
            }
            finally
            {
                gl.glEnd( );
            }

            GlimpseColor.glColor( gl, getBorderColor( painter, isSelected ) );
            gl.glLineWidth( getBorderThickness( painter, isSelected ) );
            gl.glBegin( GL.GL_LINE_LOOP );
            try
            {
                gl.glVertex2d( sizeMin, timeMin );
                gl.glVertex2d( sizeMax, timeMin );
                gl.glVertex2d( sizeMax, timeMax );
                gl.glVertex2d( sizeMin, timeMax );
            }
            finally
            {
                gl.glEnd( );
            }
        }
    }

    protected String calculateDisplayText( TextRenderer textRenderer, String fullText, double availableSpace )
    {
        for ( int endIndex = fullText.length( ); endIndex >= 0; endIndex-- )
        {
            String subText = fullText.substring( 0, endIndex ) + "...";
            Rectangle2D bounds = textRenderer.getBounds( subText );
            if ( bounds.getWidth( ) < availableSpace ) return subText;
        }

        return "";
    }

    protected double getTextAvailableSpace( int size, int buffer, double remainingSpaceX, int pixelX, int nextStartPixel )
    {
        double insideBoxSpace = remainingSpaceX - buffer;
        double outsideBoxSpace = nextStartPixel - pixelX - buffer;

        switch ( overlapRenderingMode )
        {
        case Overfull:
            return insideBoxSpace;
        case Intersecting:
            return outsideBoxSpace;
        case None:
        default:
            return Double.MAX_VALUE;
        }
    }

    protected boolean isTextOverfull( int size, int buffer, double remainingSpaceX, int pixelX, int nextStartPixel, Rectangle2D bounds )
    {
        return bounds.getWidth( ) + buffer > remainingSpaceX && overlapRenderingMode == Overfull;
    }

    protected boolean isTextIntersecting( int size, int buffer, double remainingSpaceX, int pixelX, int nextStartPixel, Rectangle2D bounds )
    {
        return pixelX + bounds.getWidth( ) + buffer > nextStartPixel && overlapRenderingMode == Intersecting;
    }

    protected boolean isIconOverlapping( int size, int buffer, double remainingSpaceX, int pixelX, int nextStartPixel )
    {
        return ( size + buffer > remainingSpaceX && overlapRenderingMode == Overfull ) || ( pixelX + size + buffer > nextStartPixel && overlapRenderingMode == Intersecting );
    }

    public void setToolTipText( String text )
    {
        this.toolTipText = text;
    }

    public String getToolTipText( )
    {
        return this.toolTipText;
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
     * @deprecated use {@link #getLabel()}
     * @return
     */
    public String getName( )
    {
        return label;
    }

    /**
     * @deprecated use {@link #setLabel(String)}
     * @return
     */
    public void setName( String name )
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
            this.info.updateEvent( this, startTime, endTime );
        }
    }

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

    public boolean isFixedRow( )
    {
        return this.fixedRow;
    }

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
            this.info.updateEventRow( this, rowIndex );
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
        setTimes( startTime, endTime, false );
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
        setTimes( startTime, this.endTime );
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
        setTimes( this.startTime, endTime );
    }

    protected void setEndTime0( TimeStamp endTime )
    {
        this.endTime = endTime;
    }

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
     * @return if false, the label is not visible, either because there is no room to show it,
     *         or {@link #isShowLabel()} is set to false.
     */
    public boolean isLabelVisible( )
    {
        return isTextVisible;
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
        return this.overlapRenderingMode;
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
     * @return if false, the icon is not visible, either because there is no room to show it,
     *         or {@link #isShowIcon()} is set to false.
     */
    public boolean isIconVisible( )
    {
        return isIconVisible;
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
     * Returns the timestamp associated with the left hand side of the icon. Because the icon
     * is drawn at a fixed pixel size, this value will change as the timeline scale is zoomed
     * in and out. This method is intended mainly for use by display routines.
     */
    public TimeStamp getIconStartTime( )
    {
        return iconStartTime;
    }

    /**
     * Returns the timestamp associated with the right hand side of the icon. Because the icon
     * is drawn at a fixed pixel size, this value will change as the timeline scale is zoomed
     * in and out. This method is intended mainly for use by display routines.
     */
    public TimeStamp getIconEndTime( )
    {
        return iconEndTime;
    }

    /**
     * Returns the timestamp associated with the left hand side of the label. Because the label
     * is drawn at a fixed pixel size, this value will change as the timeline scale is zoomed
     * in and out. This method is intended mainly for use by display routines.
     */
    public TimeStamp getLabelStartTime( )
    {
        return textStartTime;
    }

    /**
     * Returns the timestamp associated with the right hand side of the label. Because the label
     * is drawn at a fixed pixel size, this value will change as the timeline scale is zoomed
     * in and out. This method is intended mainly for use by display routines.
     */
    public TimeStamp getLabelEndTime( )
    {
        return textEndTime;
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

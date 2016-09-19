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

import com.metsci.glimpse.plot.timeline.event.listener.EventSelectionHandler;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * Metadata about an Event used by the {@link EventSelectionHandler} to indicate
 * what part of an event was clicked (the icon, label, etc...).
 *
 * @author ulman
 */
public class EventBounds
{
    protected boolean isIconVisible;
    protected boolean isTextVisible;
    protected TimeStamp iconStartTime;
    protected TimeStamp iconEndTime;
    protected TimeStamp textStartTime;
    protected TimeStamp textEndTime;

    public boolean containsText( TimeStamp time )
    {
        TimeStamp l1 = getTextStartTime( );
        TimeStamp l2 = getTextEndTime( );

        if ( l1 == null || l2 == null ) return false;

        boolean text = isTextVisible( ) && l1 != null && l2 != null && time.isAfterOrEquals( l1 ) && time.isBeforeOrEquals( l2 );

        return text;
    }

    public boolean containsIcon( TimeStamp time )
    {
        TimeStamp i1 = getIconStartTime( );
        TimeStamp i2 = getIconEndTime( );

        if ( i1 == null || i2 == null ) return false;

        boolean icon = isIconVisible( ) && i1 != null && i2 != null && time.isAfterOrEquals( i1 ) && time.isBeforeOrEquals( i2 );

        return icon;
    }

    public void setIconVisible( boolean isIconVisible )
    {
        this.isIconVisible = isIconVisible;
    }

    public void setTextVisible( boolean isTextVisible )
    {
        this.isTextVisible = isTextVisible;
    }

    public void setIconStartTime( TimeStamp iconStartTime )
    {
        this.iconStartTime = iconStartTime;
    }

    public void setIconEndTime( TimeStamp iconEndTime )
    {
        this.iconEndTime = iconEndTime;
    }

    public void setTextStartTime( TimeStamp textStartTime )
    {
        this.textStartTime = textStartTime;
    }

    public void setTextEndTime( TimeStamp textEndTime )
    {
        this.textEndTime = textEndTime;
    }

    /**
     * @return if false, the label is not visible, either because there is no room to show it,
     *         or {@link #isShowLabel()} is set to false.
     */
    public boolean isTextVisible( )
    {
        return isTextVisible;
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
    public TimeStamp getTextStartTime( )
    {
        return textStartTime;
    }

    /**
     * Returns the timestamp associated with the right hand side of the label. Because the label
     * is drawn at a fixed pixel size, this value will change as the timeline scale is zoomed
     * in and out. This method is intended mainly for use by display routines.
     */
    public TimeStamp getTextEndTime( )
    {
        return textEndTime;
    }
}

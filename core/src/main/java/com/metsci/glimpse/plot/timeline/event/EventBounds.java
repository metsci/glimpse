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

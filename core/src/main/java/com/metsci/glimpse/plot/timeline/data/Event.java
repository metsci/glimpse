package com.metsci.glimpse.plot.timeline.data;

import java.awt.geom.Rectangle2D;
import java.util.Comparator;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.plot.timeline.painter.EventPainter;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.atlas.support.ImageData;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.sun.opengl.util.j2d.TextRenderer;

public class Event
{
    public static final float[] DEFAULT_COLOR = GlimpseColor.getGray( );

    protected Object id;
    protected String name;
    protected Object iconId; // references id in associated TextureAtlas

    protected float[] backgroundColor;
    protected float[] borderColor;
    protected float[] textColor;
    protected float borderThickness = 1.8f;

    protected TimeStamp startTime;
    protected TimeStamp endTime;

    protected boolean showName = true;
    protected boolean showIcon = true;
    protected boolean showBorder = true;

    protected boolean hideOverfull;
    protected boolean hideIntersecting;
    
    protected boolean isIconVisible;
    protected boolean isTextVisible;
    protected TimeStamp iconStartTime;
    protected TimeStamp iconEndTime;
    protected TimeStamp textStartTime;
    protected TimeStamp textEndTime;

    private Event( TimeStamp time )
    {
        this( null, null, time );
    }

    public Event( Object id, String name, TimeStamp time )
    {
        this.id = id;
        this.name = name;
        this.startTime = time;
        this.endTime = time;
        
        this.hideIntersecting = true;
        this.hideOverfull = false;
    }

    public Event( Object id, String name, TimeStamp startTime, TimeStamp endTime )
    {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        
        this.hideIntersecting = true;
        this.hideOverfull = true;
    }

    public void paint( GL gl, Axis1D axis, EventPainter painter, Event next, int width, int height, int sizeMin, int sizeMax )
    {
        int size = sizeMax - sizeMin;
        int buffer = painter.getBufferSize( );
        
        Epoch epoch = painter.getEpoch( );
        double timeMin = epoch.fromTimeStamp( startTime );
        double timeMax = epoch.fromTimeStamp( endTime );

        double timeSpan = Math.min( axis.getMax( ), timeMax ) - Math.max( axis.getMin( ), timeMin );
        double remainingSpaceX = axis.getPixelsPerValue( ) * timeSpan - buffer * 2;

        int pixelX = buffer + Math.max( 0, axis.valueToScreenPixel( timeMin ) );
        
        // start positions of the next event in this row
        double nextStartValue = next != null ? epoch.fromTimeStamp( next.getStartTime( ) ) : Double.MAX_VALUE;
        int nextStartPixel = next != null ? axis.valueToScreenPixel( nextStartValue ) : Integer.MAX_VALUE;
        
        if ( painter.isHorizontal( ) )
        {
            GlimpseColor.glColor( gl, backgroundColor != null ? backgroundColor : painter.getBackgroundColor( ) );
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

            if ( showBorder )
            {
                GlimpseColor.glColor( gl, borderColor != null ? borderColor : painter.getBorderColor( ) );
                gl.glLineWidth( borderThickness );
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

            isIconVisible = isIconVisible( size, buffer, remainingSpaceX, pixelX, nextStartPixel );
            
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

            TextRenderer textRenderer = painter.getTextRenderer( );
            Rectangle2D bounds = showName ? textRenderer.getBounds( name ) : null;

            isTextVisible = isTextVisible( size, buffer, remainingSpaceX, pixelX, nextStartPixel, bounds );
            
            if ( isTextVisible )
            {
                double valueX = axis.screenPixelToValue( pixelX );
                textStartTime = epoch.toTimeStamp( valueX );
                textEndTime = textStartTime.add( bounds.getWidth( ) / axis.getPixelsPerValue( ) );
                
                GlimpseColor.setColor( textRenderer, textColor != null ? textColor : painter.getTextColor( ) );
                textRenderer.beginRendering( width, height );
                try
                {
                    int pixelY = ( int ) ( size / 2.0 - bounds.getHeight( ) * 0.3 + sizeMin );
                    textRenderer.draw( name, pixelX, pixelY );

                    remainingSpaceX -= bounds.getWidth( ) + buffer;
                    pixelX += bounds.getWidth( ) + buffer;
                }
                finally
                {
                    textRenderer.endRendering( );
                }
            }
        }
        else
        {
            //TODO handle drawing text and icons in HORIZONTAL orientation

            GlimpseColor.glColor( gl, backgroundColor != null ? backgroundColor : painter.getBackgroundColor( ) );
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

            GlimpseColor.glColor( gl, borderColor != null ? borderColor : painter.getBorderColor( ) );
            gl.glLineWidth( borderThickness );
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
    
    protected boolean isTextVisible( int size, int buffer, double remainingSpaceX, int pixelX, int nextStartPixel, Rectangle2D bounds )
    {
        return showName && ( bounds.getWidth( ) + buffer < remainingSpaceX || !hideOverfull ) && ( pixelX + bounds.getWidth( ) + buffer < nextStartPixel || !hideIntersecting );
    }
    
    protected boolean isIconVisible( int size, int buffer, double remainingSpaceX, int pixelX, int nextStartPixel )
    {
        return showIcon && iconId != null && ( size + buffer < remainingSpaceX || !hideOverfull ) && ( pixelX + size + buffer < nextStartPixel || !hideIntersecting );
    }

    public String getLabel( )
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public Object getIconId( )
    {
        return iconId;
    }

    public void setIconId( Object iconId )
    {
        this.iconId = iconId;
    }

    public void setBorderThickness( float thickness )
    {
        this.borderThickness = thickness;
    }

    public float[] getBackgroundColor( )
    {
        return backgroundColor;
    }

    public void setBackgroundColor( float[] backgroundColor )
    {
        this.backgroundColor = backgroundColor;
    }

    public float[] getBorderColor( )
    {
        return borderColor;
    }

    public void setBorderColor( float[] borderColor )
    {
        this.borderColor = borderColor;
    }

    public float[] getLabelColor( )
    {
        return textColor;
    }

    public void setLabelColor( float[] textColor )
    {
        this.textColor = textColor;
    }

    public TimeStamp getStartTime( )
    {
        return startTime;
    }

    public void setStartTime( TimeStamp startTime )
    {
        this.startTime = startTime;
    }

    public TimeStamp getEndTime( )
    {
        return endTime;
    }

    public void setEndTime( TimeStamp endTime )
    {
        this.endTime = endTime;
    }

    public boolean isShowLabel( )
    {
        return showName;
    }

    public void setShowLabel( boolean showName )
    {
        this.showName = showName;
    }

    /**
     * If true, hides labels and/or icons if they would intersect with other events.
     */
    public void setHideIntersecting( boolean hide )
    {
        this.hideIntersecting = hide;
    }
    
    /**
     * If true, hides labels and/or icons if they would fall outside this event's time window.
     */
    public void setHideOverfull( boolean hide )
    {
        this.hideOverfull = hide;
    }

    public boolean isShowIcon( )
    {
        return showIcon;
    }

    public void setShowIcon( boolean showIcon )
    {
        this.showIcon = showIcon;
    }

    public boolean isShowBorder( )
    {
        return showBorder;
    }

    public void setShowBorder( boolean showBorder )
    {
        this.showBorder = showBorder;
    }

    public Object getId( )
    {
        return id;
    }
    
    public boolean isIconVisible( )
    {
        return isIconVisible;
    }

    public boolean isLabelVisible( )
    {
        return isTextVisible;
    }

    public TimeStamp getIconStartTime( )
    {
        return iconStartTime;
    }

    public TimeStamp getIconEndTime( )
    {
        return iconEndTime;
    }

    public TimeStamp getLabelStartTime( )
    {
        return textStartTime;
    }

    public TimeStamp getLabelEndTime( )
    {
        return textEndTime;
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
        return String.format( "%s (%s)", name, id );
    }

    public static Comparator<Event> getStartTimeComparator( )
    {
        return new Comparator<Event>( )
        {
            @Override
            public int compare( Event o1, Event o2 )
            {
                return o1.getStartTime( ).compareTo( o2.getStartTime( ) );
            }
        };
    }

    public static Event createDummyEvent( Event event )
    {
        return new Event( event.getId( ), null, event.getStartTime( ), event.getEndTime( ) );
    }
    
    public static Event createDummyEvent( TimeStamp time )
    {
        return new Event( time );
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

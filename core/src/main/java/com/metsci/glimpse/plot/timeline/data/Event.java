package com.metsci.glimpse.plot.timeline.data;

import java.awt.geom.Rectangle2D;
import java.util.Comparator;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.plot.timeline.painter.EventPainter;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.sun.opengl.util.j2d.TextRenderer;

public class Event
{
    public static final float[] DEFAULT_COLOR = GlimpseColor.getGray( );
    public static final int BUFFER = 2;
    
    protected Object id;
    protected String name;
    protected Object iconId; // references id in associated TextureAtlas
    
    protected float[] backgroundColor;
    protected float[] borderColor;
    protected float[] textColor;
    protected float borderThickness = 1.8f;
    
    protected TimeStamp startTime;
    protected TimeStamp endTime;

    protected boolean showName;
    protected boolean showIcon;
    protected boolean showBorder;
    
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
    }

    public Event( Object id, String name, TimeStamp startTime, TimeStamp endTime )
    {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public void paint( GL gl, Axis1D axis, EventPainter painter, int width, int height, int sizeMin, int sizeMax )
    {
        Epoch epoch = painter.getEpoch( );
        double timeMin = epoch.fromTimeStamp( startTime );
        double timeMax = epoch.fromTimeStamp( endTime );
        double timeSpan = timeMax - timeMin;
        double pixelSpan = axis.getPixelsPerValue( ) * timeSpan;
        
        
        if ( painter.isHorizontal( ) )
        {
            // draw text
            TextRenderer textRenderer = painter.getTextRenderer( );
            Rectangle2D bounds = textRenderer.getBounds( name );
            
            
            
            // only draw the text if it will fit in the event box
            if ( bounds.getWidth( ) < pixelSpan )
            {
                GlimpseColor.setColor( textRenderer, textColor != null ? textColor : painter.getTextColor( ) );
                textRenderer.beginRendering( width, height );
                try
                {
                    int x = BUFFER + axis.valueToScreenPixel( timeMin );
                    int y = (int) ( height / 2.0 - bounds.getHeight( ) * 0.38 );
                    textRenderer.draw( name, x, y );
                }
                finally
                {
                    textRenderer.endRendering( );
                }
            }
            
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
        else
        {
            //TODO handle drawing text in VERTICAL orientation
            
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

    public String getName( )
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

    public float[] getTextColor( )
    {
        return textColor;
    }

    public void setTextColor( float[] textColor )
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

    public boolean isShowName( )
    {
        return showName;
    }

    public void setShowName( boolean showName )
    {
        this.showName = showName;
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

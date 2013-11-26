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

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.UUID;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.atlas.support.ImageData;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * <p>Paints the default visualization for provided {@code Event} objects. Paints
 * a box with configurable border and background color and an optional icon
 * and text description.</p>
 * 
 * <p>The appearance of a single Event can be customized via
 * {@code Event#setEventPainter(EventPainter)} and the default appearance of all
 * Events can be customized via {@code EventPlotInfo#setEventPainter(EventPainter)}.</p>
 * 
 * @author ulman
 */
public class DefaultEventPainter implements EventPainter
{
    public static final Object DEFAULT_ICON = UUID.randomUUID( );
    public static final int DEFAULT_ICON_SIZE = 64;
    public static final Color DEFAULT_ICON_COLOR = Color.BLACK;
    public static final int DEFAULT_NUM_ICONS_ROWS = 3;

    public static final int ARROW_TIP_BUFFER = 2;
    public static final int ARROW_SIZE = 10;
    public static final float[] DEFAULT_COLOR = GlimpseColor.getGray( );

    protected Object defaultIconId = DEFAULT_ICON;
    protected int maxIconRows = DEFAULT_NUM_ICONS_ROWS;

    /**
     * Sets the default icon which is used when no icon is set for an aggregate event.
     * 
     * @param id
     */
    public void setDefaultIconId( Object id )
    {
        this.defaultIconId = id;
    }

    public Object getDefaultIconId( )
    {
        return this.defaultIconId;
    }

    /**
     * Sets the maximum number of rows used to display icons in aggregate groups.
     * 
     * @param rows
     */
    public void setMaxIconRows( int rows )
    {
        this.maxIconRows = rows;
    }

    public int getMaxIconRows( )
    {
        return this.maxIconRows;
    }

    @Override
    public void paint( GL2 gl, Event event, Event nextEvent, EventPlotInfo info, GlimpseBounds bounds, int posMin, int posMax )
    {
        StackedTimePlot2D plot = info.getStackedTimePlot( );
        TaggedAxis1D axis = info.getCommonAxis( );

        int height = bounds.getHeight( );
        int width = bounds.getWidth( );

        int buffer = info.getEventPadding( );

        // the size of the event in pixels perpendicular to the time axis
        int sizePerpPixels = posMax - posMin;
        // the location of the event center perpendicular to the time axis
        double sizePerpCenter = posMin + sizePerpPixels / 2.0;
        int arrowSize = Math.min( sizePerpPixels, ARROW_SIZE );

        Epoch epoch = plot.getEpoch( );
        double timeMin = epoch.fromTimeStamp( event.getStartTime( ) );
        double timeMax = epoch.fromTimeStamp( event.getEndTime( ) );

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
        double remainingSpace = axis.getPixelsPerValue( ) * timeSpan - buffer * 2;

        int pixel = buffer + ( offEdgeMin ? arrowSize : 0 ) + Math.max( 0, axis.valueToScreenPixel( timeMin ) );

        // start positions of the next event in this row
        double nextStartValue = nextEvent != null ? epoch.fromTimeStamp( nextEvent.getStartTime( ) ) : axis.getMax( );
        int nextStartPixel = nextEvent != null ? axis.valueToScreenPixel( nextStartValue ) : width;

        EventSelectionHandler selectionHandler = info.getEventSelectionHandler( );
        boolean highlightSelected = selectionHandler.isHighlightSelectedEvents( );
        boolean isSelected = highlightSelected ? selectionHandler.isEventSelected( event ) : false;

        boolean horiz = plot.isTimeAxisHorizontal( );
        
        if ( !offEdgeMin && !offEdgeMax )
        {
            if ( event.isShowBackground( ) )
            {
                GlimpseColor.glColor( gl, event.getBackgroundColor( info, isSelected ) );
                gl.glBegin( GL2.GL_QUADS );
                try
                {
                    if ( horiz )
                    {
                        gl.glVertex2d( timeMin, posMin );
                        gl.glVertex2d( timeMin, posMax );
                        gl.glVertex2d( timeMax, posMax );
                        gl.glVertex2d( timeMax, posMin );
                    }
                    else
                    {
                        gl.glVertex2d( posMin, timeMin );
                        gl.glVertex2d( posMax, timeMin );
                        gl.glVertex2d( posMax, timeMax );
                        gl.glVertex2d( posMin, timeMax );
                    }
                }
                finally
                {
                    gl.glEnd( );
                }
            }

            if ( event.isShowBorder( ) )
            {
                GlimpseColor.glColor( gl, event.getBorderColor( info, isSelected ) );
                gl.glLineWidth( event.getBorderThickness( info, isSelected ) );
                gl.glBegin( GL2.GL_LINE_LOOP );
                try
                {
                    if ( horiz )
                    {
                        gl.glVertex2d( timeMin, posMin );
                        gl.glVertex2d( timeMin, posMax );
                        gl.glVertex2d( timeMax, posMax );
                        gl.glVertex2d( timeMax, posMin );
                    }
                    else
                    {
                        gl.glVertex2d( posMin, timeMin );
                        gl.glVertex2d( posMax, timeMin );
                        gl.glVertex2d( posMax, timeMax );
                        gl.glVertex2d( posMin, timeMax );
                    }
                }
                finally
                {
                    gl.glEnd( );
                }
            }
        }
        else
        {
            if ( event.isShowBackground( ) )
            {
                GlimpseColor.glColor( gl, event.getBackgroundColor( info, isSelected ) );
                gl.glBegin( GL2.GL_POLYGON );
                try
                {
                    if ( horiz )
                    {
                        gl.glVertex2d( arrowBaseMin, posMax );
                        gl.glVertex2d( arrowBaseMax, posMax );
                        gl.glVertex2d( timeMax, sizePerpCenter );
                        gl.glVertex2d( arrowBaseMax, posMin );
                        gl.glVertex2d( arrowBaseMin, posMin );
                        gl.glVertex2d( timeMin, sizePerpCenter );
                    }
                    else
                    {
                        gl.glVertex2d( posMax, arrowBaseMin );
                        gl.glVertex2d( posMax, arrowBaseMax );
                        gl.glVertex2d( sizePerpCenter, timeMax );
                        gl.glVertex2d( posMin, arrowBaseMax );
                        gl.glVertex2d( posMin, arrowBaseMin );
                        gl.glVertex2d( sizePerpCenter, timeMin );
                    }
                }
                finally
                {
                    gl.glEnd( );
                }
            }

            if ( event.isShowBorder( ) )
            {
                GlimpseColor.glColor( gl, event.getBorderColor( info, isSelected ) );
                gl.glLineWidth( event.getBorderThickness( info, isSelected ) );
                gl.glBegin( GL2.GL_LINE_LOOP );
                try
                {
                    if ( horiz )
                    {
                        gl.glVertex2d( arrowBaseMin, posMax );
                        gl.glVertex2d( arrowBaseMax, posMax );
                        gl.glVertex2d( timeMax, sizePerpCenter );
                        gl.glVertex2d( arrowBaseMax, posMin );
                        gl.glVertex2d( arrowBaseMin, posMin );
                        gl.glVertex2d( timeMin, sizePerpCenter );
                    }
                    else
                    {
                        gl.glVertex2d( posMax, arrowBaseMin );
                        gl.glVertex2d( posMax, arrowBaseMax );
                        gl.glVertex2d( sizePerpCenter, timeMax );
                        gl.glVertex2d( posMin, arrowBaseMax );
                        gl.glVertex2d( posMin, arrowBaseMin );
                        gl.glVertex2d( sizePerpCenter, timeMin );
                    }
                }
                finally
                {
                    gl.glEnd( );
                }
            }
        }
        
        int totalIconSizePerpPixels = getIconSizePerpPixels( event, info, sizePerpPixels );

        if ( event.hasChildren( ) )
        {
            final int numChildren = event.getEventCount( );
            final int numRows = maxIconRows;
            
            // the requested size of the icon in the direction perpendicular to the time axis
            int iconSizePerpPixels = totalIconSizePerpPixels / numRows;

            int columnsByAvailableSpace = ( int ) Math.floor( remainingSpace / ( double ) iconSizePerpPixels );
            int columnsByNumberOfIcons = ( int ) Math.ceil( numChildren / ( double ) numRows );
            int numColumns = ( int ) Math.min( columnsByAvailableSpace, columnsByNumberOfIcons );

            double iconSizePerpValue = iconSizePerpPixels / axis.getPixelsPerValue( );
            int totalIconWidthPixels = iconSizePerpPixels * numColumns;

            event.isIconVisible = event.isShowIcon( ) && !event.isIconOverlapping( totalIconWidthPixels, 0, remainingSpace, pixel, nextStartPixel );
            if ( event.isIconVisible )
            {
                double value = axis.screenPixelToValue( pixel );
                event.iconStartTime = epoch.toTimeStamp( value );
                event.iconEndTime = event.iconStartTime.add( totalIconWidthPixels / axis.getPixelsPerValue( ) );

                TextureAtlas atlas = info.getTextureAtlas( );
                atlas.beginRendering( );
                try
                {                    
                    Iterator<Event> iter = event.iterator( );

                    outer: for ( int c = 0; c < numColumns; c++ )
                    {
                        for ( int r = numRows - 1; r >= 0; r-- )
                        {
                            if ( iter.hasNext( ) )
                            {
                                Event child = iter.next( );
                                Object icon = child.getIconId( );
                                if ( icon == null )
                                {
                                    GlimpseColor.glColor( gl, child.getBackgroundColor( info, isSelected ), 0.5f );
                                    icon = defaultIconId;
                                }
                                else
                                {
                                    GlimpseColor.glColor( gl, GlimpseColor.getWhite( ) );
                                }
                                
                                ImageData iconData = atlas.getImageData( icon );
                                
                                // the size of the icon in the direction perpendicular to the time axis
                                int iconSizePerp = horiz ? iconData.getHeight( ) : iconData.getWidth( );
                                
                                double iconScale =  iconSizePerpPixels / ( double ) iconSizePerp;

                                double x = value + c * iconSizePerpValue;
                                double startY = sizePerpCenter - totalIconSizePerpPixels / 2.0;
                                double y = startY + r * iconSizePerpPixels;

                                if ( horiz )
                                {
                                    atlas.drawImageAxisX( gl, icon, axis, x, y, iconScale, iconScale, 0, iconSizePerp );
                                }
                                else
                                {
                                    atlas.drawImageAxisY( gl, icon, axis, y, x, iconScale, iconScale, 0, iconSizePerp );
                                }
                            }
                            else
                            {
                                break outer;
                            }
                        }
                    }
                }
                finally
                {
                    atlas.endRendering( );
                }

                remainingSpace -= totalIconWidthPixels + buffer;
                pixel += totalIconWidthPixels + buffer;
            }
        }
        else
        {
            //XXX there is currently no way for custom subclasses of EventPainter to properly
            //    set isIconVisible and isTextVisible. This isn't a huge problem, but will cause
            //    EventSelection callbacks to incorrectly indicate the visibility of icons or text
            event.isIconVisible = event.isShowIcon( ) && event.getIconId( ) != null && !event.isIconOverlapping( totalIconSizePerpPixels, buffer, remainingSpace, pixel, nextStartPixel );

            if ( event.isIconVisible )
            {
                TextureAtlas atlas = info.getTextureAtlas( );                
                atlas.beginRendering( );
                try
                {
                    ImageData iconData = atlas.getImageData( event.getIconId( ) );
                    
                    // the size of the icon in the direction perpendicular to the time axis
                    int iconSizePerp = horiz ? iconData.getHeight( ) : iconData.getWidth( );
                    
                    // the size of the icon in the direction parallel to the time axis
                    int iconSizeTime = horiz ? iconData.getWidth( ) : iconData.getHeight( );
                    
                    // the requested size of the icon in the direction perpendicular to the time axis
                    int iconSizePerpPixels = getIconSizePerpPixels( event, info, sizePerpPixels );
                    
                    double iconScale = iconSizePerpPixels / ( double ) iconSizePerp;
                    
                    // the axis value corresponding to the left side of the icon
                    double posTime = axis.screenPixelToValue( pixel );
                    event.iconStartTime = epoch.toTimeStamp( posTime );
                    // the size of the icon (parallel to the time axis) in axis units
                    double iconSizeTimeAxis = iconSizeTime / axis.getPixelsPerValue( );
                    event.iconEndTime = event.iconStartTime.add( iconSizeTimeAxis );
                    
                    // the scaled size of the icon parallel to the time axis in pixels
                    int iconSizeTimeScaledPixels = (int) ( iconSizeTime * iconScale );
                    
                    // the position of the bottom of the icon in pixels perpendicular to the time axis
                    double posPerp = sizePerpCenter - iconSizePerpPixels / 2.0;
                    
                    if ( horiz )
                    {
                        atlas.drawImageAxisX( gl, event.getIconId( ), axis, posTime, posPerp, iconScale, iconScale, 0, iconSizePerp );
                    }
                    else
                    {
                        atlas.drawImageAxisY( gl, event.getIconId( ), axis, posPerp, posTime, iconScale, iconScale, 0, iconSizePerp );
                    }
                    
                    remainingSpace -= iconSizeTimeScaledPixels + buffer;
                    pixel += iconSizeTimeScaledPixels + buffer;
                }
                finally
                {
                    atlas.endRendering( );
                }
            }
        }

        if ( event.isShowLabel( ) && event.getLabel( ) != null )
        {
            TextRenderer textRenderer = info.getTextRenderer( );
            Rectangle2D labelBounds = textRenderer.getBounds( event.getLabel( ) );

            boolean isTextOverfull = event.isTextOverfull( sizePerpPixels, buffer, remainingSpace, pixel, nextStartPixel, labelBounds );
            boolean isTextIntersecting = event.isTextIntersecting( sizePerpPixels, buffer, remainingSpace, pixel, nextStartPixel, labelBounds );
            boolean isTextOverlappingAndHidden = ( ( isTextOverfull || isTextIntersecting ) && event.getTextRenderingMode( ) == TextRenderingMode.HideAll );
            double availableSpace = event.getTextAvailableSpace( sizePerpPixels, buffer, remainingSpace, pixel, nextStartPixel );

            event.isTextVisible = !isTextOverlappingAndHidden;

            if ( event.isTextVisible )
            {
                Rectangle2D displayBounds = labelBounds;
                String displayText = event.getLabel( );

                if ( labelBounds.getWidth( ) > availableSpace && event.getTextRenderingMode( ) != TextRenderingMode.ShowAll )
                {
                    displayText = event.calculateDisplayText( textRenderer, displayText, availableSpace );
                    displayBounds = textRenderer.getBounds( displayText );
                }

                double value = axis.screenPixelToValue( pixel );
                event.textStartTime = epoch.toTimeStamp( value );
                event.textEndTime = event.textStartTime.add( displayBounds.getWidth( ) / axis.getPixelsPerValue( ) );

                // use this event's text color if it has been set
                if ( event.getLabelColor( ) != null )
                {
                    GlimpseColor.setColor( textRenderer, event.getLabelColor( ) );
                }
                // otherwise, use the default no background color if the background is not showing
                // and if a color has not been explicitly set for the EventPainter
                else if ( !info.isTextColorSet( ) && !event.isShowBackground( ) )
                {
                    GlimpseColor.setColor( textRenderer, info.getTextColorNoBackground( ) );
                }
                // otherwise use the EventPainter's default text color
                else
                {
                    GlimpseColor.setColor( textRenderer, info.getTextColor( ) );
                }

                if ( horiz )
                {
                    textRenderer.beginRendering( width, height );
                    try
                    {
                        // use the labelBounds for the height (if the text shortening removed a character which
                        // hangs below the line, we don't want the text position to move)
                        int pixelY = ( int ) ( sizePerpPixels / 2.0 - labelBounds.getHeight( ) * 0.3 + posMin );
                        textRenderer.draw( displayText, pixel, pixelY );
    
                        remainingSpace -= displayBounds.getWidth( ) + buffer;
                        pixel += displayBounds.getWidth( ) + buffer;
                    }
                    finally
                    {
                        textRenderer.endRendering( );
                    }
                }
                else
                {
                    textRenderer.beginRendering( width, height );
                    try
                    {
                        double shiftX = sizePerpPixels / 2.0 + posMin;
                        int pixelX = ( int ) shiftX;
                     
                        double shiftY = pixel;
                        int pixelY = ( int ) ( pixel - labelBounds.getHeight( ) * 0.34 );

                        gl.glMatrixMode( GL2.GL_PROJECTION );
                        
                        gl.glTranslated( shiftX, shiftY, 0 );
                        gl.glRotated( 90, 0, 0, 1.0f );
                        gl.glTranslated( -shiftX, -shiftY, 0 );
                        
                        textRenderer.draw( displayText, pixelX, pixelY );
    
                        remainingSpace -= displayBounds.getWidth( ) + buffer;
                        pixel += displayBounds.getWidth( ) + buffer;
                    }
                    finally
                    {
                        textRenderer.endRendering( );
                    }
                }
            }
        }
        else
        {
            event.isTextVisible = false;
        }
    }
    
    protected int getIconSizePerpPixels( Event event, EventPlotInfo info, int sizePerpPixels )
    {
        int iconSizePerpPixels;
        if ( !event.isUseDefaultIconSize( )  )
        {
            iconSizePerpPixels = event.getIconSize( );
        }
        else if ( !info.isUseDefaultIconSize( ) )
        {
            iconSizePerpPixels = info.getDefaultIconSize( );
        }
        else
        {
            iconSizePerpPixels = sizePerpPixels;
        }
        
        return iconSizePerpPixels;
    }
}

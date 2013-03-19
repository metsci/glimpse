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

import static com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode.*;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.UUID;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.atlas.support.ImageData;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.sun.opengl.util.j2d.TextRenderer;

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
    public void paint( GL gl, Event event, Event nextEvent, EventPlotInfo info, GlimpseBounds bounds, int posMin, int posMax )
    {
        StackedTimePlot2D plot = info.getStackedTimePlot( );
        TaggedAxis1D axis = info.getCommonAxis( );

        int height = bounds.getHeight( );
        int width = bounds.getWidth( );

        int buffer = info.getEventPadding( );

        int size = posMax - posMin;
        double sizeCenter = posMin + size / 2.0;
        int arrowSize = Math.min( size, ARROW_SIZE );

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
        double remainingSpaceX = axis.getPixelsPerValue( ) * timeSpan - buffer * 2;

        int pixelX = buffer + ( offEdgeMin ? arrowSize : 0 ) + Math.max( 0, axis.valueToScreenPixel( timeMin ) );

        // start positions of the next event in this row
        double nextStartValue = nextEvent != null ? epoch.fromTimeStamp( nextEvent.getStartTime( ) ) : axis.getMax( );
        int nextStartPixel = nextEvent != null ? axis.valueToScreenPixel( nextStartValue ) : width;

        EventSelectionHandler selectionHandler = info.getEventSelectionHandler( );
        boolean highlightSelected = selectionHandler.isHighlightSelectedEvents( );
        boolean isSelected = highlightSelected ? selectionHandler.isEventSelected( event ) : false;

        if ( plot.isTimeAxisHorizontal( ) )
        {
            if ( !offEdgeMin && !offEdgeMax )
            {
                if ( event.isShowBackground( ) )
                {
                    GlimpseColor.glColor( gl, event.getBackgroundColor( info, isSelected ) );
                    gl.glBegin( GL.GL_QUADS );
                    try
                    {
                        gl.glVertex2d( timeMin, posMin );
                        gl.glVertex2d( timeMin, posMax );
                        gl.glVertex2d( timeMax, posMax );
                        gl.glVertex2d( timeMax, posMin );
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
                    gl.glBegin( GL.GL_LINE_LOOP );
                    try
                    {
                        gl.glVertex2d( timeMin, posMin );
                        gl.glVertex2d( timeMin, posMax );
                        gl.glVertex2d( timeMax, posMax );
                        gl.glVertex2d( timeMax, posMin );
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
                    gl.glBegin( GL.GL_POLYGON );
                    try
                    {
                        gl.glVertex2d( arrowBaseMin, posMax );
                        gl.glVertex2d( arrowBaseMax, posMax );
                        gl.glVertex2d( timeMax, sizeCenter );
                        gl.glVertex2d( arrowBaseMax, posMin );
                        gl.glVertex2d( arrowBaseMin, posMin );
                        gl.glVertex2d( timeMin, sizeCenter );
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
                    gl.glBegin( GL.GL_LINE_LOOP );
                    try
                    {
                        gl.glVertex2d( arrowBaseMin, posMax );
                        gl.glVertex2d( arrowBaseMax, posMax );
                        gl.glVertex2d( timeMax, sizeCenter );
                        gl.glVertex2d( arrowBaseMax, posMin );
                        gl.glVertex2d( arrowBaseMin, posMin );
                        gl.glVertex2d( timeMin, sizeCenter );
                    }
                    finally
                    {
                        gl.glEnd( );
                    }
                }
            }

            if ( event.hasChildren( ) )
            {
                final int numChildren = event.getEventCount( );
                final int numRows = maxIconRows;
                int iconSizePixels = size / numRows;

                int columnsByAvailableSpace = ( int ) Math.floor( remainingSpaceX / ( double ) iconSizePixels );
                int columnsByNumberOfIcons = ( int ) Math.ceil( numChildren / ( double ) numRows );
                int numColumns = ( int ) Math.min( columnsByAvailableSpace, columnsByNumberOfIcons );

                double iconSizeValue = iconSizePixels / axis.getPixelsPerValue( );
                int totalIconWidthPixels = iconSizePixels * numColumns;

                event.isIconVisible = event.isShowIcon( ) && !event.isIconOverlapping( totalIconWidthPixels, 0, remainingSpaceX, pixelX, nextStartPixel );
                if ( event.isIconVisible )
                {
                    double valueX = axis.screenPixelToValue( pixelX );
                    event.iconStartTime = epoch.toTimeStamp( valueX );
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
                                    double iconScale = iconSizePixels / ( double ) iconData.getHeight( );

                                    double x = valueX + c * iconSizeValue;
                                    double y = posMin + r * iconSizePixels;

                                    atlas.drawImageAxisX( gl, icon, axis, x, y, iconScale, iconScale, 0, iconData.getHeight( ) );
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

                    remainingSpaceX -= totalIconWidthPixels + buffer;
                    pixelX += totalIconWidthPixels + buffer;
                }
            }
            else
            {
                //XXX there is currently no way for custom subclasses of EventPainter to properly
                //    set isIconVisible and isTextVisible. This isn't a huge problem, but will cause
                //    EventSelection callbacks to incorrectly indicate the visibility of icons or text
                event.isIconVisible = event.isShowIcon( ) && event.getIconId( ) != null && !event.isIconOverlapping( size, buffer, remainingSpaceX, pixelX, nextStartPixel );

                if ( event.isIconVisible )
                {
                    double valueX = axis.screenPixelToValue( pixelX );
                    event.iconStartTime = epoch.toTimeStamp( valueX );
                    event.iconEndTime = event.iconStartTime.add( size / axis.getPixelsPerValue( ) );

                    TextureAtlas atlas = info.getTextureAtlas( );
                    atlas.beginRendering( );
                    try
                    {
                        ImageData iconData = atlas.getImageData( event.getIconId( ) );
                        double iconScale = size / ( double ) iconData.getHeight( );

                        atlas.drawImageAxisX( gl, event.getIconId( ), axis, valueX, posMin, iconScale, iconScale, 0, iconData.getHeight( ) );
                    }
                    finally
                    {
                        atlas.endRendering( );
                    }

                    remainingSpaceX -= size + buffer;
                    pixelX += size + buffer;
                }
            }

            if ( event.isShowLabel( ) )
            {
                TextRenderer textRenderer = info.getTextRenderer( );
                Rectangle2D labelBounds = textRenderer.getBounds( event.getLabel( ) );

                boolean isTextOverfull = event.isTextOverfull( size, buffer, remainingSpaceX, pixelX, nextStartPixel, labelBounds );
                boolean isTextIntersecting = event.isTextIntersecting( size, buffer, remainingSpaceX, pixelX, nextStartPixel, labelBounds );
                boolean isTextOverlappingAndHidden = ( ( isTextOverfull || isTextIntersecting ) && event.getTextRenderingMode( ) == HideAll );
                double availableSpace = event.getTextAvailableSpace( size, buffer, remainingSpaceX, pixelX, nextStartPixel );

                event.isTextVisible = !isTextOverlappingAndHidden;

                if ( event.isTextVisible )
                {
                    Rectangle2D displayBounds = labelBounds;
                    String displayText = event.getLabel( );

                    if ( labelBounds.getWidth( ) > availableSpace && event.getTextRenderingMode( ) != ShowAll )
                    {
                        displayText = event.calculateDisplayText( textRenderer, displayText, availableSpace );
                        displayBounds = textRenderer.getBounds( displayText );
                    }

                    double valueX = axis.screenPixelToValue( pixelX );
                    event.textStartTime = epoch.toTimeStamp( valueX );
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

                    textRenderer.beginRendering( width, height );
                    try
                    {
                        // use the labelBounds for the height (if the text shortening removed a character which
                        // hangs below the line, we don't want the text position to move)
                        int pixelY = ( int ) ( size / 2.0 - labelBounds.getHeight( ) * 0.3 + posMin );
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
                event.isTextVisible = false;
            }
        }
        else
        {
            //TODO handle drawing text and icons in HORIZONTAL orientation

            GlimpseColor.glColor( gl, event.getBackgroundColor( info, isSelected ) );
            gl.glBegin( GL.GL_QUADS );
            try
            {
                gl.glVertex2d( posMin, timeMin );
                gl.glVertex2d( posMax, timeMin );
                gl.glVertex2d( posMax, timeMax );
                gl.glVertex2d( posMin, timeMax );
            }
            finally
            {
                gl.glEnd( );
            }

            GlimpseColor.glColor( gl, event.getBorderColor( info, isSelected ) );
            gl.glLineWidth( event.getBorderThickness( info, isSelected ) );
            gl.glBegin( GL.GL_LINE_LOOP );
            try
            {
                gl.glVertex2d( posMin, timeMin );
                gl.glVertex2d( posMax, timeMin );
                gl.glVertex2d( posMax, timeMax );
                gl.glVertex2d( posMin, timeMax );
            }
            finally
            {
                gl.glEnd( );
            }
        }
    }
}

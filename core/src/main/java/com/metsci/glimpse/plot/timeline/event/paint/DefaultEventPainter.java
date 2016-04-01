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
package com.metsci.glimpse.plot.timeline.event.paint;

import static com.metsci.glimpse.plot.timeline.event.Event.OverlapRenderingMode.Intersecting;
import static com.metsci.glimpse.plot.timeline.event.Event.OverlapRenderingMode.Overfull;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.event.Event;
import com.metsci.glimpse.plot.timeline.event.Event.OverlapRenderingMode;
import com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode;
import com.metsci.glimpse.plot.timeline.event.EventBounds;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.event.listener.EventSelectionHandler;
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
    public static final int DEFAULT_ICON_SIZE = 64;
    public static final Color DEFAULT_ICON_COLOR = Color.BLACK;
    public static final int DEFAULT_NUM_ICONS_ROWS = 3;

    public static final int ARROW_TIP_BUFFER = 2;
    public static final int ARROW_SIZE = 10;
    public static final float[] DEFAULT_COLOR = GlimpseColor.getGray( );

    protected int maxIconRows = DEFAULT_NUM_ICONS_ROWS;

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
    public void paint( GL2 gl, Event event, Event nextEvent, EventPlotInfo info, GlimpseBounds bounds, Axis1D timeAxis, int posMin, int posMax )
    {
        StackedTimePlot2D plot = info.getStackedTimePlot( );

        int height = bounds.getHeight( );
        int width = bounds.getWidth( );

        Orientation orient = plot.getOrientation( );
        int size = orient == Orientation.HORIZONTAL ? height : width;

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
        if ( timeAxis.getMin( ) > timeMin )
        {
            offEdgeMin = true;
            timeMin = timeAxis.getMin( ) + ARROW_TIP_BUFFER / timeAxis.getPixelsPerValue( );
            arrowBaseMin = timeMin + arrowSize / timeAxis.getPixelsPerValue( );
        }

        double arrowBaseMax = timeMax;
        boolean offEdgeMax = false;
        if ( timeAxis.getMax( ) < timeMax )
        {
            offEdgeMax = true;
            timeMax = timeAxis.getMax( ) - ARROW_TIP_BUFFER / timeAxis.getPixelsPerValue( );
            arrowBaseMax = timeMax - arrowSize / timeAxis.getPixelsPerValue( );
        }

        arrowBaseMax = Math.max( timeMin, arrowBaseMax );
        arrowBaseMin = Math.min( timeMax, arrowBaseMin );

        double timeSpan = arrowBaseMax - arrowBaseMin;
        double remainingSpace = timeAxis.getPixelsPerValue( ) * timeSpan - buffer * 2;

        int pixel = buffer + ( offEdgeMin ? arrowSize : 0 ) + Math.max( 0, timeAxis.valueToScreenPixel( timeMin ) );

        // start positions of the next event in this row
        double nextStartValue = nextEvent != null ? epoch.fromTimeStamp( nextEvent.getStartTime( ) ) : timeAxis.getMax( );
        int nextStartPixel = nextEvent != null ? timeAxis.valueToScreenPixel( nextStartValue ) : size;

        EventSelectionHandler selectionHandler = info.getEventSelectionHandler( );
        boolean highlightSelected = selectionHandler.isHighlightSelectedEvents( );
        boolean isSelected = highlightSelected ? selectionHandler.isEventSelected( event ) : false;

        boolean horiz = plot.isTimeAxisHorizontal( );

        EventBounds eventBounds = info.getEventBounds( event.getId( ) );

        if ( !offEdgeMin && !offEdgeMax )
        {
            if ( event.isShowBackground( ) )
            {
                GlimpseColor.glColor( gl, getBackgroundColor( event, info, isSelected ) );
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
                GlimpseColor.glColor( gl, getBorderColor( event, info, isSelected ) );
                gl.glLineWidth( getBorderThickness( event, info, isSelected ) );
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
                GlimpseColor.glColor( gl, getBackgroundColor( event, info, isSelected ) );
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
                GlimpseColor.glColor( gl, getBorderColor( event, info, isSelected ) );
                gl.glLineWidth( getBorderThickness( event, info, isSelected ) );
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

        Object defaultIconId = info.getDefaultIconId( );

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

            double iconSizePerpValue = iconSizePerpPixels / timeAxis.getPixelsPerValue( );
            int totalIconWidthPixels = iconSizePerpPixels * numColumns;

            eventBounds.setIconVisible( event.isShowIcon( ) && !isIconOverlapping( totalIconWidthPixels, 0, remainingSpace, pixel, nextStartPixel, event.getOverlapRenderingMode( ) ) );
            if ( eventBounds.isIconVisible( ) )
            {
                double value = timeAxis.screenPixelToValue( pixel );
                eventBounds.setIconStartTime( epoch.toTimeStamp( value ) );
                eventBounds.setIconEndTime( eventBounds.getIconStartTime( ).add( totalIconWidthPixels / timeAxis.getPixelsPerValue( ) ) );

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
                                if ( icon == null || !atlas.isImageLoaded( icon ) )
                                {
                                    GlimpseColor.glColor( gl, getBackgroundColor( child, info, isSelected ), 0.5f );
                                    icon = defaultIconId;
                                }
                                else
                                {
                                    GlimpseColor.glColor( gl, GlimpseColor.getWhite( ) );
                                }

                                ImageData iconData = atlas.getImageData( icon );

                                // the size of the icon in the direction perpendicular to the time axis
                                int iconSizePerp = horiz ? iconData.getHeight( ) : iconData.getWidth( );

                                double iconScale = iconSizePerpPixels / ( double ) iconSizePerp;

                                double x = value + c * iconSizePerpValue;
                                double startY = sizePerpCenter - totalIconSizePerpPixels / 2.0;
                                double y = startY + r * iconSizePerpPixels;

                                if ( horiz )
                                {
                                    atlas.drawImageAxisX( gl, icon, timeAxis, x, y, iconScale, iconScale, 0, iconSizePerp );
                                }
                                else
                                {
                                    atlas.drawImageAxisY( gl, icon, timeAxis, y, x, iconScale, iconScale, 0, iconSizePerp );
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
            boolean isOverlapping = isIconOverlapping( totalIconSizePerpPixels, buffer, remainingSpace, pixel, nextStartPixel, event.getOverlapRenderingMode( ) );
            eventBounds.setIconVisible( event.isShowIcon( ) && event.getIconId( ) != null && !isOverlapping );

            if ( eventBounds.isIconVisible( ) )
            {
                TextureAtlas atlas = info.getTextureAtlas( );
                atlas.beginRendering( );
                try
                {
                    Object icon = event.getIconId( );
                    if ( icon == null || !atlas.isImageLoaded( icon ) )
                    {
                        icon = defaultIconId;
                    }

                    ImageData iconData = atlas.getImageData( icon );

                    // the size of the icon in the direction perpendicular to the time axis
                    int iconSizePerp = horiz ? iconData.getHeight( ) : iconData.getWidth( );

                    // the size of the icon in the direction parallel to the time axis
                    int iconSizeTime = horiz ? iconData.getWidth( ) : iconData.getHeight( );

                    // the requested size of the icon in the direction perpendicular to the time axis
                    int iconSizePerpPixels = getIconSizePerpPixels( event, info, sizePerpPixels );

                    double iconScale = iconSizePerpPixels / ( double ) iconSizePerp;

                    // the axis value corresponding to the left side of the icon
                    double posTime = timeAxis.screenPixelToValue( pixel );
                    eventBounds.setIconStartTime( epoch.toTimeStamp( posTime ) );
                    // the size of the icon (parallel to the time axis) in axis units
                    double iconSizeTimeAxis = iconSizeTime / timeAxis.getPixelsPerValue( );
                    eventBounds.setIconEndTime( eventBounds.getIconStartTime( ).add( iconSizeTimeAxis ) );

                    // the scaled size of the icon parallel to the time axis in pixels
                    int iconSizeTimeScaledPixels = ( int ) ( iconSizeTime * iconScale );

                    // the position of the bottom of the icon in pixels perpendicular to the time axis
                    double posPerp = sizePerpCenter - iconSizePerpPixels / 2.0;

                    if ( horiz )
                    {
                        atlas.drawImageAxisX( gl, icon, timeAxis, posTime, posPerp, iconScale, iconScale, 0, iconSizePerp );
                    }
                    else
                    {
                        atlas.drawImageAxisY( gl, icon, timeAxis, posPerp, posTime, iconScale, iconScale, 0, iconSizePerp );
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

            boolean isTextOverfull = isTextOverfull( sizePerpPixels, buffer, remainingSpace, pixel, nextStartPixel, labelBounds, event.getOverlapRenderingMode( ) );
            boolean isTextIntersecting = isTextIntersecting( sizePerpPixels, buffer, remainingSpace, pixel, nextStartPixel, labelBounds, event.getOverlapRenderingMode( ) );
            boolean isTextOverlappingAndHidden = ( ( isTextOverfull || isTextIntersecting ) && event.getTextRenderingMode( ) == TextRenderingMode.HideAll );
            double availableSpace = getTextAvailableSpace( sizePerpPixels, buffer, remainingSpace, pixel, nextStartPixel, event.getOverlapRenderingMode( ) );

            eventBounds.setTextVisible( !isTextOverlappingAndHidden );

            if ( eventBounds.isTextVisible( ) )
            {
                Rectangle2D displayBounds = labelBounds;
                String displayText = event.getLabel( );

                if ( labelBounds.getWidth( ) > availableSpace && event.getTextRenderingMode( ) != TextRenderingMode.ShowAll )
                {
                    displayText = calculateDisplayText( textRenderer, displayText, availableSpace );
                    displayBounds = textRenderer.getBounds( displayText );
                }

                double value = timeAxis.screenPixelToValue( pixel );
                eventBounds.setTextStartTime( epoch.toTimeStamp( value ) );
                eventBounds.setTextEndTime( eventBounds.getTextStartTime( ).add( displayBounds.getWidth( ) / timeAxis.getPixelsPerValue( ) ) );

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
            eventBounds.setTextVisible( false );
        }
    }

    public static int getIconSizePerpPixels( Event event, EventPlotInfo info, int sizePerpPixels )
    {
        int iconSizePerpPixels;
        if ( !event.isUseDefaultIconSize( ) )
        {
            iconSizePerpPixels = event.getIconSize( );
        }
        else if ( info.isUseDefaultIconSize( ) )
        {
            iconSizePerpPixels = info.getDefaultIconSize( );
        }
        else
        {
            iconSizePerpPixels = sizePerpPixels;
        }

        return iconSizePerpPixels;
    }

    public static boolean isTextOverfull( int size, int buffer, double remainingSpaceX, int pixelX, int nextStartPixel, Rectangle2D bounds, OverlapRenderingMode mode )
    {
        return bounds.getWidth( ) + buffer > remainingSpaceX && mode == Overfull;
    }

    public static boolean isTextIntersecting( int size, int buffer, double remainingSpaceX, int pixelX, int nextStartPixel, Rectangle2D bounds, OverlapRenderingMode mode )
    {
        return pixelX + bounds.getWidth( ) + buffer > nextStartPixel && mode == Intersecting;
    }

    public static boolean isIconOverlapping( int size, int buffer, double remainingSpaceX, int pixelX, int nextStartPixel, OverlapRenderingMode mode )
    {
        return ( size + buffer > remainingSpaceX && mode == Overfull ) || ( pixelX + size + buffer > nextStartPixel && mode == Intersecting );
    }

    public static double getTextAvailableSpace( int size, int buffer, double remainingSpaceX, int pixelX, int nextStartPixel, OverlapRenderingMode mode )
    {
        double insideBoxSpace = remainingSpaceX - buffer;
        double outsideBoxSpace = nextStartPixel - pixelX - buffer;

        switch ( mode )
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

    public static String calculateDisplayText( TextRenderer textRenderer, String fullText, double availableSpace )
    {
        for ( int endIndex = fullText.length( ); endIndex >= 0; endIndex-- )
        {
            String subText = fullText.substring( 0, endIndex ) + "...";
            Rectangle2D bounds = textRenderer.getBounds( subText );
            if ( bounds.getWidth( ) < availableSpace ) return subText;
        }

        return "";
    }

    public static float[] getBackgroundColor( Event event, EventPlotInfo info, boolean isSelected )
    {
        float[] backgroundColor = event.getBackgroundColor( );
        float[] defaultColor = info.getDefaultEventBackgroundColor( );
        float[] selectedColor = info.getEventSelectionHandler( ).getSelectedEventBackgroundColor( );

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

    public static float[] getBorderColor( Event event, EventPlotInfo info, boolean isSelected )
    {
        float[] borderColor = event.getBorderColor( );
        float[] defaultColor = info.getDefaultEventBorderColor( );
        float[] selectedColor = info.getEventSelectionHandler( ).getSelectedEventBorderColor( );

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

    public static float getBorderThickness( Event event, EventPlotInfo info, boolean isSelected )
    {
        float borderThickness = event.getBorderThickness( );

        if ( isSelected )
        {
            return info.getEventSelectionHandler( ).getSelectedEventBorderThickness( );
        }
        else
        {
            return borderThickness;
        }
    }
}

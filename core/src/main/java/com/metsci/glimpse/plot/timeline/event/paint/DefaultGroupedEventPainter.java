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

import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.ARROW_SIZE;
import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.ARROW_TIP_BUFFER;
import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.DEFAULT_NUM_ICONS_ROWS;
import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.calculateDisplayText;
import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.getBackgroundColor;
import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.getBorderColor;
import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.getIconSizePerpPixels;
import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.getTextAvailableSpace;
import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.isIconOverlapping;
import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.isTextIntersecting;
import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.isTextOverfull;

import java.awt.geom.Rectangle2D;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.google.common.collect.Lists;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.event.Event;
import com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode;
import com.metsci.glimpse.plot.timeline.event.EventBounds;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.event.listener.EventSelectionHandler;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.atlas.support.ImageData;
import com.metsci.glimpse.support.color.GlimpseColor;

public class DefaultGroupedEventPainter implements GroupedEventPainter
{
    protected int maxIconRows = DEFAULT_NUM_ICONS_ROWS;
    protected int minimumTextDisplayWidth = 20;

    public DefaultGroupedEventPainter( )
    {

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

    /**
     * If the width of an event is less than this value, text is never displayed.
     * This provides an optimization when lots of events are on screen since it
     * is expensive to calculate the width of a text string.
     */
    public void setMinimumTextDisplayWidth( int pixels )
    {
        this.minimumTextDisplayWidth = pixels;
    }

    public int getMinimumTextDisplayWidth( )
    {
        return this.minimumTextDisplayWidth;
    }

    @Override
    public void paint( final GL2 gl, final EventPlotInfo info, final GlimpseBounds bounds, final Axis1D timeAxis, final Collection<EventDrawInfo> events )
    {
        final StackedTimePlot2D plot = info.getStackedTimePlot( );

        final int height = bounds.getHeight( );
        final int width = bounds.getWidth( );

        final Orientation orient = plot.getOrientation( );
        final int size = orient == Orientation.HORIZONTAL ? height : width;

        final int buffer = info.getEventPadding( );

        List<IconDrawInfo> iconDrawList = Lists.newArrayList( );
        List<TextDrawInfo> textDrawList = Lists.newArrayList( );

        // we may need up to 6 vertices per event (although some may need 4 and some may not be displayed at all
        FloatBuffer fillBuffer = Buffers.newDirectFloatBuffer( events.size( ) * 6 * 2 );
        FloatBuffer borderBuffer = Buffers.newDirectFloatBuffer( events.size( ) * 6 * 2 );
        FloatBuffer fillColorBuffer = Buffers.newDirectFloatBuffer( events.size( ) * 6 * 4 );
        FloatBuffer borderColorBuffer = Buffers.newDirectFloatBuffer( events.size( ) * 6 * 4 );
        IntBuffer fillCounts = Buffers.newDirectIntBuffer( events.size( ) );
        IntBuffer borderCounts = Buffers.newDirectIntBuffer( events.size( ) );
        IntBuffer fillIndices = Buffers.newDirectIntBuffer( events.size( ) );
        IntBuffer borderIndices = Buffers.newDirectIntBuffer( events.size( ) );

        int fillIndex = 0;
        int borderIndex = 0;

        int fillCount = 0;
        int borderCount = 0;

        Object defaultIconId = info.getDefaultIconId( );
        TextureAtlas atlas = info.getTextureAtlas( );
        TextRenderer textRenderer = info.getTextRenderer( );
        boolean horiz = plot.isTimeAxisHorizontal( );

        info.getEventManager( ).lock( );
        try
        {
            for ( EventDrawInfo eventInfo : events )
            {
                int posMax = eventInfo.posMax;
                int posMin = eventInfo.posMin;
                Event event = eventInfo.event;
                Event nextEvent = eventInfo.nextEvent;

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

                EventBounds eventBounds = info.getEventBounds( event.getId( ) );

                if ( !offEdgeMin && !offEdgeMax )
                {
                    if ( event.isShowBackground( ) )
                    {
                        float[] color = getBackgroundColor( event, info, isSelected );
                        fillIndex = addVerticesBox( fillCounts, fillIndices, fillIndex, fillBuffer, fillColorBuffer, horiz, color, ( float ) timeMin, ( float ) timeMax, ( float ) posMin, ( float ) posMax );
                        fillCount++;
                    }

                    if ( event.isShowBorder( ) )
                    {
                        float[] color = getBorderColor( event, info, isSelected );
                        borderIndex = addVerticesBox( borderCounts, borderIndices, borderIndex, borderBuffer, borderColorBuffer, horiz, color, ( float ) timeMin, ( float ) timeMax, ( float ) posMin, ( float ) posMax );
                        borderCount++;
                    }
                }
                else
                {
                    if ( event.isShowBackground( ) )
                    {
                        float[] color = getBackgroundColor( event, info, isSelected );
                        fillIndex = addVerticesArrow( fillCounts, fillIndices, fillIndex, fillBuffer, fillColorBuffer, horiz, color, ( float ) timeMin, ( float ) timeMax, ( float ) posMin, ( float ) posMax, ( float ) arrowBaseMin, ( float ) arrowBaseMax, ( float ) sizePerpCenter );
                        fillCount++;
                    }

                    if ( event.isShowBorder( ) )
                    {
                        float[] color = getBorderColor( event, info, isSelected );
                        borderIndex = addVerticesArrow( borderCounts, borderIndices, borderIndex, borderBuffer, borderColorBuffer, horiz, color, ( float ) timeMin, ( float ) timeMax, ( float ) posMin, ( float ) posMax, ( float ) arrowBaseMin, ( float ) arrowBaseMax, ( float ) sizePerpCenter );
                        borderCount++;
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

                    double iconSizePerpValue = iconSizePerpPixels / timeAxis.getPixelsPerValue( );
                    int totalIconWidthPixels = iconSizePerpPixels * numColumns;

                    eventBounds.setIconVisible( event.isShowIcon( ) && !isIconOverlapping( totalIconWidthPixels, 0, remainingSpace, pixel, nextStartPixel, event.getOverlapRenderingMode( ) ) );
                    if ( eventBounds.isIconVisible( ) )
                    {
                        double value = timeAxis.screenPixelToValue( pixel );
                        eventBounds.setIconStartTime( epoch.toTimeStamp( value ) );
                        eventBounds.setIconEndTime( eventBounds.getIconStartTime( ).add( totalIconWidthPixels / timeAxis.getPixelsPerValue( ) ) );

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

                                    if ( atlas.isImageLoaded( icon ) )
                                    {
                                        ImageData iconData = atlas.getImageData( icon );

                                        // the size of the icon in the direction perpendicular to the time axis
                                        int iconSizePerp = horiz ? iconData.getHeight( ) : iconData.getWidth( );

                                        double iconScale = iconSizePerpPixels / ( double ) iconSizePerp;

                                        double x = value + c * iconSizePerpValue;
                                        double startY = sizePerpCenter - totalIconSizePerpPixels / 2.0;
                                        double y = startY + r * iconSizePerpPixels;

                                        if ( horiz )
                                        {
                                            iconDrawList.add( new IconDrawInfo( icon, x, y, iconScale, iconScale, 0, iconSizePerp, true ) );
                                        }
                                        else
                                        {
                                            iconDrawList.add( new IconDrawInfo( icon, x, y, iconScale, iconScale, 0, iconSizePerp, false ) );
                                        }
                                    }
                                }
                                else
                                {
                                    break outer;
                                }
                            }
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
                        Object icon = event.getIconId( );
                        if ( icon == null || !atlas.isImageLoaded( icon ) )
                        {
                            icon = defaultIconId;
                        }

                        if ( atlas.isImageLoaded( icon ) )
                        {
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
                                iconDrawList.add( new IconDrawInfo( icon, posTime, posPerp, iconScale, iconScale, 0, iconSizePerp, true ) );
                            }
                            else
                            {
                                iconDrawList.add( new IconDrawInfo( icon, posPerp, posTime, iconScale, iconScale, 0, iconSizePerp, false ) );
                            }

                            remainingSpace -= iconSizeTimeScaledPixels + buffer;
                            pixel += iconSizeTimeScaledPixels + buffer;
                        }
                    }
                }

                boolean isBoxTooSmallForText = remainingSpace < this.minimumTextDisplayWidth;

                if ( event.isShowLabel( ) && event.getLabel( ) != null && !isBoxTooSmallForText )
                {
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

                        float[] color;

                        // use this event's text color if it has been set
                        if ( event.getLabelColor( ) != null )
                        {
                            color = event.getLabelColor( );
                        }
                        // otherwise, use the default no background color if the background is not showing
                        // and if a color has not been explicitly set for the EventPainter
                        else if ( !info.isTextColorSet( ) && !event.isShowBackground( ) )
                        {
                            color = info.getTextColorNoBackground( );
                        }
                        // otherwise use the EventPainter's default text color
                        else
                        {
                            color = info.getTextColor( );
                        }

                        if ( horiz )
                        {

                            // use the labelBounds for the height (if the text shortening removed a character which
                            // hangs below the line, we don't want the text position to move)
                            int pixelY = ( int ) ( sizePerpPixels / 2.0 - labelBounds.getHeight( ) * 0.3 + posMin );
                            textDrawList.add( new TextDrawInfo( displayText, color, pixel, pixelY, 0, 0 ) );

                            remainingSpace -= displayBounds.getWidth( ) + buffer;
                            pixel += displayBounds.getWidth( ) + buffer;
                        }
                        else
                        {
                            double shiftX = sizePerpPixels / 2.0 + posMin;
                            int pixelX = ( int ) shiftX;

                            double shiftY = pixel;
                            int pixelY = ( int ) ( pixel - labelBounds.getHeight( ) * 0.34 );

                            textDrawList.add( new TextDrawInfo( displayText, color, pixelX, pixelY, shiftX, shiftY ) );

                            remainingSpace -= displayBounds.getWidth( ) + buffer;
                            pixel += displayBounds.getWidth( ) + buffer;
                        }
                    }
                }
                else
                {
                    eventBounds.setTextVisible( false );
                }
            }

            if ( fillCount > 0 )
            {
                gl.glEnableClientState( GL2.GL_COLOR_ARRAY );
                gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );

                fillBuffer.flip( );
                fillColorBuffer.flip( );
                fillIndices.flip( );
                fillCounts.flip( );

                gl.glVertexPointer( 2, GL.GL_FLOAT, 0, fillBuffer );
                gl.glColorPointer( 4, GL.GL_FLOAT, 0, fillColorBuffer );
                gl.glMultiDrawArrays( GL2.GL_POLYGON, fillIndices, fillCounts, fillCount );

                gl.glDisableClientState( GL2.GL_COLOR_ARRAY );
                gl.glDisableClientState( GL2.GL_VERTEX_ARRAY );
            }

            if ( borderCount > 0 )
            {
                gl.glEnableClientState( GL2.GL_COLOR_ARRAY );
                gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );

                gl.glLineWidth( info.getDefaultEventBorderThickness( ) );

                borderBuffer.flip( );
                borderColorBuffer.flip( );
                borderIndices.flip( );
                borderCounts.flip( );

                gl.glVertexPointer( 2, GL.GL_FLOAT, 0, borderBuffer );
                gl.glColorPointer( 4, GL.GL_FLOAT, 0, borderColorBuffer );
                gl.glMultiDrawArrays( GL2.GL_LINE_LOOP, borderIndices, borderCounts, borderCount );

                gl.glDisableClientState( GL2.GL_COLOR_ARRAY );
                gl.glDisableClientState( GL2.GL_VERTEX_ARRAY );
            }

            if ( !iconDrawList.isEmpty( ) )
            {
                atlas.beginRendering( );
                try
                {
                    for ( IconDrawInfo iconInfo : iconDrawList )
                    {
                        if ( iconInfo.isX( ) )
                        {
                            atlas.drawImageAxisX( gl, iconInfo.id, timeAxis, iconInfo.positionX, iconInfo.positionY, iconInfo.scaleX, iconInfo.scaleY, iconInfo.centerX, iconInfo.centerY );
                        }
                        else
                        {
                            atlas.drawImageAxisY( gl, iconInfo.id, timeAxis, iconInfo.positionX, iconInfo.positionY, iconInfo.scaleX, iconInfo.scaleY, iconInfo.centerX, iconInfo.centerY );
                        }
                    }
                }
                finally
                {
                    atlas.endRendering( );
                }
            }

            if ( !textDrawList.isEmpty( ) )
            {
                textRenderer.beginRendering( width, height );
                try
                {
                    for ( TextDrawInfo textInfo : textDrawList )
                    {

                        if ( !horiz )
                        {
                            gl.glMatrixMode( GL2.GL_PROJECTION );

                            gl.glTranslated( textInfo.getShiftX( ), textInfo.getShiftY( ), 0 );
                            gl.glRotated( 90, 0, 0, 1.0f );
                            gl.glTranslated( -textInfo.getShiftX( ), -textInfo.getShiftY( ), 0 );
                        }

                        GlimpseColor.setColor( textRenderer, textInfo.getColor( ) );
                        textRenderer.draw( textInfo.getText( ), textInfo.getX( ), textInfo.getY( ) );
                    }
                }
                finally
                {
                    textRenderer.endRendering( );
                }
            }
        }
        finally
        {
            info.getEventManager( ).unlock( );
        }
    }

    protected int addVerticesBox( IntBuffer countBuffer, IntBuffer countIndices, int countIndex, FloatBuffer vertBuffer, FloatBuffer colorBuffer, boolean horiz, float[] color, float timeMin, float timeMax, float posMin, float posMax )
    {
        // all four vertices have the same color
        colorBuffer.put( color );
        colorBuffer.put( color );
        colorBuffer.put( color );
        colorBuffer.put( color );

        if ( horiz )
        {
            vertBuffer.put( timeMin );
            vertBuffer.put( posMin );

            vertBuffer.put( timeMin );
            vertBuffer.put( posMax );

            vertBuffer.put( timeMax );
            vertBuffer.put( posMax );

            vertBuffer.put( timeMax );
            vertBuffer.put( posMin );
        }
        else
        {
            vertBuffer.put( posMin );
            vertBuffer.put( timeMin );

            vertBuffer.put( posMax );
            vertBuffer.put( timeMin );

            vertBuffer.put( posMax );
            vertBuffer.put( timeMax );

            vertBuffer.put( posMin );
            vertBuffer.put( timeMax );
        }

        countBuffer.put( 4 );
        countIndices.put( countIndex );
        return countIndex + 4;
    }

    protected int addVerticesArrow( IntBuffer countBuffer, IntBuffer countIndices, int countIndex, FloatBuffer vertBuffer, FloatBuffer colorBuffer, boolean horiz, float[] color, float timeMin, float timeMax, float posMin, float posMax, float arrowBaseMin, float arrowBaseMax, float sizePerpCenter )
    {
        // all six vertices have the same color
        colorBuffer.put( color );
        colorBuffer.put( color );
        colorBuffer.put( color );
        colorBuffer.put( color );
        colorBuffer.put( color );
        colorBuffer.put( color );

        if ( horiz )
        {
            vertBuffer.put( arrowBaseMin );
            vertBuffer.put( posMax );

            vertBuffer.put( arrowBaseMax );
            vertBuffer.put( posMax );

            vertBuffer.put( timeMax );
            vertBuffer.put( sizePerpCenter );

            vertBuffer.put( arrowBaseMax );
            vertBuffer.put( posMin );

            vertBuffer.put( arrowBaseMin );
            vertBuffer.put( posMin );

            vertBuffer.put( timeMin );
            vertBuffer.put( sizePerpCenter );
        }
        else
        {
            vertBuffer.put( posMax );
            vertBuffer.put( arrowBaseMin );

            vertBuffer.put( posMax );
            vertBuffer.put( arrowBaseMax );

            vertBuffer.put( sizePerpCenter );
            vertBuffer.put( timeMax );

            vertBuffer.put( posMin );
            vertBuffer.put( arrowBaseMax );

            vertBuffer.put( posMin );
            vertBuffer.put( arrowBaseMin );

            vertBuffer.put( sizePerpCenter );
            vertBuffer.put( timeMin );
        }

        countBuffer.put( 6 );
        countIndices.put( countIndex );
        return countIndex + 6;
    }
}

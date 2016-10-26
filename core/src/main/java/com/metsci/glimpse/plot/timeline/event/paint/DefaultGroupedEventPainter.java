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

import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.*;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.google.common.collect.Lists;
import com.jogamp.opengl.math.Matrix4;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
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
import com.metsci.glimpse.support.shader.line.ColorLinePath;
import com.metsci.glimpse.support.shader.line.ColorLineProgram;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.triangle.ArrayColorProgram;

public class DefaultGroupedEventPainter implements GroupedEventPainter
{
    private static final float PI_2 = ( float ) ( Math.PI / 2.0f );

    protected int maxIconRows = DEFAULT_NUM_ICONS_ROWS;
    protected int minimumTextDisplayWidth = 20;

    protected ColorLineProgram lineProg;
    protected ColorLinePath linePath;
    protected LineStyle lineStyle;

    protected ArrayColorProgram fillProg;
    protected GLEditableBuffer fillPath;
    protected GLEditableBuffer fillColor;

    protected Matrix4 transformMatrix;

    public DefaultGroupedEventPainter( )
    {
        this.lineProg = new ColorLineProgram( );
        this.linePath = new ColorLinePath( );
        this.lineStyle = new LineStyle( );
        this.lineStyle.joinType = LineJoinType.JOIN_MITER;
        this.lineStyle.stippleEnable = false;

        this.fillProg = new ArrayColorProgram( );
        this.fillPath = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
        this.fillColor = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );

        this.transformMatrix = new Matrix4( );
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
    public void paint( GlimpseContext context, final EventPlotInfo info, final Collection<EventDrawInfo> events )
    {
        final StackedTimePlot2D plot = info.getStackedTimePlot( );

        GlimpseBounds bounds = GlimpsePainterBase.getBounds( context );
        Axis1D timeAxis = GlimpsePainterBase.requireAxis1D( context );
        GL3 gl = context.getGL( ).getGL3( );

        final int height = bounds.getHeight( );
        final int width = bounds.getWidth( );

        final Orientation orient = plot.getOrientation( );
        final int size = orient == Orientation.HORIZONTAL ? height : width;

        final int buffer = info.getEventPadding( );

        List<IconDrawInfo> iconDrawList = Lists.newArrayList( );
        List<TextDrawInfo> textDrawList = Lists.newArrayList( );

        this.linePath.clear( );
        this.fillPath.clear( );
        this.fillColor.clear( );

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
                        addVerticesBox( horiz, color, ( float ) timeMin, ( float ) timeMax, posMin, posMax );
                    }

                    if ( event.isShowBorder( ) )
                    {
                        float[] color = getBorderColor( event, info, isSelected );
                        addVerticesBox( horiz, color, ( float ) timeMin, ( float ) timeMax, posMin, posMax );
                    }
                }
                else
                {
                    if ( event.isShowBackground( ) )
                    {
                        float[] color = getBackgroundColor( event, info, isSelected );
                        addVerticesArrow( horiz, color, ( float ) timeMin, ( float ) timeMax, posMin, posMax, ( float ) arrowBaseMin, ( float ) arrowBaseMax, ( float ) sizePerpCenter );
                    }

                    if ( event.isShowBorder( ) )
                    {
                        float[] color = getBorderColor( event, info, isSelected );
                        addVerticesArrow( horiz, color, ( float ) timeMin, ( float ) timeMax, posMin, posMax, ( float ) arrowBaseMin, ( float ) arrowBaseMax, ( float ) sizePerpCenter );
                    }
                }

                int totalIconSizePerpPixels = getIconSizePerpPixels( event, info, sizePerpPixels );

                if ( event.hasChildren( ) )
                {
                    final int numChildren = event.getEventCount( );
                    final int numRows = maxIconRows;

                    // the requested size of the icon in the direction perpendicular to the time axis
                    int iconSizePerpPixels = totalIconSizePerpPixels / numRows;

                    int columnsByAvailableSpace = ( int ) Math.floor( remainingSpace / iconSizePerpPixels );
                    int columnsByNumberOfIcons = ( int ) Math.ceil( numChildren / ( double ) numRows );
                    int numColumns = Math.min( columnsByAvailableSpace, columnsByNumberOfIcons );

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
                                    float[] color;
                                    if ( icon == null || !atlas.isImageLoaded( icon ) )
                                    {
                                        color = getBackgroundColor( child, info, isSelected );
                                        color[3] = 0.5f;
                                        icon = defaultIconId;
                                    }
                                    else
                                    {
                                        color = GlimpseColor.getWhite( );
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
                                            iconDrawList.add( new IconDrawInfo( icon, x, y, iconScale, iconScale, 0, iconSizePerp, true, color ) );
                                        }
                                        else
                                        {
                                            iconDrawList.add( new IconDrawInfo( icon, x, y, iconScale, iconScale, 0, iconSizePerp, false, color ) );
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
                                iconDrawList.add( new IconDrawInfo( icon, posTime, posPerp, iconScale, iconScale, 0, iconSizePerp, true, GlimpseColor.getWhite( ) ) );
                            }
                            else
                            {
                                iconDrawList.add( new IconDrawInfo( icon, posPerp, posTime, iconScale, iconScale, 0, iconSizePerp, false, GlimpseColor.getWhite( ) ) );
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

            float xMin, yMin, xMax, yMax;

            if ( horiz )
            {
                xMin = ( float ) timeAxis.getMin( );
                xMax = ( float ) timeAxis.getMax( );
                yMin = 0;
                yMax = height;
            }
            else
            {
                xMin = 0;
                xMax = width;
                yMin = ( float ) timeAxis.getMin( );
                yMax = ( float ) timeAxis.getMax( );
            }

            fillProg.begin( gl );
            try
            {
                fillProg.setOrtho( gl, xMin, xMax, yMin, yMax );

                fillProg.draw( gl, fillPath, fillColor );
            }
            finally
            {
                fillProg.end( gl );
            }

            lineProg.begin( gl );
            try
            {
                lineProg.setOrtho( gl, xMin, xMax, yMin, yMax );
                lineProg.setViewport( gl, bounds );

                lineProg.draw( gl, lineStyle, linePath );
            }
            finally
            {
                lineProg.end( gl );
            }

            if ( !iconDrawList.isEmpty( ) )
            {
                atlas.beginRendering( context, xMin, xMax, yMin, yMax );
                try
                {
                    for ( IconDrawInfo iconInfo : iconDrawList )
                    {
                        if ( iconInfo.isX( ) )
                        {
                            atlas.drawImageAxisX( context, iconInfo.id, timeAxis, iconInfo.positionX, iconInfo.positionY, iconInfo.scaleX, iconInfo.scaleY, iconInfo.centerX, iconInfo.centerY, iconInfo.color );
                        }
                        else
                        {
                            atlas.drawImageAxisY( context, iconInfo.id, timeAxis, iconInfo.positionX, iconInfo.positionY, iconInfo.scaleX, iconInfo.scaleY, iconInfo.centerX, iconInfo.centerY, iconInfo.color );
                        }
                    }
                }
                finally
                {
                    atlas.endRendering( context );
                }
            }

            if ( !textDrawList.isEmpty( ) )
            {
                textRenderer.begin3DRendering( );
                try
                {
                    for ( TextDrawInfo textInfo : textDrawList )
                    {

                        if ( !horiz )
                        {
                            transformMatrix.loadIdentity( );
                            transformMatrix.makeOrtho( 0, width, 0, height, -1, 1 );
                            transformMatrix.translate( ( float ) textInfo.getShiftX( ), ( float ) textInfo.getShiftY( ), 0 );
                            transformMatrix.rotate( PI_2, 0, 0, 1.0f );
                            transformMatrix.translate( ( float ) -textInfo.getShiftX( ), ( float ) -textInfo.getShiftY( ), 0 );

                            textRenderer.setTransform( transformMatrix.getMatrix( ) );
                        }

                        GlimpseColor.setColor( textRenderer, textInfo.getColor( ) );
                        textRenderer.draw3D( textInfo.getText( ), textInfo.getX( ), textInfo.getY( ), 0, 1 );
                    }
                }
                finally
                {
                    textRenderer.end3DRendering( );
                }
            }
        }
        finally
        {
            info.getEventManager( ).unlock( );
        }
    }

    protected void addVerticesBox( boolean horiz, float[] color, float timeMin, float timeMax, float posMin, float posMax )
    {
        if ( horiz )
        {
            this.fillPath.growQuad2f( timeMin, posMin, timeMax, posMax );
            this.linePath.addRectangle( timeMin, posMin, timeMax, posMax, color );
        }
        else
        {
            this.fillPath.growQuad2f( posMin, timeMin, posMax, timeMax );
            this.linePath.addRectangle( posMin, timeMin, posMax, timeMax, color );
        }

        this.fillColor.growQuadSolidColor( color );
    }

    protected void addVerticesArrow( boolean horiz, float[] color, float timeMin, float timeMax, float posMin, float posMax, float arrowBaseMin, float arrowBaseMax, float sizePerpCenter )
    {
        if ( horiz )
        {
            // center rectangle
            fillPath.growQuad2f( arrowBaseMin, posMin, arrowBaseMax, posMax );

            // left arrow
            fillPath.grow2f( timeMin, sizePerpCenter );
            fillPath.grow2f( arrowBaseMin, posMax );
            fillPath.grow2f( arrowBaseMin, posMin );

            // right arrow
            fillPath.grow2f( timeMax, sizePerpCenter );
            fillPath.grow2f( arrowBaseMax, posMin );
            fillPath.grow2f( arrowBaseMax, posMax );

            linePath.moveTo( arrowBaseMin, posMax, color );
            linePath.lineTo( arrowBaseMax, posMax, color );
            linePath.lineTo( timeMax, sizePerpCenter, color );
            linePath.lineTo( arrowBaseMax, posMin, color );
            linePath.lineTo( arrowBaseMin, posMin, color );
            linePath.lineTo( timeMin, sizePerpCenter, color );
            linePath.closeLoop( );
        }
        else
        {
            // center rectangle
            fillPath.growQuad2f( posMin, arrowBaseMin, posMax, arrowBaseMax );

            // left arrow
            fillPath.grow2f( sizePerpCenter, timeMin );
            fillPath.grow2f( posMax, arrowBaseMin );
            fillPath.grow2f( posMin, arrowBaseMin );

            // right arrow
            fillPath.grow2f( sizePerpCenter, timeMax );
            fillPath.grow2f( posMin, arrowBaseMax );
            fillPath.grow2f( posMax, arrowBaseMax );

            linePath.moveTo( posMax, arrowBaseMin, color );
            linePath.lineTo( posMax, arrowBaseMax, color );
            linePath.lineTo( sizePerpCenter, timeMax, color );
            linePath.lineTo( posMin, arrowBaseMax, color );
            linePath.lineTo( posMin, arrowBaseMin, color );
            linePath.lineTo( sizePerpCenter, timeMin, color );
            linePath.closeLoop( );
        }

        for ( int i = 0; i < 12; i++ )
        {
            this.fillColor.growNfv( color, 0, 4 );
        }
    }
}

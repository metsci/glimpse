package com.metsci.glimpse.plot.timeline.event;

import static com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode.HideAll;
import static com.metsci.glimpse.plot.timeline.event.Event.TextRenderingMode.ShowAll;

import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.atlas.support.ImageData;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.sun.opengl.util.j2d.TextRenderer;

public class DefaultEventPainter implements EventPainter
{
    public static final int ARROW_TIP_BUFFER = 2;
    public static final int ARROW_SIZE = 10;
    public static final float[] DEFAULT_COLOR = GlimpseColor.getGray( );

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

            //XXX there is currently no way for custom subclasses of EventPainter to properly
            //    set isIconVisible and isTextVisible. This isn't a huge problem, but will cause
            //    EventSelection callbacks to incorrectly indicate the visibility of 
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

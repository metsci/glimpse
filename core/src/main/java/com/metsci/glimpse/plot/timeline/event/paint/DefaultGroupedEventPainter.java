package com.metsci.glimpse.plot.timeline.event.paint;

import static com.metsci.glimpse.plot.timeline.event.paint.DefaultEventPainter.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.event.Event;
import com.metsci.glimpse.plot.timeline.event.EventBounds;
import com.metsci.glimpse.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.plot.timeline.event.listener.EventSelectionHandler;

public class DefaultGroupedEventPainter implements GroupedEventPainter
{
    public DefaultGroupedEventPainter( )
    {

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

            boolean horiz = plot.isTimeAxisHorizontal( );

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
                    fillIndex = addVerticesArrow( fillCounts, fillIndices, fillIndex, fillBuffer, fillColorBuffer, horiz, color, ( float ) timeMin, ( float ) timeMax, ( float ) posMin, ( float ) posMax, (float) arrowBaseMin, (float) arrowBaseMax, (float) sizePerpCenter );
                    fillCount++;
                }

                if ( event.isShowBorder( ) )
                {
                    float[] color = getBorderColor( event, info, isSelected );
                    borderIndex = addVerticesArrow( borderCounts, borderIndices, borderIndex, borderBuffer, borderColorBuffer, horiz, color, ( float ) timeMin, ( float ) timeMax, ( float ) posMin, ( float ) posMax, (float) arrowBaseMin, (float) arrowBaseMax, (float) sizePerpCenter );
                    borderCount++;
                }
            }
        }
        
        gl.glEnableClientState( GL2.GL_COLOR_ARRAY );
        gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );
        
        if ( fillCount > 0 )
        {
            fillBuffer.flip( );
            fillColorBuffer.flip( );
            fillIndices.flip( );
            fillCounts.flip( );
            
            gl.glVertexPointer( 2, GL.GL_FLOAT, 0, fillBuffer );
            gl.glColorPointer( 4, GL.GL_FLOAT, 0, fillColorBuffer );
            gl.glMultiDrawArrays( GL2.GL_LINE_LOOP, fillIndices, fillCounts, fillCount );
        }
        
        if ( borderCount > 0 )
        {
            borderBuffer.flip( );
            borderColorBuffer.flip( );
            borderIndices.flip( );
            borderCounts.flip( );
            
            gl.glVertexPointer( 2, GL.GL_FLOAT, 0, borderBuffer );
            gl.glColorPointer( 4, GL.GL_FLOAT, 0, borderColorBuffer );
            gl.glMultiDrawArrays( GL2.GL_POLYGON, borderIndices, borderCounts, borderCount );
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

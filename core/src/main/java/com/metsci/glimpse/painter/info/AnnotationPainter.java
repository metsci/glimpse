/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.painter.info;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.metsci.glimpse.painter.info.SimpleTextPainter.xAlign;
import static com.metsci.glimpse.painter.info.SimpleTextPainter.yAlign;
import static com.metsci.glimpse.support.color.GlimpseColor.setColor;
import static com.metsci.glimpse.support.wrapped.WrappedGlimpseContext.getWrapper2D;
import static java.lang.Math.round;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.wrapped.Wrapper2D;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * Displays text annotations at specified locations on a plot.
 * @author ulman
 */
public class AnnotationPainter extends GlimpsePainterBase
{
    public static class Annotation
    {
        protected float x;
        protected float y;
        protected int xOffset_PX;
        protected int yOffset_PX;
        protected float[] color;
        protected String text;
        protected double xAlign;
        protected double yAlign;
        protected long startTime_PMILLIS;
        protected long endTime_PMILLIS;

        public Annotation( String text, float x, float y )
        {
            this( text, x, y, HorizontalPosition.Left, VerticalPosition.Bottom );
        }

        public Annotation( String text, float x, float y, HorizontalPosition hPos, VerticalPosition vPos )
        {
            this( text, x, y, 0, 0, hPos, vPos, GlimpseColor.getBlack( ) );
        }

        public Annotation( String text, float x, float y, float[] color )
        {
            this( text, x, y, 0, 0, HorizontalPosition.Left, VerticalPosition.Bottom, color );
        }

        public Annotation( String text, float x, float y, int xOffset_PX, int yOffset_PX, HorizontalPosition hPos, VerticalPosition vPos, float[] color )
        {
            this( text, x, y, xOffset_PX, yOffset_PX, hPos, vPos, color, 0L, 0L );
        }

        public Annotation( String text, float x, float y, int xOffset_PX, int yOffset_PX, HorizontalPosition hPos, VerticalPosition vPos, float[] color, long startTime_PMILLIS, long endTime_PMILLIS )
        {
            this( text, x, y, xOffset_PX, yOffset_PX, xAlign( hPos ), yAlign( vPos ), color, startTime_PMILLIS, endTime_PMILLIS );
        }

        public Annotation( String text,

                           float x,
                           float y,

                           int xOffset_PX,
                           int yOffset_PX,

                           double xAlign,
                           double yAlign,

                           float[] color,

                           long startTime_PMILLIS,
                           long endTime_PMILLIS )
        {
            this.text = text;

            this.x = x;
            this.y = y;

            this.xOffset_PX = xOffset_PX;
            this.yOffset_PX = yOffset_PX;

            this.xAlign = xAlign;
            this.yAlign = yAlign;

            this.color = color;

            this.startTime_PMILLIS = startTime_PMILLIS;
            this.endTime_PMILLIS = endTime_PMILLIS;
        }


        //@formatter:on

        public TimeStamp getStartTimeStamp( )
        {
            return TimeStamp.fromPosixMillis( startTime_PMILLIS );
        }

        public void setStartTime( TimeStamp startTime )
        {
            this.startTime_PMILLIS = startTime.toPosixMillis( );
        }

        public TimeStamp getEndTimeStamp( )
        {
            return TimeStamp.fromPosixMillis( startTime_PMILLIS );
        }

        public void setEndTime( TimeStamp endTime )
        {
            this.endTime_PMILLIS = endTime.toPosixMillis( );
        }

        public float getX( )
        {
            return x;
        }

        public void setX( float x )
        {
            this.x = x;
        }

        public float getY( )
        {
            return y;
        }

        public void setY( float y )
        {
            this.y = y;
        }

        public int getOffset_x( )
        {
            return xOffset_PX;
        }

        public void setOffset_x( int xOffset_PX )
        {
            this.xOffset_PX = xOffset_PX;
        }

        public int getOffset_y( )
        {
            return yOffset_PX;
        }

        public void setOffset_y( int yOffset_PX )
        {
            this.yOffset_PX = yOffset_PX;
        }

        public float[] getColor( )
        {
            return color;
        }

        public void setColor( float[] color )
        {
            this.color = color;
        }

        public String getText( )
        {
            return text;
        }

        public void setText( String text )
        {
            this.text = text;
        }

        public double getXAlign( )
        {
            return this.xAlign;
        }

        public void setXAlign( double xAlign )
        {
            this.xAlign = xAlign;
        }

        public void setHorizontalPosition( HorizontalPosition hPos )
        {
            this.setXAlign( xAlign( hPos ) );
        }

        public double getYAlign( )
        {
            return this.yAlign;
        }

        public void setYAlign( double yAlign )
        {
            this.yAlign = yAlign;
        }

        public void setVerticalPosition( VerticalPosition vPos )
        {
            this.setYAlign( yAlign( vPos ) );
        }
    }

    protected static final float[] DEFAULT_COLOR = GlimpseColor.getBlack( );

    protected Collection<Annotation> annotations;
    protected Predicate<Annotation> displayFilter;

    protected TextRenderer textRenderer;

    public AnnotationPainter( )
    {
        this( new TextRenderer( FontUtils.getDefaultPlain( 14.0f ) ) );
    }

    public AnnotationPainter( TextRenderer textRenderer )
    {
        this.annotations = new ArrayList<Annotation>( );
        this.textRenderer = textRenderer;
        displayFilter = an -> true;
    }

    public TextRenderer getTextRenderer( )
    {
        return this.textRenderer;
    }

    public Annotation addAnnotation( String text, float x, float y )
    {
        this.painterLock.lock( );
        try
        {
            Annotation annotation = new Annotation( text, x, y );
            this.annotations.add( annotation );
            return annotation;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public Annotation addAnnotation( String text, float x, float y, int offset_x, int offset_y, HorizontalPosition hPos, VerticalPosition vPos, float[] color )
    {
        this.painterLock.lock( );
        try
        {
            Annotation annotation = new Annotation( text, x, y, offset_x, offset_y, hPos, vPos, color );
            this.annotations.add( annotation );
            return annotation;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public Annotation addAnnotation( Annotation annotation )
    {
        this.painterLock.lock( );
        try
        {
            this.annotations.add( annotation );
            return annotation;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void clearAnnotations( )
    {
        this.painterLock.lock( );
        try
        {
            this.annotations.clear( );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void displayTime( TimeStamp time )
    {
        displayTimeRange( time, time );
    }

    public void displayTimeRange( TimeStamp minTime, TimeStamp maxTime )
    {
        setDisplayFilter( a -> minTime.isBeforeOrEquals( a.getEndTimeStamp() ) && a.getStartTimeStamp().isBeforeOrEquals( maxTime ) );
    }

    public void setDisplayFilter( Predicate<Annotation> filter )
    {
        this.displayFilter = filter;
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Wrapper2D wrapper = getWrapper2D( context );
        Axis2D axis = requireAxis2D( context );
        Axis1D xAxis = axis.getAxisX( );
        Axis1D yAxis = axis.getAxisY( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        this.painterLock.lock( );
        try
        {
            this.textRenderer.beginRendering( width, height );
            try
            {
                for ( Annotation annotation : annotations )
                {
                    if ( !displayFilter.test( annotation ) )
                    {
                        continue;
                    }

                    setColor( this.textRenderer, firstNonNull( annotation.color, DEFAULT_COLOR ) );

                    double x = annotation.x;
                    double y = annotation.y;
                    double xAlign = annotation.xAlign;
                    double yAlign = annotation.yAlign;
                    double xOffset_PX = annotation.xOffset_PX;
                    double yOffset_PX = annotation.yOffset_PX;

                    Rectangle2D textBounds = textRenderer.getBounds( annotation.text );
                    int i = ( int ) round( axis.getAxisX( ).valueToScreenPixel( x ) - xAlign*( textBounds.getWidth( ) ) + xOffset_PX );
                    int j = ( int ) round( axis.getAxisY( ).valueToScreenPixel( y ) - yAlign*( textBounds.getHeight( ) ) + yOffset_PX );

                    double xMin = xAxis.screenPixelToValue( i );
                    double xMax = xAxis.screenPixelToValue( i + textBounds.getWidth( ) );

                    double yMin = yAxis.screenPixelToValue( j );
                    double yMax = yAxis.screenPixelToValue( j + textBounds.getHeight( ) );

                    for ( double yShift : wrapper.y.getRenderShifts( yMin, yMax ) )
                    {
                        for ( double xShift : wrapper.x.getRenderShifts( xMin, xMax ) )
                        {
                            int iShifted = ( int ) round( xAxis.valueToScreenPixel( x + xShift ) - xAlign*( textBounds.getWidth( ) ) + xOffset_PX );
                            int jShifted = ( int ) round( yAxis.valueToScreenPixel( y + yShift ) - yAlign*( textBounds.getHeight( ) ) + yOffset_PX );
                            textRenderer.draw( annotation.text, iShifted, jShifted );
                        }
                    }
                }
            }
            finally
            {
                this.textRenderer.endRendering( );
            }
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;
    }
}

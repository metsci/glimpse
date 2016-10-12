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
package com.metsci.glimpse.painter.info;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
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
        protected int offset_x;
        protected int offset_y;
        protected float[] color;
        protected String text;
        protected HorizontalPosition hPos;
        protected VerticalPosition vPos;
        protected long startTime;
        protected long endTime;

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

        public Annotation( String text, float x, float y, int offset_x, int offset_y, HorizontalPosition hPos, VerticalPosition vPos, float[] color )
        {
            this.x = x;
            this.y = y;
            this.offset_x = offset_x;
            this.offset_y = offset_y;
            this.text = text;
            this.color = color;
            this.hPos = hPos;
            this.vPos = vPos;
        }

        public Annotation( String text, float x, float y, int offset_x, int offset_y, HorizontalPosition hPos, VerticalPosition vPos, float[] color, long startTime, long endTime )
        {
            this.x = x;
            this.y = y;
            this.offset_x = offset_x;
            this.offset_y = offset_y;
            this.text = text;
            this.color = color;
            this.hPos = hPos;
            this.vPos = vPos;
            this.startTime = startTime;
            this.endTime = endTime;
        }
        //@formatter:on

        /**
         * @deprecated
         * @see #getStartTimeStamp()
         */
        @Deprecated
        public long getStartTime( )
        {
            return startTime;
        }

        /**
         * @deprecated
         * @see #setStartTime(TimeStamp)
         */
        @Deprecated
        public void setStartTime( long startTime )
        {
            this.startTime = startTime;
        }

        /**
         * @deprecated
         * @see #getEndTimeStamp()
         */
        @Deprecated
        public long getEndTime( )
        {
            return endTime;
        }

        /**
         * @deprecated
         * @see #setEndTime(TimeStamp)
         */
        @Deprecated
        public void setEndTime( long endTime )
        {
            this.endTime = endTime;
        }

        public TimeStamp getStartTimeStamp( )
        {
            return TimeStamp.fromPosixMillis( startTime );
        }

        public void setStartTime( TimeStamp startTime )
        {
            this.startTime = startTime.toPosixMillis( );
        }

        public TimeStamp getEndTimeStamp( )
        {
            return TimeStamp.fromPosixMillis( startTime );
        }

        public void setEndTime( TimeStamp endTime )
        {
            this.endTime = endTime.toPosixMillis( );
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
            return offset_x;
        }

        public void setOffset_x( int offset_x )
        {
            this.offset_x = offset_x;
        }

        public int getOffset_y( )
        {
            return offset_y;
        }

        public void setOffset_y( int offset_y )
        {
            this.offset_y = offset_y;
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

        public HorizontalPosition getHorizontalPosition( )
        {
            return this.hPos;
        }

        public void setHorizontalPosition( HorizontalPosition hPos )
        {
            this.hPos = hPos;
        }

        public VerticalPosition getVerticalPosition( )
        {
            return this.vPos;
        }

        public void setVerticalPosition( VerticalPosition vPos )
        {
            this.vPos = vPos;
        }
    }

    protected static final float[] DEFAULT_COLOR = GlimpseColor.getBlack( );

    protected Collection<Annotation> annotations;

    protected long minTime = Long.MIN_VALUE;
    protected long maxTime = Long.MAX_VALUE;

    protected TextRenderer textRenderer;

    public AnnotationPainter( )
    {
        this( new TextRenderer( FontUtils.getDefaultPlain( 14.0f ) ) );
    }

    public AnnotationPainter( TextRenderer textRenderer )
    {
        this.annotations = new ArrayList<Annotation>( );
        this.textRenderer = textRenderer;
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
        displayTime( time.toPosixMillis( ) );
    }

    /**
     * @see #displayTime( TimeStamp )
     * @deprecated
     */
    @Deprecated
    public void displayTime( long time )
    {
        this.painterLock.lock( );
        try
        {
            this.minTime = time;
            this.maxTime = time;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void displayTimeRange( TimeStamp minTime, TimeStamp maxTime )
    {
        displayTimeRange( minTime.toPosixMillis( ), maxTime.toPosixMillis( ) );
    }

    /**
     * @see #displayTimeRange( TimeStamp, TimeStamp )
     * @deprecated
     */
    @Deprecated
    public void displayTimeRange( long minTime, long maxTime )
    {
        this.painterLock.lock( );
        try
        {
            this.minTime = minTime;
            this.maxTime = maxTime;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = requireAxis2D( context );

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
                    if ( !inTimeRange( annotation ) ) continue;

                    float[] textColor = DEFAULT_COLOR;
                    if ( annotation.color != null ) textColor = annotation.color;

                    Rectangle2D textBounds = textRenderer.getBounds( annotation.text );

                    double textWidth = textBounds.getWidth( );
                    // textBounds.getHeight( ) is too conservative (box is too large, perhaps to fit every conceivable character)
                    double textHeight = textRenderer.getFont( ).getSize( );

                    int halfTextWidth = ( int ) ( textWidth / 2d );
                    int halfTextHeight = ( int ) ( textHeight / 2d );

                    int x = axis.getAxisX( ).valueToScreenPixel( annotation.x );
                    int y = axis.getAxisY( ).valueToScreenPixel( annotation.y );

                    switch ( annotation.hPos )
                    {
                        case Left:
                            break;
                        case Center:
                            x = x - halfTextWidth;
                            break;
                        case Right:
                            x = x - ( int ) textWidth;
                            break;
                    }

                    switch ( annotation.vPos )
                    {
                        case Bottom:
                            break;
                        case Center:
                            y = y - halfTextHeight;
                            break;
                        case Top:
                            y = y - ( int ) textHeight;
                            break;
                    }

                    this.textRenderer.setColor( textColor[0], textColor[1], textColor[2], textColor[3] );
                    this.textRenderer.draw( annotation.text, x + annotation.offset_x, y + annotation.offset_y );
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

    protected boolean inTimeRange( Annotation annotation )
    {
        return ( annotation.getStartTime( ) <= maxTime && annotation.getEndTime( ) >= minTime ) || ( minTime <= annotation.getEndTime( ) && maxTime >= annotation.getStartTime( ) );
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;
    }
}

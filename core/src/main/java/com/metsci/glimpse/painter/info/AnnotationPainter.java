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
package com.metsci.glimpse.painter.info;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.j2d.TextRenderer;

/**
 * Displays text annotations at specified locations on a plot.
 * @author ulman
 */
public class AnnotationPainter extends GlimpseDataPainter2D
{
    public static enum AnnotationFont
    {
        Monospace_8_BY_13(GLUT.BITMAP_8_BY_13, 13), Monospace_9_BY_15(GLUT.BITMAP_9_BY_15, 15), Times_Roman_10(GLUT.BITMAP_TIMES_ROMAN_10, 10), Times_Roman_24(GLUT.BITMAP_TIMES_ROMAN_24, 24), Helvetical_10(GLUT.BITMAP_HELVETICA_10, 10), Helvetical_12(GLUT.BITMAP_HELVETICA_12, 12), Helvetical_18(GLUT.BITMAP_HELVETICA_18, 18);

        private int font;
        private int height;

        private AnnotationFont( int font, int height )
        {
            this.font = font;
            this.height = height;
        }

        public int getFont( )
        {
            return this.font;
        }

        public int getHeight( )
        {
            return this.height;
        }
    }

    public static class Annotation
    {
        protected float x;
        protected float y;
        protected int offset_x;
        protected int offset_y;
        protected float[] color;
        protected String text;
        protected TextRenderer textRenderer;
        protected int font;
        protected int height;
        protected boolean centerX;
        protected boolean centerY;
        protected long startTime;
        protected long endTime;

        public Annotation( String text, float x, float y )
        {
            this( text, x, y, 0, 0, false, false, null, null );
        }

        public Annotation( String text, float x, float y, boolean centerX, boolean centerY )
        {
            this( text, x, y, 0, 0, centerX, centerY, null, null );
        }

        public Annotation( String text, float x, float y, float[] color )
        {
            this( text, x, y, 0, 0, false, false, null, color );
        }

        //@formatter:off
        public Annotation( TextRenderer textRenderer, String text,
                float x, float y, int offset_x, int offset_y,
                boolean centerX, boolean centerY, float[] color )
        {
             this( textRenderer, text, x,  y,  offset_x,  offset_y, centerX,  centerY,  color, Long.MIN_VALUE, Long.MAX_VALUE );
        }

        public Annotation( String text, float x, float y,
                           int offset_x, int offset_y,
                           boolean centerX, boolean centerY,
                           AnnotationFont font, float[] color )
        {
            this.x = x;
            this.y = y;
            this.offset_x = offset_x;
            this.offset_y = offset_y;
            this.text = text;
            this.color = color;
            this.centerX = centerX;
            this.centerY = centerY;
            font = font == null ? DEFAULT_FONT : font;
            this.font = font.getFont( );
            this.height = font.getHeight( );
        }

        public Annotation( TextRenderer textRenderer, String text,
                          float x, float y, int offset_x, int offset_y,
                          boolean centerX, boolean centerY,
                          float[] color, long startTime, long endTime )
        {
            this.x = x;
            this.y = y;
            this.offset_x = offset_x;
            this.offset_y = offset_y;
            this.text = text;
            this.color = color;
            this.centerX = centerX;
            this.centerY = centerY;
            this.font = -1;
            this.height = -1;
            this.textRenderer = textRenderer;
            this.startTime = startTime;
            this.endTime = endTime;
        }
        //@formatter:on

        public long getStartTime( )
        {
            return startTime;
        }

        public void setStartTime( long startTime )
        {
            this.startTime = startTime;
        }

        public long getEndTime( )
        {
            return endTime;
        }

        public void setEndTime( long endTime )
        {
            this.endTime = endTime;
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

        public int getFont( )
        {
            return font;
        }

        public void setFont( int font )
        {
            this.font = font;
        }

        public int getHeight( )
        {
            return height;
        }

        public void setHeight( int height )
        {
            this.height = height;
        }

        public boolean isCenterX( )
        {
            return centerX;
        }

        public void setCenterX( boolean centerX )
        {
            this.centerX = centerX;
        }

        public boolean isCenterY( )
        {
            return centerY;
        }

        public void setCenterY( boolean centerY )
        {
            this.centerY = centerY;
        }

        public TextRenderer getTextRenderer( )
        {
            return textRenderer;
        }
    }

    protected static final GLUT glut = new GLUT( );
    protected static final float[] DEFAULT_COLOR = GlimpseColor.getBlack( );
    protected static final AnnotationFont DEFAULT_FONT = AnnotationFont.Helvetical_12;

    protected Collection<Annotation> annotations;
    protected ReentrantLock lock;

    protected long minTime = Long.MIN_VALUE;
    protected long maxTime = Long.MAX_VALUE;

    public AnnotationPainter( )
    {
        this.annotations = new ArrayList<Annotation>( );
        this.lock = new ReentrantLock( );
    }

    public Annotation addAnnotation( String text, float x, float y )
    {
        this.lock.lock( );
        try
        {
            Annotation annotation = new Annotation( text, x, y );
            this.annotations.add( annotation );
            return annotation;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public Annotation addAnnotation( String text, float x, float y, int offset_x, int offset_y, boolean centerX, boolean centerY, AnnotationFont font, float[] color )
    {
        this.lock.lock( );
        try
        {
            Annotation annotation = new Annotation( text, x, y, offset_x, offset_y, centerX, centerY, font, color );
            this.annotations.add( annotation );
            return annotation;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public Annotation addAnnotation( TextRenderer textRenderer, String text, float x, float y, int offset_x, int offset_y, boolean centerX, boolean centerY, float[] color )
    {
        this.lock.lock( );
        try
        {
            Annotation annotation = new Annotation( textRenderer, text, x, y, offset_x, offset_y, centerX, centerY, color );
            this.annotations.add( annotation );
            return annotation;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public Annotation addAnnotation( Annotation annotation )
    {
        this.lock.lock( );
        try
        {
            this.annotations.add( annotation );
            return annotation;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void clearAnnotations( )
    {
        this.lock.lock( );
        try
        {
            this.annotations.clear( );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void displayTime( long time )
    {
        this.lock.lock( );
        try
        {
            this.minTime = time;
            this.maxTime = time;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void displayTimeRange( long minTime, long maxTime )
    {
        this.lock.lock( );
        try
        {
            this.minTime = minTime;
            this.maxTime = maxTime;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    @Override
    public void paintTo( GL gl, GlimpseBounds bounds, Axis2D axis )
    {
        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        this.lock.lock( );
        try
        {
            for ( Annotation annotation : annotations )
            {
                if ( !inTimeRange( annotation ) )
                    continue;

                float[] textColor = DEFAULT_COLOR;
                if ( annotation.color != null ) textColor = annotation.color;

                if ( annotation.getTextRenderer( ) != null )
                {
                    TextRenderer textRenderer = annotation.getTextRenderer( );

                    Rectangle2D textBounds = textRenderer.getBounds( annotation.text );
                    int x = ( int ) ( axis.getAxisX( ).valueToScreenPixel( annotation.x ) - ( annotation.centerX ? textBounds.getWidth( ) / 2 : 0 ) );
                    int y = ( int ) ( axis.getAxisY( ).valueToScreenPixel( annotation.y ) - ( annotation.centerY ? textBounds.getHeight( ) / 2 : 0 ) );

                    textRenderer.beginRendering( width, height );
                    try
                    {
                        textRenderer.setColor( textColor[0], textColor[1], textColor[2], textColor[3] );
                        textRenderer.draw( annotation.text, x + annotation.offset_x, y + annotation.offset_y );
                    }
                    finally
                    {
                        textRenderer.endRendering( );
                    }
                }
                else
                {
                    gl.glColor3fv( textColor, 0 );

                    float posX = annotation.x;
                    float posY = annotation.y;

                    posX += annotation.offset_x / axis.getAxisX( ).getPixelsPerValue( );
                    posY += annotation.offset_y / axis.getAxisY( ).getPixelsPerValue( );

                    if ( annotation.centerX )
                    {
                        posX -= glut.glutBitmapLength( annotation.font, annotation.text ) / ( 2 * axis.getAxisX( ).getPixelsPerValue( ) );
                    }

                    if ( annotation.centerY )
                    {
                        posY -= annotation.height / ( 3 * axis.getAxisY( ).getPixelsPerValue( ) );
                    }

                    gl.glRasterPos2f( posX, posY );

                    glut.glutBitmapString( annotation.font, annotation.text );
                }
            }
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    protected boolean inTimeRange( Annotation annotation )
    {
        return ( annotation.getStartTime( ) <= maxTime && annotation.getEndTime( ) >= minTime ) ||
               ( minTime <= annotation.getEndTime( ) && maxTime >= annotation.getStartTime( ) );
    }
}

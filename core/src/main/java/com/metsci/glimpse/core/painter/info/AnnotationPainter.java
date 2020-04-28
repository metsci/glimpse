/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.painter.info;

import static com.metsci.glimpse.core.gl.util.GLUtils.disableBlending;
import static com.metsci.glimpse.core.gl.util.GLUtils.enableStandardBlending;
import static com.metsci.glimpse.core.painter.info.SimpleTextPainter.xAlign;
import static com.metsci.glimpse.core.painter.info.SimpleTextPainter.yAlign;
import static com.metsci.glimpse.core.support.wrapped.WrappedGlimpseContext.getWrapper2D;
import static java.lang.Math.round;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.context.GlimpseBounds;
import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.gl.GLEditableBuffer;
import com.metsci.glimpse.core.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.core.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.core.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.core.support.color.GlimpseColor;
import com.metsci.glimpse.core.support.font.FontUtils;
import com.metsci.glimpse.core.support.shader.line.LineJoinType;
import com.metsci.glimpse.core.support.shader.line.LinePath;
import com.metsci.glimpse.core.support.shader.line.LineProgram;
import com.metsci.glimpse.core.support.shader.line.LineStyle;
import com.metsci.glimpse.core.support.shader.triangle.FlatColorProgram;
import com.metsci.glimpse.core.support.wrapped.Wrapper2D;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * Displays text annotations at specified locations on a plot.
 * @author ulman
 */
public class AnnotationPainter extends GlimpsePainterBase
{
    public static class Annotation
    {
        protected double x;
        protected double y;
        protected int xOffset_PX;
        protected int yOffset_PX;
        protected float[] color;
        protected float[] bgColor;
        protected float[] borderColor;
        protected String text;
        protected HorizontalPosition hPos;
        protected VerticalPosition vPos;
        protected long startTime_PMILLIS;
        protected long endTime_PMILLIS;

        public Annotation( String text, double x, double y )
        {
            this( text, x, y, HorizontalPosition.Left, VerticalPosition.Bottom );
        }

        public Annotation( String text, double x, double y, HorizontalPosition hPos, VerticalPosition vPos )
        {
            this( text, x, y, 0, 0, hPos, vPos, GlimpseColor.getBlack( ) );
        }

        public Annotation( String text, double x, double y, float[] color )
        {
            this( text, x, y, 0, 0, HorizontalPosition.Left, VerticalPosition.Bottom, color );
        }

        public Annotation( String text, double x, double y, int xOffset_PX, int yOffset_PX, HorizontalPosition hPos, VerticalPosition vPos, float[] color )
        {
            this( text, x, y, xOffset_PX, yOffset_PX, hPos, vPos, color, 0L, 0L );
        }

        public Annotation( String text, double x, double y, int xOffset_PX, int yOffset_PX, HorizontalPosition hPos, VerticalPosition vPos, float[] color, long startTime_PMILLIS, long endTime_PMILLIS )
        {
            this.text = text;

            this.x = x;
            this.y = y;

            this.xOffset_PX = xOffset_PX;
            this.yOffset_PX = yOffset_PX;

            this.hPos = hPos;
            this.vPos = vPos;

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

        public double getX( )
        {
            return x;
        }

        public void setX( float x )
        {
            this.x = x;
        }

        public double getY( )
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

        public void setBackgroundColor( float[] bgColor )
        {
            this.bgColor = bgColor;
        }

        public void clearBackgroundColor( )
        {
            this.bgColor = null;
        }

        public void setBorderColor( float[] borderColor )
        {
            this.borderColor = borderColor;
        }

        public void clearBorderColor( )
        {
            this.borderColor = null;
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
    protected Predicate<Annotation> displayFilter;

    protected TextRenderer textRenderer;
    protected float fontDescent;

    protected FlatColorProgram fillProg;
    protected GLEditableBuffer fillBuffer;

    protected LineProgram lineProg;
    protected LinePath linePath;
    protected LineStyle lineStyle;

    public AnnotationPainter( )
    {
        this( FontUtils.getDefaultPlain( 14 ) );
    }

    public AnnotationPainter( Font font )
    {
        this.annotations = new ArrayList<Annotation>( );
        this.textRenderer = new TextRenderer( font, true, true );
        this.fontDescent = font.getLineMetrics( "gpqy", textRenderer.getFontRenderContext( ) ).getDescent( );

        this.displayFilter = an -> true;

        this.lineProg = new LineProgram( );
        this.fillProg = new FlatColorProgram( );

        this.lineStyle = new LineStyle( );
        this.lineStyle.stippleEnable = false;
        this.lineStyle.joinType = LineJoinType.JOIN_NONE;
        this.lineStyle.feather_PX = 0f;

        this.linePath = new LinePath( );
        this.fillBuffer = new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
    }

    public Annotation addAnnotation( String text, double x, double y )
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

    public Annotation addAnnotation( String text, double x, double y, int offset_x, int offset_y, HorizontalPosition hPos, VerticalPosition vPos, float[] color )
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
        setDisplayFilter( a -> minTime.isBeforeOrEquals( a.getEndTimeStamp( ) ) && a.getStartTimeStamp( ).isBeforeOrEquals( maxTime ) );
    }

    public void setDisplayFilter( Predicate<Annotation> filter )
    {
        this.displayFilter = filter;
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GL3 gl = getGL3( context );
        GlimpseBounds bounds = getBounds( context );
        Wrapper2D wrapper = getWrapper2D( context );
        Axis2D axis = requireAxis2D( context );
        Axis1D xAxis = axis.getAxisX( );
        Axis1D yAxis = axis.getAxisY( );

        this.painterLock.lock( );
        try
        {
            for ( Annotation annotation : annotations )
            {
                if ( annotation.text == null ||
                        annotation.text.isEmpty( ) ||
                        !displayFilter.test( annotation ) )
                {
                    continue;
                }

                double x = annotation.x;
                double y = annotation.y;
                double xOffset_PX = annotation.xOffset_PX;
                double yOffset_PX = annotation.yOffset_PX;
                double xAlign = xAlign( annotation.hPos );
                double yAlign = yAlign( annotation.vPos );

                Rectangle2D textBounds = textRenderer.getBounds( annotation.text );
                int i = ( int ) round( axis.getAxisX( ).valueToScreenPixel( x ) - xAlign * ( textBounds.getWidth( ) ) + xOffset_PX );
                int j = ( int ) round( axis.getAxisY( ).valueToScreenPixel( y ) - yAlign * ( textBounds.getHeight( ) ) + yOffset_PX );

                double xMin = xAxis.screenPixelToValue( i );
                double xMax = xAxis.screenPixelToValue( i + textBounds.getWidth( ) );

                double yMin = yAxis.screenPixelToValue( j );
                double yMax = yAxis.screenPixelToValue( j + textBounds.getHeight( ) );

                for ( double yShift : wrapper.y.getRenderShifts( yMin, yMax ) )
                {
                    for ( double xShift : wrapper.x.getRenderShifts( xMin, xMax ) )
                    {
                        int iShifted = ( int ) round( xAxis.valueToScreenPixel( x + xShift ) - xAlign * ( textBounds.getWidth( ) ) + xOffset_PX );
                        int jShifted = ( int ) round( yAxis.valueToScreenPixel( y + yShift ) - yAlign * ( textBounds.getHeight( ) ) + yOffset_PX );
                        textBounds.setFrame( iShifted, jShifted, textBounds.getWidth( ), textBounds.getHeight( ) );

                        paintAnnotation( gl, annotation, bounds, textBounds );
                    }
                }
            }
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    protected void paintAnnotation( GL3 gl, Annotation annotation, GlimpseBounds bounds, Rectangle2D bound )
    {
        boolean paintBackground = annotation.bgColor != null;
        boolean paintBorder = annotation.borderColor != null;

        float yMin = ( float ) bound.getMinY( );
        float xMin = ( float ) bound.getMinX( );
        float xMax = ( float ) bound.getMaxX( );
        float yMax = ( float ) bound.getMaxY( );

        enableStandardBlending( gl );
        if ( paintBackground || paintBorder )
        {
            float padX = 2.5f;
            float padY = 2.5f;

            if ( paintBackground )
            {
                this.fillBuffer.clear( );
                this.fillBuffer.growQuad2f( xMin - padX, yMin - padY / 2, xMax + padX, yMax + padY );

                this.fillProg.begin( gl );
                try
                {
                    this.fillProg.setPixelOrtho( gl, bounds );

                    this.fillProg.draw( gl, this.fillBuffer, annotation.bgColor );
                }
                finally
                {
                    this.fillProg.end( gl );
                }
            }

            if ( paintBorder )
            {
                this.linePath.clear( );
                this.linePath.addRectangle( xMin - padX, yMin - padY, xMax + padX, yMax + padY );

                this.lineProg.begin( gl );
                try
                {
                    this.lineProg.setPixelOrtho( gl, bounds );
                    this.lineProg.setViewport( gl, bounds );

                    this.lineStyle.rgba = annotation.borderColor;

                    this.lineProg.draw( gl, this.lineStyle, this.linePath );
                }
                finally
                {
                    this.lineProg.end( gl );
                }
            }
        }

        textRenderer.beginRendering( bounds.getWidth( ), bounds.getHeight( ) );
        try
        {
            GlimpseColor.setColor( textRenderer, annotation.color );

            textRenderer.draw3D( annotation.text, xMin, yMin + fontDescent, 0, 1 );
        }
        finally
        {
            textRenderer.endRendering( );
            disableBlending( gl );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        textRenderer.dispose( );
    }
}

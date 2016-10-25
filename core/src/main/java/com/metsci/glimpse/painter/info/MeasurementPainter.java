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

import static com.metsci.glimpse.support.font.FontUtils.*;

import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;

/**
 * Displays a protractor and ruler when the mouse cursor is locked
 * via the middle mouse button.
 *
 * @author ulman
 */
public class MeasurementPainter extends GlimpsePainterBase
{
    private static final double RADIANS_PER_VERTEX = Math.PI / 60.0;
    private static final double RAD_TO_DEG = 180.0 / Math.PI;

    private static final double ANGLE_WEDGE_RADIUS_FRACTION = 0.8;

    private static final int DIST_TEXT_OFFSET_X = 10;
    private static final int DIST_TEXT_OFFSET_Y = 0;

    private static final int ANGLE_TEXT_OFFSET_X = 20;
    private static final int ANGLE_TEXT_OFFSET_Y = 5;

    protected float[] protractorColor = new float[] { 0.0f, 0.769f, 1.0f, 0.6f };
    protected float[] rulerColor = new float[] { 0.0f, 0.769f, 1.0f, 1.0f };
    protected float[] textColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    protected float rulerWidth = 1.5f;

    protected TextRenderer textRenderer;
    protected DecimalFormat angleFormatter;
    protected DecimalFormat distanceFormatter;
    protected AxisUnitConverter distanceUnitConverter;

    protected FlatColorProgram fillProg;
    protected GLEditableBuffer fillBuffer;

    protected LineProgram lineProg;
    protected LinePath linePath;
    protected LineStyle lineStyle;

    public MeasurementPainter( )
    {
        this( "" );
    }

    public MeasurementPainter( String units )
    {
        this( new DecimalFormat( " ##0.0\u00B0" ), new DecimalFormat( " ##0 " + units ) );
    }

    public MeasurementPainter( DecimalFormat angleFormatter, DecimalFormat distanceFormatter )
    {
        this.textRenderer = new TextRenderer( getDefaultBold( 16 ) );
        this.angleFormatter = angleFormatter;
        this.distanceFormatter = distanceFormatter;
        this.distanceUnitConverter = AxisUnitConverters.identity;

        this.lineProg = new LineProgram( );
        this.fillProg = new FlatColorProgram( );

        this.lineStyle = new LineStyle( );
        this.lineStyle.stippleEnable = false;
        this.lineStyle.feather_PX = 0.8f;

        this.linePath = new LinePath( );
        this.fillBuffer= new GLEditableBuffer( GL.GL_STATIC_DRAW, 0 );
    }

    public void setDistanceUnitConverter( AxisUnitConverter converter )
    {
        distanceUnitConverter = converter;
    }

    public void setRulerWidth( float width )
    {
        rulerWidth = width;
    }

    public void setTextColor( float[] rgba )
    {
        textColor = rgba;
    }

    public void setTextColor( float r, float g, float b, float a )
    {
        textColor[0] = r;
        textColor[1] = g;
        textColor[2] = b;
        textColor[3] = a;
    }

    public void setRulerColor( float[] rgba )
    {
        rulerColor = rgba;
    }

    public void setRulerColor( float r, float g, float b, float a )
    {
        rulerColor[0] = r;
        rulerColor[1] = g;
        rulerColor[2] = b;
        rulerColor[3] = a;
    }

    public void setProtractorColor( float[] rgba )
    {
        protractorColor = rgba;
    }

    public void setProtractorColor( float r, float g, float b, float a )
    {
        protractorColor[0] = r;
        protractorColor[1] = g;
        protractorColor[2] = b;
        protractorColor[3] = a;
    }

    @Override
    public void doDispose( GlimpseContext context )
    {
        this.textRenderer.dispose( );

        this.lineProg.dispose( context.getGL( ).getGL3( ) );
        this.fillProg.dispose( context.getGL( ).getGL3( ) );

        this.linePath.dispose( context.getGL( ) );
        this.fillBuffer.dispose( context.getGL( ) );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = requireAxis2D( context );
        GL3 gl = context.getGL( ).getGL3( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        boolean lockedX = axisX.isSelectionLocked( );
        boolean lockedY = axisY.isSelectionLocked( );

        if ( !lockedX && !lockedY ) return;

        float lockX = ( float ) axisX.getSelectionCenter( );
        float lockY = ( float ) axisY.getSelectionCenter( );

        float mouseX = ( float ) axisX.getMouseValue( );
        float mouseY = ( float ) axisY.getMouseValue( );

        float diffX = mouseX - lockX;
        float diffY = mouseY - lockY;

        float distance = ( float ) Math.sqrt( diffX * diffX + diffY * diffY );

        float angle = ( float ) Math.atan2( mouseY - lockY, mouseX - lockX );
        int sign = angle < 0 ? -1 : 1;
        float step = ( float ) RADIANS_PER_VERTEX;
        float radius = ( float ) ( distance * ANGLE_WEDGE_RADIUS_FRACTION );

        //// draw ruler ////

        this.linePath.clear( );
        this.linePath.moveTo( lockX, lockY );
        this.linePath.lineTo( mouseX, mouseY );

        this.lineProg.begin( gl );
        try
        {
            this.lineProg.setAxisOrtho( gl, axis );
            this.lineProg.setViewport( gl, bounds );

            this.lineStyle.rgba = rulerColor;
            this.lineStyle.thickness_PX = rulerWidth;

            this.lineProg.draw( gl, lineStyle, linePath );
        }
        finally
        {
            this.lineProg.end( gl );
        }

        this.fillBuffer.clear( );

        this.fillBuffer.grow2f( lockX, lockY );

        for ( double a = 0; a < angle * sign; a += step )
        {
            double x = lockX + Math.cos( a * sign ) * radius;
            double y = lockY + Math.sin( a * sign ) * radius;
            this.fillBuffer.grow2f( ( float ) x, ( float ) y );
        }

        double x = lockX + Math.cos( angle ) * radius;
        double y = lockY + Math.sin( angle ) * radius;
        this.fillBuffer.grow2f( ( float ) x, ( float ) y );

        this.fillProg.begin( gl );
        try
        {
            this.fillProg.setAxisOrtho( gl, axis );
            this.fillProg.setColor( gl, protractorColor );

            this.fillProg.draw( gl, GL.GL_TRIANGLE_FAN, this.fillBuffer, 0, this.fillBuffer.sizeFloats( ) / 2 );
        }
        finally
        {
            this.fillProg.end( gl );
        }

        //// draw angle text ////
        String angleString = angleFormatter.format( angle * RAD_TO_DEG );
        int anglePosX = axisX.valueToScreenPixel( lockX );
        int anglePosY = axisY.valueToScreenPixel( lockY );

        if ( sign > 0 )
        {
            anglePosX += ANGLE_TEXT_OFFSET_X;
            anglePosY += ANGLE_TEXT_OFFSET_Y;
        }
        else
        {
            Rectangle2D rec = textRenderer.getBounds( angleString );
            anglePosX += ANGLE_TEXT_OFFSET_X;
            anglePosY += -( int ) rec.getHeight( ) - ANGLE_TEXT_OFFSET_Y;
        }

        textRenderer.beginRendering( width, height );
        try
        {
            GlimpseColor.setColor( textRenderer, textColor );
            textRenderer.draw( angleString, anglePosX, anglePosY );
        }
        finally
        {
            textRenderer.endRendering( );
        }

        //// draw distance text ////
        double distanceUnits = distanceUnitConverter.toAxisUnits( distance );
        String distanceString = distanceFormatter.format( distanceUnits );
        int distancePosX = axisX.valueToScreenPixel( mouseX ) + DIST_TEXT_OFFSET_X;
        int distancePosY = axisY.valueToScreenPixel( mouseY ) + DIST_TEXT_OFFSET_Y;

        textRenderer.beginRendering( width, height );
        try
        {
            GlimpseColor.setColor( textRenderer, textColor );
            textRenderer.draw( distanceString, distancePosX, distancePosY );
        }
        finally
        {
            textRenderer.endRendering( );
        }
    }
}

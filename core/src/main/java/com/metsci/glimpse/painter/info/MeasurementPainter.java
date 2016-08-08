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

import static com.metsci.glimpse.support.font.FontUtils.getDefaultBold;

import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.support.color.GlimpseColor;

/**
 * Displays a protractor and ruler when the mouse cursor is locked
 * via the middle mouse button.
 *
 * @author ulman
 */
public class MeasurementPainter extends GlimpseDataPainter2D
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
    public void dispose( GLContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;
    }

    @Override
    public void paintTo( GL2 gl, GlimpseBounds bounds, Axis2D axis )
    {
        if ( textRenderer == null ) return;

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        boolean lockedX = axisX.isSelectionLocked( );
        boolean lockedY = axisY.isSelectionLocked( );

        if ( !lockedX && !lockedY ) return;

        double lockX = axisX.getSelectionCenter( );
        double lockY = axisY.getSelectionCenter( );

        double mouseX = axisX.getMouseValue( );
        double mouseY = axisY.getMouseValue( );

        double diffX = mouseX - lockX;
        double diffY = mouseY - lockY;

        double distance = Math.sqrt( diffX * diffX + diffY * diffY );

        double angle = Math.atan2( mouseY - lockY, mouseX - lockX );
        int sign = angle < 0 ? -1 : 1;
        double step = RADIANS_PER_VERTEX;
        double radius = distance * ANGLE_WEDGE_RADIUS_FRACTION;

        //// draw ruler ////
        gl.glLineWidth( rulerWidth );
        gl.glColor4fv( rulerColor, 0 );

        gl.glBegin( GL2.GL_LINES );
        try
        {
            gl.glVertex2d( lockX, lockY );
            gl.glVertex2d( mouseX, mouseY );
        }
        finally
        {
            gl.glEnd( );
        }

        //// draw protractor ////
        gl.glColor4fv( protractorColor, 0 );

        gl.glBegin( GL2.GL_TRIANGLE_FAN );
        try
        {
            gl.glVertex2d( lockX, lockY );

            for ( double a = 0; a < angle * sign; a += step )
            {
                double x = lockX + Math.cos( a * sign ) * radius;
                double y = lockY + Math.sin( a * sign ) * radius;
                gl.glVertex2d( x, y );
            }

            double x = lockX + Math.cos( angle ) * radius;
            double y = lockY + Math.sin( angle ) * radius;
            gl.glVertex2d( x, y );
        }
        finally
        {
            gl.glEnd( );
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

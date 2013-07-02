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
package com.metsci.glimpse.axis.painter;

import static com.metsci.glimpse.support.font.FontUtils.getDefaultPlain;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.axis.painter.label.GridAxisExponentLabelHandler;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.sun.opengl.util.j2d.TextRenderer;

/**
 * A floating axis plot with axes, tick marks, and labels drawn along
 * the x and y origins. If the axes are translated such that the x or
 * y origin is not visible, the tick marks will stick along the top
 * or bottom of the screen as appropriate.
 *
 * @author ulman
 * @see com.metsci.glimpse.examples.basic.FunctionPlotExample
 */
public class NumericXYAxisPainter extends GlimpsePainter2D
{
    protected TextRenderer textRenderer;
    protected Font font;
    
    protected volatile Font newFont = null;
    protected volatile boolean antialias = false;

    protected float[] lineColor;
    protected float[] textColor;

    protected int tickWidth = 5;
    protected int labelBuffer = 4;

    // buffer in pixels before labels switch to below/left of the axes
    protected int rightBuffer = 60;
    protected int topBuffer = 60;

    protected AxisLabelHandler ticksX;
    protected AxisLabelHandler ticksY;

    protected boolean showHorizontal = true;
    protected boolean showVertical = true;
    protected boolean showLabelsNearOrigin = false;
    protected boolean showOrigin = true;
    protected boolean showZero = false;

    protected boolean lockLeft = false;
    protected boolean lockRight = false;
    protected boolean lockTop = false;
    protected boolean lockBottom = false;
    
    protected boolean fontSet = false;
    protected boolean tickColorSet = false;
    protected boolean labelColorSet = false;
    
    public NumericXYAxisPainter( AxisLabelHandler ticksX, AxisLabelHandler ticksY )
    {
        this.ticksX = ticksX;
        this.ticksY = ticksY;

        this.setFont( getDefaultPlain( 12 ), false );

        this.textColor = GlimpseColor.getBlack( );
        this.lineColor = GlimpseColor.getBlack( );
    }
    
    public NumericXYAxisPainter( )
    {
        this( new GridAxisExponentLabelHandler( ), new GridAxisExponentLabelHandler( ) );
    }

    public void setFont( Font font )
    {
        setFont( font, true );
    }

    public NumericXYAxisPainter setFont( Font font, boolean antialias )
    {
        this.newFont = font;
        this.antialias = antialias;
        this.fontSet = true;
        return this;
    }

    public NumericXYAxisPainter setTextColor( float[] rgba )
    {
        this.textColor = rgba;
        this.labelColorSet = true;
        return this;
    }

    public NumericXYAxisPainter setLineColor( float r, float g, float b, float a )
    {
        lineColor[0] = r;
        lineColor[1] = g;
        lineColor[2] = b;
        lineColor[3] = a;

        this.tickColorSet = true;
        
        return this;
    }

    public NumericXYAxisPainter setLockLeft( boolean lock )
    {
        this.lockLeft = lock;
        return this;
    }

    public NumericXYAxisPainter setLockRight( boolean lock )
    {
        this.lockRight = lock;
        return this;
    }

    public NumericXYAxisPainter setLockBottom( boolean lock )
    {
        this.lockBottom = lock;
        return this;
    }

    public NumericXYAxisPainter setLockTop( boolean lock )
    {
        this.lockTop = lock;
        return this;
    }

    public NumericXYAxisPainter setShowOriginLabel( boolean show )
    {
        this.showZero = show;
        return this;
    }

    public NumericXYAxisPainter setLineColor( float[] rgba )
    {
        this.lineColor = rgba;
        this.tickColorSet = true;
        return this;
    }

    public NumericXYAxisPainter setShowVerticalTicks( boolean show )
    {
        this.showVertical = show;
        return this;
    }

    public NumericXYAxisPainter setShowHorizontalTicks( boolean show )
    {
        this.showHorizontal = show;
        return this;
    }

    public NumericXYAxisPainter setShowLabelsNearOrigin( boolean show )
    {
        this.showLabelsNearOrigin = show;
        return this;
    }

    public NumericXYAxisPainter setShowOriginLines( boolean show )
    {
        this.showOrigin = show;
        return this;
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
    {
        if ( this.newFont != null )
        {
            if ( this.textRenderer != null ) this.textRenderer.dispose( );
            this.textRenderer = new TextRenderer( this.newFont, this.antialias, false );
            this.newFont = null;
        }
        
        if ( this.textRenderer == null ) return;
        
        GL gl = context.getGL( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        double[] positionsX = ticksX.getTickPositions( axis.getAxisX( ) );
        double[] positionsY = ticksY.getTickPositions( axis.getAxisY( ) );

        AxisUnitConverter convX = ticksX.getAxisUnitConverter( );
        convX = convX == null ? AxisUnitConverters.identity : convX;

        AxisUnitConverter convY = ticksY.getAxisUnitConverter( );
        convY = convY == null ? AxisUnitConverters.identity : convY;

        // a small half pixel fudge-factor to make things look good
        double onePixelX = 0.5 / axisX.getPixelsPerValue( );
        double onePixelY = 0.5 / axisY.getPixelsPerValue( );

        int originY = axisY.valueToScreenPixel( 0.0 );
        if ( originY < 0 || lockBottom ) originY = 0;
        if ( originY > height || lockTop ) originY = height;

        int originX = axisX.valueToScreenPixel( 0.0 );
        if ( originX < 0 || lockLeft ) originX = 0;
        if ( originX > width || lockRight ) originX = width;

        boolean rightCornerX = false;
        boolean leftCornerX = false;
        boolean topCornerY = false;
        boolean bottomCornerY = false;

        double doriginX = 0.0;
        if ( doriginX <= axisX.getMin( ) || lockLeft )
        {
            doriginX = axisX.getMin( ) + onePixelX;
            leftCornerX = true;
        }

        if ( doriginX > axisX.getMax( ) || lockRight )
        {
            doriginX = axisX.getMax( );
            rightCornerX = true;
        }

        double doriginY = 0.0;
        if ( doriginY <= axisY.getMin( ) || lockBottom )
        {
            doriginY = axisY.getMin( ) + onePixelY;
            topCornerY = true;
        }

        if ( doriginY > axisY.getMax( ) || lockTop )
        {
            doriginY = axisY.getMax( );
            bottomCornerY = true;
        }

        boolean labelRight = width - originX > rightBuffer;
        boolean labelTop = height - originY > topBuffer;

        boolean[] paintLabelsX = new boolean[ positionsX.length ];
        boolean[] paintLabelsY = new boolean[ positionsY.length ];
        
        GlimpseColor.setColor( textRenderer, textColor );
        textRenderer.beginRendering( width, height );
        try
        {
            if ( showHorizontal )
            {
                String[] labelsX = ticksX.getTickLabels( axis.getAxisX( ), positionsX );

                // the y offset of the x axis labels is different depending on whether
                // the labels are being drawn above or below the axis
                int offsetY;
                if ( labelTop )
                {
                    offsetY = tickWidth + labelBuffer;
                }
                else
                {
                    Rectangle2D textBounds = textRenderer.getBounds( labelsX[0] );
                    offsetY = ( int ) - ( textBounds.getHeight( ) + tickWidth + labelBuffer );
                }

                for ( int i = 0; i < positionsX.length; i++ )
                {
                    String label = labelsX[i];
                    double valueX = positionsX[i];

                    if ( valueX == 0.0 && !showZero ) continue;

                    int posX = axisX.valueToScreenPixel( valueX );

                    paintLabelsX[i] = shouldPaintLabel( rightCornerX, leftCornerX, posX, width, rightBuffer );
                    if ( paintLabelsX[i] ) textRenderer.draw( label, posX + labelBuffer, originY + offsetY );
                }
            }

            if ( showVertical )
            {
                String[] labelsY = ticksY.getTickLabels( axis.getAxisY( ), positionsY );

                for ( int i = 0; i < positionsY.length; i++ )
                {
                    String label = labelsY[i];
                    Rectangle2D textBounds = textRenderer.getBounds( label );

                    double valueY = positionsY[i];

                    if ( valueY == 0.0 && !showZero ) continue;

                    // the x offset of the y axis labels is different depending on whether
                    // the labels are being drawn to the right or left of the axis
                    int offsetX;
                    if ( labelRight )
                    {
                        offsetX = tickWidth + labelBuffer;
                    }
                    else
                    {
                        offsetX = ( int ) - ( textBounds.getWidth( ) + tickWidth + labelBuffer );
                    }

                    int posY = axisY.valueToScreenPixel( valueY );

                    paintLabelsY[i] = shouldPaintLabel( bottomCornerY, topCornerY, posY, height, topBuffer );
                    if ( paintLabelsY[i] ) textRenderer.draw( label, originX + offsetX, posY + labelBuffer );
                }
            }
        }
        finally
        {
            textRenderer.endRendering( );
        }

        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( axis.getMinX( ), axis.getMaxX( ), axis.getMinY( ), axis.getMaxY( ), -1, 1 );
        
        GlimpseColor.glColor( gl, lineColor );
        
        double labelBufferX = labelBuffer / axisX.getPixelsPerValue( );
        double labelBufferY = labelBuffer / axisY.getPixelsPerValue( );
        
        gl.glBegin( GL.GL_LINES );
        try
        {
            if ( showHorizontal )
            {
                double tickWidthY = tickWidth / axisY.getPixelsPerValue( );

                for ( int i = 0; i < positionsX.length; i++ )
                {
                    if ( paintLabelsX[i] )
                    {
                        double valueX = convX.fromAxisUnits( positionsX[i] );
    
                        gl.glVertex2d( valueX, doriginY - tickWidthY );
                        gl.glVertex2d( valueX, doriginY + tickWidthY );
    
                        if ( labelTop )
                        {
                            gl.glVertex2d( valueX, doriginY + tickWidthY );
                            gl.glVertex2d( valueX + labelBufferX, doriginY + tickWidthY + labelBufferY );
                        }
                        else
                        {
                            gl.glVertex2d( valueX, doriginY - tickWidthY );
                            gl.glVertex2d( valueX + labelBufferX, doriginY - tickWidthY - labelBufferY );
                        }
                    }
                }
            }

            if ( showVertical )
            {
                double tickWidthX = tickWidth / axisX.getPixelsPerValue( );

                for ( int i = 0; i < positionsY.length; i++ )
                {
                    if ( paintLabelsY[i] )
                    {
                        double valueY = convY.fromAxisUnits( positionsY[i] );
    
                        gl.glVertex2d( doriginX - tickWidthX, valueY );
                        gl.glVertex2d( doriginX + tickWidthX, valueY );
    
                        if ( labelRight )
                        {
                            gl.glVertex2d( doriginX + tickWidthX, valueY );
                            gl.glVertex2d( doriginX + tickWidthX + labelBufferX, valueY + labelBufferY );
                        }
                        else
                        {
                            gl.glVertex2d( doriginX - tickWidthX, valueY );
                            gl.glVertex2d( doriginX - tickWidthX - labelBufferX, valueY + labelBufferY );
                        }
                    }
                }
            }
            
            if ( showHorizontal && showOrigin )
            {
                gl.glVertex2d( convX.fromAxisUnits( axis.getMinX( ) ), convY.fromAxisUnits( doriginY ) );
                gl.glVertex2d( convX.fromAxisUnits( axis.getMaxX( ) ), convY.fromAxisUnits( doriginY ) );
            }
    
            if ( showVertical && showOrigin )
            {
                gl.glVertex2d( convX.fromAxisUnits( doriginX ), convY.fromAxisUnits( axis.getMinY( ) ) );
                gl.glVertex2d( convX.fromAxisUnits( doriginX ), convY.fromAxisUnits( axis.getMaxY( ) ) );
            }
        }
        finally
        {
            gl.glEnd( );
        }
    }

    protected boolean shouldPaintLabel( boolean atMinCorner, boolean atMaxCorner, int labelPos, int axisSize, int buffer )
    {
        return ( showLabelsNearOrigin || ( ( !atMaxCorner || labelPos > buffer ) && ( !atMinCorner || labelPos < axisSize - buffer ) ) );
    }
    
    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // ignore the look and feel if a font has been manually set
        if ( !fontSet )
        {
            setFont( laf.getFont( AbstractLookAndFeel.AXIS_FONT ), false );
            fontSet = false;
        }
        
        if ( !tickColorSet )
        {
            setLineColor( laf.getColor( AbstractLookAndFeel.AXIS_TICK_COLOR ) );
            tickColorSet = false;
        }
        
        if ( !labelColorSet )
        {
            setTextColor( laf.getColor( AbstractLookAndFeel.AXIS_TEXT_COLOR ) );
            labelColorSet = false;
        }
    }

    @Override
    public void dispose( GLContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;
    }
}

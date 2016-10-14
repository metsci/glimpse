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
package com.metsci.glimpse.axis.painter;

import static com.metsci.glimpse.support.font.FontUtils.*;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.axis.painter.label.GridAxisExponentLabelHandler;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;

/**
 * A floating axis plot with axes, tick marks, and labels drawn along
 * the x and y origins. If the axes are translated such that the x or
 * y origin is not visible, the tick marks will stick along the top
 * or bottom of the screen as appropriate.
 *
 * @author ulman
 * @see com.metsci.glimpse.examples.basic.FunctionPlotExample
 */
public class NumericXYAxisPainter extends GlimpsePainterBase
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

    protected LinePath pathTick;
    protected LinePath pathLeader;
    protected LineStyle style;
    protected LineProgram prog;

    public NumericXYAxisPainter( AxisLabelHandler ticksX, AxisLabelHandler ticksY )
    {
        this.ticksX = ticksX;
        this.ticksY = ticksY;

        this.setFont( getDefaultPlain( 12 ), false );

        this.textColor = GlimpseColor.getBlack( );
        this.lineColor = GlimpseColor.getBlack( );

        this.initLineShader( );
    }

    public NumericXYAxisPainter( )
    {
        this( new GridAxisExponentLabelHandler( ), new GridAxisExponentLabelHandler( ) );
    }

    protected void initLineShader( )
    {
        this.pathTick = new LinePath( );
        this.pathLeader = new LinePath( );

        this.style = new LineStyle( );
        this.style.stippleEnable = false;

        this.prog = new LineProgram( );
    }

    public void setLabelHandlerX( AxisLabelHandler ticksX )
    {
        this.ticksX = ticksX;
    }

    public void setLabelHandlerY( AxisLabelHandler ticksY )
    {
        this.ticksY = ticksY;
    }

    public AxisLabelHandler getLabelHandlerX( )
    {
        return this.ticksX;
    }

    public AxisLabelHandler getLabelHandlerY( )
    {
        return this.ticksY;
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
        this.lineColor[0] = r;
        this.lineColor[1] = g;
        this.lineColor[2] = b;
        this.lineColor[3] = a;

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
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = requireAxis2D( context );
        GL3 gl = context.getGL( ).getGL3( );

        GLUtils.enableStandardBlending( gl );
        try
        {
            if ( this.newFont != null )
            {
                if ( this.textRenderer != null ) this.textRenderer.dispose( );
                this.textRenderer = new TextRenderer( this.newFont, this.antialias, false );
                this.newFont = null;
            }

            if ( this.textRenderer == null ) return;

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

            double doriginX;
            if ( lockLeft )
            {
                doriginX = 0;
                leftCornerX = true;
            }
            else if ( lockRight )
            {
                doriginX = 1.0;
                rightCornerX = true;
            }
            else if ( 0.0 <= axisX.getMin( ) )
            {
                doriginX = 0;
                leftCornerX = true;
            }
            else if ( 0.0 > axisX.getMax( ) )
            {
                doriginX = 1.0;
                rightCornerX = true;
            }
            else
            {
                doriginX = axisX.valueToScreenPixelUnits( 0.0 ) / width;
            }

            double doriginY;
            if ( lockBottom )
            {
                doriginY = 0;
                topCornerY = true;
            }
            else if ( lockTop )
            {
                doriginY = 1.0;
                bottomCornerY = true;
            }
            else if ( 0.0 <= axisY.getMin( ) )
            {
                doriginY = 0;
                topCornerY = true;
            }
            else if ( 0.0 > axisY.getMax( ) )
            {
                doriginY = 1.0;
                bottomCornerY = true;
            }
            else
            {
                doriginY = axisY.valueToScreenPixelUnits( 0.0 ) / height;
            }

            boolean labelRight = width - originX > rightBuffer;
            boolean labelTop = height - originY > topBuffer;

            boolean[] paintLabelsX = new boolean[positionsX.length];
            boolean[] paintLabelsY = new boolean[positionsY.length];

            this.textRenderer.beginRendering( width, height );
            try
            {
                GlimpseColor.setColor( textRenderer, textColor );

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
                        double valueX = convX.fromAxisUnits( positionsX[i] );

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

                        double valueY = convY.fromAxisUnits( positionsY[i] );

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
                this.textRenderer.endRendering( );
            }

            double labelBufferX = labelBuffer / ( double ) width;
            double labelBufferY = labelBuffer / ( double ) height;

            prog.begin( gl );
            try
            {
                pathLeader.clear( );
                pathTick.clear( );
                style.rgba = lineColor;

                if ( showHorizontal )
                {
                    double tickWidthY = tickWidth / ( double ) height;

                    for ( int i = 0; i < positionsX.length; i++ )
                    {
                        if ( paintLabelsX[i] )
                        {
                            double valueX = axis.getAxisX( ).valueToScreenPixelUnits( convX.fromAxisUnits( positionsX[i] ) ) / width;

                            pathTick.moveTo( ( float ) valueX, ( float ) ( doriginY - tickWidthY ) );
                            pathTick.lineTo( ( float ) valueX, ( float ) ( doriginY + tickWidthY ) );

                            if ( labelTop )
                            {
                                pathLeader.moveTo( ( float ) valueX, ( float ) ( doriginY + tickWidthY ) );
                                pathLeader.lineTo( ( float ) ( valueX + labelBufferX ), ( float ) ( doriginY + tickWidthY + labelBufferY ) );
                            }
                            else
                            {
                                pathLeader.moveTo( ( float ) valueX, ( float ) ( doriginY - tickWidthY ) );
                                pathLeader.lineTo( ( float ) ( valueX + labelBufferX ), ( float ) ( doriginY - tickWidthY - labelBufferY ) );
                            }
                        }
                    }
                }

                if ( showVertical )
                {
                    double tickWidthX = tickWidth / ( double ) width;

                    for ( int i = 0; i < positionsY.length; i++ )
                    {
                        if ( paintLabelsY[i] )
                        {
                            double valueY = axis.getAxisY( ).valueToScreenPixelUnits( convY.fromAxisUnits( positionsY[i] ) ) / height;

                            pathTick.moveTo( ( float ) ( doriginX - tickWidthX ), ( float ) valueY );
                            pathTick.lineTo( ( float ) ( doriginX + tickWidthX ), ( float ) valueY );

                            if ( labelRight )
                            {
                                pathLeader.moveTo( ( float ) ( doriginX + tickWidthX ), ( float ) valueY );
                                pathLeader.lineTo( ( float ) ( doriginX + tickWidthX + labelBufferX ), ( float ) ( valueY + labelBufferY ) );
                            }
                            else
                            {
                                pathLeader.moveTo( ( float ) ( doriginX - tickWidthX ), ( float ) valueY );
                                pathLeader.lineTo( ( float ) ( doriginX - tickWidthX - labelBufferX ), ( float ) ( valueY + labelBufferY ) );
                            }
                        }
                    }
                }

                if ( showHorizontal && showOrigin )
                {
                    pathTick.moveTo( 0f, ( float ) doriginY );
                    pathTick.lineTo( 1f, ( float ) doriginY );
                }

                if ( showVertical && showOrigin )
                {
                    pathTick.moveTo( ( float ) doriginX, 0f );
                    pathTick.lineTo( ( float ) doriginX, 1f );
                }

                prog.setViewport( gl, bounds );
                prog.setOrtho( gl, 0, 1, 0, 1 );

                // feathering makes horizontal/vertical lines look bad, so draw them separately

                style.feather_PX = 0.5f;
                prog.draw( gl, style, pathLeader );

                style.feather_PX = 0f;
                prog.draw( gl, style, pathTick );
            }
            finally
            {
                prog.end( gl );
            }
        }
        finally
        {
            GLUtils.disableBlending( gl );
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
    public void doDispose( GlimpseContext context )
    {
        if ( textRenderer != null ) textRenderer.dispose( );
        textRenderer = null;

        prog.dispose( context.getGL( ).getGL3( ) );
        pathTick.dispose( context.getGL( ) );
        pathLeader.dispose( context.getGL( ) );
    }
}

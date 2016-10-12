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

import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.jogamp.opengl.math.Matrix4;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.shader.line.LineJoinType;
import com.metsci.glimpse.support.shader.line.LinePath;
import com.metsci.glimpse.support.shader.line.LineStyle;

public class NumericPolarAxisPainter extends NumericXYAxisPainter
{
    private static final double TWO_PI = 2d * Math.PI;
    private static final double PI_2 = Math.PI / 2.0;

    // Padding between labels and axis lines
    private int textPadding = 2;

    // Number of sectors to draw when there's a full circle shown
    private int nSectors = 12;

    private float lineWidth = 1.0f;

    private float maxRadius = 0;

    private int radialStippleFactor = 1;
    private short radialStipplePattern = 0;
    private int circleStippleFactor = 1;
    private short circleStipplePattern = 0;
    private float[] insideTextColor = null;
    private float[] outsideTextColor = null;

    protected LinePath pathRadial;
    protected LinePath pathCircle;

    protected LineStyle styleRadial;
    protected LineStyle styleCircle;

    protected Matrix4 transformMatrix;

    public NumericPolarAxisPainter( )
    {
        this.pathRadial = new LinePath( );
        this.pathCircle = new LinePath( );

        this.styleRadial = new LineStyle( );
        this.styleRadial.thickness_PX = lineWidth;
        this.styleRadial.rgba = GlimpseColor.getBlack( );

        this.styleCircle = new LineStyle( );
        this.styleCircle.thickness_PX = lineWidth;
        this.styleCircle.joinType = LineJoinType.JOIN_NONE;
        this.styleCircle.rgba = GlimpseColor.getBlack( );

        this.transformMatrix = new Matrix4( );
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        if ( this.newFont != null )
        {
            if ( this.textRenderer != null ) this.textRenderer.dispose( );
            this.textRenderer = new TextRenderer( this.newFont, this.antialias, false );
            this.newFont = null;
        }

        if ( this.textRenderer == null ) return;

        GL3 gl = context.getGL( ).getGL3( );
        Axis2D axis = requireAxis2D( context );
        GlimpseBounds bounds = getBounds( context );

        int height = bounds.getHeight( );
        int width = bounds.getWidth( );

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        double xMin = axisX.getMin( );
        double xMax = axisX.getMax( );
        double yMin = axisY.getMin( );
        double yMax = axisY.getMax( );

        // maximum distance is to the point with highest absolute value in each dimension
        double xFar = Math.abs( xMin ) > Math.abs( xMax ) ? xMin : xMax;
        double yFar = Math.abs( yMin ) > Math.abs( yMax ) ? yMin : yMax;
        double maxDist = Math.sqrt( xFar * xFar + yFar * yFar ); // hypot is a lot slower

        boolean yCross = xMin * xMax < 0; // viewport crosses y axis
        boolean xCross = yMin * yMax < 0; // viewport crosses x axis
        boolean originVisible = yCross && xCross;

        double minDist = 0;
        if ( originVisible )
        {
            // minimum distance is zero if the origin is visible
        }
        else if ( yCross )
        {
            // min dist = y dist alone
            minDist = Math.abs( yMin ) < Math.abs( yMax ) ? Math.abs( yMin ) : Math.abs( yMax );
        }
        else if ( xCross )
        {
            // min dist = x dist alone
            minDist = Math.abs( xMin ) < Math.abs( xMax ) ? Math.abs( xMin ) : Math.abs( xMax );
        }
        else
        {
            double nearY = Math.abs( yMin ) < Math.abs( yMax ) ? Math.abs( yMin ) : Math.abs( yMax );
            double nearX = Math.abs( xMin ) < Math.abs( xMax ) ? Math.abs( xMin ) : Math.abs( xMax );
            minDist = Math.sqrt( nearX * nearX + nearY * nearY );
        }

        if ( maxRadius > 0 && maxDist > maxRadius )
        {
            maxDist = maxRadius;
        }

        double minTheta = 0;
        double maxTheta = TWO_PI;

        if ( !originVisible )
        {
            minTheta = Math.atan2( yMin, xMin );
            // This condition implies we can see parts of (only) quadrants II and III, and the
            // angle discontinuity on the negative x axis is a problem.
            if ( xCross && minTheta < -Math.PI / 2 ) minTheta += TWO_PI;
            maxTheta = minTheta;

            double theta = Math.atan2( yMin, xMax );
            if ( xCross && theta < -Math.PI / 2 ) theta += TWO_PI;
            minTheta = theta < minTheta ? theta : minTheta;
            maxTheta = theta > maxTheta ? theta : maxTheta;

            theta = Math.atan2( yMax, xMax );
            if ( xCross && theta < -Math.PI / 2 ) theta += TWO_PI;
            minTheta = theta < minTheta ? theta : minTheta;
            maxTheta = theta > maxTheta ? theta : maxTheta;

            theta = Math.atan2( yMax, xMin );
            if ( xCross && theta < -Math.PI / 2 ) theta += TWO_PI;
            minTheta = theta < minTheta ? theta : minTheta;
            maxTheta = theta > maxTheta ? theta : maxTheta;
        }

        // Create an Axis1D for ranges so we can use existing heuristics
        // for tick picking
        Axis1D xAxis = new Axis1D( );
        xAxis.setMin( minDist );
        xAxis.setMax( maxDist );

        if ( minDist == 0 && maxRadius > 0 )
        {
            xAxis.setSizePixels( ( width > height ? width : height ) / 2 );
        }
        else
        {
            xAxis.setSizePixels( width > height ? width : height );
        }

        double[] positionsX = ticksX.getTickPositions( xAxis );

        /*
         * Different heuristic for angles, because the axis size is difficult to
         * define in a way that gets good results.  The idea here is always have 30 degree
         * intervals when the origin is visible, and approximately nSectors intervals when not.
         * Subdivision is done by repeated halving of the original 30 degree intervals.
         */
        double approxInterval = ( maxTheta - minTheta ) / nSectors;
        double intervalRatio = 2 * Math.PI / ( maxTheta - minTheta );
        double exactInterval = Math.PI / 6;
        long minIndex = 0;
        long maxIndex = 11;

        if ( !originVisible )
        {
            long nSplits = Math.round( Math.log( approxInterval ) / Math.log( 2 ) );
            nSplits = ( long ) Math.floor( Math.log( intervalRatio ) / Math.log( 2 ) );
            exactInterval = Math.PI / 6 / Math.pow( 2, nSplits );
            minIndex = Math.round( minTheta / exactInterval );
            maxIndex = Math.round( maxTheta / exactInterval );
        }

        double[] positionsY = new double[( int ) ( maxIndex - minIndex + 1 )];
        for ( int ii = 0; ii < positionsY.length; ++ii )
        {
            positionsY[ii] = ( minIndex + ii ) * exactInterval;
        }

        AxisUnitConverter convX = ticksX.getAxisUnitConverter( );
        convX = convX == null ? AxisUnitConverters.identity : convX;

        AxisUnitConverter convY = AxisUnitConverters.suShownAsNavigationDegrees;

        GLUtils.enableStandardBlending( gl );
        prog.begin( gl );
        try
        {
            prog.setViewport( gl, bounds );
            prog.setAxisOrtho( gl, axis );

            if ( showHorizontal )
            {
                if ( radialStipplePattern > 0 )
                {
                    styleRadial.stippleEnable = true;
                    styleRadial.stipplePattern = radialStipplePattern;
                    styleRadial.stippleScale = radialStippleFactor;
                }
                else
                {
                    styleRadial.stippleEnable = false;
                }

                pathRadial.clear( );

                for ( int ii = 0; ii < positionsY.length; ii++ )
                {
                    pathRadial.moveTo( 0, 0 );
                    pathRadial.lineTo( ( float ) ( Math.cos( positionsY[ii] ) * maxDist ), ( float ) ( Math.sin( positionsY[ii] ) * maxDist ) );
                }

                prog.draw( gl, style, pathRadial );
            }

            if ( showVertical )
            {
                if ( radialStipplePattern > 0 )
                {
                    styleRadial.stippleEnable = true;
                    styleRadial.stipplePattern = radialStipplePattern;
                    styleRadial.stippleScale = radialStippleFactor;
                }
                else
                {
                    styleRadial.stippleEnable = false;
                }

                pathCircle.clear( );

                for ( int ii = 0; ii < positionsX.length; ii++ )
                {
                    calculateCircleVertices( gl, pathCircle, 0f, 0f, ( float ) positionsX[ii], 360 );
                }

                prog.draw( gl, styleCircle, pathCircle );
            }
        }
        finally
        {
            prog.end( gl );
            gl.glDisable( GL.GL_BLEND );
        }

        textRenderer.begin3DRendering( );
        try
        {
            // Draw Labels
            GlimpseColor.setColor( textRenderer, insideTextColor == null ? textColor : insideTextColor );

            /* first - label range rings:
             * If x-axis is visible, label all intersections with that axis
             * otherwise, if y-axis is visible, label all intersections with that axis
             * otherwise, label on the middle radial
             */
            for ( int ii = 1; ii < positionsX.length; ++ii )
            {
                String label = String.format( "%.3g", convX.fromAxisUnits( positionsX[ii] ) );
                Rectangle2D textBounds = textRenderer.getBounds( label );
                if ( xCross ) // x-axis visible
                {
                    boolean labelAbove = yMax + yMin > 0;
                    int labelX = axisX.valueToScreenPixel( -positionsX[ii] );
                    int labelY = axisY.valueToScreenPixel( 0 ) + ( labelAbove ? 2 : ( int ) -textBounds.getHeight( ) );
                    textRenderer.draw( label, labelX, labelY );
                    labelX = axisX.valueToScreenPixel( positionsX[ii] ) - ( int ) textBounds.getWidth( ) - 1;
                    label = String.format( "%.3g", convX.fromAxisUnits( positionsX[ii] ) );
                    textRenderer.draw( label, labelX, labelY );
                }
                else if ( yCross ) // y-axis visible
                {
                    boolean labelRight = xMax + xMin > 0;
                    int labelX = axisX.valueToScreenPixel( 0 ) + ( labelRight ? 2 : ( int ) -textBounds.getWidth( ) );
                    int labelY = axisY.valueToScreenPixel( -positionsX[ii] );
                    textRenderer.draw( label, labelX, labelY );
                    labelY = axisY.valueToScreenPixel( positionsX[ii] ) - ( int ) textBounds.getHeight( );
                    textRenderer.draw( label, labelX, labelY );
                }
                else
                {
                    double theta = positionsY[positionsY.length / 2];
                    double r = positionsX[ii];
                    double xCoord = Math.cos( theta ) * r;
                    double yCoord = Math.sin( theta ) * r;
                    int labelX = axisX.valueToScreenPixel( xCoord );
                    int labelY = axisY.valueToScreenPixel( yCoord );
                    int xOffset = ( int ) -textBounds.getWidth( ) - textPadding;
                    int yOffset = textPadding;

                    if ( Math.cos( theta ) < 0 )
                    {
                        theta += Math.PI;
                        yOffset = ( int ) -textBounds.getHeight( ) - textPadding + 2;
                        xOffset = textPadding;
                    }

                    transformMatrix.loadIdentity( );
                    transformMatrix.makeOrtho( 0, width, 0, height, -1, 1 );
                    transformMatrix.translate( labelX, labelY, 0 );
                    transformMatrix.rotate( ( float ) theta, 0, 0, 1.0f );
                    textRenderer.setTransform( transformMatrix.getMatrix( ) );

                    textRenderer.draw3D( label, xOffset, yOffset, 0.0f, 1.0f );
                }
            }
        }
        finally
        {
            textRenderer.end3DRendering( );
        }

        /*
         * Now label bearing lines.  If room, do it outside the outermost circle (this requires a
         * maximum range), else where they intersect with the middle range ring
         */
        boolean labelOutside = false;

        if ( maxRadius > 0 )
        {
            Rectangle2D buffer = textRenderer.getBounds( "Test!" );
            double minX = axisX.screenPixelToValue( buffer.getHeight( ) + textPadding );
            double maxX = axisX.screenPixelToValue( axisX.getSizePixels( ) - 1 - buffer.getHeight( ) - textPadding );
            double minY = axisY.screenPixelToValue( buffer.getHeight( ) + textPadding );
            double maxY = axisY.screenPixelToValue( axisY.getSizePixels( ) - 1 - buffer.getHeight( ) - textPadding );

            labelOutside = ( minX < -maxRadius ) && ( maxX > maxRadius ) && ( minY < -maxRadius ) && ( maxY > maxRadius );
        }

        double r = labelOutside ? positionsX[positionsX.length - 1] : positionsX[positionsX.length / 2];

        textRenderer.begin3DRendering( );
        try
        {
            GlimpseColor.setColor( textRenderer, ( labelOutside && outsideTextColor != null ) ? outsideTextColor : textColor );

            for ( int ii = 0; ii < positionsY.length; ++ii )
            {
                double degrees = convY.fromAxisUnits( positionsY[ii] );
                int wholeDegrees = ( int ) Math.floor( degrees );
                int minutes = ( int ) Math.floor( ( degrees - wholeDegrees ) * 60 );
                String label = "";
                if ( minutes == 0 )
                {
                    label = String.format( "%d\u00B0", wholeDegrees );
                }
                else
                {
                    label = String.format( "%d\u00B0%d'", wholeDegrees, minutes );
                }

                Rectangle2D textBounds = textRenderer.getBounds( label );

                // position / rotation
                double xCoord = Math.cos( positionsY[ii] ) * r;
                double yCoord = Math.sin( positionsY[ii] ) * r;
                int labelX = axisX.valueToScreenPixel( xCoord );
                int labelY = axisY.valueToScreenPixel( yCoord );
                int xOffset = labelOutside ? ( int ) ( -textBounds.getWidth( ) / 2 ) : textPadding;
                int yOffset = textPadding;

                double angle = positionsY[ii];
                if ( Math.sin( angle ) < 0 )
                {
                    angle += PI_2;
                    yOffset = ( int ) -textBounds.getHeight( ) - textPadding + 2;
                    if ( !labelOutside )
                    {
                        xOffset = ( int ) -textBounds.getWidth( ) - textPadding;
                    }
                }
                else
                {
                    angle -= PI_2;
                }

                transformMatrix.loadIdentity( );
                transformMatrix.makeOrtho( 0, width, 0, height, -1, 1 );
                transformMatrix.translate( labelX, labelY, 0 );
                transformMatrix.rotate( ( float ) angle, 0, 0, 1.0f );
                textRenderer.setTransform( transformMatrix.getMatrix( ) );

                textRenderer.draw3D( label, xOffset, yOffset, 0.0f, 1.0f );
            }
        }
        finally
        {
            textRenderer.end3DRendering( );
        }
    }

    protected void calculateCircleVertices( GL gl, LinePath path, float cx, float cy, float r, int num_segments )
    {
        float start_x = 0;
        float start_y = 0;

        for ( int ii = 0; ii < num_segments; ii++ )
        {
            float theta = ( float ) ( TWO_PI * ii / num_segments );

            float x = ( float ) ( r * Math.cos( theta ) );
            float y = ( float ) ( r * Math.sin( theta ) );

            if ( ii == 0 )
            {
                start_x = x;
                start_y = y;

                path.moveTo( x + cx, y + cy );
            }
            else
            {
                path.lineTo( x + cx, y + cy );

            }
        }

        path.lineTo( start_x + cx, start_y + cy );
    }

    // Padding between labels and axis lines
    public int getTextPadding( )
    {
        return textPadding;
    }

    // Padding between labels and axis lines
    public void setTextPadding( int textPadding )
    {
        this.textPadding = textPadding;
    }

    public int getnSectors( )
    {
        return nSectors;
    }

    public void setnSectors( int nSectors )
    {
        this.nSectors = nSectors;
    }

    public float getLineWidth( )
    {
        return lineWidth;
    }

    public void setLineWidth( float lineWidth )
    {
        this.lineWidth = lineWidth;
    }

    public float getMaxRadius( )
    {
        return maxRadius;
    }

    public void setMaxRadius( float maxRadius )
    {
        this.maxRadius = maxRadius;
    }

    public int getRadialStippleFactor( )
    {
        return radialStippleFactor;
    }

    public void setRadialStippleFactor( int radialStippleFactor )
    {
        this.radialStippleFactor = radialStippleFactor;
    }

    public short getRadialStipplePattern( )
    {
        return radialStipplePattern;
    }

    public void setRadialStipplePattern( short radialStipplePattern )
    {
        this.radialStipplePattern = radialStipplePattern;
    }

    public int getCircleStippleFactor( )
    {
        return circleStippleFactor;
    }

    public void setCircleStippleFactor( int circleStippleFactor )
    {
        this.circleStippleFactor = circleStippleFactor;
    }

    public short getCircleStipplePattern( )
    {
        return circleStipplePattern;
    }

    public void setCircleStipplePattern( short circleStipplePattern )
    {
        this.circleStipplePattern = circleStipplePattern;
    }

    public float[] getInsideTextColor( )
    {
        return insideTextColor;
    }

    public void setInsideTextColor( float[] insideTextColor )
    {
        this.insideTextColor = insideTextColor;
    }

    public float[] getOutsideTextColor( )
    {
        return outsideTextColor;
    }

    public void setOutsideTextColor( float[] outsideTextColor )
    {
        this.outsideTextColor = outsideTextColor;
    }
}

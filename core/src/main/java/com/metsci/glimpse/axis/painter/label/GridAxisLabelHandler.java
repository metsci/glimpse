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
package com.metsci.glimpse.axis.painter.label;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.lang.Math.round;

import java.text.NumberFormat;

import com.metsci.glimpse.axis.Axis1D;

/**
 * The default AxisLabelHandler implementation. Provides a uniformly spaced grid
 * of axis tick marks based on the set tickSpacing. If axis values become very
 * large or very small, they are shifted and a magnitude indicator is added to the
 * axis label.<p>
 *
 * For example, an axis with min and max values of 0.001 and 0.006 might have tick
 * values displayed as "1.0" or "6.0" with "(x 1,000)" specified in the axis label.
 *
 * @author ulman
 */
public class GridAxisLabelHandler implements AxisLabelHandler
{
    protected int tickSpacing;

    protected int minorTickCount;

    protected NumberFormat tickNumberFormatter;
    protected NumberFormat orderNumberFormatter;

    protected String axisLabel = "";
    protected String axisUnits = "";
    protected String axisKiloUnits = "";
    protected String axisMilliUnits = "";

    protected AxisUnitConverter converter;

    public GridAxisLabelHandler( )
    {
        this.tickNumberFormatter = NumberFormat.getNumberInstance( );
        this.tickNumberFormatter.setGroupingUsed( false );

        this.orderNumberFormatter = NumberFormat.getNumberInstance( );
        this.orderNumberFormatter.setGroupingUsed( true );

        this.converter = AxisUnitConverters.identity;

        this.tickSpacing = 100;
        this.minorTickCount = 4;
    }

    @Override
    public AxisUnitConverter getAxisUnitConverter( )
    {
        return this.converter;
    }

    @Override
    public void setAxisUnitConverter( AxisUnitConverter converter )
    {
        this.converter = converter;
    }

    @Override
    public double[] getTickPositions( Axis1D axis )
    {
        if ( axis.getSizePixels( ) == 0 ) return new double[0];
        return tickPositions( axis, tickInterval( axis ) );
    }

    @Override
    public String[] getTickLabels( Axis1D axis, double[] tickPositions )
    {
        double tickInterval = tickInterval( axis );
        int orderAxis = getOrderAxis( axis );
        int orderTick = getOrderTick( tickInterval );
        updateFormatter( orderAxis, orderTick );

        String[] tickLabels = new String[tickPositions.length];
        for ( int i = 0; i < tickPositions.length; i++ )
        {
            tickLabels[i] = tickString( axis, tickPositions[i], orderAxis );
        }

        return tickLabels;
    }

    @Override
    public double[] getMinorTickPositions( double[] tickPositions )
    {
        if ( tickPositions.length < 2 ) return new double[0];

        // assume all the ticks are evenly spaced
        double start = tickPositions[0];
        double end = tickPositions[1];
        double step = ( end - start ) / ( minorTickCount + 1 );

        int minorIndex = 0;
        double[] minorTickPositions = new double[ ( tickPositions.length + 1 ) * minorTickCount];

        for ( int i = 0; i < tickPositions.length; i++ )
        {
            start = tickPositions[i];

            for ( int j = 1; j < minorTickCount + 1; j++ )
            {
                minorTickPositions[minorIndex++] = start - step * j;
            }
        }

        start = tickPositions[tickPositions.length - 1];

        for ( int j = 1; j < minorTickCount + 1; j++ )
        {
            minorTickPositions[minorIndex++] = start + step * j;
        }

        return minorTickPositions;
    }

    @Override
    public String getAxisLabel( Axis1D axis )
    {
        return axisLabel( getOrderAxis( axis ) );
    }

    public void setTickSpacing( int spacing )
    {
        this.tickSpacing = spacing;
    }

    public void setMinorTickCount( int count )
    {
        this.minorTickCount = count;
    }

    @Override
    public void setAxisLabel( String label )
    {
        this.axisLabel = label;
    }

    public String getAxisLabel( )
    {
        return this.axisLabel;
    }

    public void setAxisUnits( String units, boolean abbreviated )
    {
        if ( abbreviated )
        {
            this.setAxisUnits( "m" + units, units, "k" + units );
        }
        else
        {
            this.setAxisUnits( "milli" + units, units, "kilo" + units );
        }
    }

    public void setAxisUnits( String milliUnits, String units, String kiloUnits )
    {
        this.axisMilliUnits = milliUnits;
        this.axisUnits = units;
        this.axisKiloUnits = kiloUnits;
    }

    public String getAxisUnits( )
    {
        return this.axisUnits;
    }

    protected String axisLabel( int orderX )
    {
        String pad = axisLabel.length( ) > 0 ? " " : "";

        if ( axisUnits.length( ) == 0 )
        {
            if ( orderX == 0 )
            {
                return axisLabel;
            }
            else if ( orderX == 3 )
            {
                return axisLabel + pad + "(x 1,000)";
            }
            else if ( orderX == -3 )
            {
                return axisLabel + pad + "(x 0.001)";
            }
            else
            {
                return axisLabel + pad + "(x 10^" + orderX + ")";
            }
        }

        if ( orderX == 0 )
        {
            return axisLabel + pad + "(" + axisUnits + ")";
        }
        else if ( orderX == 3 )
        {
            return axisLabel + pad + "(" + axisKiloUnits + ")";
        }
        else if ( orderX == -3 )
        {
            return axisLabel + pad + "(" + axisMilliUnits + ")";
        }
        else if ( orderX > 3 )
        {
            int labelPower = 1;
            while ( orderX > 3 )
            {
                labelPower *= 10;
                orderX--;
            }

            return axisLabel + pad + "(x " + orderNumberFormatter.format( labelPower ) + " " + axisKiloUnits + ")";
        }
        else
        {
            return axisLabel + pad + "(x 10^" + orderX + " " + axisUnits + ")";
        }
    }

    protected double tickInterval( Axis1D axis )
    {
        double approxNumTicks = ( double ) axis.getSizePixels( ) / ( double ) tickSpacing;
        return tickInterval( axis, approxNumTicks );
    }

    protected String tickString( Axis1D axis, double number, int orderAxis )
    {
        double axisFactor = Math.pow( 10, -orderAxis );
        return tickNumberFormatter.format( number * axisFactor );
    }

    protected double tickInterval( Axis1D axis, double approxNumTicks )
    {
        double calculatedMin = converter.toAxisUnits( axis.getMin( ) );
        double calculatedMax = converter.toAxisUnits( axis.getMax( ) );
        double min = Math.min( calculatedMin, calculatedMax );
        double max = Math.max( calculatedMin, calculatedMax );
        double approxTickInterval = ( max - min ) / approxNumTicks;
        double prelimTickInterval = pow( 10, round( log10( approxTickInterval ) ) );
        double prelimNumTicks = ( max - min ) / prelimTickInterval;

        if ( prelimNumTicks >= 5 * approxNumTicks ) return prelimTickInterval * 5;
        if ( prelimNumTicks >= 2 * approxNumTicks ) return prelimTickInterval * 2;

        if ( 5 * prelimNumTicks <= approxNumTicks ) return prelimTickInterval / 5;
        if ( 2 * prelimNumTicks <= approxNumTicks ) return prelimTickInterval / 2;

        return prelimTickInterval;
    }

    protected double[] tickPositions( Axis1D axis, double tickInterval )
    {
        double min = converter.toAxisUnits( axis.getMin( ) );
        double max = converter.toAxisUnits( axis.getMax( ) );

        double cacheMin = min;
        double cacheMax = max;
        if ( min >= max )
        {
            cacheMin = max;
            cacheMax = min;
        }

        double minTickNumber = floor( cacheMin / tickInterval );
        int tickCount = ( int ) ceil( ( cacheMax - cacheMin ) / tickInterval );

        double[] ticks = new double[tickCount + 1];
        for ( int i = 0; i < ticks.length; i++ )
            ticks[i] = ( i + minTickNumber ) * tickInterval;

        // handle case where axis min/max are reversed
        if ( min >= max )
        {
            int size = ticks.length;
            for ( int i = 0; i < size / 2; i++ )
            {
                double temp = ticks[i];
                ticks[i] = ticks[size - i - 1];
                ticks[size - i - 1] = temp;
            }
        }

        return ticks;
    }

    protected int getOrderTick( double d )
    {
        if ( d == 0 )
        {
            return 0;
        }

        double log10 = Math.log10( d );
        int order = ( int ) Math.floor( log10 );
        if ( ( log10 - order ) > ( 1.0 - 1e-12 ) ) order++;

        return order;
    }

    protected int getOrderAxis( Axis1D axis )
    {
        double min = converter.toAxisUnits( axis.getMin( ) );
        double max = converter.toAxisUnits( axis.getMax( ) );

        int orderX = getOrderTick( Math.abs( max - min ) );

        if ( orderX > 0 )
        {
            return 3 * ( ( orderX - 1 ) / 3 );
        }

        if ( orderX < 0 )
        {
            return 3 * ( orderX / 3 - 1 );
        }

        return 0;
    }

    protected void updateFormatter( int orderAxis, int orderTick )
    {
        tickNumberFormatter.setMaximumFractionDigits( orderAxis - orderTick );
    }
}

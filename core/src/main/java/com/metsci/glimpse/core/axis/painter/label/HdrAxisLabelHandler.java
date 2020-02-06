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
package com.metsci.glimpse.core.axis.painter.label;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.signum;
import static java.lang.String.format;

import com.metsci.glimpse.core.axis.Axis1D;

/**
 * High dynamic range axis label handler. The tick labels are always a fixed
 * width (modulo the negative sign) so zooming will never obfuscate the values.
 *
 * @author borkholder
 */
public class HdrAxisLabelHandler extends GridAxisLabelHandler
{
    protected boolean zoomedIn;
    protected int orderAxis;
    protected int orderDelta;
    protected double baseValue;

    protected String prefix;
    protected String suffix;

    public HdrAxisLabelHandler( )
    {
        prefix = "";
        suffix = "";
    }

    @Override
    public void setAxisLabel( String label )
    {
        super.setAxisLabel( label );

        if ( label == null || label.isEmpty( ) )
        {
            prefix = "";
        }
        else
        {
            prefix = label + " ";
        }
    }

    @Override
    public void setAxisUnits( String milliUnits, String units, String kiloUnits )
    {
        super.setAxisUnits( milliUnits, units, kiloUnits );

        if ( units == null || units.isEmpty( ) )
        {
            suffix = " " + units;
        }
        else
        {
            suffix = "";
        }
    }

    @Override
    public String getAxisLabel( Axis1D axis )
    {
        double min = converter.toAxisUnits( axis.getMin( ) );
        double max = converter.toAxisUnits( axis.getMax( ) );

        orderAxis = getOrderTick( abs( min ) );
        orderDelta = getOrderTick( abs( max - min ) );

        if ( signum( min ) != signum( max ) )
        {
            orderAxis = getOrderTick( max( abs( min ), abs( max ) ) );
        }

        zoomedIn = orderDelta <= orderAxis - 3 && signum( min ) == signum( max );

        orderAxis = ( int ) floor( orderAxis / 3.0 ) * 3;
        orderDelta = ( int ) ceil( orderDelta / 3.0 ) * 3;

        String s = null;
        if ( zoomedIn )
        {
            double e = pow( 10, orderDelta + 1 );
            baseValue = floor( min / e ) * e;
            e = pow( 10, orderDelta + 1 );
            baseValue = floor( min / e ) * e;

            if ( orderDelta == 0 )
            {
                s = format( "%,.0f + __", baseValue );
            }
            else
            {
                int precision = orderDelta < 0 ? -orderDelta : 1;
                s = format( "%,." + precision + "f + __ e%d", baseValue, orderDelta );
            }
        }
        else if ( orderAxis == 0 )
        {
            s = "";
        }
        else
        {
            s = format( "__ e%d", orderAxis );
        }

        if ( !prefix.isEmpty( ) || !suffix.isEmpty( ) )
        {
            s = prefix + s + suffix;
        }

        return s;
    }

    @Override
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
        {
            ticks[i] = ( i + minTickNumber ) * tickInterval;
        }

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

    @Override
    public String[] getTickLabels( Axis1D axis, double[] tickPositions )
    {
        String[] tickLabels = new String[tickPositions.length];
        for ( int i = 0; i < tickPositions.length; i++ )
        {
            double value = tickPositions[i];

            if ( zoomedIn )
            {
                value -= baseValue;
                value /= pow( 10, orderDelta );
            }
            else
            {
                value /= pow( 10, orderAxis );
            }

            String s = format( "%,.3f", value );
            if ( value < 0 )
            {
                s = s.substring( 0, 6 );
            }
            else
            {
                s = s.substring( 0, 5 );
            }

            tickLabels[i] = s;
        }

        return tickLabels;
    }
}

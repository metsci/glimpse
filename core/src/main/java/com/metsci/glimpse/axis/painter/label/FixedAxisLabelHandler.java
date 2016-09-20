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

import com.metsci.glimpse.axis.Axis1D;

import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;

/**
 * Draws pre-defined labels at pre-defined locations.
 *
 * @author borkholder
 */
public class FixedAxisLabelHandler implements AxisLabelHandler
{
    protected Double2ObjectSortedMap<String> labels;

    protected String axisLabel = "";

    protected AxisUnitConverter converter;

    public FixedAxisLabelHandler( )
    {
        setAxisUnitConverter( new AxisUnitConverter( )
        {
            @Override
            public double toAxisUnits( double value )
            {
                return value;
            }

            @Override
            public double fromAxisUnits( double value )
            {
                return value;
            }
        } );
    }

    @Override
    public double[] getTickPositions( Axis1D axis )
    {
        if ( labels == null )
        {
            return new double[0];
        }

        double min = getAxisUnitConverter( ).fromAxisUnits( axis.getMin( ) );
        double max = getAxisUnitConverter( ).fromAxisUnits( axis.getMax( ) );

        DoubleSortedSet subset = labels.keySet( ).subSet( min, max );
        return subset.toDoubleArray( );
    }

    @Override
    public String[] getTickLabels( Axis1D axis, double[] tickPositions )
    {
        if ( labels == null )
        {
            return new String[0];
        }

        String[] tickLabels = new String[tickPositions.length];
        for ( int i = 0; i < tickPositions.length; i++ )
        {
            String text = labels.get( tickPositions[i] );
            tickLabels[i] = text == null ? "" : text;
        }

        return tickLabels;
    }

    @Override
    public double[] getMinorTickPositions( double[] tickPositions )
    {
        return new double[0];
    }

    @Override
    public String getAxisLabel( Axis1D axis )
    {
        return axisLabel;
    }

    @Override
    public void setAxisLabel( String label )
    {
        axisLabel = label;
    }

    @Override
    public AxisUnitConverter getAxisUnitConverter( )
    {
        return converter;
    }

    @Override
    public void setAxisUnitConverter( AxisUnitConverter converter )
    {
        this.converter = converter;
    }

    public void clearLabels( )
    {
        setLabels( null );
    }

    public void setLabels( Double2ObjectSortedMap<String> labels )
    {
        this.labels = labels;
    }

    public void setLabels( double[] values, String[] labels )
    {
        if ( values.length != labels.length )
        {
            throw new IllegalArgumentException( "Number of values must match number of labels" );
        }

        Double2ObjectSortedMap<String> map = new Double2ObjectAVLTreeMap<String>( );
        for ( int i = 0; i < values.length; i++ )
        {
            map.put( values[i], labels[i] );
        }

        setLabels( map );
    }
}
package com.metsci.glimpse.axis.painter.label;

import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;

import com.metsci.glimpse.axis.Axis1D;

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
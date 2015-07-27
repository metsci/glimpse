package com.metsci.glimpse.axis.tagged;

import java.util.List;
import java.util.Map;

/**
 * A constraint which enforces the ordering of the provided constraints.
 * 
 * The first constraint in the provided list must have the smallest value, the
 * last constraint must have the largest value.
 * 
 * @author sindhwani
 */
public class OrderedConstraint extends NamedConstraint
{
    private List<String> constraintIds;
    private double buffer;

    public OrderedConstraint( String name, List<String> constraints )
    {
        this( name, 0.0, constraints );
    }

    public OrderedConstraint( String name, double buffer, List<String> constraints )
    {
        super( name );
        this.buffer = buffer;
        this.constraintIds = constraints;
    }

    @Override
    public void applyConstraint( TaggedAxis1D currentAxis, Map<String, Tag> previousTags )
    {
        String tagIndex = constraintIds.get( 0 );

        for ( int k = constraintIds.size( ) - 2; k >= 0; k-- )
        {
            String temp = constraintIds.get( k );
            if ( previousTags.get( temp ).getValue( ) < currentAxis.getTag( temp ).getValue( ) ) tagIndex = temp;
        }

        for ( int k = 1; k < constraintIds.size( ); k++ )
        {
            String temp = constraintIds.get( k );
            if ( previousTags.get( temp ).getValue( ) > currentAxis.getTag( temp ).getValue( ) ) tagIndex = temp;
        }

        double newVal = currentAxis.getTag( tagIndex ).getValue( );
        if ( newVal > previousTags.get( tagIndex ).getValue( ) )
        {
            for ( int k = 0; k < constraintIds.size( ) - 1; k++ )
            {
                if ( currentAxis.getTag( constraintIds.get( k ) ).getValue( ) > currentAxis.getTag( constraintIds.get( k + 1 ) ).getValue( ) - buffer )
                {
                    currentAxis.getTag( constraintIds.get( k + 1 ) ).setValue( currentAxis.getTag( constraintIds.get( k ) ).getValue( ) + buffer );
                }
            }
        }
        else if ( newVal < previousTags.get( tagIndex ).getValue( ) )
        {
            for ( int k = constraintIds.size( ) - 1; k > 0; k-- )
            {
                if ( currentAxis.getTag( constraintIds.get( k - 1 ) ).getValue( ) > currentAxis.getTag( constraintIds.get( k ) ).getValue( ) - buffer )
                {
                    currentAxis.getTag( constraintIds.get( k - 1 ) ).setValue( currentAxis.getTag( constraintIds.get( k ) ).getValue( ) - buffer );
                }
            }
        }
    }
}
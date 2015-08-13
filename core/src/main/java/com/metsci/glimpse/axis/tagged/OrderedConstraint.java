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
        // find a tag (if any) whose value changed from the last time constraints were applied
        String id = getChangedTagId( currentAxis, previousTags );

        if ( id != null )
        {
            double currentValue = currentAxis.getTag( id ).getValue( );
            double previousValue = previousTags.get( id ).getValue( );   
            boolean valueIncreased = currentValue > previousValue;
            
            // if the tag which changed increased in value, bump other tags up
            // if the tag which changed decreased in value, bump other tags down
            // (if multiple tags changed, the last changed tag is used to determine the direction)
            if ( valueIncreased )
            {
                for ( int k = 0; k < constraintIds.size( ) - 1; k++ )
                {
                    compareAndSet( currentAxis, k+1, k, buffer );
                }
            }
            else
            {
                for ( int k = constraintIds.size( ) - 1; k > 0; k-- )
                {
                    compareAndSet( currentAxis, k-1, k, buffer );
                }
            }
        }
    }
    
    protected String getChangedTagId( TaggedAxis1D currentAxis, Map<String, Tag> previousTags )
    {
        for ( String id : constraintIds )
        {
            Tag previousTag = previousTags.get( id );
            Tag currentTag = currentAxis.getTag( id );
            
            if ( previousTag != null && currentTag != null && previousTag.getValue( ) != currentTag.getValue( ) )
            {
                return id;
            }
        }
        
        return null;
    }
    
    protected void compareAndSet( TaggedAxis1D currentAxis, int firstIndex, int secondIndex, double buffer )
    {
        Tag firstTag = currentAxis.getTag( constraintIds.get( firstIndex ) );
        Tag secondTag = currentAxis.getTag( constraintIds.get( secondIndex ) );

        if ( firstTag == null || secondTag == null ) return;
        
        double firstValue = firstTag.getValue( );
        double secondValue = secondTag.getValue( );

        if ( firstIndex > secondIndex && firstValue < secondValue + buffer )
        {
            firstTag.setValue( secondValue + buffer );
        }
        else if ( firstIndex < secondIndex && firstValue > secondValue - buffer )
        {
            firstTag.setValue( secondValue - buffer );
        }
    }
}
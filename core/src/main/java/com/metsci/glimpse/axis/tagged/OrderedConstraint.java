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
                    compareAndSet( currentAxis, k + 1, k, buffer );
                }
            }
            else
            {
                for ( int k = constraintIds.size( ) - 1; k > 0; k-- )
                {
                    compareAndSet( currentAxis, k - 1, k, buffer );
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
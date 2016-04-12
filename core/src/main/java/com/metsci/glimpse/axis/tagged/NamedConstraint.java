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

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * <p>Simple {@link Constraint} implementation which provides a constructor for specifying the constraint name.</p>
 *
 * <p>This class is not thread-safe.</p>
 *
 * @author ulman
 */
public abstract class NamedConstraint implements Constraint
{
    protected String name;

    protected Map<String, Tag> previousTags;
    protected TaggedAxis1D currentAxis;

    public NamedConstraint( String name )
    {
        this.name = name;
        this.previousTags = Collections.emptyMap( );
    }

    @Override
    public String getName( )
    {
        return this.name;
    }

    public void applyConstraint( TaggedAxis1D axis )
    {
        currentAxis = axis;

        if ( previousTags.isEmpty( ) ) saveTags( axis );

        applyConstraint( axis, previousTags );

        saveTags( axis );
    }

    protected void resetTags( )
    {
        resetTags( currentAxis );
    }

    /**
     * Resets the tags for the provided axis to the saved values
     */
    protected void resetTags( TaggedAxis1D axis )
    {
        for ( Tag t : previousTags.values( ) )
        {
            axis.getTag( t.getName( ) ).setValue( t.getValue( ) );
        }
    }

    private void saveTags( TaggedAxis1D axis )
    {
        previousTags = Maps.newHashMap( );

        for ( Tag tag : axis.getSortedTags( ) )
        {
            previousTags.put( tag.getName( ), new Tag( tag ) );
        }
    }

    public abstract void applyConstraint( TaggedAxis1D currentAxis, Map<String, Tag> previousTags );

}

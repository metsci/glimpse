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

import java.util.ArrayList;
import java.util.List;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener1D;

/**
 * An AxisListener1D which only reports when axis tags have changed.
 * Changes to name, value, or number of tags are reported, but changes
 * to Tag attributes or their values do not trigger the listener.
 *
 * @author ulman
 */
public abstract class TaggedAxisListener1D implements AxisListener1D
{
    protected List<Tag> previous;

    @Override
    public void axisUpdated( Axis1D axis )
    {
        if ( axis == null ) return;

        if ( axis instanceof TaggedAxis1D )
        {
            TaggedAxis1D taggedAxis = ( TaggedAxis1D ) axis;

            List<Tag> current = taggedAxis.getSortedTags( );
            if ( haveTagsChanged( previous, current ) )
            {
                tagsUpdated( taggedAxis );

                previous = copyTags( current );
            }
        }
    }

    protected List<Tag> copyTags( List<Tag> list )
    {
        List<Tag> newList = new ArrayList<Tag>( list.size( ) );
        for ( Tag tag : list )
        {
            newList.add( new Tag( tag ) );
        }
        return newList;
    }

    protected boolean haveTagsChanged( List<Tag> previous, List<Tag> current )
    {
        if ( previous == null && current != null ) return true;
        if ( previous != null && current == null ) return true;
        if ( previous == null && current == null ) return false;
        if ( previous.size( ) != current.size( ) ) return true;

        for ( int i = 0; i < previous.size( ); i++ )
        {
            Tag previousTag = previous.get( i );
            Tag currentTag = current.get( i );

            // compare names first
            if ( !previousTag.equals( currentTag ) ) return true;
            if ( previousTag.getValue( ) != currentTag.getValue( ) ) return true;
        }

        return false;
    }

    public abstract void tagsUpdated( TaggedAxis1D axis );
}

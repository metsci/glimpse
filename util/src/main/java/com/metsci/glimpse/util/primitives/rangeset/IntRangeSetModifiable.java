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
package com.metsci.glimpse.util.primitives.rangeset;

import com.metsci.glimpse.util.primitives.sorted.SortedInts;
import com.metsci.glimpse.util.primitives.sorted.SortedIntsArray;

public class IntRangeSetModifiable implements IntRangeSet
{

    // The coalesce() method is more efficient when it doesn't
    // have to work in-place, so keep two arrays and swap them
    protected SortedIntsArray ranges;
    protected SortedIntsArray scratch;


    public IntRangeSetModifiable( )
    {
        this.ranges = new SortedIntsArray( );
        this.scratch = new SortedIntsArray( );
    }

    @Override
    public boolean contains( int x )
    {
        return ( this.ranges.indexAtOrBefore( x ) % 2 == 0 );
    }

    @Override
    public SortedInts ranges( )
    {
        return this.ranges;
    }

    public void add( int first, int count )
    {
        if ( count > 0 )
        {
            int start = first;
            int iBeforeStart = this.ranges.indexAtOrBefore( start );
            boolean startsInExistingRange = ( iBeforeStart % 2 == 0 );
            if ( !startsInExistingRange && iBeforeStart >= 0 && start == this.ranges.v( iBeforeStart ) )
            {
                // Broaden to overlap an adjacent existing range
                start--;
                iBeforeStart--;
                startsInExistingRange = true;
            }

            int end = first + count;
            int iAfterEnd = this.ranges.indexAtOrAfter( end );
            boolean endsInExistingRange = ( iAfterEnd % 2 == 1 );
            if ( !endsInExistingRange && iAfterEnd < this.ranges.n && end == this.ranges.v( iAfterEnd ) )
            {
                // Broaden to overlap an adjacent existing range
                end++;
                iAfterEnd++;
                endsInExistingRange = true;
            }

            if ( startsInExistingRange && endsInExistingRange )
            {
                this.ranges.removeRange( iBeforeStart + 1, iAfterEnd );
            }
            else if ( startsInExistingRange )
            {
                this.ranges.a[ iBeforeStart + 1 ] = end;
                this.ranges.removeRange( iBeforeStart + 2, iAfterEnd );
            }
            else if ( endsInExistingRange )
            {
                this.ranges.a[ iAfterEnd - 1 ] = start;
                this.ranges.removeRange( iBeforeStart + 1, iAfterEnd - 1 );
            }
            else
            {
                this.ranges.removeRange( iBeforeStart + 1, iAfterEnd );

                this.ranges.prepForInsert( iBeforeStart + 1, 2 );
                this.ranges.a[ iBeforeStart + 1 ] = start;
                this.ranges.a[ iBeforeStart + 2 ] = end;
            }
        }
    }

    public void coalesce( int tolerance )
    {
        if ( tolerance > 0 && this.ranges.n >= 4 )
        {
            // Reuse swapped out array, to avoid allocating a new one
            SortedIntsArray coalesced = this.scratch;
            coalesced.clear( );

            // Begin a coalesced range, but don't finish it until we see
            // whether the ranges that follow it can be coalesced with it
            coalesced.append( this.ranges.v( 0 ) );
            int pendingEnd = this.ranges.v( 1 );

            for ( int i = 2; i < this.ranges.n; i += 2 )
            {
                // If the next range is too far away to coalesce, finish
                // the current coalesced range and begin a new one
                int start = this.ranges.v( i + 0 );
                if ( pendingEnd + tolerance < start )
                {
                    coalesced.append( pendingEnd );
                    coalesced.append( start );
                }

                pendingEnd = this.ranges.v( i + 1 );
            }

            // Finish the final coalesced range
            coalesced.append( pendingEnd );

            // Swap arrays, to avoid allocating a new one on the next coalesce
            this.scratch = this.ranges;
            this.ranges = coalesced;
        }
    }

    public void clear( )
    {
        this.ranges.clear( );
    }

    @Override
    public String toString( )
    {
        StringBuilder s = new StringBuilder( );

        s.append( "{ " );
        for ( int i = 0; i < this.ranges.n; i += 2 )
        {
            int start = this.ranges.v( i + 0 );
            int end = this.ranges.v( i + 1 );
            s.append( "[" ).append( start ).append( "," ).append( end ).append( ") " );
        }
        s.append( "}" );

        return s.toString( );
    }

}

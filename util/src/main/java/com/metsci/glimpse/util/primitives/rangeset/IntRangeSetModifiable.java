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
            SortedIntsArray coalesced = this.scratch;


            int firstStart = this.ranges.v( 0 );
            int firstEnd = this.ranges.v( 1 );

            coalesced.append( firstStart );
            int pendingEnd = firstEnd;
            for ( int i = 2; i < this.ranges.n; i += 2 )
            {
                int start = this.ranges.v( i + 0 );
                int end = this.ranges.v( i + 1 );

                // If we can't continue to coalesce, finish the current coalesced range and begin a new one
                if ( pendingEnd + tolerance < start )
                {
                    coalesced.append( pendingEnd );
                    coalesced.append( start );
                }

                pendingEnd = end;
            }
            coalesced.append( pendingEnd );


            this.scratch = this.ranges;
            this.scratch.clear( );

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

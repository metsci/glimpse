package com.metsci.glimpse.gl;

import com.metsci.glimpse.util.primitives.sorted.SortedInts;
import com.metsci.glimpse.util.primitives.sorted.SortedIntsArray;

public class DirtyIndexSet
{

    protected final SortedIntsArray ranges;


    public DirtyIndexSet( )
    {
        this.ranges = new SortedIntsArray( );
    }

    public void add( int first, int count )
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

            this.ranges.prepForInsert( iBeforeStart, 2 );
            this.ranges.a[ iBeforeStart + 0 ] = start;
            this.ranges.a[ iBeforeStart + 1 ] = end;
        }
    }

    public void coalesce( int tolerance )
    {
        for ( int i = 0; i < this.ranges.n - 2; i += 2 )
        {
            int prevEnd = this.ranges.v( i + 1 );
            int nextStart = this.ranges.v( i + 2 );

            if ( prevEnd + tolerance >= nextStart )
            {
                // XXX: Not particularly efficient
                this.ranges.removeRange( i + 1, i + 3 );
            }
        }
    }

    public SortedInts ranges( )
    {
        return this.ranges;
    }

    public void clear( )
    {
        this.ranges.clear( );
    }

}

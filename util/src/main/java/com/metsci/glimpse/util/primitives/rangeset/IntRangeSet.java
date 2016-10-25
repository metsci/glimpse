package com.metsci.glimpse.util.primitives.rangeset;

import com.metsci.glimpse.util.primitives.sorted.SortedInts;

public interface IntRangeSet
{

    boolean contains( int x );

    SortedInts ranges( );

}

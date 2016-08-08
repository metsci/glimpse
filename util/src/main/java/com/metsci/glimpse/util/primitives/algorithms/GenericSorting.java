/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose
is hereby granted without fee, provided that the above copyright notice appear in all copies and
that both that copyright notice and this permission notice appear in supporting documentation.
CERN makes no representations about the suitability of this software for any purpose.
It is provided "as is" without expressed or implied warranty.
 */
package com.metsci.glimpse.util.primitives.algorithms;

/**
 * QuickSort and MergeSort with free-form Comparator and Swapper classes
 * allowing parallel arrays to be sorted simultaneously. The basic algorithms
 * are taken from Parallel Colt. Only a handful of modest modifications and
 * additions have been undertaken on the original.
 *
 * @author osborn
 */
public class GenericSorting extends Object
{
    private static final int SMALL = 7;
    private static final int MEDIUM = 40;

    private GenericSorting( )
    {
    }

    /**
     * Sorts the specified range of elements according to the order induced by
     * the specified comparator. All elements in the range must be <i>mutually
     * comparable</i> by the specified comparator (that is,
     * <tt>c.compare(a, b)</tt> must not throw an exception for any indexes
     * <tt>a</tt> and <tt>b</tt> in the range).
     * <p>
     *
     * This sort is guaranteed to be <i>stable</i>: equal elements will not be
     * reordered as a result of the sort.
     * <p>
     *
     * The sorting algorithm is a modified mergesort (in which the merge is
     * omitted if the highest element in the low sublist is less than the lowest
     * element in the high sublist). This algorithm offers guaranteed n*log(n)
     * performance, and can approach linear performance on nearly sorted lists.
     *
     * @param fromIndex
     *            the index of the first element (inclusive) to be sorted.
     * @param toIndex
     *            the index of the last element (exclusive) to be sorted.
     * @param c
     *            the comparator to determine the order of the generic data.
     * @param s
     *            an object that knows how to swap the elements at any two
     *            indexes (a,b).
     *
     * @see Comparator
     * @see Swapper
     */
    public static void mergesort( int fromIndex, int toIndex, Comparator c, Swapper s )
    {
        /*
         * We retain the same method signature as quickSort. Given only a
         * comparator and swapper we do not know how to copy and move elements
         * from/to temporary arrays. Hence, in contrast to the JDK mergesorts
         * this is an "in-place" mergesort, i.e. does not allocate any temporary
         * arrays. A non-inplace mergesort would perhaps be faster in most
         * cases, but would require non-intuitive delegate objects...
         */
        int length = toIndex - fromIndex;

        // Insertion sort on smallest arrays
        if ( length < SMALL )
        {
            for ( int i = fromIndex; i < toIndex; i++ )
            {
                for ( int j = i; j > fromIndex && ( c.compare( j - 1, j ) > 0 ); j-- )
                {
                    s.swap( j, j - 1 );
                }
            }
            return;
        }

        // Recursively sort halves
        int mid = ( fromIndex + toIndex ) / 2;
        mergesort( fromIndex, mid, c, s );
        mergesort( mid, toIndex, c, s );

        // If list is already sorted, nothing left to do. This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if ( c.compare( mid - 1, mid ) <= 0 ) return;

        // Merge sorted halves
        inplace_merge( fromIndex, mid, toIndex, c, s );
    }

    /**
     * Sorts the specified range of elements according to the order induced by
     * the specified comparator. All elements in the range must be <i>mutually
     * comparable</i> by the specified comparator (that is,
     * <tt>c.compare(a, b)</tt> must not throw an exception for any indexes
     * <tt>a</tt> and <tt>b</tt> in the range).
     * <p>
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley
     * and M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice
     * and Experience, Vol. 23(11) P. 1249-1265 (November 1993). This algorithm
     * offers n*log(n) performance on many data sets that cause other quicksorts
     * to degrade to quadratic performance.
     *
     * @param fromIndex
     *            the index of the first element (inclusive) to be sorted.
     * @param toIndex
     *            the index of the last element (exclusive) to be sorted.
     * @param c
     *            the comparator to determine the order of the generic data.
     * @param s
     *            an object that knows how to swap the elements at any two
     *            indexes (a,b).
     *
     * @see Comparator
     * @see Swapper
     */
    public static void quicksort( int fromIndex, int toIndex, Comparator c, Swapper s )
    {
        quickSort1( fromIndex, toIndex - fromIndex, c, s );
    }

    ////////////
    ////////////
    ////////////

    /**
     * Transforms two consecutive sorted ranges into a single sorted range. The
     * initial ranges are <code>[first, middle)</code> and
     * <code>[middle, last)</code>, and the resulting range is
     * <code>[first, last)</code>. Elements in the first input range will
     * precede equal elements in the second.
     */
    private static void inplace_merge( int first, int middle, int last, Comparator c, Swapper s )
    {
        if ( first >= middle || middle >= last ) return;
        if ( last - first == 2 )
        {
            if ( c.compare( middle, first ) < 0 )
            {
                s.swap( first, middle );
            }
            return;
        }
        int firstCut;
        int secondCut;
        if ( middle - first > last - middle )
        {
            firstCut = first + ( middle - first ) / 2;
            secondCut = lower_bound( middle, last, firstCut, c );
        }
        else
        {
            secondCut = middle + ( last - middle ) / 2;
            firstCut = upper_bound( first, middle, secondCut, c );
        }

        // rotate(firstCut, middle, secondCut, swapper);
        // is manually inlined for speed (jitter inlining seems to work only for
        // small call depths, even if methods are "static private")
        int first2 = firstCut;
        int middle2 = middle;
        int last2 = secondCut;
        if ( middle2 != first2 && middle2 != last2 )
        {
            int first1 = first2;
            int last1 = middle2;
            while ( first1 < --last1 )
                s.swap( first1++, last1 );
            first1 = middle2;
            last1 = last2;
            while ( first1 < --last1 )
                s.swap( first1++, last1 );
            first1 = first2;
            last1 = last2;
            while ( first1 < --last1 )
                s.swap( first1++, last1 );
        }
        // end inline

        middle = firstCut + ( secondCut - middle );
        inplace_merge( first, firstCut, middle, c, s );
        inplace_merge( middle, secondCut, last, c, s );
    }

    private static int upper_bound( int first, int last, int x, Comparator c )
    {
        int len = last - first;
        while ( len > 0 )
        {
            int half = len / 2;
            int middle = first + half;
            if ( c.compare( x, middle ) < 0 )
            {
                len = half;
            }
            else
            {
                first = middle + 1;
                len -= half + 1;
            }
        }
        return first;
    }

    private static int lower_bound( int first, int last, int x, Comparator c )
    {
        int len = last - first;
        while ( len > 0 )
        {
            int half = len / 2;
            int middle = first + half;
            if ( c.compare( middle, x ) < 0 )
            {
                first = middle + 1;
                len -= half + 1;
            }
            else
            {
                len = half;
            }
        }
        return first;
    }

    private static void quickSort1( int off, int len, Comparator comp, Swapper sw )
    {
        // Insertion sort on smallest arrays
        if ( len < SMALL )
        {
            for ( int i = off; i < len + off; i++ )
                for ( int j = i; j > off && ( comp.compare( j - 1, j ) > 0 ); j-- )
                {
                    sw.swap( j, j - 1 );
                }
            return;
        }

        // Choose a partition element, v
        int m = off + len / 2; // Small arrays, middle element
        if ( len > SMALL )
        {
            int l = off;
            int n = off + len - 1;
            if ( len > MEDIUM )
            { // Big arrays, pseudomedian of 9
                int s = len / 8;
                l = med3( l, l + s, l + 2 * s, comp );
                m = med3( m - s, m, m + s, comp );
                n = med3( n - 2 * s, n - s, n, comp );
            }
            m = med3( l, m, n, comp ); // Mid-size, med of 3
        }
        // long v = x[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while ( true )
        {
            int comparison;
            while ( b <= c && ( ( comparison = comp.compare( b, m ) ) <= 0 ) )
            {
                if ( comparison == 0 )
                {
                    if ( a == m )
                        m = b; // moving target; DELTA to JDK !!!
                    else if ( b == m ) m = a; // moving target; DELTA to JDK !!!
                    sw.swap( a++, b );
                }
                b++;
            }
            while ( c >= b && ( ( comparison = comp.compare( c, m ) ) >= 0 ) )
            {
                if ( comparison == 0 )
                {
                    if ( c == m )
                        m = d; // moving target; DELTA to JDK !!!
                    else if ( d == m ) m = c; // moving target; DELTA to JDK !!!
                    sw.swap( c, d-- );
                }
                c--;
            }
            if ( b > c ) break;
            if ( b == m )
                m = d; // moving target; DELTA to JDK !!!
            else if ( c == m ) m = c; // moving target; DELTA to JDK !!!
            sw.swap( b++, c-- );
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min( a - off, b - a );
        sw.vecswap( off, b - s, s );
        s = Math.min( d - c, n - d - 1 );
        sw.vecswap( b, n - s, s );

        // Recursively sort non-partition-elements
        if ( ( s = b - a ) > 1 ) quickSort1( off, s, comp, sw );
        if ( ( s = d - c ) > 1 ) quickSort1( n - s, s, comp, sw );
    }

    private static int med3( int a, int b, int c, Comparator comp )
    {
        int ab = comp.compare( a, b );
        int ac = comp.compare( a, c );
        int bc = comp.compare( b, c );
        return ( ab < 0 ? ( bc < 0 ? b : ac < 0 ? c : a ) : ( bc > 0 ? b : ac > 0 ? c : a ) );
    }

    ////////////
    ////////////
    ////////////

    public static abstract class Swapper
    {
        public abstract void swap( int a, int b );

        protected void vecswap( int a, int b, int n )
        {
            for ( int i = 0; i < n; i++, a++, b++ )
            {
                swap( a, b );
            }
        }
    }

    ////////////
    ////////////
    ////////////

    public interface Comparator
    {
        int compare( int o1, int o2 );
    }
}

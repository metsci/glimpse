package com.metsci.glimpse.support.interval;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.metsci.glimpse.util.quadtree.longvalued.LongQuadTreeObjects;
import com.metsci.glimpse.util.units.time.TimeStamp;

//XXX The inclusive/exclusive bounds are not currently working correctly
public abstract class IntervalQuadTree<V>
{
    // set is necessary to enforce set semantics, which LongQuadTreeObjects does not do
    protected Set<V> set;
    protected LongQuadTreeObjects<V> tree;
    protected int maxBucketSize;
    
    public IntervalQuadTree( int maxBucketSize )
    {
        this.maxBucketSize = maxBucketSize;
        this.tree = buildTree( maxBucketSize );
        this.set = new HashSet<>( );
    }
    
    protected LongQuadTreeObjects<V> buildTree( int maxBucketSize )
    {
        // x is start time, y is end time
        return new LongQuadTreeObjects<V>( maxBucketSize )
        {
            @Override
            public long x( V v )
            {
                return getStartTimeMillis( v );
            }

            @Override
            public long y( V v )
            {
                return getEndTimeMillis( v );
            }
        };
    }
    
    public abstract long getStartTimeMillis( V v );
    public abstract long getEndTimeMillis( V v );
    
    public void clear( )
    {
        this.tree = buildTree( maxBucketSize );
        this.set.clear( );
    }
    
    public void add( V value )
    {
        if ( this.set.add( value ) )
        {
            this.tree.add( value );
        }
    }
    
    public void remove( V value )
    {
        if ( this.set.remove( value ) )
        {
            this.tree.remove( value );
        }
    }
    
    public Collection<V> get( TimeStamp time )
    {
        return get( time, true, time, true );
    }

    /**
     * @param start inclusive start TimeStamp
     * @param end exclusive end TimeStamp
     * 
     * @return all Events whose time span overlaps with the provided bounds.
     */
    public Collection<V> get( TimeStamp start, TimeStamp end )
    {
        return get( start, true, end, false );
    }

    /**
     * @return all Events whose time span overlaps with the provided bounds.
     */
    public Collection<V> get( TimeStamp start, boolean startInclusive, TimeStamp end, boolean endInclusive )
    {
        return get( start.toPosixMillis( ), startInclusive, end.toPosixMillis( ), endInclusive );
    }
    
    /**
     * @param time time expressed as posix milliseconds
     * @see #get(TimeStamp)
     */
    public Collection<V> get( long time )
    {
        return get( time, true, time, true );
    }

    /**
     * @param start time expressed as posix milliseconds
     * @param end time expressed as posix milliseconds
     * @see #get(TimeStamp, TimeStamp)
     */
    public Collection<V> get( long start, long end )
    {
        return get( start, true, end, false );
    }

    /**
     * @param start time expressed as posix milliseconds
     * @param end time expressed as posix milliseconds
     * @see #get(TimeStamp, boolean, TimeStamp, boolean )
     */
    public Collection<V> get( long start, boolean startInclusive, long end, boolean endInclusive )
    {
        return this.tree.search( Long.MIN_VALUE, end, start, Long.MAX_VALUE );
    }
    
    public Collection<V> getOverlapping( V value )
    {
        return get( getStartTimeMillis( value ), getEndTimeMillis( value ) );
    }
    
    public Collection<V> getInterior( TimeStamp start, TimeStamp end )
    {
        return getInterior( start.toPosixMillis( ), end.toPosixMillis( ) );
    }
    
    public Collection<V> getInterior( TimeStamp start, boolean startInclusive, TimeStamp end, boolean endInclusive )
    {
        return getInterior( start.toPosixMillis( ), startInclusive, end.toPosixMillis( ), endInclusive );
    }
    
    public Collection<V> getInterior( long start, long end )
    {
        return getInterior( start, true, end, false );
    }
    
    public Collection<V> getInterior( long start, boolean startInclusive, long end, boolean endInclusive )
    {
        return this.tree.search( start, end, start, end );
    }
    
    public boolean isEmpty( )
    {
        return size( ) == 0;
    }

    public int size( )
    {
        return this.set.size( );
    }
}

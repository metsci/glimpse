package com.metsci.glimpse.plot.timeline.event;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>A Sorted Multiset of "identifiable" objects.</p>
 * 
 * <p>This set works identically to Guava's SortedMultiset, except that if two objects are
 * equal according to the set's {@link Comparator} but are not equal according to
 * {@link #equals(Object)}, then both objects will be stored by the set. Guava's Multiset
 * would simply see two instances of the first object added to the list in this case
 * (because it ignores {@link #equals(Object)} entirely).</p>
 * 
 * <p>A possible use case for this class would be storing times associated with events. Two
 * events may happen at the same time, but the events are still unique.</p>
 * 
 * @author ulman
 */
public class IdentifiableSortedMultiset<D> implements NavigableSet<D>
{
    NavigableSet<D> delegate;
    
    public IdentifiableSortedMultiset( )
    {
        delegate = new TreeSet<D>( );
    }
    
    public IdentifiableSortedMultiset( Comparator<? super D> comparator )
    {
        delegate = new TreeSet<D>( comparator );
    }

    @Override
    public java.util.Comparator<? super D> comparator( )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public D first( )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public D last( )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size( )
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isEmpty( )
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains( Object o )
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object[] toArray( )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray( T[] a )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean add( D e )
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean remove( Object o )
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsAll( Collection<?> c )
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll( Collection<? extends D> c )
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll( Collection<?> c )
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll( Collection<?> c )
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear( )
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public D lower( D e )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public D floor( D e )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public D ceiling( D e )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public D higher( D e )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public D pollFirst( )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public D pollLast( )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<D> iterator( )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableSet<D> descendingSet( )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<D> descendingIterator( )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableSet<D> subSet( D fromElement, boolean fromInclusive, D toElement, boolean toInclusive )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableSet<D> headSet( D toElement, boolean inclusive )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NavigableSet<D> tailSet( D fromElement, boolean inclusive )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSet<D> subSet( D fromElement, D toElement )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSet<D> headSet( D toElement )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSet<D> tailSet( D fromElement )
    {
        // TODO Auto-generated method stub
        return null;
    }

}

/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.canvas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.metsci.glimpse.layout.GlimpseLayout;

public class LayoutManager
{
    protected LayoutOrderComparator comparator;
    protected Map<GlimpseLayout, LayoutOrder> layoutMap;
    protected List<LayoutOrder> layoutList;
    protected List<GlimpseLayout> unmodifiableLayoutList;

    public LayoutManager( )
    {
        this.comparator = new LayoutOrderComparator( );
        this.layoutList = new ArrayList<LayoutOrder>( );
        this.layoutMap = new LinkedHashMap<GlimpseLayout, LayoutOrder>( );
        this.unmodifiableLayoutList = new LayoutList( this.layoutList );
    }

    public void removeLayout( GlimpseLayout layout )
    {
        LayoutOrder layoutOrder = this.layoutMap.remove( layout );
        this.layoutList.remove( layoutOrder );
    }
    
    public void removeAllLayouts( )
    {
        this.layoutMap.clear( );
        this.layoutList.clear( );
    }

    public void addLayout( GlimpseLayout layout )
    {
        this.addLayout( layout, 0 );
    }

    public void addLayout( GlimpseLayout layout, int zOrder )
    {
        LayoutOrder layoutOrder = new LayoutOrder( layout, zOrder );
        this.layoutMap.put( layout, layoutOrder );
        this.layoutList.add( layoutOrder );
        this.updateLayoutList( );
    }

    public void setZOrder( GlimpseLayout layout, int zOrder )
    {
        LayoutOrder layoutOrder = this.layoutMap.get( layout );
        if ( layoutOrder != null )
        {
            layoutOrder.setZOrder( zOrder );
            updateLayoutList( );
        }
    }

    public List<GlimpseLayout> getLayoutList( )
    {
        return this.unmodifiableLayoutList;
    }

    protected void updateLayoutList( )
    {
        Collections.sort( this.layoutList, this.comparator );
    }

    /**
     * An unmodifiable list wrapper for the internal List<LayoutOrder> which
     * exposes it as a List<GlimpseTarget>.
     * 
     * @author ulman
     */
    /*
     * This could be solved much more cleanly using Guava as follows:
     * 
     * Collections.unmodifiableList( Lists.transform( layoutList, new Function<LayoutOrder, GlimpseTarget>( )
     * {
     *   @Override
     *   public GlimpseTarget apply( LayoutOrder arg0 )
     *   {
     *     return arg0.getLayout( );
     *   }
     * } ) );
     *
     */
    public static class LayoutList implements List<GlimpseLayout>
    {
        protected List<LayoutOrder> backingList;

        public LayoutList( List<LayoutOrder> backingList )
        {
            this.backingList = backingList;
        }

        //@formatter:off
        @Override public boolean add( GlimpseLayout e ) {  throw new UnsupportedOperationException(); }
        @Override public void add( int index, GlimpseLayout element ) {  throw new UnsupportedOperationException(); }
        @Override public boolean addAll( Collection<? extends GlimpseLayout> c ) {  throw new UnsupportedOperationException(); }
        @Override public boolean addAll( int index, Collection<? extends GlimpseLayout> c ) {  throw new UnsupportedOperationException(); }
        @Override public void clear( ) {  throw new UnsupportedOperationException(); }
        @Override public boolean remove( Object o ) {  throw new UnsupportedOperationException(); }
        @Override public GlimpseLayout remove( int index ) {  throw new UnsupportedOperationException(); }
        @Override public boolean removeAll( Collection<?> c ) {  throw new UnsupportedOperationException(); }
        @Override public boolean retainAll( Collection<?> c ) {  throw new UnsupportedOperationException(); }
        @Override public GlimpseLayout set( int index, GlimpseLayout element ) {  throw new UnsupportedOperationException(); }
        
        @Override
        public ListIterator<GlimpseLayout> listIterator( final int index )
        {
            return new ListIterator<GlimpseLayout>()
            {
                ListIterator<LayoutOrder> i = backingList.listIterator(index);
    
                public boolean hasNext()         {return i.hasNext();}
                public GlimpseLayout next()      {return i.next().getLayout( );}
                public boolean hasPrevious()     {return i.hasPrevious();}
                public GlimpseLayout previous()  {return i.previous().getLayout( );}
                public int nextIndex()           {return i.nextIndex();}
                public int previousIndex()       {return i.previousIndex();}
    
                public void remove() { throw new UnsupportedOperationException(); }
                public void set(GlimpseLayout e) { throw new UnsupportedOperationException(); }
                public void add(GlimpseLayout e) { throw new UnsupportedOperationException(); }
            };
        }
        //@formatter:on

        @Override
        public GlimpseLayout get( int index )
        {
            LayoutOrder layoutOrder = backingList.get( index );
            return layoutOrder.getLayout( );
        }

        @Override
        public boolean isEmpty( )
        {
            return backingList.isEmpty( );
        }

        @Override
        public Iterator<GlimpseLayout> iterator( )
        {
            return listIterator( 0 );
        }

        @Override
        public ListIterator<GlimpseLayout> listIterator( )
        {
            return listIterator( 0 );
        }

        @Override
        public int size( )
        {
            return backingList.size( );
        }

        @Override
        public boolean contains( Object o )
        {
            for ( LayoutOrder order : backingList )
            {
                if ( order.getLayout( ).equals( o ) ) return true;
            }

            return false;
        }

        @Override
        public boolean containsAll( Collection<?> c )
        {
            for ( Object o : c )
            {
                if ( !contains( o ) ) return false;
            }

            return true;
        }

        @Override
        public List<GlimpseLayout> subList( int fromIndex, int toIndex )
        {
            return new LayoutList( backingList.subList( fromIndex, toIndex ) );
        }

        @Override
        public int indexOf( Object o )
        {
            for ( int i = 0; i < backingList.size( ); i++ )
            {
                LayoutOrder order = backingList.get( i );

                if ( order.getLayout( ).equals( o ) ) return i;
            }

            return -1;
        }

        @Override
        public int lastIndexOf( Object o )
        {
            for ( int i = backingList.size( ) - 1; i >= 0; i-- )
            {
                LayoutOrder order = backingList.get( i );

                if ( order.getLayout( ).equals( o ) ) return i;
            }

            return -1;
        }

        @Override
        public Object[] toArray( )
        {
            Object[] a = new Object[backingList.size( )];

            for ( int i = 0; i < backingList.size( ); i++ )
            {
                LayoutOrder order = backingList.get( i );
                a[i] = order.getLayout( );
            }

            return a;
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public <T> T[] toArray( T[] a )
        {
            Object[] o = a;

            if ( a.length < backingList.size( ) )
            {
                o = new Object[backingList.size( )];
            }

            for ( int i = 0; i < backingList.size( ); i++ )
            {
                LayoutOrder order = backingList.get( i );
                o[i] = order.getLayout( );
            }

            return ( T[] ) o;
        }

    }

    public static class LayoutOrder
    {
        protected GlimpseLayout layout;
        protected int zOrder;

        public LayoutOrder( GlimpseLayout layout )
        {
            this( layout, 0 );
        }

        public LayoutOrder( GlimpseLayout layout, int zOrder )
        {
            this.layout = layout;
            this.zOrder = zOrder;
        }

        public GlimpseLayout getLayout( )
        {
            return layout;
        }

        public int getZOrder( )
        {
            return zOrder;
        }

        public void setZOrder( int order )
        {
            this.zOrder = order;
        }

        @Override
        public int hashCode( )
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( layout == null ) ? 0 : layout.hashCode( ) );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            LayoutOrder other = ( LayoutOrder ) obj;
            if ( layout == null )
            {
                if ( other.layout != null ) return false;
            }
            else if ( !layout.equals( other.layout ) ) return false;
            return true;
        }
    }

    public static class LayoutOrderComparator implements Comparator<LayoutOrder>
    {
        @Override
        public int compare( LayoutOrder arg0, LayoutOrder arg1 )
        {
            if ( arg0.getZOrder( ) < arg1.getZOrder( ) )
            {
                return -1;
            }
            else if ( arg0.getZOrder( ) > arg1.getZOrder( ) )
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }

    }
}

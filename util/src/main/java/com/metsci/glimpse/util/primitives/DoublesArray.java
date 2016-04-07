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
package com.metsci.glimpse.util.primitives;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.nio.DoubleBuffer;

/**
 * @author hogye
 */
public class DoublesArray implements DoublesModifiable
{

    public double[] a;
    public int n;

    // Instantiation

    /**
     * For efficiency, does <em>not</em> clone the array arg.
     */
    public DoublesArray( double[] a )
    {
        this( a, a.length );
    }

    public DoublesArray( int n )
    {
        this( new double[n], 0 );
    }

    public DoublesArray( )
    {
        this( new double[0], 0 );
    }

    /**
     * For efficiency, does <em>not</em> clone the array arg.
     */
    public DoublesArray( double[] a, int n )
    {
        this.a = a;
        this.n = n;
    }

    /**
     * Clones the sequence arg.
     */
    public DoublesArray( Doubles xs )
    {
        n = xs.n( );
        a = new double[n];
        xs.copyTo( 0, a, 0, n );
    }

    // Accessors

    @Override
    public double v( int i )
    {
        // Skip bounds check for speed
        //if (i >= n) throw new ArrayIndexOutOfBoundsException("Array index out of range: index = " + i + ", length = " + n);

        return a[i];
    }

    @Override
    public int n( )
    {
        return n;
    }

    @Override
    public void copyTo( int i, double[] dest, int iDest, int c )
    {
        System.arraycopy( a, i, dest, iDest, c );
    }

    @Override
    public void copyTo( int i, DoubleBuffer dest, int c )
    {
        dest.put( a, i, c );
    }

    @Override
    public void copyTo( DoubleBuffer dest )
    {
        dest.put( a, 0, n );
    }

    @Override
    public double[] copyOf( int i, int c )
    {
        double[] copy = new double[c];
        System.arraycopy( a, i, copy, 0, c );
        return copy;
    }

    @Override
    public double[] copyOf( )
    {
        double[] copy = new double[n];
        System.arraycopy( a, 0, copy, 0, n );
        return copy;
    }

    @Override
    public boolean isEmpty( )
    {
        return ( n == 0 );
    }

    @Override
    public double first( )
    {
        return a[0];
    }

    @Override
    public double last( )
    {
        return a[n - 1];
    }

    // Mutators

    @Override
    public void set( int i, double v )
    {
        a[i] = v;
    }

    @Override
    public void set( int i, double[] vs )
    {
        set( i, vs, 0, vs.length );
    }

    @Override
    public void set( int i, double[] vs, int from, int to )
    {
        int c = to - from;
        ensureCapacity( i + c );
        System.arraycopy( vs, from, a, i, c );
        n = i + c;
    }

    @Override
    public void insert( int i, double v )
    {
        prepForInsert( i, 1 );
        a[i] = v;
    }

    @Override
    public void insert( int i, Doubles vs )
    {
        insert( i, vs, 0, vs.n( ) );
    }

    @Override
    public void insert( int i, Doubles vs, int from, int to )
    {
        int c = to - from;
        prepForInsert( i, c );
        vs.copyTo( from, a, i, c );
    }

    @Override
    public void insert( int i, double[] vs )
    {
        insert( i, vs, 0, vs.length );
    }

    @Override
    public void insert( int i, double[] vs, int from, int to )
    {
        int c = to - from;
        prepForInsert( i, c );
        System.arraycopy( vs, from, a, i, c );
    }

    @Override
    public void insert( int i, DoubleBuffer vs )
    {
        insert( i, vs, vs.remaining( ) );
    }

    @Override
    public void insert( int i, DoubleBuffer vs, int c )
    {
        prepForInsert( i, c );
        vs.get( a, i, c );
    }

    /**
     * Makes room in this array for new values to be inserted.
     *
     * When this call returns, the values in <code>this.a</code> on <code>[i,i+c)</code>
     * are undefined. Writing meaningful values to these indices is up to the
     * caller.
     *
     * @param i The index at which new values will be inserted
     * @param c The count of new values that will be inserted
     */
    public void prepForInsert( int i, int c )
    {
        double[] a = this.a;
        int capacity = a.length;
        int n = this.n;

        int nNew;
        if ( i >= n )
        {
            nNew = i + c;
            if ( nNew > capacity )
            {
                double[] aNew = newArray( capacity, nNew );
                System.arraycopy( a, 0, aNew, 0, n );
                this.a = aNew;
            }
        }
        else
        {
            nNew = n + c;
            if ( nNew > capacity )
            {
                double[] aNew = newArray( capacity, nNew );
                System.arraycopy( a, 0, aNew, 0, i );
                System.arraycopy( a, i, aNew, i + c, n - i );
                this.a = aNew;
            }
            else
            {
                System.arraycopy( a, i, a, i + c, n - i );
            }
        }
        this.n = nNew;
    }

    @Override
    public void prepend( double v )
    {
        prepForPrepend( 1 );
        a[0] = v;
    }

    @Override
    public void prepend( Doubles vs )
    {
        prepend( vs, 0, vs.n( ) );
    }

    @Override
    public void prepend( Doubles vs, int from, int to )
    {
        int c = to - from;
        prepForPrepend( c );
        vs.copyTo( from, a, 0, c );
    }

    @Override
    public void prepend( double[] vs )
    {
        prepend( vs, 0, vs.length );
    }

    @Override
    public void prepend( double[] vs, int from, int to )
    {
        int c = to - from;
        prepForPrepend( c );
        System.arraycopy( vs, from, a, 0, c );
    }

    @Override
    public void prepend( DoubleBuffer vs )
    {
        prepend( vs, vs.remaining( ) );
    }

    @Override
    public void prepend( DoubleBuffer vs, int c )
    {
        prepForPrepend( c );
        vs.get( a, 0, c );
    }

    /**
     * Makes room in this array for new values to be prepended.
     *
     * When this call returns, the values in <code>this.a</code> on <code>[0,c)</code>
     * are undefined. Writing meaningful values to these indices is up to the
     * caller.
     *
     * @param c The count of new values that will be inserted
     */
    public void prepForPrepend( int c )
    {
        double[] a = this.a;
        int capacity = a.length;
        int n = this.n;

        int nNew = n + c;
        if ( nNew > capacity )
        {
            double[] aNew = newArray( capacity, nNew );
            System.arraycopy( a, 0, aNew, c, n );
            this.a = aNew;
        }
        else
        {
            System.arraycopy( a, 0, a, c, n );
        }
        this.n = nNew;
    }

    @Override
    public void append( double v )
    {
        prepForAppend( 1 );
        a[n - 1] = v;
    }

    @Override
    public void append( Doubles vs )
    {
        append( vs, 0, vs.n( ) );
    }

    @Override
    public void append( Doubles vs, int from, int to )
    {
        int c = to - from;
        prepForAppend( c );
        vs.copyTo( from, a, n - c, c );
    }

    @Override
    public void append( double[] vs )
    {
        append( vs, 0, vs.length );
    }

    @Override
    public void append( double[] vs, int from, int to )
    {
        int c = to - from;
        prepForAppend( c );
        System.arraycopy( vs, from, a, n - c, c );
    }

    @Override
    public void append( DoubleBuffer vs )
    {
        append( vs, vs.remaining( ) );
    }

    @Override
    public void append( DoubleBuffer vs, int c )
    {
        prepForAppend( c );
        vs.get( a, n - c, c );
    }

    /**
     * Makes room in this array for new values to be appended.
     *
     * When this call returns, the values in <code>this.a</code> on <code>[this.n-c,this.n)</code>
     * are undefined. Writing meaningful values to these indices is up to the
     * caller.
     *
     * @param c The count of new values that will be appended
     */
    public void prepForAppend( int c )
    {
        double[] a = this.a;
        int capacity = a.length;
        int n = this.n;

        int nNew = n + c;
        if ( nNew > capacity )
        {
            double[] aNew = newArray( capacity, nNew );
            System.arraycopy( a, 0, aNew, 0, n );
            this.a = aNew;
        }
        this.n = nNew;
    }

    @Override
    public void remove( double v )
    {
        for ( int i = 0; i < n; i++ )
        {
            if ( a[i] == v )
            {
                System.arraycopy( a, i + 1, a, i, n - ( i + 1 ) );
                n--;
                return;
            }
        }
    }

    @Override
    public void removeRange( int from, int to )
    {
        int length = n - to;
        System.arraycopy( a, to, a, from, length );
        n -= to - from;
    }

    @Override
    public void removeIndex( int index )
    {
        removeRange( index, index + 1 );
    }

    @Override
    public void clear( )
    {
        n = 0;
    }

    @Override
    public void ensureCapacity( int minCapacity )
    {
        int capacity = a.length;
        if ( minCapacity > capacity )
        {
            double[] aNew = newArray( capacity, minCapacity );
            System.arraycopy( a, 0, aNew, 0, n );
            this.a = aNew;
        }
    }

    @Override
    public void compact( )
    {
        double[] compact = new double[n];
        System.arraycopy( a, 0, compact, 0, n );
        a = compact;
    }

    /**
     * Creates a new array whose capacity is at least minNewCapacity, and at least
     * 1.618 * oldCapacity, up to Integer.MAX_VALUE.
     */
    public static double[] newArray( int oldCapacity, int minNewCapacity )
    {
        int newCapacity = ( int ) max( minNewCapacity, min( Integer.MAX_VALUE, ( 106039L * oldCapacity ) >>> 16 ) );
        return new double[newCapacity];
    }

}

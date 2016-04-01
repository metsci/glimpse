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
package com.metsci.glimpse.util;

/**
 * A collection of classes for storing a growable array of primitives
 * including <code>double</code>, <code>float</code>,
 * <code>integer</code>, and <code>long</code> primitives.
 *
 * <p> A newly constructed <code>PrimitiveVector</code> begins with an
 * underlying storage array of 10 elements (the size may be overridden
 * by using the appropriate constructor).  Whenever its space is
 * exhausted, the underlying array will grow by a factor of 2 the next
 * time an element is added.  The underlying array may grow by at most
 * 50,000 when it expands.</p>
 *
 * @author osborn
 */
public abstract class PrimitiveVector
{
    /**
     * The default initial size of the underlying array.
     */
    private static final int defaultInitialCapacity = 10;

    /**
     * The factor by which the underlying array grows when more
     * space is needed.
     */
    private static final double growthRate = 2;

    /**
     * The most an underlying array is allowed to grow by
     * when more space is needed.
     */
    private static final int maxGrowth = 50000;

    /**
     * Returns the number of elements stored in this array.
     */
    abstract int size( );

    /**
     * Returns the size of the underlying array.
     */
    abstract int capacity( );

    /**
     * Removes all elements from the vector.
     */
    abstract void removeAll( );

    /**
     * Copies the data into a new underlying array of minimum sufficient size.
     *
     * <p>Typically there will be unused slots in the underlying array,
     * causing the <code>PrimitiveVector</code> to use more memory than
     * necessary.  Calling this function eliminates this overhead; however,
     * calling it too often will reduce performance.</p>
     *
     * <b>NOTE:</b> For large arrays, calling this method will temporarily
     * double its memory footprint for the the purpose of copying.
     */
    abstract void trimToSize( );

    abstract double getAsDouble( int i );

    public static class Integer extends PrimitiveVector
    {
        private int[] data;
        private int size;

        public Integer( )
        {
            this( defaultInitialCapacity );
        }

        public Integer( int initialCapacity )
        {
            data = new int[initialCapacity];
            size = 0;
        }

        public final void add( int x )
        {
            ensureAdditionalCapacity( 1 );
            data[size++] = x;
        }

        public final void add( int[] x )
        {
            add( x, 0, x.length );
        }

        public final void add( int[] x, int start, int len )
        {
            ensureAdditionalCapacity( len );
            System.arraycopy( x, start, data, size, len );
            size += len;
        }

        public final void add( Integer x )
        {
            add( x.getUnderlyingData( ), 0, x.size( ) );
        }

        private final void ensureAdditionalCapacity( int n )
        {
            if ( size + n > data.length )
            {
                int newSize = Math.min( ( int ) ( growthRate * size ), size + maxGrowth );
                newSize = Math.max( data.length + n, newSize );
                int[] newData = new int[newSize];
                System.arraycopy( data, 0, newData, 0, size );
                data = newData;
            }
        }

        public final int[] getUnderlyingData( )
        {
            return data;
        }

        public final int[] getCopiedData( )
        {
            int[] r = new int[size];
            System.arraycopy( data, 0, r, 0, size );
            return r;
        }

        public final int get( int i )
        {
            return data[i];
        }

        @Override
        public final int size( )
        {
            return size;
        }

        @Override
        public final int capacity( )
        {
            return data.length;
        }

        @Override
        public final double getAsDouble( int i )
        {
            return data[i];
        }

        @Override
        public void removeAll( )
        {
            size = 0;
        }

        @Override
        public void trimToSize( )
        {
            data = getCopiedData( );
        }
    }

    public static class Long extends PrimitiveVector
    {
        private long[] data;
        private int size;

        public Long( )
        {
            this( defaultInitialCapacity );
        }

        public Long( int initialCapacity )
        {
            data = new long[initialCapacity];
            size = 0;
        }

        public final void add( long x )
        {
            ensureAdditionalCapacity( 1 );
            data[size++] = x;
        }

        public final void add( long[] x )
        {
            add( x, 0, x.length );
        }

        public final void add( long[] x, int start, int len )
        {
            ensureAdditionalCapacity( len );
            System.arraycopy( x, start, data, size, len );
            size += len;
        }

        public final void add( Long x )
        {
            add( x.getUnderlyingData( ), 0, x.size( ) );
        }

        private final void ensureAdditionalCapacity( int n )
        {
            if ( size + n > data.length )
            {
                int newSize = Math.min( ( int ) ( growthRate * size ), size + maxGrowth );
                newSize = Math.max( data.length + n, newSize );
                long[] newData = new long[newSize];
                System.arraycopy( data, 0, newData, 0, size );
                data = newData;
            }
        }

        public final long[] getUnderlyingData( )
        {
            return data;
        }

        public final long[] getCopiedData( )
        {
            long[] r = new long[size];
            System.arraycopy( data, 0, r, 0, size );
            return r;
        }

        public final long get( int i )
        {
            return data[i];
        }

        @Override
        public final int size( )
        {
            return size;
        }

        @Override
        public final int capacity( )
        {
            return data.length;
        }

        @Override
        public final double getAsDouble( int i )
        {
            return data[i];
        }

        @Override
        public void removeAll( )
        {
            size = 0;
        }

        @Override
        public void trimToSize( )
        {
            data = getCopiedData( );
        }
    }

    public static class Float extends PrimitiveVector
    {
        private float[] data;
        private int size;

        public Float( )
        {
            this( defaultInitialCapacity );
        }

        public Float( int initialCapacity )
        {
            data = new float[initialCapacity];
            size = 0;
        }

        public final void add( float x )
        {
            ensureAdditionalCapacity( 1 );
            data[size++] = x;
        }

        public final void add( float x, float y )
        {
            ensureAdditionalCapacity( 2 );
            data[size++] = x;
            data[size++] = y;
        }

        public final void add( float x, float y, float z )
        {
            ensureAdditionalCapacity( 3 );
            data[size++] = x;
            data[size++] = y;
            data[size++] = z;
        }

        public final void add( float[] x )
        {
            add( x, 0, x.length );
        }

        public final void add( double[] x )
        {
            add( x, 0, x.length );
        }

        public final void add( float[] x, int start, int len )
        {
            ensureAdditionalCapacity( len );
            System.arraycopy( x, start, data, size, len );
            size += len;
        }

        public final void add( double[] x, int start, int len )
        {
            ensureAdditionalCapacity( len );
            for ( int i = start; i < start + len; i++ )
                data[size++] = ( float ) x[i];
        }

        private final void ensureAdditionalCapacity( int n )
        {
            if ( size + n > data.length )
            {
                int newSize = Math.min( ( int ) ( growthRate * size ), size + maxGrowth );
                newSize = Math.max( data.length + n, newSize );
                float[] newData = new float[newSize];
                System.arraycopy( data, 0, newData, 0, size );
                data = newData;
            }
        }

        public final float[] getUnderlyingData( )
        {
            return data;
        }

        public final float[] getCopiedData( )
        {
            float[] r = new float[size];
            System.arraycopy( data, 0, r, 0, size );
            return r;
        }

        public final double get( int i )
        {
            return data[i];
        }

        @Override
        public final int size( )
        {
            return size;
        }

        @Override
        public final int capacity( )
        {
            return data.length;
        }

        @Override
        public final double getAsDouble( int i )
        {
            return data[i];
        }

        @Override
        public void removeAll( )
        {
            size = 0;
        }

        @Override
        public void trimToSize( )
        {
            data = getCopiedData( );
        }
    }

    public static class Double extends PrimitiveVector
    {
        private double[] data;
        private int size;

        public Double( )
        {
            this( defaultInitialCapacity );
        }

        public Double( int initialCapacity )
        {
            data = new double[initialCapacity];
            size = 0;
        }

        public final void add( double x )
        {
            ensureAdditionalCapacity( 1 );
            data[size++] = x;
        }

        public final void add( double x, double y )
        {
            ensureAdditionalCapacity( 2 );
            data[size++] = x;
            data[size++] = y;
        }

        public final void add( double x, double y, double z )
        {
            ensureAdditionalCapacity( 3 );
            data[size++] = x;
            data[size++] = y;
            data[size++] = z;
        }

        public final void add( double[] x )
        {
            add( x, 0, x.length );
        }

        public final void add( float[] x )
        {
            add( x, 0, x.length );
        }

        public final void add( double[] x, int start, int len )
        {
            ensureAdditionalCapacity( len );
            System.arraycopy( x, start, data, size, len );
            size += len;
        }

        public final void add( float[] x, int start, int len )
        {
            ensureAdditionalCapacity( len );
            for ( int i = start; i < start + len; i++ )
                data[size++] = ( float ) x[i];
        }

        private final void ensureAdditionalCapacity( int n )
        {
            if ( size + n > data.length )
            {
                int newSize = Math.min( ( int ) ( growthRate * size ), size + maxGrowth );
                newSize = Math.max( data.length + n, newSize );
                double[] newData = new double[newSize];
                System.arraycopy( data, 0, newData, 0, size );
                data = newData;
            }
        }

        public final double[] getUnderlyingData( )
        {
            return data;
        }

        public final double[] getCopiedData( )
        {
            double[] r = new double[size];
            System.arraycopy( data, 0, r, 0, size );
            return r;
        }

        public final double get( int i )
        {
            return data[i];
        }

        @Override
        public final int size( )
        {
            return size;
        }

        @Override
        public final int capacity( )
        {
            return data.length;
        }

        @Override
        public final double getAsDouble( int i )
        {
            return data[i];
        }

        @Override
        public void removeAll( )
        {
            size = 0;
        }

        @Override
        public void trimToSize( )
        {
            data = getCopiedData( );
        }
    }
}

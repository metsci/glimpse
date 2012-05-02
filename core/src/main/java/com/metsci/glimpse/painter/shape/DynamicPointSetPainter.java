package com.metsci.glimpse.painter.shape;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer.Mutator;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer2D;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer2D.IndexedMutator;
import com.metsci.glimpse.gl.attribute.GLVertexAttribute;
import com.metsci.glimpse.painter.base.GlimpseDataPainter2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.primitives.FloatsArray;
import com.metsci.glimpse.util.primitives.IntsArray;

public class DynamicPointSetPainter extends GlimpseDataPainter2D
{
    protected static final double GROWTH_FACTOR = 1.3;

    protected static final float DEFAULT_POINT_SIZE = 5.0f;
    protected static final int DEFAULT_INITIAL_SIZE = 2000;
    protected static final float[] DEFAULT_COLOR = GlimpseColor.getBlack( );

    protected float pointSize;

    // number of floats in pointBuffer
    protected int bufferSize;

    protected GLFloatBuffer colorBuffer;
    protected GLFloatBuffer2D pointBuffer;

    // point id (which can be any object) -> index into pointBuffer
    // good place for Guava BiMap here...
    protected Map<Object, Integer> idMap;
    protected Map<Integer, Object> indexMap;

    protected ReentrantLock lock;

    protected IntsArray searchResults;

    public DynamicPointSetPainter( )
    {
        this( DEFAULT_INITIAL_SIZE );
    }

    public DynamicPointSetPainter( int initialSize )
    {
        this.pointSize = DEFAULT_POINT_SIZE;
        this.bufferSize = initialSize;
        this.lock = new ReentrantLock( );

        this.idMap = new LinkedHashMap<Object, Integer>( );
        this.indexMap = new LinkedHashMap<Integer, Object>( );

        this.pointBuffer = new GLFloatBuffer2D( initialSize, true );
        this.colorBuffer = new GLFloatBuffer( initialSize, 4 );

        this.searchResults = new IntsArray( );
    }

    public Collection<Object> getGeoRange( double minX, double maxX, double minY, double maxY )
    {
        lock.lock( );
        try
        {
            this.searchResults.n = 0; // clear the search results
            this.pointBuffer.search( ( float ) minX, ( float ) maxX, ( float ) minY, ( float ) maxY, searchResults );

            final List<Object> resultList = new LinkedList<Object>( );
            for ( int i = 0; i < this.searchResults.n; i++ )
            {
                int index = this.searchResults.a[i];
                Object id = this.indexMap.get( index );
                if ( id != null )
                {
                    resultList.add( id );
                }
            }

            return resultList;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setPointSize( float size )
    {
        lock.lock( );
        try
        {
            this.pointSize = size;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void putPoints( BulkPointAccumulator accumulator )
    {
        lock.lock( );
        try
        {
            int newPoints = accumulator.getSize();
            int currentSize = idMap.size( );
            if ( bufferSize < currentSize + newPoints )
            {
                growBuffers( currentSize + newPoints );
            }

            mutatePositions( accumulator );
        }
        finally
        {
            lock.unlock( );
        }
    }
    
    public void putColors( BulkColorAccumulator accumulator )
    {
        lock.lock( );
        try
        {
            mutateColors( accumulator );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void putPoint( Object id, float posX, float posY )
    {
        putPoint( id, posX, posY, DEFAULT_COLOR );
    }

    public void putPoint( Object id, float posX, float posY, float[] color )
    {
        lock.lock( );
        try
        {
            int currentSize = idMap.size( );
            if ( bufferSize < currentSize + 1 )
            {
                growBuffers( currentSize + 1 );
            }

            int index = getIndex( id, true );
            mutatePosition( index, posX, posY );
            mutateColor( index, color );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void putColor( Object id, float[] color )
    {
        lock.lock( );
        try
        {
            int index = getIndex( id, false );
            mutateColor( index, color );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removePoint( Object id )
    {
        throw new UnsupportedOperationException( "removePoint() is not yet supported" );
    }

    @Override
    public void paintTo( GL gl, GlimpseBounds bounds, Axis2D axis )
    {
        lock.lock( );
        try
        {
            colorBuffer.bind( GLVertexAttribute.ATTRIB_COLOR_4D, gl );
            pointBuffer.bind( GLVertexAttribute.ATTRIB_POSITION_2D, gl );
            try
            {
                gl.glPointSize( pointSize );
                gl.glDrawArrays( GL.GL_POINTS, 0, idMap.size( ) );
            }
            finally
            {
                colorBuffer.unbind( gl );
                pointBuffer.unbind( gl );
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected void mutateColor( final int index, final float[] color )
    {
        this.colorBuffer.mutate( new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                data.position( index * length );
                data.put( color[0] );
                data.put( color[1] );
                data.put( color[2] );
                data.put( color.length == 4 ? color[3] : 1.0f );
            }
        } );
    }

    protected void mutatePosition( final int index, final float posX, final float posY )
    {
        this.pointBuffer.mutateIndexed( new IndexedMutator( )
        {
            @Override
            public int getUpdateIndex( )
            {
                return index;
            }

            @Override
            public void mutate( FloatBuffer data, int length )
            {
                data.position( index * length );
                data.put( posX );
                data.put( posY );
            }
        } );
    }
    
    protected int getIndexArray( List<Object> ids, boolean grow, int[] listIndex )
    {
        int size = ids.size();
        int minIndex = size;

        for ( int i = 0 ; i < size ; i++ )
        {
            int index = getIndex( ids.get( i ), grow );
            listIndex[i] = index;
            if ( minIndex > index ) minIndex = index;
        }

        return minIndex;
    }

    protected void mutatePositions( BulkPointAccumulator accumulator )
    {
        final List<Object> ids = accumulator.getIds( );
        final float[] v = accumulator.getVertices( );
        final int stride = accumulator.getStride( );
        final int size = accumulator.getSize();
        
        final int[] indexList = new int[size];
        final int minIndex = getIndexArray( ids, true, indexList );

        this.pointBuffer.mutateIndexed( new IndexedMutator( )
        {
            @Override
            public int getUpdateIndex( )
            {
                return minIndex;
            }

            @Override
            public void mutate( FloatBuffer data, int length )
            {
                for ( int i = 0 ; i < size ; i++ )
                {
                    data.position( indexList[i] * length );
                    data.put( v, i*stride, length );
                }
            }
        } );

        this.colorBuffer.mutate( new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                for ( int i = 0 ; i < size ; i++ )
                {
                    data.position( indexList[i] * length );
                    data.put( v, i*stride+2, length );
                }
            }
        } );
    }
    
    protected void mutateColors( BulkColorAccumulator accumulator )
    {
        final List<Object> ids = accumulator.getIds( );
        final float[] v = accumulator.getVertices( );
        final int stride = accumulator.getStride( );
        final int size = accumulator.getSize();
        
        final int[] indexList = new int[size];
        getIndexArray( ids, false, indexList );
        
        this.colorBuffer.mutate( new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                for ( int i = 0 ; i < size ; i++ )
                {
                    data.position( indexList[i] * length );
                    data.put( v, i*stride, length );
                }
            }
        } );
    }

    protected int getIndex( Object id, boolean grow )
    {
        Integer index = this.idMap.get( id );
        if ( index == null )
        {
            if ( grow )
            {
                index = idMap.size( );
                idMap.put( id, index );
                indexMap.put( index, id );
            }
            else
            {
                throw new IllegalArgumentException( String.format(  "Id %s does not exist.", id ) );
            }
        }

        return index;
    }

    protected void growBuffers( int minSize )
    {
        this.bufferSize = Math.max( ( int ) ( this.bufferSize * GROWTH_FACTOR ), minSize );

        this.pointBuffer.ensureCapacity( bufferSize );
        this.colorBuffer.ensureCapacity( bufferSize );
    }
    
    public static class BulkColorAccumulator
    {
        List<Object> ids;
        FloatsArray v;
        
        public BulkColorAccumulator( )
        {
            ids = new ArrayList<Object>( );
            v = new FloatsArray( );
        }
        
        public void add( Object id, float[] color )
        {
            if ( color.length != 3 && color.length != 4 )
            {
                throw new IllegalArgumentException( "Color array must be size 3 or 4" );
            }
            
            // grow the FloatsArray if necessary (4 for color)
            if ( v.n == v.a.length )
            {
                v.ensureCapacity( (int) Math.max( v.n + getStride( ) , v.n * GROWTH_FACTOR ) );
            }
            
            ids.add( id );
            v.append( color );
            if ( color.length == 3 ) v.append( 1.0f );
        }
        
        int getStride( )
        {
            return 4;
        }
        
        List<Object> getIds( )
        {
            return ids;
        }
        
        float[] getVertices( )
        {
            return v.a;
        }
        
        int getSize( )
        {
            return ids.size( );
        }
    }
    
    public static class BulkPointAccumulator
    {
        List<Object> ids;
        FloatsArray v;
        
        public BulkPointAccumulator( )
        {
            ids = new ArrayList<Object>( );
            v = new FloatsArray( );
        }
        
        public void add( Object id, float x, float y, float[] color )
        {
            if ( color.length != 3 && color.length != 4 )
            {
                throw new IllegalArgumentException( "Color array must be size 3 or 4" );
            }
            
            // grow the FloatsArray if necessary (2 for x/y and 4 for color)
            if ( v.n == v.a.length )
            {
                v.ensureCapacity( (int) Math.max( v.n + getStride( ) , v.n * GROWTH_FACTOR ) );
            }
            
            ids.add( id );
            v.append( x );
            v.append( y );
            v.append( color );
            if ( color.length == 3 ) v.append( 1.0f );
        }
        
        public void add( Object id, float x, float y )
        {
            add( id, x, y, DEFAULT_COLOR );
        }
        
        int getStride( )
        {
            return 6;
        }
        
        List<Object> getIds( )
        {
            return ids;
        }
        
        float[] getVertices( )
        {
            return v.a;
        }
        
        int getSize( )
        {
            return ids.size( );
        }
    }
}

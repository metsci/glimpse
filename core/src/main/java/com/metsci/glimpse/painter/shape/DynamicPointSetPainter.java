package com.metsci.glimpse.painter.shape;

import java.nio.FloatBuffer;
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

    public Collection<Point> getGeoRange( double minX, double maxX, double minY, double maxY )
    {
        lock.lock( );
        try
        {
            this.searchResults.n = 0; // clear the search results
            this.pointBuffer.search( ( float ) minX, ( float ) maxX, ( float ) minY, ( float ) maxY, searchResults );

            final List<Point> resultList = new LinkedList<Point>( );
            for ( int i = 0; i < this.searchResults.n; i++ )
            {
                int index = this.searchResults.a[i];
                Object id = this.indexMap.get( index );
                if ( id != null )
                {
                    resultList.add( new Point( id, index ) );
                }
            }

            this.pointBuffer.mutate( new Mutator( )
            {
                @Override
                public void mutate( FloatBuffer data, int length )
                {
                    for ( Point point : resultList )
                    {
                        point.x = data.get( point.index * length );
                        point.y = data.get( point.index * length + 1 );
                    }
                }
            } );

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

    public void putPoints( Object[] listIds, float[] listPosX, float[] listPosY )
    {
        putPoints( listIds, listPosX, listPosY, DEFAULT_COLOR );
    }

    public void putPoints( Object[] listIds, float[] listPosX, float[] listPosY, float[] color )
    {
        lock.lock( );
        try
        {
            int newPoints = listIds.length;
            int currentSize = idMap.size( );
            if ( bufferSize < currentSize + newPoints )
            {
                growBuffers( currentSize + newPoints );
            }

            mutatePositionsColors( listIds, listPosX, listPosY, color );
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

            int index = getIndex( id );
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
            Integer index = this.idMap.get( id );
            if ( index == null )
            {
                throw new IllegalArgumentException( String.format( "Id %s does not exist", id ) );
            }

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

    protected void mutatePositionsColors( final Object[] listIds, final float[] listPosX, final float[] listPosY, final float[] color )
    {
        final int size = listIds.length;
        final int[] listIndex = new int[size];
        int minIndex = size;

        for ( int i = 0; i < size; i++ )
        {
            int index = getIndex( listIds[i] );
            listIndex[i] = index;
            if ( minIndex > index ) minIndex = index;
        }

        final int finalMinIndex = minIndex;

        this.pointBuffer.mutateIndexed( new IndexedMutator( )
        {
            @Override
            public int getUpdateIndex( )
            {
                return finalMinIndex;
            }

            @Override
            public void mutate( FloatBuffer data, int length )
            {
                for ( int i = 0; i < size; i++ )
                {
                    data.position( listIndex[i] * length );
                    data.put( listPosX[i] );
                    data.put( listPosY[i] );
                }
            }
        } );

        this.colorBuffer.mutate( new Mutator( )
        {
            @Override
            public void mutate( FloatBuffer data, int length )
            {
                for ( int i = 0; i < size; i++ )
                {
                    data.position( listIndex[i] * length );
                    data.put( color[0] );
                    data.put( color[1] );
                    data.put( color[2] );
                    data.put( color.length == 4 ? color[3] : 1.0f );
                }
            }
        } );
    }

    protected int getIndex( Object id )
    {
        Integer index = this.idMap.get( id );
        if ( index == null )
        {
            index = idMap.size( );
            idMap.put( id, index );
            indexMap.put( index, id );
        }

        return index;
    }

    protected void growBuffers( int minSize )
    {
        this.bufferSize = Math.max( ( int ) ( this.bufferSize * GROWTH_FACTOR ), minSize );

        this.pointBuffer.ensureCapacity( bufferSize );
        this.colorBuffer.ensureCapacity( bufferSize );
    }

    public class Point
    {
        private Object id;
        private double x;
        private double y;
        private int index;

        Point( Object id, int index )
        {
            this.id = id;
            this.index = index;
        }

        Point( Object id, double x, double y, int index )
        {
            this.id = id;
            this.x = x;
            this.y = y;
            this.index = index;
        }

        public Object getId( )
        {
            return id;
        }

        public double getX( )
        {
            return x;
        }

        public double getY( )
        {
            return y;
        }

        public int getIndex( )
        {
            return index;
        }

        @Override
        public String toString( )
        {
            return String.format( "id: %s index: %d x: %f y: %f", id, index, x, y );
        }
    }
}

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

    public Collection<Point> get( final Collection<? extends Object> ids )
    {
        lock.lock( );
        try
        {
            final List<Point> returnList = new LinkedList<Point>( );

            this.pointBuffer.mutate( new Mutator( )
            {
                @Override
                public void mutate( FloatBuffer data, int length )
                {
                    for ( Object id : ids )
                    {
                        Integer index = idMap.get( id );
                        if ( index != null )
                        {
                            Point point = new Point( id, index );
                            
                            point.x = data.get( index * length );
                            point.y = data.get( index * length + 1 );
                        
                            returnList.add( point );
                        }
                    }
                }
            } );

            return returnList;
        }
        finally
        {
            lock.unlock( );
        }
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

    public void putPoints( List<BulkLoadPoint> points )
    {
        lock.lock( );
        try
        {
            int newPoints = points.size();
            int currentSize = idMap.size( );
            if ( bufferSize < currentSize + newPoints )
            {
                growBuffers( currentSize + newPoints );
            }

            mutatePositionsColors( points, true, true );
        }
        finally
        {
            lock.unlock( );
        }
    }
    
    public void putColors( List<BulkLoadPoint> points )
    {
        lock.lock( );
        try
        {
            mutatePositionsColors( points, false, true );
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

    protected void mutatePositionsColors( final List<BulkLoadPoint> points, boolean position, boolean color )
    {
        final int size = points.size();
        final int[] listIndex = new int[size];
        int minIndex = size;

        for ( int i = 0 ; i < size ; i++ )
        {
            int index = getIndex( points.get( i ).getId( ), position );
            listIndex[i] = index;
            if ( minIndex > index ) minIndex = index;
        }

        final int finalMinIndex = minIndex;

        if ( position )
        {
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
                    for ( int i = 0 ; i < size ; i++ )
                    {
                        BulkLoadPoint point = points.get( i );
                        
                        data.position( listIndex[i] * length );
                        data.put( point.x );
                        data.put( point.y );
                    }
                }
            } );
        }

        if ( color )
        {
            this.colorBuffer.mutate( new Mutator( )
            {
                @Override
                public void mutate( FloatBuffer data, int length )
                {
                    int i = 0;
                    for ( BulkLoadPoint point : points )
                    {
                        data.position( listIndex[i] * length );
                        
                        float[] color = point.color;
                        data.put( color[0] );
                        data.put( color[1] );
                        data.put( color[2] );
                        data.put( color.length == 4 ? color[3] : 1.0f );
                        
                        i += 1;
                    }
                }
            } );
        }
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
    
    public static class BulkLoadPoint
    {
        private Object id;
        private float x;
        private float y;
        private float[] color;
        
        public BulkLoadPoint( Object id, float x, float y )
        {
            this( id, x, y, DEFAULT_COLOR );
        }
        
        public BulkLoadPoint( Object id, float[] color )
        {
            this( id, 0, 0, color );
        }
        
        public BulkLoadPoint( Object id, float x, float y, float[] color  )
        {
            this.id = id;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public Object getId( )
        {
            return id;
        }

        public float getX( )
        {
            return x;
        }

        public float getY( )
        {
            return y;
        }

        public float[] getColor( )
        {
            return color;
        }

        @Override
        public int hashCode( )
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( id == null ) ? 0 : id.hashCode( ) );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            BulkLoadPoint other = ( BulkLoadPoint ) obj;
            if ( id == null )
            {
                if ( other.id != null ) return false;
            }
            else if ( !id.equals( other.id ) ) return false;
            return true;
        }
    }

    public static class Point
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

        @Override
        public int hashCode( )
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( id == null ) ? 0 : id.hashCode( ) );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            Point other = ( Point ) obj;
            if ( id == null )
            {
                if ( other.id != null ) return false;
            }
            else if ( !id.equals( other.id ) ) return false;
            return true;
        }
    }
}

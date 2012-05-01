package com.metsci.glimpse.painter.shape;

import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
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

public class DynamicLineSetPainter extends GlimpseDataPainter2D
{
    protected static final double GROWTH_FACTOR = 1.3;

    protected static final float DEFAULT_LINE_WIDTH = 2.0f;
    protected static final int DEFAULT_INITIAL_SIZE = 2000;
    protected static final float[] DEFAULT_COLOR = GlimpseColor.getBlack( );

    protected float lineWidth;

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

    public DynamicLineSetPainter( )
    {
        this( DEFAULT_INITIAL_SIZE );
    }

    public DynamicLineSetPainter( int initialSize )
    {
        this.lineWidth = DEFAULT_LINE_WIDTH;
        this.bufferSize = initialSize;
        this.lock = new ReentrantLock( );

        this.idMap = new LinkedHashMap<Object, Integer>( );
        this.indexMap = new LinkedHashMap<Integer, Object>( );

        this.pointBuffer = new GLFloatBuffer2D( initialSize * 2, false );
        this.colorBuffer = new GLFloatBuffer( initialSize * 2, 4 );

        this.searchResults = new IntsArray( );
    }

    public void setLineWidth( float size )
    {
        lock.lock( );
        try
        {
            this.lineWidth = size;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void putLines( List<BulkLoadLine> lines )
    {
        lock.lock( );
        try
        {
            int newPoints = lines.size( );
            int currentSize = idMap.size( );
            if ( bufferSize < currentSize + newPoints )
            {
                growBuffers( currentSize + newPoints );
            }

            mutatePositionsColors( lines, true, true );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void putColors( List<BulkLoadLine> colors )
    {
        lock.lock( );
        try
        {
            mutatePositionsColors( colors, false, true );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void putLine( Object id, float posX1, float posY1, float posX2, float posY2 )
    {
        putLine( id, posX1, posY1, posX2, posY2, DEFAULT_COLOR );
    }

    public void putLine( Object id, float posX1, float posY1, float posX2, float posY2, float[] color )
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
            mutatePosition( index, posX1, posY1, posX2, posY2 );
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

    public void removeLine( Object id )
    {
        throw new UnsupportedOperationException( "removeLine() is not yet supported" );
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
                gl.glLineWidth( lineWidth );
                gl.glDrawArrays( GL.GL_LINES, 0, idMap.size( ) * 2 );
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
                data.position( index * 2 * length );

                for ( int i = 0; i < 2; i++ )
                {
                    data.put( color[0] );
                    data.put( color[1] );
                    data.put( color[2] );
                    data.put( color.length == 4 ? color[3] : 1.0f );
                }
            }
        } );
    }

    protected void mutatePosition( final int index, final float posX1, final float posY1, final float posX2, final float posY2 )
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
                data.position( index * 2 * length );
                data.put( posX1 );
                data.put( posY1 );
                data.put( posX2 );
                data.put( posY2 );
            }
        } );
    }

    protected void mutatePositionsColors( final List<BulkLoadLine> lines, boolean position, boolean color )
    {
        final int size = lines.size( );
        final int[] listIndex = new int[size];
        int minIndex = size;

        for ( int i = 0; i < size; i++ )
        {
            int index = getIndex( lines.get( i ).getId( ), position );
            listIndex[i] = index;
            if ( minIndex > index ) minIndex = index;
        }

        final int finalMinIndex = minIndex * 2;

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
                    for ( int i = 0; i < size; i++ )
                    {
                        BulkLoadLine line = lines.get( i );

                        data.position( listIndex[i] * 2 * length );
                        data.put( line.x1 );
                        data.put( line.y1 );
                        data.put( line.x2 );
                        data.put( line.y2 );
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
                    for ( int i = 0; i < size; i++ )
                    {
                        data.position( listIndex[i] * 2 * length );

                        BulkLoadLine line = lines.get( i );

                        for ( int j = 0; j < 2; j++ )
                        {
                            float[] color = line.color;

                            data.put( color[0] );
                            data.put( color[1] );
                            data.put( color[2] );
                            data.put( color.length == 4 ? color[3] : 1.0f );
                        }
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

        this.pointBuffer.ensureCapacity( bufferSize * 2 );
        this.colorBuffer.ensureCapacity( bufferSize * 2 );
    }

    public static class BulkLoadLine
    {
        private Object id;
        private float x1, x2, y1, y2;
        private float[] color;

        public BulkLoadLine( Object id, float[] color )
        {
            this( id, 0, 0, 0, 0, color );
        }

        public BulkLoadLine( Object id, float x1, float y1, float x2, float y2 )
        {
            this( id, x1, y1, x2, y2, DEFAULT_COLOR );
        }

        public BulkLoadLine( Object id, float x1, float y1, float x2, float y2, float[] color )
        {
            this.id = id;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
        }

        public Object getId( )
        {
            return id;
        }

        public float getX1( )
        {
            return x1;
        }

        public float getX2( )
        {
            return x2;
        }

        public float getY1( )
        {
            return y1;
        }

        public float getY2( )
        {
            return y2;
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
            BulkLoadLine other = ( BulkLoadLine ) obj;
            if ( id == null )
            {
                if ( other.id != null ) return false;
            }
            else if ( !id.equals( other.id ) ) return false;
            return true;
        }
    }

}

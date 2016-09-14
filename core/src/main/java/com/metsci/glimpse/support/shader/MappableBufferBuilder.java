package com.metsci.glimpse.support.shader;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.support.line.util.LineUtils.*;
import static javax.media.opengl.GL.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.support.line.util.MappableBuffer;

public class MappableBufferBuilder
{
    protected MappableBufferData data;
    protected MappableBuffer buffer;
    protected boolean dirty;
    
    public MappableBufferBuilder( )
    {
        this( 0, 10 );
    }

    public MappableBufferBuilder( int initialNumVertices, int vboBlockSizeFactor )
    {
        this.data = new MappableBufferData( initialNumVertices );

        this.buffer = new MappableBuffer( GL_ARRAY_BUFFER, GL_STATIC_DRAW, vboBlockSizeFactor );
        this.dirty = true;
    }
    
    public MappableBuffer getBuffer( GL gl )
    {
        if ( dirty )
        {
            buffer.setFloats( gl, data.getBuffer( ) );
            this.dirty = false;
        }

        return buffer;
    }
    
    public void clear( )
    {
        this.data.clear( );
        this.dirty = true;
    }
    
    public int numFloats( )
    {
        return data.position( );
    }
    
    public void addVertex1f( float v )
    {
        data.addVertex1f( v );
        dirty = true;
    }
    
    public void addVertex2f( float x, float y )
    {
        data.addVertex2f( x, y );
        dirty = true;
    }
    
    public void addQuad2f( float x1, float y1, float x2, float y2 )
    {
        data.addVertex2f( x1, y1 );
        data.addVertex2f( x1, y2 );
        data.addVertex2f( x2, y1 );
        
        data.addVertex2f( x2, y1 );
        data.addVertex2f( x1, y2 );
        data.addVertex2f( x2, y2 );
        
        dirty = true;
    }
    
    public void addQuad1f( float x1y1, float x1y2, float x2y1, float x2y2 )
    {
        data.addVertex1f( x1y1 );
        data.addVertex1f( x1y2 );
        data.addVertex1f( x2y1 );
        
        data.addVertex1f( x2y1 );
        data.addVertex1f( x1y2 );
        data.addVertex1f( x2y2);
        
        dirty = true;
    }
    
    public static class MappableBufferData
    {
        protected FloatBuffer buffer;
        
        public MappableBufferData( int initialNumVertices )
        {
            this.buffer = newDirectFloatBuffer( 2*initialNumVertices );
        }
        
        public void clear( )
        {
            this.buffer.clear( );
        }

        public void addVertex1f( float v )
        {
            this.buffer = ensureAdditionalCapacity( buffer, 1, true );
            buffer.put( v );
        }
        
        public void addVertex2f( float x, float y )
        {
            this.buffer = ensureAdditionalCapacity( buffer, 2, true );
            buffer.put( x ).put( y );
        }

        public int position( )
        {
            return buffer.position( );
        }

        public FloatBuffer getBuffer( )
        {
            return flipped( buffer );
        }
   
    }
}

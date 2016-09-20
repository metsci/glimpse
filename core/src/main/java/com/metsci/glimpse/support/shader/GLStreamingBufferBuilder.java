package com.metsci.glimpse.support.shader;

import static com.jogamp.common.nio.Buffers.newDirectFloatBuffer;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.ensureAdditionalCapacity;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.flipped;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_STATIC_DRAW;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.gl.GLStreamingBuffer;

/**
 * A buffer used for generating geometry for drawing using glDrawArrays.
 * Suitable for cases where geometry is changing and being regenerated every frame.
 */
public class GLStreamingBufferBuilder
{
    protected GLStreamingBufferData data;
    protected GLStreamingBuffer buffer;
    protected boolean dirty;

    public GLStreamingBufferBuilder( )
    {
        this( 0, 10 );
    }

    public GLStreamingBufferBuilder( int initialNumVertices, int vboBlockSizeFactor )
    {
        this.data = new GLStreamingBufferData( initialNumVertices );

        this.buffer = new GLStreamingBuffer( GL_ARRAY_BUFFER, GL_STATIC_DRAW, vboBlockSizeFactor );
        this.dirty = true;
    }

    public void dispose( GL gl )
    {
        this.data.clear( );

        this.buffer.dispose( gl );
    }

    public GLStreamingBuffer getBuffer( GL gl )
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

    /**
     * Add vertices to form an axis-parallel rectangle defined by its bottom left and upper
     * right vertices. Six x-y vertices are added to the buffer forming two triangles defining the upper left
     * and lower right halves of the rectangle.
     * <p>
     * These vertices can be drawn using glDrawArrays in GL_TRIANGLES mode.
     * <p>
     * The following built-in shader programs support drawing with vertices added in this way:
     * <ol>
     *   <li>{@code com.metsci.glimpse.support.shader.FlatColorProgram}
     *   <li>{@code com.metsci.glimpse.support.shader.ArrayColorProgram}
     *   <li>{@code com.metsci.glimpse.support.shader.ColorTexture1DProgram}
     *   <li>{@code com.metsci.glimpse.support.shader.ColorTexture2DProgram}
     * </ol>
     * <p>
     * Typical usage will look like:
     * <pre>
     *     // At init-time
     *
     *     GLStreamingBufferBuilder builder = new GLStreamingBufferBuilder( );
     *     FlatColorProgram program = new FlatColorProgram( );
     *
     *     // At render-time
     *
     *     builder.clear( );
     *     builder.addQuad2f( x1, y1, x2, y2 );
     *
     *     program.begin( gl );
     *     try
     *     {
     *         program.setAxisOrtho( gl, axis );
     *
     *         program.draw( gl, builder, GlimpseColor.getBlack( ) );
     *     }
     *     program.end( gl );
     * </pre>
     * @param x1 x coordinate of bottom left vertex
     * @param y1 y coordinate of bottom left vertex
     * @param x2 x coordinate of top right vertex
     * @param y2 y coordinate of top right vertex
     */
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

    /**
     * Add one float associated with the corners of a axis-parallel rectangle.
     * Six values are added to the buffer forming two triangles which make up
     * the rectangle.
     * <p>
     * The order of the added values corresponds to the order of the vertices added
     * by {@code #addQuad2f(float, float, float, float)}.
     *
     * @see #addQuad2f(float, float, float, float)
     *
     * @param x1y1 value associated with lower left rectangle corner
     * @param x1y2 value associated with upper left rectangle corner
     * @param x2y1 value associated with lower right rectangle corner
     * @param x2y2 value associated with upper right rectangle corner
     */
    public void addQuad1f( float x1y1, float x1y2, float x2y1, float x2y2 )
    {
        data.addVertex1f( x1y1 );
        data.addVertex1f( x1y2 );
        data.addVertex1f( x2y1 );

        data.addVertex1f( x2y1 );
        data.addVertex1f( x1y2 );
        data.addVertex1f( x2y2 );

        dirty = true;
    }

    /**
     * Add four float values associated with the corners of a axis-parallel rectangle.
     * Values are added to the buffer corresponding to the vertices forming two triangles
     * which make up the rectangle.
     * <p>
     * The order of the added values corresponds to the order of the vertices added
     * by {@code #addQuad2f(float, float, float, float)}.
     *
     * @see #addQuad2f(float, float, float, float)
     *
     * @param x1y1 value associated with lower left rectangle corner
     * @param x1y2 value associated with upper left rectangle corner
     * @param x2y1 value associated with lower right rectangle corner
     * @param x2y2 value associated with upper right rectangle corner
     */
    public void addQuad4fv( float[] x1y1, float[] x1y2, float[] x2y1, float[] x2y2 )
    {
        data.addVertex4fv( x1y1 );
        data.addVertex4fv( x1y2 );
        data.addVertex4fv( x2y1 );

        data.addVertex4fv( x2y1 );
        data.addVertex4fv( x1y2 );
        data.addVertex4fv( x2y2 );

        dirty = true;
    }

    public void addQuadSolidColor( float[] color )
    {
        addQuad4fv( color, color, color, color );
    }

    public static class GLStreamingBufferData
    {
        protected FloatBuffer buffer;

        public GLStreamingBufferData( int initialNumVertices )
        {
            this.buffer = newDirectFloatBuffer( 2 * initialNumVertices );
        }

        public void clear( )
        {
            this.buffer.clear( );
        }

        public void addVertex1f( float v )
        {
            this.buffer = ensureAdditionalCapacity( buffer, 1, true );
            this.buffer.put( v );
        }

        public void addVertex2f( float x, float y )
        {
            this.buffer = ensureAdditionalCapacity( buffer, 2, true );
            this.buffer.put( x ).put( y );
        }

        public void addVertex4fv( float[] v )
        {
            this.buffer = ensureAdditionalCapacity( buffer, 4, true );
            this.buffer.put( v );
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

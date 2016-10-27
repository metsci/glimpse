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
package com.metsci.glimpse.painter.shape;

import static com.metsci.glimpse.gl.shader.GLShaderUtils.createProgram;
import static com.metsci.glimpse.gl.shader.GLShaderUtils.requireResourceText;
import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;
import static com.metsci.glimpse.util.GeneralUtils.floats;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_LINE_STRIP;
import static javax.media.opengl.GL2ES2.GL_STREAM_DRAW;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL3;

import com.google.common.collect.Sets;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLStreamingBuffer;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.shape.DynamicPointSetPainter.BulkColorAccumulator;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.util.primitives.FloatsArray;

/**
 * Efficiently paints dynamically changing groups of colored lines. Support is provided
 * for very efficiently changing the color of existing lines, as well as for adding
 * to existing sets of lines.
 *
 * @author ulman
 * @see com.metsci.glimpse.examples.misc.DynamicLinePainterExample
 */
public class DynamicLineSetPainter extends GlimpsePainterBase
{
    protected static final double GROWTH_FACTOR = 1.3;

    protected static final float DEFAULT_LINE_WIDTH = 2.0f;
    protected static final int DEFAULT_INITIAL_SIZE = 2000;
    protected static final float[] DEFAULT_COLOR = GlimpseColor.getBlack( );

    protected boolean rgbaBufferDirty = false;
    protected boolean xyBufferDirty = false;

    protected FloatBuffer rgbaBuffer;
    protected FloatBuffer xyBuffer;

    protected GLStreamingBuffer rgbaStreamingBuffer;
    protected GLStreamingBuffer xyStreamingBuffer;
    protected FloatBuffer tempBuffer;

    // point id (which can be any object) -> index into pointBuffer
    // good place for Guava BiMap here...
    protected Map<Object, Integer> idMap;
    protected Map<Integer, Object> indexMap;

    protected int initialSize;

    protected LineStyle style;
    protected DynamicLineSetPainterProgram prog;

    public DynamicLineSetPainter( )
    {
        this( DEFAULT_INITIAL_SIZE );
    }

    public DynamicLineSetPainter( int initialSize )
    {
        this.initialSize = initialSize;

        this.idMap = new LinkedHashMap<Object, Integer>( );
        this.indexMap = new LinkedHashMap<Integer, Object>( );

        this.xyBuffer = FloatBuffer.allocate( initialSize * 2 * 2 );
        this.rgbaBuffer = FloatBuffer.allocate( initialSize * 2 * 4 );

        this.xyStreamingBuffer = new GLStreamingBuffer( GL_STREAM_DRAW, 20 );
        this.rgbaStreamingBuffer = new GLStreamingBuffer( GL_STREAM_DRAW, 20 );

        this.style = new LineStyle( );

        this.style.rgba = floats( 0.7f, 0, 0, 1 );
        this.style.thickness_PX = DEFAULT_LINE_WIDTH;
        this.style.stippleEnable = false;
        this.style.stippleScale = 1;
        this.style.stipplePattern = ( short ) 0x00FF;

        this.prog = new DynamicLineSetPainterProgram( );
    }

    public void setDotted( boolean dotted )
    {
        this.painterLock.lock( );
        try
        {
            this.style.stippleEnable = dotted;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setDotted( int stippleFactor, short stipplePattern )
    {
        this.painterLock.lock( );
        try
        {
            this.style.stippleEnable = true;
            this.style.stippleScale = stippleFactor;
            this.style.stipplePattern = stipplePattern;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void setLineWidth( float size )
    {
        this.painterLock.lock( );
        try
        {
            this.style.thickness_PX = size;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void putLines( BulkLineAccumulator accumulator )
    {
        this.painterLock.lock( );
        try
        {
            int newPoints = accumulator.getAddedSize( );
            int currentSize = getSize( );
            if ( getCapacity( ) < currentSize + newPoints )
            {
                growBuffers( currentSize + newPoints );
            }

            deletePositions( accumulator );

            mutatePositions( accumulator );

            this.xyBufferDirty = true;
            this.rgbaBufferDirty = true;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void putColors( BulkColorAccumulator accumulator )
    {
        this.painterLock.lock( );
        try
        {
            mutateColors( accumulator );

            this.rgbaBufferDirty = true;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void putLine( Object id, float posX1, float posY1, float posX2, float posY2 )
    {
        putLine( id, posX1, posY1, posX2, posY2, DEFAULT_COLOR );
    }

    public void putLine( Object id, float posX1, float posY1, float posX2, float posY2, float[] color )
    {
        this.painterLock.lock( );
        try
        {
            int currentSize = getSize( );
            if ( getCapacity( ) < currentSize + 1 )
            {
                growBuffers( currentSize + 1 );
            }

            int index = getIndex( id, true );
            mutatePosition( index, posX1, posY1, posX2, posY2 );
            mutateColor( index, color );

            this.xyBufferDirty = true;
            this.rgbaBufferDirty = true;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void putColor( Object id, float[] color )
    {
        this.painterLock.lock( );
        try
        {
            int index = getIndex( id, false );
            mutateColor( index, color );

            this.rgbaBufferDirty = true;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void removeAll( )
    {
        this.painterLock.lock( );
        try
        {
            this.idMap.clear( );
            this.indexMap.clear( );
            this.xyBuffer = FloatBuffer.allocate( initialSize * 2 * 2 );
            this.rgbaBuffer = FloatBuffer.allocate( initialSize * 2 * 4 );
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    public void removeLine( Object id )
    {
        this.painterLock.lock( );
        try
        {
            int index = getIndex( id, false );
            if ( index == -1 ) return; // nothing to remove, the point does not exist
            deletePosition( index );

            this.xyBufferDirty = true;
            this.rgbaBufferDirty = true;
        }
        finally
        {
            this.painterLock.unlock( );
        }
    }

    @Override
    public void doPaintTo( GlimpseContext context )
    {
        GlimpseBounds bounds = getBounds( context );
        Axis2D axis = requireAxis2D( context );
        GL3 gl = context.getGL( ).getGL3( );

        enableStandardBlending( gl );
        try
        {
            int lineCount = getSize( );

            if ( lineCount == 0 ) return;

            if ( this.rgbaBufferDirty )
            {
                this.rgbaBuffer.position( 0 );
                this.rgbaBuffer.limit( lineCount * 2 * 4 );
                this.rgbaStreamingBuffer.setFloats( gl, rgbaBuffer );
                this.rgbaBuffer.clear( ); // doesn't actually erase data, just resets position/limit/mark
            }

            if ( this.xyBufferDirty )
            {
                this.xyBuffer.position( 0 );
                this.xyBuffer.limit( lineCount * 2 * 2 );
                this.xyStreamingBuffer.setFloats( gl, xyBuffer );
                this.xyBuffer.clear( ); // doesn't actually erase data, just resets position/limit/mark
            }

            this.prog.begin( gl );
            try
            {
                this.prog.setViewport( gl, bounds );
                this.prog.setAxisOrtho( gl, axis );
                this.prog.setStyle( gl, style );

                this.prog.draw( gl, xyStreamingBuffer, rgbaStreamingBuffer, 0, lineCount * 2 );
            }
            finally
            {
                this.prog.end( gl );
            }
        }
        finally
        {
            gl.glDisable( GL_BLEND );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        this.prog.dispose( context.getGL( ).getGL3( ) );

        this.rgbaStreamingBuffer.dispose( context.getGL( ) );
        this.xyStreamingBuffer.dispose( context.getGL( ) );
    }

    protected int getSize( )
    {
        return this.idMap.size( );
    }

    protected int getCapacity( )
    {
        // divide by ( 2 * 2 ) in order to count lines, not vertices
        // ( 2 vertices per line and 2 floats per vertex )
        return this.xyBuffer.capacity( ) / ( 2 * 2 );
    }

    protected static void shiftMaps( Map<Object, Integer> idMap, Map<Integer, Object> indexMap, Set<Integer> indices, int size )
    {
        for ( Integer index : indices )
        {
            Object id = indexMap.remove( index );
            idMap.remove( id );
        }

        //XXX this is inefficient for low index values
        // shift everything down in the index map
        int lastDelete = -1;
        int nextDelete = -1;
        int deleteCount = 0;
        for ( Integer index : indices )
        {
            lastDelete = nextDelete;
            nextDelete = index;
            deleteCount += 1;

            if ( lastDelete == -1 ) continue;

            shiftMaps( idMap, indexMap, lastDelete, nextDelete, deleteCount - 1 );
        }

        shiftMaps( idMap, indexMap, nextDelete, size, deleteCount );
    }

    protected static void shiftMaps( Map<Object, Integer> idMap, Map<Integer, Object> indexMap, int lastDelete, int nextDelete, int deleteCount )
    {
        for ( int i = lastDelete + 1; i < nextDelete; i++ )
        {
            Object id = indexMap.remove( i );
            indexMap.put( i - deleteCount, id );
            idMap.put( id, i - deleteCount );
        }
    }

    protected static void shift( FloatBuffer data, FloatBuffer tempBuffer, int length, int size, Set<Integer> indices )
    {
        int lastDelete = -1;
        int nextDelete = -1;
        int deleteCount = 0;
        for ( Integer index : indices )
        {
            lastDelete = nextDelete;
            nextDelete = index;

            if ( lastDelete != -1 )
            {
                shift( data, tempBuffer, nextDelete - lastDelete - 1, lastDelete - deleteCount + 1, lastDelete + 1, length );
            }

            deleteCount += 1;
        }

        nextDelete += 1;

        shift( data, tempBuffer, size - nextDelete, nextDelete - deleteCount, nextDelete, length );
    }

    /**
     * @param data buffer to shift
     * @param shiftCount number of logical indices to shift (each index represents 'length' buffer entries)
     * @param toIndex the logical index to start copying data to
     * @param fromIndex the logical index to start copying data from
     * @param length the number of buffer entries per logical index
     */
    protected static void shift( FloatBuffer data, FloatBuffer tempBuffer, int shiftCount, int toIndex, int fromIndex, int length )
    {
        if ( shiftCount == 0 || toIndex == fromIndex ) return;

        // lazy load tempBuffer (only needed if removePoint is called)
        if ( tempBuffer == null || tempBuffer.capacity( ) < shiftCount * length )
        {
            tempBuffer = FloatBuffer.allocate( shiftCount * length );
        }

        // copy the data to shift into tempBuffer
        tempBuffer.limit( shiftCount * length );
        tempBuffer.position( 0 );
        data.limit( ( fromIndex + shiftCount ) * length );
        data.position( fromIndex * length );
        tempBuffer.put( data );

        // copy the data back, shifted left by one, to data buffer
        tempBuffer.rewind( );
        data.limit( ( toIndex + shiftCount ) * length );
        data.position( toIndex * length );
        data.put( tempBuffer );
    }

    protected void deletePositions( final Set<Integer> indices )
    {
        if ( indices.isEmpty( ) ) return;

        final int size = this.getSize( );

        shiftMaps( idMap, indexMap, indices, size );

        shift( this.rgbaBuffer, tempBuffer, 4 * 2, size, indices );
        shift( this.xyBuffer, tempBuffer, 2 * 2, size, indices );

    }

    protected void deletePositions( BulkLineAccumulator accum )
    {
        Set<Integer> indices = Sets.newTreeSet( );

        for ( Object id : accum.getRemovedIds( ) )
        {
            Integer index = this.idMap.get( id );
            if ( index != null )
            {
                indices.add( index );
            }
        }

        deletePositions( indices );
    }

    protected void deletePosition( int index )
    {
        deletePositions( Collections.singleton( index ) );
    }

    protected void mutateColor( final int index, final float[] color )
    {
        this.rgbaBuffer.position( index * 2 * 4 );

        for ( int i = 0; i < 2; i++ )
        {
            this.rgbaBuffer.put( color[0] );
            this.rgbaBuffer.put( color[1] );
            this.rgbaBuffer.put( color[2] );
            this.rgbaBuffer.put( color.length == 4 ? color[3] : 1.0f );
        }
    }

    protected void mutatePosition( final int index, final float posX1, final float posY1, final float posX2, final float posY2 )
    {
        this.xyBuffer.position( index * 2 * 2 );
        this.xyBuffer.put( posX1 );
        this.xyBuffer.put( posY1 );
        this.xyBuffer.put( posX2 );
        this.xyBuffer.put( posY2 );
    }

    protected int getIndexArray( List<Object> ids, int[] listIndex )
    {
        int size = ids.size( );
        int minIndex = size;

        for ( int i = 0; i < size; i++ )
        {
            int index = getIndex( ids.get( i ), true );
            listIndex[i] = index;
            if ( minIndex > index ) minIndex = index;
        }

        return minIndex;
    }

    protected void mutatePositions( BulkLineAccumulator accumulator )
    {
        final List<Object> ids = accumulator.getAddedIds( );
        final float[] v = accumulator.getVertices( );
        final int stride = accumulator.getStride( );
        final int size = accumulator.getAddedSize( );

        final int[] indexList = new int[size];
        getIndexArray( ids, indexList );

        for ( int i = 0; i < size; i++ )
        {
            this.xyBuffer.position( indexList[i] * 2 * 2 );
            this.xyBuffer.put( v, i * stride, 2 * 2 );
        }

        for ( int i = 0; i < size; i++ )
        {
            this.rgbaBuffer.position( indexList[i] * 2 * 4 );

            for ( int j = 0; j < 2; j++ )
            {
                this.rgbaBuffer.put( v, i * stride + 4, 4 );
            }
        }
    }

    protected void mutateColors( BulkColorAccumulator accumulator )
    {
        final List<Object> ids = accumulator.getIds( );
        final float[] v = accumulator.getVertices( );
        final int stride = accumulator.getStride( );
        final int size = accumulator.getSize( );

        final int[] indexList = new int[size];
        getIndexArray( ids, indexList );

        for ( int i = 0; i < size; i++ )
        {
            this.rgbaBuffer.position( indexList[i] * 2 * 4 );

            for ( int j = 0; j < 2; j++ )
            {
                this.rgbaBuffer.put( v, i * stride, 4 );
            }
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
                return -1;
            }
        }

        return index;
    }

    protected void growBuffers( int minSize )
    {
        minSize = Math.max( ( int ) ( getCapacity( ) * GROWTH_FACTOR ), minSize );

        this.xyBuffer = growBuffer( this.xyBuffer, minSize * 2 * 2 );
        this.rgbaBuffer = growBuffer( this.rgbaBuffer, minSize * 4 * 2 );
    }

    static FloatBuffer growBuffer( FloatBuffer buffer, int size )
    {
        if ( buffer.capacity( ) < size )
        {
            FloatBuffer b = FloatBuffer.allocate( size );
            buffer.rewind( );
            b.put( buffer ).rewind( );
            return b;
        }
        else
        {
            return buffer;
        }
    }

    public static class BulkLineAccumulator
    {
        List<Object> removedIds;
        List<Object> addedIds;
        FloatsArray addedVertices;

        public BulkLineAccumulator( )
        {
            removedIds = new ArrayList<Object>( );
            addedIds = new ArrayList<Object>( );
            addedVertices = new FloatsArray( );
        }

        public void add( Object id, float x1, float y1, float x2, float y2, float[] color )
        {
            if ( color.length != 3 && color.length != 4 )
            {
                throw new IllegalArgumentException( "Color array must be size 3 or 4" );
            }

            // grow the FloatsArray if necessary (4 for x/y and 4 for color)
            if ( addedVertices.n == addedVertices.a.length )
            {
                addedVertices.ensureCapacity( ( int ) Math.max( addedVertices.n + getStride( ), addedVertices.n * GROWTH_FACTOR ) );
            }

            addedIds.add( id );

            addedVertices.append( x1 );
            addedVertices.append( y1 );
            addedVertices.append( x2 );
            addedVertices.append( y2 );
            addedVertices.append( color );

            if ( color.length == 3 ) addedVertices.append( 1.0f );
        }

        public void add( Object id, float x1, float y1, float x2, float y2 )
        {
            add( id, x1, y1, x2, y2, DEFAULT_COLOR );
        }

        public void remove( Object id )
        {
            removedIds.add( id );
        }

        int getStride( )
        {
            return 8;
        }

        List<Object> getRemovedIds( )
        {
            return removedIds;
        }

        List<Object> getAddedIds( )
        {
            return addedIds;
        }

        float[] getVertices( )
        {
            return addedVertices.a;
        }

        int getAddedSize( )
        {
            return addedIds.size( );
        }
    }

    public static class DynamicLineSetPainterProgram
    {

        public static final String lineVertShader_GLSL = requireResourceText( "shaders/line/DynamicLineSetPainter/line.vs" );
        public static final String lineGeomShader_GLSL = requireResourceText( "shaders/line/DynamicLineSetPainter/line.gs" );
        public static final String lineFragShader_GLSL = requireResourceText( "shaders/line/DynamicLineSetPainter/line.fs" );

        public static class LineProgramHandles
        {
            public final int program;

            public final int AXIS_RECT;
            public final int VIEWPORT_SIZE_PX;

            public final int LINE_THICKNESS_PX;
            public final int FEATHER_THICKNESS_PX;

            public final int STIPPLE_ENABLE;
            public final int STIPPLE_SCALE;
            public final int STIPPLE_PATTERN;

            public final int inXy;
            public final int inRgba;

            public LineProgramHandles( GL2ES2 gl )
            {
                this.program = createProgram( gl, lineVertShader_GLSL, lineGeomShader_GLSL, lineFragShader_GLSL );

                this.AXIS_RECT = gl.glGetUniformLocation( program, "AXIS_RECT" );
                this.VIEWPORT_SIZE_PX = gl.glGetUniformLocation( program, "VIEWPORT_SIZE_PX" );

                this.LINE_THICKNESS_PX = gl.glGetUniformLocation( program, "LINE_THICKNESS_PX" );
                this.FEATHER_THICKNESS_PX = gl.glGetUniformLocation( program, "FEATHER_THICKNESS_PX" );

                this.STIPPLE_ENABLE = gl.glGetUniformLocation( program, "STIPPLE_ENABLE" );
                this.STIPPLE_SCALE = gl.glGetUniformLocation( program, "STIPPLE_SCALE" );
                this.STIPPLE_PATTERN = gl.glGetUniformLocation( program, "STIPPLE_PATTERN" );

                this.inXy = gl.glGetAttribLocation( program, "inXy" );
                this.inRgba = gl.glGetAttribLocation( program, "inRgba" );
            }
        }

        protected LineProgramHandles handles;

        public DynamicLineSetPainterProgram( )
        {
            this.handles = null;
        }

        /**
         * Returns the raw GL handles for the shader program, uniforms, and attributes. Compiles and
         * links the program, if necessary.
         * <p>
         * It is perfectly acceptable to use these handles directly, rather than calling the convenience
         * methods in this class. However, the convenience methods are intended to be a fairly stable API,
         * whereas the handles may change frequently.
         */
        public LineProgramHandles handles( GL2ES2 gl )
        {
            if ( this.handles == null )
            {
                this.handles = new LineProgramHandles( gl );
            }

            return this.handles;
        }

        public void begin( GL2ES2 gl )
        {
            if ( this.handles == null )
            {
                this.handles = new LineProgramHandles( gl );
            }

            gl.getGL3( ).glBindVertexArray( GLUtils.defaultVertexAttributeArray( gl ) );
            gl.glUseProgram( this.handles.program );
            gl.glEnableVertexAttribArray( this.handles.inXy );
            gl.glEnableVertexAttribArray( this.handles.inRgba );
        }

        public void setViewport( GL2ES2 gl, GlimpseBounds bounds )
        {
            setViewport( gl, bounds.getWidth( ), bounds.getHeight( ) );
        }

        public void setViewport( GL2ES2 gl, int viewportWidth, int viewportHeight )
        {
            gl.glUniform2f( this.handles.VIEWPORT_SIZE_PX, viewportWidth, viewportHeight );
        }

        public void setAxisOrtho( GL2ES2 gl, Axis2D axis )
        {
            setOrtho( gl, ( float ) axis.getMinX( ), ( float ) axis.getMaxX( ), ( float ) axis.getMinY( ), ( float ) axis.getMaxY( ) );
        }

        public void setPixelOrtho( GL2ES2 gl, GlimpseBounds bounds )
        {
            setOrtho( gl, 0, bounds.getWidth( ), 0, bounds.getHeight( ) );
        }

        public void setOrtho( GL2ES2 gl, float xMin, float xMax, float yMin, float yMax )
        {
            gl.glUniform4f( this.handles.AXIS_RECT, xMin, xMax, yMin, yMax );
        }

        public void setStyle( GL2ES2 gl, LineStyle style )
        {
            if ( style.stippleEnable )
            {
                gl.glUniform1i( this.handles.STIPPLE_ENABLE, 1 );
                gl.glUniform1f( this.handles.STIPPLE_SCALE, style.stippleScale );
                gl.glUniform1i( this.handles.STIPPLE_PATTERN, style.stipplePattern );
            }
            else
            {
                gl.glUniform1i( this.handles.STIPPLE_ENABLE, 0 );
            }

            gl.glUniform1f( this.handles.LINE_THICKNESS_PX, style.thickness_PX );
            gl.glUniform1f( this.handles.FEATHER_THICKNESS_PX, style.feather_PX );
        }

        public void draw( GL2ES2 gl, GLStreamingBuffer xyVbo, GLStreamingBuffer rgbaVbo, int first, int count )
        {
            gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo.buffer( gl ) );
            gl.glVertexAttribPointer( this.handles.inXy, 2, GL_FLOAT, false, 0, xyVbo.sealedOffset( ) );

            gl.glBindBuffer( GL_ARRAY_BUFFER, rgbaVbo.buffer( gl ) );
            gl.glVertexAttribPointer( this.handles.inRgba, 4, GL_FLOAT, false, 0, rgbaVbo.sealedOffset( ) );

            gl.glDrawArrays( GL_LINE_STRIP, first, count );
        }

        public void end( GL2ES2 gl )
        {
            gl.glDisableVertexAttribArray( this.handles.inXy );
            gl.glDisableVertexAttribArray( this.handles.inRgba );
            gl.glUseProgram( 0 );
            gl.getGL3( ).glBindVertexArray( 0 );
        }

        /**
         * Deletes the program, and resets this object to the way it was before {@link #begin(GL2ES2)}
         * was first called.
         * <p>
         * This object can be safely reused after being disposed, but in most cases there is no
         * significant advantage to doing so.
         */
        public void dispose( GL2ES2 gl )
        {
            if ( this.handles != null )
            {
                gl.glDeleteProgram( this.handles.program );
                this.handles = null;
            }
        }
    }
}

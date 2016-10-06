package com.metsci.glimpse.support.shader.line;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;
import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.gl.IntRangeSet;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.gl.GLEditableBuffer2;
import com.metsci.glimpse.util.primitives.sorted.SortedInts;

public class LineStrip
{

    protected final GLEditableBuffer2 xyBuffer;
    protected final GLEditableBuffer2 flagsBuffer;
    protected final GLEditableBuffer2 mileageBuffer;


    public LineStrip( int initialVertices )
    {
        this( initialVertices, 10 );
    }

    public LineStrip( int initialVertices, int scratchBlockSizeFactor )
    {
        this.xyBuffer = new GLEditableBuffer2( GL_ARRAY_BUFFER, 2 * initialVertices * SIZEOF_FLOAT, scratchBlockSizeFactor );
        this.flagsBuffer = new GLEditableBuffer2( GL_ARRAY_BUFFER, 1 * initialVertices * SIZEOF_BYTE, scratchBlockSizeFactor );
        this.mileageBuffer = new GLEditableBuffer2( GL_ARRAY_BUFFER, 1 * initialVertices * SIZEOF_FLOAT, scratchBlockSizeFactor );
    }

    public void grow( int additionalVertices )
    {
        this.xyBuffer.growFloats( 2 * additionalVertices );
        this.flagsBuffer.growBytes( 1 * additionalVertices );
        this.mileageBuffer.growFloats( 1 * additionalVertices );
    }

    public void put( FloatBuffer xys )
    {
        int firstVertex = this.flagsBuffer.numBytes( );
        this.put( firstVertex, xys );
    }

    public void put( int firstVertex, FloatBuffer xys )
    {
        int numVertices = xys.remaining( ) / 2;

        this.xyBuffer.putFloats( xys );

        ByteBuffer flagsEdit = this.flagsBuffer.editBytes( 1 * firstVertex, 1 * numVertices );
        flagsEdit.put( b );

        FloatBuffer mileageEdit = this.mileageBuffer.editFloats( 1 * firstVertex, 1 * numVertices );
        mileageEdit.put( f );
    }

    public void put( int firstVertex, float[] xys )
    {
        this.put( firstVertex, xys, 0, xys.length );
    }

    public void put( int firstVertex, float[] xys, int position, int limit )
    {
        int numVertices = ( limit - position ) / 2;

        this.xyBuffer.putFloats( xys );

        ByteBuffer flagsEdit = this.flagsBuffer.editBytes( 1 * firstVertex, 1 * numVertices );
        flagsEdit.put( b );

        FloatBuffer mileageEdit = this.mileageBuffer.editFloats( 1 * firstVertex, 1 * numVertices );
        mileageEdit.put( f );
    }

    public int numVertices( )
    {
        return this.flagsBuffer.numBytes( );
    }

    public void dispose( GL gl )
    {
        this.xyBuffer.dispose( gl );
        this.flagsBuffer.dispose( gl );
        this.mileageBuffer.dispose( gl );
    }

}

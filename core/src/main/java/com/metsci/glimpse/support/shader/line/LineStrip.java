package com.metsci.glimpse.support.shader.line;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.support.shader.line.LinePathData.*;
import static com.metsci.glimpse.support.shader.line.LineUtils.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;
import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.gl.GLEditableBuffer2;
import com.metsci.glimpse.util.primitives.DoublesArray;

public class LineStrip
{

    protected final GLEditableBuffer2 xyBuffer;
    protected final GLEditableBuffer2 flagsBuffer;
    protected final GLEditableBuffer2 mileageBuffer;
    protected final DoublesArray segmentMileages;


    public LineStrip( int initialVertices )
    {
        this( initialVertices, 10 );
    }

    public LineStrip( int initialVertices, int scratchBlockSizeFactor )
    {
        this.xyBuffer = new GLEditableBuffer2( GL_ARRAY_BUFFER, 2 * initialVertices * SIZEOF_FLOAT, scratchBlockSizeFactor );
        this.flagsBuffer = new GLEditableBuffer2( GL_ARRAY_BUFFER, 1 * initialVertices * SIZEOF_BYTE, scratchBlockSizeFactor );
        this.mileageBuffer = new GLEditableBuffer2( GL_ARRAY_BUFFER, 1 * initialVertices * SIZEOF_FLOAT, scratchBlockSizeFactor );
        this.segmentMileages = new DoublesArray( 1 * initialVertices );
    }

    public int size( )
    {
        return this.flagsBuffer.numBytes( );
    }

    public void grow( int additionalVertices )
    {
        this.xyBuffer.growFloats( 2 * additionalVertices );
        this.flagsBuffer.growBytes( 1 * additionalVertices );
        this.mileageBuffer.growFloats( 1 * additionalVertices );
        this.segmentMileages.ensureCapacity( this.segmentMileages.n + ( 1 * additionalVertices ) );
    }

    public void put( FloatBuffer xys )
    {
        int firstVertex = this.flagsBuffer.numBytes( );
        this.put( firstVertex, xys );
    }

    public void put( int firstVertex, FloatBuffer xys )
    {
        if ( xys.remaining( ) >= 2 )
        {
            int putFirst = firstVertex + 1;
            int putCount = xys.remaining( ) / 2;

            // If we're writing the first visible vertex, we need to write a leader as well
            boolean putLeader = ( firstVertex == 0 );
            if ( putLeader )
            {
                putFirst--;
                putCount++;
            }

            // If we're writing the last visible vertex, we need to write a trailer as well
            int oldCount = this.flagsBuffer.numBytes( );
            boolean putTrailer = ( putFirst + putCount >= oldCount - 1 );
            if ( putTrailer )
            {
                putCount++;
            }

            // Update xys
            FloatBuffer xyEdit = this.xyBuffer.editFloats( 2 * putFirst, 2 * putCount );
            {
                if ( putLeader )
                {
                    float xLeader = xys.get( 0 );
                    float yLeader = xys.get( 1 );
                    xyEdit.put( xLeader ).put( yLeader );
                }

                xyEdit.put( xys );

                if ( putTrailer )
                {
                    int iLast = xys.limit( ) - 2;
                    float xTrailer = xys.get( iLast + 0 );
                    float yTrailer = xys.get( iLast + 1 );
                    xyEdit.put( xTrailer ).put( yTrailer );
                }
            }

            // Update flags
            ByteBuffer flagsEdit = this.flagsBuffer.editBytes( 1 * putFirst, 1 * putCount );
            int newCount = this.flagsBuffer.numBytes( );
            for ( int i = putFirst; i < putFirst + putCount; i++ )
            {
                if ( i == 0 )
                {
                    // Leading phantom vertex
                    flagsEdit.put( ( byte ) 0 );
                }
                else if ( i == 1 )
                {
                    // First visible vertex
                    flagsEdit.put( ( byte ) 0 );
                }
                else if ( i == newCount - 2 )
                {
                    // Last visible vertex
                    flagsEdit.put( ( byte ) FLAGS_CONNECT );
                }
                else if ( i == newCount - 1 )
                {
                    // Trailing phantom vertex
                    flagsEdit.put( ( byte ) 0 );
                }
                else
                {
                    // Regular visible vertex
                    flagsEdit.put( ( byte ) ( FLAGS_CONNECT | FLAGS_JOIN ) );
                }
            }

            // Update mileages, all the way to the end
            FloatBuffer mileageEdit = this.mileageBuffer.editFloats( 1 * putFirst, 1 * newCount );
            {
                updateMileageBuffer( this.xyBuffer.hostFloats( ),
                                     this.flagsBuffer.hostBytes( ),
                                     mileageEdit,
                                     false,
                                     ppvAspectRatio );
            }
        }
    }

    public void dispose( GL gl )
    {
        this.xyBuffer.dispose( gl );
        this.flagsBuffer.dispose( gl );
        this.mileageBuffer.dispose( gl );
    }

}

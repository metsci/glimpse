package com.metsci.glimpse.support.shader.line;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.support.shader.line.LinePathData.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;
import static java.lang.Math.*;
import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.gl.GLEditableBuffer2;
import com.metsci.glimpse.util.primitives.DoublesArray;

public class LineStrip
{

    protected final GLEditableBuffer2 xyBuffer;
    protected final GLEditableBuffer2 flagsBuffer;
    protected final GLEditableBuffer2 mileageBuffer;
    protected final DoublesArray segmentMileages;


    public LineStrip( int initialCapacity )
    {
        this( initialCapacity, 10 );
    }

    public LineStrip( int initialCapacity, int scratchBlockSizeFactor )
    {
        this.xyBuffer = new GLEditableBuffer2( GL_ARRAY_BUFFER, 2 * initialCapacity * SIZEOF_FLOAT, scratchBlockSizeFactor );
        this.flagsBuffer = new GLEditableBuffer2( GL_ARRAY_BUFFER, 1 * initialCapacity * SIZEOF_BYTE, scratchBlockSizeFactor );
        this.mileageBuffer = new GLEditableBuffer2( GL_ARRAY_BUFFER, 1 * initialCapacity * SIZEOF_FLOAT, scratchBlockSizeFactor );
        this.segmentMileages = new DoublesArray( 1 * initialCapacity );
    }

    public int actualSize( )
    {
        return this.flagsBuffer.numBytes( );
    }

    public int logicalSize( )
    {
        int actualSize = this.actualSize( );
        return ( actualSize == 0 ? 0 : actualSize - 2 );
    }

    public int xyBuffer( GL2ES3 gl )
    {
        return this.xyBuffer.deviceBuffer( gl );
    }

    public int flagsBuffer( GL2ES3 gl )
    {
        return this.flagsBuffer.deviceBuffer( gl );
    }

    public int mileageBuffer( GL2ES3 gl, double ppvAspectRatio )
    {
        return this.mileageBuffer.deviceBuffer( gl );
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
        int firstVertex = this.logicalSize( );
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
            {
                int xyFirst = 2 * putFirst;
                int xyCount = 2 * putCount;
                FloatBuffer xyEdit = this.xyBuffer.editFloats( xyFirst, xyCount );

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
            {
                int flagsFirst = ( putTrailer ? max( 0, min( putFirst, oldCount - 2 ) ) : putFirst );
                int flagsCount = putFirst + putCount - flagsFirst;
                ByteBuffer flagsEdit = this.flagsBuffer.editBytes( flagsFirst, flagsCount );

                int newCount = this.flagsBuffer.numBytes( );
                for ( int i = flagsFirst; i < flagsFirst + flagsCount; i++ )
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
            }

            // Update mileages, all the way to the end
            {
                // Include the previous mileage so updateMileage() can read it, but position after it so that updateMileage() doesn't write it
                int mileageFirst = max( 0, putFirst - 1 );
                int mileageCount = this.flagsBuffer.numBytes( ) - mileageFirst;
                FloatBuffer mileageEdit = this.mileageBuffer.editFloats( mileageFirst, mileageCount );
                mileageEdit.position( putFirst - mileageFirst );

                FloatBuffer xySlice = sliced( this.xyBuffer.hostFloats( ), 2*mileageFirst, 2*mileageCount );
                ByteBuffer flagsSlice = sliced( this.flagsBuffer.hostBytes( ), 1*mileageFirst, 1*mileageCount );

                // WIP: ppvAspectRatio
                updateMileageBuffer( xySlice, flagsSlice, mileageEdit, false, 1.0 );
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

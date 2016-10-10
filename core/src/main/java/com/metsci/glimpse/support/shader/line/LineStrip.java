package com.metsci.glimpse.support.shader.line;

import static com.jogamp.common.nio.Buffers.SIZEOF_BYTE;
import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.metsci.glimpse.support.shader.line.LinePathData.FLAGS_CONNECT;
import static com.metsci.glimpse.support.shader.line.LinePathData.FLAGS_JOIN;
import static com.metsci.glimpse.support.shader.line.LinePathData.updateMileageBuffer;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.sliced;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.gl.GLEditableBuffer2;
import com.metsci.glimpse.support.shader.line.LineProgram.LineBufferHandles;
import com.metsci.glimpse.util.primitives.DoublesArray;
import com.metsci.glimpse.util.primitives.rangeset.IntRangeSet;
import com.metsci.glimpse.util.primitives.sorted.SortedInts;

public class LineStrip
{

    public static int logicalToActualIndex( int logicalIndex )
    {
        return ( logicalIndex + 1 );
    }

    public static int logicalToActualSize( int logicalSize )
    {
        return ( logicalSize == 0 ? 0 : logicalSize + 2 );
    }

    public static int actualToLogicalSize( int actualSize )
    {
        return ( actualSize == 0 ? 0 : actualSize - 2 );
    }

    protected final GLEditableBuffer2 xyBuffer;
    protected final GLEditableBuffer2 flagsBuffer;
    protected final GLEditableBuffer2 mileageBuffer;
    protected final DoublesArray segmentMileages;

    protected int logicalSize;

    public LineStrip( int logicalCapacity )
    {
        this( logicalCapacity, 10 );
    }

    public LineStrip( int logicalCapacity, int scratchBlockSizeFactor )
    {
        int actualCapacity = logicalToActualSize( logicalCapacity );
        this.xyBuffer = new GLEditableBuffer2( 2 * actualCapacity * SIZEOF_FLOAT, scratchBlockSizeFactor );
        this.flagsBuffer = new GLEditableBuffer2( 1 * actualCapacity * SIZEOF_BYTE, scratchBlockSizeFactor );
        this.mileageBuffer = new GLEditableBuffer2( 1 * actualCapacity * SIZEOF_FLOAT, scratchBlockSizeFactor );
        this.segmentMileages = new DoublesArray( 1 * actualCapacity );

        this.logicalSize = 0;
    }

    public int logicalSize( )
    {
        return this.logicalSize;
    }

    public int actualSize( )
    {
        return logicalToActualSize( this.logicalSize );
    }

    public void grow( int logicalAdditional )
    {
        int actualAdditional = logicalToActualIndex( logicalAdditional );
        this.xyBuffer.growFloats( 2 * actualAdditional );
        this.flagsBuffer.growBytes( 1 * actualAdditional );
        this.mileageBuffer.growFloats( 1 * actualAdditional );
        this.segmentMileages.ensureCapacity( this.segmentMileages.n + ( 1 * actualAdditional ) );
    }

    public FloatBuffer edit( int logicalCount )
    {
        int logicalFirst = this.logicalSize;
        return this.edit( logicalFirst, logicalCount );
    }

    public FloatBuffer edit( int logicalFirst, int logicalCount )
    {
        this.logicalSize = max( this.logicalSize, logicalFirst + logicalCount );

        int actualFirst = logicalToActualIndex( logicalFirst );
        int actualCount = logicalCount;
        return this.xyBuffer.editFloats( 2 * actualFirst, 2 * actualCount );
    }

    public LineBufferHandles deviceBuffers( GL2ES3 gl, boolean needMileage, double ppvAspectRatio )
    {
        FloatBuffer xyRead = this.xyBuffer.hostFloats( );
        IntRangeSet xyDirtyByteSet = this.xyBuffer.dirtyByteRanges( );

        // Update leader xy
        int actualFirstVisible = logicalToActualIndex( 0 );
        boolean putLeader = xyDirtyByteSet.contains( 2 * actualFirstVisible * SIZEOF_FLOAT );
        if ( putLeader )
        {
            float xLeader = xyRead.get( 2 * actualFirstVisible + 0 );
            float yLeader = xyRead.get( 2 * actualFirstVisible + 1 );
            FloatBuffer xyEdit = this.xyBuffer.editFloats( 2 * ( actualFirstVisible - 1 ), 2 );
            xyEdit.put( xLeader ).put( yLeader );
        }

        // Update trailer xy
        int actualLastVisible = logicalToActualIndex( this.logicalSize - 1 );
        boolean putTrailer = xyDirtyByteSet.contains( 2 * actualLastVisible * SIZEOF_FLOAT );
        if ( putTrailer )
        {
            float xTrailer = xyRead.get( 2 * actualLastVisible + 0 );
            float yTrailer = xyRead.get( 2 * actualLastVisible + 1 );
            FloatBuffer xyEdit = this.xyBuffer.editFloats( 2 * ( actualLastVisible + 1 ), 2 );
            xyEdit.put( xTrailer ).put( yTrailer );
        }

        // Update flags
        int oldActualLastVisible = this.flagsBuffer.sizeBytes( ) - 2;
        SortedInts xyDirtyByteRanges = xyDirtyByteSet.ranges( );
        for ( int r = 0; r < xyDirtyByteRanges.n( ); r += 2 )
        {
            int xyByteRangeStart = xyDirtyByteRanges.v( r + 0 );
            int xyByteRangeEnd = xyDirtyByteRanges.v( r + 1 );

            int dirtyFirst = xyByteRangeStart / ( 2 * SIZEOF_FLOAT );
            int editFirst = ( putTrailer ? max( 0, min( dirtyFirst, oldActualLastVisible ) ) : dirtyFirst );
            int editCount = ( xyByteRangeEnd / ( 2 * SIZEOF_FLOAT ) ) - editFirst;
            ByteBuffer flagsEdit = this.flagsBuffer.editBytes( editFirst, editCount );

            for ( int actualIndex = editFirst; actualIndex < editFirst + editCount; actualIndex++ )
            {
                if ( actualIndex < actualFirstVisible )
                {
                    // Leading phantom vertex
                    flagsEdit.put( ( byte ) 0 );
                }
                else if ( actualIndex == actualFirstVisible )
                {
                    // First visible vertex
                    flagsEdit.put( ( byte ) 0 );
                }
                else if ( actualIndex == actualLastVisible )
                {
                    // Last visible vertex
                    flagsEdit.put( ( byte ) FLAGS_CONNECT );
                }
                else if ( actualIndex > actualLastVisible )
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

        // Update mileage
        if ( needMileage && !xyDirtyByteRanges.isEmpty( ) )
        {
            // XXX: Recompute all mileages if ppvAspectRatio has changed noticeably

            // Include the previous mileage so that updateMileage() can read it,
            // but position after it so that updateMileage() doesn't write it
            int dirtyFirst = xyDirtyByteRanges.first( ) / ( 2 * SIZEOF_FLOAT );
            int editFirst = max( 0, dirtyFirst - 1 );
            int editCount = this.actualSize( ) - editFirst;
            FloatBuffer mileageEdit = this.mileageBuffer.editFloats( editFirst, editCount );
            mileageEdit.position( dirtyFirst - editFirst );

            FloatBuffer xySlice = sliced( this.xyBuffer.hostFloats( ), 2 * editFirst, 2 * editCount );
            ByteBuffer flagsSlice = sliced( this.flagsBuffer.hostBytes( ), 1 * editFirst, 1 * editCount );

            updateMileageBuffer( xySlice, flagsSlice, mileageEdit, false, ppvAspectRatio );
        }

        return new LineBufferHandles( this.xyBuffer.deviceBuffer( gl ), this.flagsBuffer.deviceBuffer( gl ), ( needMileage ? this.mileageBuffer.deviceBuffer( gl ) : 0 ) );
    }

    public void dispose( GL gl )
    {
        this.xyBuffer.dispose( gl );
        this.flagsBuffer.dispose( gl );
        this.mileageBuffer.dispose( gl );
    }

}

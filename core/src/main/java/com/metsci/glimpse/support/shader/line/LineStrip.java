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
package com.metsci.glimpse.support.shader.line;

import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.metsci.glimpse.support.shader.line.LinePathData.FLAGS_CONNECT;
import static com.metsci.glimpse.support.shader.line.LinePathData.FLAGS_JOIN;
import static com.metsci.glimpse.support.shader.line.LinePathData.updateMileageBuffer;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.sliced;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;

import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.support.shader.line.LineProgram.LineBufferHandles;
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


    protected final GLEditableBuffer xyBuffer;
    protected final GLEditableBuffer flagsBuffer;
    protected final GLEditableBuffer mileageBuffer;

    protected double mileagePpvAspectRatio;

    protected boolean hCloseLoop;
    protected boolean dCloseLoop;


    public LineStrip( )
    {
        this( 0 );
    }

    public LineStrip( int logicalCapacity )
    {
        int actualCapacity = logicalToActualSize( logicalCapacity );
        this.xyBuffer = new GLEditableBuffer( GL_DYNAMIC_DRAW, 2 * actualCapacity * SIZEOF_FLOAT );
        this.flagsBuffer = new GLEditableBuffer( GL_DYNAMIC_DRAW, 0 );
        this.mileageBuffer = new GLEditableBuffer( GL_DYNAMIC_DRAW, 0 );

        this.mileagePpvAspectRatio = Double.NaN;

        this.hCloseLoop = false;
        this.dCloseLoop = false;
    }

    public void setLoop( boolean isLoop )
    {
        this.hCloseLoop = isLoop;
    }

    public int logicalSize( )
    {
        return actualToLogicalSize( this.actualSize( ) );
    }

    public int actualSize( )
    {
        return ( this.xyBuffer.sizeFloats( ) / 2 );
    }

    public FloatBuffer logicalXys( )
    {
        FloatBuffer actualXys = this.xyBuffer.hostFloats( );
        return sliced( actualXys, 2 * logicalToActualIndex( 0 ), 2 * this.logicalSize( ) );
    }

    public void clear( )
    {
        this.xyBuffer.clear( );
    }

    public void truncate( int logicalSize )
    {
        this.xyBuffer.truncateFloats( 2 * logicalToActualSize( logicalSize ) );
    }

    public void grow1( float x, float y )
    {
        this.grow( 1 ).put( x ).put( y );
    }

    public void grow2( float xA, float yA, float xB, float yB )
    {
        this.grow( 2 ).put( xA ).put( yA ).put( xB ).put( yB );
    }

    public void growNv( float[] array )
    {
        this.growNv( array, 0, array.length / 2 );
    }

    public void growNv( float[] array, int offset, int logicalCount )
    {
        this.grow( logicalCount ).put( array, offset, 2 * logicalCount );
    }

    public FloatBuffer grow( int logicalCount )
    {
        int newActualSize = logicalToActualSize( this.logicalSize( ) + logicalCount );
        this.xyBuffer.ensureCapacityFloats( 2 * newActualSize );
        return this.edit( logicalCount );
    }

    public FloatBuffer edit( int logicalCount )
    {
        int logicalFirst = this.logicalSize( );
        return this.edit( logicalFirst, logicalCount );
    }

    public FloatBuffer edit( int logicalFirst, int logicalCount )
    {
        int actualFirst = logicalToActualIndex( logicalFirst );

        // Include an extra vertex beyond the end, to leave room for a trailing vertex
        FloatBuffer xyEdit = this.xyBuffer.editFloats( 2 * actualFirst, 2 * ( logicalCount + 1 ) );
        xyEdit.limit( 2 * logicalCount );

        return xyEdit;
    }

    public LineBufferHandles deviceBuffers( GL2ES3 gl, boolean needMileage, double ppvAspectRatio )
    {
        return deviceBuffers( gl, needMileage, ppvAspectRatio, 1.0000000001 );
    }

    public LineBufferHandles deviceBuffers( GL2ES3 gl, boolean needMileage, double ppvAspectRatio, double ppvAspectRatioChangeThreshold )
    {
        this.flagsBuffer.ensureCapacityBytes( 1 * this.actualSize( ) );
        this.mileageBuffer.ensureCapacityFloats( 1 * this.actualSize( ) );

        FloatBuffer xyRead = this.xyBuffer.hostFloats( );
        IntRangeSet xyDirtyByteSet = this.xyBuffer.dirtyByteRanges( );

        int actualFirstVisible = logicalToActualIndex( 0 );
        int actualSecondVisible = logicalToActualIndex( 1 );
        int actualNextToLastVisible = logicalToActualIndex( this.logicalSize( ) - 2 );
        int actualLastVisible = logicalToActualIndex( this.logicalSize( ) - 1 );

        // If loop status has changed, trigger update of phantom vertices
        boolean closeLoop = ( this.hCloseLoop && this.logicalSize( ) >= 2 );
        if ( this.dCloseLoop != closeLoop )
        {
            this.xyBuffer.editFloats( 2 * actualFirstVisible, 4 );
            this.xyBuffer.editFloats( 2 * actualNextToLastVisible, 4 );
            this.dCloseLoop = closeLoop;
        }

        // Update leader xy
        int actualLeaderOrig = ( closeLoop ? actualNextToLastVisible : actualFirstVisible );
        boolean putLeader = xyDirtyByteSet.contains( 2 * actualLeaderOrig * SIZEOF_FLOAT );
        if ( putLeader )
        {
            float xLeader = xyRead.get( 2 * actualLeaderOrig + 0 );
            float yLeader = xyRead.get( 2 * actualLeaderOrig + 1 );
            FloatBuffer xyEdit = this.xyBuffer.editFloats( 2 * ( actualFirstVisible - 1 ), 2 );
            xyEdit.put( xLeader ).put( yLeader );
        }

        // Update trailer xy
        int actualTrailerOrig = ( closeLoop ? actualSecondVisible : actualLastVisible );
        boolean putTrailer = xyDirtyByteSet.contains( 2 * actualTrailerOrig * SIZEOF_FLOAT );
        if ( putTrailer )
        {
            float xTrailer = xyRead.get( 2 * actualTrailerOrig + 0 );
            float yTrailer = xyRead.get( 2 * actualTrailerOrig + 1 );
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
                    flagsEdit.put( ( byte ) ( this.dCloseLoop ? FLAGS_JOIN : 0 ) );
                }
                else if ( actualIndex == actualLastVisible )
                {
                    // Last visible vertex
                    flagsEdit.put( ( byte ) ( this.dCloseLoop ? ( FLAGS_CONNECT | FLAGS_JOIN ) : FLAGS_CONNECT ) );
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
        if ( needMileage )
        {
            // If any relevant values are NaN, all inequalities will return false, so keepPpvAspectRatio will be false
            boolean keepPpvAspectRatio = ( mileagePpvAspectRatio / ppvAspectRatioChangeThreshold <= ppvAspectRatio && ppvAspectRatio <= mileagePpvAspectRatio * ppvAspectRatioChangeThreshold );

            int dirtyFirst;
            if ( keepPpvAspectRatio )
            {
                dirtyFirst = xyDirtyByteRanges.first( ) / ( 2 * SIZEOF_FLOAT );
            }
            else
            {
                this.mileagePpvAspectRatio = ppvAspectRatio;
                dirtyFirst = 0;
            }

            // Include the previous mileage so that updateMileage() can read it,
            // but position after it so that updateMileage() doesn't write it
            int editFirst = max( 0, dirtyFirst - 1 );
            int editCount = this.actualSize( ) - editFirst;
            FloatBuffer mileageEdit = this.mileageBuffer.editFloats( editFirst, editCount );
            mileageEdit.position( dirtyFirst - editFirst );

            FloatBuffer xySlice = sliced( this.xyBuffer.hostFloats( ), 2 * editFirst, 2 * editCount );
            ByteBuffer flagsSlice = sliced( this.flagsBuffer.hostBytes( ), 1 * editFirst, 1 * editCount );

            // Some mileage values may already have been computed based on this.mileagePpvAspectRatio, so always
            // compute the rest of them based on this.mileagePpvAspectRatio -- not on ppvAspectRatio
            updateMileageBuffer( xySlice, flagsSlice, mileageEdit, false, this.mileagePpvAspectRatio );
        }
        else
        {
            // The xy-dirty ranges are about to get cleared, but we don't want to assume
            // that all mileages are up to date -- so just mark all mileages as out of date
            this.mileagePpvAspectRatio = Double.NaN;
        }

        // Remember device-buffers' loop status
        this.dCloseLoop = this.hCloseLoop;

        return new LineBufferHandles( this.xyBuffer.deviceBuffer( gl ), this.flagsBuffer.deviceBuffer( gl ), ( needMileage ? this.mileageBuffer.deviceBuffer( gl ) : 0 ) );
    }

    public void dispose( GL gl )
    {
        this.xyBuffer.dispose( gl );
        this.flagsBuffer.dispose( gl );
        this.mileageBuffer.dispose( gl );
    }

}

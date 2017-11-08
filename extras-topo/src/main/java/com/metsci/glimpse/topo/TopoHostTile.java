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
package com.metsci.glimpse.topo;

import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.deallocateDirectBuffers;

import java.nio.ByteBuffer;

import com.metsci.glimpse.topo.io.TopoDataType;

public class TopoHostTile
{

    /**
     * North edge, NOT including border cells.
     */
    public final double northLat_DEG;

    /**
     * South edge, NOT including border cells.
     */
    public final double southLat_DEG;

    /**
     * West edge, NOT including border cells.
     */
    public final double westLon_DEG;

    /**
     * East edge, NOT including border cells.
     */
    public final double eastLon_DEG;

    /**
     * Number of rows in dataBytes, INCLUDING border rows.
     */
    public final int numDataRows;

    /**
     * Number of columns in dataBytes, INCLUDING border columns.
     */
    public final int numDataCols;

    public final int numBorderCells;
    public final ByteBuffer dataBytes;
    public final TopoDataType dataType;

    public long frameNumOfLastUse;


    public TopoHostTile( double northLat_DEG,
                         double southLat_DEG,
                         double westLon_DEG,
                         double eastLon_DEG,

                         int numDataRows,
                         int numDataCols,
                         int numBorderCells,
                         ByteBuffer dataBytes,
                         TopoDataType dataType,

                         long frameNum )
    {
        int expectedBytes = dataType.numBytes * numDataRows * numDataCols;
        int actualBytes = dataBytes.remaining( );
        if ( actualBytes != expectedBytes )
        {
            throw new RuntimeException( "Unexpected buffer size: expected = " + expectedBytes + " bytes, found = " + actualBytes + " bytes" );
        }

        this.northLat_DEG = northLat_DEG;
        this.southLat_DEG = southLat_DEG;
        this.westLon_DEG = westLon_DEG;
        this.eastLon_DEG = eastLon_DEG;

        this.numDataRows = numDataRows;
        this.numDataCols = numDataCols;
        this.numBorderCells = numBorderCells;
        this.dataBytes = dataBytes;
        this.dataType = dataType;

        this.frameNumOfLastUse = frameNum;
    }

    public void dispose( )
    {
        deallocateDirectBuffers( this.dataBytes );
    }

}

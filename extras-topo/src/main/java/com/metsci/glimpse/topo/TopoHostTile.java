/*
 * Copyright (c) 2020, Metron, Inc.
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
     * North edge of data, INCLUDING border rows.
     */
    public final double northLat_RAD;

    /**
     * South edge of data, INCLUDING border rows.
     */
    public final double southLat_RAD;

    /**
     * West edge of data, INCLUDING border columns.
     */
    public final double westLon_RAD;

    /**
     * East edge of data, INCLUDING border columns.
     */
    public final double eastLon_RAD;

    /**
     * Number of rows in dataBytes, INCLUDING border rows.
     */
    public final int numDataRows;

    /**
     * Number of columns in dataBytes, INCLUDING border columns.
     */
    public final int numDataCols;

    public final ByteBuffer dataBytes;
    public final TopoDataType dataType;

    public final double borderSize_RAD;

    public long frameNumOfLastUse;


    public TopoHostTile( double northLat_RAD,
                         double southLat_RAD,
                         double eastLon_RAD,
                         double westLon_RAD,

                         int numDataRows,
                         int numDataCols,
                         ByteBuffer dataBytes,
                         TopoDataType dataType,

                         double borderSize_RAD,

                         long frameNum )
    {
        int expectedBytes = dataType.numBytes * numDataRows * numDataCols;
        int actualBytes = dataBytes.remaining( );
        if ( actualBytes != expectedBytes )
        {
            throw new RuntimeException( "Unexpected buffer size: expected = " + expectedBytes + " bytes, found = " + actualBytes + " bytes" );
        }

        this.northLat_RAD = northLat_RAD;
        this.southLat_RAD = southLat_RAD;
        this.eastLon_RAD = eastLon_RAD;
        this.westLon_RAD = westLon_RAD;

        this.numDataRows = numDataRows;
        this.numDataCols = numDataCols;
        this.dataBytes = dataBytes;
        this.dataType = dataType;

        this.borderSize_RAD = borderSize_RAD;

        this.frameNumOfLastUse = frameNum;
    }

    public void dispose( )
    {
        deallocateDirectBuffers( this.dataBytes );
    }

}

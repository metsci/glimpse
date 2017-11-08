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
package com.metsci.glimpse.topo.io;

import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.logging.Logger;

public class TopoDataFile
{
    private static final Logger logger = getLogger( TopoDataFile.class );


    public final int numRows;
    public final int numCols;
    public final double cellSize_DEG;
    public final double southLat_DEG;
    public final double northLat_DEG;
    public final double westLon_DEG;
    public final double eastLon_DEG;

    public final File dataFile;
    public final TopoDataType dataType;
    public final ByteOrder dataByteOrder;
    public final TopoDataUnits dataUnits;


    public TopoDataFile( int numRows,
                         int numCols,
                         double cellSize_DEG,
                         double southLat_DEG,
                         double westLon_DEG,

                         File dataFile,
                         TopoDataType dataType,
                         ByteOrder dataByteOrder,
                         TopoDataUnits dataUnits ) throws IOException
    {
        this.numRows = numRows;
        this.numCols = numCols;
        this.cellSize_DEG = cellSize_DEG;
        this.southLat_DEG = southLat_DEG;
        this.westLon_DEG = westLon_DEG;

        this.northLat_DEG = this.southLat_DEG + ( this.numRows * this.cellSize_DEG );
        this.eastLon_DEG = this.westLon_DEG + ( this.numCols * this.cellSize_DEG );

        this.dataFile = dataFile;
        this.dataType = dataType;
        this.dataByteOrder = dataByteOrder;
        this.dataUnits = dataUnits;
    }

    public void requireValid( ) throws IOException
    {
        long expectedSize = this.numRows * this.numCols * this.dataType.numBytes;
        long actualSize = this.dataFile.length( );

        if ( actualSize == 0 && !this.dataFile.exists( ) )
        {
            throw new IOException( "Topo data file does not exist: file = " + this.dataFile.getAbsolutePath( ) );
        }
        else if ( actualSize < expectedSize )
        {
            throw new IOException( "Topo data file is too small: file = " + this.dataFile.getAbsolutePath( ) + ", expected-size = " + expectedSize + " bytes, actual-size = " + actualSize + " bytes" );
        }
        else if ( actualSize > expectedSize )
        {
            logger.warning( "Topo data file is larger than expected: file = " + this.dataFile.getAbsolutePath( ) + ", expected-size = " + expectedSize + " bytes, actual-size = " + actualSize + " bytes" );
        }
    }

    public String toLongString( String linePrefix )
    {
        StringBuilder s = new StringBuilder( );

        s.append( linePrefix ).append( "numRows:       " ).append( this.numRows ).append( "\n" );
        s.append( linePrefix ).append( "numCols:       " ).append( this.numCols ).append( "\n" );
        s.append( linePrefix ).append( "cellSize_DEG:  " ).append( this.cellSize_DEG ).append( "\n" );
        s.append( linePrefix ).append( "southLat_DEG:  " ).append( this.southLat_DEG ).append( "\n" );
        s.append( linePrefix ).append( "northLat_DEG:  " ).append( this.northLat_DEG ).append( "\n" );
        s.append( linePrefix ).append( "westLon_DEG:   " ).append( this.westLon_DEG ).append( "\n" );
        s.append( linePrefix ).append( "eastLon_DEG:   " ).append( this.eastLon_DEG ).append( "\n" );
        s.append( linePrefix ).append( "dataFile:      " ).append( this.dataFile.getAbsolutePath( ) ).append( "\n" );
        s.append( linePrefix ).append( "dataType:      " ).append( this.dataType ).append( "\n" );
        s.append( linePrefix ).append( "dataByteOrder: " ).append( this.dataByteOrder ).append( "\n" );
        s.append( linePrefix ).append( "dataUnits:     " ).append( this.dataUnits ).append( "\n" );

        return s.toString( );
    }

}

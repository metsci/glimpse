/*
 * Copyright (c) 2019, Metron, Inc.
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

import static com.jogamp.common.nio.Buffers.newDirectByteBuffer;
import static com.metsci.glimpse.util.GeneralUtils.clamp;
import static com.metsci.glimpse.util.GeneralUtils.min;
import static com.metsci.glimpse.util.units.Angle.degreesToRadians;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.metsci.glimpse.topo.io.TopoDataFile;
import com.metsci.glimpse.topo.io.TopoDataType;
import com.metsci.glimpse.util.io.MappedFile;

public class TopoLevel
{

    public final TopoDataFile file;
    protected final MappedFile mapped;

    public final int numRows;
    public final int numCols;
    public final double cellSize_DEG;
    public final double northLat_DEG;
    public final double southLat_DEG;
    public final double eastLon_DEG;
    public final double westLon_DEG;

    public final double tileWidth_DEG;
    public final double bandHeight_DEG;

    public final int numBands;
    public final int numTiles;
    protected final TopoTileBounds[][] tileBounds;

    public TopoLevel( TopoDataFile file, int maxRowsPerBand, int maxColsPerTile ) throws IOException
    {
        file.requireValid( );

        this.file = file;
        this.mapped = new MappedFile( this.file.dataFile, this.file.dataByteOrder );

        this.numRows = this.file.numRows;
        this.numCols = this.file.numCols;
        this.cellSize_DEG = this.file.cellSize_DEG;
        this.northLat_DEG = this.file.northLat_DEG;
        this.southLat_DEG = this.file.southLat_DEG;
        this.eastLon_DEG = this.file.eastLon_DEG;
        this.westLon_DEG = this.file.westLon_DEG;

        int maxMemmappableRows = Integer.MAX_VALUE / ( this.numCols * this.file.dataType.numBytes );
        int rowsPerBand = min( maxRowsPerBand, this.numRows, maxMemmappableRows );
        int colsPerTile = min( maxColsPerTile, this.numCols );
        this.tileWidth_DEG = colsPerTile * this.cellSize_DEG;
        this.bandHeight_DEG = rowsPerBand * this.cellSize_DEG;

        this.numBands = ceilDiv( this.numRows, rowsPerBand );
        this.numTiles = ceilDiv( this.numCols, colsPerTile );

        this.tileBounds = new TopoTileBounds[this.numBands][this.numTiles];
        for ( int bandNum = 0; bandNum < this.numBands; bandNum++ )
        {
            int bandFirstRow = bandNum * rowsPerBand;
            int bandNumRows = min( rowsPerBand, this.numRows - bandFirstRow );
            double bandNorthLat_DEG = this.northLat_DEG - ( this.cellSize_DEG * ( bandFirstRow ) );
            double bandSouthLat_DEG = this.northLat_DEG - ( this.cellSize_DEG * ( bandFirstRow + bandNumRows ) );

            for ( int tileNum = 0; tileNum < this.numTiles; tileNum++ )
            {
                int tileFirstCol = tileNum * colsPerTile;
                int tileNumCols = min( colsPerTile, this.numCols - tileFirstCol );
                double tileWestLon_DEG = this.westLon_DEG + ( this.cellSize_DEG * ( tileFirstCol ) );
                double tileEastLon_DEG = this.westLon_DEG + ( this.cellSize_DEG * ( tileFirstCol + tileNumCols ) );

                this.tileBounds[bandNum][tileNum] = new TopoTileBounds( bandFirstRow,
                        bandNumRows,
                        bandSouthLat_DEG,
                        bandNorthLat_DEG,

                        tileFirstCol,
                        tileNumCols,
                        tileWestLon_DEG,
                        tileEastLon_DEG );
            }
        }
    }

    public static int ceilDiv( int a, int b )
    {
        return -Math.floorDiv( -a, b );
    }

    public TopoTileBounds tileBounds( int bandNum, int tileNum )
    {
        return this.tileBounds[bandNum][tileNum];
    }

    public TopoHostTile copyTile( int bandNum, int tileNum, int numBorderCells )
    {
        TopoTileBounds tile = this.tileBounds[bandNum][tileNum];

        double borderSize_RAD = degreesToRadians( numBorderCells * this.cellSize_DEG );
        double dataNorthLat_RAD = degreesToRadians( tile.northLat_DEG ) + borderSize_RAD;
        double dataSouthLat_RAD = degreesToRadians( tile.southLat_DEG ) - borderSize_RAD;
        double dataEastLon_RAD = degreesToRadians( tile.eastLon_DEG ) + borderSize_RAD;
        double dataWestLon_RAD = degreesToRadians( tile.westLon_DEG ) - borderSize_RAD;

        int rFirst = tile.firstRow - numBorderCells;
        int cFirst = tile.firstCol - numBorderCells;
        int rLast = tile.firstRow + tile.numRows + numBorderCells - 1;
        int cLast = tile.firstCol + tile.numCols + numBorderCells - 1;

        int numDataRows = tile.numRows + 2 * numBorderCells;
        int numDataCols = tile.numCols + 2 * numBorderCells;
        TopoDataType dataType = this.file.dataType;
        ByteBuffer dataBytes = newDirectByteBuffer( dataType.numBytes * numDataRows * numDataCols );
        dataBytes.order( this.file.dataByteOrder );

        for ( int r = rFirst; r <= rLast; r++ )
        {
            long rowNum = clamp( r, 0, this.numRows - 1 );

            // For columns beyond the level's left edge, copy data from along the right edge
            if ( cFirst < 0 )
            {
                long firstCol = this.numCols + cFirst;
                int numCols = 0 - cFirst;
                this.copyTo( rowNum, firstCol, numCols, dataBytes );
            }

            // For columns between the level's left and right edges, copy data from those columns
            {
                long firstCol = max( cFirst, 0 );
                long lastCol = min( cLast, this.numCols - 1 );
                int numCols = ( int ) ( lastCol + 1 - firstCol );
                this.copyTo( rowNum, firstCol, numCols, dataBytes );
            }

            // For columns beyond the level's right edge, copy data from along the left edge
            if ( cLast >= this.numCols )
            {
                long firstCol = 0;
                int numCols = cLast - ( this.numCols - 1 );
                this.copyTo( rowNum, firstCol, numCols, dataBytes );
            }
        }

        dataBytes.flip( );

        return new TopoHostTile( dataNorthLat_RAD,
                dataSouthLat_RAD,
                dataEastLon_RAD,
                dataWestLon_RAD,

                numDataRows,
                numDataCols,
                dataBytes,
                dataType,

                borderSize_RAD,

                0 );
    }

    protected void copyTo( long rowNum, long firstCol, int numCols, ByteBuffer dest )
    {
        int bytesPerValue = this.file.dataType.numBytes;
        long firstByte = bytesPerValue * ( ( rowNum * this.numCols ) + firstCol );
        int numBytes = bytesPerValue * numCols;
        this.mapped.copyTo( firstByte, numBytes, dest );
    }

    public void dispose( )
    {
        this.mapped.dispose( );
    }

}

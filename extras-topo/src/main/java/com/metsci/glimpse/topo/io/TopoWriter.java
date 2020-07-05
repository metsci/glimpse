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
package com.metsci.glimpse.topo.io;

import static com.google.common.base.Charsets.UTF_8;
import static com.metsci.glimpse.topo.io.TopoDataType.TOPO_I2;
import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.lang.String.format;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;
import com.metsci.glimpse.util.io.MappedFile;

public class TopoWriter
{

    public static TopoDataset writeTopoDataset( TopoDataFile inLevel, File outDir ) throws IOException
    {
        List<TopoDataFile> levels = new ArrayList<>( );

        TopoDataFile level0 = writeTopoDataLevel( inLevel, outDir, "L0" );
        levels.add( level0 );

        TopoDataFile level = level0;
        for ( int levelNum = 1; level.numRows > 8192 || level.numCols > 8192; levelNum++ )
        {
            level = writeNextTopoDataLevel( level, outDir, format( "L%d", levelNum ) );
            levels.add( level );
        }

        return new TopoDataset( levels );
    }

    public static TopoDataFile writeTopoDataLevel( TopoDataFile inLevel, File outDir, String outLevelName ) throws IOException
    {
        TopoDataFile outLevel = new TopoDataFile( inLevel.numRows,
                                                  inLevel.numCols,
                                                  inLevel.cellSize_DEG,
                                                  inLevel.southLat_DEG,
                                                  inLevel.westLon_DEG,

                                                  new File( outDir, outLevelName + ".bin" ),
                                                  TOPO_I2,
                                                  LITTLE_ENDIAN,
                                                  inLevel.dataUnits );

        long outNumBytes = ( ( long ) outLevel.numRows ) * ( ( long ) outLevel.numCols ) * outLevel.dataType.numBytes;
        MappedFile outMapped = new MappedFile( outLevel.dataFile, outLevel.dataByteOrder, outNumBytes );

        MappedFile inMapped = new MappedFile( inLevel.dataFile, inLevel.dataByteOrder );
        switch ( inLevel.dataType )
        {
            case TOPO_I2:
            {
                int wordBytes = inLevel.dataType.numBytes;
                long totalWords = inMapped.size( ) / wordBytes;
                int maxSliceWords = 512 * 1024 * 1024;
                for ( long firstSliceWord = 0; firstSliceWord < totalWords; firstSliceWord += maxSliceWords )
                {
                    int sliceWords = ( int ) min( maxSliceWords, totalWords - firstSliceWord );
                    MappedByteBuffer inSlice = inMapped.slice( firstSliceWord * wordBytes, sliceWords * wordBytes );
                    MappedByteBuffer outSlice = outMapped.slice( firstSliceWord * wordBytes, sliceWords * wordBytes );
                    outSlice.put( inSlice );
                }
            }
            break;

            case TOPO_F4:
            {
                int inWordBytes = inLevel.dataType.numBytes;
                int outWordBytes = outLevel.dataType.numBytes;
                long totalWords = inMapped.size( ) / inWordBytes;
                int maxSliceWords = 256 * 1024 * 1024;
                for ( long firstSliceWord = 0; firstSliceWord < totalWords; firstSliceWord += maxSliceWords )
                {
                    int sliceWords = ( int ) min( maxSliceWords, totalWords - firstSliceWord );
                    MappedByteBuffer inSlice = inMapped.slice( firstSliceWord * inWordBytes, sliceWords * inWordBytes );
                    MappedByteBuffer outSlice = outMapped.slice( firstSliceWord * outWordBytes, sliceWords * outWordBytes );

                    FloatBuffer inFloats = inSlice.asFloatBuffer( );
                    ShortBuffer outShorts = outSlice.asShortBuffer( );
                    while ( inFloats.hasRemaining( ) )
                    {
                        float inFloat = inFloats.get( );
                        short outShort = ( short ) round( inFloat );
                        outShorts.put( outShort );
                    }
                }
            }
            break;

            default:
            {
                throw new RuntimeException( "Unrecognized data type: " + inLevel.dataType );
            }
        }

        inMapped.dispose( );

        outMapped.force( );
        outMapped.dispose( );

        outLevel.requireValid( );

        File outHdrFile = new File( outDir, outLevelName + ".hdr" );
        writeTopoHdrFile( outLevel, outHdrFile );

        return outLevel;
    }

    public static TopoDataFile writeNextTopoDataLevel( TopoDataFile inLevel, File outDir, String outLevelName ) throws IOException
    {
        double outCellSize_DEG = 2.0 * inLevel.cellSize_DEG;
        int outNumRows = ( int ) ceil( inLevel.numRows * inLevel.cellSize_DEG / outCellSize_DEG );
        int outNumCols = ( int ) ceil( inLevel.numCols * inLevel.cellSize_DEG / outCellSize_DEG );

        TopoDataFile outLevel = new TopoDataFile( outNumRows,
                                                  outNumCols,
                                                  outCellSize_DEG,
                                                  inLevel.southLat_DEG,
                                                  inLevel.westLon_DEG,

                                                  new File( outDir, outLevelName + ".bin" ),
                                                  TOPO_I2,
                                                  LITTLE_ENDIAN,
                                                  inLevel.dataUnits );

        long outNumBytes = ( ( long ) outLevel.numRows ) * ( ( long ) outLevel.numCols ) * outLevel.dataType.numBytes;
        MappedFile outMapped = new MappedFile( outLevel.dataFile, outLevel.dataByteOrder, outNumBytes );

        MappedFile inMapped = new MappedFile( inLevel.dataFile, inLevel.dataByteOrder );
        switch ( inLevel.dataType )
        {
            case TOPO_I2:
            {
                int inRowBytes = inLevel.numCols * inLevel.dataType.numBytes;
                int outRowBytes = outLevel.numCols * outLevel.dataType.numBytes;
                for ( long outRowNum = 0; outRowNum < outLevel.numRows; outRowNum++ )
                {
                    long inRowNumA = min( inLevel.numRows - 1, 2*outRowNum + 0 );
                    long inRowNumB = min( inLevel.numRows - 1, 2*outRowNum + 1 );

                    MappedByteBuffer inSliceA = inMapped.slice( inRowNumA * inRowBytes, inRowBytes );
                    MappedByteBuffer inSliceB = inMapped.slice( inRowNumB * inRowBytes, inRowBytes );
                    MappedByteBuffer outSlice = outMapped.slice( outRowNum * outRowBytes, outRowBytes );

                    ShortBuffer inShortsA = inSliceA.asShortBuffer( );
                    ShortBuffer inShortsB = inSliceB.asShortBuffer( );
                    ShortBuffer outShorts = outSlice.asShortBuffer( );

                    for ( int outColNum = 0; outColNum < outLevel.numCols; outColNum++ )
                    {
                        int inColNumA = min( inLevel.numCols - 1, 2*outColNum + 0 );
                        int inColNumB = min( inLevel.numCols - 1, 2*outColNum + 1 );

                        int sum = ( int ) inShortsA.get( inColNumA )
                                + ( int ) inShortsA.get( inColNumB )
                                + ( int ) inShortsB.get( inColNumA )
                                + ( int ) inShortsB.get( inColNumB );

                        outShorts.put( ( short ) round( 0.25f * sum ) );
                    }
                }
            }
            break;

            case TOPO_F4:
            {
                int inRowBytes = inLevel.numCols * inLevel.dataType.numBytes;
                int outRowBytes = outLevel.numCols * outLevel.dataType.numBytes;
                for ( long outRowNum = 0; outRowNum < outLevel.numRows; outRowNum++ )
                {
                    long inRowNumA = min( inLevel.numRows - 1, 2*outRowNum + 0 );
                    long inRowNumB = min( inLevel.numRows - 1, 2*outRowNum + 1 );

                    MappedByteBuffer inSliceA = inMapped.slice( inRowNumA * inRowBytes, inRowBytes );
                    MappedByteBuffer inSliceB = inMapped.slice( inRowNumB * inRowBytes, inRowBytes );
                    MappedByteBuffer outSlice = outMapped.slice( outRowNum * outRowBytes, outRowBytes );

                    FloatBuffer inFloatsA = inSliceA.asFloatBuffer( );
                    FloatBuffer inFloatsB = inSliceB.asFloatBuffer( );
                    FloatBuffer outShorts = outSlice.asFloatBuffer( );

                    for ( int outColNum = 0; outColNum < outLevel.numCols; outColNum++ )
                    {
                        int inColNumA = min( inLevel.numCols - 1, 2*outColNum + 0 );
                        int inColNumB = min( inLevel.numCols - 1, 2*outColNum + 1 );

                        double sum = ( double ) inFloatsA.get( inColNumA )
                                   + ( double ) inFloatsA.get( inColNumB )
                                   + ( double ) inFloatsB.get( inColNumA )
                                   + ( double ) inFloatsB.get( inColNumB );

                        outShorts.put( ( short ) round( 0.25f * sum ) );
                    }
                }
            }
            break;

            default:
            {
                throw new RuntimeException( "Unrecognized data type: " + inLevel.dataType );
            }
        }

        inMapped.dispose( );

        outMapped.force( );
        outMapped.dispose( );

        outLevel.requireValid( );

        File outHdrFile = new File( outDir, outLevelName + ".hdr" );
        writeTopoHdrFile( outLevel, outHdrFile );

        return outLevel;
    }

    public static void writeTopoHdrFile( TopoDataFile level, File file ) throws IOException
    {
        StringBuilder s = new StringBuilder( );

        s.append( "NCOLS       " ).append( level.numCols ).append( "\n" );
        s.append( "NROWS       " ).append( level.numRows ).append( "\n" );
        s.append( "XLLCORNER   " ).append( level.westLon_DEG ).append( "\n" );
        s.append( "YLLCORNER   " ).append( level.southLat_DEG ).append( "\n" );
        s.append( "CELLSIZE    " ).append( level.cellSize_DEG ).append( "\n" );
        s.append( "BYTEORDER   " ).append( formatDataByteOrder( level.dataByteOrder ) ).append( "\n" );
        s.append( "NUMBERTYPE  " ).append( formatDataType( level.dataType ) ).append( "\n" );
        s.append( "ZUNITS      " ).append( formatDataUnits( level.dataUnits ) ).append( "\n" );

        Files.asCharSink( file, UTF_8 ).write( s );
    }

    public static String formatDataType( TopoDataType dataType )
    {
        switch ( dataType )
        {
            case TOPO_I2: return "2_BYTE_INTEGER";
            case TOPO_F4: return "4_BYTE_FLOAT";
            default: throw new RuntimeException( "Unrecognized data type: " + dataType );
        }
    }

    public static String formatDataByteOrder( ByteOrder dataByteOrder )
    {
        if ( dataByteOrder == LITTLE_ENDIAN )
        {
            return "LSBFIRST";
        }
        else if ( dataByteOrder == BIG_ENDIAN )
        {
            return "MSBFIRST";
        }
        else
        {
            throw new RuntimeException( "Unrecognized data byte order: " + dataByteOrder );
        }
    }

    public static String formatDataUnits( TopoDataUnits dataUnits )
    {
        switch ( dataUnits )
        {
            case TOPO_METERS: return "METERS";
            default: throw new RuntimeException( "Unrecognized data units: " + dataUnits );
        }
    }

}

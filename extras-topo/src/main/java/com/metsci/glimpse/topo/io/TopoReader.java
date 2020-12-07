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
package com.metsci.glimpse.topo.io;

import static com.metsci.glimpse.topo.io.TopoDataType.TOPO_F4;
import static com.metsci.glimpse.topo.io.TopoDataType.TOPO_I2;
import static com.metsci.glimpse.topo.io.TopoDataUnits.TOPO_METERS;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static java.lang.Double.parseDouble;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.String.format;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopoReader
{
    private static final Logger logger = getLogger( TopoReader.class );

    public static TopoDataset readTopoDataset( File dir ) throws IOException
    {
        List<TopoDataFile> levels = new ArrayList<>( );

        File[] dataFiles = dir.listFiles( ( f ) ->
        {
            String s = f.getName( ).toLowerCase( );
            return ( s.endsWith( ".bin" ) || s.endsWith( ".flt" ) );
        } );

        for ( File dataFile : dataFiles )
        {
            TopoDataFile level = readTopoLevel( dataFile );
            levels.add( level );
        }

        return new TopoDataset( levels );
    }

    public static TopoDataFile readTopoLevel( File dataFile ) throws IOException
    {
        File hdrFile = siblingFile( dataFile, "hdr" );
        if ( hdrFile.isFile( ) )
        {
            try
            {
                logger.info( "Reading a topo level with file header: data-file = " + dataFile.getAbsolutePath( ) + ", hdr-file = " + hdrFile.getAbsolutePath( ) );
                TopoDataFile level = readTopoLevelWithFileHeader( dataFile, hdrFile );
                logger.fine( "Read a topo level with file header:\n" + level.toLongString( "  " ) );
                return level;
            }
            catch ( TopoParseException e )
            {
                logWarning( logger, "Topo hdr file exists, but is not parseable", e );
            }
        }

        logger.info( "Reading a topo level with inferred header: data-file = " + dataFile.getAbsolutePath( ) );
        TopoDataFile level = readTopoLevelWithGuessedHeader( dataFile );
        logger.fine( "Read a topo level with inferred header:\n" + level.toLongString( "  " ) );
        return level;
    }

    public static File siblingFile( File file, String siblingExtension )
    {
        File dir = file.getParentFile( );

        String filename = file.getName( );
        int iDot = filename.lastIndexOf( '.' );
        String root = ( iDot < 0 ? filename : filename.substring( 0, iDot ) ) + ".";

        File sibling = new File( dir, root + siblingExtension );
        if ( sibling.isFile( ) )
        {
            return sibling;
        }

        File siblingLower = new File( dir, root + siblingExtension.toLowerCase( ) );
        if ( siblingLower.isFile( ) )
        {
            return siblingLower;
        }

        File siblingUpper = new File( dir, root + siblingExtension.toUpperCase( ) );
        if ( siblingUpper.isFile( ) )
        {
            return siblingUpper;
        }

        return sibling;
    }

    public static TopoDataFile readTopoLevelWithFileHeader( File dataFile, File hdrFile ) throws IOException
    {
        Map<String,String> header = readTopoHdrFile( hdrFile );

        int numRows = parse( header, "NROWS", Integer::parseInt );
        int numCols = parse( header, "NCOLS", Integer::parseInt );
        double cellSize_DEG = parse( header, "CELLSIZE", Double::parseDouble );

        double southLat_DEG;
        if ( header.containsKey( "YLLCORNER" ) )
        {
            // Cell registered
            southLat_DEG = parse( header, "YLLCORNER", Double::parseDouble );
        }
        else
        {
            // Grid registered
            southLat_DEG = parse( header, "YLLCENTER", Double::parseDouble ) - 0.5*cellSize_DEG;
        }

        double westLon_DEG;
        if ( header.containsKey( "XLLCORNER" ) )
        {
            // Cell registered
            westLon_DEG = parse( header, "XLLCORNER", Double::parseDouble );
        }
        else
        {
            // Grid registered
            westLon_DEG = parse( header, "XLLCENTER", Double::parseDouble ) - 0.5*cellSize_DEG;
        }

        TopoDataType dataType = parse( header, "NUMBERTYPE", TopoReader::parseDataType );
        ByteOrder dataByteOrder = parse( header, "BYTEORDER", TopoReader::parseDataByteOrder );
        TopoDataUnits dataUnits = parse( header, "ZUNITS", TopoReader::parseDataUnits );

        return new TopoDataFile( numRows,
                                 numCols,
                                 cellSize_DEG,
                                 southLat_DEG,
                                 westLon_DEG,

                                 dataFile,
                                 dataType,
                                 dataByteOrder,
                                 dataUnits );
    }

    public static Map<String,String> readTopoHdrFile( File hdrFile ) throws IOException
    {
        try ( BufferedReader reader = new BufferedReader( new FileReader( hdrFile ) ) )
        {
            Map<String,String> map = new LinkedHashMap<>( );
            for ( int lineNum = 1; true; lineNum++ )
            {
                String line = reader.readLine( );
                if ( line == null )
                {
                    break;
                }

                line = line.trim( );
                if ( line.isEmpty( ) )
                {
                    continue;
                }

                String[] tokens = line.split( "\\s+" );
                if ( tokens.length != 2 )
                {
                    throw new IOException( "Failed to parse etopo hdr file: file = " + hdrFile.getAbsolutePath( ) + ", line-num = " + lineNum );
                }

                String key = ( tokens[ 0 ] ).toUpperCase( );
                String value = tokens[ 1 ];
                if ( map.containsKey( key ) )
                {
                    throw new IOException( "Duplicate key in etopo hdr file: file = " + hdrFile.getAbsolutePath( ) + ", line-num = " + lineNum );
                }

                map.put( key, value );
            }
            return map;
        }
    }

    public static <K,V,W> W parse( Map<K,V> map, K key, Function<V,W> parseFn ) throws TopoParseException
    {
        try
        {
            return parseFn.apply( map.get( key ) );
        }
        catch ( Exception e )
        {
            throw new TopoParseException( e );
        }
    }

    public static TopoDataType parseDataType( String s )
    {
        switch ( s.toUpperCase( ) )
        {
            case "2_BYTE_INTEGER": return TOPO_I2;
            case "4_BYTE_FLOAT":   return TOPO_F4;
            default: throw new RuntimeException( "Unrecognized data type: " + s );
        }
    }

    public static ByteOrder parseDataByteOrder( String s )
    {
        switch ( s.toUpperCase( ) )
        {
            case "LSBFIRST": return LITTLE_ENDIAN;
            default: return BIG_ENDIAN;
        }
    }

    public static TopoDataUnits parseDataUnits( String s )
    {
        if ( s == null )
        {
            return TOPO_METERS;
        }
        else
        {
            switch ( s.toUpperCase( ) )
            {
                case "METERS": return TOPO_METERS;
                default: throw new RuntimeException( "Unrecognized data units: " + s );
            }
        }
    }

    public static TopoDataFile readTopoLevelWithGuessedHeader( File dataFile ) throws IOException
    {
        if ( !dataFile.isFile( ) )
        {
            throw new FileNotFoundException( "Not an existing file: " + dataFile.getAbsolutePath( ) );
        }

        TopoDataType dataType = guessDataTypeFromFilename( dataFile );
        ByteOrder dataByteOrder = LITTLE_ENDIAN;
        TopoDataUnits dataUnits = TOPO_METERS;
        double cellSize_DEG = guessCellSizeFromFilename_DEG( dataFile );

        long numCells = divideEvenly( dataFile.length( ), dataType.numBytes );

        long numRows;
        long numCols;
        double southLat_DEG;
        double westLon_DEG;
        if ( guessIsGridCenteredFromFilename( dataFile ) )
        {
            southLat_DEG = -90.0 - 0.5*cellSize_DEG;
            westLon_DEG = -180.0 - 0.5*cellSize_DEG;
            numCols = solveQuadraticEvenly( 1, 1, -2*numCells, +1 );
            numRows = divideEvenly( numCols + 1, 2 );
        }
        else
        {
            southLat_DEG = -90.0;
            westLon_DEG = -180.0;
            numCols = sqrtEvenly( 2 * numCells );
            numRows = divideEvenly( numCols, 2 );
        }

        return new TopoDataFile( ( int ) numRows,
                                 ( int ) numCols,
                                 cellSize_DEG,
                                 southLat_DEG,
                                 westLon_DEG,

                                 dataFile,
                                 dataType,
                                 dataByteOrder,
                                 dataUnits );
    }

    public static TopoDataType guessDataTypeFromFilename( File dataFile ) throws TopoParseException
    {
        String name = dataFile.getName( ).toLowerCase( );
        if ( name.endsWith( ".bin" ) )
        {
            return TOPO_I2;
        }
        else if ( name.endsWith( ".flt" ) )
        {
            return TOPO_F4;
        }
        else
        {
            throw new TopoParseException( format( "Could not guess dataType from filename: file = '%s'", dataFile.getName( ) ) );
        }
    }

    public static double guessCellSizeFromFilename_DEG( File dataFile ) throws TopoParseException
    {
        String name = dataFile.getName( ).toLowerCase( );
        Matcher matcher = Pattern.compile( "^etopo([0-9]+)_.*$" ).matcher( name );
        if ( matcher.matches( ) )
        {
            String s = matcher.group( 1 );
            try
            {
                double cellSize_MINUTES = parseDouble( s );
                return ( cellSize_MINUTES / 60.0 );
            }
            catch ( NumberFormatException e )
            {
                throw new TopoParseException( format( "Could not guess cellSize_DEG from filename: file = '%s', substring = '%s'", dataFile.getName( ), s ), e );
            }
        }
        else
        {
            throw new TopoParseException( "Could not guess cellSize_DEG from filename: " + dataFile.getName( ) );
        }
    }

    public static boolean guessIsGridCenteredFromFilename( File dataFile ) throws TopoParseException
    {
        String name = dataFile.getName( ).toLowerCase( );
        Matcher matcher = Pattern.compile( "^etopo[0-9]*_[^_]+_([^_\\.]+).*$" ).matcher( name );
        if ( matcher.matches( ) )
        {
            String s = matcher.group( 1 );
            switch ( s )
            {
                case "c": return false;
                case "g": return true;
                default: throw new TopoParseException( format( "Could not guess isGridCentered from filename: file = '%s', substring = '%s'", dataFile.getName( ), s ) );
            }
        }
        else
        {
            throw new TopoParseException( "Could not guess isGridCentered from filename: " + dataFile.getName( ) );
        }
    }

    public static long divideEvenly( long a, long b )
    {
        long result = a / b;
        if ( result * b == a )
        {
            return result;
        }
        else
        {
            throw new RuntimeException( format( "%d is not evenly divisible by %d", a, b ) );
        }
    }

    public static long sqrtEvenly( long a )
    {
        long result = round( sqrt( a ) );
        if ( result * result == a )
        {
            return result;
        }
        else
        {
            throw new RuntimeException( format( "%d is not a perfect square", a ) );
        }
    }

    public static long solveQuadraticEvenly( long a, long b, long c, int signDiscriminant )
    {
        long result = round( ( -b + ( signDiscriminant * sqrt( b*b - 4*a*c ) ) ) / ( 2.0 * a ) );
        if ( a*result*result + b*result + c == 0 )
        {
            return result;
        }
        else
        {
            throw new RuntimeException( format( "Solution to '%dx^2 + %dx + %d = 0' is not an integer", a, b, c ) );
        }
    }

}

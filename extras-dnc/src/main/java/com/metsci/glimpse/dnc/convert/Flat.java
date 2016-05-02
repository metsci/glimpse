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
package com.metsci.glimpse.dnc.convert;

import static com.google.common.base.Charsets.US_ASCII;
import static com.metsci.glimpse.dnc.convert.Flat.FlatAttrType.FLAT_DOUBLE_ATTR;
import static com.metsci.glimpse.dnc.convert.Flat.FlatAttrType.FLAT_INT_ATTR;
import static com.metsci.glimpse.dnc.convert.Flat.FlatAttrType.FLAT_PACKED_STRING_ATTR;
import static com.metsci.glimpse.dnc.convert.Flat.FlatAttrType.FLAT_STRING_ATTR;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_AREA_FEATURE;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_LINE_FEATURE;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_POINT_FEATURE;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.memmapReadOnly;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.poslim;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.readIdsMapFile;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.unpackLongIntoBytes;
import static java.lang.Double.longBitsToDouble;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.sort;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.Files;
import com.metsci.glimpse.util.geo.LatLonGeo;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class Flat
{

    public static final Pattern flatDirnamePattern = Pattern.compile( "^dncflat([0-9][0-9])$" );


    public static final String flatCharsetFilename = "charset";
    public static final String flatChecksumFilename = "checksum";


    public static final String flatLibraryNamesFilename = "library-names";
    public static final String flatCoverageNamesFilename = "coverage-names";
    public static final String flatFcodeNamesFilename = "fcode-names";
    public static final String flatAttrNamesFilename = "attr-names";


    public static final String flatChunksFilename = "chunks";
    public static final String flatLibrariesFilename = "libraries";
    public static final String flatFeaturesFilename = "features";
    public static final String flatRingsFilename = "rings";
    public static final String flatVerticesFilename = "vertices";
    public static final String flatAttrsFilename = "attrs";
    public static final String flatStringsFilename = "strings";


    public static final int intsPerFlatChunk = 4;
    public static final int doublesPerFlatLibrary = 4;
    public static final int intsPerFlatFeature = 6;
    public static final int intsPerFlatRing = 2;
    public static final int doublesPerFlatVertex = 2;
    public static final int longsPerFlatAttr = 2;


    public static File[] flatChildDirs( File parentDir )
    {
        File[] childDirs = parentDir.listFiles( new FileFilter( )
        {
            public boolean accept( File f )
            {
                return ( f.isDirectory( ) && flatDirnamePattern.matcher( f.getName( ) ).matches( ) );
            }
        } );

        sort( childDirs, new Comparator<File>( )
        {
            public int compare( File a, File b )
            {
                return a.getName( ).compareTo( b.getName( ) );
            }
        } );

        return childDirs;
    }


    public static int flatDatabaseNum( File flatDir )
    {
        Matcher m = flatDirnamePattern.matcher( flatDir.getName( ) );
        if ( m.matches( ) )
        {
            return parseInt( m.group( 1 ) );
        }
        else
        {
            throw new RuntimeException( "No flat database number found: " + flatDir.getName( ) );
        }
    }


    public static Charset readFlatCharset( File flatDir ) throws IOException
    {
        File charsetFile = new File( flatDir, flatCharsetFilename );
        return Charset.forName( Files.toString( charsetFile, US_ASCII ).trim( ) );
    }


    public static void writeFlatCharset( File flatDir, Charset charset ) throws IOException
    {
        File charsetFile = new File( flatDir, flatCharsetFilename );
        Files.write( charset.name( ), charsetFile, US_ASCII );
    }


    public static String readFlatChecksum( File flatDir ) throws IOException
    {
        File checksumFile = new File( flatDir, flatChecksumFilename );
        return Files.toString( checksumFile, US_ASCII ).trim( );
    }


    public static void writeFlatChecksum( File flatDir, byte[] digest ) throws IOException
    {
        File checksumFile = new File( flatDir, flatChecksumFilename );
        Files.write( printHexBinary( digest ).toLowerCase( ), checksumFile, US_ASCII );
    }


    public static class FlatFeatureType
    {
        public static final byte FLAT_POINT_FEATURE = ( byte ) 0;
        public static final byte FLAT_LINE_FEATURE = ( byte ) 1;
        public static final byte FLAT_AREA_FEATURE = ( byte ) 2;
    }


    public static String flatFeatureDelineation( int featureTypeId )
    {
        switch ( featureTypeId )
        {
            case FLAT_AREA_FEATURE: return "Area";
            case FLAT_LINE_FEATURE: return "Line";
            case FLAT_POINT_FEATURE: return "Point";
            default: throw new RuntimeException( "Unrecognized feature-type ID: " + featureTypeId );
        }
    }


    public static class FlatAttrType
    {
        public static final byte FLAT_INT_ATTR = ( byte ) 0;
        public static final byte FLAT_DOUBLE_ATTR = ( byte ) 1;
        public static final byte FLAT_STRING_ATTR = ( byte ) 2;
        public static final byte FLAT_PACKED_STRING_ATTR = ( byte ) 3;
    }


    public static Map<String,Object> readFlatAttrs( LongBuffer attrsBuf, int attrFirst, int attrCount, Int2ObjectMap<String> attrNames, ByteBuffer stringsBuf, Charset charset )
    {
        Map<String,Object> featureAttrsMap = new HashMap<>( );

        poslim( attrsBuf, attrFirst, attrCount, longsPerFlatAttr );
        while ( attrsBuf.hasRemaining( ) )
        {
            long attrNameIdAndType = attrsBuf.get( );
            long attrValueParam = attrsBuf.get( );

            int attrNameId = ( int ) ( ( attrNameIdAndType >> 32 ) & 0xFFFFFFFF );
            String attrName = attrNames.get( attrNameId );

            Object attrValue;
            byte attrType = ( byte ) ( attrNameIdAndType & 0xFF );
            switch ( attrType )
            {
                case FLAT_INT_ATTR:
                {
                    attrValue = ( int ) attrValueParam;
                }
                break;

                case FLAT_DOUBLE_ATTR:
                {
                    attrValue = longBitsToDouble( attrValueParam );
                }
                break;

                case FLAT_PACKED_STRING_ATTR:
                {
                    attrValue = new String( unpackLongIntoBytes( attrValueParam ), charset );
                }
                break;

                case FLAT_STRING_ATTR:
                {
                    int stringsByteFirst = ( int ) ( ( attrValueParam >> 32 ) & 0xFFFFFFFF );
                    int stringsByteCount = ( int ) ( attrValueParam & 0xFFFFFFFF );
                    poslim( stringsBuf, stringsByteFirst, stringsByteCount, 1 );
                    byte[] stringBytes = new byte[ stringsByteCount ];
                    stringsBuf.get( stringBytes );
                    attrValue = new String( stringBytes, charset );
                }
                break;

                default:
                {
                    throw new RuntimeException( "Unrecognized attr-type code: " + attrType );
                }
            }
            featureAttrsMap.put( attrName, attrValue );
        }

        return featureAttrsMap;
    }


    public static List<List<LatLonGeo>> readFlatAreaRings( IntBuffer ringsBuf, int ringFirst, int ringCount, DoubleBuffer verticesBuf )
    {
        List<List<LatLonGeo>> rings = new ArrayList<>( );
        poslim( ringsBuf, ringFirst, ringCount, intsPerFlatRing );
        for ( int r = 0; r < ringCount; r++ )
        {
            int vertexFirst = ringsBuf.get( );
            int vertexCount = ringsBuf.get( );
            rings.add( readFlatLineVertices( verticesBuf, vertexFirst, vertexCount ) );
        }
        return rings;
    }


    public static List<LatLonGeo> readFlatLineVertices( DoubleBuffer verticesBuf, int vertexFirst, int vertexCount )
    {
        List<LatLonGeo> vertices = new ArrayList<>( );
        poslim( verticesBuf, vertexFirst, vertexCount, doublesPerFlatVertex );
        for ( int v = 0; v < vertexCount; v++ )
        {
            double lat_DEG = verticesBuf.get( );
            double lon_DEG = verticesBuf.get( );
            vertices.add( LatLonGeo.fromDeg( lat_DEG, lon_DEG ) );
        }
        return vertices;
    }


    public static LatLonGeo readFlatPointVertex( DoubleBuffer verticesBuf, int vertexIndex )
    {
        poslim( verticesBuf, vertexIndex, 1, doublesPerFlatVertex );
        double lat_DEG = verticesBuf.get( );
        double lon_DEG = verticesBuf.get( );
        return LatLonGeo.fromDeg( lat_DEG, lon_DEG );
    }


    public static Int2ObjectMap<String> readFlatFcodeNames( File flatDir, Charset charset ) throws IOException
    {
        return readIdsMapFile( new File( flatDir, flatFcodeNamesFilename ), charset );
    }


    public static Int2ObjectMap<String> readFlatAttrNames( File flatDir, Charset charset ) throws IOException
    {
        return readIdsMapFile( new File( flatDir, flatAttrNamesFilename ), charset );
    }


    public static Int2ObjectMap<String> readFlatLibraryNames( File flatDir, Charset charset ) throws IOException
    {
        return readIdsMapFile( new File( flatDir, flatLibraryNamesFilename ), charset );
    }


    public static Int2ObjectMap<String> readFlatCoverageNames( File flatDir, Charset charset ) throws IOException
    {
        return readIdsMapFile( new File( flatDir, flatCoverageNamesFilename ), charset );
    }


    public static DoubleBuffer memmapFlatLibrariesBuf( File flatDir ) throws IOException
    {
        return memmapReadOnly( new File( flatDir, flatLibrariesFilename ) ).asDoubleBuffer( );
    }


    public static IntBuffer memmapFlatRingsBuf( File flatDir ) throws IOException
    {
        return memmapReadOnly( new File( flatDir, flatRingsFilename ) ).asIntBuffer( );
    }


    public static DoubleBuffer memmapFlatVerticesBuf( File flatDir ) throws IOException
    {
        return memmapReadOnly( new File( flatDir, flatVerticesFilename ) ).asDoubleBuffer( );
    }


    public static LongBuffer memmapFlatAttrsBuf( File flatDir ) throws IOException
    {
        return memmapReadOnly( new File( flatDir, flatAttrsFilename ) ).asLongBuffer( );
    }


    public static ByteBuffer memmapFlatStringsBuf( File flatDir ) throws IOException
    {
        return memmapReadOnly( new File( flatDir, flatStringsFilename ) );
    }


    public static class FlatChunkKey
    {
        public final int libraryNum;
        public final int coverageNum;

        public FlatChunkKey( int libraryNum, int coverageNum )
        {
            this.libraryNum = libraryNum;
            this.coverageNum = coverageNum;
        }

        @Override
        public int hashCode( )
        {
            final int prime = 257;
            int result = 1;
            result = prime * result + libraryNum;
            result = prime * result + coverageNum;
            return result;
        }

        @Override
        public boolean equals( Object o )
        {
            if ( o == this ) return true;
            if ( o == null ) return false;
            if ( o.getClass( ) != getClass( ) ) return false;

            FlatChunkKey k = ( FlatChunkKey ) o;
            return ( k.libraryNum == libraryNum && k.coverageNum == coverageNum );
        }
    }


    public static Map<FlatChunkKey,IntBuffer> readFlatChunks( File flatDir ) throws IOException
    {
        IntBuffer chunksBuf = memmapReadOnly( new File( flatDir, flatChunksFilename ) ).asIntBuffer( );
        IntBuffer featuresBuf = memmapReadOnly( new File( flatDir, flatFeaturesFilename ) ).asIntBuffer( );

        Map<FlatChunkKey,IntBuffer> featuresBufs = new LinkedHashMap<>( );
        while ( chunksBuf.hasRemaining( ) )
        {
            int libraryNum = chunksBuf.get( );
            int coverageNum = chunksBuf.get( );
            int featureFirst = chunksBuf.get( );
            int featureCount = chunksBuf.get( );

            FlatChunkKey chunkKey = new FlatChunkKey( libraryNum, coverageNum );

            poslim( featuresBuf, featureFirst, featureCount, intsPerFlatFeature );
            featuresBufs.put( chunkKey, featuresBuf.slice( ) );
        }
        return featuresBufs;
    }

}

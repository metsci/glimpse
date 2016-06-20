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

import static com.jogamp.common.nio.Buffers.SIZEOF_DOUBLE;
import static com.jogamp.common.nio.Buffers.SIZEOF_INT;
import static com.jogamp.common.nio.Buffers.SIZEOF_LONG;
import static com.metsci.glimpse.dnc.convert.Flat.doublesPerFlatLibrary;
import static com.metsci.glimpse.dnc.convert.Flat.doublesPerFlatVertex;
import static com.metsci.glimpse.dnc.convert.Flat.flatAttrNamesFilename;
import static com.metsci.glimpse.dnc.convert.Flat.flatAttrsFilename;
import static com.metsci.glimpse.dnc.convert.Flat.flatCharsetFilename;
import static com.metsci.glimpse.dnc.convert.Flat.flatChunksFilename;
import static com.metsci.glimpse.dnc.convert.Flat.flatCoverageNamesFilename;
import static com.metsci.glimpse.dnc.convert.Flat.flatFcodeNamesFilename;
import static com.metsci.glimpse.dnc.convert.Flat.flatFeaturesFilename;
import static com.metsci.glimpse.dnc.convert.Flat.flatLibrariesFilename;
import static com.metsci.glimpse.dnc.convert.Flat.flatLibraryNamesFilename;
import static com.metsci.glimpse.dnc.convert.Flat.flatRingsFilename;
import static com.metsci.glimpse.dnc.convert.Flat.flatStringsFilename;
import static com.metsci.glimpse.dnc.convert.Flat.flatVerticesFilename;
import static com.metsci.glimpse.dnc.convert.Flat.intsPerFlatChunk;
import static com.metsci.glimpse.dnc.convert.Flat.intsPerFlatFeature;
import static com.metsci.glimpse.dnc.convert.Flat.intsPerFlatRing;
import static com.metsci.glimpse.dnc.convert.Flat.longsPerFlatAttr;
import static com.metsci.glimpse.dnc.convert.Flat.writeFlatCharset;
import static com.metsci.glimpse.dnc.convert.Flat.writeFlatChecksum;
import static com.metsci.glimpse.dnc.convert.Flat.FlatAttrType.FLAT_DOUBLE_ATTR;
import static com.metsci.glimpse.dnc.convert.Flat.FlatAttrType.FLAT_INT_ATTR;
import static com.metsci.glimpse.dnc.convert.Flat.FlatAttrType.FLAT_PACKED_STRING_ATTR;
import static com.metsci.glimpse.dnc.convert.Flat.FlatAttrType.FLAT_STRING_ATTR;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_AREA_FEATURE;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_LINE_FEATURE;
import static com.metsci.glimpse.dnc.convert.Flat.FlatFeatureType.FLAT_POINT_FEATURE;
import static com.metsci.glimpse.dnc.convert.Vpf.createPrimitiveDatas;
import static com.metsci.glimpse.dnc.convert.Vpf.readAllFeatureClasses;
import static com.metsci.glimpse.dnc.convert.Vpf.vpfAreaRings;
import static com.metsci.glimpse.dnc.convert.Vpf.vpfDatabases;
import static com.metsci.glimpse.dnc.convert.Vpf.vpfLibraryNameComparator;
import static com.metsci.glimpse.dnc.convert.Vpf.vpfLineVertices;
import static com.metsci.glimpse.dnc.convert.Vpf.vpfPointVertex;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.createAndMemmapReadWrite;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.createNewDir;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.packBytesIntoLong;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.sorted;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.writeIdsMapFile;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static java.lang.Double.doubleToLongBits;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.io.Files;

import gov.nasa.worldwind.formats.vpf.VPFBasicFeatureFactory;
import gov.nasa.worldwind.formats.vpf.VPFCoverage;
import gov.nasa.worldwind.formats.vpf.VPFDatabase;
import gov.nasa.worldwind.formats.vpf.VPFFeature;
import gov.nasa.worldwind.formats.vpf.VPFFeatureClass;
import gov.nasa.worldwind.formats.vpf.VPFFeatureFactory;
import gov.nasa.worldwind.formats.vpf.VPFLibrary;
import gov.nasa.worldwind.formats.vpf.VPFPrimitiveData;
import gov.nasa.worldwind.formats.vpf.VPFTile;
import gov.nasa.worldwind.geom.LatLon;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class Vpf2Flat
{

    protected static final Logger logger = getLogger( Vpf2Flat.class );



    public static void convertVpfToFlat( File vpfParentDir, File flatParentDir, Charset charset ) throws IOException
    {
        flatParentDir.mkdirs( );
        for ( VPFDatabase vpfDatabase : vpfDatabases( vpfParentDir, null ) )
        {
            Database database = readVpfDatabase( vpfDatabase );
            String dirname = vpfDatabase.getName( ).toLowerCase( ).replace( "dnc", "dncflat" );
            File flatDir = createNewDir( flatParentDir, dirname );
            writeFlatDatabase( database, flatDir, charset );
        }
    }



    // Data classes
    //

    public static class Database
    {
        public String name;
        public List<Library> libraries = new ArrayList<>( );
    }

    public static class Library
    {
        public String name;
        public double minLat_DEG;
        public double maxLat_DEG;
        public double minLon_DEG;
        public double maxLon_DEG;
        public Map<String,List<Feature>> featuresByCoverage = new LinkedHashMap<>( );
    }

    public static abstract class Feature
    {
        public String fcode;
        public List<Attribute> attrs = new ArrayList<>( );
    }

    public static class AreaFeature extends Feature
    {
        public List<List<Vertex>> rings = new ArrayList<>( );
    }

    public static class LineFeature extends Feature
    {
        public List<Vertex> vertices = new ArrayList<>( );
    }

    public static class PointFeature extends Feature
    {
        public Vertex vertex = new Vertex( );
    }

    public static abstract class Attribute
    {
        public String name;
    }

    public static class StringAttribute extends Attribute
    {
        public String value;
    }

    public static class DoubleAttribute extends Attribute
    {
        public double value;
    }

    public static class IntAttribute extends Attribute
    {
        public int value;
    }

    public static class Vertex
    {
        public double lat_DEG = Double.NaN;
        public double lon_DEG = Double.NaN;
    }



    // Read VPF
    //

    public static Database readVpfDatabase( VPFDatabase database )
    {
        Database result = new Database( );
        result.name = database.getName( );
        readVpfLibraries( database.getLibraries( ), result.libraries );
        return result;
    }

    public static void readVpfLibraries( Collection<VPFLibrary> libraries, Collection<Library> results )
    {
        for ( VPFLibrary library : sorted( libraries, vpfLibraryNameComparator ) )
        {
            VPFFeatureClass[] featureClasses = readAllFeatureClasses( library );
            if ( featureClasses == null || featureClasses.length == 0 ) continue;

            Library result = new Library( );
            result.name = library.getName( );

            result.minLat_DEG = library.getBounds( ).getYmin( );
            result.maxLat_DEG = library.getBounds( ).getYmax( );
            result.minLon_DEG = library.getBounds( ).getXmin( );
            result.maxLon_DEG = library.getBounds( ).getXmax( );

            // Null is the pseudo-tile for untiled libraries
            VPFTile[] tiles = ( library.hasTiledCoverages( ) ? library.getTiles( ) : new VPFTile[] { null } );
            for ( VPFTile tile : tiles )
            {
                Map<VPFCoverage,VPFPrimitiveData> primitiveDatas = createPrimitiveDatas( library, tile );
                for ( VPFFeatureClass featureClass : featureClasses )
                {
                    if ( featureClass == null ) continue;

                    VPFPrimitiveData primitiveData = primitiveDatas.get( featureClass.getCoverage( ) );
                    if ( primitiveData == null ) continue;

                    VPFFeatureFactory featureFactory = new VPFBasicFeatureFactory( tile, primitiveData );
                    Collection<? extends VPFFeature> features = featureClass.createFeatures( featureFactory );
                    if ( features == null ) continue;

                    String coverage = featureClass.getCoverage( ).getName( );
                    if ( !result.featuresByCoverage.containsKey( coverage ) )
                    {
                        result.featuresByCoverage.put( coverage, new ArrayList<Feature>( ) );
                    }

                    readVpfFeatures( features, primitiveData, result.featuresByCoverage.get( coverage ) );
                }
            }

            results.add( result );
        }
    }

    public static void readVpfFeatures( Iterable<? extends VPFFeature> features, VPFPrimitiveData primitiveData, Collection<Feature> results )
    {
        for ( VPFFeature feature : features )
        {
            switch ( feature.getType( ) )
            {
                case AREA:
                {
                    AreaFeature result = new AreaFeature( );
                    result.fcode = fcode( feature );
                    readVpfAttrs( feature.getEntries( ), result.attrs );

                    for ( List<LatLon> ring : vpfAreaRings( feature, primitiveData ) )
                    {
                        List<Vertex> resultRing = new ArrayList<>( );
                        for ( LatLon vertex : ring )
                        {
                            Vertex resultVertex = new Vertex( );
                            resultVertex.lat_DEG = vertex.latitude.degrees;
                            resultVertex.lon_DEG = vertex.longitude.degrees;
                            resultRing.add( resultVertex );
                        }
                        result.rings.add( resultRing );
                    }

                    results.add( result );
                }
                break;

                case LINE:
                {
                    LineFeature result = new LineFeature( );
                    result.fcode = fcode( feature );
                    readVpfAttrs( feature.getEntries( ), result.attrs );

                    for ( LatLon vertex : vpfLineVertices( feature, primitiveData ) )
                    {
                        Vertex resultVertex = new Vertex( );
                        resultVertex.lat_DEG = vertex.latitude.degrees;
                        resultVertex.lon_DEG = vertex.longitude.degrees;
                        result.vertices.add( resultVertex );
                    }

                    results.add( result );
                }
                break;

                case POINT:
                {
                    PointFeature result = new PointFeature( );
                    result.fcode = fcode( feature );
                    readVpfAttrs( feature.getEntries( ), result.attrs );

                    LatLon vertex = vpfPointVertex( feature, primitiveData );
                    result.vertex.lat_DEG = vertex.latitude.degrees;
                    result.vertex.lon_DEG = vertex.longitude.degrees;

                    results.add( result );
                }
                break;

                default:
                {
                    // Skip
                }
                break;
            }
        }
    }

    public static void readVpfAttrs( Iterable<Entry<String,Object>> attrs, Collection<Attribute> results )
    {
        for ( Entry<String,Object> attr : attrs )
        {
            String name = attr.getKey( );
            Object value = attr.getValue( );

            if ( value instanceof String )
            {
                StringAttribute result = new StringAttribute( );
                result.name = name;
                result.value = ( String ) value;
                results.add( result );
            }
            else if ( value instanceof Double )
            {
                DoubleAttribute result = new DoubleAttribute( );
                result.name = name;
                result.value = ( ( Double ) value ).doubleValue( );
                results.add( result );
            }
            else if ( value instanceof Integer )
            {
                IntAttribute result = new IntAttribute( );
                result.name = name;
                result.value = ( ( Integer ) value ).intValue( );
                results.add( result );
            }
            else
            {
                throw new RuntimeException( "Can't handle attr-value of this type: name = " + name + ", value-type = " + value.getClass( ).getName( ) );
            }
        }
    }

    public static String fcode( VPFFeature feature )
    {
        return feature.getStringValue( "f_code" );
    }



    // Write Flat
    //

    public static void writeFlatDatabase( Database database, File flatDir, Charset charset ) throws IOException
    {
        // Output Files

        File chunksFile    = new File( flatDir, flatChunksFilename    );
        File librariesFile = new File( flatDir, flatLibrariesFilename );
        File featuresFile  = new File( flatDir, flatFeaturesFilename  );
        File ringsFile     = new File( flatDir, flatRingsFilename     );
        File verticesFile  = new File( flatDir, flatVerticesFilename  );
        File attrsFile     = new File( flatDir, flatAttrsFilename     );
        File stringsFile   = new File( flatDir, flatStringsFilename   );

        File charsetFile       = new File( flatDir, flatCharsetFilename       );
        File libraryNamesFile  = new File( flatDir, flatLibraryNamesFilename  );
        File coverageNamesFile = new File( flatDir, flatCoverageNamesFilename );
        File fcodeNamesFile    = new File( flatDir, flatFcodeNamesFilename    );
        File attrNamesFile     = new File( flatDir, flatAttrNamesFilename     );


        // Charset
        writeFlatCharset( flatDir, charset );


        // Chunks
        int totalChunkCount = 0;
        for ( Library library : database.libraries )
        {
            totalChunkCount += library.featuresByCoverage.size( );
        }
        int totalChunksByteCount = totalChunkCount * intsPerFlatChunk * SIZEOF_INT;
        MappedByteBuffer chunksMapped = createAndMemmapReadWrite( chunksFile, totalChunksByteCount );
        IntBuffer chunksBuf = chunksMapped.asIntBuffer( );


        // Libraries
        int totalLibraryCount = database.libraries.size( );
        int totalLibrariesByteCount = totalLibraryCount * doublesPerFlatLibrary * SIZEOF_DOUBLE;
        MappedByteBuffer librariesMapped = createAndMemmapReadWrite( librariesFile, totalLibrariesByteCount );
        DoubleBuffer librariesBuf = librariesMapped.asDoubleBuffer( );


        // Features
        int totalFeatureCount = 0;
        for ( Library library : database.libraries )
        {
            for ( List<Feature> features : library.featuresByCoverage.values( ) )
            {
                totalFeatureCount += features.size( );
            }
        }
        int totalFeaturesByteCount = totalFeatureCount * intsPerFlatFeature * SIZEOF_INT;
        MappedByteBuffer featuresMapped = createAndMemmapReadWrite( featuresFile, totalFeaturesByteCount );
        IntBuffer featuresBuf = featuresMapped.asIntBuffer( );


        // Rings
        int totalRingCount = 0;
        for ( Library library : database.libraries )
        {
            for ( List<Feature> features : library.featuresByCoverage.values( ) )
            {
                for ( Feature feature : features )
                {
                    if ( feature instanceof AreaFeature )
                    {
                        totalRingCount += ( ( AreaFeature ) feature ).rings.size( );
                    }
                }
            }
        }
        int totalRingsByteCount = totalRingCount * intsPerFlatRing * SIZEOF_INT;
        MappedByteBuffer ringsMapped = createAndMemmapReadWrite( ringsFile, totalRingsByteCount );
        IntBuffer ringsBuf = ringsMapped.asIntBuffer( );


        // Vertices
        int totalVertexCount = 0;
        for ( Library library : database.libraries )
        {
            for ( List<Feature> features : library.featuresByCoverage.values( ) )
            {
                for ( Feature feature : features )
                {
                    if ( feature instanceof AreaFeature )
                    {
                        for ( List<Vertex> ring : ( ( AreaFeature ) feature ).rings )
                        {
                            totalVertexCount += ring.size( );
                        }
                    }
                    else if ( feature instanceof LineFeature )
                    {
                        totalVertexCount += ( ( LineFeature ) feature ).vertices.size( );
                    }
                    else if ( feature instanceof PointFeature )
                    {
                        totalVertexCount += 1;
                    }
                    else
                    {
                        throw new RuntimeException( "Can't handle feature of this type: type = " + feature.getClass( ).getName( ) );
                    }
                }
            }
        }
        int totalVerticesByteCount = totalVertexCount * doublesPerFlatVertex * SIZEOF_DOUBLE;
        MappedByteBuffer verticesMapped = createAndMemmapReadWrite( verticesFile, totalVerticesByteCount );
        DoubleBuffer verticesBuf = verticesMapped.asDoubleBuffer( );


        // Attrs
        int totalAttrCount = 0;
        for ( Library library : database.libraries )
        {
            for ( List<Feature> features : library.featuresByCoverage.values( ) )
            {
                for ( Feature feature : features )
                {
                    totalAttrCount += feature.attrs.size( );
                }
            }
        }
        int totalAttrsByteCount = totalAttrCount * longsPerFlatAttr * SIZEOF_LONG;
        MappedByteBuffer attrsMapped = createAndMemmapReadWrite( attrsFile, totalAttrsByteCount );
        LongBuffer attrsBuf = attrsMapped.asLongBuffer( );


        // Strings
        int totalStringsByteCount = 0;
        for ( Library library : database.libraries )
        {
            for ( List<Feature> features : library.featuresByCoverage.values( ) )
            {
                for ( Feature feature : features )
                {
                    for ( Attribute attr : feature.attrs )
                    {
                        if ( attr instanceof StringAttribute )
                        {
                            byte[] bytes = ( ( StringAttribute ) attr ).value.getBytes( charset );
                            if ( bytes.length > 7 )
                            {
                                totalStringsByteCount += bytes.length;
                            }
                        }
                    }
                }
            }
        }
        MappedByteBuffer stringsMapped = createAndMemmapReadWrite( stringsFile, totalStringsByteCount );
        ByteBuffer stringsBuf = stringsMapped.duplicate( );


        // ID Maps
        Object2IntMap<String> libraryIds = new Object2IntLinkedOpenHashMap<>( );
        Object2IntMap<String> coverageIds = new Object2IntLinkedOpenHashMap<>( );
        Object2IntMap<String> fcodeIds = new Object2IntLinkedOpenHashMap<>( );
        Object2IntMap<String> attrNameIds = new Object2IntLinkedOpenHashMap<>( );


        // Put data into buffers
        for ( Library library : database.libraries )
        {
            int libraryIndex = librariesBuf.position( ) / doublesPerFlatLibrary;

            libraryIds.put( library.name, libraryIndex );

            librariesBuf.put( library.minLat_DEG )
                        .put( library.maxLat_DEG )
                        .put( library.minLon_DEG )
                        .put( library.maxLon_DEG );


            for ( Entry<String,List<Feature>> chunk : library.featuresByCoverage.entrySet( ) )
            {
                String coverage = chunk.getKey( );
                List<Feature> features = chunk.getValue( );


                int coverageId = getOrCreateId( coverageIds, coverage );
                int featureFirst = featuresBuf.position( ) / intsPerFlatFeature;
                int featureCount = features.size( );

                chunksBuf.put( libraryIndex )
                         .put( coverageId )
                         .put( featureFirst )
                         .put( featureCount );


                for ( Feature feature : features )
                {

                    // Fcode
                    //

                    int fcodeId = getOrCreateId( fcodeIds, feature.fcode );


                    // Attrs
                    //

                    int attrFirst = attrsBuf.position( ) / longsPerFlatAttr;
                    int attrCount = feature.attrs.size( );
                    for ( Attribute attr : feature.attrs )
                    {
                        byte attrType;
                        long attrValue;

                        if ( attr instanceof StringAttribute )
                        {
                            byte[] bytes = ( ( StringAttribute ) attr ).value.getBytes( charset );
                            if ( bytes.length > 7 )
                            {
                                attrType = FLAT_STRING_ATTR;
                                int stringsByteFirst = stringsBuf.position( );
                                int stringsByteCount = bytes.length;
                                stringsBuf.put( bytes, 0, stringsByteCount );
                                attrValue = ( ( ( ( long ) stringsByteFirst ) & 0xFFFFFFFF ) << 32 ) | ( ( ( long ) stringsByteCount ) & 0xFFFFFFFF );
                            }
                            else
                            {
                                attrType = FLAT_PACKED_STRING_ATTR;
                                attrValue = packBytesIntoLong( bytes );
                            }
                        }
                        else if ( attr instanceof DoubleAttribute )
                        {
                            attrType = FLAT_DOUBLE_ATTR;
                            attrValue = doubleToLongBits( ( ( DoubleAttribute ) attr ).value );
                        }
                        else if ( attr instanceof IntAttribute )
                        {
                            attrType = FLAT_INT_ATTR;
                            attrValue = ( ( IntAttribute ) attr ).value;
                        }
                        else
                        {
                            throw new RuntimeException( "Can't handle attr of this type: name = " + attr.name + ", type = " + attr.getClass( ).getName( ) );
                        }

                        int attrNameId = getOrCreateId( attrNameIds, attr.name );
                        long attrNameIdAndType = ( ( ( ( long ) attrNameId ) & 0xFFFFFFFF ) << 32 ) | ( ( ( int ) attrType ) & 0xFF );

                        attrsBuf.put( attrNameIdAndType ).put( attrValue );
                    }


                    // Delineation & Vertices
                    //

                    byte featureType;
                    int featureItemFirst;
                    int featureItemCount;

                    if ( feature instanceof AreaFeature )
                    {
                        featureType = FLAT_AREA_FEATURE;

                        AreaFeature areaFeature = ( AreaFeature ) feature;
                        featureItemFirst = ringsBuf.position( ) / intsPerFlatRing;
                        featureItemCount = areaFeature.rings.size( );

                        for ( List<Vertex> ring : areaFeature.rings )
                        {
                            int vertexFirst = verticesBuf.position( ) / doublesPerFlatVertex;
                            int vertexCount = ring.size( );

                            for ( Vertex vertex : ring )
                            {
                                verticesBuf.put( vertex.lat_DEG ).put( vertex.lon_DEG );
                            }

                            ringsBuf.put( vertexFirst ).put( vertexCount );
                        }
                    }
                    else if ( feature instanceof LineFeature )
                    {
                        featureType = FLAT_LINE_FEATURE;

                        LineFeature lineFeature = ( LineFeature ) feature;
                        featureItemFirst = verticesBuf.position( ) / doublesPerFlatVertex;
                        featureItemCount = lineFeature.vertices.size( );

                        for ( Vertex vertex : lineFeature.vertices )
                        {
                            verticesBuf.put( vertex.lat_DEG ).put( vertex.lon_DEG );
                        }
                    }
                    else if ( feature instanceof PointFeature )
                    {
                        featureType = FLAT_POINT_FEATURE;

                        PointFeature pointFeature = ( PointFeature ) feature;
                        featureItemFirst = verticesBuf.position( ) / doublesPerFlatVertex;
                        featureItemCount = 1;

                        verticesBuf.put( pointFeature.vertex.lat_DEG ).put( pointFeature.vertex.lon_DEG );
                    }
                    else
                    {
                        throw new RuntimeException( "Can't handle feature of this type: type = " + feature.getClass( ).getName( ) );
                    }


                    featuresBuf.put( fcodeId )
                               .put( ( int ) featureType )
                               .put( attrFirst )
                               .put( attrCount )
                               .put( featureItemFirst )
                               .put( featureItemCount );
                }
            }
        }


        // Flush buffers to disk
        chunksMapped.force( );
        librariesMapped.force( );
        featuresMapped.force( );
        ringsMapped.force( );
        verticesMapped.force( );
        attrsMapped.force( );
        stringsMapped.force( );


        // Make sure we wrote the expected number of bytes to each buffer
        if ( SIZEOF_INT    * chunksBuf.position( )    != totalChunksByteCount    ) logger.severe( "Wrong number of bytes written to chunks file: expected = "    + totalChunksByteCount    + ", found = " + ( SIZEOF_INT    * chunksBuf.position( )    ) );
        if ( SIZEOF_LONG   * librariesBuf.position( ) != totalLibrariesByteCount ) logger.severe( "Wrong number of bytes written to libraries file: expected = " + totalLibrariesByteCount + ", found = " + ( SIZEOF_LONG   * librariesBuf.position( ) ) );
        if ( SIZEOF_INT    * featuresBuf.position( )  != totalFeaturesByteCount  ) logger.severe( "Wrong number of bytes written to features file: expected = "  + totalFeaturesByteCount  + ", found = " + ( SIZEOF_INT    * featuresBuf.position( )  ) );
        if ( SIZEOF_INT    * ringsBuf.position( )     != totalRingsByteCount     ) logger.severe( "Wrong number of bytes written to rings file: expected = "     + totalRingsByteCount     + ", found = " + ( SIZEOF_INT    * ringsBuf.position( )     ) );
        if ( SIZEOF_DOUBLE * verticesBuf.position( )  != totalVerticesByteCount  ) logger.severe( "Wrong number of bytes written to vertices file: expected = "  + totalVerticesByteCount  + ", found = " + ( SIZEOF_DOUBLE * verticesBuf.position( )  ) );
        if ( SIZEOF_LONG   * attrsBuf.position( )     != totalAttrsByteCount     ) logger.severe( "Wrong number of bytes written to attrs file: expected = "     + totalAttrsByteCount     + ", found = " + ( SIZEOF_LONG   * attrsBuf.position( )     ) );
        if ( 1             * stringsBuf.position( )   != totalStringsByteCount   ) logger.severe( "Wrong number of bytes written to strings file: expected = "   + totalStringsByteCount   + ", found = " + ( 1             * stringsBuf.position( )   ) );


        // Write ID maps
        writeIdsMapFile( libraryIds,  libraryNamesFile,  charset );
        writeIdsMapFile( coverageIds, coverageNamesFile, charset );
        writeIdsMapFile( fcodeIds,    fcodeNamesFile,    charset );
        writeIdsMapFile( attrNameIds, attrNamesFile,     charset );


        // Write checksum
        try
        {
            MessageDigest digest = MessageDigest.getInstance( "MD5" );

            digest.update( Files.toByteArray( charsetFile ) );
            digest.update( Files.toByteArray( libraryNamesFile ) );
            digest.update( Files.toByteArray( coverageNamesFile ) );
            digest.update( Files.toByteArray( fcodeNamesFile ) );
            digest.update( Files.toByteArray( attrNamesFile ) );

            digest.update( chunksMapped );
            digest.update( librariesMapped );
            digest.update( featuresMapped );
            digest.update( ringsMapped );
            digest.update( verticesMapped );
            digest.update( attrsMapped );
            digest.update( stringsMapped );

            writeFlatChecksum( flatDir, digest.digest( ) );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static <K> int getOrCreateId( Object2IntMap<K> idsMap, K key )
    {
        if ( !idsMap.containsKey( key ) )
        {
            idsMap.put( key, idsMap.size( ) );
        }
        return idsMap.getInt( key );
    }

}

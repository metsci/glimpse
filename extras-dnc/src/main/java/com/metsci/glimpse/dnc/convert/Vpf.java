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

import static com.google.common.base.Objects.equal;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.filenameToLowercase;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.last;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.sorted;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.toArrayList;
import static com.metsci.glimpse.util.GeneralUtils.ints;
import static gov.nasa.worldwind.formats.vpf.VPFConstants.DATABASE_HEADER_TABLE;
import static gov.nasa.worldwind.formats.vpf.VPFConstants.EDGE_PRIMITIVE_TABLE;
import static java.util.Arrays.sort;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import gov.nasa.worldwind.formats.vpf.VPFBasicPrimitiveDataFactory;
import gov.nasa.worldwind.formats.vpf.VPFBufferedRecordData;
import gov.nasa.worldwind.formats.vpf.VPFCoverage;
import gov.nasa.worldwind.formats.vpf.VPFFeature;
import gov.nasa.worldwind.formats.vpf.VPFFeatureClass;
import gov.nasa.worldwind.formats.vpf.VPFFeatureTableFilter;
import gov.nasa.worldwind.formats.vpf.VPFFeatureType;
import gov.nasa.worldwind.formats.vpf.VPFLibrary;
import gov.nasa.worldwind.formats.vpf.VPFPrimitiveData;
import gov.nasa.worldwind.formats.vpf.VPFPrimitiveData.EdgeInfo;
import gov.nasa.worldwind.formats.vpf.VPFPrimitiveData.FaceInfo;
import gov.nasa.worldwind.formats.vpf.VPFPrimitiveData.Ring;
import gov.nasa.worldwind.formats.vpf.VPFPrimitiveDataFactory;
import gov.nasa.worldwind.formats.vpf.VPFSurfaceLine;
import gov.nasa.worldwind.formats.vpf.VPFTile;
import gov.nasa.worldwind.formats.vpf.VPFUtils;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.CompoundVecBuffer;
import gov.nasa.worldwind.util.VecBuffer;
import gov.nasa.worldwind.util.VecBufferSequence;

public class Vpf
{

    public static final Comparator<VPFLibrary> vpfLibraryNameComparator = new Comparator<VPFLibrary>()
    {
        public int compare(VPFLibrary a, VPFLibrary b)
        {
            String aName = a.getName();
            String bName = b.getName();
            return aName.compareTo(bName);
        }
    };


    public static final Comparator<VPFCoverage> vpfCoverageNameComparator = new Comparator<VPFCoverage>()
    {
        public int compare(VPFCoverage a, VPFCoverage b)
        {
            String aName = a.getName();
            String bName = b.getName();
            return aName.compareTo(bName);
        }
    };


    public static final Comparator<VPFFeatureClass> vpfFeatureClassNameComparator = new Comparator<VPFFeatureClass>()
    {
        public int compare(VPFFeatureClass a, VPFFeatureClass b)
        {
            String aName = a.getClassName();
            String bName = b.getClassName();
            return aName.compareTo(bName);
        }
    };


    public static VPFFeatureClass[] readAllFeatureClasses(VPFLibrary lib)
    {
        return readFeatureClasses(lib, VPFFeatureType.values());
    }


    public static VPFFeatureClass[] readFeatureClasses(VPFLibrary lib, VPFFeatureType... types)
    {
        EnumSet<VPFFeatureType> typeSet = EnumSet.noneOf(VPFFeatureType.class);
        for (VPFFeatureType t : types) typeSet.add(t);

        // WW's VPF reader breaks (by silently skipping data!) if filenames are case-
        // sensitive and contain uppercase characters. In such a case, assume that the
        // necessary lowercase symlinks have already been created (which is reasonable,
        // because that's the only way the reader will work).
        FileFilter featureTableFilter = new VPFFeatureTableFilter()
        {
            public boolean accept(File file)
            {
                return ( super.accept(file) && equal(file, filenameToLowercase(file)) );
            }
        };

        Set<VPFFeatureClass> featureClasses = new LinkedHashSet<>();
        for (VPFCoverage cov : lib.getCoverages())
        {
            if (cov.isReferenceCoverage()) continue;

            VPFFeatureClass[] fcs = VPFUtils.readFeatureClasses(cov, featureTableFilter);
            for (VPFFeatureClass fc : fcs)
            {
                VPFFeatureType type = fc.getType();
                if (typeSet.contains(type)) featureClasses.add(fc);
            }
        }

        VPFFeatureClass[] featureClassesArray = featureClasses.toArray(new VPFFeatureClass[0]);
        sort( featureClassesArray, vpfFeatureClassNameComparator);
        return featureClassesArray;
    }


    public static Map<VPFCoverage,VPFPrimitiveData> createPrimitiveDatas(VPFLibrary lib, VPFTile tile)
    {
        VPFPrimitiveDataFactory factory = new VPFBasicPrimitiveDataFactory(tile);

        Map<VPFCoverage,VPFPrimitiveData> primitiveDatas = new LinkedHashMap<>();
        for (VPFCoverage cov : sorted(lib.getCoverages(), vpfCoverageNameComparator))
        {
            primitiveDatas.put(cov, factory.createPrimitiveData(cov));
        }
        return primitiveDatas;
    }


    /**
     * Returns a map from database name to database dir
     */
    public static Map<String,File> vpfDatabaseDirsByName(File parentDir)
    {
        Map<String,File> dbDirs = new LinkedHashMap<>();

        File[] children = parentDir.listFiles();
        if (children != null)
        {
            sort(children);
            for (File dbDir : children)
            {
                File dhtFile = findDhtFile(dbDir);
                if (dhtFile != null)
                {
                    String dbName = readDatabaseName(dhtFile);
                    if (dbName != null && !dbDirs.containsKey(dbName))
                    {
                        dbDirs.put(dbName, dhtFile.getParentFile().getAbsoluteFile());
                    }
                }
            }
        }

        return dbDirs;
    }


    public static File findDhtFile(File dbDir)
    {
        File fileOrSymlink = null;
        File fileActual = null;

        File[] children = dbDir.listFiles();
        if (children != null)
        {
            sort(children);
            for (File f : children)
            {
                if (f.getName().equalsIgnoreCase(DATABASE_HEADER_TABLE) && f.isFile())
                {
                    fileOrSymlink = f;
                    if (!Files.isSymbolicLink(f.toPath()))
                    {
                        fileActual = f;
                    }
                }
            }
        }

        return (fileActual == null ? fileOrSymlink : fileActual);
    }


    public static String readDatabaseName(File dhtFile)
    {
        if (dhtFile != null)
        {
            VPFBufferedRecordData dht = VPFUtils.readTable(dhtFile);
            if (dht != null && dht.getNumRecords() >= 1)
            {
                Object o = dht.getRecord(1).getValue("database_name");
                if (o instanceof String)
                {
                    return ((String) o);
                }
            }
        }
        return null;
    }


    public static class Edge
    {
        public final int start;
        public final int end;
        public final List<LatLon> locations;

        Edge(int start, int end, List<LatLon> locations)
        {
            this.start = start;
            this.end = end;
            this.locations = unmodifiableList(new ArrayList<LatLon>(locations));
        }

        public Edge flipped()
        {
            List<LatLon> locations2 = new ArrayList<LatLon>(locations);
            reverse(locations2);
            return new Edge(end, start, locations2);
        }
    }


    public static boolean edgeIsReversed(int edgeOrientation)
    {
        switch (edgeOrientation)
        {
            case 1: return false;
            case -1: return true;
            default: throw new RuntimeException("Unrecognized edge-orientation code: " + edgeOrientation);
        }
    }


    public static List<Edge> extractRingEdges(VPFPrimitiveData primitiveData, VecBufferSequence edgeTableData, Ring ring)
    {
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < ring.getNumEdges(); i++)
        {
            int id = ring.getEdgeId(i);

            EdgeInfo info = (EdgeInfo) primitiveData.getPrimitiveInfo(EDGE_PRIMITIVE_TABLE, id);
            List<LatLon> locations = toArrayList( edgeTableData.slice(ints(id), 0, 1).getLocations() );
            Edge edge = new Edge(info.getStartNode(), info.getEndNode(), locations);

            boolean reverse = edgeIsReversed(ring.getEdgeOrientation(i));
            edges.add( reverse ? edge.flipped() : edge );
        }
        return edges;
    }


    public static class RingEdgesBacktrackException extends RuntimeException
    {
        public final List<Edge> edgesExtracted;
        public final List<Edge> edgesRegularizedSoFar;
        public final Edge currentEdge;

        public RingEdgesBacktrackException(List<Edge> edgesExtracted, List<Edge> edgesRegularizedSoFar)
        {
            this(edgesExtracted, edgesRegularizedSoFar, null);
        }

        public RingEdgesBacktrackException(List<Edge> edgesExtracted, List<Edge> edgesRegularizedSoFar, Edge currentEdge)
        {
            super("Ring-edges backtrack failed");
            this.edgesExtracted = unmodifiableList(new ArrayList<Edge>(edgesExtracted));
            this.edgesRegularizedSoFar = unmodifiableList(new ArrayList<Edge>(edgesRegularizedSoFar));
            this.currentEdge = currentEdge;
        }

        @Override
        public String toString()
        {
            StringBuilder s = new StringBuilder(super.toString());

            s.append(" ... edges:");
            for (Edge edge : edgesExtracted) s.append(edge.start).append("-").append(edge.end).append(",");
            s.setLength(s.length()-1);

            s.append(" ... regularized:");
            for (Edge edge : edgesRegularizedSoFar) s.append(edge.start).append("-").append(edge.end).append(",");
            s.setLength(s.length()-1);

            if (currentEdge != null)
            {
                s.append(" ... current:");
                s.append(currentEdge.start).append("-").append(currentEdge.end);
            }

            return s.toString();
        }
    }


    public static List<Edge> regularizeRingEdges(List<Edge> extractedRingEdges)
    {
        List<Edge> regularized = new ArrayList<>();
        Stack<Edge> stack = new Stack<Edge>();

        for (int i = 0; i < extractedRingEdges.size(); i++)
        {
            Edge current = extractedRingEdges.get(i);
            regularized.add(current);
            stack.push(current);

            // When an edge is completely contained in the polygon, WWJ only gives us the edge once,
            // making sort of a T intersection (... AB, BC, BD, DE, ...). To make a well-behaved
            // polygon, we insert "backtrack" edges (... AB, BC, *CB*, BD, DE, ...).
            //
            // Note that we may have to backtrack after the last edge, to get back to the start of
            // the first edge -- hence the "(i+1) % size" here.
            //
            Edge next = extractedRingEdges.get((i + 1) % extractedRingEdges.size());
            while (last(regularized).end != next.start)
            {
                // If the stack is empty, it means next.start is a location the polygon hasn't visited
                // before ... which means that these edges just don't make a well-behaved polygon.
                //
                if (stack.isEmpty()) throw new RingEdgesBacktrackException(extractedRingEdges, regularized, current);
                regularized.add( stack.pop().flipped() );
            }
        }

        return regularized;
    }


    public static List<LatLon> locations(List<Edge> edges)
    {
        List<LatLon> locations = new ArrayList<>();
        for (Edge edge : edges) locations.addAll(edge.locations);
        return locations;
    }


    public static List<LatLon> ringLocations(VPFPrimitiveData primitiveData, VecBufferSequence edgeTableData, Ring ring)
    {
        return locations( regularizeRingEdges( extractRingEdges(primitiveData, edgeTableData, ring) ) );
    }


    public static List<List<LatLon>> vpfAreaRings(VPFFeature areaFeature, VPFPrimitiveData primitiveData)
    {
        List<List<LatLon>> vertices = new ArrayList<>();
        VecBufferSequence edgeTableData = primitiveData.getPrimitiveCoords(EDGE_PRIMITIVE_TABLE);
        String primitiveName = areaFeature.getFeatureClass().getPrimitiveTableName();
        for (int id : areaFeature.getPrimitiveIds())
        {
            FaceInfo faceInfo = (FaceInfo) primitiveData.getPrimitiveInfo(primitiveName, id);

            vertices.add( ringLocations(primitiveData, edgeTableData, faceInfo.getOuterRing()) );

            for (Ring innerRing : faceInfo.getInnerRings())
            {
                vertices.add( ringLocations(primitiveData, edgeTableData, innerRing) );
            }
        }
        return vertices;
    }


    public static List<LatLon> vpfLineVertices(VPFFeature lineFeature, VPFPrimitiveData primitiveData)
    {
        VPFSurfaceLine line = new VPFSurfaceLine(lineFeature, primitiveData);

        List<LatLon> lines = new ArrayList<>();

        LatLon vStart = null;
        for (LatLon vEnd : line.getLocations())
        {
            if (vStart != null)
            {
                lines.add(vStart);
                lines.add(vEnd);
            }
            vStart = vEnd;
        }

        return lines;
    }


    public static LatLon vpfPointVertex(VPFFeature pointFeature, VPFPrimitiveData primitiveData)
    {
        List<LatLon> points = new ArrayList<>();

        String primitiveName = pointFeature.getFeatureClass().getPrimitiveTableName();
        CompoundVecBuffer combinedCoords = primitiveData.getPrimitiveCoords(primitiveName);
        if (combinedCoords != null)
        {
            for (int id : pointFeature.getPrimitiveIds())
            {
                VecBuffer coords = combinedCoords.subBuffer(id);
                if (coords == null || coords.getSize() == 0) continue;

                for (LatLon location : coords.getLocations()) points.add(location);
            }
        }
        if (points.size() != 1) throw new RuntimeException("Point feature has " + points.size() + " vertices");

        return points.get(0);
    }

}

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

import static com.metsci.glimpse.dnc.util.DncMiscUtils.last;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.toArrayList;
import static com.metsci.glimpse.util.GeneralUtils.ints;
import static com.metsci.glimpse.util.GeneralUtils.newArrayList;
import static com.metsci.glimpse.util.GeneralUtils.newHashMap;
import static com.metsci.glimpse.util.GeneralUtils.newHashSet;
import static gov.nasa.worldwind.formats.vpf.VPFConstants.EDGE_PRIMITIVE_TABLE;
import static java.lang.String.format;
import static java.util.Arrays.sort;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import gov.nasa.worldwind.formats.vpf.VPFBasicPrimitiveDataFactory;
import gov.nasa.worldwind.formats.vpf.VPFBufferedRecordData;
import gov.nasa.worldwind.formats.vpf.VPFConstants;
import gov.nasa.worldwind.formats.vpf.VPFCoverage;
import gov.nasa.worldwind.formats.vpf.VPFDatabase;
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

    public static final Comparator<VPFFeatureClass> featureClassNameComparator = new Comparator<VPFFeatureClass>()
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

        Set<VPFFeatureClass> featureClasses = newHashSet();
        for (VPFCoverage cov : lib.getCoverages())
        {
            if (cov.isReferenceCoverage()) continue;

            // Some copies of the DNCs have uppercase symlinks to lowercase files,
            // or vice versa, to allow case-insensitive code to work on case-sensitive
            // file systems.
            //
            // We don't want to process the uppercase and the lowercase as if they were
            // two separate items, so keep track of which names we've seen so far (in a
            // consistent case), and don't process them again.
            //
            FileFilter filter = new VPFFeatureTableFilter()
            {
                Set<String> namesSeen = newHashSet();
                public boolean accept(File file)
                {
                    String name = file.getName().toLowerCase();
                    return namesSeen.add(name) && super.accept(file);
                }
            };

            VPFFeatureClass[] fcs = VPFUtils.readFeatureClasses(cov, filter);
            for (VPFFeatureClass fc : fcs)
            {
                VPFFeatureType type = fc.getType();
                if (typeSet.contains(type)) featureClasses.add(fc);
            }
        }

        VPFFeatureClass[] featureClassesArray = featureClasses.toArray(new VPFFeatureClass[0]);
        sort( featureClassesArray, featureClassNameComparator);
        return featureClassesArray;
    }


    public static Map<VPFCoverage,VPFPrimitiveData> createPrimitiveDatas(VPFLibrary lib, VPFTile tile)
    {
        VPFPrimitiveDataFactory factory = new VPFBasicPrimitiveDataFactory(tile);

        Map<VPFCoverage,VPFPrimitiveData> primitiveDatas = newHashMap();
        for (VPFCoverage cov : lib.getCoverages())
        {
            primitiveDatas.put(cov, factory.createPrimitiveData(cov));
        }
        return primitiveDatas;
    }


    public static final Comparator<VPFLibrary> vpfLibraryNameComparator = new Comparator<VPFLibrary>()
    {
        public int compare(VPFLibrary a, VPFLibrary b)
        {
            String aName = a.getName();
            String bName = b.getName();
            return aName.compareTo(bName);
        }
    };


    public static Iterable<VPFDatabase> vpfDatabases(File parentDir)
    {
        return vpfDatabases(parentDir, null);
    }


    public static Iterable<VPFDatabase> vpfDatabases(File parentDir, int[] dbNums)
    {
        final Map<String,File> dhtFiles = vpfDatabaseFilesByName(parentDir);

        if (dbNums != null)
        {
            Set<String> dbNames = new HashSet<>();
            for (int dbNum : dbNums)
            {
                dbNames.add(format("DNC%02d", dbNum));
            }
            for (Iterator<String> it = dhtFiles.keySet().iterator(); it.hasNext(); )
            {
                String dbName = it.next();
                if (!dbNames.contains(dbName.toUpperCase()))
                {
                    it.remove();
                }
            }
        }

        return new Iterable<VPFDatabase>()
        {
            public Iterator<VPFDatabase> iterator()
            {
                final Iterator<File> it = dhtFiles.values().iterator();

                return new Iterator<VPFDatabase>()
                {
                    public boolean hasNext()
                    {
                        return it.hasNext();
                    }

                    public VPFDatabase next()
                    {
                        File dhtFile = it.next();
                        return VPFDatabase.fromFile(dhtFile.getPath());
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }


    /**
     * Returns a map from database name to DHT file
     */
    public static Map<String,File> vpfDatabaseFilesByName(File parentDir)
    {
        Map<String,File> dbPaths = new LinkedHashMap<>();

        File[] children = parentDir.listFiles();
        if (children != null)
        {
            for (File dbDir : children)
            {
                File dhtFile = findDhtFile(dbDir);
                if (dhtFile != null)
                {
                    String dbName = readDatabaseName(dhtFile);
                    if (dbName != null && !dbPaths.containsKey(dbName))
                    {
                        dbPaths.put(dbName, dhtFile.getAbsoluteFile());
                    }
                }
            }
        }

        return dbPaths;
    }


    public static File findDhtFile(File dbDir)
    {
        File fileOrSymlink = null;
        File fileActual = null;
        if (dbDir.isDirectory())
        {
            for (File f : dbDir.listFiles())
            {
                if (f.getName().equalsIgnoreCase(VPFConstants.DATABASE_HEADER_TABLE) && f.isFile())
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
        List<Edge> edges = newArrayList();
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
        List<Edge> regularized = newArrayList();
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
        List<LatLon> locations = newArrayList();
        for (Edge edge : edges) locations.addAll(edge.locations);
        return locations;
    }


    public static List<LatLon> ringLocations(VPFPrimitiveData primitiveData, VecBufferSequence edgeTableData, Ring ring)
    {
        return locations( regularizeRingEdges( extractRingEdges(primitiveData, edgeTableData, ring) ) );
    }


    public static List<List<LatLon>> vpfAreaRings(VPFFeature areaFeature, VPFPrimitiveData primitiveData)
    {
        List<List<LatLon>> vertices = newArrayList();
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

        List<LatLon> lines = newArrayList();

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
        List<LatLon> points = newArrayList();

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

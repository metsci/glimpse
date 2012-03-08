/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.charts.vector.iteration;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.util.io.StreamOpener;



/**
 * An "ArrayList" of GeoObjects with filtering capabilities.
 * 
 * @author Cunningham
 */
public class GeoRecordListForStream<V extends GeoObject> implements GeoFilterableRecordList<V> {

    private static final Logger logger = Logger.getLogger(GeoRecordListForStream.class.getName());

    private List<V> filteredRecordList;
    private List<V> recordList;


    public GeoRecordListForStream(StreamToGeoObjectConverter<V> streamConverter, String resourceName) throws IOException {
        DataInputStream geoStream = new DataInputStream(new BufferedInputStream(StreamOpener.fileThenResource.openForRead(resourceName)));

        recordList = new ArrayList<V>();
        readInFile(streamConverter, geoStream, recordList);
        filteredRecordList = recordList;
        geoStream.close();
    }

    public GeoRecordListForStream(StreamToGeoObjectConverter<V> streamConverter, InputStream stream) throws IOException {
        DataInputStream encStream = new DataInputStream(stream);

        recordList = new ArrayList<V>();
        readInFile(streamConverter, encStream, recordList);
        filteredRecordList = recordList;
    }

    public GeoRecordListForStream(List<V> recordList) {
        this.recordList = recordList;
        filteredRecordList = recordList;
    }

    /**
     * Adds the new records to the georecord list.  Does not apply any previously added filter to the new records.
     *
     * @param newRecordList
     */
    public void add(List<V> newRecordList) {
        this.recordList.addAll(newRecordList);
    }


    /**
     * Adds the new records to the georecord list.  Does not apply any previously added filter to the new records.
     *
     * @param newRecordList
     * @throws IOException
     */
    public void add(StreamToGeoObjectConverter<V> streamConverter, InputStream stream) throws IOException {
        DataInputStream geoStream = new DataInputStream(stream);
        readInFile(streamConverter, geoStream, recordList);
    }

    /**
     * Adds the new records to the georecord list.  Does not apply any previously added filter to the new records.
     *
     * @param streamConverter
     * @param resourceName
     * @throws IOException
     */
    public void add(StreamToGeoObjectConverter<V> streamConverter, String resourceName) throws IOException {
        DataInputStream geoStream = new DataInputStream(new BufferedInputStream(StreamOpener.fileThenResource.openForRead(resourceName)));

        readInFile(streamConverter, geoStream, recordList);
        filteredRecordList = recordList;
        geoStream.close();
    }


    /**
     *
     * @param filter
     * @param clobber if true, filter will be applied on original unfiltered list.
     * If false, existing filtered list will be filtered even more
     */

    @Override
    public void applyFilter(GeoFilter<V> filter, boolean clobber) {
        List<V> listToFilter;
        if (clobber) {
            listToFilter = recordList;
        } else {
            listToFilter = filteredRecordList;
        }
        filteredRecordList = filterList(filter, listToFilter, null);
    }

    public void clearAllFilters() {
        filteredRecordList = recordList;
    }

    public int size() {
        return filteredRecordList.size();
    }

    @Override
    public V get(int index) throws IOException  {
        return filteredRecordList.get(index);
    }

    public Iterator<V> iterator()  {
        return new Iterator<V>() {
            private int recordIndex = -1;
            private V next = internalNext();

            @Override
            public boolean hasNext() { return (next != null); }

            @Override
            public V next()  {
                V current = next;
                next = internalNext();
                return current;
            }

            private V internalNext()  {
                if((recordIndex+1) < filteredRecordList.size())
                    return filteredRecordList.get(++recordIndex);
                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported");
            }
        };
    }

    @Override
    public GeoObjectIterator geoObjectIterator() throws IOException {
        return new GeoObjectIterator() {
            private int recordIndex = -1;
            private V next = internalNext();

            @Override
            public boolean hasNext() { return (next != null); }

            @Override
            public V nextGeo()  {
                V current = next;
                next = internalNext();
                return current;
            }

            private V internalNext()  {
                if((recordIndex+1) < filteredRecordList.size())
                    return filteredRecordList.get(++recordIndex);
                return null;
            }
        };
    }

    private static <T extends GeoObject> void readInFile(StreamToGeoObjectConverter<T> loader, DataInputStream geoStream, List<T> recordList) throws IOException {
        while (geoStream.available() > 0) {
            recordList.add(loader.readNext(geoStream));
        }
    }

    private static <T extends GeoObject> List<T> filterList(GeoFilter filter, List<T> recordList, List<T> filteredRecordList) {
        if (filteredRecordList == null) {
            filteredRecordList = new ArrayList<T>(recordList.size());
        }

        for (T object : recordList) {
            if (filter.passGeoFilter(object))
                filteredRecordList.add(object);
        }
        return filteredRecordList;
    }
}

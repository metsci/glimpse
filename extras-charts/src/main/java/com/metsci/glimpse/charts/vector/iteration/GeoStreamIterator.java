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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.util.io.StreamOpener;


/**
 * Given single or multiple streams of geo objects, this class will iterate over 
 * every GeoObject in those streams.
 *
 * @author john
 */
public class GeoStreamIterator<V extends GeoObject> implements GeoObjectIterator<V>, Iterator<V> {

    private static final Logger logger = Logger.getLogger(GeoStreamIterator.class.getName());
    
    private List<InputStream> sourceStreamList;
    private List<GeoFilter<V>> filterList;
    private DataInputStream dis;
    private V next = null;
    private	int fileIndex = -1;
    private int recordIndex = -1;
    private StreamToGeoObjectConverter<V> streamConverter;
    

    public GeoStreamIterator(String resources, StreamToGeoObjectConverter<V> loader) throws IOException {
        InputStream inputStream = new BufferedInputStream( StreamOpener.fileThenResource.openForRead(resources) );
        sourceStreamList = Collections.<InputStream>singletonList(inputStream);
        this.filterList = new ArrayList<GeoFilter<V>>();
        this.streamConverter = loader;
        init();
    }

    public GeoStreamIterator(InputStream inputStream, StreamToGeoObjectConverter<V> loader) throws IOException {
        sourceStreamList = Collections.<InputStream>singletonList(inputStream);
        this.filterList = new ArrayList<GeoFilter<V>>();
        this.streamConverter = loader;
        init();
    }

    public GeoStreamIterator(StreamToGeoObjectConverter<V> loader, InputStream ... streams) throws IOException {
        sourceStreamList = new ArrayList<InputStream>(streams.length);
        this.filterList = new ArrayList<GeoFilter<V>>();
        this.streamConverter = loader;
        init();
    }


    public void addFilter(GeoFilter<V> filter) {
        filterList.add(filter);
    }
    public void clearFilters() {
        filterList.clear();
    }

    public void init() throws IOException {
        next = null;
        fileIndex = -1;
        recordIndex = -1;
        nextFile();
        next = internalNext();
    }

    @Override
    public boolean hasNext() { return (next != null); }

    public V next() {
        try {
            V current = next;
            next = internalNext();
            return current;
        } catch (IOException ie) {
            throw new UncheckedIOException(ie);
        }
    }

    @Override
    public V nextGeo() throws IOException {
        V current = next;
        next = internalNext();
        return current;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }

    private V internalNext() throws IOException {
        V enc = nextGeoInFile();
        while (enc == null) {
            boolean success = nextFile();
            if (! success)
                break;
            enc = nextGeoInFile();
        }
        return enc;
    }

    private boolean nextFile() throws IOException {
        if (dis != null)
            dis.close();
        ++fileIndex;
        recordIndex = -1;
        if (fileIndex >= sourceStreamList.size())
            return false;
        dis = new DataInputStream(sourceStreamList.get(fileIndex));
        return true;
    }

    private V nextGeoInFile() throws IOException {
        try {
            int available = dis.available();
            while (available > 0) {
                ++recordIndex;
                V encObject = streamConverter.readNext(dis); 
                if (passFilter(encObject)) {
                    return encObject;
                }
                available = dis.available();
            }
        } catch (EOFException e){}
        return null;
    }

    private boolean passFilter(V object) {
        for (GeoFilter<V> filter : filterList) {
            if (! filter.passGeoFilter(object))
                return false;
        }
        return true;
    }

}

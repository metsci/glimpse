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
package com.metsci.glimpse.charts.vector.parser;

import com.metsci.glimpse.charts.vector.iteration.DNCObjectLoader;
import com.metsci.glimpse.charts.vector.iteration.ENCObjectLoader;
import com.metsci.glimpse.charts.vector.iteration.GeoFilterableRecordList;
import com.metsci.glimpse.charts.vector.iteration.GeoRecordListForStream;
import com.metsci.glimpse.charts.vector.iteration.StreamToGeoObjectConverter;
import com.metsci.glimpse.charts.vector.parser.objects.DNCObject;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.util.io.StreamOpener;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 *
 * @author Cunningham
 */
public class MetsciGeoReader<V extends GeoObject> implements GeoReader<V> {

    private List<V> recordList;

    public static MetsciGeoReader<ENCObject> createENCReader( String resourceName )  throws IOException {
        return new MetsciGeoReader<ENCObject>( new ENCObjectLoader( ), resourceName );
    }

    public static MetsciGeoReader<DNCObject> createDNCReader( String resourceName )  throws IOException {
        return new MetsciGeoReader<DNCObject>( new DNCObjectLoader( ), resourceName );
    }

    public MetsciGeoReader(StreamToGeoObjectConverter<V> loader, String resourceName) throws IOException {
        DataInputStream encStream = new DataInputStream(StreamOpener.fileThenResource.openForRead(resourceName));

        recordList = new ArrayList<V>();
        readInFile(loader, encStream, recordList);
        encStream.close();
    }

    public MetsciGeoReader(StreamToGeoObjectConverter<V> loader, InputStream stream) throws IOException {
        DataInputStream encStream = new DataInputStream(stream);

        recordList = new ArrayList<V>();
        readInFile(loader, encStream, recordList);
    }

    @Override
    public GeoFilterableRecordList<V> getGeoFilterableRecordList() {
        return new GeoRecordListForStream<V>(recordList);
    }

    @Override
    public Collection<V> getCollection() {
        return Collections.unmodifiableCollection(recordList);
    }

    private static <T extends GeoObject> void readInFile(StreamToGeoObjectConverter<T> loader, DataInputStream dncStream, List<T> recordList) throws IOException {
        while (dncStream.available() > 0) {
            T geo = loader.readNext(dncStream);
            recordList.add(geo);
        }
    }
}

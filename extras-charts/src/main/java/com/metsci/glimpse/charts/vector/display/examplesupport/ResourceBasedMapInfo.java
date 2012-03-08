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
package com.metsci.glimpse.charts.vector.display.examplesupport;

import com.metsci.glimpse.charts.raster.BsbRasterData;
import com.metsci.glimpse.charts.vector.iteration.DNCObjectLoader;
import com.metsci.glimpse.charts.vector.iteration.ENCObjectLoader;
import com.metsci.glimpse.charts.vector.iteration.GeoObjectIterator;
import com.metsci.glimpse.charts.vector.iteration.GeoStreamIterator;
import com.metsci.glimpse.charts.vector.iteration.StreamToGeoObjectConverter;
import com.metsci.glimpse.charts.vector.parser.objects.DNCObject;
import com.metsci.glimpse.charts.vector.parser.objects.ENCObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.util.io.StreamOpener;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author john
 */
public class ResourceBasedMapInfo<V extends GeoObject> implements MapInfo<V>
{

    private StreamOpener streamOpener = StreamOpener.fileThenResource;

    private String encResource;
    private String ndgcResource;
    private String bsbResource;
    private StreamToGeoObjectConverter<V> converter;

    public static MapInfo<ENCObject> createENCMapInfo( String resourceRoot, String resourcePath )
    {
        return new ResourceBasedMapInfo<ENCObject>(new ENCObjectLoader( ), new String [] { ".enc", "_bin.txt" }, resourceRoot, resourcePath );
    }

    public static MapInfo<DNCObject> createDNCMapInfo( String resourceRoot, String resourcePath )
    {
        return new ResourceBasedMapInfo<DNCObject>(new DNCObjectLoader( ), new String[] { ".dnc" }, resourceRoot, resourcePath );
    }

    public ResourceBasedMapInfo( StreamToGeoObjectConverter<V> converter, String [] fileSuffixes, String resourceRoot, String resourcePath )
    {
        this.converter = converter;

        String prefix = "";
        if ( resourcePath != null ) prefix = resourcePath + "/";

        String geoResource = null;
        for (String fileSuffix : fileSuffixes ) {
            geoResource = prefix + resourceRoot + fileSuffix;
            if ( doesResourceExist( geoResource ) ) {
                break;
            }
            geoResource = null;
        }


        String ndgcResource = prefix + resourceRoot + ".ndgc";
        if ( !doesResourceExist( ndgcResource ) )
        {
            ndgcResource = null;
        }

        String bsbResource = prefix + resourceRoot + ".KAP";
        if ( !doesResourceExist( bsbResource ) )
        {
            bsbResource = null;
        }

        commonConstructor( resourceRoot, null, geoResource, ndgcResource, bsbResource );
    }

    public void commonConstructor( String publicName, Rectangle2D coverageRect, String encResource, String ndgcResource, String bsbResource )
    {
        this.encResource = encResource;
        this.ndgcResource = ndgcResource;
        this.bsbResource = bsbResource;
    }


    @Override
    public boolean hasGeoIterator( )
    {
        return encResource != null;
    }

    @Override
    public GeoObjectIterator<V> getGeoIterator( ) throws IOException
    {
        if ( encResource != null )
            return new GeoStreamIterator<V>( encResource, converter );
        else
            return null;
    }

    @Override
    public boolean hasBsbRasterData( )
    {
        return bsbResource != null;
    }

    @Override
    public BsbRasterData getBsbRasterData( ) throws IOException
    {
        if ( bsbResource != null )
            return BsbRasterData.readImage( streamOpener.openForRead( bsbResource ) );
        else
            return null;
    }

    @Override
    public String getNdgcResourceName( ) throws IOException
    {
        return ndgcResource;
    }

    private boolean doesResourceExist( String resourceName )
    {
        try
        {
            InputStream stream = streamOpener.openForRead( resourceName );
            stream.close( );
            return true;
        }
        catch ( IOException ex )
        {
            return false;
        }
    }

}

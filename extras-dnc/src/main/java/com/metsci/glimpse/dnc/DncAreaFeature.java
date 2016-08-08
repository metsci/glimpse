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
package com.metsci.glimpse.dnc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.util.geo.LatLonGeo;

public class DncAreaFeature extends DncFeature
{

    protected final Supplier<List<List<LatLonGeo>>> ringsLoader;
    protected final Object ringsMutex;
    protected volatile ImmutableList<ImmutableList<LatLonGeo>> rings;


    public DncAreaFeature( DncChunkKey chunkKey, int featureNum, String fcode, Supplier<Map<String,Object>> attrsLoader, Supplier<List<List<LatLonGeo>>> ringsLoader )
    {
        super( chunkKey, featureNum, fcode, attrsLoader );

        this.ringsLoader = ringsLoader;
        this.ringsMutex = new Object( );
        this.rings = null;
    }

    public ImmutableList<ImmutableList<LatLonGeo>> getRings( )
    {
        // Field is volatile, and only assigned once
        if ( rings != null )
        {
            return rings;
        }
        else
        {
            synchronized ( ringsMutex )
            {
                if ( rings == null )
                {
                    rings = immutableCopy( ringsLoader.get( ) );
                }
                return rings;
            }
        }
    }

    protected static <V> ImmutableList<ImmutableList<V>> immutableCopy( List<List<V>> lists )
    {
        List<ImmutableList<V>> listsCopy = new ArrayList<>( );
        for ( List<V> list : lists )
        {
            listsCopy.add( ImmutableList.copyOf( list ) );
        }
        return ImmutableList.copyOf( listsCopy );
    }

}

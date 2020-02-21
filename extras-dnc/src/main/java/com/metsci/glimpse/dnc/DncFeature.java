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
package com.metsci.glimpse.dnc;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;

public class DncFeature
{

    public final DncChunkKey chunkKey;
    public final int featureNum;
    public final String fcode;

    protected final Supplier<Map<String,Object>> attrsLoader;
    protected final Object attrsMutex;
    protected volatile ImmutableMap<String,Object> attrs;


    public DncFeature( DncChunkKey chunkKey, int featureNum, String fcode, Supplier<Map<String,Object>> attrsLoader )
    {
        this.chunkKey = chunkKey;
        this.featureNum = featureNum;
        this.fcode = fcode;

        this.attrsLoader = attrsLoader;
        this.attrsMutex = new Object( );
        this.attrs = null;
    }

    public Map<String,Object> getAttrs( )
    {
        // Field is volatile, and only assigned once
        if ( attrs != null )
        {
            return attrs;
        }
        else
        {
            synchronized ( attrsMutex )
            {
                if ( attrs == null )
                {
                    attrs = ImmutableMap.copyOf( attrsLoader.get( ) );
                }
                return attrs;
            }
        }
    }

}

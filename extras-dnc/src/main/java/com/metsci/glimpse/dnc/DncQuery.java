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

import static com.metsci.glimpse.util.GeneralUtils.floatsEqual;

import java.util.Collection;
import java.util.Objects;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.util.GeneralUtils;

public class DncQuery
{

    public final ImmutableCollection<DncChunkKey> chunkKeys;
    public final float xMin;
    public final float xMax;
    public final float yMin;
    public final float yMax;


    public DncQuery( Collection<DncChunkKey> chunkKeys, float xMin, float xMax, float yMin, float yMax )
    {
        this.chunkKeys = ImmutableList.copyOf( chunkKeys );
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    @Override
    public int hashCode( )
    {
        int prime = 1021;
        int result = 1;
        result = prime * result + Objects.hashCode( chunkKeys );
        result = prime * result + GeneralUtils.hashCode( xMin );
        result = prime * result + GeneralUtils.hashCode( xMax );
        result = prime * result + GeneralUtils.hashCode( yMin );
        result = prime * result + GeneralUtils.hashCode( yMax );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != getClass( ) ) return false;

        DncQuery other = ( DncQuery ) o;
        return ( Objects.equals( other.chunkKeys, chunkKeys )
              && floatsEqual( other.xMin, xMin )
              && floatsEqual( other.xMax, xMax )
              && floatsEqual( other.yMin, yMin )
              && floatsEqual( other.yMax, yMax ) );
    }

}

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

import static com.google.common.base.Objects.equal;

import java.util.Objects;

import com.metsci.glimpse.util.GeneralUtils;

public class DncLibrary
{

    public final int databaseNum;
    public final String libraryName;
    public final float xMin;
    public final float xMax;
    public final float yMin;
    public final float yMax;


    public DncLibrary( int databaseNum, String libraryName, float xMin, float xMax, float yMin, float yMax )
    {
        this.databaseNum = databaseNum;
        this.libraryName = libraryName;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 743;
        int result = 1;
        result = prime * result + Objects.hashCode( libraryName );
        result = prime * result + GeneralUtils.hashCode( databaseNum );
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

        DncLibrary other = ( DncLibrary ) o;
        return ( equal( other.libraryName, libraryName )
              && other.databaseNum == databaseNum
              && other.xMin == xMin
              && other.xMax == xMax
              && other.yMin == yMin
              && other.yMax == yMax );
    }

}

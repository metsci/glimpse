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
package com.metsci.glimpse.core.painter.geo;

import static com.metsci.glimpse.util.units.Length.toKilometers;

/**
 * A bounding box for a tile. Tiles are expected to be in lat/lon space and must not cross the antimeridian.
 */
public class TileKey
{
    /**
     * This is a measure of the zoom scale.  When picking which tile to display, {@code TilePainter} will choose
     * the tiles that have a {@code lengthScale} closest to the total distance across the viewport.
     */
    public final double lengthScale;

    public final double minLat_DEG;
    public final double maxLat_DEG;
    public final double minLon_DEG;
    public final double maxLon_DEG;

    public TileKey( double lengthScale, double minLat_DEG, double maxLat_DEG, double minLon_DEG, double maxLon_DEG )
    {
        this.lengthScale = lengthScale;
        this.minLat_DEG = minLat_DEG;
        this.maxLat_DEG = maxLat_DEG;
        this.minLon_DEG = minLon_DEG;
        this.maxLon_DEG = maxLon_DEG;
    }

    @Override
    public int hashCode( )
    {
        int hash = 0;
        hash += 31 * Double.hashCode( lengthScale );
        hash += 31 * Double.hashCode( minLat_DEG );
        hash += 31 * Double.hashCode( maxLat_DEG );
        hash += 31 * Double.hashCode( minLon_DEG );
        hash += 31 * Double.hashCode( maxLon_DEG );
        return hash;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof TileKey )
        {
            TileKey other = ( TileKey ) obj;
            return lengthScale == other.lengthScale &&
                    minLat_DEG == other.minLat_DEG &&
                    maxLat_DEG == other.maxLat_DEG &&
                    minLon_DEG == other.minLon_DEG &&
                    maxLon_DEG == other.maxLon_DEG;
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString( )
    {
        return String.format( "TileKey[scale=%f; lat=%f,%f; lon=%f,%f]", toKilometers( lengthScale ), minLat_DEG, maxLat_DEG, minLon_DEG, maxLon_DEG );
    }
}
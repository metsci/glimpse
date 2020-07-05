/*
 * Copyright (c) 2019 Metron, Inc.
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
package com.metsci.glimpse.charts.shoreline.gshhs;

import java.io.DataInput;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author hogye
 */
public class GshhsPolygonHeader
{
    private static final Set<Integer> supportedVersions = new LinkedHashSet<Integer>( );

    static
    {
        supportedVersions.add( 15 );
        supportedVersions.add( 14 );
        supportedVersions.add( 13 );
        supportedVersions.add( 12 );
        supportedVersions.add( 11 );
        supportedVersions.add( 10 );
        supportedVersions.add( 9 );
        supportedVersions.add( 8 );
        supportedVersions.add( 6 );
        supportedVersions.add( 4 );
    }

    /**
     * In GSHHS version 7 (perhaps aka 1.10, version 2)
     * the polygon type "river-lake" was added and consigned to level 2
     */
    public static enum PolygonType
    {
        land(1), lake(2), islandInLake(3), pondInIslandInLake(4), riverLake(2);

        public final int level;

        private PolygonType( int level )
        {
            this.level = level;
        }
    }

    public static enum PolygonDataSource
    {
        wdb2, wvs
    }

    public static class UnrecognizedValueException extends Exception
    {
        private static final long serialVersionUID = 2928195545196276594L;

        public UnrecognizedValueException( String fieldName, int value )
        {
            super( String.format( "Unrecognized value for %s: %d", fieldName, value ) );
        }
    }

    public final int id;
    public final int numVertices;
    public final PolygonType type;
    public final boolean crossesGreenwich;
    public final PolygonDataSource dataSource;

    public final double westLon_DEG;
    public final double eastLon_DEG;
    public final double southLat_DEG;
    public final double northLat_DEG;

    /**
     * Area of polygon in km^2
     */
    public final double area_KM2;

    /**
     * Area of original full-resolution polygon in km^2 (Double.NaN if not supported in GSHHS version)
     */
    public final double area_full;

    /**
     * Id of container polygon that encloses this polygon (-1 if none, -2 if not supported in GSHHS version)
     */
    public final int container;

    /**
     * Id of ancestor polygon in the full resolution set that was the source of this polygon (-1 if none, -2 if not supported in GSHHS version)
     */
    public final int ancestor;

    public GshhsPolygonHeader( DataInput in ) throws IOException, UnrecognizedValueException
    {
        id = in.readInt( );
        numVertices = in.readInt( );

        int flag = in.readInt( );

        int typeByte = ( flag & 0xff );

        int versionByte = ( ( flag >> 8 ) & 0xff );
        if ( !supportedVersions.contains( versionByte ) ) throw new UnrecognizedValueException( "version", versionByte );

        crossesGreenwich = ( ( ( flag >> 16 ) & 0xff ) == 1 );

        int dataSourceByte = ( ( flag >> 24 ) & 0x01 );
        switch ( dataSourceByte )
        {
            case 0:
                dataSource = PolygonDataSource.wdb2;
                break;
            case 1:
                dataSource = PolygonDataSource.wvs;
                break;
            default:
                throw new UnrecognizedValueException( "data source", dataSourceByte );
        }

        boolean riverLake = ( ( flag >> 25 ) & 0x01 ) == 1;
        if ( versionByte == 7 && riverLake )
        {
            type = PolygonType.riverLake;
        }
        else
        {
            switch ( typeByte )
            {
                case 1:
                    type = PolygonType.land;
                    break;
                case 2:
                    type = PolygonType.lake;
                    break;
                case 3:
                    type = PolygonType.islandInLake;
                    break;
                case 4:
                    type = PolygonType.pondInIslandInLake;
                    break;
                default:
                    type = PolygonType.lake;
            }
        }

        westLon_DEG = 1e-6 * in.readInt( );
        eastLon_DEG = 1e-6 * in.readInt( );
        southLat_DEG = 1e-6 * in.readInt( );
        northLat_DEG = 1e-6 * in.readInt( );

        area_KM2 = 0.1 * in.readInt( );

        if ( versionByte >= 7 )
        {
            area_full = 0.1 * in.readInt( );
            container = in.readInt( );
            ancestor = in.readInt( );
        }
        else
        {
            area_full = Double.NaN;
            container = -2;
            ancestor = -2;
        }
    }

}

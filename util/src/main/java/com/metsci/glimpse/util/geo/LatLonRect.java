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
package com.metsci.glimpse.util.geo;

import com.metsci.glimpse.util.geo.datum.Datum;
import com.metsci.glimpse.util.vector.Vector3d;

/**
 * @author osborn
 */
public class LatLonRect
{
    protected static final double piOver180 = Math.PI / 180d;

    protected final double xUnit;
    protected final double yUnit;
    protected final double zUnit;
    protected final double length;

    private LatLonRect( double x, double y, double z )
    {
        length = Math.sqrt( x * x + y * y + z * z );
        final double invLen = 1d / length;

        xUnit = x * invLen;
        yUnit = y * invLen;
        zUnit = z * invLen;
    }

    private LatLonRect( double xUnit, double yUnit, double zUnit, double length )
    {
        this.xUnit = xUnit;
        this.yUnit = yUnit;
        this.zUnit = zUnit;
        this.length = length;
    }

    public static final LatLonRect fromUnitVectorAndLength( double xUnit, double yUnit, double zUnit, double length )
    {
        return new LatLonRect( xUnit, yUnit, xUnit, length );
    }

    public static final LatLonRect fromEnu( Vector3d enuPoint, LatLonGeo refPoint, Datum datum )
    {
        return datum.fromEnu( enuPoint, refPoint );
    }

    public static LatLonRect fromRad( double northLat, double eastLon, double altitude, Datum d )
    {
        return d.toLatLonRect( northLat, eastLon, altitude );
    }

    public static LatLonRect fromRad( double northLat, double eastLon, Datum d )
    {
        return d.toLatLonRect( northLat, eastLon, 0d );
    }

    public static LatLonRect fromDeg( double northLat, double eastLon, double altitude, Datum d )
    {
        return d.toLatLonRect( northLat * piOver180, eastLon * piOver180, altitude );
    }

    public static LatLonRect fromDeg( double northLat, double eastLon, Datum d )
    {
        return d.toLatLonRect( northLat * piOver180, eastLon * piOver180, 0d );
    }

    public static LatLonRect fromXyz( double x, double y, double z )
    {
        return new LatLonRect( x, y, z );
    }

    public final LatLonGeo toLatLonGeo( Datum datum )
    {
        return datum.toLatLonGeo( this );
    }

    /**
     * Creates an (east,north,up) representation of this point on the plane
     * tangent to Earth at the given reference point.
     * See {@link Datum#toEnu(LatLonRect, LatLonGeo)}.
     */
    public final Vector3d toEnu( LatLonGeo refPoint, Datum datum )
    {
        return datum.toEnu( this, refPoint );
    }

    public final LatLonRect scale( double by )
    {
        return new LatLonRect( xUnit, yUnit, zUnit, length * by );
    }

    public final LatLonRect withLength( double length )
    {
        return new LatLonRect( xUnit, yUnit, zUnit, length );
    }

    public final double chordDistanceSquared( LatLonRect r )
    {
        final double rlen = r.length;
        final double dx = xUnit * length - r.xUnit * rlen;
        final double dy = yUnit * length - r.yUnit * rlen;
        final double dz = zUnit * length - r.zUnit * rlen;

        return dx * dx + dy * dy + dz * dz;
    }

    public final double chordDistance( LatLonRect r )
    {
        final double rlen = r.length;
        final double dx = xUnit * length - r.xUnit * rlen;
        final double dy = yUnit * length - r.yUnit * rlen;
        final double dz = zUnit * length - r.zUnit * rlen;

        return Math.sqrt( dx * dx + dy * dy + dz * dz );
    }

    public final double getX( )
    {
        return xUnit * length;
    }

    public final double getY( )
    {
        return yUnit * length;
    }

    public final double getZ( )
    {
        return zUnit * length;
    }

    public final double getUnitX( )
    {
        return xUnit;
    }

    public final double getUnitY( )
    {
        return yUnit;
    }

    public final double getUnitZ( )
    {
        return zUnit;
    }

    public final double getLength( )
    {
        return length;
    }

    public final boolean almostEquals( LatLonRect o, double eps )
    {
        if ( Math.abs( o.xUnit - xUnit ) > eps ) return false;
        if ( Math.abs( o.yUnit - yUnit ) > eps ) return false;
        if ( Math.abs( o.zUnit - zUnit ) > eps ) return false;
        if ( Math.abs( o.length - length ) > eps ) return false;

        return true;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;

        LatLonRect o = ( LatLonRect ) obj;

        if ( Double.doubleToLongBits( length ) != Double.doubleToLongBits( o.length ) ) return false;
        if ( Double.doubleToLongBits( xUnit ) != Double.doubleToLongBits( o.xUnit ) ) return false;
        if ( Double.doubleToLongBits( yUnit ) != Double.doubleToLongBits( o.yUnit ) ) return false;
        if ( Double.doubleToLongBits( zUnit ) != Double.doubleToLongBits( o.zUnit ) ) return false;

        return true;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits( length );
        result = prime * result + ( int ) ( temp ^ ( temp >>> 32 ) );
        temp = Double.doubleToLongBits( xUnit );
        result = prime * result + ( int ) ( temp ^ ( temp >>> 32 ) );
        temp = Double.doubleToLongBits( yUnit );
        result = prime * result + ( int ) ( temp ^ ( temp >>> 32 ) );
        temp = Double.doubleToLongBits( zUnit );
        result = prime * result + ( int ) ( temp ^ ( temp >>> 32 ) );
        return result;
    }
}

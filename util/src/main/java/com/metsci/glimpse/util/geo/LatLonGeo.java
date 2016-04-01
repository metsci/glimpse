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

import java.io.Serializable;

import com.metsci.glimpse.util.geo.datum.Datum;
import com.metsci.glimpse.util.geo.datum.DatumSphere;
import com.metsci.glimpse.util.geo.format.LatLonFormat;
import com.metsci.glimpse.util.geo.format.LatLonFormatDegreesMinutesSeconds;
import com.metsci.glimpse.util.geo.util.DistanceAzimuth;
import com.metsci.glimpse.util.units.Angle;
import com.metsci.glimpse.util.vector.Vector3d;

/**
 * @author osborn
 */
public class LatLonGeo implements Serializable
{
    private static final long serialVersionUID = 997651518096632023L;

    public final static LatLonFormat defaultFormat = new LatLonFormatDegreesMinutesSeconds( 2 );
    public final static DatumSphere defaultDatum = Datum.wgs84sphere;

    protected static final double piOverTwo = Math.PI / 2d;
    protected static final double piOver180 = Math.PI / 180d;
    protected static final double invPiOver180 = 1d / piOver180;
    protected static final double twoPi = Math.PI * 2d;
    protected static final double pi = Math.PI;

    /**
     * north latitude in radians
     */
    private final double lat;

    /**
     * east longitude in radians
     */
    private final double lon;

    /**
     * altitude above surface (positive up)
     */
    private final double altitude;

    /**
     * Constructs and initializes a <code>LatLonGeo</code> at the given coordinates.
     *
     * @param northLatHours hours of north latitude (degrees) in new <code>LatLonGeo</code>
     * @param northLatMinutes minutes of north latitude (degrees) in new <code>LatLonGeo</code>
     * @param eastLonHours hours of east longitude (degrees) in new <code>LatLonGeo</code>
     * @param eastLonMinutes minutes of east longitude (degrees) in new <code>LatLonGeo</code>
     */
    public static LatLonGeo fromDeg( int northLatHours, double northLatMinutes, int eastLonHours, double eastLonMinutes )
    {
        final double northLatDeg = northLatHours + northLatMinutes / 60d;
        final double eastLonDeg = eastLonHours + eastLonMinutes / 60d;
        return new LatLonGeo( northLatDeg * piOver180, eastLonDeg * piOver180, 0d, true );
    }

    /**
     * Constructs and initializes a <code>LatLonGeo</code> at the given coordinates.
     *
     * @param northLat north latitude in degrees of the newly constructed <code>LatLonGeo</code>
     * @param eastLon east longitude in degrees of the newly constructed <code>LatLonGeo</code>
     * @param altitude altitude in system units of the newly constructed <code>LatLonGeo</code>
     */
    public static LatLonGeo fromDeg( double northLat, double eastLon, double altitude )
    {
        return new LatLonGeo( northLat * piOver180, eastLon * piOver180, altitude, true );
    }

    /**
     * Constructs and initializes a <code>LatLonGeo</code> at the given coordinates. Altitude
     * will default to 0.
     *
     * @param northLat north latitude in degrees of the newly constructed <code>LatLonGeo</code>
     * @param eastLon east longitude in degrees of the newly constructed <code>LatLonGeo</code>
     */
    public static LatLonGeo fromDeg( double northLat, double eastLon )
    {
        return new LatLonGeo( northLat * piOver180, eastLon * piOver180, 0d, true );
    }

    /**
     * Constructs and initializes a <code>LatLonGeo</code> at the given coordinates.
     *
     * @param northLat north latitude in radians of the newly constructed <code>LatLonGeo</code>
     * @param eastLon east longitude in radians of the newly constructed <code>LatLonGeo</code>
     * @param altitude altitude in system units of the newly constructed <code>LatLonGeo</code>
     */
    public static LatLonGeo fromRad( double northLat, double eastLon, double altitude )
    {
        return new LatLonGeo( northLat, eastLon, altitude, true );
    }

    /**
     * Constructs and initializes a <code>LatLonGeo</code> at the given coordinates.  Altitude
     * will default to 0.
     *
     * @param northLat north latitude in radians of the newly constructed <code>LatLonGeo</code>
     * @param eastLon east longitude in radians of the newly constructed <code>LatLonGeo</code>
     */
    public static LatLonGeo fromRad( double northLat, double eastLon )
    {
        return new LatLonGeo( northLat, eastLon, 0d, true );
    }

    /**
     * Constructs and initializes a <code>LatLonGeo</code> at the given coordinates. Altitude
     * will default to 0.
     *
     * @param northLatDeg north latitude in degrees of the newly constructed <code>LatLonGeo</code>
     * @param eastLonDeg east longitude in degrees of the newly constructed <code>LatLonGeo</code>
     */
    public LatLonGeo( double northLatDeg, double eastLonDeg )
    {
        this( northLatDeg * piOver180, eastLonDeg * piOver180, 0d, true );
    }

    /**
     * Constructs and initializes a <code>LatLonGeo</code> at the given coordinates.
     *
     * @see #fromDeg(double,double,double)
     * @see #fromDeg(double,double)
     * @see #fromRad(double,double,double)
     * @see #fromRad(double,double)
     *
     * @param northLatDeg north latitude in degrees of the newly constructed <code>LatLonGeo</code>
     * @param eastLonDeg east longitude in degrees of the newly constructed <code>LatLonGeo</code>
     * @param altitude altitude in system units of the newly constructed <code>LatLonGeo</code>
     */
    public LatLonGeo( double northLatDeg, double eastLonDeg, double altitude )
    {
        this( northLatDeg * piOver180, eastLonDeg * piOver180, altitude, true );
    }

    /**
     * Constructs and initializes a <code>LatLonGeo</code> at the given coordinates.
     *
     * <p>
     * This constructor takes an unused <code>boolean</code> argument to distinguish
     * it from the constructor that accepts arguments with units of degrees.  The
     * construct is private to avoid confusing users.  This constructor is used
     * internally to avoid unnecessarily switching between degrees and radians.
     * </p>
     *
     * @param northLatRad north latitude in radians of the newly constructed <code>LatLonGeo</code>
     * @param eastLonRad east longitude in radians of the newly constructed <code>LatLonGeo</code>
     * @param altitude altitude in system units of the newly constructed <code>LatLonGeo</code>
     * @param arg unused argument to distinguish this constructor from the one that uses degrees
     */
    private LatLonGeo( double northLatRad, double eastLonRad, double altitude, boolean arg )
    {
        lat = normalizeLat( northLatRad );
        lon = normalizeLon( eastLonRad );
        this.altitude = altitude;
    }

    /**
     * Constructs and initializes a <code>LatLonGeo</code> based on the given
     * (east,north,up) coordinates in a local tangent plane coordinate system that is
     * tangent to Earth at the given reference point.
     *
     * @param enuPoint (east,north,up) coordinates of point to be converted, in system units
     * @param refPoint local tangent plane point of tangency
     * @param datum underlying datum used for calculations
     */
    public static final LatLonGeo fromEnu( Vector3d enuPoint, LatLonGeo refPoint, Datum datum )
    {
        LatLonRect llr = datum.fromEnu( enuPoint, refPoint );
        LatLonGeo llg = llr.toLatLonGeo( datum );
        return llg;
    }

    /**
     * Treats this <code>LatLonGeo</code> as an ECEF-g coordinate in the given
     * <code>Datum</code> for the purpose of converting it to an ECEF-r coordinate
     * in the form of a newly constructed <code>LatLonRect</code>. The new
     * <code>LatLonRect</code> is formed by calling {@link Datum#toLatLonRect(LatLonGeo)}
     * on this <code>LatLonGeo</code>.
     *
     * <p>
     * <b>NOTE:</b> The latitude and longitude may be treated as geocentric or geodetic
     * depending on the <code>Datum</code> used.
     * </p>
     *
     * @see Datum#toLatLonRect(LatLonGeo)
     *
     * @param datum <code>Datum</code> used to construct the new <code>LatLonRect</code>
     */
    public LatLonRect toLatLonRect( Datum datum )
    {
        return datum.toLatLonRect( this );
    }

    /**
     * Creates an (east,north,up) representation of this point on the plane
     * tangent to Earth at the given reference point.
     * See {@link Datum#toEnu(LatLonRect, LatLonGeo)}.
     */
    public final Vector3d toEnu( LatLonGeo refPoint, Datum datum )
    {
        LatLonRect thisRect = toLatLonRect( datum );
        return datum.toEnu( thisRect, refPoint );
    }

    /**
     * @return altitude above surface
     */
    public double getAltitude( )
    {
        return altitude;
    }

    /**
     * @return north latitude in radians
     */
    public double getLatRad( )
    {
        return lat;
    }

    /**
     * @return east longitude in radians
     */
    public double getLonRad( )
    {
        return lon;
    }

    /**
     * @return north latitude in degrees
     */
    public double getLatDeg( )
    {
        return lat * invPiOver180;
    }

    /**
     * @return east longitude in degrees
     */
    public double getLonDeg( )
    {
        return lon * invPiOver180;
    }

    /**
     * Returns a newly constructed <code>LatLonGeo</code> with a
     * new altitude but same latitude and same longitude as the current
     * <code>LatLonGeo</code>.
     *
     * @param altitude altitude of the newly constructed <code>LatLonGeo</code>
     */
    public LatLonGeo withAltitude( double altitude )
    {
        return new LatLonGeo( getLatRad( ), getLonRad( ), altitude, true );
    }

    /**
     * Returns a newly constructed <code>LatLonGeo</code> with
     * a geodetic latitude based on interpreting the latitude
     * in this <code>LatLonGeo</code> as geocentric.  Longitude
     * and altitude will remain the same. See
     * {@link Datum#toGeodeticLatitude(LatLonGeo)}.
     *
     * @param d Datum defines the spheroid used for projecting the latitude
     * @return newly constructed <code>LatLonGeo</code> with a geodetic latitude
     */
    public LatLonGeo withGeodeticLatitudeOn( Datum d )
    {
        return d.toGeodeticLatitude( this );
    }

    /**
     * Returns a newly constructed <code>LatLonGeo</code> with
     * a geocentric latitude based on interpreting the latitude
     * in this <code>LatLonGeo</code> as geodetic.  Longitude
     * and altitude will remain the same.  See
     * {@link Datum#toGeocentricLatitude(LatLonGeo)}.
     *
     * @param d Datum defines the spheroid used for projecting the latitude
     * @return newly constructed <code>LatLonGeo</code> with a geocentric latitude
     */
    public LatLonGeo withGeocentricLatitudeOn( Datum d )
    {
        return d.toGeocentricLatitude( this );
    }

    /**
     * <p>
     * Returns a newly constructed <code>LatLonGeo</code> generated
     * by displacing this <code>LatLonGeo</code> a specified distance
     * along a geodesic at the given azimuth.  If the datum is a
     * sphere the geodesic is a great circle.
     * </p>
     *
     * <p>
     * This method calls {@link Datum#displace(LatLonGeo, double, double)}
     * on the supplied datum using this <code>LatLonGeo</code> as the
     * initial point.
     * </p>
     *
     * @param dist distance to displace the current <code>LatLonGeo</code>
     * @param azimuth initial azimuth of displacement direction
     * @param datum underlying datum to use for the displacement
     * @return a newly constructed <code>LatLonGeo</code> that has been displaced
     *         by the specified distance
     */
    public LatLonGeo displacedBy( double dist, double azimuth, Datum datum )
    {
        return datum.displace( this, dist, azimuth );
    }

    /**
     * Same as {@link LatLonGeo#displacedBy(double, double, Datum)}, only
     * the default datum is used instead of a user-supplied datum.
     *
     * @see{@link LatLonGeo#defaultDatum}
     */
    public LatLonGeo displacedBy( double dist, double azimuth )
    {
        return displacedBy( dist, azimuth, defaultDatum );
    }

    public LatLonGeo displacedBy( DistanceAzimuth distAzimuth, Datum datum )
    {
        return displacedBy( distAzimuth.getDistance( ), distAzimuth.getAzimuth( ), datum );
    }

    public LatLonGeo displacedBy( DistanceAzimuth distAzimuth )
    {
        return displacedBy( distAzimuth, defaultDatum );
    }

    public DistanceAzimuth getDistanceAzimuthTo( LatLonGeo to, Datum datum )
    {
        return datum.getDistanceAzimuth( this, to );
    }

    public DistanceAzimuth getDistanceAzimuthTo( LatLonGeo to )
    {
        return getDistanceAzimuthTo( to, defaultDatum );
    }

    public double getDistanceTo( LatLonGeo to, Datum datum )
    {
        return datum.getDistance( this, to );
    }

    public double getDistanceTo( LatLonGeo to )
    {
        return getDistanceTo( to, defaultDatum );
    }

    public double getAzimuthTo( LatLonGeo to, Datum datum )
    {
        return datum.getAzimuth( this, to );
    }

    public double getAzimuthTo( LatLonGeo to )
    {
        return getAzimuthTo( to, defaultDatum );
    }

    public double getAzimuthFrom( LatLonGeo from, Datum datum )
    {
        return datum.getAzimuth( from, this );
    }

    public double getAzimuthFrom( LatLonGeo from )
    {
        return getAzimuthFrom( from, defaultDatum );
    }

    /**
     * Normalizes a longitude to be in the range [-&pi;,&pi;) radians.
     *
     * <p>Warning: This method produces a near-infinite loop if argument is a very large value.</p>
     */
    public static final double normalizeLon( double lonRad )
    {
        return normalizeAnglePi( lonRad );
    }

    /**
     * Normalizes a latitude to be in the range [-&pi;/2,&pi;/2] radians.
     *
     * <p>Warning: This method produces a near-infinite loop if argument is a very large value.</p>
     */
    public static final double normalizeLat( double latRad )
    {
        double angle = normalizeAnglePi( latRad );

        if ( angle < -piOverTwo )
        {
            angle = -pi - angle;
        }
        else if ( angle > piOverTwo )
        {
            angle = pi - angle;
        }
        return angle;
    }

    /**
     * Normalizes an angle to lie within [-&pi;,&pi;) radians.
     */
    public static final double normalizeAnglePi( double rad )
    {
        rad = Angle.normalizeAnglePi( rad );

        if ( rad == Math.PI )
        {
            return -Math.PI; // to maintain backward compatibility for this version of the method
        }

        return rad;
    }

    public String format( String componentFormat )
    {
        return formatDeg( componentFormat );
    }

    public String formatDeg( String componentFormat )
    {
        return String.format( componentFormat + "," + componentFormat, getLatDeg( ), getLonDeg( ) );
    }

    public String formatRad( String componentFormat )
    {
        return String.format( componentFormat + "," + componentFormat, getLatRad( ), getLonRad( ) );
    }

    public String toString( LatLonFormat latLonFormat )
    {
        return latLonFormat.format( this );
    }

    @Override
    public String toString( )
    {
        return defaultFormat.format( this );
    }

    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits( altitude );
        result = prime * result + ( int ) ( temp ^ ( temp >>> 32 ) );
        temp = Double.doubleToLongBits( lat );
        result = prime * result + ( int ) ( temp ^ ( temp >>> 32 ) );
        temp = Double.doubleToLongBits( lon );
        result = prime * result + ( int ) ( temp ^ ( temp >>> 32 ) );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;

        LatLonGeo other = ( LatLonGeo ) obj;
        if ( Double.doubleToLongBits( altitude ) != Double.doubleToLongBits( other.altitude ) ) return false;

        if ( Double.doubleToLongBits( lat ) != Double.doubleToLongBits( other.lat ) ) return false;

        if ( Double.doubleToLongBits( lon ) != Double.doubleToLongBits( other.lon ) ) return false;

        return true;
    }
}

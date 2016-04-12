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
package com.metsci.glimpse.util.geo.projection;

import java.io.Serializable;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.datum.DatumSphereWgs84;
import com.metsci.glimpse.util.math.fast.PolynomialApprox;
import com.metsci.glimpse.util.units.Angle;
import com.metsci.glimpse.util.vector.Vector2d;
import com.metsci.glimpse.util.vector.Vector3d;

/**
 * Implementation of GeoProjection via a plane which is tangent to the Earth and maps x, y
 * coordinates on the plane to/from lat/lon pairs.  Class instances are immutable.
 *
 * @author moskowitz
 */
public class TangentPlane implements GeoProjection, Serializable
{
    public static final long serialVersionUID = -6802219476339525122L;
    private static final Vector2d defaultTangentPointOnPlane = new Vector2d( 0.0, 0.0 );
    private static final double earthRadius = DatumSphereWgs84.Constants.avgGeodesicRadius;

    // reference LatLon at point of tangency as a LatLon instance and also represented as point on
    // unit sphere (Earth units: radius of earth = 1.0)
    private final Vector3d _refPointOnUnitSphere;
    private final LatLonGeo _refLatLon;

    // coordinates (x,y) of point on the plane tangent to reference LatLon
    private final Vector2d _tangentPointOnPlane;

    // directions in Earth/Sphere coordinate system (but not tangent plane) at reference LatLon
    private final Vector3d _localNorth;
    private final Vector3d _localEast;

    /**
     * Create tangent plane mapping latLon to 0,0 on plane.
     *
     * @param  latLon
     */
    public TangentPlane( LatLonGeo latLon )
    {
        this( latLon, defaultTangentPointOnPlane );
    }

    /**
     * Create tangent plane mapping latLon to x, y on plane.
     *
     * @param  latLon
     * @param  tangentPointOnPlaneX
     * @param  tangentPointOnPlaneY
     */
    public TangentPlane( LatLonGeo latLon, double tangentPointOnPlaneX, double tangentPointOnPlaneY )
    {
        this( latLon, new Vector2d( tangentPointOnPlaneX, tangentPointOnPlaneY ) );
    }

    /**
     * Create tangent plane mapping latLon to Vector2d(x, y) on plane.
     *
     * @param  latLon
     * @param  tangentPointOnPlane
     */
    private TangentPlane( LatLonGeo latLon, Vector2d tangentPointOnPlane )
    {
        _refLatLon = latLon;
        _refPointOnUnitSphere = latLonToPointOnUnitSphere( latLon );
        _tangentPointOnPlane = tangentPointOnPlane;

        double x = _refPointOnUnitSphere.getX( );
        double y = _refPointOnUnitSphere.getY( );
        double z = _refPointOnUnitSphere.getZ( );
        _localEast = new Vector3d( -y, x, 0 ).normalizedLenient( );
        _localNorth = new Vector3d( -x * z, -y * z, ( x * x ) + ( y * y ) ).normalizedLenient( );
    }

    @Override
    public LatLonGeo unproject( double planeX, double planeY )
    {
        Vector3d pointOnUnitSphere = planeXYToUnitSphere( planeX, planeY );

        return pointOnUnitSphereToLatLon( pointOnUnitSphere );
    }

    @Override
    public Vector2d project( LatLonGeo latLon )
    {
        Vector3d pointOnUnitSphere = latLonToPointOnUnitSphere( latLon );
        Vector2d ns = unitSphereToPlaneXY( pointOnUnitSphere );

        return ns;
    }

    // This is defined here as a member function so it can be overriden by subclasses to improve runtime.
    protected double calcAtan2( double y, double x )
    {
        return Math.atan2( y, x );
    }

    /**
     * Converts from LatLon to a point on the unit sphere (ECEF-r).
     */
    private static Vector3d latLonToPointOnUnitSphere( LatLonGeo latLon )
    {
        double latRad = latLon.getLatRad( );
        double lonRad = latLon.getLonRad( );
        Vector3d pointOnUnitSphere = new Vector3d( Math.cos( latRad ) * Math.cos( lonRad ), Math.cos( latRad ) * Math.sin( lonRad ), Math.sin( latRad ) );

        return pointOnUnitSphere;
    }

    /**
     * Converts a point on the unit sphere (ECEF-r) to LatLon.
     */
    private LatLonGeo pointOnUnitSphereToLatLon( Vector3d pointOnUnitSphere )
    {
        double lonRad = calcAtan2( pointOnUnitSphere.getY( ), pointOnUnitSphere.getX( ) );
        double latRad = PolynomialApprox.asin( pointOnUnitSphere.getZ( ) );

        LatLonGeo latLon = new LatLonGeo( Angle.radiansToDegrees( latRad ), Angle.radiansToDegrees( lonRad ) );

        return latLon;
    }

    /**
     * Converts position in tangent plane coordinates to point in Earth/sphere coordinates.
     *
     * @param   planeX  x position on tangent plane
     * @param   planeY  y position on tangent plane
     * @return  point in Earth/sphere coordinates
     */
    private Vector3d planeCoordsToEarthCoords( double planeX, double planeY )
    {
        Vector3d sphereCoords = Vector3d.linearCombination( earthRadius, _refPointOnUnitSphere, planeX, _localEast, planeY, _localNorth );

        sphereCoords = sphereCoords.scaledBy( 1.0 / earthRadius );

        return sphereCoords;
    }

    /**
     * Convert x, y position on tangent plane to (nearly) equivalent point on the unit sphere
     * (ECEF-r).
     *
     * @param   planeX  x position on tangent plane
     * @param   planeY  y position on tangent plane
     * @return  point on unit sphere
     */
    private Vector3d planeXYToUnitSphere( double planeX, double planeY )
    {
        double dx = planeX - _tangentPointOnPlane.getX( );
        double dy = planeY - _tangentPointOnPlane.getY( );
        Vector3d earthCoords = planeCoordsToEarthCoords( dx, dy );

        double a = dx / earthRadius;
        double b = dy / earthRadius;

        double beta = 4.0 / ( 4.0 + ( a * a ) + ( b * b ) );

        Vector3d pointOnUnitSphere = Vector3d.linearCombination( beta, earthCoords, beta - 1.0, _refPointOnUnitSphere );

        return pointOnUnitSphere;
    }

    /**
     * Convert point on the unit sphere (ECEF-r) to (nearly) equivalent x, y position on tangent
     * plane.
     *
     * @param   pointOnUnitSphere
     * @return  position on tangent plane
     */
    private Vector2d unitSphereToPlaneXY( Vector3d pointOnUnitSphere )
    {
        assert Math.abs( pointOnUnitSphere.norm( ) - 1.0 ) < 1.0e-3;

        double xdotp = pointOnUnitSphere.dotProduct( _refPointOnUnitSphere );
        double div = 1 + xdotp;
        Vector2d planeXY = new Vector2d( 2 * pointOnUnitSphere.dotProduct( _localEast ) / div, 2 * pointOnUnitSphere.dotProduct( _localNorth ) / div );
        planeXY = planeXY.scaledBy( earthRadius ).plus( _tangentPointOnPlane );

        return planeXY;
    }

    /**
     * Convert velocity at position on tangent plane to (nearly) equivalent velocity on unit sphere.
     *
     * @param   velX    x velocity on old tangent plane
     * @param   velY    y velocity on old tangent plane
     * @param   planeX  x position on old tangent plane
     * @param   planeY  y position on old tangent plane
     * @return  velocity on unit sphere
     */
    private Vector3d velXYToUnitSphere( double velX, double velY, double planeX, double planeY )
    {
        double a = planeX / earthRadius;
        double b = planeY / earthRadius;
        double da = velX / earthRadius;
        double db = velY / earthRadius;
        double denom = 4.0 + ( a * a ) + ( b * b );
        double beta = 4.0 / denom;
        double dBeta = ( ( da * a ) + ( db * b ) ) * -8.0 / ( denom * denom );

        Vector3d velOnSphere = Vector3d.linearCombination( 2.0, _refPointOnUnitSphere, a, _localEast, b, _localNorth );
        velOnSphere = velOnSphere.scaledBy( dBeta );

        Vector3d addTerm = Vector3d.linearCombination( da, _localEast, db, _localNorth );
        addTerm = addTerm.scaledBy( beta );
        velOnSphere = velOnSphere.plus( addTerm );

        return velOnSphere;
    }

    /**
     * Convert velocity at position on unit sphere to (nearly) equivalent velocity on tangent plane.
     *
     * @param   velOnUnitSphere
     * @param   pointOnUnitSphere
     * @return  velocity on tangent plane
     */
    private Vector2d velUnitSphereToVelXY( Vector3d velOnUnitSphere, Vector3d pointOnUnitSphere )
    {
        double onePlusXdotP = ( 1.0 + pointOnUnitSphere.dotProduct( _refPointOnUnitSphere ) );
        Vector2d velXY = new Vector2d( velOnUnitSphere.dotProduct( _localEast ), velOnUnitSphere.dotProduct( _localNorth ) );
        velXY = velXY.scaledBy( 2.0 / onePlusXdotP );

        Vector2d subTerm = new Vector2d( pointOnUnitSphere.dotProduct( _localEast ), pointOnUnitSphere.dotProduct( _localNorth ) );

        double dxdotP = velOnUnitSphere.dotProduct( _refPointOnUnitSphere );
        subTerm = subTerm.scaledBy( 2.0 * dxdotP / ( onePlusXdotP * onePlusXdotP ) );
        velXY = velXY.minus( subTerm ).scaledBy( earthRadius );

        return velXY;
    }

    public LatLonGeo getRefLatLon( )
    {
        return _refLatLon;
    }

    public Vector3d getRefPointOnUnitSphere( )
    {
        return _refPointOnUnitSphere;
    }

    /**
     * The vector (in ECEF-r coordinates) corresponding to traveling East on the tangent plane.
     */
    public Vector3d getLocalEast( )
    {
        return _localEast;
    }

    /**
     * The vector (in ECEF-r coordinates) corresponding to traveling North on the tangent plane.
     */
    public Vector3d getLocalNorth( )
    {
        return _localNorth;
    }

    /**
     * Tangent plane coordinates (x,y) of point on the plane tangent to reference LatLon.
     */
    public Vector2d getTangentPointOnPlane( )
    {
        return _tangentPointOnPlane;
    }

    @Override
    public Vector2d reprojectFrom( double x, double y, GeoProjection fromProjection )
    {
        if ( fromProjection instanceof TangentPlane )
        {
            TangentPlane fromTangentPlane = ( TangentPlane ) fromProjection;
            Vector3d pointOnUnitSphere = fromTangentPlane.planeXYToUnitSphere( x, y );

            return unitSphereToPlaneXY( pointOnUnitSphere );
        }
        else
        {
            LatLonGeo unproj = fromProjection.unproject( x, y );

            return project( unproj );
        }
    }

    @Override
    public KinematicVector2d reprojectPosVelFrom( double x, double y, double vx, double vy, GeoProjection fromProjection )
    {
        if ( fromProjection instanceof TangentPlane )
        {
            TangentPlane fromTangentPlane = ( TangentPlane ) fromProjection;

            // Unproject from old plane to unit sphere
            Vector3d pointOnUnitSphere = fromTangentPlane.planeXYToUnitSphere( x, y );
            Vector3d velOnUnitSphere = fromTangentPlane.velXYToUnitSphere( vx, vy, x, y );

            // Project from unit sphere to new plane
            Vector2d planeXY = unitSphereToPlaneXY( pointOnUnitSphere );
            Vector2d velXY = velUnitSphereToVelXY( velOnUnitSphere, pointOnUnitSphere );

            return new KinematicVector2d( planeXY, velXY );
        }
        else
        {
            throw new RuntimeException( " cannot handle arbitrary case with different type of projection" );
        }
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == null )
        {
            return false;
        }
        else if ( ! ( o instanceof TangentPlane ) )
        {
            return false;
        }
        else
        {
            TangentPlane other = ( TangentPlane ) o;

            return _refPointOnUnitSphere.equals( other._refPointOnUnitSphere ) && _tangentPointOnPlane.equals( other._tangentPointOnPlane );
        }
    }

    @Override
    public int hashCode( )
    {
        return _refPointOnUnitSphere.hashCode( ) ^ _tangentPointOnPlane.hashCode( );
    }

    @Override
    public String toString( )
    {
        StringBuffer sb = new StringBuffer( );
        sb.append( String.format( "TP[LatLon(%s)", getRefLatLon( ).format( "%.4f" ) ) );
        if ( !_tangentPointOnPlane.equals( defaultTangentPointOnPlane ) )
        {
            sb.append( String.format( ", %s", _tangentPointOnPlane.format( "%.3f" ) ) );
        }

        sb.append( "]" );

        return sb.toString( );
    }
}

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
package com.metsci.glimpse.util.vector;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.io.Serializable;
import java.util.logging.Logger;

import com.metsci.glimpse.util.StackTraceUtils;
import com.metsci.glimpse.util.math.fast.FastAtan;
import com.metsci.glimpse.util.units.Angle;
import com.metsci.glimpse.util.units.AngleRelative;
import com.metsci.glimpse.util.units.Azimuth;

/**
 * Basic class representing an immutable vector containing 3 doubles.  All inputs and outputs are in
 * system units.
 *
 * @author moskowitz
 */
public final class Vector3d implements Serializable
{
    private static final long serialVersionUID = 7660130239937273594L;
    private static final Logger logger = Logger.getLogger( Vector3d.class.getName( ) );

    private final double x;
    private final double y;
    private final double z;

    /**
     * Standard constructor.
     *
     * @param  x
     * @param  y
     * @param  z
     */
    public Vector3d( double x, double y, double z )
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Default constructor.  Creates zero vector.
     */
    public Vector3d( )
    {
        this( 0, 0, 0 );
    }

    /**
     * Create Vector3d from cylindrical coordinates.
     *
     * <p>Note: Negative radius is allowed.  This reverses the vector direction in x and y.</p>
     *
     * @param   radius  distance from z-axis (XY plane projection radius)
     * @param   theta   horizontal azimuth angle (XY plane projection theta)
     * @param   z       z-coordinate
     * @return  Vector3d
     */
    public static Vector3d createCylindrical( double radius, double theta, double z )
    {
        double theta_MATHRAD = Azimuth.toMathRad( theta );
        double unitX = Math.cos( theta_MATHRAD );
        double unitY = Math.sin( theta_MATHRAD );

        return new Vector3d( radius * unitX, radius * unitY, z );
    }

    public double getX( )
    {
        return x;
    }

    public double getY( )
    {
        return y;
    }

    public double getZ( )
    {
        return z;
    }

    public Vector2d getXY( )
    {
        return new Vector2d( x, y );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == null )
        {
            return false;
        }
        else if ( ! ( o instanceof Vector3d ) )
        {
            return false;
        }
        else
        {
            Vector3d other = ( Vector3d ) o;

            return ( x == other.x ) && ( y == other.y ) && ( z == other.z );
        }
    }

    @Override
    public int hashCode( )
    {
        return new Double( x ).hashCode( ) ^ new Double( y ).hashCode( ) ^ new Double( z ).hashCode( );
    }

    /**
     * Get formatted String representation.
     *
     * @param   coordFormat  format applied to each coordinate (as in String.format)
     * @return  formatted string with comma separated coordinates
     */
    public String format( String coordFormat )
    {
        return String.format( "(" + coordFormat + ", " + coordFormat + ", " + coordFormat + ")", x, y, z );
    }

    public double[] toArray( )
    {
        return new double[] { x, y, z };
    }

    public static Vector3d fromArray( double[] coords )
    {
        assert coords.length == 3;

        return new Vector3d( coords[0], coords[1], coords[2] );
    }

    @Override
    public String toString( )
    {
        return format( "%.5g" );
    }

    public Vector3d plus( Vector3d v )
    {
        return new Vector3d( x + v.x, y + v.y, z + v.z );
    }

    public Vector3d minus( Vector3d v )
    {
        return new Vector3d( x - v.x, y - v.y, z - v.z );
    }

    /**
     * @deprecated use {@link #scaledBy(double)}
     */
    @Deprecated
    public Vector3d scalarProduct( double alpha )
    {
        return scaledBy( alpha );
    }

    public Vector3d scaledBy( double scaleFactor )
    {
        if ( scaleFactor != 1.0 )
        {
            return new Vector3d( scaleFactor * x, scaleFactor * y, scaleFactor * z );
        }

        return this;
    }

    public double dotProduct( Vector3d v )
    {
        return ( x * v.x ) + ( y * v.y ) + ( z * v.z );
    }

    public Vector3d crossProduct( Vector3d v )
    {
        return new Vector3d( ( y * v.z ) - ( z * v.y ), ( z * v.x ) - ( x * v.z ), ( x * v.y ) - ( y * v.x ) );
    }

    public double normSquared( )
    {
        return ( x * x ) + ( y * y ) + ( z * z );
    }

    public double distanceSquared( Vector3d v )
    {
        return ( ( x - v.x ) * ( x - v.x ) ) + ( ( y - v.y ) * ( y - v.y ) ) + ( ( z - v.z ) * ( z - v.z ) );
    }

    public double distance( Vector3d v )
    {
        return Math.sqrt( distanceSquared( v ) );
    }

    public double norm( )
    {
        return Math.sqrt( normSquared( ) );
    }

    public boolean isZero( )
    {
        return ( x == 0 ) && ( y == 0 ) && ( z == 0 );
    }

    public double distanceOnXYPlaneSquared( Vector2d v )
    {
        return ( ( x - v.getX( ) ) * ( x - v.getX( ) ) ) + ( ( y - v.getY( ) ) * ( y - v.getY( ) ) );
    }

    public double distanceOnXYPlane( Vector2d v )
    {
        return Math.sqrt( distanceOnXYPlaneSquared( v ) );
    }

    public double distanceOnXYPlaneSquared( Vector3d v )
    {
        return ( ( x - v.x ) * ( x - v.x ) ) + ( ( y - v.y ) * ( y - v.y ) );
    }

    public double distanceOnXYPlane( Vector3d v )
    {
        return Math.sqrt( distanceOnXYPlaneSquared( v ) );
    }

    /**
     * False if any of the coordinates is NaN or Infinite.
     */
    public boolean isValid( )
    {
        if ( Double.isNaN( x ) || Double.isNaN( y ) || Double.isNaN( z ) )
        {
            return false;
        }

        if ( Double.isInfinite( x ) || Double.isInfinite( y ) || Double.isInfinite( z ) )
        {
            return false;
        }

        return true;
    }

    public Vector3d withZ( double z )
    {
        return new Vector3d( x, y, z );
    }

    /**
     * Returns normalized (rescaled to norm = 1) version of this vector.
     *
     * <p>Note: "Strict" version: if zero vector, returned vector will contain all NaN values and a
     * warning will be logged.</p>
     *
     * @return  normalized version of this vector
     */
    public Vector3d normalized( )
    {
        double alpha = norm( );
        if ( alpha == 0 )
        {
            logWarning( logger, "Normalizing a zero vector.  Will return all NaN values.\n" + StackTraceUtils.getCallers( 5 ) );
        }

        return scaledBy( 1.0 / alpha );
    }

    /**
     * Returns normalized (rescaled to norm = 1) version of this vector.
     *
     * <p>Note: "Lenient" version: If zero vector, returned vector will be (1, 0, 0).</p>
     *
     * @return  normalized version of this vector
     */
    public Vector3d normalizedLenient( )
    {
        double alpha = norm( );
        if ( alpha == 0 )
        {
            return new Vector3d( 1, 0, 0 );
        }

        return scaledBy( 1.0 / alpha );
    }

    /**
     * Returns the azimuth angle of the projection of this vector on the xy-plane.
     */
    public double horizontalAzimuthAngle( )
    {
        double angle_MATHRAD = Math.atan2( this.getY( ), this.getX( ) );

        return Azimuth.fromMathRad( angle_MATHRAD );
    }

    /**
     * Returns the azimuth angle of the projection of this vector on the xy-plane.
     *
     * <p>Note: This version uses a faster, less accurate, calculation.</p>
     */
    public double horizontalAzimuthAngleFast( )
    {
        double angle_MATHRAD = FastAtan.getInstance( ).atan2( this.getY( ), this.getX( ) );

        return Azimuth.fromMathRad( angle_MATHRAD );
    }

    /**
     * Returns the conical angle between this vector and the positive z axis.  Zero on the +z axis.
     * Positive elsewhere with max value of PI radians or 180 degrees (in system units).
     */
    public double phiAngle( )
    {
        double phi_RAD;
        if ( ( this.getX( ) == 0 ) && ( this.getY( ) == 0 ) )
        {
            if ( this.getZ( ) < 0 )
            {
                phi_RAD = Math.PI;
            }
            else
            {
                phi_RAD = 0;
            }
        }
        else
        {
            phi_RAD = Math.acos( this.getZ( ) / this.norm( ) );
        }

        return Angle.fromRad( phi_RAD );
    }

    /**
     * Returns the conical angle between this vector and the xy-plane.  Zero on the xy-plane.
     * Positive above the plane to a max of PI/2 or 90 degrees (in system units), negative below the
     * plane to a min of -PI/2 or -90 degrees (in system units).
     */
    public double elevationAngle( )
    {
        return Angle.fromRad( Math.PI / 2 ) - phiAngle( );
    }

    /**
     * @param   theta  rotation angle around the x-axis.
     * @return  rotated copy
     */
    public Vector3d rotatedAboutXAxisBy( double theta )
    {
        double theta_CCWRAD = AngleRelative.toCcwRad( theta );
        double sin = Math.sin( theta_CCWRAD );
        double cos = Math.cos( theta_CCWRAD );
        double rotY = ( y * cos ) - ( z * sin );
        double rotZ = ( y * sin ) + ( z * cos );

        return new Vector3d( x, rotY, rotZ );
    }

    /**
     * @param   theta  rotation angle around the y-axis.
     * @return  rotated copy
     */
    public Vector3d rotatedAboutYAxisBy( double theta )
    {
        double theta_CCWRAD = AngleRelative.toCcwRad( theta );
        double sin = Math.sin( theta_CCWRAD );
        double cos = Math.cos( theta_CCWRAD );
        double rotZ = ( z * cos ) - ( x * sin );
        double rotX = ( z * sin ) + ( x * cos );

        return new Vector3d( rotX, y, rotZ );
    }

    /**
     * @param   theta  rotation angle around the z-axis.
     * @return  rotated copy
     */
    public Vector3d rotatedAboutZAxisBy( double theta )
    {
        double theta_CCWRAD = AngleRelative.toCcwRad( theta );
        double sin = Math.sin( theta_CCWRAD );
        double cos = Math.cos( theta_CCWRAD );
        double rotX = ( x * cos ) - ( y * sin );
        double rotY = ( x * sin ) + ( y * cos );

        return new Vector3d( rotX, rotY, z );
    }

    /**
     * Returns cosine of the angle between this vector and vector v.
     */
    public double cosAngleWith( Vector3d v )
    {
        if ( isZero( ) || v.isZero( ) )
        {
            return 1;
        }

        double cosAngle = dotProduct( v ) / ( norm( ) * v.norm( ) );
        cosAngle = Math.min( 1, Math.max( -1, cosAngle ) );

        return cosAngle;
    }

    /**
     * Returns the conical (unsigned) angle between this vector and vector v.
     */
    public double angleWith( Vector3d v )
    {
        return Angle.fromRad( Math.acos( cosAngleWith( v ) ) );
    }

    /**
     * Returns the projection of this vector onto the vector v.
     *
     * @see #projectionOnto(Vector2d, boolean) for lenient handling of v
     */
    public Vector3d projectOnto( Vector3d v )
    {
        double scale = dotProduct( v ) / v.normSquared( );

        return v.scaledBy( scale );
    }

    /**
     * Returns the projection of this vector onto the vector v.  Lenient version treats zero vector for v as (1,0,0)
     * in order to avoid returning vector containing NaN values.
     */
    public Vector3d projectionOnto( Vector3d v, boolean isLenient )
    {
        if ( isLenient && v.isZero( ) )
        {
            v = new Vector3d( 1, 0, 0 );
        }

        return projectOnto( v );
    }

    /**
     * Returns reflection of this vector through a plane specified by a point on the plane and a
     * vector normal to the plane.
     *
     * @param   x0      point on the plane
     * @param   normal  normal to the plane
     * @return  reflected position
     */
    public Vector3d reflectionThroughPlaneAt( Vector3d x0, Vector3d normal )
    {
        Vector3d x1 = this.minus( x0 );

        double alpha = -2 * x1.dotProduct( normal ) / normal.dotProduct( normal );
        Vector3d reflection = Vector3d.linearCombination( 1, this, alpha, normal );

        return reflection;
    }

    /**
     * Returns unit vectors w0 and w1 which are orthogonal to this vector and each other.
     */
    public Vector3d[] orthonormalVectors( )
    {
        Vector3d w0;
        Vector3d w1;
        if ( !isZero( ) )
        {
            if ( x == 0 )
            {
                w0 = new Vector3d( 1, 0, 0 );
            }
            else
            {
                w0 = new Vector3d( -y, x, 0 ).normalized( );
            }

            w1 = crossProduct( w0 ).normalized( );
        }
        else
        {
            w0 = new Vector3d( 1.0, 0.0, 0.0 );
            w1 = new Vector3d( 0.0, 1.0, 0.0 );
        }

        Vector3d[] w0w1 = { w0, w1 };

        return w0w1;
    }

    public static Vector3d linearCombination( double a1, Vector3d v1, double a2, Vector3d v2 )
    {
        return new Vector3d( ( a1 * v1.getX( ) ) + ( a2 * v2.getX( ) ), ( a1 * v1.getY( ) ) + ( a2 * v2.getY( ) ), ( a1 * v1.getZ( ) ) + ( a2 * v2.getZ( ) ) );
    }

    public static Vector3d linearCombination( double a1, Vector3d v1, double a2, Vector3d v2, double a3, Vector3d v3 )
    {
        Vector3d v = new Vector3d( ( a1 * v1.getX( ) ) + ( a2 * v2.getX( ) ) + ( a3 * v3.getX( ) ), ( a1 * v1.getY( ) ) + ( a2 * v2.getY( ) ) + ( a3 * v3.getY( ) ), ( a1 * v1.getZ( ) ) + ( a2 * v2.getZ( ) ) + ( a3 * v3.getZ( ) ) );

        return v;
    }

    public static Vector3d linearCombination( double[] a, Vector3d[] w )
    {
        if ( a.length != w.length )
        {
            throw new RuntimeException( "Incompatible arrays in linearCombination" );
        }

        double xTot = 0.0;
        double yTot = 0.0;
        double zTot = 0.0;
        for ( int i = 0; i < a.length; i++ )
        {
            xTot += ( a[i] * w[i].getX( ) );
            yTot += ( a[i] * w[i].getY( ) );
            zTot += ( a[i] * w[i].getZ( ) );
        }

        return new Vector3d( xTot, yTot, zTot );
    }

    /**
     * Linear interpolation between vectors v0 and v1.  t = 0 returns v0 and t = 1 returns v1.
     */
    public static Vector3d interpolate( double t, Vector3d v0, Vector3d v1 )
    {
        return linearCombination( ( 1 - t ), v0, t, v1 );
    }
}

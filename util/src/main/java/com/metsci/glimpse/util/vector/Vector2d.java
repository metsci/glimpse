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
 * Basic class representing an immutable vector containing 2 doubles.  All inputs and outputs are in
 * system units.
 *
 * @author moskowitz
 */
public final class Vector2d implements Serializable
{
    private static final long serialVersionUID = 7660130239937273594L;
    private static final Logger logger = Logger.getLogger( Vector2d.class.getName( ) );

    private final double x;
    private final double y;

    public Vector2d( double x, double y )
    {
        this.x = x;
        this.y = y;
    }

    public Vector2d( )
    {
        this( 0, 0 );
    }

    public double getX( )
    {
        return x;
    }

    public double getY( )
    {
        return y;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == null )
        {
            return false;
        }
        else if ( ! ( o instanceof Vector2d ) )
        {
            return false;
        }
        else
        {
            Vector2d other = ( Vector2d ) o;

            return ( x == other.x ) && ( y == other.y );
        }
    }

    @Override
    public int hashCode( )
    {
        return new Double( x ).hashCode( ) ^ new Double( y ).hashCode( );
    }

    /**
     * Get formatted String representation.
     *
     * @param   coordFormat  format applied to each coordinate (as in String.format)
     * @return  formatted string with comma separated coordinates
     */
    public String format( String coordFormat )
    {
        return String.format( "(" + coordFormat + ", " + coordFormat + ")", x, y );
    }

    @Override
    public String toString( )
    {
        return format( "%.5g" );
    }

    public Vector2d plus( Vector2d v )
    {
        return new Vector2d( x + v.x, y + v.y );
    }

    public Vector2d minus( Vector2d v )
    {
        return new Vector2d( x - v.x, y - v.y );
    }

    /**
     * @deprecated use {@link #scaledBy(double)}
     */
    @Deprecated
    public Vector2d scalarProduct( double alpha )
    {
        return scaledBy( alpha );
    }

    public Vector2d scaledBy( double scaleFactor )
    {
        if ( scaleFactor != 1.0 )
        {
            return new Vector2d( scaleFactor * x, scaleFactor * y );
        }

        return this;
    }

    public double dotProduct( Vector2d v )
    {
        return ( x * v.x ) + ( y * v.y );
    }

    public double crossProduct( Vector2d v )
    {
        return ( x * v.y ) - ( y * v.x );
    }

    public boolean isToRightOf( Vector2d v )
    {
        return crossProduct( v ) > 0.0;
    }

    public double azimuthAngle( )
    {
        return Azimuth.fromMathRad( Math.atan2( y, x ) );
    }

    public double azimuthAngleFast( )
    {
        return Azimuth.fromMathRad( FastAtan.getInstance( ).atan2( this.getY( ), this.getX( ) ) );
    }

    public double normSquared( )
    {
        return ( x * x ) + ( y * y );
    }

    public double norm( )
    {
        return Math.sqrt( normSquared( ) );
    }

    public boolean isZero( )
    {
        return ( x == 0 ) && ( y == 0 );
    }

    /**
     * Returns normalized (rescaled to norm = 1) version of this vector.
     *
     * <p>Note: "Strict" version: if zero vector, returned vector will contain all NaN values and a
     * warning will be logged.</p>
     *
     * @return  normalized version of this vector
     */
    public Vector2d normalized( )
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
     * <p>Note: "Lenient" version: If zero vector, returned vector will be (1, 0).</p>
     *
     * @return  normalized version of this vector
     */
    public Vector2d normalizedLenient( )
    {
        double alpha = norm( );
        if ( alpha == 0 )
        {
            return new Vector2d( 1, 0 );
        }

        return scaledBy( 1.0 / alpha );
    }

    public double distanceSquared( Vector2d v )
    {
        return ( ( x - v.x ) * ( x - v.x ) ) + ( ( y - v.y ) * ( y - v.y ) );
    }

    public double distance( Vector2d v )
    {
        return Math.sqrt( distanceSquared( v ) );
    }

    /**
     * Returns cosine of the angle between this vector and vector v.
     */
    public double cosAngleWith( Vector2d v )
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
    public double angleWith( Vector2d v )
    {
        return Angle.fromRad( Math.acos( cosAngleWith( v ) ) );
    }

    /**
     * Returns the projection of this vector onto the vector v.
     *
     * @see #projectionOnto(Vector2d, boolean) for lenient handling of v
     */
    public Vector2d projectOnto( Vector2d v )
    {
        double scale = dotProduct( v ) / v.normSquared( );

        return v.scaledBy( scale );
    }

    /**
     * Returns the projection of this vector onto the vector v.  Lenient version treats zero vector for v as (1,0)
     * in order to avoid returning vector containing NaN values.
     */
    public Vector2d projectionOnto( Vector2d v, boolean isLenient )
    {
        if ( isLenient && v.isZero( ) )
        {
            v = new Vector2d( 1, 0 );
        }

        return projectOnto( v );
    }

    /**
     * @param   theta  rotation angle
     * @return  rotated copy
     */
    public Vector2d rotatedAboutOriginBy( double theta )
    {
        double theta_CCWRAD = AngleRelative.toCcwRad( theta );
        double sin = Math.sin( theta_CCWRAD );
        double cos = Math.cos( theta_CCWRAD );

        return new Vector2d( ( x * cos ) - ( y * sin ), ( x * sin ) + ( y * cos ) );
    }

    /**
     * Returns a vector perpendicular to this one, with same norm.
     */
    public Vector2d perpendicularVector( )
    {
        return new Vector2d( -y, x );
    }

    /**
     * Concatenate z component to form ThreeVector.
     *
     * @param   z
     * @return  ThreeVector
     */
    public Vector3d toVector3d( double z )
    {
        return new Vector3d( x, y, z );
    }

    public double[] toArray( )
    {
        return new double[] { x, y };
    }

    public static Vector2d fromArray( double[] coords )
    {
        assert coords.length == 2;

        return new Vector2d( coords[0], coords[1] );
    }

    /**
     * Create Vector2d from polar coordinates.
     *
     * @param   radius  distance from center (0,0)
     * @param   theta   azimuth angle
     * @return  Vector2d
     */
    public static Vector2d createPolar( double radius, double theta )
    {
        double theta_MATHRAD = Azimuth.toMathRad( theta );
        double unitX = Math.cos( theta_MATHRAD );
        double unitY = Math.sin( theta_MATHRAD );

        return new Vector2d( radius * unitX, radius * unitY );
    }

    /**
     * Create Vector2d from polar coordinates with given translation of origin.
     *
     * @param   radius      distance from center (translateX, translateY)
     * @param   theta       azimuth angle
     * @param   translateX
     * @param   translateY
     * @return  Vector2d
     */
    public static Vector2d createPolarTranslated( double radius, double theta, double translateX, double translateY )
    {
        double theta_MATHRAD = Azimuth.toMathRad( theta );
        double unitX = Math.cos( theta_MATHRAD );
        double unitY = Math.sin( theta_MATHRAD );

        return new Vector2d( translateX + ( radius * unitX ), translateY + ( radius * unitY ) );
    }

    public static Vector2d linearCombination( double a1, Vector2d v1, double a2, Vector2d v2 )
    {
        return new Vector2d( ( a1 * v1.getX( ) ) + ( a2 * v2.getX( ) ), ( a1 * v1.getY( ) ) + ( a2 * v2.getY( ) ) );
    }

    public static Vector2d linearCombination( double[] a, Vector2d[] w )
    {
        if ( a.length != w.length )
        {
            throw new RuntimeException( "Incompatible arrays in linearCombination" );
        }

        double xTot = 0.0;
        double yTot = 0.0;
        for ( int i = 0; i < a.length; i++ )
        {
            xTot += ( a[i] * w[i].getX( ) );
            yTot += ( a[i] * w[i].getY( ) );
        }

        return new Vector2d( xTot, yTot );
    }
}

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
package com.metsci.glimpse.util.geo.util;

import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.LatLonRect;
import com.metsci.glimpse.util.geo.datum.Datum;
import com.metsci.glimpse.util.units.Azimuth;

/**
 * @author osborn
 */
public final class SpheroidUtil
{
    private static final double pi = Math.PI;
    private static final double piOverTwo = pi / 2d;
    private static final double twoPi = pi * 2d;

    /**
     * Projects a geodetic latitude onto a geocentric latitude
     * using a ray through the center of the spheroid.
     *
     * @param geodeticLatRad geodetic latitude to project (in radians)
     * @param e2 squared eccentricity of new latitude's underlying spheroid
     * @return geocentric latitude corresponding to input geodetic latitude
     */
    public static double geodeticToGeocentric( double geodeticLatRad, double e2 )
    {
        final double absLatRad = Math.abs( geodeticLatRad );
        if ( absLatRad == Math.PI || absLatRad == 0 ) return geodeticLatRad;

        return Math.atan( ( 1d - e2 ) * Math.tan( geodeticLatRad ) );
    }

    /**
     * Projects a geocentric latitude onto a geodetic latitude
     * using a ray through the center of the sphere.
     *
     * @param geocentricLatRad geocentric latitude to project (in radians)
     * @param e2 squared eccentricity of the input latitude's underlying spheroid
     * @return geodetic latitude corresponding to input geocentric latitude
     */
    public static double geocentricToGeodetic( double geocentricLatRad, double e2 )
    {
        final double absLatRad = Math.abs( geocentricLatRad );
        if ( absLatRad == Math.PI || absLatRad == 0 ) return geocentricLatRad;

        return Math.atan( Math.tan( geocentricLatRad ) / ( 1d - e2 ) );
    }

    /**
     * Computes a spheroid's radius of curvature in the plane
     * of the meridian at a given latitude. Sign of latitude
     * is irrelevant due to symmetry.
     */
    public static double curvatureMeridional( double latRad, Datum d )
    {
        if ( d.isSpherical( ) ) return d.getEquatorialRadius( );

        final double e2 = d.getEccentricitySquared( );
        final double sinLat = Math.sin( latRad );

        final double A = 1d - e2 * sinLat * sinLat;
        final double B = 1d / Math.sqrt( A );
        final double C = 1d - e2;

        return d.getEquatorialRadius( ) * B * ( C * B * B );
    }

    /**
     * Computes a spheroid's radius of curvature in the plane perpendicular
     * to both the plane of the meridian and the tangent plane at
     * a given latitude.  Sign of latitude is irrelevant due to symmetry.
     */
    public static double curvatureNormal( double latRad, Datum d )
    {
        if ( d.isSpherical( ) ) return d.getEquatorialRadius( );

        final double e2 = d.getEccentricitySquared( );
        final double sinLat = Math.sin( latRad );

        final double A = 1d - e2 * sinLat * sinLat;
        final double B = 1d / Math.sqrt( A );

        return d.getEquatorialRadius( ) * B;
    }

    /**
     * Direct transformation from ECEF-r to ECEF-g coordinates.  Exact, but
     * point must be >~ 50km from center of earth on a WGS-84 ellipse.
     *
     * <p>H. Vermeille, Journal of Geodesy (2002) 76:451-454.</p>
     */
    public static LatLonGeo toLatLonGeo( double x, double y, double z, Datum d )
    {
        final double a = d.getEquatorialRadius( );
        final double e2 = d.getEccentricitySquared( );
        final double ra2 = 1d / ( a * a );

        final double e4 = e2 * e2;

        final double XXpYY = x * x + y * y;
        final double sqrtXXpYY = Math.sqrt( XXpYY );
        final double p = XXpYY * ra2;
        final double q = z * z * ( 1d - e2 ) * ra2;
        final double r = 1d / 6d * ( p + q - e4 );
        final double s = e4 * p * q / ( 4d * r * r * r );
        final double t = Math.pow( 1 + s + Math.sqrt( s * ( 2d + s ) ), 1d / 3d );
        final double u = r * ( 1d + t + 1d / t );
        final double v = Math.sqrt( u * u + e4 * q );
        final double w = e2 * ( u + v - q ) / ( 2d * v );
        final double k = Math.sqrt( u + v + w * w ) - w;
        final double D = k * sqrtXXpYY / ( k + e2 );
        final double sqrtDDpZZ = Math.sqrt( D * D + z * z );

        final double lat = 2d * Math.atan2( z, D + sqrtDDpZZ );
        final double lon = 2d * Math.atan2( y, x + sqrtXXpYY );
        final double altitude = ( k + e2 - 1d ) * sqrtDDpZZ / k;

        return LatLonGeo.fromRad( lat, lon, altitude );
    }

    /**
     * Transformation from ECEF-g to ECEF-r coordinates.
     */
    public static LatLonRect toLatLonRect( double northLatRad, double eastLonRad, double altitude, Datum d )
    {
        final double a = d.getEquatorialRadius( );
        final double e2 = d.getEccentricitySquared( );

        final double sinLat = Math.sin( northLatRad );
        final double cosLat = Math.cos( northLatRad );
        final double sinLon = Math.sin( eastLonRad );
        final double cosLon = Math.cos( eastLonRad );

        final double N = a / Math.sqrt( 1d - e2 * sinLat * sinLat );
        final double x = ( N + altitude ) * cosLat * cosLon;
        final double y = ( N + altitude ) * cosLat * sinLon;
        final double z = ( altitude + ( 1d - e2 ) * N ) * sinLat;

        return LatLonRect.fromXyz( x, y, z );
    }

    /**
     * <p>Solution of the geodetic direct problem using T. Vincenty modified
     * Rainsford's method with Helmert's elliptical terms.</p>
     *
     * <p>Code adapted from the Fortran implementation used in NOAA's
     * "The Geodetic Toolkit."</p>
     *
     * <p>
     * <b>NOTE:</b> Effective in any azimuth and at any distance short of antipodal.
     * </p>
     *
     * @param datum underlying datum for geodesic
     * @param from starting position
     * @param dist distance to travel
     * @param azimuth initial azimuth of geodesic
     * @return final position and initial azimuth of geodesic back to starting position
     */
    public static PositionAzimuth forward( Datum datum, LatLonGeo from, double dist, double azimuth )
    {
        /// map our args onto Fortran args
        final double GLAT1 = from.getLatRad( ); // start north lat (rad)
        final double GLON1 = from.getLonRad( ); // start east lon (rad)
        final double S = dist; // distance to move
        final double FAZ = Azimuth.toNavRad( azimuth ); // initial azimuth
        final double A = datum.getEquatorialRadius( );
        final double F = datum.getFlattening( );

        /// the results end up here
        double BAZ; // backward azimuth
        double GLAT2; // end north latitude
        double GLON2; // end east longitude

        /// specify tolerance
        final double EPS = 0.5e-13;

        double R = 1.0 - F;
        double TU = R * Math.sin( GLAT1 ) / Math.cos( GLAT1 );
        double SF = Math.sin( FAZ );
        double CF = Math.cos( FAZ );

        BAZ = 0;
        if ( CF != 0 )
        {
            BAZ = Math.atan2( TU, CF ) * 2;
        }

        double CU = 1.0 / Math.sqrt( TU * TU + 1.0 );
        double SU = TU * CU;
        double SA = CU * SF;
        double C2A = -SA * SA + 1.0;
        double X = Math.sqrt( ( 1.0 / R / R - 1.0 ) * C2A + 1.0 ) + 1.0;
        X = ( X - 2.0 ) / X;
        double C = 1.0 - X;
        C = ( X * X / 4.0 + 1.0 ) / C;
        double D = ( 0.375 * X * X - 1.0 ) * X;
        TU = S / R / A / C;
        double Y = TU;

        double SY;
        double CY;
        double CZ;
        double E;
        do
        {
            SY = Math.sin( Y );
            CY = Math.cos( Y );
            CZ = Math.cos( BAZ + Y );
            E = CZ * CZ * 2.0 - 1.0;
            C = Y;
            X = E * CY;
            Y = E + E - 1.0;
            Y = ( ( ( SY * SY * 4 - 3 ) * Y * CZ * D / 6 + X ) * D / 4 - CZ ) * SY * D + TU;
        }
        while ( Math.abs( Y - C ) > EPS );

        BAZ = CU * CY * CF - SU * SY;
        C = R * Math.sqrt( SA * SA + BAZ * BAZ );
        D = SU * CY + CU * SY * CF;
        GLAT2 = Math.atan2( D, C );
        C = CU * CY - SU * SY * CF;
        X = Math.atan2( SY * SF, C );
        C = ( ( -3 * C2A + 4 ) * F + 4 ) * C2A * F / 16;
        D = ( ( E * CY * C + CZ ) * SY * C + Y ) * SA;
        GLON2 = GLON1 + X - ( 1 - C ) * D * F;
        BAZ = Math.atan2( SA, BAZ ) + pi;

        /// package and ship results
        return new PositionAzimuth( LatLonGeo.fromRad( GLAT2, GLON2, from.getAltitude( ) ), Azimuth.fromNavRad( BAZ ) );
    }

    /**
     * Solution of the geodetic inverse problem using T. Vincenty modified
     * Rainsford's method with Helmert's elliptical terms. Code adapted from
     * the Fortran implementation used in NOAA's "The Geodetic Toolkit."
     *
     * <p>
     * <b>LIMITATION:</b> Effective in any azimuth and at any distance short of antipodal.
     * </p>
     *
     * <p>
     * <b>LIMITATION:</b> From/to stations must not be the geographic pole.
     * </p>
     *
     * <p>
     * <b>NOTE:</b> Be aware of lift-off and be careful on the equator.
     * </p>
     *
     * @param datum datum on which to invert geodesic
     * @param from start position
     * @param to end position
     * @return distance between points and initial geodesic azimuth from start to end
     */
    public static DistanceAzimuth inverse( Datum datum, LatLonGeo from, LatLonGeo to )
    {
        // map our args onto Fortran args
        final double A = datum.getEquatorialRadius( );
        final double F = datum.getFlattening( );
        final double ESQ = datum.getEccentricitySquared( );

        final double P1 = from.getLatRad( );
        final double E1 = from.getLonRad( );
        final double P2 = to.getLatRad( );
        double E2 = to.getLonRad( );

        // outputs get filled in here
        double AZ1;
        double AZ2;
        double S;

        /// specify various tolerance variables
        final double TOL0 = 5e-15;
        final double TOL1 = 5e-14;
        final double TOL2 = 7e-03;

        /// HANDLE EQUAL LONGITUDES
        if ( Math.abs( E2 - E1 ) < TOL1 )
        {
            E2 = E2 + TOL1;
            S = Math.abs( meridionalDistance( datum, from.getLatRad( ), to.getLatRad( ) ) );

            if ( P2 > P1 )
            {
                AZ1 = 0;
                AZ2 = pi;
            }
            else
            {
                AZ1 = pi;
                AZ2 = 0;
            }

            return new DistanceAzimuth( S, Azimuth.fromNavRad( AZ1 ) );
        }

        final double DLON = LatLonGeo.normalizeLon( E2 - E1 );
        double SS = Math.abs( DLON );
        double ALIMIT = pi * ( 1 - F );

        /// TEST FOR ANTI-NODAL DISTANCE
        while ( SS >= ALIMIT )
        {
            final double R1 = Math.abs( P1 );
            final double R2 = Math.abs( P2 );

            if ( R1 > TOL2 && R2 > TOL2 ) break;
            if ( R1 < TOL1 && R2 > TOL2 ) break;
            if ( R2 < TOL1 && R1 > TOL2 ) break;

            if ( R1 > TOL1 || R2 > TOL1 )
            {
                AZ1 = 0;
                AZ2 = 0;
                S = 0;
                return new DistanceAzimuth( S, Azimuth.fromNavRad( AZ1 ) );
            }

            // COMPUTE AZIMUTH TO ANTI-NODAL POINT
            ResultOfInverseLiftOff result = inverseLiftOff( datum, DLON );

            AZ1 = result.AZ1;
            AZ2 = result.AZ2;
            //          double AA   = result.A0;
            //          double BB   = result.B0;
            double SMS = result.SMS;

            // COMPUTE EQUATORIAL DISTANCE & GEODETIC
            double EQU = A * Math.abs( DLON );
            S = EQU - SMS;

            return new DistanceAzimuth( S, Azimuth.fromNavRad( AZ1 ) );
        }

        double F0 = 1 - F;
        double B = A * F0;
        double EPSQ = ESQ / ( 1 - ESQ );
        double F2 = F * F;
        double F3 = F * F2;
        double F4 = F * F3;

        /// LONGITUDE DIFFERENCE
        double AB = E2 - E1;
        int KOUNT = 0;

        /// REDUCED LATITUDES
        double U1 = F0 * Math.sin( P1 ) / Math.cos( P1 );
        double U2 = F0 * Math.sin( P2 ) / Math.cos( P2 );
        U1 = Math.atan( U1 );
        U2 = Math.atan( U2 );
        double SU1 = Math.sin( U1 );
        double CU1 = Math.cos( U1 );
        double SU2 = Math.sin( U2 );
        double CU2 = Math.cos( U2 );

        double XY;
        double CLON, SLON;
        double SIG, SSIG;
        double SINALF;
        double Q2, Q4, Q6;
        double R2, R3;
        double W;
        do
        {
            CLON = Math.cos( AB );
            SLON = Math.sin( AB );

            final double CSIG = SU1 * SU2 + CU1 * CU2 * CLON;
            final double DUM1 = SLON * CU2;
            final double DUM2 = SU2 * CU1 - SU1 * CU2 * CLON;

            SSIG = Math.sqrt( DUM1 * DUM1 + DUM2 * DUM2 );
            SIG = Math.atan2( SSIG, CSIG );
            SINALF = CU1 * CU2 * SLON / SSIG;
            W = ( 1 - SINALF * SINALF );

            final double T4 = W * W;
            final double T6 = W * T4;
            final double A0 = F - F2 * ( 1 + F + F2 ) * W / 4 + 3 * F3 * ( 1 + 9 * F / 4 ) * T4 / 16 - 25 * F4 * T6 / 128;
            final double A2 = F2 * ( 1 + F + F2 ) * W / 4 - F3 * ( 1 + 9 * F / 4 ) * T4 / 4 + 75 * F4 * T6 / 256;
            final double A4 = F3 * ( 1 + 9 * F / 4 ) * T4 / 32 - 15 * F4 * T6 / 256;
            final double A6 = 5 * F4 * T6 / 768;

            double Q0 = 0;
            if ( W > TOL0 ) Q0 = -2 * SU1 * SU2 / W;
            Q2 = CSIG + Q0;
            Q4 = 2 * Q2 * Q2 - 1;
            Q6 = Q2 * ( 4 * Q2 * Q2 - 3 );
            R2 = 2 * SSIG * CSIG;
            R3 = SSIG * ( 3 - 4 * SSIG * SSIG );

            // longitude difference
            S = SINALF * ( A0 * SIG + A2 * SSIG * Q2 + A4 * R2 * Q4 + A6 * R3 * Q6 );
            double XZ = DLON + S;

            XY = Math.abs( XZ - AB );
            AB = DLON + S;
        }
        while ( KOUNT++ <= 7 && XY > 0.5e-13 );

        double Z = EPSQ * W;
        double B0 = 1 + Z * ( 1. / 4 + Z * ( -3. / 64 + Z * ( 5. / 256 - Z * 175. / 16384 ) ) );
        double B2 = Z * ( -1. / 4 + Z * ( 1. / 16 + Z * ( -15. / 512 + Z * 35. / 2048 ) ) );
        double B4 = Z * Z * ( -1. / 128 + Z * ( 3. / 512 - Z * 35. / 8192 ) );
        double B6 = Z * Z * Z * ( -1. / 1536 + Z * 5. / 6144 );

        S = B * ( B0 * SIG + B2 * SSIG * Q2 + B4 * R2 * Q4 + B6 * R3 * Q6 );

        /// AZ1 & AZ2 ALONG EQUATOR
        AZ1 = pi / 2;
        if ( DLON < 0 )
        {
            AZ1 = 3 * AZ1;
        }

        AZ2 = AZ1 + pi;
        if ( AZ2 > twoPi )
        {
            AZ2 = AZ2 - twoPi;
        }

        /// AZ1 & AZ2 NOT ON EQUATOR
        if ( ! ( Math.abs( SU1 ) < TOL0 && Math.abs( SU2 ) < TOL0 ) )
        {
            double tana1 = SLON * CU2 / ( SU2 * CU1 - CLON * SU1 * CU2 );
            double tana2 = SLON * CU1 / ( SU1 * CU2 - CLON * SU2 * CU1 );
            double sina1 = SINALF / CU1;
            double sina2 = -SINALF / CU2;

            /// AZIMUTHS FROM NORTH, LONGITUDES POSITIVE EAST
            AZ1 = Math.atan2( sina1, sina1 / tana1 );
            AZ2 = pi - Math.atan2( sina2, sina2 / tana2 );
        }

        return new DistanceAzimuth( S, Azimuth.fromNavRad( AZ1 ) );
    }

    /**
     * Computes inverse geodetic problem between the lift off point and the antipodal point
     * along the equator.
     */
    private static ResultOfInverseLiftOff inverseLiftOff( Datum datum, double lonDiffRad )
    {
        /// map our args to Fortran args
        final double AMAX = datum.getEquatorialRadius( );
        final double FLAT = datum.getFlattening( );
        final double ESQ = datum.getEccentricitySquared( );
        final double DL = lonDiffRad;

        /// results go here
        double AZ2;
        double AO;
        double BO;
        double SMS;

        /// specify tolerance
        final double TT = 5.0e-13;

        final double DLON = Math.abs( DL );
        final double CONS = ( pi - DLON ) / ( pi * FLAT );
        final double F = FLAT;

        /// COMPUTE AN APPROXIMATE AZ
        double AZ = Math.sin( CONS );

        double T1 = 1.0;
        double T2 = ( -1.0 / 4.0 ) * F * ( 1.0 + F + F * F );
        double T4 = 3.0 / 16.0 * F * F * ( 1.0 + ( 9.0 / 4.0 ) * F );
        double T6 = ( -25.0 / 128.0 ) * F * F * F;

        double S = AZ;
        int ITER = 0;
        do
        {
            ITER++;

            AZ = S;
            S = Math.cos( AZ );
            double C2 = S * S;

            /// COMPUTE NEW AO
            AO = T1 + T2 * C2 + T4 * C2 * C2 + T6 * C2 * C2 * C2;
            double CS = CONS / AO;
            S = Math.sin( CS );
        }
        while ( ITER <= 6 && Math.abs( S - AZ ) > TT );

        double AZ1 = S;
        if ( DL < 0 )
        {
            AZ1 = 2.0 * pi - AZ1;
        }
        AZ2 = 2.0 * pi - AZ1;

        /// EQUATORIAL - GEODESIC  (S - s)   "SMS"
        double ESQP = ESQ / ( 1.0 - ESQ );
        S = Math.cos( AZ1 );

        double U2 = ESQP * S * S;
        double U4 = U2 * U2;
        double U6 = U4 * U2;
        double U8 = U6 * U2;

        T1 = 1.0;
        T2 = ( 1.0 / 4.0 ) * U2;
        T4 = ( -3.0 / 64.0 ) * U4;
        T6 = ( 5.0 / 256.0 ) * U6;
        double T8 = ( -175.0 / 16384.0 ) * U8;

        BO = T1 + T2 + T4 + T6 + T8;
        S = Math.sin( AZ1 );
        SMS = AMAX * pi * ( 1.0 - FLAT * Math.abs( S ) * AO - BO * ( 1.0 - FLAT ) );

        return new ResultOfInverseLiftOff( AZ1, AZ2, SMS, AO, BO );
    }

    private static final class ResultOfInverseLiftOff
    {
        private final double AZ1; // forward azimuth
        private final double AZ2; // backward azimuth
        private final double SMS; // equatorial - geodesic distance
        //        private final double A0;  // constant
        //        private final double B0;  // constant

        private ResultOfInverseLiftOff( double AZ1, double AZ2, double SMS, double A0, double B0 )
        {
            this.AZ1 = AZ1;
            this.AZ2 = AZ2;
            this.SMS = SMS;
            //            this.A0  = A0;
            //            this.B0  = B0;
        }
    }

    public static double meridionalDistance( Datum datum, double startLatRad, double endLatRad )
    {
        /// map our args onto Fortran args
        final double AMAX = datum.getEquatorialRadius( );
        //        final double FLAT = datum.getFlattening( );
        final double ESQ = datum.getEccentricitySquared( );
        final double P1 = startLatRad;
        final double P2 = endLatRad;

        /// specify tolerance parameters
        final double TT = 5.0e-15;

        /// CHECK FOR A 90 DEGREE LOOKUP
        double S1 = Math.abs( P1 );
        double S2 = Math.abs( P2 );
        boolean FLAG = false;
        if ( ( piOverTwo - TT < S2 ) && ( S2 < piOverTwo + TT ) && ( S1 <= TT ) )
        {
            FLAG = true;
        }

        double DA = P2 - P1;
        S1 = 0;
        S2 = 0;

        /// COMPUTE THE LENGTH OF A MERIDIONAL ARC BETWEEN TWO LATITUDES
        double E2 = ESQ;
        double E4 = E2 * E2;
        double E6 = E4 * E2;
        double E8 = E6 * E2;
        double EX = E8 * E2;

        double T1 = E2 * ( 003.0 / 4.0 );
        double T2 = E4 * ( 015.0 / 64.0 );
        double T3 = E6 * ( 035.0 / 512.0 );
        double T4 = E8 * ( 315.0 / 16384.0 );
        double T5 = EX * ( 693.0 / 131072.0 );

        double A = 1.0 + T1 + 3.0 * T2 + 10.0 * T3 + 35.0 * T4 + 126.0 * T5;

        if ( !FLAG )
        {
            double B = T1 + 4.0 * T2 + 15.0 * T3 + 56.0 * T4 + 210.0 * T5;
            double C = T2 + 06.0 * T3 + 28.0 * T4 + 120.0 * T5;
            double D = T3 + 08.0 * T4 + 045.0 * T5;
            double E = T4 + 010.0 * T5;
            double F = T5;

            double DB = Math.sin( P2 * 2.0 ) - Math.sin( P1 * 2.0 );
            double DC = Math.sin( P2 * 4.0 ) - Math.sin( P1 * 4.0 );
            double DD = Math.sin( P2 * 6.0 ) - Math.sin( P1 * 6.0 );
            double DE = Math.sin( P2 * 8.0 ) - Math.sin( P1 * 8.0 );
            double DF = Math.sin( P2 * 10.0 ) - Math.sin( P1 * 10.0 );

            /// COMPUTE THE S2 PART OF THE SERIES EXPANSION
            S2 = -DB * B / 2.0 + DC * C / 4.0 - DD * D / 6.0 + DE * E / 8.0 - DF * F / 10.0;
        }

        /// COMPUTE THE S1 PART OF THE SERIES EXPANSION
        S1 = DA * A;

        // COMPUTE THE ARC LENGTH
        return AMAX * ( 1.0 - ESQ ) * ( S1 + S2 );
    }
}

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
package com.metsci.glimpse.util.units;

import com.metsci.glimpse.util.units.time.Time;

/**
 * @author hogye
 */
public class Speed
{
    // To make conversions as accurate as possible, first define all the
    // factors that can be written exactly as literals, then compute other
    // factors based on the exact ones. Be careful about computing factors
    // based on others that are not literal-exact.
    //
    public static final double knotsToMetersPerSecond = Length.nauticalMilesToMeters / Time.hoursToSeconds;

    public static final double metersPerSecondToKnots = 1.0 / knotsToMetersPerSecond;

    /**
     * Converts speed from meters-per-second to knots.
     */
    public static double metersPerSecondToKnots( double speed_METERS_PER_SECOND )
    {
        return speed_METERS_PER_SECOND * metersPerSecondToKnots;
    }

    /**
     * Converts speed from meters-per-second to knots.
     */
    public static double[] metersPerSecondToKnots( double... speeds_METERS_PER_SECOND )
    {
        return multiply( speeds_METERS_PER_SECOND, metersPerSecondToKnots );
    }

    /**
     * Converts speed from knots to meters-per-second.
     */
    public static double knotsToMetersPerSecond( double speed_KNOTS )
    {
        return speed_KNOTS * knotsToMetersPerSecond;
    }

    /**
     * Converts speed from knots to meters-per-second.
     */
    public static double[] knotsToMetersPerSecond( double... speeds_KNOTS )
    {
        return multiply( speeds_KNOTS, knotsToMetersPerSecond );
    }

    // System units for speed are derived from system units for length and time.
    //
    public static final double metersPerSecondToSu = Length.metersToSu / Time.secondsToSu;
    public static final double knotsToSu = Length.nauticalMilesToSu / Time.hoursToSu;

    public static final double suToMetersPerSecond = 1.0 / metersPerSecondToSu;
    public static final double suToKnots = 1.0 / knotsToSu;

    /**
     * Converts speed from meters-per-second to system-units.
     */
    public static double fromMetersPerSecond( double speed_METERS_PER_SECOND )
    {
        return speed_METERS_PER_SECOND * metersPerSecondToSu;
    }

    /**
     * Converts speed from meters-per-second to system-units.
     */
    public static double[] fromMetersPerSecond( double... speeds_METERS_PER_SECOND )
    {
        return multiply( speeds_METERS_PER_SECOND, metersPerSecondToSu );
    }

    /**
     * Converts speed from system-units to meters-per-second.
     */
    public static double toMetersPerSecond( double speed_SU )
    {
        return speed_SU * suToMetersPerSecond;
    }

    /**
     * Converts speed from system-units to meters-per-second.
     */
    public static double[] toMetersPerSecond( double... speeds_SU )
    {
        return multiply( speeds_SU, suToMetersPerSecond );
    }

    /**
     * Converts speed from knots to system-units.
     */
    public static double fromKnots( double speed_KNOTS )
    {
        return speed_KNOTS * knotsToSu;
    }

    /**
     * Converts speed from knots to system-units.
     */
    public static double[] fromKnots( double... speeds_KNOTS )
    {
        return multiply( speeds_KNOTS, knotsToSu );
    }

    /**
     * Converts speed from system-units to knots.
     */
    public static double toKnots( double speed_SU )
    {
        return speed_SU * suToKnots;
    }

    /**
     * Converts speed from system-units to knots.
     */
    public static double[] toKnots( double... speeds_SU )
    {
        return multiply( speeds_SU, suToKnots );
    }

    private static double[] multiply( double[] array, double factor )
    {
        double[] result = new double[array.length];
        for ( int i = 0; i < result.length; i++ )
            result[i] = factor * array[i];
        return result;
    }

}

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

/**
 * @author hogye
 */
public class Frequency
{
    // To make conversions as accurate as possible, first define all the
    // factors that can be written exactly as literals, then compute other
    // factors based on the exact ones. Be careful about computing factors
    // based on others that are not literal-exact.
    //
    public static final double kilohertzToHertz = 1.0e3;

    public static final double hertzToKilohertz = 1.0 / kilohertzToHertz;

    /**
     * Converts frequency from hertz to kilohertz.
     */
    public static double hertzToKiloherts( double frequency_HERTZ )
    {
        return frequency_HERTZ * hertzToKilohertz;
    }

    /**
     * Converts frequency from hertz to kilohertz.
     */
    public static double[] hertzToKiloherts( double... frequencies_HERTZ )
    {
        return multiply( frequencies_HERTZ, hertzToKilohertz );
    }

    /**
     * Converts frequency from kilohertz to hertz.
     */
    public static double kilohertzToHertz( double frequency_KILOHERTZ )
    {
        return frequency_KILOHERTZ * kilohertzToHertz;
    }

    /**
     * Converts frequency from kilohertz to hertz.
     */
    public static double[] kilohertzToHertz( double... frequencies_KILOHERTZ )
    {
        return multiply( frequencies_KILOHERTZ, kilohertzToHertz );
    }

    // System units for frequency are hertz.
    //
    public static final double hertzToSu = 1.0;
    public static final double kilohertzToSu = kilohertzToHertz;

    public static final double suToHertz = 1.0 / hertzToSu;
    public static final double suToKilohertz = 1.0 / kilohertzToSu;

    /**
     * Converts frequency from hertz to system-units.
     */
    public static double fromHertz( double frequency_HERTZ )
    {
        return frequency_HERTZ * hertzToSu;
    }

    /**
     * Converts frequency from hertz to system-units.
     */
    public static double[] fromHertz( double... frequencies_HERTZ )
    {
        return multiply( frequencies_HERTZ, hertzToSu );
    }

    /**
     * Converts frequency from system-units to hertz.
     */
    public static double toHertz( double frequency_SU )
    {
        return frequency_SU * suToHertz;
    }

    /**
     * Converts frequency from system-units to hertz.
     */
    public static double[] toHertz( double... frequencies_SU )
    {
        return multiply( frequencies_SU, suToHertz );
    }

    /**
     * Converts frequency from kilohertz to system-units.
     */
    public static double fromKilohertz( double frequency_KILOHERTZ )
    {
        return frequency_KILOHERTZ * kilohertzToSu;
    }

    /**
     * Converts frequency from kilohertz to system-units.
     */
    public static double[] fromKilohertz( double... frequencies_KILOHERTZ )
    {
        return multiply( frequencies_KILOHERTZ, kilohertzToSu );
    }

    /**
     * Converts frequency from system-units to kilohertz
     */
    public static double toKilohertz( double frequency_SU )
    {
        return frequency_SU * suToKilohertz;
    }

    /**
     * Converts frequency from system-units to kilohertz
     */
    public static double[] toKilohertz( double... frequencies_SU )
    {
        return multiply( frequencies_SU, suToKilohertz );
    }

    private static double[] multiply( double[] array, double factor )
    {
        double[] result = new double[array.length];
        for ( int i = 0; i < result.length; i++ )
            result[i] = factor * array[i];
        return result;
    }

}

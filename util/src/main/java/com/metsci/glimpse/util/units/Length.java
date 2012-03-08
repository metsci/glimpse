/*
 * Copyright (c) 2012, Metron, Inc.
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
public class Length
{
    // To make conversions as accurate as possible, first define all the
    // factors that can be written exactly as literals, then compute other
    // factors based on the exact ones. Be careful about computing factors
    // based on others that are not literal-exact.
    //
    public static final double nauticalMilesToMeters    = 1852.0;
    public static final double yardsToFeet              = 3.0;
    public static final double feetToMeters             = 0.3048;
    public static final double kiloyardsToYards         = 1.0e3;
    public static final double yardsToMeters            = yardsToFeet * feetToMeters;
    public static final double kiloyardsToMeters        = kiloyardsToYards * yardsToMeters;
    public static final double kiloyardsToFeet          = kiloyardsToYards * yardsToFeet;
    public static final double nauticalMilesToFeet      = nauticalMilesToMeters / feetToMeters;
    public static final double nauticalMilesToYards     = nauticalMilesToMeters / yardsToMeters;
    public static final double nauticalMilesToKiloyards = nauticalMilesToMeters / kiloyardsToMeters;

    public static final double metersToNauticalMiles    = 1.0 / nauticalMilesToMeters;
    public static final double feetToYards              = 1.0 / yardsToFeet;
    public static final double metersToFeet             = 1.0 / feetToMeters;
    public static final double yardsToKiloyards         = 1.0 / kiloyardsToYards;
    public static final double metersToYards            = 1.0 / yardsToMeters;
    public static final double metersToKiloyards        = 1.0 / kiloyardsToMeters;
    public static final double feetToKiloyards          = 1.0 / kiloyardsToFeet;
    public static final double feetToNauticalMiles      = 1.0 / nauticalMilesToFeet;
    public static final double yardsToNauticalMiles     = 1.0 / nauticalMilesToYards;
    public static final double kiloyardsToNauticalMiles = 1.0 / nauticalMilesToKiloyards;


    /**
     * Converts length from meters to nautical-miles.
     */
    public static double metersToNauticalMiles(double length_METERS) { return length_METERS * metersToNauticalMiles; }

    /**
     * Converts length from meters to nautical-miles.
     */
    public static double[] metersToNauticalMiles(double... lengths_METERS) { return multiply(lengths_METERS, metersToNauticalMiles); }

    /**
     * Converts length from nautical-miles to meters.
     */
    public static double nauticalMilesToMeters(double length_NAUTICAL_MILES) { return length_NAUTICAL_MILES * nauticalMilesToMeters; }

    /**
     * Converts length from nautical-miles to meters.
     */
    public static double[] nauticalMilesToMeters(double... lengths_NAUTICAL_MILES) { return multiply(lengths_NAUTICAL_MILES, nauticalMilesToMeters); }

    /**
     * Converts length from meters to feet.
     */
    public static double metersToFeet(double length_METERS) { return length_METERS * metersToFeet; }

    /**
     * Converts length from meters to feet.
     */
    public static double[] metersToFeet(double... lengths_METERS) { return multiply(lengths_METERS, metersToFeet); }

    /**
     * Converts length from feet to meters.
     */
    public static double feetToMeters(double length_FEET) { return length_FEET * feetToMeters; }

    /**
     * Converts length from feet to meters.
     */
    public static double[] feetToMeters(double... lengths_FEET) { return multiply(lengths_FEET, feetToMeters); }

    /**
     * Converts length from meters to yards.
     */
    public static double metersToYards(double length_METERS) { return length_METERS * metersToYards; }

    /**
     * Converts length from meters to yards.
     */
    public static double[] metersToYards(double... lengths_METERS) { return multiply(lengths_METERS, metersToYards); }

    /**
     * Converts length from yards to meters.
     */
    public static double yardsToMeters(double length_YARDS) { return length_YARDS * yardsToMeters; }

    /**
     * Converts length from yards to meters.
     */
    public static double[] yardsToMeters(double... lengths_YARDS) { return multiply(lengths_YARDS, yardsToMeters); }

    /**
     * Converts length from meters to kiloyards.
     */
    public static double metersToKiloyards(double length_METERS) { return length_METERS * metersToKiloyards; }

    /**
     * Converts length from meters to kiloyards.
     */
    public static double[] metersToKiloyards(double... lengths_METERS) { return multiply(lengths_METERS, metersToKiloyards); }

    /**
     * Converts length from kiloyards to meters.
     */
    public static double kiloyardsToMeters(double length_KILOYARDS) { return length_KILOYARDS * kiloyardsToMeters; }

    /**
     * Converts length from kiloyards to meters.
     */
    public static double[] kiloyardsToMeters(double... lengths_KILOYARDS) { return multiply(lengths_KILOYARDS, kiloyardsToMeters); }

    /**
     * Converts length from nautical-miles to feet.
     */
    public static double nauticalMilesToFeet(double length_NAUTICAL_MILES) { return length_NAUTICAL_MILES * nauticalMilesToFeet; }

    /**
     * Converts length from nautical-miles to feet.
     */
    public static double[] nauticalMilesToFeet(double... lengths_NAUTICAL_MILES) { return multiply(lengths_NAUTICAL_MILES, nauticalMilesToFeet); }

    /**
     * Converts length from feet to nautical-miles.
     */
    public static double feetToNauticalMiles(double length_FEET) { return length_FEET * feetToNauticalMiles; }

    /**
     * Converts length from feet to nautical-miles.
     */
    public static double[] feetToNauticalMiles(double... lengths_FEET) { return multiply(lengths_FEET, feetToNauticalMiles); }

    /**
     * Converts length from nautical-miles to yards.
     */
    public static double nauticalMilesToYards(double length_NAUTICAL_MILES) { return length_NAUTICAL_MILES * nauticalMilesToYards; }

    /**
     * Converts length from nautical-miles to yards.
     */
    public static double[] nauticalMilesToYards(double... lengths_NAUTICAL_MILES) { return multiply(lengths_NAUTICAL_MILES, nauticalMilesToYards); }

    /**
     * Converts length from yards to nautical-miles.
     */
    public static double yardsToNauticalMiles(double length_YARDS) { return length_YARDS * yardsToNauticalMiles; }

    /**
     * Converts length from yards to nautical-miles.
     */
    public static double[] yardsToNauticalMiles(double... lengths_YARDS) { return multiply(lengths_YARDS, yardsToNauticalMiles); }

    /**
     * Converts length from nautical-miles to kiloyards.
     */
    public static double nauticalMilesToKiloyards(double length_NAUTICAL_MILES) { return length_NAUTICAL_MILES * nauticalMilesToKiloyards; }

    /**
     * Converts length from nautical-miles to kiloyards.
     */
    public static double[] nauticalMilesToKiloyards(double... lengths_NAUTICAL_MILES) { return multiply(lengths_NAUTICAL_MILES, nauticalMilesToKiloyards); }

    /**
     * Converts length from kiloyards to nautical-miles.
     */
    public static double kiloyardsToNauticalMiles(double length_KILOYARDS) { return length_KILOYARDS * kiloyardsToNauticalMiles; }

    /**
     * Converts length from kiloyards to nautical-miles.
     */
    public static double[] kiloyardsToNauticalMiles(double... lengths_KILOYARDS) { return multiply(lengths_KILOYARDS, kiloyardsToNauticalMiles); }

    /**
     * Converts length from feet to yards.
     */
    public static double feetToYards(double length_FEET) { return length_FEET * feetToYards; }

    /**
     * Converts length from feet to yards.
     */
    public static double[] feetToYards(double... lengths_FEET) { return multiply(lengths_FEET, feetToYards); }

    /**
     * Converts length from yards to feet.
     */
    public static double yardsToFeet(double length_YARDS) { return length_YARDS * yardsToFeet; }

    /**
     * Converts length from yards to feet.
     */
    public static double[] yardsToFeet(double... lengths_YARDS) { return multiply(lengths_YARDS, yardsToFeet); }

    /**
     * Converts length from feet to kiloyards.
     */
    public static double feetToKiloyards(double length_FEET) { return length_FEET * feetToKiloyards; }

    /**
     * Converts length from feet to kiloyards.
     */
    public static double[] feetToKiloyards(double... lengths_FEET) { return multiply(lengths_FEET, feetToKiloyards); }

    /**
     * Converts length from kiloyards to feet.
     */
    public static double kiloyardsToFeet(double length_KILOYARDS) { return length_KILOYARDS * kiloyardsToFeet; }

    /**
     * Converts length from kiloyards to feet.
     */
    public static double[] kiloyardsToFeet(double... lengths_KILOYARDS) { return multiply(lengths_KILOYARDS, kiloyardsToFeet); }

    /**
     * Converts length from yards to kiloyards.
     */
    public static double yardsToKiloyards(double length_YARDS) { return length_YARDS * yardsToKiloyards; }

    /**
     * Converts length from yards to kiloyards.
     */
    public static double[] yardsToKiloyards(double... lengths_YARDS) { return multiply(lengths_YARDS, yardsToKiloyards); }

    /**
     * Converts length from kiloyards to yards.
     */
    public static double kiloyardsToYards(double length_KILOYARDS) { return length_KILOYARDS * kiloyardsToYards; }

    /**
     * Converts length from kiloyards to yards.
     */
    public static double[] kiloyardsToYards(double... lengths_KILOYARDS) { return multiply(lengths_KILOYARDS, kiloyardsToYards); }


    // System units for length are meters.
    //
    public static final double metersToSu        = 1.0;
    public static final double nauticalMilesToSu = nauticalMilesToMeters;
    public static final double feetToSu          = feetToMeters;
    public static final double yardsToSu         = yardsToMeters;
    public static final double kiloyardsToSu     = kiloyardsToMeters;

    public static final double suToMeters        = 1.0 / metersToSu;
    public static final double suToNauticalMiles = 1.0 / nauticalMilesToSu;
    public static final double suToFeet          = 1.0 / feetToSu;
    public static final double suToYards         = 1.0 / yardsToSu;
    public static final double suToKiloyards     = 1.0 / kiloyardsToSu;


    /**
     * Converts length meters to system-units.
     */
    public static double fromMeters(double length_METERS) { return length_METERS * metersToSu; }

    /**
     * Converts length meters to system-units.
     */
    public static double[] fromMeters(double... lengths_METERS) { return multiply(lengths_METERS, metersToSu); }

    /**
     * Converts length from system-units to meters.
     */
    public static double toMeters(double length_SU) { return length_SU * suToMeters; }

    /**
     * Converts length from system-units to meters.
     */
    public static double[] toMeters(double... lengths_SU) { return multiply(lengths_SU, suToMeters); }

    /**
     * Converts length from nautical-miles to system-units.
     */
    public static double fromNauticalMiles(double length_NAUTICAL_MILES) { return length_NAUTICAL_MILES * nauticalMilesToSu; }

    /**
     * Converts length from nautical-miles to system-units.
     */
    public static double[] fromNauticalMiles(double... lengths_NAUTICAL_MILES) { return multiply(lengths_NAUTICAL_MILES, nauticalMilesToSu); }

    /**
     * Converts length from system-units to nautical-miles.
     */
    public static double toNauticalMiles(double length_SU) { return length_SU * suToNauticalMiles; }

    /**
     * Converts length from system-units to nautical-miles.
     */
    public static double[] toNauticalMiles(double... lengths_SU) { return multiply(lengths_SU, suToNauticalMiles); }

    /**
     * Converts length from feet to system-units.
     */
    public static double fromFeet(double length_FEET) { return length_FEET * feetToSu; }

    /**
     * Converts length from feet to system-units.
     */
    public static double[] fromFeet(double... lengths_FEET) { return multiply(lengths_FEET, feetToSu); }

    /**
     * Converts length from system-units to feet.
     */
    public static double toFeet(double length_SU) { return length_SU * suToFeet; }

    /**
     * Converts length from system-units to feet.
     */
    public static double[] toFeet(double... lengths_SU) { return multiply(lengths_SU, suToFeet); }

    /**
     * Converts length from yards to system-units.
     */
    public static double fromYards(double length_YARDS) { return length_YARDS * yardsToSu; }

    /**
     * Converts length from yards to system-units.
     */
    public static double[] fromYards(double... lengths_YARDS) { return multiply(lengths_YARDS, yardsToSu); }

    /**
     * Converts length from system-units to yards.
     */
    public static double toYards(double length_SU) { return length_SU * suToYards; }

    /**
     * Converts length from system-units to yards.
     */
    public static double[] toYards(double... lengths_SU) { return multiply(lengths_SU, suToYards); }

    /**
     * Converts length from kiloyards to system-units.
     */
    public static double fromKiloyards(double length_KILOYARDS) { return length_KILOYARDS * kiloyardsToSu; }

    /**
     * Converts length from kiloyards to system-units.
     */
    public static double[] fromKiloyards(double... lengths_KILOYARDS) { return multiply(lengths_KILOYARDS, kiloyardsToSu); }

    /**
     * Converts length from system-units to kiloyards.
     */
    public static double toKiloyards(double length_SU) { return length_SU * suToKiloyards; }

    /**
     * Converts length from system-units to kiloyards.
     */
    public static double[] toKiloyards(double... lengths_SU) { return multiply(lengths_SU, suToKiloyards); }


    private static double[] multiply(double[] array, double factor)
    {
        double[] result = new double[array.length];
        for (int i = 0; i < result.length; i++) result[i] = factor * array[i];
        return result;
    }

}

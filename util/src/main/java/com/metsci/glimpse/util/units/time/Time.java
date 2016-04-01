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
package com.metsci.glimpse.util.units.time;

/**
 * @author hogye
 */
public class Time
{
    // To make conversions as accurate as possible, first define all the
    // factors that can be written exactly as literals, then compute other
    // factors based on the exact ones. Be careful about computing factors
    // based on others that are not literal-exact.
    //
    public static final double weeksToDays = 7.0;
    public static final double daysToHours = 24.0;
    public static final double hoursToMinutes = 60.0;
    public static final double minutesToSeconds = 60.0;
    public static final double secondsToMilliseconds = 1.0e3;
    public static final double millisecondsToMicroseconds = 1.0e3;

    public static final double weeksToHours = weeksToDays * daysToHours;
    public static final double weeksToMinutes = weeksToHours * hoursToMinutes;
    public static final double weeksToSeconds = weeksToMinutes * minutesToSeconds;
    public static final double weeksToMilliseconds = weeksToSeconds * secondsToMilliseconds;
    public static final double daysToMinutes = daysToHours * hoursToMinutes;
    public static final double daysToSeconds = daysToMinutes * minutesToSeconds;
    public static final double daysToMilliseconds = daysToSeconds * secondsToMilliseconds;
    public static final double hoursToSeconds = hoursToMinutes * minutesToSeconds;
    public static final double hoursToMilliseconds = hoursToSeconds * secondsToMilliseconds;
    public static final double minutesToMilliseconds = minutesToSeconds * secondsToMilliseconds;
    public static final double secondsToMicroseconds = secondsToMilliseconds * millisecondsToMicroseconds;

    public static final double daysToWeeks = 1.0 / weeksToDays;
    public static final double hoursToWeeks = 1.0 / weeksToHours;
    public static final double minutesToWeeks = 1.0 / weeksToMinutes;
    public static final double secondsToWeeks = 1.0 / weeksToSeconds;
    public static final double millisecondsToWeeks = 1.0 / weeksToMilliseconds;
    public static final double hoursToDays = 1.0 / daysToHours;
    public static final double minutesToDays = 1.0 / daysToMinutes;
    public static final double secondsToDays = 1.0 / daysToSeconds;
    public static final double millisecondsToDays = 1.0 / daysToMilliseconds;
    public static final double minutesToHours = 1.0 / hoursToMinutes;
    public static final double secondsToHours = 1.0 / hoursToSeconds;
    public static final double millisecondsToHours = 1.0 / hoursToMilliseconds;
    public static final double secondsToMinutes = 1.0 / minutesToSeconds;
    public static final double millisecondsToMinutes = 1.0 / minutesToMilliseconds;
    public static final double millisecondsToSeconds = 1.0 / secondsToMilliseconds;
    public static final double microsecondsToSeconds = 1.0 / secondsToMicroseconds;
    public static final double microsecondsToMilliseconds = 1.0 / millisecondsToMicroseconds;

    /**
     * Converts duration from days to weeks.
     */
    public static double daysToWeeks( double duration_DAYS )
    {
        return duration_DAYS * daysToWeeks;
    }

    /**
     * Converts duration from days to weeks.
     */
    public static double[] daysToWeeks( double... durations_DAYS )
    {
        return multiply( durations_DAYS, daysToWeeks );
    }

    /**
     * Converts duration from weeks to days.
     */
    public static double weeksToDays( double duration_WEEKS )
    {
        return duration_WEEKS * weeksToDays;
    }

    /**
     * Converts duration from weeks to days.
     */
    public static double[] weeksToDays( double... durations_WEEKS )
    {
        return multiply( durations_WEEKS, weeksToDays );
    }

    /**
     * Converts duration from hours to weeks.
     */
    public static double hoursToWeeks( double duration_HOURS )
    {
        return duration_HOURS * hoursToWeeks;
    }

    /**
     * Converts duration from hours to weeks.
     */
    public static double[] hoursToWeeks( double... durations_HOURS )
    {
        return multiply( durations_HOURS, hoursToWeeks );
    }

    /**
     * Converts duration from weeks to hours.
     */
    public static double weeksToHours( double duration_WEEKS )
    {
        return duration_WEEKS * weeksToHours;
    }

    /**
     * Converts duration from weeks to hours.
     */
    public static double[] weeksToHours( double... durations_WEEKS )
    {
        return multiply( durations_WEEKS, weeksToHours );
    }

    /**
     * Converts duration from minutes to weeks.
     */
    public static double minutesToWeeks( double duration_MINUTES )
    {
        return duration_MINUTES * minutesToWeeks;
    }

    /**
     * Converts duration from minutes to weeks.
     */
    public static double[] minutesToWeeks( double... durations_MINUTES )
    {
        return multiply( durations_MINUTES, minutesToWeeks );
    }

    /**
     * Converts duration from weeks to minutes.
     */
    public static double weeksToMinutes( double duration_WEEKS )
    {
        return duration_WEEKS * weeksToMinutes;
    }

    /**
     * Converts duration from weeks to minutes.
     */
    public static double[] weeksToMinutes( double... durations_WEEKS )
    {
        return multiply( durations_WEEKS, weeksToMinutes );
    }

    /**
     * Converts duration from seconds to weeks.
     */
    public static double secondsToWeeks( double duration_SECONDS )
    {
        return duration_SECONDS * secondsToWeeks;
    }

    /**
     * Converts duration from seconds to weeks.
     */
    public static double[] secondsToWeeks( double... durations_SECONDS )
    {
        return multiply( durations_SECONDS, secondsToWeeks );
    }

    /**
     * Converts duration from weeks to seconds.
     */
    public static double weeksToSeconds( double duration_WEEKS )
    {
        return duration_WEEKS * weeksToSeconds;
    }

    /**
     * Converts duration from weeks to seconds.
     */
    public static double[] weeksToSeconds( double... durations_WEEKS )
    {
        return multiply( durations_WEEKS, weeksToSeconds );
    }

    /**
     * Converts duration from milliseconds to weeks.
     */
    public static double millisecondsToWeeks( double duration_MILLISECONDS )
    {
        return duration_MILLISECONDS * millisecondsToWeeks;
    }

    /**
     * Converts duration from milliseconds to weeks.
     */
    public static double[] millisecondsToWeeks( double... durations_MILLISECONDS )
    {
        return multiply( durations_MILLISECONDS, millisecondsToWeeks );
    }

    /**
     * Converts duration from weeks to milliseconds.
     */
    public static double weeksToMilliseconds( double duration_WEEKS )
    {
        return duration_WEEKS * weeksToMilliseconds;
    }

    /**
     * Converts duration from weeks to milliseconds.
     */
    public static double[] weeksToMilliseconds( double... durations_WEEKS )
    {
        return multiply( durations_WEEKS, weeksToMilliseconds );
    }

    /**
     * Converts duration from hours to days.
     */
    public static double hoursToDays( double duration_HOURS )
    {
        return duration_HOURS * hoursToDays;
    }

    /**
     * Converts duration from hours to days.
     */
    public static double[] hoursToDays( double... durations_HOURS )
    {
        return multiply( durations_HOURS, hoursToDays );
    }

    /**
     * Converts duration from days to hours.
     */
    public static double daysToHours( double duration_DAYS )
    {
        return duration_DAYS * daysToHours;
    }

    /**
     * Converts duration from days to hours.
     */
    public static double[] daysToHours( double... durations_DAYS )
    {
        return multiply( durations_DAYS, daysToHours );
    }

    /**
     * Converts duration from minutes to days.
     */
    public static double minutesToDays( double duration_MINUTES )
    {
        return duration_MINUTES * minutesToDays;
    }

    /**
     * Converts duration from minutes to days.
     */
    public static double[] minutesToDays( double... durations_MINUTES )
    {
        return multiply( durations_MINUTES, minutesToDays );
    }

    /**
     * Converts duration from days to minutes.
     */
    public static double daysToMinutes( double duration_DAYS )
    {
        return duration_DAYS * daysToMinutes;
    }

    /**
     * Converts duration from days to minutes.
     */
    public static double[] daysToMinutes( double... durations_DAYS )
    {
        return multiply( durations_DAYS, daysToMinutes );
    }

    /**
     * Converts duration from seconds to days.
     */
    public static double secondsToDays( double duration_SECONDS )
    {
        return duration_SECONDS * secondsToDays;
    }

    /**
     * Converts duration from seconds to days.
     */
    public static double[] secondsToDays( double... durations_SECONDS )
    {
        return multiply( durations_SECONDS, secondsToDays );
    }

    /**
     * Converts duration from days to seconds.
     */
    public static double daysToSeconds( double duration_DAYS )
    {
        return duration_DAYS * daysToSeconds;
    }

    /**
     * Converts duration from days to seconds.
     */
    public static double[] daysToSeconds( double... durations_DAYS )
    {
        return multiply( durations_DAYS, daysToSeconds );
    }

    /**
     * Converts duration from milliseconds to days.
     */
    public static double millisecondsToDays( double duration_MILLISECONDS )
    {
        return duration_MILLISECONDS * millisecondsToDays;
    }

    /**
     * Converts duration from milliseconds to days.
     */
    public static double[] millisecondsToDays( double... durations_MILLISECONDS )
    {
        return multiply( durations_MILLISECONDS, millisecondsToDays );
    }

    /**
     * Converts duration from days to milliseconds.
     */
    public static double daysToMilliseconds( double duration_DAYS )
    {
        return duration_DAYS * daysToMilliseconds;
    }

    /**
     * Converts duration from days to milliseconds.
     */
    public static double[] daysToMilliseconds( double... durations_DAYS )
    {
        return multiply( durations_DAYS, daysToMilliseconds );
    }

    /**
     * Converts duration from seconds to hours.
     */
    public static double secondsToHours( double duration_SECONDS )
    {
        return duration_SECONDS * secondsToHours;
    }

    /**
     * Converts duration from seconds to hours.
     */
    public static double[] secondsToHours( double... durations_SECONDS )
    {
        return multiply( durations_SECONDS, secondsToHours );
    }

    /**
     * Converts duration from hours to seconds.
     */
    public static double hoursToSeconds( double duration_HOURS )
    {
        return duration_HOURS * hoursToSeconds;
    }

    /**
     * Converts duration from hours to seconds.
     */
    public static double[] hoursToSeconds( double... durations_HOURS )
    {
        return multiply( durations_HOURS, hoursToSeconds );
    }

    /**
     * Converts duration from seconds to minutes.
     */
    public static double secondsToMinutes( double duration_SECONDS )
    {
        return duration_SECONDS * secondsToMinutes;
    }

    /**
     * Converts duration from seconds to minutes.
     */
    public static double[] secondsToMinutes( double... durations_SECONDS )
    {
        return multiply( durations_SECONDS, secondsToMinutes );
    }

    /**
     * Converts duration from minutes to seconds.
     */
    public static double minutesToSeconds( double duration_MINUTES )
    {
        return duration_MINUTES * minutesToSeconds;
    }

    /**
     * Converts duration from minutes to seconds.
     */
    public static double[] minutesToSeconds( double... durations_MINUTES )
    {
        return multiply( durations_MINUTES, minutesToSeconds );
    }

    /**
     * Converts duration from seconds to milliseconds.
     */
    public static double secondsToMilliseconds( double duration_SECONDS )
    {
        return duration_SECONDS * secondsToMilliseconds;
    }

    /**
     * Converts duration from seconds to milliseconds.
     */
    public static double[] secondsToMilliseconds( double... durations_SECONDS )
    {
        return multiply( durations_SECONDS, secondsToMilliseconds );
    }

    /**
     * Converts duration from milliseconds to seconds.
     */
    public static double millisecondsToSeconds( double duration_MILLISECONDS )
    {
        return duration_MILLISECONDS * millisecondsToSeconds;
    }

    /**
     * Converts duration from milliseconds to seconds.
     */
    public static double[] millisecondsToSeconds( double... durations_MILLISECONDS )
    {
        return multiply( durations_MILLISECONDS, millisecondsToSeconds );
    }

    /**
     * Converts duration from seconds to microseconds.
     */
    public static double secondsToMicroseconds( double duration_SECONDS )
    {
        return duration_SECONDS * secondsToMicroseconds;
    }

    /**
     * Converts duration from seconds to microseconds.
     */
    public static double[] secondsToMicroseconds( double... durations_SECONDS )
    {
        return multiply( durations_SECONDS, secondsToMicroseconds );
    }

    /**
     * Converts duration from microseconds to seconds.
     */
    public static double microsecondsToSeconds( double duration_MICROSECONDS )
    {
        return duration_MICROSECONDS * microsecondsToSeconds;
    }

    /**
     * Converts duration from microseconds to seconds.
     */
    public static double[] microsecondsToSeconds( double... durations_MICROSECONDS )
    {
        return multiply( durations_MICROSECONDS, microsecondsToSeconds );
    }

    /**
     * Converts duration from minutes to hours.
     */
    public static double minutesToHours( double duration_MINUTES )
    {
        return duration_MINUTES * minutesToHours;
    }

    /**
     * Converts duration from minutes to hours.
     */
    public static double[] minutesToHours( double... durations_MINUTES )
    {
        return multiply( durations_MINUTES, minutesToHours );
    }

    /**
     * Converts duration from hours to minutes.
     */
    public static double hoursToMinutes( double duration_HOURS )
    {
        return duration_HOURS * hoursToMinutes;
    }

    /**
     * Converts duration from hours to minutes.
     */
    public static double[] hoursToMinutes( double... durations_HOURS )
    {
        return multiply( durations_HOURS, hoursToMinutes );
    }

    /**
     * Converts duration from minutes to milliseconds.
     */
    public static double minutesToMilliseconds( double duration_MINUTES )
    {
        return duration_MINUTES * minutesToMilliseconds;
    }

    /**
     * Converts duration from minutes to milliseconds.
     */
    public static double[] minutesToMilliseconds( double... durations_MINUTES )
    {
        return multiply( durations_MINUTES, minutesToMilliseconds );
    }

    /**
     * Converts duration from milliseconds to minutes.
     */
    public static double millisecondsToMinutes( double duration_MILLISECONDS )
    {
        return duration_MILLISECONDS * millisecondsToMinutes;
    }

    /**
     * Converts duration from milliseconds to minutes.
     */
    public static double[] millisecondsToMinutes( double... durations_MILLISECONDS )
    {
        return multiply( durations_MILLISECONDS, millisecondsToMinutes );
    }

    /**
     * Converts duration from hours to milliseconds.
     */
    public static double hoursToMilliseconds( double duration_HOURS )
    {
        return duration_HOURS * hoursToMilliseconds;
    }

    /**
     * Converts duration from hours to milliseconds.
     */
    public static double[] hoursToMilliseconds( double... durations_HOURS )
    {
        return multiply( durations_HOURS, hoursToMilliseconds );
    }

    /**
     * Converts duration from milliseconds to hours.
     */
    public static double millisecondsToHours( double duration_MILLISECONDS )
    {
        return duration_MILLISECONDS * millisecondsToHours;
    }

    /**
     * Converts duration from milliseconds to hours.
     */
    public static double[] millisecondsToHours( double... durations_MILLISECONDS )
    {
        return multiply( durations_MILLISECONDS, millisecondsToHours );
    }

    // System units for time are seconds.
    //
    public static final double weeksToSu = weeksToSeconds;
    public static final double daysToSu = daysToSeconds;
    public static final double hoursToSu = hoursToSeconds;
    public static final double minutesToSu = minutesToSeconds;
    public static final double secondsToSu = 1.0;
    public static final double millisecondsToSu = millisecondsToSeconds;
    public static final double microsecondsToSu = microsecondsToSeconds;

    public static final double suToWeeks = 1.0 / weeksToSu;
    public static final double suToDays = 1.0 / daysToSu;
    public static final double suToHours = 1.0 / hoursToSu;
    public static final double suToMinutes = 1.0 / minutesToSu;
    public static final double suToSeconds = 1.0 / secondsToSu;
    public static final double suToMilliseconds = 1.0 / millisecondsToSu;
    public static final double suToMicroseconds = 1.0 / microsecondsToSu;

    /**
     * Converts duration from weeks to system-units.
     */
    public static double fromWeeks( double duration_WEEKS )
    {
        return duration_WEEKS * weeksToSu;
    }

    /**
     * Converts duration from weeks to system-units.
     */
    public static double[] fromWeeks( double... durations_WEEKS )
    {
        return multiply( durations_WEEKS, weeksToSu );
    }

    /**
     * Converts duration from system-units to weeks.
     */
    public static double toWeeks( double duration_SU )
    {
        return duration_SU * suToWeeks;
    }

    /**
     * Converts duration from system-units to weeks.
     */
    public static double[] toWeeks( double... durations_SU )
    {
        return multiply( durations_SU, suToWeeks );
    }

    /**
     * Converts duration from days to system-units.
     */
    public static double fromDays( double duration_DAYS )
    {
        return duration_DAYS * daysToSu;
    }

    /**
     * Converts duration from days to system-units.
     */
    public static double[] fromDays( double... durations_DAYS )
    {
        return multiply( durations_DAYS, daysToSu );
    }

    /**
     * Converts duration from system-units to days.
     */
    public static double toDays( double duration_SU )
    {
        return duration_SU * suToDays;
    }

    /**
     * Converts duration from system-units to days.
     */
    public static double[] toDays( double... durations_SU )
    {
        return multiply( durations_SU, suToDays );
    }

    /**
     * Converts duration from hours to system-units.
     */
    public static double fromHours( double duration_HOURS )
    {
        return duration_HOURS * hoursToSu;
    }

    /**
     * Converts duration from hours to system-units.
     */
    public static double[] fromHours( double... durations_HOURS )
    {
        return multiply( durations_HOURS, hoursToSu );
    }

    /**
     * Converts duration from system-units to hours.
     */
    public static double toHours( double duration_SU )
    {
        return duration_SU * suToHours;
    }

    /**
     * Converts duration from system-units to hours.
     */
    public static double[] toHours( double... durations_SU )
    {
        return multiply( durations_SU, suToHours );
    }

    /**
     * Converts duration from minutes to system-units.
     */
    public static double fromMinutes( double duration_MINUTES )
    {
        return duration_MINUTES * minutesToSu;
    }

    /**
     * Converts duration from minutes to system-units.
     */
    public static double[] fromMinutes( double... durations_MINUTES )
    {
        return multiply( durations_MINUTES, minutesToSu );
    }

    /**
     * Converts duration from system-units to minutes.
     */
    public static double toMinutes( double duration_SU )
    {
        return duration_SU * suToMinutes;
    }

    /**
     * Converts duration from system-units to minutes.
     */
    public static double[] toMinutes( double... durations_SU )
    {
        return multiply( durations_SU, suToMinutes );
    }

    /**
     * Converts duration from seconds to system-units.
     */
    public static double fromSeconds( double duration_SECONDS )
    {
        return duration_SECONDS * secondsToSu;
    }

    /**
     * Converts duration from seconds to system-units.
     */
    public static double[] fromSeconds( double... durations_SECONDS )
    {
        return multiply( durations_SECONDS, secondsToSu );
    }

    /**
     * Converts duration from system-units to seconds.
     */
    public static double toSeconds( double duration_SU )
    {
        return duration_SU * suToSeconds;
    }

    /**
     * Converts duration from system-units to seconds.
     */
    public static double[] toSeconds( double... durations_SU )
    {
        return multiply( durations_SU, suToSeconds );
    }

    /**
     * Converts duration from milliseconds to system-units.
     */
    public static double fromMilliseconds( double duration_MILLISECONDS )
    {
        return duration_MILLISECONDS * millisecondsToSu;
    }

    /**
     * Converts duration from milliseconds to system-units.
     */
    public static double[] fromMilliseconds( double... durations_MILLISECONDS )
    {
        return multiply( durations_MILLISECONDS, millisecondsToSu );
    }

    /**
     * Converts duration from system-units to milliseconds.
     */
    public static double toMilliseconds( double duration_SU )
    {
        return duration_SU * suToMilliseconds;
    }

    /**
     * Converts duration from system-units to milliseconds.
     */
    public static double[] toMilliseconds( double... durations_SU )
    {
        return multiply( durations_SU, suToMilliseconds );
    }

    /**
     * Converts duration from system-units to microseconds.
     */
    public static double toMicroseconds( double duration_SU )
    {
        return duration_SU * suToMicroseconds;
    }

    /**
     * Converts duration from system-units to microseconds.
     */
    public static double[] toMicroseconds( double... durations_SU )
    {
        return multiply( durations_SU, suToMicroseconds );
    }

    /**
     * Converts duration from microseconds to system-units.
     */
    public static double fromMicroseconds( double duration_MICROSECONDS )
    {
        return duration_MICROSECONDS * microsecondsToSu;
    }

    /**
     * Converts duration from microseconds to system-units.
     */
    public static double[] fromMicroseconds( double... durations_MICROSECONDS )
    {
        return multiply( durations_MICROSECONDS, microsecondsToSu );
    }

    private static double[] multiply( double[] array, double factor )
    {
        double[] result = new double[array.length];
        for ( int i = 0; i < result.length; i++ )
            result[i] = factor * array[i];
        return result;
    }

}

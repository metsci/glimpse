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
package com.metsci.glimpse.axis.painter.label;

import com.metsci.glimpse.util.units.Angle;
import com.metsci.glimpse.util.units.Length;
import com.metsci.glimpse.util.units.Speed;
import com.metsci.glimpse.util.units.time.Time;

public class AxisUnitConverters
{

    // General

    public static final AxisUnitConverter identity = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return value;
        }

        public double toAxisUnits( double value )
        {
            return value;
        }
    };

    public static AxisUnitConverter negated( final AxisUnitConverter c )
    {
        return new AxisUnitConverter( )
        {
            public double fromAxisUnits( double value )
            {
                return c.fromAxisUnits( -value );
            }

            public double toAxisUnits( double value )
            {
                return -c.toAxisUnits( value );
            }
        };
    }

    public static final AxisUnitConverter fractionShownAsPercentage = new AxisUnitConverter( )
    {
        public double toAxisUnits( double value )
        {
            return ( 100 * value );
        }

        public double fromAxisUnits( double value )
        {
            return ( 0.01 * value );
        }
    };

    // Length

    public static final AxisUnitConverter suShownAsMeters = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Length.fromMeters( value );
        }

        public double toAxisUnits( double value )
        {
            return Length.toMeters( value );
        }
    };

    public static final AxisUnitConverter suShownAsNauticalMiles = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Length.fromNauticalMiles( value );
        }

        public double toAxisUnits( double value )
        {
            return Length.toNauticalMiles( value );
        }
    };

    public static final AxisUnitConverter suShownAsFeet = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Length.fromFeet( value );
        }

        public double toAxisUnits( double value )
        {
            return Length.toFeet( value );
        }
    };

    public static final AxisUnitConverter suShownAsYards = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Length.fromYards( value );
        }

        public double toAxisUnits( double value )
        {
            return Length.toYards( value );
        }
    };

    public static final AxisUnitConverter suShownAsKiloyards = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Length.fromKiloyards( value );
        }

        public double toAxisUnits( double value )
        {
            return Length.toKiloyards( value );
        }
    };

    // Speed

    public static final AxisUnitConverter suShownAsMetersPerSecond = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Speed.fromMetersPerSecond( value );
        }

        public double toAxisUnits( double value )
        {
            return Speed.toMetersPerSecond( value );
        }
    };

    public static final AxisUnitConverter suShownAsKnots = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Speed.fromKnots( value );
        }

        public double toAxisUnits( double value )
        {
            return Speed.toKnots( value );
        }
    };

    // Time

    public static final AxisUnitConverter suShownAsMicroseconds = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Time.fromMicroseconds( value );
        }

        public double toAxisUnits( double value )
        {
            return Time.toMicroseconds( value );
        }
    };

    public static final AxisUnitConverter suShownAsMilliseconds = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Time.fromMilliseconds( value );
        }

        public double toAxisUnits( double value )
        {
            return Time.toMilliseconds( value );
        }
    };

    public static final AxisUnitConverter suShownAsSeconds = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Time.fromSeconds( value );
        }

        public double toAxisUnits( double value )
        {
            return Time.toSeconds( value );
        }
    };

    public static final AxisUnitConverter suShownAsMinutes = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Time.fromMinutes( value );
        }

        public double toAxisUnits( double value )
        {
            return Time.toMinutes( value );
        }
    };

    public static final AxisUnitConverter suShownAsHours = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Time.fromHours( value );
        }

        public double toAxisUnits( double value )
        {
            return Time.toHours( value );
        }
    };

    public static final AxisUnitConverter suShownAsDays = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Time.fromDays( value );
        }

        public double toAxisUnits( double value )
        {
            return Time.toDays( value );
        }
    };

    public static final AxisUnitConverter suShownAsWeeks = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Time.fromWeeks( value );
        }

        public double toAxisUnits( double value )
        {
            return Time.toWeeks( value );
        }
    };

    // Angle

    public static final AxisUnitConverter suShownAsDegrees = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            return Angle.toDeg( value );
        }

        public double toAxisUnits( double value )
        {
            return Angle.fromDeg( value );
        }
    };

    public static final AxisUnitConverter suShownAsNavigationDegrees = new AxisUnitConverter( )
    {
        public double fromAxisUnits( double value )
        {
            double ans = 90 - Angle.toDeg( value );
            if ( ans <= 0 ) ans += 360;
            return ans;
        }

        public double toAxisUnits( double value )
        {
            return Angle.fromDeg( value );
        }
    };

}

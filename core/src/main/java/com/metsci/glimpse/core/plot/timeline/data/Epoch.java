/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.core.plot.timeline.data;

import static com.metsci.glimpse.util.units.time.Time.*;
import static java.lang.Math.*;

import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * A class encapsulating the conversion between absolute time specified
 * as a TimeStamp and values on an Axis1D representing time.<p>
 *
 * Values on an Axis1D representing time are usually seconds since
 * an epoch chosen to be close to the times of interest (in order to
 * avoid numerical precision issues, particularly with OpenGL painters
 * which deal with axis values as floats ).<p>
 *
 * Times Axis1D timeline are stored as seconds.
 *
 * @author ulman
 */
public class Epoch
{
    protected final TimeStamp epoch;
    protected final long epoch_PMILLIS;

    public static Epoch posixEpoch( )
    {
        return new Epoch( TimeStamp.posixEpoch( ) );
    }

    public static Epoch currentTime( )
    {
        return new Epoch( TimeStamp.currentTime( ) );
    }

    public Epoch( TimeStamp epoch )
    {
        this( epoch.toPosixMillis( ) );
    }

    public Epoch( long epoch_PMILLIS )
    {
        this.epoch_PMILLIS = epoch_PMILLIS;
        this.epoch = TimeStamp.fromPosixMillis( epoch_PMILLIS );
    }

    public TimeStamp getTimeStamp( )
    {
        return this.epoch;
    }

    public long getPosixMillis( )
    {
        return this.epoch_PMILLIS;
    }

    /**
     * Converts a value along an Axis1D into an absolute time by interpreting
     * the values along the Axis1D as offsets in seconds from the point in
     * time represented by this Epoch.
     *
     * @param axisValue a value returned by an Axis1D
     * @return a TimeStamp representing an absolute time
     * @see #fromTimeStamp(TimeStamp)
     */
    public TimeStamp toTimeStamp( double axisValue )
    {
        return TimeStamp.fromPosixMillis( this.toPosixMillis( axisValue ) );
    }

    /**
     * Converts a value along an Axis1D into an absolute time by interpreting
     * the values along the Axis1D as offsets in seconds from the point in
     * time represented by this Epoch.
     *
     * @param axisValue a value returned by an Axis1D
     * @return a long representing an absolute time, in milliseconds since the posix epoch
     * @see #fromPosixMillis(long)
     */
    public long toPosixMillis( double axisValue )
    {
        return ( this.epoch_PMILLIS + round( secondsToMilliseconds( axisValue ) ) );
    }

    /**
     * Converts a TimeStamp to a value along an Axis1D.
     *
     * @param time an absolute TimeStamp
     * @return an Axis1D value
     * @see #toTimeStamp( double )
     */
    public double fromTimeStamp( TimeStamp time )
    {
        return this.fromPosixMillis( time.toPosixMillis( ) );
    }

    /**
     * Converts a time to a value along an Axis1D.
     *
     * @param time an absolute time, in milliseconds since the posix epoch
     * @return an Axis1D value
     * @see #toPosixMillis(double)
     */
    public double fromPosixMillis( long time_PMILLIS )
    {
        return millisecondsToSeconds( time_PMILLIS - this.epoch_PMILLIS );
    }

    @Override
    public int hashCode( )
    {
        final int prime = 12347;
        int result = 1;
        result = prime * result + Long.hashCode( this.epoch_PMILLIS );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        Epoch other = ( Epoch ) o;
        return ( other.epoch_PMILLIS == this.epoch_PMILLIS );
    }
}

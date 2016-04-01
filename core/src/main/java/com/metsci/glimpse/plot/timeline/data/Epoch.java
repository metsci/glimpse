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
package com.metsci.glimpse.plot.timeline.data;

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
    protected TimeStamp epoch;

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
        this.epoch = epoch;
    }

    public TimeStamp getTimeStamp( )
    {
        return this.epoch;
    }

    /**
     * Converts a value along an Axis1D into an absolute time by interpreting
     * the values along the Axis1D as offsets in seconds from the point in
     * time represented by this Epoch.
     *
     * @param axisValue a value returned by an Axis1D
     * @return a TimeStamp representing an absolute time
     */
    public TimeStamp toTimeStamp( double axisValue )
    {
        return TimeStamp.fromPosixSeconds( axisValue + epoch.toPosixSeconds( ) );
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
        return time.toPosixSeconds( ) - epoch.toPosixSeconds( );
    }
}

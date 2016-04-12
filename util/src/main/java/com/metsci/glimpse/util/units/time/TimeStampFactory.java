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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import com.metsci.glimpse.util.units.time.format.TimeStampFormat;
import com.metsci.glimpse.util.units.time.format.TimeStampParseException;

/**
 * @author hogye
 */
public interface TimeStampFactory<T extends TimeStamp>
{
    /**
     * See {@link TimeStamp#fromPosixSeconds}.
     */
    T fromPosixSeconds( double posixSeconds );

    /**
     * See {@link TimeStamp#fromPosixSeconds}.
     */
    T fromPosixSeconds( BigDecimal posixSeconds );

    /**
     * See {@link TimeStamp#fromPosixMillis}.
     */
    T fromPosixMillis( long posixMillis );

    /**
     * See {@link TimeStamp#fromPosixMicros}.
     */
    T fromPosixMicros( long posixMicros );

    /**
     * See {@link TimeStamp#fromPosixNanos}.
     */
    T fromPosixNanos( long posixNanos );

    /**
     * See {@link TimeStamp#fromTimeStamp}.
     */
    T fromTimeStamp( TimeStamp timeStamp );

    /**
     * See {@link TimeStamp#fromDate}.
     */
    T fromDate( Date date );

    /**
     * See {@link TimeStamp#fromCalendar}.
     */
    T fromCalendar( Calendar calendar );

    /**
     * See {@link TimeStamp#fromString}.
     */
    T fromString( String string, TimeStampFormat format ) throws TimeStampParseException;

    /**
     * See {@link TimeStamp#posixEpoch}.
     */
    T posixEpoch( );

    /**
     * See {@link TimeStamp#currentTime}.
     */
    T currentTime( );

}

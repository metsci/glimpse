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
package com.metsci.glimpse.util.units.time.format;

import java.math.BigDecimal;

public interface TimeStampFormat extends Cloneable
{
    /**
     * The string must be a date-time string in ISO-8601 format, with the following
     * extra restrictions:
     * <ul>
     *   <li>The string <i>must</i> include separators, even though ISO-8601 says
     *       separators may be omitted in some cases.
     *
     *   <li>The string <i>must</i> include precision down to seconds, even though
     *       ISO-8601 says precision may be truncated at any point.
     *
     *   <li>The string <i>must</i> end with a "Z", signifying the UTC timezone,
     *       even though ISO-8601 says the "Z" is optional.
     *
     *   <li>The year must be <i>exactly</i> 4 digits, even though ISO-8601 says
     *       the year may have more than 4 digits.
     *
     *   <li>"Week dates" (that is, week number 1-52) are not supported.
     *
     *   <li>"Ordinal dates" (that is, day of year 1-365) are not supported.
     * </ul>
     * Examples:
     * <ul>
     *   <li>2008-06-11T13:40:12Z
     *   <li>2008-06-11T13:40:12.5432931Z
     * </ul>
     */
    public static final TimeStampFormat iso8601 = new TimeStampFormatStandard( "%y-%M-%dT%H:%m:%SZ", "UTC" );

    /**
     * @return seconds since the epoch
     * @throws TimeStampParseException if the string cannot be parsed according to this format
     */
    BigDecimal parse( String string ) throws TimeStampParseException;

    /**
     * @return a date-time string, formatted according to this format
     */
    String format( BigDecimal posixSeconds );

}

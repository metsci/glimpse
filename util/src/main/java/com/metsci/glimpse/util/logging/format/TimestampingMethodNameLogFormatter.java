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
package com.metsci.glimpse.util.logging.format;

import static com.metsci.glimpse.util.logging.format.TerseLogFormatter.LINE_SEPARATOR;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

/**
 * Extends TerseLogFormatter to include a timestamp before each line.
 *
 * @author hogye
 */
public class TimestampingMethodNameLogFormatter extends TerseMethodNameLogFormatter
{
    private final DateFormat _fullDateTriggerFormatter = new SimpleDateFormat( "yyyy-MM-dd z" );
    private final DateFormat _fullFormatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" );
    private final DateFormat _shortFormatter = new SimpleDateFormat( "HH:mm:ss" );

    private String _previousDateString = null;

    @Override
    public String format( LogRecord record )
    {
        Date now = new Date( record.getMillis( ) );
        String dateString = getFullDateTriggerFormatter( ).format( now );
        if ( ( _previousDateString == null ) || !_previousDateString.equals( dateString ) )
        {
            _previousDateString = dateString;
            StringBuilder stringBuilder = new StringBuilder( getFullFormatter( ).format( now ) );
            stringBuilder.append( LINE_SEPARATOR );
            stringBuilder.append( super.format( record ) );
            return stringBuilder.toString( );
        }
        else
        {
            return super.format( record );
        }
    }

    @Override
    protected void appendPrefix( LogRecord record, StringBuilder prefix )
    {
        Date now = new Date( record.getMillis( ) );
        prefix.append( getShortFormatter( ).format( now ) );
        prefix.append( " " );
        super.appendPrefix( record, prefix );
    }

    protected DateFormat getFullDateTriggerFormatter( )
    {
        return _fullDateTriggerFormatter;
    }

    protected DateFormat getFullFormatter( )
    {
        return _fullFormatter;
    }

    protected DateFormat getShortFormatter( )
    {
        return _shortFormatter;
    }

}

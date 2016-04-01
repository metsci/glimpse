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

import static com.metsci.glimpse.util.logging.format.TerseLogFormatter.BLANKS;
import static com.metsci.glimpse.util.logging.format.TerseLogFormatter.LINE_SEPARATOR;
import static com.metsci.glimpse.util.logging.format.TerseLogFormatter.MAX_LEVEL_LENGTH;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;

/**
 * Produces "Terse" Log Messages which fit on one line and include just the level, method, class
 * name, and raw message.
 *
 * @author hogye
 */
public class TerseMethodNameLogFormatter extends Formatter
{
    @Override
    public String format( LogRecord record )
    {
        super.format( record );

        StringBuilder prefix0 = new StringBuilder( );
        appendPrefix( record, prefix0 );

        String prefix = prefix0.toString( );

        StringWriter message = new StringWriter( );
        message.write( record.getMessage( ) );

        Throwable thrown = record.getThrown( );
        if ( thrown != null )
        {
            message.write( LINE_SEPARATOR );
            thrown.printStackTrace( new PrintWriter( message ) );
        }

        return prefix + message.toString( ).replace( LINE_SEPARATOR, LINE_SEPARATOR + prefix.replaceAll( ".", " " ) ) + LINE_SEPARATOR;
    }

    protected void appendPrefix( LogRecord record, StringBuilder prefix )
    {
        String levelName = record.getLevel( ).getName( );
        String padding = BLANKS.substring( 0, MAX_LEVEL_LENGTH - levelName.length( ) );
        prefix.append( padding ).append( levelName ).append( ": " );

        String loggerName = record.getLoggerName( );
        if ( loggerName != null )
        {
            prefix.append( Thread.currentThread( ).getName( ) ).append( " - " );

            String loggerShortName = loggerName.substring( loggerName.lastIndexOf( '.' ) + 1 );
            prefix.append( loggerShortName );

            String methodName = record.getSourceMethodName( );
            if ( methodName != null )
            {
                prefix.append( "." + methodName );
            }

            prefix.append( ": " );
        }
    }
}

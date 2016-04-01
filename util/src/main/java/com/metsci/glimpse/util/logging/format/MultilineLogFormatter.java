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

import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Produces "Multiline" Log Messages which place each of LEVEL, CLASS, METHOD, MSG, and TIME on a
 * separate line.
 *
 * @author moskowitz
 */
public class MultilineLogFormatter extends Formatter
{
    @Override
    public String format( LogRecord rec )
    {
        super.format( rec );

        StringBuilder builder = new StringBuilder( 1000 );

        // give a red color to any messages with levels >= WARNING
        builder.append( LINE_SEPARATOR );
        builder.append( "LEVEL  : " );
        if ( rec.getLevel( ).intValue( ) >= Level.WARNING.intValue( ) )
        {
            builder.append( "<font color=\"red\">" );
            builder.append( rec.getLevel( ) );
            builder.append( "</font>" );
        }
        else
        {
            builder.append( rec.getLevel( ) );
        }

        // builder.append(": ");
        builder.append( LINE_SEPARATOR );
        builder.append( "CLASS  : " ).append( rec.getSourceClassName( ) );

        // builder.append('.');
        builder.append( LINE_SEPARATOR );
        builder.append( "METHOD : " ).append( rec.getSourceMethodName( ) );

        // builder.append(": ");
        builder.append( LINE_SEPARATOR );
        builder.append( "MSG    : " ).append( formatMessage( rec ) );

        // builder.append('.');
        builder.append( LINE_SEPARATOR );
        builder.append( "TIME   : " ).append( new Date( ) );

        // builder.append('\n');
        builder.append( LINE_SEPARATOR );

        return builder.toString( );
    }

    // This method is called when the handler is created

    @Override
    public String getHead( Handler h )
    {
        return "<HTML><HEAD> My Custom Log from " + new Date( ) + "</HEAD><BODY><H1>The logs</H1><PRE>" + LINE_SEPARATOR;
    }

    // This method is called when the handler is closed

    @Override
    public String getTail( Handler h )
    {
        return "</PRE></BODY></HTML>" + LINE_SEPARATOR;
    }

}

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

import java.util.logging.LogRecord;

/**
 * Base class for Metron logging formatters.  This exists in order to provide a base format method
 * which will infer the correct caller and method names and inject them into the LogRecord.
 *
 * @author  moskowitz
 */
public abstract class Formatter extends java.util.logging.Formatter
{

    /**
     * This method should be called via super.format(record) as the first line of any subclass
     * format method.  It infers the correct caller and method names and injects them into the
     * LogRecord (filtering out the internal classes and methods of the Metron logging itself).
     *
     * @see  java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    @Override
    public String format( LogRecord record )
    {
        inferAndInjectCaller( record );

        return null;
    }

    // Private method to infer the caller's class and method names
    // and inject these into a LogRecord.
    //
    // Note: Based on similar method in the LogRecord class but filters out
    // Metron LoggerUtils in addition to standard Java Logger.
    private void inferAndInjectCaller( LogRecord record )
    {

        // Get the stack trace.
        StackTraceElement[] stack = ( new Throwable( ) ).getStackTrace( );

        // First, search back to a method in the Logger or LoggerUtils class.
        int ix = 0;
        while ( ix < stack.length )
        {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName( );
            if ( cname.equals( "java.util.logging.Logger" ) )
            {
                break;
            }

            if ( cname.equals( "com.metsci.glimpse.util.logging.LoggerUtils" ) )
            {
                break;
            }

            ix++;
        }

        // Now search for the first frame before the "Logger" class.
        while ( ix < stack.length )
        {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName( );
            if ( !cname.equals( "java.util.logging.Logger" ) && !cname.equals( "com.metsci.glimpse.util.logging.LoggerUtils" ) )
            {

                // We've found the relevant frame.
                record.setSourceClassName( cname );
                record.setSourceMethodName( frame.getMethodName( ) );

                return;
            }

            ix++;
        }
    }

}

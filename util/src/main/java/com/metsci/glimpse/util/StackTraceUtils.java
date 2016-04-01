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
package com.metsci.glimpse.util;

/**
 * Utilities for examining stack traces at any point during program execution.
 *
 * @author moskowitz
 */
public class StackTraceUtils
{
    private StackTraceUtils( )
    {
    }

    /**
     * Name of caller for debug/trace/logging purposes.
     *
     * @return  Information String about caller suitable for logging.
     */
    public static String getCaller( )
    {
        StackTraceElement[] trace = Thread.currentThread( ).getStackTrace( );

        // skip past getCaller call, getStackTrace, and caller of this method to reach trace
        // element of interest
        int index = 3;
        if ( trace.length < ( index + 1 ) )
        {
            return "n/a";
        }

        StackTraceElement elem = trace[index];

        String className = elem.getClassName( );

        return className + "." + elem.getMethodName( ) + " line " + elem.getLineNumber( );
    }

    /**
     * Names of callers for debug/trace/logging purposes.
     *
     * @param   nBack  how far up the stack to go (should be >=1)
     * @return  Information String about callers (up the stack) suitable for logging.
     */
    public static String getCallers( int nBack )
    {
        StackTraceElement[] trace = Thread.currentThread( ).getStackTrace( );

        // skip past getCaller call, getStackTrace, and caller of this method to reach trace
        // element of interest
        int index = 3;
        if ( trace.length < ( index + 1 ) )
        {
            return "callers unidentified";
        }

        StringBuilder sb = new StringBuilder( "called by..." );
        while ( index < trace.length )
        {
            StackTraceElement elem = trace[index];
            sb.append( String.format( "%n%s.%s line %s", elem.getClassName( ), elem.getMethodName( ), elem.getLineNumber( ) ) );
            nBack--;
            if ( nBack <= 0 )
            {
                break;
            }

            index++;
        }

        return sb.toString( );
    }

    /**
     * Names of callers for debug/trace/logging purposes.
     *
     * @param   nBack  how far up the stack to go (should be >=1)
     * @return  Information String about callers (up the stack) suitable for logging.
     */
    public static String getCallersCompact( int nBack )
    {
        StackTraceElement[] trace = Thread.currentThread( ).getStackTrace( );

        // skip past getCaller call, getStackTrace, and caller of this method to reach trace
        // element of interest
        int index = 3;
        if ( trace.length < ( index + 1 ) )
        {
            return "";
        }

        StringBuilder sb = new StringBuilder( );
        while ( index < trace.length )
        {
            StackTraceElement elem = trace[index];
            String className = elem.getClassName( );
            sb.append( String.format( "%s.%s:%s", className.substring( className.lastIndexOf( "." ) + 1 ), elem.getMethodName( ), elem.getLineNumber( ) ) );
            nBack--;
            if ( nBack <= 0 )
            {
                break;
            }

            sb.append( " <- " );

            index++;
        }

        return sb.toString( );
    }

    public static String stackTraceToString( Exception e, int nBack )
    {
        return stackTraceToString( e.getStackTrace( ), nBack );
    }

    public static String stackTraceToString( StackTraceElement[] trace, int nBack )
    {
        int index = 0;
        if ( trace.length < ( index + 1 ) )
        {
            return "n/a";
        }

        StringBuilder sb = new StringBuilder( "trace..." );
        while ( index < trace.length )
        {
            StackTraceElement elem = trace[index];
            sb.append( String.format( "%n%s.%s line %s", elem.getClassName( ), elem.getMethodName( ), elem.getLineNumber( ) ) );
            nBack--;
            if ( nBack <= 0 )
            {
                break;
            }

            index++;
        }

        return sb.toString( );
    }
}

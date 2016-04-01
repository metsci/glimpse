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
package com.metsci.glimpse.dspl.parser.util;

import static com.metsci.glimpse.dspl.parser.util.QuoteIgnorringStringSplitter.ParseMode.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuoteIgnorringStringSplitter
{
    protected static final char QUOTE = '\"';
    protected static final char COMMA = ',';

    protected static enum ParseMode
    {
        BETWEEN_QUOTES, DEFAULT
    };

    public static String[] splitLine( String s ) throws IOException
    {
        return splitLine( s, Integer.MAX_VALUE );
    }

    public static String[] splitLine( String s, int limit ) throws IOException
    {
        return splitLine( s, COMMA, Integer.MAX_VALUE );
    }

    public static String[] splitLine( String s, char split, int limit ) throws IOException
    {
        final char[] line = s.toCharArray( );
        final int lineLength = s.length( );

        List<String> items = new ArrayList<String>( 20 );

        int itemStartPos = 0;
        ParseMode mode = DEFAULT;
        for ( int linePos = 0; linePos < lineLength; linePos++ )
        {
            if ( items.size( ) == limit - 1 )
            {
                linePos = lineLength - 1;
            }

            char c = line[linePos];

            if ( linePos == lineLength - 1 )
            {
                if ( c == QUOTE )
                {
                    items.add( s.substring( itemStartPos, linePos ) );
                }
                else if ( c == split )
                {
                    items.add( s.substring( itemStartPos, linePos ) );
                    if ( items.size( ) < limit )
                    {
                        items.add( s.substring( 0, 0 ) );
                    }
                }
                else
                {
                    items.add( s.substring( itemStartPos, linePos + 1 ) );
                }
                break;
            }

            switch ( mode )
            {

            case BETWEEN_QUOTES:
                if ( c == QUOTE )
                {
                    mode = DEFAULT;
                }
                else if ( c == split )
                {
                    // ignore, this is part of item between quotes
                }
                else
                {
                    // ignore, part of item
                }
                break;

            case DEFAULT:
                if ( c == QUOTE )
                {
                    mode = BETWEEN_QUOTES;
                }
                else if ( c == split )
                {
                    items.add( s.substring( itemStartPos, linePos ) );
                    itemStartPos = linePos + 1;
                }
                else
                {
                    // ignore, part of item
                }
                break;

            default:
                throw new RuntimeException( "Impossible state." );

            }
        }

        return items.toArray( new String[0] );
    }
}

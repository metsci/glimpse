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
package com.metsci.glimpse.dnc.facc;

import static com.google.common.base.Objects.equal;
import static java.lang.Integer.parseInt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaccIo
{

    public static final URL faccFeaturesUrl = FaccIo.class.getResource( "/com/metsci/glimpse/dnc/facc/facc-features.csv" );
    public static final URL faccAttrsUrl = FaccIo.class.getResource( "/com/metsci/glimpse/dnc/facc/facc-attrs.csv" );
    public static final URL faccAttrValsUrl = FaccIo.class.getResource( "/com/metsci/glimpse/dnc/facc/facc-attr-vals.csv" );


    protected static Reader createReader( URL url ) throws IOException
    {
        return new BufferedReader( new InputStreamReader( url.openStream( ) ) );
    }

    public static Map<String,FaccFeature> readFaccFeatures( ) throws IOException
    {
        try ( Reader reader = createReader( faccFeaturesUrl ) )
        {
            return readFaccFeatures( reader );
        }
    }

    public static Map<String,FaccFeature> readFaccFeatures( Reader reader ) throws IOException
    {
        List<List<String>> rows = parseFaccCsvFile( reader );

        // Skip header row
        rows = rows.subList( 1, rows.size( ) );

        Map<String,FaccFeature> features = new HashMap<>( );
        for ( List<String> tokens : rows )
        {
            String fcode = tokens.get( 0 ).toUpperCase( );
            String name = tokens.get( 1 );
            String text = tokens.get( 2 );

            features.put( fcode, new FaccFeature( fcode, name, text ) );
        }
        return features;
    }

    public static Map<String,FaccAttr> readFaccAttrs( ) throws IOException
    {
        Map<String,Map<Object,Object>> valueLookups = readFaccValues( );
        try ( Reader reader = createReader( faccAttrsUrl ) )
        {
            return readFaccAttrs( reader, valueLookups );
        }
    }

    public static Map<String,FaccAttr> readFaccAttrs( Reader reader, Map<String,Map<Object,Object>> valueLookups ) throws IOException
    {
        List<List<String>> rows = parseFaccCsvFile( reader );

        // Skip header row
        rows = rows.subList( 1, rows.size( ) );

        Map<String,FaccAttr> attrs = new HashMap<>( );
        for ( List<String> tokens : rows )
        {
            String code = tokens.get( 0 ).toLowerCase( );
            String name = tokens.get( 12 );
            String text = tokens.get( 13 );

            boolean hasCodedValues = tokens.get( 14 ).equalsIgnoreCase( "Coded" );
            Map<Object,Object> valueLookup = ( hasCodedValues ? valueLookups.get( code ) : null );

            attrs.put( code, new FaccAttr( code, name, text, valueLookup ) );
        }
        return attrs;
    }

    public static Map<String,Map<Object,Object>> readFaccValues( ) throws IOException
    {
        try ( Reader reader = createReader( faccAttrValsUrl ) )
        {
            return readFaccValues( reader );
        }
    }

    public static Map<String,Map<Object,Object>> readFaccValues( Reader reader ) throws IOException
    {
        List<List<String>> rows = parseFaccCsvFile( reader );

        // Skip header row
        rows = rows.subList( 1, rows.size( ) );

        Map<String,Map<Object,Object>> valueLookups = new HashMap<>( );
        for ( List<String> tokens : rows )
        {
            String attr = tokens.get( 0 ).toLowerCase( );
            Integer valueCode = parseInt( tokens.get( 1 ) );
            String valueText = tokens.get( 2 );

            Map<Object,Object> valueLookup = valueLookups.computeIfAbsent( attr, ( k ) -> new HashMap<>( ) );
            valueLookup.put( valueCode, valueText );
        }
        return valueLookups;
    }

    /**
     * Parses a FACC CSV file, honoring newlines inside quotes
     */
    public static List<List<String>> parseFaccCsvFile( Reader reader ) throws IOException
    {
        List<List<String>> rows = new ArrayList<>( );

        List<String> row = new ArrayList<>( );

        StringBuilder token = new StringBuilder( );

        Character c = readChar( reader );
        boolean quoted = false;
        while ( c != null )
        {
            Character cNext = readChar( reader );

            // Collapse EOL to a single char
            if ( ( equal( c, '\n' ) && equal( cNext, '\r' ) ) || ( equal( c, '\r' ) && equal( cNext, '\n' ) ) )
            {
                c = '\n';
                cNext = readChar( reader );
            }

            if ( quoted )
            {
                if ( equal( c, '"' ) && !equal( cNext, '"' ) )
                {
                    // Lone quote char (not two in a row) -- end quoted region
                    quoted = false;
                }
                else if ( equal( c, '"' ) && equal( cNext, '"' ) )
                {
                    // Two quote chars in a row -- skip the second one
                    cNext = readChar( reader );
                    token.append( c );
                }
                else
                {
                    token.append( c );
                }
            }
            else
            {
                if ( equal( c, '\n' ) || equal( c, '\r' ) )
                {
                    // Unquoted newline -- end both token and row
                    row.add( token.toString( ).trim( ) );
                    token.setLength( 0 );
                    rows.add( row );
                    row = new ArrayList<>( );
                }
                else if ( equal( c, ',' ) )
                {
                    // Unquoted delimiter -- end token
                    row.add( token.toString( ).trim( ) );
                    token.setLength( 0 );
                }
                else if ( equal( c, '"' ) )
                {
                    quoted = true;
                }
                else
                {
                    token.append( c );
                }
            }

            c = cNext;
        }

        // EOF
        if ( token.length( ) > 0 )
        {
            row.add( token.toString( ) );
        }
        if ( !row.isEmpty( ) )
        {
            rows.add( row );
        }

        return rows;
    }

    protected static Character readChar( Reader reader ) throws IOException
    {
        int v = reader.read( );
        if ( v == -1 )
        {
            return null;
        }
        else
        {
            return ( char ) v;
        }
    }

}

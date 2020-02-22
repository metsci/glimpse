/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.util.io;

import static com.google.common.base.Charsets.UTF_8;
import static com.metsci.glimpse.util.GeneralUtils.asSet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.function.LongFunction;

import com.google.common.io.Resources;

public class IoUtils
{

    /**
     * Terse way to get the URL of a file.
     */
    public static URL file( String loc )
    {
        return url( new File( loc ) );
    }

    /**
     * Terse way to get the URL of a file.
     */
    public static URL url( File file )
    {
        try
        {
            return file.toURI( ).toURL( );
        }
        catch ( MalformedURLException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static String requireText( URL url )
    {
        return requireText( url, UTF_8 );
    }

    public static String requireText( URL url, Charset charset )
    {
        try
        {
            return Resources.toString( url, charset );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static File findUniqueFile( File dir, LongFunction<String> getFilenameForIndex )
    {
        Set<String> existingNames = asSet( dir.list( ) );
        for ( long i = 0; i < Long.MAX_VALUE; i++ )
        {
            String name = getFilenameForIndex.apply( i );
            if ( !existingNames.contains( name ) )
            {
                File file = new File( dir, name );
                if ( !file.exists( ) )
                {
                    return file;
                }
            }
        }

        // This will never happen in practice, since we try up to 2^63 possible filenames
        throw new RuntimeException( "Too many files with similar names already exist" );
    }

}

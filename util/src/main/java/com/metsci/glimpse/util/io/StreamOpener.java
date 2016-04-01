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
package com.metsci.glimpse.util.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author hogye
 */
public interface StreamOpener
{

    InputStream openForRead( String location ) throws IOException;

    public static final StreamOpener fileOpener = new StreamOpener( )
    {
        public InputStream openForRead( String location ) throws IOException
        {
            return new FileInputStream( location );
        }
    };

    public static final StreamOpener resourceOpener = new StreamOpener( )
    {
        public InputStream openForRead( String location ) throws IOException
        {
            InputStream stream = getClass( ).getClassLoader( ).getResourceAsStream( location );
            if ( stream == null ) throw new FileNotFoundException( "Resource not found: " + location );
            return stream;
        }
    };

    public static final StreamOpener fileThenResourceOpener = new StreamOpener( )
    {
        public InputStream openForRead( String location ) throws IOException
        {
            try
            {
                return new FileInputStream( location );
            }
            catch ( FileNotFoundException e )
            {
            }
            catch ( SecurityException e )
            {
            }

            try
            {
                InputStream stream = getClass( ).getClassLoader( ).getResourceAsStream( location );
                if ( stream == null ) throw new FileNotFoundException( );
                return stream;
            }
            catch ( FileNotFoundException e )
            {
            }
            catch ( SecurityException e )
            {
            }

            throw new FileNotFoundException( "File is not available as a local file or as a classpath resource: " + location );
        }
    };

    // Here for compatibility; will soon be deprecated

    public static final StreamOpener file = fileOpener;

    public static final StreamOpener resource = resourceOpener;

    public static final StreamOpener fileThenResource = fileThenResourceOpener;

}

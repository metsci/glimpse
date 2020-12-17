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

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MappedFileTest
{

    @Test
    void mappedFileShouldBasicallyWork( ) throws IOException
    {
        File tempFile = File.createTempFile( "MappedFileTest_", "" );
        int numBytes = 16;

        // Write to file
        {
            MappedFile mappedFile = new MappedFile( tempFile, LITTLE_ENDIAN, numBytes );
            ByteBuffer bytes = mappedFile.slice( 0, numBytes );
            for ( byte i = 0; i < numBytes; i++ )
            {
                bytes.put( i );
            }
            mappedFile.dispose( );
        }

        // Read from file
        {
            MappedFile mappedFile = new MappedFile( tempFile, LITTLE_ENDIAN );
            ByteBuffer bytes = mappedFile.slice( 0, numBytes );
            for ( byte i = 0; i < numBytes; i++ )
            {
                Assertions.assertEquals( i, bytes.get( ) );
            }
            mappedFile.dispose( );
        }

        tempFile.delete( );
    }

}

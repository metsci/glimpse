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
package com.metsci.glimpse.util.io;

import static com.metsci.glimpse.util.io.MappedBufferStats.*;
import static com.metsci.glimpse.util.io.MappedFile.*;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.metsci.glimpse.util.jnlu.FileUtils;

public class FileMapperWindows64 implements FileMapper
{
    static
    {
        try
        {
            URL resourceUrl = MappedFile.class.getResource( "FileMapperWindows64.dll" );

            File tempDir = FileUtils.createTempDir( "MappedFile" );
            File tempFile = new File( tempDir, "FileMapperWindows64.dll" );

            Resources.asByteSource( resourceUrl ).copyTo( Files.asByteSink( tempFile ) );

            System.load( tempFile.getAbsolutePath( ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    private static native long _map( FileDescriptor fileDescriptor, long size, boolean writable ) throws IOException;
    private static native void _unmap( long mappingAddr ) throws IOException;

    @Override
    public long map( RandomAccessFile raf, long size, boolean writable ) throws IOException
    {
        FileDescriptor fd = getFileDescriptorForMapping( raf );
        return _map( fd, size, writable );
    }

    @Override
    public Runnable createUnmapper( long address, long size, RandomAccessFile raf )
    {
        addToMappedBufferStats( +1, +size, +size );
        return ( ) ->
        {
            if ( address != 0 )
            {
                try
                {
                    _unmap( address );
                    addToMappedBufferStats( -1, -size, -size );
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( e );
                }
            }
        };
    }

}

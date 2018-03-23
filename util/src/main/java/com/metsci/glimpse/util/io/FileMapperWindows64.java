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
            URL resourceUrl = MappedFile.class.getClassLoader( ).getResource( "MappedFile/FileMapperWindows64.dll" );

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

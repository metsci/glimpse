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

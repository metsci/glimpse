package com.metsci.glimpse.util.io;

import static com.metsci.glimpse.util.io.FileSync.isHeldByCurrentThread;
import static com.metsci.glimpse.util.io.FileSync.lockFile;
import static com.metsci.glimpse.util.io.FileSync.unlockFile;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileSyncTest
{
    File file;

    @BeforeEach
    public void createFile( ) throws IOException
    {
        file = Files.createTempFile( "glimpse_test", ".lock" ).toFile( );
    }

    @AfterEach
    public void deleteFile( ) throws IOException
    {
        file.delete( );
    }

    @Test
    public void testReentrant( ) throws IOException
    {
        // Lock #1
        lockFile( file );

        // Lock #2
        lockFile( file );

        // Lock #3
        lockFile( file );

        // Unlock #3
        unlockFile( file );
        assertTrue( isHeldByCurrentThread( file ) );

        // Unlock #2
        unlockFile( file );
        assertTrue( isHeldByCurrentThread( file ) );

        // Unlock #1
        unlockFile( file );
        assertFalse( isHeldByCurrentThread( file ) );
    }
}

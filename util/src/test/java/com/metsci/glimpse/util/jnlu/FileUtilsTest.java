package com.metsci.glimpse.util.jnlu;

import static com.google.common.io.ByteStreams.copy;
import static com.metsci.glimpse.util.jnlu.FileUtils.execDeleteRecursively;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class FileUtilsTest
{
    @Test
    public void testDeleteAsync( ) throws IOException, InterruptedException
    {
        File dir = Files.createTempDirectory( "glimpse_test" ).toFile( );

        Path file1 = dir.toPath( ).resolve( ".keep" );
        Path file2 = dir.toPath( ).resolve( "test" );
        createFile( file1 );
        createFile( file2 );

        Process p = execDeleteRecursively( 1, 0, dir );
        copy( p.getErrorStream( ), System.err );
        p.waitFor( );

        assertFalse( isRegularFile( file1 ) );
        assertFalse( isRegularFile( file2 ) );
        assertFalse( isDirectory( dir.toPath( ) ) );
    }
}

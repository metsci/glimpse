package com.metsci.glimpse.platformFixes;

import static com.metsci.glimpse.util.GeneralUtils.newArrayList;
import static com.metsci.glimpse.util.jnlu.FileUtils.copy;
import static com.metsci.glimpse.util.jnlu.FileUtils.createTempDir;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.net.URL;
import java.util.List;

public class LibraryList
{

    public final String resourceDir;
    public final List<String> filenames;


    public LibraryList( String resourceDir, String... filenames )
    {
        this( resourceDir, asList( filenames ) );
    }

    public LibraryList( String resourceDir, List<String> filenames )
    {
        this.resourceDir = resourceDir;
        this.filenames = unmodifiableList( newArrayList( filenames ) );
    }

    public void extractAndLoad( ClassLoader resourceLoader, String tempDirName )
    {
        try
        {
            File tempDir = createTempDir( tempDirName );
            for ( String filename : filenames )
            {
                URL url = resourceLoader.getResource( resourceDir + "/" + filename );
                File file = new File( tempDir, filename );
                copy( url, file );
                System.load( file.getPath( ) );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to extract and load native libraries", e );
        }
    }

}
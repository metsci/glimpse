package com.metsci.glimpse.platformFixes;

import static com.metsci.glimpse.util.GeneralUtils.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

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
}
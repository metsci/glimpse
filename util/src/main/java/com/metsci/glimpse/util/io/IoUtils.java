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

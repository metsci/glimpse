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
package com.metsci.glimpse.util.jnlu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author hogye
 */
public class FileUtils
{

    public static File createTempDir( String prefix ) throws IOException
    {
        int numAttempts = 25;
        for ( int i = 0; i < numAttempts; i++ )
        {
            File temp = File.createTempFile( prefix + "_", "" );

            if ( !temp.delete( ) ) throw new RuntimeException( "Failed to delete temp file while creating temp directory: " + temp.getAbsolutePath( ) );

            if ( temp.mkdirs( ) )
            {
                scheduleRecursiveDeleteAfterShutdown( temp );
                return temp;
            }
        }

        throw new RuntimeException( "Failed to create temp directory in " + numAttempts + " attempts" );
    }

    /**
     * On some platforms, a file cannot be deleted while it is in use. In some cases,
     * certain files may be in use by the JVM until it exits, and therefore can only
     * be deleted <em>after</em> the JVM exits. Notably, this includes native library
     * files.
     *
     * This method registers a shutdown hook that will spawn a separate JVM, from which
     * repeated attempts will be made to delete the specified file or directory. These
     * attempts will continue for up to 30 seconds, which should be enough time for this
     * JVM to exit, at which point any files it had open can be deleted.
     */
    public static void scheduleRecursiveDeleteAfterShutdown( final File fileOrDir )
    {
        Runtime.getRuntime( ).addShutdownHook( new Thread( )
        {
            public void run( )
            {
                try
                {
                    execDeleteRecursively( 60, 500, fileOrDir );
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } );
    }

    /**
     * Spawns a separate process that will delete the specified file or directory. The
     * process attempts the deletion repeatedly, until it succeeds, up to numAttempts
     * times.
     */
    public static void execDeleteRecursively( int numAttempts, int millisBetweenAttempts, File fileOrDir ) throws IOException
    {
        String java = System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty( "java.class.path" );
        Runtime.getRuntime( ).exec( new String[] { java, "-cp", classpath, DeleteRecursively.class.getName( ), Integer.toString( numAttempts ), Integer.toString( millisBetweenAttempts ), fileOrDir.getCanonicalPath( ) } );
    }

    public static class DeleteRecursively
    {
        public static void main( String[] args )
        {
            int numAttempts = Integer.parseInt( args[0] );
            int millisBetweenAttempts = Integer.parseInt( args[1] );
            File fileOrDir = new File( args[2] );

            for ( int i = 0; fileOrDir.exists( ) && i < numAttempts; i++ )
            {
                deleteRecursively( fileOrDir );
                try
                {
                    Thread.sleep( millisBetweenAttempts );
                }
                catch ( InterruptedException e )
                {
                }
            }
        }
    }

    public static boolean deleteRecursively( File fileOrDir )
    {
        if ( fileOrDir.isDirectory( ) )
        {
            for ( File child : fileOrDir.listFiles( ) )
                deleteRecursively( child );
        }

        return fileOrDir.delete( );
    }

    public static void copy( URL fromUrl, File toFile ) throws IOException
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            in = fromUrl.openStream( );
            out = new FileOutputStream( toFile );
            byte[] bytes = new byte[16384];
            while ( true )
            {
                int bytesRead = in.read( bytes );
                if ( bytesRead < 0 ) break;

                out.write( bytes, 0, bytesRead );
            }
        }
        finally
        {
            if ( in != null ) try
            {
                in.close( );
            }
            catch ( IOException e )
            {
            }
            if ( out != null ) try
            {
                out.close( );
            }
            catch ( IOException e )
            {
            }
        }
    }

}

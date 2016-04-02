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

import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author hogye
 */
public class NativeLibUtils
{

    public static boolean onPlatform( String osPrefix, String osArch )
    {
        String givenOsPrefix = osPrefix.toLowerCase( );
        String givenOsArch = osArch.toLowerCase( );

        String actualOsName = System.getProperty( "os.name" ).toLowerCase( );
        String actualOsArch = System.getProperty( "os.arch" ).toLowerCase( );

        return actualOsName.startsWith( givenOsPrefix ) && actualOsArch.equals( givenOsArch );
    }

    public static void loadLibs( String resourceSearchPath, File extractDir, String... libShortNames ) throws IOException
    {
        loadLibs( new String[] { resourceSearchPath }, extractDir, libShortNames );
    }

    public static void loadLibs( String[] resourceSearchPaths, File extractDir, String... libShortNames ) throws IOException
    {
        for ( String libShortName : libShortNames )
        {
            File libFile = extractLib( resourceSearchPaths, extractDir, libShortName );
            System.load( libFile.getPath( ) );
        }
    }

    public static File extractLib( String resourceSearchPath, File destDir, String libShortName ) throws IOException
    {
        return extractLib( new String[] { resourceSearchPath }, destDir, libShortName );
    }

    public static File extractLib( String[] resourceSearchPaths, File destDir, String libShortName ) throws IOException
    {
        List<String> resourceSearchPaths2 = unmodifiableList( Arrays.asList( resourceSearchPaths ) );

        ResolvedResource lib = resolveLib( resourceSearchPaths2, libShortName );
        if ( lib == null ) throw new RuntimeException( "Couldn't find library on classpath: " + libShortName );

        return copy( lib, destDir );
    }

    public static class ResolvedResource
    {
        public final URL url;
        public final String name;

        public ResolvedResource( URL url, String name )
        {
            this.url = url;
            this.name = name;
        }
    }

    public static ResolvedResource resolveLib( List<String> resourceSearchPaths, String libShortName )
    {
        return resolveResource( resourceSearchPaths, possibleLibNames( libShortName ) );
    }

    public static List<String> possibleLibNames( String libShortName )
    {
        String likely = System.mapLibraryName( libShortName );

        List<String> possibles = new ArrayList<String>( );
        possibles.add( likely );

        // On Darwin, we need to check the .dylib extension if no .jnilib is found
        if ( likely.endsWith( ".jnilib" ) ) possibles.add( likely.substring( 0, likely.length( ) - ".jnilib".length( ) ) + ".dylib" );

        return possibles;
    }

    public static ResolvedResource resolveResource( List<String> possiblePaths, List<String> possibleNames )
    {
        ClassLoader cl = NativeLibUtils.class.getClassLoader( );
        for ( String path : possiblePaths )
        {
            for ( String name : possibleNames )
            {
                boolean needSep = ( !path.isEmpty( ) && !path.endsWith( "/" ) );
                URL url = cl.getResource( path + ( needSep ? "/" : "" ) + name );
                if ( url != null ) return new ResolvedResource( url, name );
            }
        }
        return null;
    }

    public static File copy( ResolvedResource from, File toDir ) throws IOException
    {
        File toFile = new File( toDir, from.name );
        FileUtils.copy( from.url, toFile );
        return toFile;
    }

    /**
     * By itself, this method is not very helpful, because the ClassLoader loads the
     * "java.library.path" property once at startup. However, it can be useful in
     * conjunction with {@link NativeLibUtils#addLibDirToClassLoader_FRAGILE(File)}.
     */
    public static void addLibDirToSystemProperty( File newDir, boolean prepend ) throws IOException
    {
        String propKey = "java.library.path";

        String originalProp = System.getProperty( propKey, "" );
        String[] originalArray = originalProp.split( Pattern.quote( File.pathSeparator ) );

        Set<String> originalPaths = new HashSet<String>( );
        for ( String p : originalArray )
            originalPaths.add( ( new File( p ) ).getCanonicalPath( ) );
        if ( originalPaths.contains( newDir.getCanonicalPath( ) ) ) return;

        String newProp;
        if ( prepend )
        {
            newProp = newDir.getPath( ) + File.pathSeparator + originalProp;
        }
        else
        {
            newProp = originalProp + File.pathSeparator + newDir.getPath( );
        }
        System.setProperty( propKey, newProp );
    }

}

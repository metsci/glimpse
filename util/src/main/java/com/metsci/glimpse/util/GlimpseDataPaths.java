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
package com.metsci.glimpse.util;

import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logConfig;

import java.io.File;
import java.util.logging.Logger;

/**
 * A class containing paths for Glimpse-related data directories. Most client code should
 * not refer to this class directly, but should instead use the appropriate module-specific
 * {@code DataPaths} class (e.g. {@code DncDataPaths}).
 * <p>
 * If a Glimpse module needs to store module-specific data, it should have its own module-
 * specific directories, and should provide a module-specific {@code DataPaths} class that
 * contains the paths of those directories. Module-specific dirs should be subdirs under
 * the {@link GlimpseDataPaths} dirs.
 * <p>
 * The value of each {@link GlimpseDataPaths} path is set based on:
 * <ol>
 * <li>A JVM property (highest precedence)
 * <li>An environment variable
 * <li>An OS-appropriate default (lowest precedence)
 * </ol>
 * <p>
 * The "shared" dir is intended to allow users to share data. It should generally be
 * expected to be read-only.
 * <p>
 * The "user" dirs are user-specific, and can generally be assumed to be writable.
 * Typically, if data exists in both the user dir and the shared dir, the user dir
 * should take precedence, because the user has more control over it than over the
 * shared dir.
 * <p>
 * The "cache" dir is intended to hold data that a program can generate if it does not
 * already exist. Cache dirs can be freely deleted when the program is not running.
 * <p>
 * The "data" dirs are intended to hold data which cannot be regenerated easily.
 * <p>
 * @author hogye
 */
public class GlimpseDataPaths
{

    /**
     * Parent directory for permanent data (not easy to regenerate) that can be read by
     * multiple users.
     * <ol>
     * <li>JVM prop (if defined): {@code glimpse.sharedDataDir}
     * <li>Env var (if defined): {@code GLIMPSE_SHARED_DATA}
     * <li>Default:
     *   <ul>
     *   <li>Windows: {@code %ALLUSERSPROFILE%\\Glimpse\\Data}
     *   <li>Mac: {@code /Library/Application Support/Glimpse/Data}
     *   <li>Other: {@code /var/lib/glimpse/data}
     *   </ul>
     * </ol>
     */
    public static final File glimpseSharedDataDir = glimpseSharedDataDir( );
    private static File glimpseSharedDataDir( )
    {
        String jvmProp = System.getProperty( "glimpse.sharedDataDir" );
        if ( jvmProp != null )
        {
            return new File( jvmProp );
        }

        String envVar = System.getenv( "GLIMPSE_SHARED_DATA" );
        if ( envVar != null )
        {
            return new File( envVar );
        }

        if ( onWindows( ) )
        {
            String allUsersProfile = trimWindowsPath( System.getenv( "ALLUSERSPROFILE" ) );
            return new File( allUsersProfile + "\\Glimpse\\Data" );
        }

        if ( onMac( ) )
        {
            return new File( "/Library/Application Support/Glimpse/Data" );
        }

        return new File( "/var/lib/glimpse/data" );
    }

    /**
     * Parent directory for permanent data (not easy to regenerate) that can be read and
     * written by the current user.
     * <ol>
     * <li>JVM prop (if defined): {@code glimpse.userDataDir}
     * <li>Env var (if defined): {@code GLIMPSE_USER_DATA}
     * <li>Default:
     *   <ul>
     *   <li>Windows: {@code %LOCALAPPDATA%\\Glimpse\\Data}
     *   <li>Mac: {@code ~/Library/Glimpse/Data}
     *   <li>Other: {@code ~/.local/share/glimpse}
     *   </ul>
     * </ol>
     */
    public static final File glimpseUserDataDir = glimpseUserDataDir( );
    private static File glimpseUserDataDir( )
    {
        String jvmProp = System.getProperty( "glimpse.userDataDir" );
        if ( jvmProp != null )
        {
            return new File( jvmProp );
        }

        String envVar = System.getenv( "GLIMPSE_USER_DATA" );
        if ( envVar != null )
        {
            return new File( envVar );
        }

        if ( onWindows( ) )
        {
            String localAppData = trimWindowsPath( System.getenv( "LOCALAPPDATA" ) );
            return new File( localAppData + "\\Glimpse\\Data" );
        }

        if ( onMac( ) )
        {
            String userHome = trimUnixPath( System.getProperty( "user.home" ) );
            return new File( userHome + "/Library/Glimpse/Data" );
        }

        String userHome = trimUnixPath( System.getProperty( "user.home" ) );
        return new File( userHome + "/.local/share/glimpse" );
    }

    /**
     * Parent directory for cache data (easy to regenerate) that can be read and written
     * by the current user.
     * <ol>
     * <li>JVM prop (if defined): {@code glimpse.userCacheDir}
     * <li>Env var (if defined): {@code GLIMPSE_USER_CACHE}
     * <li>Default:
     *   <ul>
     *   <li>Windows: {@code %LOCALAPPDATA%\\Glimpse\\Cache}
     *   <li>Mac: {@code ~/Library/Caches/Glimpse}
     *   <li>Other: {@code ~/.cache/glimpse}
     *   </ul>
     * </ol>
     */
    public static final File glimpseUserCacheDir = glimpseUserCacheDir( );
    private static File glimpseUserCacheDir( )
    {
        String jvmProp = System.getProperty( "glimpse.userCacheDir" );
        if ( jvmProp != null )
        {
            return new File( jvmProp );
        }

        String envVar = System.getenv( "GLIMPSE_USER_CACHE" );
        if ( envVar != null )
        {
            return new File( envVar );
        }

        if ( onWindows( ) )
        {
            String localAppData = trimWindowsPath( System.getenv( "LOCALAPPDATA" ) );
            return new File( localAppData + "\\Glimpse\\Cache" );
        }

        if ( onMac( ) )
        {
            String userHome = trimUnixPath( System.getProperty( "user.home" ) );
            return new File( userHome + "/Library/Caches/Glimpse" );
        }

        String userHome = trimUnixPath( System.getProperty( "user.home" ) );
        return new File( userHome + "/.cache/glimpse" );
    }

    // Log dir locations, after they've been initialized
    private static final Logger logger = getLogger( GlimpseDataPaths.class );
    static
    {
        logConfig( logger, "Glimpse shared data: %s", glimpseSharedDataDir );
        logConfig( logger, "Glimpse user data: %s", glimpseUserDataDir );
        logConfig( logger, "Glimpse user cache: %s", glimpseUserCacheDir );
    }

    /**
     * Determine whether current platform's OS is Windows.
     */
    private static boolean onWindows( )
    {
        String osName = System.getProperty( "os.name" );
        return ( osName != null && osName.toLowerCase( ).startsWith( "windows" ) );
    }

    /**
     * Determine whether current platform's OS is Mac OS.
     */
    private static boolean onMac( )
    {
        String osName = System.getProperty( "os.name" );
        return ( osName != null && osName.toLowerCase( ).startsWith( "mac" ) );
    }

    /**
     * Remove trailing backslashes.
     */
    private static String trimWindowsPath( String windowsPath )
    {
        return windowsPath.replaceAll( "\\\\*$", "" );
    }

    /**
     * Remove trailing forward slashes, except for a forward slash that is the
     * first char of the string.
     */
    private static String trimUnixPath( String unixPath )
    {
        return unixPath.replaceAll( "(?<=.)/*$", "" );
    }

    /**
     * Throws {@link RuntimeException} if {@code dir} is not an existing directory.
     */
    public static File requireExistingDir( File dir )
    {
        if ( dir.isDirectory( ) )
        {
            return dir;
        }
        else
        {
            throw new RuntimeException( "No such directory: " + dir.getAbsolutePath( ) );
        }
    }

}

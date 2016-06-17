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

import java.io.File;

public class GlimpseDataPaths
{

    /**
     *
     */
    public static final String glimpseSharedDataDirProp = "glimpse.sharedDataDir";

    /**
     *
     */
    public static final File glimpseSharedDataDir;
    static
    {
        String override = System.getProperty( glimpseSharedDataDirProp );
        if ( override != null )
        {
            glimpseSharedDataDir = new File( override );
        }
        else if ( onWindows( ) )
        {
            String allUsersProfile = trimWindowsPath( System.getenv( "ALLUSERSPROFILE" ) );
            glimpseSharedDataDir = new File( allUsersProfile + "\\Glimpse\\Data" );
        }
        else if ( onMac( ) )
        {
            glimpseSharedDataDir = new File( "/Library/Application Support/Glimpse/Data" );
        }
        else
        {
            glimpseSharedDataDir = new File( "/var/lib/glimpse/data" );
        }
    }

    /**
     *
     */
    public static final String glimpseUserDataDirProp = "glimpse.userDataDir";

    /**
     *
     */
    public static final File glimpseUserDataDir;
    static
    {
        String override = System.getProperty( glimpseUserDataDirProp );
        if ( override != null )
        {
            glimpseUserDataDir = new File( override );
        }
        else if ( onWindows( ) )
        {
            String localAppData = trimWindowsPath( System.getenv( "LOCALAPPDATA" ) );
            glimpseUserDataDir = new File( localAppData + "\\Glimpse\\Data" );
        }
        else if ( onMac( ) )
        {
            String userHome = trimUnixPath( System.getProperty( "user.home" ) );
            glimpseUserDataDir = new File( userHome + "/Library/Glimpse/Data" );
        }
        else
        {
            String userHome = trimUnixPath( System.getProperty( "user.home" ) );
            glimpseUserDataDir = new File( userHome + "/.local/share/glimpse" );
        }
    }

    /**
     *
     */
    public static final String glimpseUserCacheDirProp = "glimpse.userCacheDir";

    /**
     *
     */
    public static final File glimpseUserCacheDir;
    static
    {
        String override = System.getProperty( glimpseUserCacheDirProp );
        if ( override != null )
        {
            glimpseUserCacheDir = new File( override );
        }
        else if ( onWindows( ) )
        {
            String localAppData = trimWindowsPath( System.getenv( "LOCALAPPDATA" ) );
            glimpseUserCacheDir = new File( localAppData + "\\Glimpse\\Cache" );
        }
        else if ( onMac( ) )
        {
            String userHome = trimUnixPath( System.getProperty( "user.home" ) );
            glimpseUserCacheDir = new File( userHome + "/Library/Caches/Glimpse" );
        }
        else
        {
            String userHome = trimUnixPath( System.getProperty( "user.home" ) );
            glimpseUserCacheDir = new File( userHome + "/.cache/glimpse" );
        }
    }

    /**
     *
     */
    public static boolean onWindows( )
    {
        String osName = System.getProperty( "os.name" );
        return ( osName != null && osName.toLowerCase( ).startsWith( "windows" ) );
    }

    /**
     *
     */
    public static boolean onMac( )
    {
        String osName = System.getProperty( "os.name" );
        return ( osName != null && osName.toLowerCase( ).startsWith( "mac" ) );
    }

    /**
     *
     */
    public static boolean onLinux( )
    {
        String osName = System.getProperty( "os.name" );
        return ( osName != null && osName.toLowerCase( ).startsWith( "linux" ) );
    }

    /**
     * Remove trailing backslashes.
     */
    public static String trimWindowsPath( String windowsPath )
    {
        return windowsPath.replaceAll( "\\\\*$", "" );
    }

    /**
     * Remove trailing forward slashes, except for a forward slash that is the
     * first char of the string.
     */
    public static String trimUnixPath( String unixPath )
    {
        return unixPath.replaceAll( "(?<=.)/*$", "" );
    }

}

package com.metsci.glimpse.charts.slippy;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.metsci.glimpse.util.GlimpseDataPaths;

public class SlippyDataPaths
{

    private SlippyDataPaths( )
    {
    }

    /**
     * Standard dir for slippy tile cache. Contains a folder for each tile source.
     * <ol>
     * <li>JVM prop (if defined): {@code glimpse.slippy.cacheDir}
     * <li>Env var (if defined): {@code GLIMPSE_SLIPPY_CACHE}
     * <li>Default: subdir slippy-cache of {@link GlimpseDataPaths#glimpseUserCacheDir}
     * </ol>
     */
    public static final Path slippyCacheRoot = slippyCacheRootDir( );
    private static Path slippyCacheRootDir( )
    {
        String jvmProp = System.getProperty( "glimpse.slippy.cacheDir" );
        if ( jvmProp != null )
        {
            return Paths.get( jvmProp );
        }

        String envVar = System.getenv( "GLIMPSE_SLIPPY_CACHE" );
        if ( envVar != null )
        {
            return Paths.get( envVar );
        }
        Path parent = GlimpseDataPaths.glimpseUserCacheDir.toPath( );
        return parent.resolve("slippy-cache");
    }
}

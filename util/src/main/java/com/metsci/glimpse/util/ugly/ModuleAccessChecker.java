package com.metsci.glimpse.util.ugly;

import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static java.util.logging.Level.SEVERE;

import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;

/**
 * Checks whether caller modules have the access they expect to packages in other
 * modules. This is useful for detecting missing {@code --add-opens} JVM args, for
 * example.
 * <p>
 * The particular approach used to do the check is unspecified and subject to change.
 */
public class ModuleAccessChecker
{
    private static final Logger logger = getLogger( ModuleAccessChecker.class );

    protected static final ImmutableSet<String> jvmArgs = ImmutableSet.copyOf( ManagementFactory.getRuntimeMXBean( ).getInputArguments( ) );

    protected static final Set<String> checkedJvmArgs = ConcurrentHashMap.newKeySet( );
    protected static void expectJvmArg( String arg, Level logLevel )
    {
        if ( checkedJvmArgs.add( arg ) && !jvmArgs.contains( arg ) )
        {
            logger.log( logLevel, "Missing expected JVM arg: " + arg );
        }
    }

    protected static final boolean jvmSupportsModules = doesJvmSupportModules( );
    protected static boolean doesJvmSupportModules( )
    {
        try
        {
            Class.forName( "java.lang.Module" );
            return true;
        }
        catch ( ClassNotFoundException e )
        {
            return false;
        }
    }

    public static void expectDeepReflectiveAccess( Class<?> fromClass, String toModuleName, String toPackageName )
    {
        expectDeepReflectiveAccess( fromClass, toModuleName, toPackageName, SEVERE );
    }

    public static void expectDeepReflectiveAccess( Class<?> fromClass, String toModuleName, String toPackageName, Level logLevel )
    {
        if ( jvmSupportsModules )
        {
            String fromModuleName = getModuleName( fromClass );
            String jvmArg = "--add-opens=" + toModuleName + "/" + toPackageName + "=" + ( fromModuleName != null ? fromModuleName : "ALL-UNNAMED" );
            expectJvmArg( jvmArg, logLevel );
        }
    }

    /**
     * Returns the name of the module that contains the given class, or null if
     * the given class is not contained in a named module (either because it is
     * in the unnamed pseudo-module, or because the running JVM does not support
     * modules).
     */
    protected static String getModuleName( Class<?> clazz )
    {
        try
        {
            Object module = Class.class.getMethod( "getModule" ).invoke( clazz );
            Object moduleName = Class.forName( "java.lang.Module" ).getMethod( "getName" ).invoke( module );
            return ( ( String ) moduleName );
        }
        catch ( Exception e )
        {
            return null;
        }
    }
}

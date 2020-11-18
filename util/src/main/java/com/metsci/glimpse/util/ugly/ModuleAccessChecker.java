package com.metsci.glimpse.util.ugly;

import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static java.util.Collections.singleton;
import static java.util.logging.Level.SEVERE;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
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

    protected static class Warning
    {
        public final String missingJvmArg;
        public final Level logLevel;

        public Warning( String missingJvmArg, Level logLevel )
        {
            this.missingJvmArg = missingJvmArg;
            this.logLevel = logLevel;
        }

        @Override
        public int hashCode( )
        {
            int prime = 30671;
            int result = 1;
            result = prime * result + Objects.hashCode( this.missingJvmArg );
            result = prime * result + Objects.hashCode( this.logLevel );
            return result;
        }

        @Override
        public boolean equals( Object o )
        {
            if ( o == this ) return true;
            if ( o == null ) return false;
            if ( o.getClass( ) != this.getClass( ) ) return false;

            Warning other = ( Warning ) o;
            return ( Objects.equals( other.missingJvmArg, this.missingJvmArg )
                  && Objects.equals( other.logLevel, this.logLevel ) );
        }
    }

    protected static void logWarning( String jvmArg, Level logLevel )
    {
        logWarnings( singleton( new Warning( jvmArg, logLevel ) ) );
    }

    protected static void logWarnings( Collection<? extends Warning> warnings )
    {
        if ( warnings.size( ) == 1 )
        {
            Warning warning = warnings.iterator( ).next( );
            logger.log( warning.logLevel, "Missing expected JVM arg: " + warning.missingJvmArg );
        }
        else if ( warnings.size( ) > 1 )
        {
            StringBuilder message = new StringBuilder( "Missing expected JVM args: " );
            Level maxLogLevel = null;
            for ( Warning warning : warnings )
            {
                message.append( System.lineSeparator( ) );
                message.append( "  " );
                message.append( warning.missingJvmArg );

                if ( maxLogLevel == null || warning.logLevel.intValue( ) > maxLogLevel.intValue( ) )
                {
                    maxLogLevel = warning.logLevel;
                }
            }
            logger.log( maxLogLevel, message.toString( ) );
        }
    }

    protected static final ThreadLocal<LinkedHashSet<Warning>> activeWarnings = new ThreadLocal<>( );
    public static void coalesceModuleAccessWarnings( Runnable task )
    {
        if ( activeWarnings.get( ) != null )
        {
            // Already inside a coalescer
            task.run( );
        }
        else
        {
            LinkedHashSet<Warning> warnings = new LinkedHashSet<>( );
            activeWarnings.set( warnings );
            try
            {
                task.run( );
                logWarnings( warnings );
            }
            finally
            {
                activeWarnings.set( null );
            }
        }
    }

    protected static final ImmutableSet<String> jvmArgs = ImmutableSet.copyOf( ManagementFactory.getRuntimeMXBean( ).getInputArguments( ) );
    protected static final Set<String> checkedJvmArgs = ConcurrentHashMap.newKeySet( );
    protected static void expectJvmArg( String jvmArg, Level logLevel )
    {
        if ( checkedJvmArgs.add( jvmArg ) && !jvmArgs.contains( jvmArg ) )
        {
            Set<Warning> warnings = activeWarnings.get( );
            if ( warnings == null )
            {
                // No active coalescer, so log the warning immediately
                logWarning( jvmArg, logLevel );
            }
            else
            {
                // Defer the warning until the end of the active coalescer
                warnings.add( new Warning( jvmArg, logLevel ) );
            }
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

    public static void expectInternalApiAccess( Class<?> fromClass, String toModuleName, String toPackageName )
    {
        expectAccess( "--add-exports", fromClass, toModuleName, toPackageName, SEVERE );
    }

    public static void expectInternalApiAccess( Class<?> fromClass, String toModuleName, String toPackageName, Level logLevel )
    {
        expectAccess( "--add-exports", fromClass, toModuleName, toPackageName, logLevel );
    }

    public static void expectDeepReflectiveAccess( Class<?> fromClass, String toModuleName, String toPackageName )
    {
        expectAccess( "--add-opens", fromClass, toModuleName, toPackageName, SEVERE );
    }

    public static void expectDeepReflectiveAccess( Class<?> fromClass, String toModuleName, String toPackageName, Level logLevel )
    {
        expectAccess( "--add-opens", fromClass, toModuleName, toPackageName, logLevel );
    }

    protected static void expectAccess( String jvmFlag, Class<?> fromClass, String toModuleName, String toPackageName, Level logLevel )
    {
        if ( jvmSupportsModules )
        {
            String fromModuleName = getModuleName( fromClass );
            String jvmArg = jvmFlag + "=" + toModuleName + "/" + toPackageName + "=" + ( fromModuleName != null ? fromModuleName : "ALL-UNNAMED" );
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

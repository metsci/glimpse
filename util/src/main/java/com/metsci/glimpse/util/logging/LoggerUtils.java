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
package com.metsci.glimpse.util.logging;

import static com.metsci.glimpse.util.io.StreamOpener.fileThenResourceOpener;
import static java.lang.String.format;
import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.logging.LogManager.getLogManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.metsci.glimpse.util.io.StreamOpener;
import com.metsci.glimpse.util.logging.format.Formatter;
import com.metsci.glimpse.util.logging.format.TimestampingMethodNameLogFormatter;

/**
 * @author moskowitz
 */
public class LoggerUtils
{

    /**
     * Convenience wrapper around {@link Logger#getLogger(String)}. Uses
     * the fully qualified classname of the specified class as the logger
     * name.
     */
    public static Logger getLogger( Class<?> clazz )
    {
        return Logger.getLogger( clazz.getName( ) );
    }

    /**
     * Traverses parents to find effective log level.
     */
    public static Level getLevelRecursive( Logger logger )
    {
        Level level = logger.getLevel( );
        while ( level == null )
        {
            logger = logger.getParent( );
            if ( logger != null )
            {
                level = logger.getLevel( );
            }
            else
            {
                return null;
            }
        }

        return level;
    }

    /**
     * Prints own and parents' log levels to standard out (for debugging).
     */
    public void dumpAncestry( Logger logger )
    {
        Logger loggerAncestor = logger;

        int nUp = 0;
        while ( loggerAncestor != null )
        {
            String name = loggerAncestor.getName( );
            if ( name.isEmpty( ) )
            {
                name = "<root>";
            }

            System.out.printf( "logger ancestor %d: %s  level=%s%n", nUp, name, loggerAncestor.getLevel( ) );
            loggerAncestor = loggerAncestor.getParent( );
            nUp++;
        }

        System.out.println( "recursive level = " + getLevelRecursive( logger ) );
    }

    /**
     * Initialize Java logging to use "logging.properties" as the configuration
     * file and re-read the logging configuration from this file.
     * <p>
     * Note: Similar to setting
     * -Djava.util.logging.config.file=logging.properties on java command line.
     * </p>
     */
    public static void initializeLogging( )
    {
        initializeLogging( "logging.properties" );
    }

    /**
     * Initialize Java logging to use given configuration file and re-read the
     * logging configuration from this file.
     * <p>
     * Note: Similar to setting
     * -Djava.util.logging.config.file=configurationFilename on java command
     * line.
     * </p>
     */
    public static void initializeLogging( String configurationFilename )
    {
        initializeLogging( configurationFilename, fileThenResourceOpener );
    }

    /**
     * Initialize Java logging to use given configuration file and re-read the
     * logging configuration from this file.
     * <p>
     * Note: Similar to setting
     * -Djava.util.logging.config.file=configurationFilename on java command
     * line.
     * </p>
     */
    public static void initializeLogging( String configurationFilename, StreamOpener streamOpener )
    {
        try
        {
            InputStream stream = null;
            try
            {
                stream = streamOpener.openForRead( configurationFilename );
                getLogManager( ).readConfiguration( stream );

                System.setProperty( "java.util.logging.config.file", configurationFilename );

                Logger logger = getLogger( LoggerUtils.class );
                logger.info( "Loaded logging configuration from " + configurationFilename );
            }
            finally
            {
                if ( stream != null ) stream.close( );
            }
        }
        catch ( IOException e )
        {
            System.err.println( LoggerUtils.class.getSimpleName( ) + ".initializeLogging: IO exception - " + e.toString( ) );
            e.printStackTrace( System.err );
        }
    }

    /**
     * In cases where a logging.properties file is too cumbersome, sets terse
     * formatter for console handler.
     *
     * @param level maximum logging level; set on root logger
     */
    public static final void setTerseConsoleLogger( Level level )
    {
        java.util.logging.Logger logger = Logger.getLogger( "" );

        if ( logger == null ) return;

        Handler[] handlers = logger.getHandlers( );
        for ( Handler h : handlers )
            if ( h instanceof ConsoleHandler ) logger.removeHandler( h );

        ConsoleHandler handler = new ConsoleHandler( )
        {
            Formatter formatter = new TimestampingMethodNameLogFormatter( );

            @Override
            public Formatter getFormatter( )
            {
                return formatter;
            }
        };

        handler.setLevel( level );
        logger.addHandler( handler );
    }

    /**
     * In cases where a logging.properties file is too cumbersome, sets terse
     * formatter for file handler.
     *
     * @param level maximum logging level; set on root logger
     * @throws IOException
     * @throws SecurityException
     */
    public static final void addTerseFileLogger( Level level, String filename ) throws SecurityException, IOException
    {
        java.util.logging.Logger logger = Logger.getLogger( "" );

        if ( logger == null ) return;

        FileHandler handler = new FileHandler( filename )
        {
            Formatter formatter = new TimestampingMethodNameLogFormatter( );

            @Override
            public Formatter getFormatter( )
            {
                return formatter;
            }
        };

        handler.setLevel( level );
        logger.addHandler( handler );
    }

    public static final void setLoggerLevel( final Level level )
    {
        java.util.logging.Logger logger = Logger.getLogger( "" );

        if ( logger == null ) return;

        logger.setLevel( level );
    }

    public static void sendStdoutToLog( )
    {
        Logger logger = Logger.getLogger( "stdout" );
        LoggingOutputStream los = new LoggingOutputStream( logger, StdOutErrLevel.STDOUT );
        System.setOut( new PrintStream( los, true ) );
    }

    public static void sendStderrToLog( )
    {
        Logger logger = Logger.getLogger( "stderr" );
        LoggingOutputStream los = new LoggingOutputStream( logger, StdOutErrLevel.STDERR );
        System.setErr( new PrintStream( los, true ) );
    }

    /**
     * An OutputStream that writes contents to a Logger upon each call to flush()
     *
     * Original URL: https://blogs.oracle.com/nickstephen/entry/java_redirecting_system_out_and
     * Author gives permission for free use in blog comments section.
     */
    public static class LoggingOutputStream extends ByteArrayOutputStream
    {
        private String lineSeparator;

        private Logger logger;
        private Level level;

        /**
         * Constructor
         *
         * @param logger Logger to write to
         * @param level Level at which to write the log message
         */
        public LoggingOutputStream( Logger logger, Level level )
        {
            super( );
            this.logger = logger;
            this.level = level;
            lineSeparator = System.getProperty( "line.separator" );
        }

        /**
         * upon flush() write the existing contents of the OutputStream to the
         * logger as a log record.
         *
         * @throws java.io.IOException in case of error
         */
        public void flush( ) throws IOException
        {

            String record;
            synchronized ( this )
            {
                super.flush( );
                record = this.toString( );
                super.reset( );

                if ( record.length( ) == 0 || record.equals( lineSeparator ) )
                {
                    // avoid empty records
                    return;
                }

                logger.logp( level, "", "", record );
            }
        }
    }

    /**
     * Class defining 2 new Logging levels, one for STDOUT, one for STDERR, used
     * when multiplexing STDOUT and STDERR into the same rolling log file via
     * the Java Logging APIs.<br><br>
     *
     * From: http://blogs.sun.com/nickstephen/entry/java_redirecting_system_out_and
     */
    public static class StdOutErrLevel extends Level
    {
        private static final long serialVersionUID = 2386782470168630460L;

        /**
         * Private constructor
         */
        private StdOutErrLevel( String name, int value )
        {
            super( name, value );
        }

        /**
         * Level for STDOUT activity.
         */
        public static Level STDOUT = new StdOutErrLevel( "STDOUT", Level.INFO.intValue( ) + 53 );

        /**
         * Level for STDERR activity
         */
        public static Level STDERR = new StdOutErrLevel( "STDERR", Level.INFO.intValue( ) + 54 );

        /**
         * Method to avoid creating duplicate instances when deserializing the
         * object.
         *
         * @return the singleton instance of this <code>Level</code> value in
         *         this classloader
         * @throws ObjectStreamException If unable to deserialize
         */
        protected Object readResolve( ) throws ObjectStreamException
        {
            if ( this.intValue( ) == STDOUT.intValue( ) ) return STDOUT;

            if ( this.intValue( ) == STDERR.intValue( ) ) return STDERR;

            throw new InvalidObjectException( "Unknown instance :" + this );
        }
    }

    ///////////////
    /////////////// Java logger call wrappers without throwable argument.
    ///////////////

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void log( Logger logger, Level level, String format, Object... args )
    {
        if ( logger.isLoggable( level ) )
        {
            logger.log( level, format( format, args ) );
        }
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logFinest( Logger logger, String format, Object... args )
    {
        log( logger, FINEST, format, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logFiner( Logger logger, String format, Object... args )
    {
        log( logger, FINER, format, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logFine( Logger logger, String format, Object... args )
    {
        log( logger, FINE, format, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logConfig( Logger logger, String format, Object... args )
    {
        log( logger, CONFIG, format, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logInfo( Logger logger, String format, Object... args )
    {
        log( logger, INFO, format, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logWarning( Logger logger, String format, Object... args )
    {
        log( logger, WARNING, format, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logSevere( Logger logger, String format, Object... args )
    {
        log( logger, SEVERE, format, args );
    }

    ///////////////
    /////////////// Java logger call wrappers with throwable argument.
    ///////////////

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void log( Logger logger, Level level, String format, Throwable thrown, Object... args )
    {
        if ( logger.isLoggable( level ) )
        {
            logger.log( level, format( format, args ), thrown );
        }
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logFinest( Logger logger, String format, Throwable thrown, Object... args )
    {
        log( logger, FINEST, format, thrown, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logFiner( Logger logger, String format, Throwable thrown, Object... args )
    {
        log( logger, FINER, format, thrown, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logFine( Logger logger, String format, Throwable thrown, Object... args )
    {
        log( logger, FINE, format, thrown, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logConfig( Logger logger, String format, Throwable thrown, Object... args )
    {
        log( logger, CONFIG, format, thrown, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logInfo( Logger logger, String format, Throwable thrown, Object... args )
    {
        log( logger, INFO, format, thrown, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logWarning( Logger logger, String format, Throwable thrown, Object... args )
    {
        log( logger, WARNING, format, thrown, args );
    }

    /**
     * Wraps call to Java logger with varargs and performance optimization: no argument formatting
     * if unneeded.
     */
    public static void logSevere( Logger logger, String format, Throwable thrown, Object... args )
    {
        log( logger, SEVERE, format, thrown, args );
    }
}

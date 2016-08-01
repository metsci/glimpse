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
package com.metsci.glimpse.docking;

import static java.util.logging.Level.WARNING;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class AppConfigUtils
{
    private static final Logger logger = Logger.getLogger( AppConfigUtils.class.getName( ) );


    /**
     * Save the specified config object to an XML file in the application's config directory
     * (see {@link AppConfigUtils#createAppDir(String)}.
     * <p>
     * Every class that could possibly be serialized, including the class of the config object,
     * the classes of its fields, and any relevant subclasses, should be included in {@code dataClasses}.
     * <p>
     * In many cases it is helpful to have a separate package of classes just for serialization.
     * Notably, this makes it easier to avoid accidentally changing the serialization format
     * (e.g. by renaming a class or package). See the {@code com.metsci.glimpse.docking.xml}
     * package (including its {@code package-info.java}) for an example.
     * <p>
     * <strong>NOTE:</strong> This function catches exceptions and logs them, instead of letting
     * them propagate up the call stack. Ordinarily this would be a terrible idea, but for this
     * function, terse usage is more important than careful error handling.
     */
    public static void saveAppConfig( String appName, String filename, Object config, Collection<Class<?>> dataClasses )
    {
        try
        {
            File file = new File( createAppDir( appName ), filename );
            newMarshaller( dataClasses ).marshal( config, file );
        }
        catch ( Exception e )
        {
            logger.log( WARNING, "Failed to write app config to file: app-name = " + appName + ", filename = " + filename, e );
        }
    }

    /**
     * Load a config object from the specified XML file in the application's config directory
     * (see {@link AppConfigUtils#createAppDir(String)}. If that fails, load the config object
     * from {@code fallbackUrl} (if it is non-null).
     * <p>
     * Every class that could possibly be deserialized, including the class of the config object,
     * the classes of its fields, and any relevant subclasses, should be included in {@code dataClasses}.
     * <p>
     * In many cases it is helpful to have a separate package of classes just for serialization.
     * Notably, this makes it easier to avoid accidentally changing the serialization format
     * (e.g. by renaming a class or package). See the {@code com.metsci.glimpse.docking.xml}
     * package (including its {@code package-info.java}) for an example.
     * <p>
     * <strong>NOTE:</strong> This function catches exceptions and logs them, instead of letting
     * them propagate up the call stack. Ordinarily this would be a terrible idea, but for this
     * function, terse usage is more important than careful error handling.
     */
    public static <T> T loadAppConfig( String appName, String filename, URL fallbackUrl, Class<T> configClass, Collection<Class<?>> dataClasses )
    {
        try
        {
            File file = new File( createAppDir( appName ), filename );
            if ( file.exists( ) )
            {
                Object unmarshalled = newUnmarshaller( dataClasses ).unmarshal( file );
                return castUnmarshalled( unmarshalled, configClass );
            }
        }
        catch ( Exception e )
        {
            logger.log( WARNING, "Failed to load application config from file: app-name = " + appName + ", filename = " + filename, e );
        }

        if ( fallbackUrl != null )
        {
            InputStream fallbackStream = null;
            try
            {
                fallbackStream = fallbackUrl.openStream( );
                Object unmarshalled = newUnmarshaller( dataClasses ).unmarshal( fallbackStream );
                return castUnmarshalled( unmarshalled, configClass );
            }
            catch ( Exception e )
            {
                logger.log( WARNING, "Failed to load fallback application config from resource: resource = " + fallbackUrl.toString( ), e );
            }
            finally
            {
                if ( fallbackStream != null )
                {
                    try
                    {
                        fallbackStream.close( );
                    }
                    catch ( IOException e )
                    {
                        logger.log( WARNING, "Failed to close fallback application config resource: resource = " + fallbackUrl.toString( ), e );
                    }
                }
            }
        }

        return null;
    }

    public static File createAppDir( String appName )
    {
        String homePath = System.getProperty( "user.home" );
        if ( homePath == null ) throw new RuntimeException( "Property user.home is not defined" );

        File appDir = new File( homePath, "." + appName );
        appDir.mkdirs( );

        if ( !appDir.isDirectory( ) ) throw new RuntimeException( "Failed to create app dir: " + appDir.getAbsolutePath( ) );
        if ( !appDir.canRead( ) ) throw new RuntimeException( "Do not have read permission on app dir: " + appDir.getAbsolutePath( ) );
        if ( !appDir.canWrite( ) ) throw new RuntimeException( "Do not have write permission on app dir: " + appDir.getAbsolutePath( ) );

        return appDir;
    }

    public static Marshaller newMarshaller( Collection<Class<?>> dataClasses ) throws IOException, JAXBException
    {
        Class<?>[] classes = dataClasses.toArray( new Class[ 0 ] );
        Marshaller marshaller = JAXBContext.newInstance( classes ).createMarshaller( );
        marshaller.setProperty( JAXB_FORMATTED_OUTPUT, true );
        return marshaller;
    }

    public static Unmarshaller newUnmarshaller( Collection<Class<?>> dataClasses ) throws JAXBException, IOException
    {
        Class<?>[] classes = dataClasses.toArray( new Class[ 0 ] );
        Unmarshaller unmarshaller = JAXBContext.newInstance( classes ).createUnmarshaller( );
        return unmarshaller;
    }

    public static <T> T castUnmarshalled( Object unmarshalled, Class<T> clazz )
    {
        if ( clazz.isInstance( unmarshalled ) )
        {
            return clazz.cast( unmarshalled );
        }
        else if ( unmarshalled instanceof JAXBElement )
        {
            return castUnmarshalled( ( ( JAXBElement<?> ) unmarshalled ).getValue( ), clazz );
        }
        else
        {
            throw new ClassCastException( "Unmarshalled object is neither a " + clazz.getName( ) + " nor a " + JAXBElement.class.getName( ) + ": classname = " + unmarshalled.getClass( ).getName( ) );
        }
    }

}

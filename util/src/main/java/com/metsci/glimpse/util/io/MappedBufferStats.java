package com.metsci.glimpse.util.io;

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.lang.reflect.Field;
import java.util.logging.Logger;

public class MappedBufferStats
{
    private static final Logger logger = getLogger( MappedBufferStats.class );


    protected static class UnmapperAccess
    {
        public final Class<?> clazz;

        /**
         * int
         */
        public final Field count;

        /**
         * long
         */
        public final Field totalSize;

        /**
         * long
         */
        public final Field totalCapacity;

        public UnmapperAccess( ) throws Exception
        {
            this.clazz = Class.forName( "sun.nio.ch.FileChannelImpl$Unmapper" );

            this.count = this.clazz.getDeclaredField( "count" );
            this.count.setAccessible( true );

            this.totalSize = this.clazz.getDeclaredField( "totalSize" );
            this.totalSize.setAccessible( true );

            this.totalCapacity = this.clazz.getDeclaredField( "totalCapacity" );
            this.totalCapacity.setAccessible( true );
        }
    }

    protected static final UnmapperAccess access = getUnmapperAccess( );
    protected static UnmapperAccess getUnmapperAccess( )
    {
        try
        {
            return new UnmapperAccess( );
        }
        catch ( Exception e )
        {
            logWarning( logger, "MappedBuffer stats may not reflect custom-mapped instances" );
            return null;
        }
    }

    public static void addToMappedBufferStats( int count, long size, long capacity )
    {
        if ( access != null )
        {
            try
            {
                synchronized ( access.clazz )
                {
                    int oldCount = access.count.getInt( null );
                    long oldTotalSize = access.totalSize.getLong( null );
                    long oldTotalCapacity = access.totalCapacity.getLong( null );

                    access.count.setInt( null, oldCount + count );
                    access.totalSize.setLong( null, oldTotalSize + size );
                    access.totalCapacity.setLong( null, oldTotalCapacity + capacity );
                }
            }
            catch ( Exception e )
            { }
        }
    }

}

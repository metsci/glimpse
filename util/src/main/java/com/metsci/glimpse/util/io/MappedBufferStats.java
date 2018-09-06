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

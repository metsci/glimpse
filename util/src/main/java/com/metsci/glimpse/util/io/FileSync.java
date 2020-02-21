/*
 * Copyright (c) 2020, Metron, Inc.
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

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class FileSync
{

    private static class FileEntry
    {
        public final ReentrantLock lock;
        public final FileChannel fileChannel;
        public FileLock fileLock;

        public FileEntry( FileChannel fileChannel )
        {
            this.lock = new ReentrantLock( );
            this.fileChannel = fileChannel;
            this.fileLock = null;
        }
    }


    private static final Map<File,FileSync.FileEntry> fileEntries = new HashMap<>( );


    public static void lockFile( File file ) throws IOException
    {
        FileSync.FileEntry entry;
        synchronized ( fileEntries )
        {
            if ( !fileEntries.containsKey( file ) )
            {
                // This approach keeps all lock files open until the JVM shuts down.
                // This is much simpler than trying to close files, because it gets
                // complicated to keep locks in valid states through all exception-
                // handling code paths.
                //
                // This approach works fine for DNC purposes, where we only sync on
                // one file. However, for more general-purpose uses, keeping files
                // open forever would not be a good idea.
                //

                FileChannel fileChannel = FileChannel.open( file.toPath( ), READ, WRITE, CREATE );
                fileEntries.put( file, new FileEntry( fileChannel ) );
            }
            entry = fileEntries.get( file );
        }

        if ( !entry.lock.isHeldByCurrentThread( ) )
        {
            entry.lock.lock( );
            try
            {
                while ( true )
                {
                    try
                    {
                        entry.fileLock = entry.fileChannel.lock( );
                        break;
                    }
                    catch ( OverlappingFileLockException e )
                    {
                        // Another thread in the same JVM but with a different classloader
                        // may have the file locked, without holding entry.lock -- in which
                        // case, our lock attempt will throw an exception. The only way to
                        // deal with this, AFAICT, is to spin.
                        //
                        try { Thread.sleep( 10 ); } catch ( InterruptedException e2 ) { }
                    }
                }
            }
            finally
            {
                if ( entry.fileLock == null )
                {
                    entry.lock.unlock( );
                }
            }
        }
    }


    public static void unlockFile( File file ) throws IOException
    {
        FileSync.FileEntry entry;
        synchronized ( fileEntries )
        {
            entry = fileEntries.get( file );
        }

        if ( entry.lock.isHeldByCurrentThread( ) )
        {
            entry.fileLock.release( );
            entry.fileLock = null;
            entry.lock.unlock( );
        }
    }

}
package com.metsci.glimpse.util.examples;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;

import com.metsci.glimpse.util.io.MappedFile;

public class MappedFileExample
{

    public static void main( String[] args ) throws Exception
    {
        long GB = 1024L * 1024L * 1024L;

        File file = new File( "test.dat" );
        System.err.println( file.getAbsolutePath( ) );
        MappedFile mapped = new MappedFile( file, ByteOrder.nativeOrder( ), 5*GB );

        MappedByteBuffer slice = mapped.slice( 3*GB, 1 );

        byte r0 = slice.get( 0 );
        System.err.println( "Read: " + r0 );

        byte w1 = ( byte ) 111;
        slice.put( 0, w1 );
        System.err.println( "Wrote: " + w1 );

        byte r1 = slice.get( 0 );
        System.err.println( "Read: " + r1 );

        byte w2 = ( byte ) 55;
        slice.put( 0, w2 );
        System.err.println( "Wrote: " + w2 );

        byte r2 = slice.get( 0 );
        System.err.println( "Read: " + r2 );

        mapped.dispose( );
    }

}

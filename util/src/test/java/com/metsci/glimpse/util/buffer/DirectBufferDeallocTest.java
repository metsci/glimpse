package com.metsci.glimpse.util.buffer;

import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.deallocateDirectBuffer;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

public class DirectBufferDeallocTest
{

    @Test
    void directBufferDeallocShouldBasicallyWork( )
    {
        var buffer = ByteBuffer.allocateDirect( 12345 );
        deallocateDirectBuffer( buffer );
    }

}

package com.metsci.glimpse.util.buffer;

import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.deallocateDirectBuffer;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.junit.jupiter.api.Test;

public class DirectBufferDeallocTest
{

    @Test
    void directBufferDeallocShouldNotBeANoop( )
    {
        assertFalse( DirectBufferDealloc.impl instanceof DirectBufferDealloc.NoopImpl, "DirectBufferDealloc impl is a NOOP" );
    }

    @Test
    void directBufferDeallocShouldNotThrow( )
    {
        FloatBuffer buffer = ByteBuffer.allocateDirect( 100 * Float.SIZE ).asFloatBuffer( );
        deallocateDirectBuffer( buffer );
    }

}

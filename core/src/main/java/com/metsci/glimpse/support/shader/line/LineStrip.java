package com.metsci.glimpse.support.shader.line;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;
import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES3;

import com.metsci.glimpse.gl.DirtyIndexSet;
import com.metsci.glimpse.gl.GLEditableBuffer;
import com.metsci.glimpse.util.buffer.DirectBufferUtils;
import com.metsci.glimpse.util.primitives.sorted.SortedInts;

public class LineStrip
{

    public static class DeviceBuffers
    {
        public final int xyBuffer;
        public final int flagsBuffer;
        public final int mileageBuffer;

        public DeviceBuffers( int xyBuffer, int flagsBuffer, int mileageBuffer )
        {
            this.xyBuffer = xyBuffer;
            this.flagsBuffer = flagsBuffer;
            this.mileageBuffer = mileageBuffer;
        }
    }


    protected FloatBuffer hXy;
    protected ByteBuffer hFlags;
    protected FloatBuffer hMileage;

    protected final GLEditableBuffer dXy;
    protected final GLEditableBuffer dFlags;
    protected final GLEditableBuffer dMileage;

    protected final DirtyIndexSet dirtyRanges;


    public LineStrip( int initialVertices )
    {
        this( initialVertices, 10 );
    }

    public LineStrip( int initialVertices, int scratchBlockSizeFactor )
    {
        // Make room for leading and trailing phantom vertices
        initialVertices += 2;

        this.hXy = newDirectFloatBuffer( 2 * initialVertices );
        this.hFlags = newDirectByteBuffer( 1 * initialVertices );
        this.hMileage = newDirectFloatBuffer( 1 * initialVertices );

        this.dXy = new GLEditableBuffer( GL_ARRAY_BUFFER, 2 * initialVertices * SIZEOF_FLOAT, scratchBlockSizeFactor );
        this.dFlags = new GLEditableBuffer( GL_ARRAY_BUFFER, 1 * initialVertices * SIZEOF_BYTE, scratchBlockSizeFactor );
        this.dMileage = new GLEditableBuffer( GL_ARRAY_BUFFER, 1 * initialVertices * SIZEOF_FLOAT, scratchBlockSizeFactor );

        this.dirtyRanges = new DirtyIndexSet( );
    }

    public void grow( int additionalVertices )
    {
        this.hXy = ensureAdditionalCapacity( this.hXy, 2 * additionalVertices, true );
        this.hFlags = ensureAdditionalCapacity( this.hFlags, 1 * additionalVertices, true );
        this.hMileage = ensureAdditionalCapacity( this.hMileage, 1 * additionalVertices, true );
    }

    public void append( FloatBuffer xys )
    {
        int firstVertex = this.hFlags.position( );
        int numVertices = xys.remaining( ) / 2;
        this.dirtyRanges.add( this.hFlags.position( ), numVertices );

        this.grow( numVertices );

        this.hXy.position(  );

        this.hXy.put( xys );
        this.hFlags.put( b );
        this.hMileage.put( f );
    }

    public void set( int firstVertex, FloatBuffer xys )
    {
        int numVertices = xys.remaining( ) / 2;
        this.dirtyRanges.add( firstVertex, numVertices );

        FloatBuffer hXy2 = this.hXy.duplicate( );
        hXy2.position( 2 * firstVertex );
        hXy2.put( xys );

        FloatBuffer hMileage2 = this.hMileage.duplicate( );
        hMileage2.position( 1 * firstVertex );
        hMileage2.put( f );
    }

    public void set( int firstVertex, float[] xys )
    {
        this.set( firstVertex, xys, 0, xys.length );
    }

    public void set( int firstVertex, float[] xys, int position, int limit )
    {
        int numVertices = ( limit - position ) / 2;
        this.dirtyRanges.add( firstVertex, numVertices );

        FloatBuffer hXy2 = this.hXy.duplicate( );
        hXy2.position( 2 * firstVertex );
        hXy2.put( xys );

        FloatBuffer hMileage2 = this.hMileage.duplicate( );
        hMileage2.position( 1 * firstVertex );
        hMileage2.put( f );
    }

    public int numVertices( )
    {
        return this.hFlags.position( );
    }

    public DeviceBuffers buffers( GL2ES3 gl )
    {
        this.dXy.ensureCapacity( gl, this.hXy.position( ) * SIZEOF_FLOAT );
        this.dFlags.ensureCapacity( gl, this.hFlags.position( ) * SIZEOF_BYTE );
        this.dMileage.ensureCapacity( gl, this.hMileage.position( ) * SIZEOF_FLOAT );

        // XXX: Higher tolerance might be better
        this.dirtyRanges.coalesce( 256 );

        SortedInts ranges = this.dirtyRanges.ranges( );
        for ( int i = 0; i < ranges.n( ); i += 2 )
        {

            int hStart = ranges.v( i + 0 );
            int hEnd = ranges.v( i + 1 );

            FloatBuffer hXyRange = sliced( this.hXy, 2*hStart, 2*hEnd );
            ByteBuffer hFlagsRange = sliced( this.hFlags, 1*hStart, 1*hEnd );
            FloatBuffer hMileageRange = sliced( this.hMileage, 1*hStart, 1*hEnd );


            int dStart = asdf;
            int dEnd = asdf;

            FloatBuffer dXyRange = this.dXy.mapFloats( gl, 2*dStart, 2*dEnd );
            ByteBuffer dFlagsRange = this.dFlags.mapBytes( gl, 1*dStart, 1*dEnd );
            FloatBuffer dMileageRange = this.dMileage.mapFloats( gl, 1*dStart, 1*dEnd );


            if ( hStart == 0 )
            {
                dXyRange.put( f ).put( f );
                dFlagsRange.put( b );
                dMileageRange.put( f );
            }

            dXyRange.put( hXyRange );
            dFlagsRange.put( hFlagsRange );
            dMileageRange.put( hMileageRange );

            if ( asdf )
            {
                dXyRange.put( f ).put( f );
                dFlagsRange.put( b );
                dMileageRange.put( f );
            }

        }

        this.dirtyRanges.clear( );

        return new DeviceBuffers( this.dXy.buffer( gl ), this.dFlags.buffer( gl ), this.dMileage.buffer( gl ) );
    }

    public void dispose( GL gl )
    {
        deallocateDirectBuffers( this.hXy, this.hFlags, this.hMileage );
        this.hXy = null;
        this.hFlags = null;
        this.hMileage = null;

        this.dXy.dispose( gl );
        this.dFlags.dispose( gl );
        this.dMileage.dispose( gl );

        this.dirtyRanges.clear( );
    }

}

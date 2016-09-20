package com.metsci.glimpse.support.line;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.support.line.LineUtils.*;
import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class LinePathData
{

    /**
     * Mask for the CONNECT bit, which indicates whether to draw a line segment
     * from the previous vertex to the current vertex.
     */
    public static final byte FLAGS_CONNECT = 1 << 0;

    /**
     * Mask for the JOIN bit, which indicates whether to use a join (e.g. miter)
     * at the current vertex.
     */
    public static final byte FLAGS_JOIN = 1 << 1;


    /**
     * The index of the first vertex in the current line-strip. Assigned a new
     * value when {@link #moveTo(float, float, float)} is called. Set to -1 when
     * when {@link #closeLoop()} is called.
     * <p>
     * Note the "first" vertex actually comes after the leading phantom vertex.
     */
    protected int stripFirst;

    /**
     * Contains two floats (x and y) for each vertex, indicating the position of
     * the vertex.
     */
    protected FloatBuffer xyBuffer;

    /**
     * Contains one byte for each vertex, containing bit-flags:
     * <ul>
     * <li>Bit 0: CONNECT  (Least Significant Bit)
     * <li>Bit 1: JOIN
     * </ul>
     * <p>
     * Example vertex flags for a line-strip:
     * <pre>
     *   Vertex:   (   [----*----*----]
     *   Flags:    0   0   CJ   CJ    C </pre>
     * And for a loop:
     * <pre>
     *   Vertex:   (   [----*----*----]   )
     *   Flags:    0   J   CJ   CJ   CJ   0 </pre>
     * Where:
     * <pre>
     *   ( = Leading Phantom Vertex
     *   [ = Strip First Vertex
     *   * = Normal Vertex
     *   ] = Strip Last Vertex
     *   ) = Trailing Phantom Vertex </pre>
     * The loop differs from the strip in the following ways:
     * <ul>
     * <li>The loop's first and last vertices have their JOIN flags set
     * <li>The loop's leading phantom vertex will have the same position as the loop's last vertex
     * <li>The loop has a trailing phantom vertex, with the same position as the loop's first vertex
     * <ul>
     */
    protected ByteBuffer flagsBuffer;

    /**
     * Contains one float for each vertex, indicating the cumulative distance
     * to the vertex from the start of the connected line strip.
     * <p>
     * Mileage is dependent on ppv-aspect-ratio. The ppv-aspect-ratio that was
     * used to compute the current mileage values is stored in {@link #mileagePpvAspectRatio}.
     * <p>
     * Only needed when stippling is enabled, and can be expensive to compute.
     * Therefore, values are not updated until specifically requested by calling
     * {@link #updateMileage(double, double)}.
     * <p>
     * A vertex that starts a new line strip will have an initial-mileage value.
     * These initial-mileages are stored in this buffer -- they are <em>read</em>,
     * rather than written, by {@link #updateMileage(double, double)}.
     */
    protected FloatBuffer mileageBuffer;

    /**
     * The first index at which mileage values need to be updated.
     */
    protected int mileageValidCount;

    /**
     * The ppv-aspect-ratio used to populate the first {@link #mileageValidCount}
     * values of {@link #mileageBuffer}.
     */
    protected double mileagePpvAspectRatio;


    public LinePathData( int initialNumVertices )
    {
        this.stripFirst = -1;

        this.xyBuffer = newDirectFloatBuffer( 2*initialNumVertices );
        this.flagsBuffer = newDirectByteBuffer( 1*initialNumVertices );
        this.mileageBuffer = newDirectFloatBuffer( 1*initialNumVertices );

        this.mileageValidCount = 0;
        this.mileagePpvAspectRatio = Double.NaN;
    }

    public void moveTo( float x, float y, float stripInitialMileage )
    {
        // Rewrite flags of previous vertex, to end previous strip
        int stripLast = this.flagsBuffer.position( ) - 1;
        if ( stripLast >= 0 )
        {
            byte flagsLast = this.flagsBuffer.get( 1*stripLast );
            this.flagsBuffer.put( 1*stripLast, ( byte ) ( flagsLast & ~FLAGS_JOIN ) );
        }


        // Leading phantom vertex, in case this strip is at the start of the VBO,
        // or turns out to be a loop
        this.appendVertex( x, y, 0, stripInitialMileage );

        // Index of first vertex in strip
        this.stripFirst = this.flagsBuffer.position( );

        // First vertex in strip
        this.appendVertex( x, y, 0, stripInitialMileage );
    }

    public void lineTo( float x, float y )
    {
        if ( this.stripFirst < 0 )
        {
            throw new RuntimeException( "No current line-strip -- moveTo() must be called after instantiation, and after each call to closeLoop()" );
        }

        this.appendVertex( x, y, FLAGS_CONNECT | FLAGS_JOIN );
    }

    /**
     * After calling this method, client code must next call {@link #moveTo(float, float, float)},
     * before calling either {@link #lineTo(float, float)} or {@link #closeLoop()} again.
     */
    public void closeLoop( )
    {
        if ( this.stripFirst < 0 )
        {
            throw new RuntimeException( "No current line-strip -- moveTo() must be called after instantiation, and after each call to closeLoop()" );
        }


        // Read last vertex in strip
        int stripLast = this.flagsBuffer.position( ) - 1;
        float xLast = this.xyBuffer.get( 2*stripLast + 0 );
        float yLast = this.xyBuffer.get( 2*stripLast + 1 );

        // Rewrite position of leading phantom vertex
        int stripLeader = this.stripFirst - 1;
        this.xyBuffer.put( 2*stripLeader + 0, xLast );
        this.xyBuffer.put( 2*stripLeader + 1, yLast );

        // Rewrite flags of first vertex in strip
        this.flagsBuffer.put( 1*stripFirst, FLAGS_JOIN );


        // Append loop-closing vertex
        float xFirst = this.xyBuffer.get( 2*stripFirst + 0 );
        float yFirst = this.xyBuffer.get( 2*stripFirst + 1 );
        this.appendVertex( xFirst, yFirst, FLAGS_CONNECT | FLAGS_JOIN );

        // Append trailing phantom vertex
        float xSecond = this.xyBuffer.get( 2*stripFirst + 2 );
        float ySecond = this.xyBuffer.get( 2*stripFirst + 3 );
        this.appendVertex( xSecond, ySecond, 0 );


        // Next vertex must start a new strip
        this.stripFirst = -1;
    }

    protected void appendVertex( float x, float y, int flags )
    {
        // Use a placeholder for mileage -- the first vertex in each strip should be
        // given a real mileage, but others will be computed later in updateMileage
        this.appendVertex( x, y, flags, 0f );
    }

    protected void appendVertex( float x, float y, int flags, float mileage )
    {
        this.xyBuffer = grow2f( this.xyBuffer, x, y );
        this.flagsBuffer = grow1b( this.flagsBuffer, ( byte ) flags );
        this.mileageBuffer = grow1f( this.mileageBuffer, mileage );
    }

    public void clear( )
    {
        this.stripFirst = -1;

        this.xyBuffer.clear( );
        this.flagsBuffer.clear( );
        this.mileageBuffer.clear( );

        this.mileageValidCount = 0;
        this.mileagePpvAspectRatio = Double.NaN;
    }

    /**
     * Deallocates buffers.
     * <p>
     * <em>This object must not be used again after this method has been called.</em>
     */
    public void dispose( )
    {
        deallocateDirectBuffers( this.xyBuffer, this.flagsBuffer, this.mileageBuffer );
        this.xyBuffer = null;
        this.flagsBuffer = null;
        this.mileageBuffer = null;
    }

    public int numVertices( )
    {
        return this.flagsBuffer.position( );
    }

    public FloatBuffer xyBuffer( )
    {
        return flipped( this.xyBuffer );
    }

    public ByteBuffer flagsBuffer( )
    {
        return flipped( this.flagsBuffer );
    }

    public FloatBuffer mileageBuffer( )
    {
        return flipped( this.mileageBuffer );
    }

    public int updateMileage( double ppvAspectRatio, double ppvAspectRatioChangeThreshold )
    {
        // If any relevant values are NaN, all inequalities will return false, so keepPpvAspectRatio will be false
        boolean keepPpvAspectRatio = ( mileagePpvAspectRatio / ppvAspectRatioChangeThreshold <= ppvAspectRatio && ppvAspectRatio <= mileagePpvAspectRatio * ppvAspectRatioChangeThreshold );
        if ( !keepPpvAspectRatio )
        {
            this.mileageValidCount = 0;
            this.mileagePpvAspectRatio = ppvAspectRatio;
        }

        // Prepare to read xy, starting at index mileageValidCount
        FloatBuffer xyReadable = flipped( this.xyBuffer );
        xyReadable.position( 2*mileageValidCount );

        // Prepare to read flags, starting at index mileageValidCount
        ByteBuffer flagsReadable = flipped( this.flagsBuffer );
        flagsReadable.position( 1*mileageValidCount );

        // Prepare to write mileage, starting at index mileageValidCount
        this.mileageBuffer.position( mileageValidCount );


        float x;
        float y;
        float mileage;

        if ( this.mileageValidCount > 0 )
        {
            // If we're starting partway through, initialize loop vars based on last valid values
            int i = this.mileageValidCount - 1;
            x = xyReadable.get( 2*i + 0 );
            y = xyReadable.get( 2*i + 1 );
            mileage = this.mileageBuffer.get( 1*i );
        }
        else
        {
            // If we're starting from the beginning, the first vertex must be non-connected,
            // so loop vars' initial values don't matter
            x = 0;
            y = 0;
            mileage = 0;
        }

        while ( flagsReadable.hasRemaining( ) )
        {
            float xNew = xyReadable.get( );
            float yNew = xyReadable.get( );
            byte flagsNew = flagsReadable.get( );

            boolean connect = ( ( flagsNew & FLAGS_CONNECT ) != 0 );
            if ( !connect )
            {
                // The intention is to run this block if the vertex starts a new strip -- because
                // such a vertex has an initial-mileage value. However, this block will also run
                // if the vertex is the trailing phantom vertex after a loop (because CONNECT will
                // be false). Such a vertex will have only a placeholder mileage. This works out
                // okay, though, because the shader doesn't use mileage when CONNECT is false.

                // The value in mileageBuffer here is the strip's initial mileage, so get the
                // exsting value instead of putting a new one
                mileage = this.mileageBuffer.get( );
            }
            else
            {
                // Use the old ppv-aspect-ratio -- NOT the new one passed in -- so that all values
                // in the mileage buffer were computed with exactly the same ppv-aspect-ratio
                mileage += distance( x, y, xNew, yNew, this.mileagePpvAspectRatio );
                this.mileageBuffer.put( ( float ) mileage );
            }

            x = xNew;
            y = yNew;
        }

        int oldValidCount = this.mileageValidCount;
        this.mileageValidCount = this.mileageBuffer.position( );
        return ( this.mileageValidCount - oldValidCount );
    }

}

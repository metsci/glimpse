package com.metsci.glimpse.support.line;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.support.line.LineUtils.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class LinePathData
{

    /**
     * WIP
     */
    public static final byte FLAGS_CONNECT = 1 << 0;

    /**
     * WIP
     */
    public static final byte FLAGS_JOIN = 1 << 1;


    /**
     * WIP
     */
    protected int stripFirst;

    /**
     * WIP
     */
    protected FloatBuffer xyBuffer;

    /**
     * WIP: Contains one byte for each vertex.
     */
    protected ByteBuffer flagsBuffer;

    /**
     * WIP
     *
     * Contains one float for each vertex, indicating the cumulative distance
     * to the vertex from the start of the connected line strip.
     *
     * Mileage is dependent on ppv-aspect-ratio. The ppv-aspect-ratio used to
     * compute the current mileages is stored in {@link #mileagePpvAspectRatio}.
     *
     * Only needed when stippling is enabled, and can be expensive to compute.
     * Therefore, values are not updated until specifically requested. <em>Values
     * starting at {@link FloatBuffer#position()} are undefined.</em>
     */
    protected FloatBuffer mileageBuffer;

    /**
     * WIP
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
            throw new RuntimeException( "No current line-strip" );
        }

        this.appendVertex( x, y, FLAGS_CONNECT | FLAGS_JOIN );
    }

    public void closeLoop( )
    {
        if ( this.stripFirst < 0 )
        {
            throw new RuntimeException( "No current line-strip" );
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

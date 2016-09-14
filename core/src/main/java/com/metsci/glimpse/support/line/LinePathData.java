package com.metsci.glimpse.support.line;

import static com.jogamp.common.nio.Buffers.*;
import static com.metsci.glimpse.support.line.LineUtils.*;
import static com.metsci.glimpse.util.buffer.DirectBufferUtils.*;
import static java.lang.Math.*;

import java.nio.FloatBuffer;

public class LinePathData
{

    protected FloatBuffer xyBuffer;

    /**
     * Contains one float for each vertex:
     * <ul>
     * <li>0.0f = do not connect this vertex to previous
     * <li>1.0f = connect this vertex to previous
     * <ul>
     * Can be used in place of mileageBuffer, when stippling is disabled.
     */
    protected FloatBuffer connectBuffer;

    /**
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
     * The ppv-aspect-ratio last used to populate {@link #mileageBuffer}.
     */
    protected double mileagePpvAspectRatio;


    public LinePathData( int initialNumVertices )
    {
        this.xyBuffer = newDirectFloatBuffer( 2*initialNumVertices );
        this.connectBuffer = newDirectFloatBuffer( 1*initialNumVertices );
        this.mileageBuffer = newDirectFloatBuffer( 1*initialNumVertices );
        this.mileagePpvAspectRatio = Double.NaN;
    }

    public void addVertex( float x, float y, boolean connect )
    {
        this.xyBuffer = ensureAdditionalCapacity( xyBuffer, 2, true );
        xyBuffer.put( x ).put( y );

        this.connectBuffer = ensureAdditionalCapacity( connectBuffer, 1, true );
        connectBuffer.put( connect ? 1f : 0f );
    }

    public void clear( )
    {
        xyBuffer.clear( );
        connectBuffer.clear( );
        mileageBuffer.clear( );
        this.mileagePpvAspectRatio = Double.NaN;
    }

    public int numVertices( )
    {
        return connectBuffer.position( );
    }

    public FloatBuffer xyBuffer( )
    {
        return flipped( xyBuffer );
    }

    public FloatBuffer connectBuffer( )
    {
        return flipped( connectBuffer );
    }

    public FloatBuffer mileageBuffer( )
    {
        return flipped( mileageBuffer );
    }

    public int updateMileage( double ppvAspectRatio, double ppvAspectRatioChangeThreshold )
    {
        // If any relevant values are NaN, all inequalities will return false, so keepPpvAspectRatio will be false
        boolean keepPpvAspectRatio = ( mileagePpvAspectRatio / ppvAspectRatioChangeThreshold <= ppvAspectRatio && ppvAspectRatio <= mileagePpvAspectRatio * ppvAspectRatioChangeThreshold );
        if ( !keepPpvAspectRatio )
        {
            mileageBuffer.clear( );
            this.mileagePpvAspectRatio = ppvAspectRatio;
        }

        int iFirst = mileageBuffer.position( );
        int iCount = connectBuffer.position( ) - iFirst;
        this.mileageBuffer = ensureAdditionalCapacity( mileageBuffer, iCount, true );

        FloatBuffer xyReadable = flipped( xyBuffer );
        xyReadable.position( 2*iFirst );

        FloatBuffer connectReadable = flipped( connectBuffer );
        connectReadable.position( 1*iFirst );

        int i = max( 0, iFirst - 1 );
        float x = xyReadable.get( 2*i + 0 );
        float y = xyReadable.get( 2*i + 1 );
        double mileage = mileageBuffer.get( i );

        while ( connectReadable.hasRemaining( ) )
        {
            float xNew = xyReadable.get( );
            float yNew = xyReadable.get( );
            float connectNew = connectReadable.get( );

            if ( connectNew == 0f )
            {
                mileage = 0.0;
            }
            else
            {
                // Use the old ppv-aspect-ratio -- NOT the new one passed in -- so that all values
                // in the mileage buffer were computed with exactly the same ppv-aspect-ratio
                mileage += distance( x, y, xNew, yNew, this.mileagePpvAspectRatio );
            }
            x = xNew;
            y = yNew;

            mileageBuffer.put( ( float ) mileage );
        }

        return iCount;
    }

}

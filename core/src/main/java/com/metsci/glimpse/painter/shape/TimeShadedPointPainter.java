package com.metsci.glimpse.painter.shape;

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer;
import com.metsci.glimpse.gl.attribute.GLFloatBuffer2D;
import com.metsci.glimpse.gl.shader.Pipeline;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * A Scatterplot point painter which associates a time with each data point. A subset of
 * the points can be displayed based on these time values.
 * 
 * @author ulman
 */
public class TimeShadedPointPainter extends ShadedPointPainter
{
    protected FloatBuffer timeAttributeBuffer;

    protected float startTime = Float.NEGATIVE_INFINITY;
    protected float endTime = Float.POSITIVE_INFINITY;

    protected int startIndex;
    protected int endIndex;

    public TimeShadedPointPainter( Axis1D colorAxis, Axis1D sizeAxis, Pipeline pipeline ) throws IOException
    {
        super( colorAxis, sizeAxis, pipeline );
    }

    public TimeShadedPointPainter( Axis1D colorAxis, Axis1D sizeAxis ) throws IOException
    {
        super( colorAxis, sizeAxis );
    }
    
    /**
     * Sets the selected range of times which will be displayed by this painter. The Epoch
     * argument is used to convert from absolute TimeStamps to relative times. It is generally
     * provided by a {@ StackedTimePlot2D}. The same Epoch should be used to fill the point
     * time values in the data buffer passed to {@code #useTimeAttribData(FloatBuffer)}.
     */
    public void displayTimeRange( Epoch epoch, TimeStamp startTime, TimeStamp endTime )
    {
        displayTimeRange( epoch.fromTimeStamp( startTime ), epoch.fromTimeStamp( endTime ) );
    }
    
    /**
     * Sets the selected range of times which will be displayed by this painter. Times should generally
     * be provided via {@code Epoch#fromTimeStamp(TimeStamp)}.
     */
    public void displayTimeRange( double startTime, double endTime )
    {
        lock.lock( );
        try
        {
            this.startTime = (float) startTime;
            this.endTime = (float) endTime;
            
            updateSelectedTime( );
        }
        finally
        {
            lock.unlock( );
        }
    }
    
    /**
     * Assigns a time value to each point painted by the time painter. Points
     * must be added in increasing time order for time selection to function.
     */
    public void useTimeAttribData( FloatBuffer attributeBuffer )
    {
        lock.lock( );
        try
        {
            this.timeAttributeBuffer = attributeBuffer;
            this.updateSelectedTime( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useVertexPositionData( GLFloatBuffer2D positionBuffer )
    {
        lock.lock( );
        try
        {
            super.useVertexPositionData( positionBuffer );
            updateSelectedTime( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useColorAttribData( GLFloatBuffer attributeBuffer )
    {
        lock.lock( );
        try
        {
            super.useColorAttribData( attributeBuffer );
            updateSelectedTime( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void useSizeAttribData( GLFloatBuffer attributeBuffer )
    {
        lock.lock( );
        try
        {
            super.useSizeAttribData( attributeBuffer );
            updateSelectedTime( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected void updateSelectedTime( )
    {
        if ( timeAttributeBuffer != null )
        {
            startIndex = binarySearch0( timeAttributeBuffer, 0, timeAttributeBuffer.limit( ), startTime );
            if ( startIndex < 0 ) startIndex = -( startIndex + 1 );
            
            endIndex = binarySearch0( timeAttributeBuffer, 0, timeAttributeBuffer.limit( ), endTime );
            if ( endIndex < 0 ) endIndex = -( endIndex + 1 );
        }
        else if ( positionBuffer != null )
        {
            startIndex = 0;
            endIndex = positionBuffer.getNumVertices( );
        }
    }

    // java.util.Arrays.binarySearch0() modified to work with FloatBuffer
    private static int binarySearch0( FloatBuffer b, int fromIndex, int toIndex, float key )
    {
        int low = fromIndex;
        int high = toIndex - 1;

        while ( low <= high )
        {
            int mid = ( low + high ) >>> 1;
            float midVal = b.get( mid );

            if ( midVal < key )
                low = mid + 1;
            else if ( midVal > key )
                high = mid - 1;
            else
                return mid; // key found
        }
        return - ( low + 1 ); // key not found.
    }
    
    @Override
    protected void drawArrays( GL gl )
    {
        gl.glDrawArrays( GL.GL_POINTS, startIndex, endIndex );
    }

}

package com.metsci.glimpse.axis.painter.label.time;

import java.util.List;
import java.util.TimeZone;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverter;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.util.units.time.TimeStamp;

public class RelativeTimeAxisLabelHandler implements TimeAxisLabelHandler
{
    protected Epoch epoch;
    protected int pixelsBetweenTicks = 60;


    public TimeStamp toTimeStamp( double time )
    {
        return epoch.toTimeStamp( time );
    }

    public double fromTimeStamp( TimeStamp time )
    {
        return epoch.fromTimeStamp( time );
    }
    
    @Override
    public void setEpoch( Epoch epoch )
    {
        this.epoch = epoch;
    }

    @Override
    public Epoch getEpoch( )
    {
        return epoch;
    }

    @Override
    public void setTimeZone( TimeZone timeZone )
    {
        // do nothing
    }

    @Override
    public TimeZone getTimeZone( )
    {
        // do nothing
        return null;
    }

    @Override
    public void setPixelsBetweenTicks( int pixels )
    {
        this.pixelsBetweenTicks = pixels;
    }

    @Override
    public List<TimeStamp> getTickPositions( Axis1D axis, double axisLengthPixels )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getTickLabels( Axis1D axis, List<TimeStamp> tickPositions )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TimeStruct> getTimeStructs( Axis1D axis, List<TimeStamp> tickTimes )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getTickInterval( List<TimeStamp> tickTimes )
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double[] getTickPositions( Axis1D axis )
    {
        List<TimeStamp> tickList = getTickPositions( axis, axis.getSizePixels( ) );
        double[] tickArray = new double[tickList.size( )];

        for ( int i = 0; i < tickList.size( ); i++ )
        {
            tickArray[i] = fromTimeStamp( tickList.get( i ) );
        }

        return tickArray;
    }

    @Override
    public String[] getTickLabels( Axis1D axis, double[] tickPositions )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public double[] getMinorTickPositions( double[] tickPositions )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public String getAxisLabel( Axis1D axis )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public void setAxisLabel( String label )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public AxisUnitConverter getAxisUnitConverter( )
    {
        throw new UnsupportedOperationException( );
    }

    @Override
    public void setAxisUnitConverter( AxisUnitConverter converter )
    {
        throw new UnsupportedOperationException( );
    }
}

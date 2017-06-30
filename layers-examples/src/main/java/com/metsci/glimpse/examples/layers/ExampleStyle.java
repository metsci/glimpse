package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.util.GeneralUtils.floats;

import java.util.Arrays;

import com.metsci.glimpse.support.color.GlimpseColor;

public class ExampleStyle
{

    public float feather_PX = 0.9f;

    public float[] rgbaOutsideTWindow = floats( 0.6f, 0.6f, 0.6f, 0.5f );
    public float[] rgbaInsideTWindow = GlimpseColor.getBlack( );

    public float pointSizeOutsideXyWindow_PX = 4.0f;
    public float pointSizeMinInsideXyWindow_PX = 5.0f;
    public float pointSizeMaxInsideXyWindow_PX = 10.0f;
    public long pointSizePeriodInsideXyWindow_MILLIS = 2000;


    public ExampleStyle( )
    {
    }

    public ExampleStyle( ExampleStyle orig )
    {
        this.feather_PX = orig.feather_PX;

        this.rgbaOutsideTWindow = Arrays.copyOf( orig.rgbaOutsideTWindow, 4 );
        this.rgbaInsideTWindow = Arrays.copyOf( orig.rgbaInsideTWindow, 4 );

        this.pointSizeOutsideXyWindow_PX = orig.pointSizeOutsideXyWindow_PX;
        this.pointSizeMinInsideXyWindow_PX = orig.pointSizeMinInsideXyWindow_PX;
        this.pointSizeMaxInsideXyWindow_PX = orig.pointSizeMaxInsideXyWindow_PX;
        this.pointSizePeriodInsideXyWindow_MILLIS = orig.pointSizePeriodInsideXyWindow_MILLIS;
    }

    public float pointSizeInsideXyWindow_PX( long renderTime_PMILLIS )
    {
        double periodFrac = ( renderTime_PMILLIS % this.pointSizePeriodInsideXyWindow_MILLIS ) / ( ( double ) ( this.pointSizePeriodInsideXyWindow_MILLIS - 1 ) );
        double sizeFrac = 2.0*( periodFrac < 0.5 ? periodFrac : 1.0 - periodFrac );
        return ( float ) ( this.pointSizeMinInsideXyWindow_PX + sizeFrac*( this.pointSizeMaxInsideXyWindow_PX - this.pointSizeMinInsideXyWindow_PX ) );
    }

}

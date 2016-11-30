package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.util.GeneralUtils.floats;

import java.util.Arrays;

import com.metsci.glimpse.support.color.GlimpseColor;

public class ExampleStyle
{

    public float pointSize_PX = 7.0f;

    public float feather_PX = 0.9f;

    public float[] rgbaOutsideTimeWindow = floats( 0.6f, 0.6f, 0.6f, 0.5f );

    public float[] rgbaInsideTimeWindow = GlimpseColor.getBlack( );


    public ExampleStyle( )
    {
    }

    public ExampleStyle( ExampleStyle orig )
    {
        this.pointSize_PX = orig.pointSize_PX;
        this.feather_PX = orig.feather_PX;
        this.rgbaOutsideTimeWindow = Arrays.copyOf( orig.rgbaOutsideTimeWindow, 4 );
        this.rgbaInsideTimeWindow = Arrays.copyOf( orig.rgbaInsideTimeWindow, 4 );
    }

}

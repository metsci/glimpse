package com.metsci.glimpse.examples.layers;

import java.util.Arrays;

import com.metsci.glimpse.support.color.GlimpseColor;

public class ExampleStyle
{

    public float pointSize_PX = 5.0f;

    public float feather_PX = 0.9f;

    public float[] rgba = GlimpseColor.getBlack( );


    public ExampleStyle( )
    {
    }

    public ExampleStyle( ExampleStyle orig )
    {
        this.pointSize_PX = orig.pointSize_PX;
        this.feather_PX = orig.feather_PX;
        this.rgba = Arrays.copyOf( orig.rgba, 4 );
    }

}

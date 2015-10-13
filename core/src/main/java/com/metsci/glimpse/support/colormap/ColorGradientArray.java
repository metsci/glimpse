package com.metsci.glimpse.support.colormap;

public class ColorGradientArray implements ColorGradient
{
    // array containing [r,g,b,r,g,b,...] samples of the color scale
    protected float[] f;
    protected int size;
    
    public ColorGradientArray( float[] f )
    {
        this.f = f;
        this.size = f.length / 3;
    }

    @Override
    public void toColor( float fraction, float[] rgba )
    {
        int index = (int) Math.floor( fraction * size );
        if ( index < 0 ) index = 0;
        if ( index >= size ) index = size-1;
        
        rgba[0] = f[3*index+0];
        rgba[1] = f[3*index+1];
        rgba[2] = f[3*index+2];
        rgba[3] = 1.0f;
    }
}
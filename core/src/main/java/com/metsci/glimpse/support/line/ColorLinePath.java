package com.metsci.glimpse.support.line;

import com.metsci.glimpse.gl.GLStreamingBuffer;

public class ColorLinePath extends LinePath
{
    protected GLStreamingBuffer rgbaVbo;
    protected boolean rgbaDirty;

    public void moveTo( float x, float y )
    {
        this.moveTo( x, y, 0f );
    }

    public void moveTo( float x, float y, float mileage )
    {
        this.data.moveTo( x, y, mileage );
        this.setDirty( );
    }

    public void lineTo( float x, float y )
    {
        this.data.lineTo( x, y );
        this.setDirty( );
    }
}

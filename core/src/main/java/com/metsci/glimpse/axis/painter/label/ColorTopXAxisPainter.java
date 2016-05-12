package com.metsci.glimpse.axis.painter.label;

import com.metsci.glimpse.axis.painter.ColorXAxisPainter;

public class ColorTopXAxisPainter extends ColorXAxisPainter
{
    public ColorTopXAxisPainter( AxisLabelHandler ticks )
    {
        super( ticks );
    }

    @Override
    public int getAxisLabelPositionY( int height, int textHeight )
    {
        if ( packLabel )
        {
            return tickBufferSize + tickSize + textBufferSize + textHeight + labelBufferSize;
        }
        else
        {
            return height - 1 - labelBufferSize - textHeight;
        }
    }

    @Override
    public int getTickTextPositionY( int height, int textHeight )
    {
        return tickBufferSize + tickSize + textBufferSize;
    }

    @Override
    public int getTickTopY( int height, int size )
    {
        return tickBufferSize + size;
    }

    @Override
    public int getTickBottomY( int height, int size )
    {
        return tickBufferSize;
    }

    @Override
    public int getColorBarMinY( int height )
    {
        return tickBufferSize;
    }

    @Override
    public int getColorBarMaxY( int height )
    {
        return tickBufferSize + colorBarSize;
    }
}
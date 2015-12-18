package com.metsci.glimpse.plot.timeline.event.paint;

public class IconDrawInfo
{
    public Object id;
    public double positionX;
    public double positionY;
    public double scaleX;
    public double scaleY;
    public int centerX;
    public int centerY;
    
    public boolean isX;
    
    public IconDrawInfo( Object id, double positionX, double positionY, double scaleX, double scaleY, int centerX, int centerY, boolean isX )
    {
        this.id = id;
        this.positionX = positionX;
        this.positionY = positionY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.centerX = centerX;
        this.centerY = centerY;
        this.isX = isX;
    }

    public Object getId( )
    {
        return id;
    }

    public double getPositionX( )
    {
        return positionX;
    }

    public double getPositionY( )
    {
        return positionY;
    }

    public double getScaleX( )
    {
        return scaleX;
    }

    public double getScaleY( )
    {
        return scaleY;
    }

    public int getCenterX( )
    {
        return centerX;
    }

    public int getCenterY( )
    {
        return centerY;
    }

    public boolean isX( )
    {
        return isX;
    }
}
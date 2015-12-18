package com.metsci.glimpse.plot.timeline.event.paint;

public class TextDrawInfo
{
    protected String text;
    protected float[] color;
    protected int x;
    protected int y;
    protected double shiftX;
    protected double shiftY;
    
    public TextDrawInfo( String text, float[] color, int x, int y, double shiftX, double shiftY )
    {
        this.text = text;
        this.color = color;
        this.x = x;
        this.y = y;
        this.shiftX = shiftX;
        this.shiftY = shiftY;
    }
    
    public String getText( )
    {
        return text;
    }
    
    public float[] getColor( )
    {
        return this.color;
    }
    
    public int getX( )
    {
        return x;
    }
    
    public int getY( )
    {
        return y;
    }
    
    public double getShiftX( )
    {
        return shiftX;
    }
    
    public double getShiftY( )
    {
        return shiftY;
    }
}
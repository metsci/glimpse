package com.metsci.glimpse.plot;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.support.settings.DefaultLookAndFeel;

/**
 * A simple plotting area which takes up its entire parent GlimpseLayout
 * (leaving no room for a plot title or axis tick marks).
 * 
 * @author ulman
 */
public class EmptyPlot2D extends GlimpseAxisLayout2D
{
    protected BackgroundPainter backgroundPainter;

    protected Axis2D axisXY;
    protected AxisMouseListener mouseListenerXY;

    public EmptyPlot2D( )
    {
        this.initialize( );
    }
    
    protected void initialize( )
    {
        initializeAxes( );
        initializeListeners( );
        initializePainters( );
        initializeLookAndFeel( );
    }

    protected void initializeAxes( )
    {
        this.axisXY = new Axis2D( createAxisX( ), createAxisY( ) );
        this.setAxis( this.axisXY );
    }

    protected void initializeListeners( )
    {
        this.mouseListenerXY = createAxisMouseListenerXY( );
        this.addGlimpseMouseAllListener( this.mouseListenerXY );
    }

    protected void initializePainters( )
    {
        this.backgroundPainter = new BackgroundPainter( true );
        this.addPainter( this.backgroundPainter, Integer.MIN_VALUE );
    }

    protected void initializeLookAndFeel( )
    {
        setLookAndFeel( new DefaultLookAndFeel( ) );
    }

    protected AxisMouseListener createAxisMouseListenerXY( )
    {
        return new AxisMouseListener2D( );
    }

    protected Axis1D createAxisX( )
    {
        return new Axis1D( );
    }

    protected Axis1D createAxisY( )
    {
        return new Axis1D( );
    }
    
    
    public BackgroundPainter getBackgroundPainter( )
    {
        return this.backgroundPainter;
    }

}

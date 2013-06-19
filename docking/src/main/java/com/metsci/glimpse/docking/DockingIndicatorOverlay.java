package com.metsci.glimpse.docking;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.swing.JComponent;

public class DockingIndicatorOverlay extends JComponent
{

    protected static final Logger logger = Logger.getLogger( DockingIndicatorOverlay.class.getName( ) );


    protected static final Method setMixingCutoutMethod = findSetMixingCutoutMethod( );
    protected static Method findSetMixingCutoutMethod( )
    {
        try
        {
            return Class.forName( "com.sun.awt.AWTUtilities" ).getMethod( "setComponentMixingCutoutShape", Component.class, Shape.class );
        }
        catch ( Exception e )
        {
            logger.warning( "AWTUtilities.setComponentMixingCutoutShape( ) is not accessible; docking overlay will appear behind heavyweight components" );
            return null;
        }
    }

    protected static void setMixingCutout( Component component, Area cutoutArea )
    {
        if ( setMixingCutoutMethod != null )
        {
            int xOrigin = 0;
            int yOrigin = 0;
            for ( Component c = component; c != null && !( c instanceof Window ); c = c.getParent( ) )
            {
                xOrigin += c.getX( );
                yOrigin += c.getY( );
            }

            Area cutoutArea2 = cutoutArea.createTransformedArea( AffineTransform.getTranslateInstance( xOrigin, yOrigin ) );

            try
            {
                setMixingCutoutMethod.invoke( null, component, cutoutArea2 );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }
        }
    }


    protected static final Area emptyArea = new Area( new Rectangle( ) );


    protected final Paint paint;
    protected final int thickness;
    protected final boolean mixingCutoutEnabled;

    protected Area fillArea;


    public DockingIndicatorOverlay( Paint paint, int thickness, boolean enableMixingCutout )
    {
        this.paint = paint;
        this.thickness = thickness;
        this.mixingCutoutEnabled = enableMixingCutout;

        this.fillArea = emptyArea;

        setOpaque( false );
    }

    public void setDockingIndicatorRectangle( Rectangle indicatorRect )
    {
        if ( indicatorRect == null )
        {
            this.fillArea = emptyArea;
        }
        else
        {
            this.fillArea = new Area( indicatorRect );
            fillArea.subtract( new Area( new Rectangle( indicatorRect.x + thickness, indicatorRect.y + thickness, indicatorRect.width - 2*thickness, indicatorRect.height - 2*thickness ) ) );
        }

        if ( mixingCutoutEnabled )
        {
            setMixingCutout( this, fillArea );
        }

        repaint( );
    }

    @Override
    public boolean contains( int x, int y )
    {
        return ( fillArea != emptyArea );
    }

    @Override
    public void paint( Graphics g )
    {
        super.paint( g );

        Graphics2D g2d = ( Graphics2D ) g;
        g2d.setPaint( paint );
        g2d.fill( fillArea );
    }

}

/*
 * Copyright (c) 2012, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

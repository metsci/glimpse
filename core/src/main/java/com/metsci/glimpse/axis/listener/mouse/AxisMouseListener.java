/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.axis.listener.mouse;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseAllListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.event.mouse.Mouseable;

/**
 * A Glimpse mouse listener which defines the default way that axes are
 * updated in response to mouse events.
 *
 * @author ulman
 */
public abstract class AxisMouseListener implements GlimpseMouseAllListener
{
    public final double zoomConstant = 1.12f;

    protected boolean allowSelectionLock = true;
    protected boolean allowSelectionZoom = true;
    protected boolean allowPan = true;
    protected boolean allowZoom = true;

    protected boolean anchoredX = false;
    protected boolean anchoredY = false;
    protected int anchorPixelsX = 0;
    protected int anchorPixelsY = 0;
    protected double anchorValueX = 0.0;
    protected double anchorValueY = 0.0;

    public void addAxisMouseListener( Mouseable mouseable )
    {
        mouseable.addGlimpseMouseListener( this );
        mouseable.addGlimpseMouseMotionListener( this );
        mouseable.addGlimpseMouseWheelListener( this );
    }

    public void setAllowSelectionLock( boolean b )
    {
        allowSelectionLock = b;
    }

    public void setAllowSelectionZoom( boolean b )
    {
        allowSelectionZoom = b;
    }

    public void setAllowZoom( boolean b )
    {
        allowZoom = b;
    }

    public void setAllowPan( boolean b )
    {
        allowPan = b;
    }

    protected int getDim( boolean horizontal, int x, int y )
    {
        return horizontal ? x : y;
    }

    protected double getDim( boolean horizontal, double x, double y )
    {
        return horizontal ? x : y;
    }

    public void mouseWheelMoved( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        if ( e.isKeyDown( ModifierKey.Shift ) )
        {
            zoomSelection( axis, horizontal, e.getWheelIncrement( ), e.getX( ), e.getY( ) );
            zoom( axis, horizontal, -e.getWheelIncrement( ), e.getX( ), e.getY( ) );
        }
        else if ( e.isKeyDown( ModifierKey.Ctrl ) )
        {
            zoomSelection( axis, horizontal, -e.getWheelIncrement( ), e.getX( ), e.getY( ) );
        }
        else
        {
            zoom( axis, horizontal, -e.getWheelIncrement( ), e.getX( ), e.getY( ) );
        }
    }

    public void mouseMoved( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        if ( e.isButtonDown( MouseButton.Button1 ) )
        {
            pan( axis, horizontal, e.getX( ), e.getY( ) );
        }
        else
        {
            move( axis, horizontal, e.getX( ), e.getY( ) );
        }
    }

    public void mousePressed( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        if ( e.isButtonDown( MouseButton.Button3 ) )
        {
            toggleSelectionLock( axis );
        }
    }

    public void mouseReleased( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        unanchor( axis, horizontal, e.getX( ), e.getY( ) );
    }

    public void unanchor( Axis1D axis, boolean horizontal, int posX, int posY )
    {
        if ( horizontal )
        {
            anchoredX = false;
        }
        else
        {
            anchoredY = false;
        }
    }

    public void anchor( Axis1D axis, boolean horizontal, int posX, int posY )
    {
        if ( horizontal && !anchoredX )
        {
            move( axis, horizontal, posX, posY );
            anchorValueX = axis.getMin( );
            anchorPixelsX = posX;
            anchoredX = true;
        }
        else if ( !horizontal && !anchoredY )
        {
            move( axis, horizontal, posX, posY );
            anchorValueY = axis.getMin( );
            anchorPixelsY = axis.getSizePixels( ) - posY;
            anchoredY = true;
        }
    }

    public void pan( Axis1D axis, boolean horizontal, int posX, int posY )
    {
        if ( !allowPan ) return;

        anchor( axis, horizontal, posX, posY );

        int mousePosPixels = getDim( horizontal, posX, axis.getSizePixels( ) - posY );

        int panPixels = getDim( horizontal, anchorPixelsX, anchorPixelsY ) - mousePosPixels;
        double diffValue = axis.getMax( ) - axis.getMin( );
        double newMinValue = getDim( horizontal, anchorValueX, anchorValueY ) + ( panPixels / axis.getPixelsPerValue( ) );
        double newMaxValue = newMinValue + diffValue;

        axis.setMin( newMinValue );
        axis.setMax( newMaxValue );
    }

    public void zoom( Axis1D axis, boolean horizontal, double zoomIncrements, int posX, int posY )
    {
        if ( !allowZoom ) return;

        int mousePosPixels = getDim( horizontal, posX, axis.getSizePixels( ) - posY );
        double mousePosValue = axis.screenPixelToValue( mousePosPixels );

        double zoomPercentDbl = Math.pow( zoomConstant, zoomIncrements );
        double oldPixelsPerValue = axis.getPixelsPerValue( );
        double newPixelsPerValue = oldPixelsPerValue * zoomPercentDbl;
        double newMinValue = mousePosValue - mousePosPixels / newPixelsPerValue;
        double newMaxValue = newMinValue + axis.getSizePixels( ) / newPixelsPerValue;

        axis.setMin( newMinValue );
        axis.setMax( newMaxValue );
    }

    public void zoomSelection( Axis1D axis, boolean horizontal, double zoomIncrements, int posX, int posY )
    {
        if ( !allowSelectionZoom ) return;

        double zoomPercentDbl = Math.pow( zoomConstant, zoomIncrements );
        double newSelectionSize = axis.getSelectionSize( ) * zoomPercentDbl;

        axis.setSelectionSize( newSelectionSize );
    }

    public void move( Axis1D axis, boolean horizontal, int posX, int posY )
    {
        int mousePosPixels = getDim( horizontal, posX, axis.getSizePixels( ) - posY );

        double mousePosValue = axis.screenPixelToValue( mousePosPixels );

        axis.setMouseValue( mousePosValue );

        // if a drag is happening or the selection is locked,
        // the the selection center in value space should never change.
        if ( !axis.isSelectionLocked( ) )
        {
            axis.setSelectionCenter( mousePosValue );
        }
    }

    public void toggleSelectionLock( Axis1D axis )
    {
        if ( !allowSelectionLock ) return;

        axis.setSelectionLock( !axis.isSelectionLocked( ) );
    }
}

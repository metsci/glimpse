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
package com.metsci.glimpse.axis.listener.touch;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.event.touch.GlimpseGestureListener;
import com.metsci.glimpse.event.touch.Touchable;

/**
 * A Glimpse gesture listener which defines the default way that axes are
 * updated in response to high level gesture events (like those from a
 * touch screen tablet device).
 *
 * @author osborn
 */
public abstract class AxisGestureListener implements GlimpseGestureListener
{
    public final double zoomConstant = 0.12f;

    protected boolean allowSelectionLock = true;
    protected boolean allowSelectionZoom = true;
    protected boolean allowPan = true;
    protected boolean allowZoom = true;
    protected boolean allowSelect = true;

    public void addAxisGestureListener( Touchable touchable )
    {
        touchable.addGlimpseGestureListener( this );
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

    public void setAllowSelect( boolean b )
    {
        allowSelect = b;
    }

    /**
     * Adjust the scaling so that it can be split between horizonal and vertical.
     */
    protected float getScale( boolean horizontal, float scale, float angle )
    {
        double diff = scale - 1;
        double scaleAdjMult = horizontal ? Math.abs( Math.cos( angle ) ) : Math.abs( Math.sin( angle ) );

        return ( float ) ( diff * scaleAdjMult + 1 );
    }

    protected int getDim( boolean horizontal, int x, int y )
    {
        return horizontal ? x : y;
    }

    protected double getDim( boolean horizontal, double x, double y )
    {
        return horizontal ? x : y;
    }

    public void pan( Axis1D axis, boolean horizontal, float dX, float dY )
    {
        if ( !allowPan ) return;

        double panPixels = getDim( horizontal, dX, dY );
        double diffValue = axis.getMax( ) - axis.getMin( );
        double newMinValue = axis.getMin( ) - panPixels / axis.getPixelsPerValue( );
        double newMaxValue = newMinValue + diffValue;

        axis.setMin( newMinValue );
        axis.setMax( newMaxValue );
    }

    public void zoom( Axis1D axis, boolean horizontal, float posX, float posY, float scale )
    {
        if ( !allowZoom ) return;

        double mousePosPixels = getDim( horizontal, posX, axis.getSizePixels( ) - posY );
        double mousePosValue = axis.screenPixelToValue( mousePosPixels );

        double oldPixelsPerValue = axis.getPixelsPerValue( );
        double newPixelsPerValue = oldPixelsPerValue * scale;
        double newMinValue = mousePosValue - mousePosPixels / newPixelsPerValue;
        double newMaxValue = newMinValue + axis.getSizePixels( ) / newPixelsPerValue;

        axis.setMin( newMinValue );
        axis.setMax( newMaxValue );
    }

    public void select( Axis1D axis, boolean horizontal, float posX, float posY )
    {
        if ( !allowSelect ) return;

        double posPixels = getDim( horizontal, posX, posY );
        double posData = axis.screenPixelToValue( posPixels );

        axis.setMouseValue( posData );

        if ( !axis.isSelectionLocked( ) )
        {
            axis.setSelectionCenter( posData );
        }
    }

    public void zoomSelection( Axis1D axis, boolean horizontal, int zoomIncrements, int posX, int posY )
    {
        if ( !allowSelectionZoom ) return;

        double zoomPercentDbl = 1.0f;
        for ( int i = 0; i < Math.abs( zoomIncrements ); i++ )
        {
            zoomPercentDbl *= 1.0 + zoomConstant;
        }
        zoomPercentDbl = zoomIncrements > 0 ? zoomPercentDbl : 1.0 / zoomPercentDbl;
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

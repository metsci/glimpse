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
package com.metsci.glimpse.event.mouse;

import java.util.EnumSet;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisNotSetException;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;


/**
 * A Glimpse-specific MouseEvent implementation which allows Glimpse axis handling
 * code to be written in a widget framework (SWT, Swing, etc...) independent manner.
 *
 * @author ulman
 *
 */
public class GlimpseMouseEvent
{
    protected GlimpseTargetStack stack;
    protected EnumSet<ModifierKey> modifiers;
    protected EnumSet<MouseButton> buttons;
    protected int wheelIncrement;
    protected int clickCount;
    protected int x;
    protected int y;
    protected boolean handled;

    public GlimpseMouseEvent( GlimpseTargetStack stack, int x, int y )
    {
        this( stack, x, y, 0 );
    }

    public GlimpseMouseEvent( GlimpseTargetStack stack, int x, int y , int wheelIncrement )
    {
        this( stack, EnumSet.noneOf( ModifierKey.class ), EnumSet.noneOf( MouseButton.class ), x, y, wheelIncrement, 0 );
    }

    public GlimpseMouseEvent( GlimpseTargetStack stack, EnumSet<ModifierKey> modifiers, EnumSet<MouseButton> buttons, int x, int y )
    {
        this( stack, modifiers, buttons, x, y, 0, 0 );
    }

    public GlimpseMouseEvent( GlimpseTargetStack stack, EnumSet<ModifierKey> modifiers, EnumSet<MouseButton> buttons, int x, int y, int wheelIncrement, int clickCount )
    {
        super( );
        this.stack = stack;
        this.modifiers = modifiers;
        this.buttons = buttons;
        this.x = x;
        this.y = y;
        this.wheelIncrement = wheelIncrement;
        this.clickCount = clickCount;
        this.handled = false;
    }
    
    public boolean isHandled( )
    {
        return this.handled;
    }
    
    public void setHandled( boolean handled )
    {
        this.handled = handled;
    }

    public EnumSet<ModifierKey> getModifiers( )
    {
        return modifiers;
    }

    public EnumSet<MouseButton> getButtons( )
    {
        return buttons;
    }

    public boolean isKeyDown( ModifierKey modifier )
    {
        return modifiers.contains( modifier );
    }

    public boolean isButtonDown( MouseButton button )
    {
        return buttons.contains( button );
    }

    public boolean isAnyButtonDown( )
    {
        return !buttons.isEmpty( );
    }

    public int getX( )
    {
        return x;
    }
    
    public int getScreenPixelsX( )
    {
        return getX( );
    }
    
    public double getAxisCoordinatesX( )
    {
        Axis1D axis = null;
        
        Axis2D axis2D = getAxis2D( );
        if ( axis2D != null )
        {
            axis = axis2D.getAxisX( );
        }
        else
        {
            axis = getAxis1D( );
        }

        if ( axis != null )
        {
            return axis.screenPixelToValue( x ); 
        }
        else
        {
            throw new AxisNotSetException( stack );
        }
    }

    public int getY( )
    {
        return y;
    }
    
    public double getAxisCoordinatesY( )
    {
        Axis1D axis = null;
        
        Axis2D axis2D = getAxis2D( );
        if ( axis2D != null )
        {
            axis = axis2D.getAxisY( );
        }
        else
        {
            axis = getAxis1D( );
        }

        if ( axis != null )
        {
            int height = axis.getSizePixels( );
            return height - axis.screenPixelToValue( y ); 
        }
        else
        {
            throw new AxisNotSetException( stack );
        }
    }
    
    public int getScreenPixelsY( )
    {
        return getY( );
    }

    public int getWheelIncrement( )
    {
        return this.wheelIncrement;
    }

    public int getClickCount( )
    {
        return this.clickCount;
    }

    public GlimpseTargetStack getTargetStack( )
    {
        return this.stack;
    }

    public Axis1D getAxis1D( )
    {
        GlimpseTargetStack stack = getTargetStack( );
        GlimpseTarget target = stack.getTarget( );

        if ( target instanceof GlimpseAxisLayout1D )
        {
            GlimpseAxisLayout1D layout = (GlimpseAxisLayout1D) target;
            return layout.getAxis( stack );
        }
        else
        {
            return null;
        }
    }

    public Axis2D getAxis2D( )
    {
        GlimpseTargetStack stack = getTargetStack( );
        GlimpseTarget target = stack.getTarget( );

        if ( target instanceof GlimpseAxisLayout2D )
        {
            GlimpseAxisLayout2D layout = (GlimpseAxisLayout2D) target;
            return layout.getAxis( stack );
        }
        else
        {
            return null;
        }
    }

    @Override
    public String toString( )
    {
        return String.format( "x: %d y: %d wheel: %d click: %d button: %s modifier: %s", x, y, wheelIncrement, clickCount, buttons, modifiers);
    }
}

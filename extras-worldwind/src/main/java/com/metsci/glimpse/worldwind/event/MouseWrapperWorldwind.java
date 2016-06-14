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
package com.metsci.glimpse.worldwind.event;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EnumSet;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.event.mouse.MouseWrapperImpl;
import com.metsci.glimpse.event.mouse.swing.GlimpseMouseWrapper;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;
import com.metsci.glimpse.worldwind.tile.GlimpseSurfaceTile;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;

/**
 * A mouse event handler which listens for java.awt mouse events from a WorldWind
 * and translates them into GlimpseMouseEvents delivered to a particular GlimpseAxisLayout2D
 * specified in MouseWrapperWorldwind's constructor.
 *
 * @author ulman
 * @see com.metsci.glimpse.event.mouse.swing.MouseWrapperSwing
 * @see com.metsci.glimpse.event.mouse.MouseWrapper
 */
public class MouseWrapperWorldwind extends MouseWrapperImpl<GlimpseMouseEvent> implements MouseWheelListener, MouseMotionListener, MouseListener
{
    protected WorldWindow wwd;
    protected GeoProjection projection;
    protected GlimpseSurfaceTile tile;

    protected GlimpseAxisLayout2D layout;

    protected static class GlimpsePosition
    {
        int x;
        int y;

        public GlimpsePosition( int x, int y )
        {
            this.x = x;
            this.y = y;
        }
    }

    public static void linkMouseEvents( WorldWindow wwd, GeoProjection projection, GlimpseSurfaceTile tile )
    {
        MouseWrapperWorldwind wrapper = new MouseWrapperWorldwind( wwd, projection, tile );
        wwd.getInputHandler( ).addMouseListener( wrapper );
        wwd.getInputHandler( ).addMouseMotionListener( wrapper );
        wwd.getInputHandler( ).addMouseWheelListener( wrapper );
    }

    private MouseWrapperWorldwind( WorldWindow wwd, GeoProjection projection, GlimpseSurfaceTile tile )
    {
        super( tile.getGlimpseCanvas( ) );

        this.wwd = wwd;
        this.projection = projection;
        this.tile = tile;

        if ( tile.getGlimpseLayout( ) instanceof GlimpseAxisLayout2D )
        {
            this.layout = ( GlimpseAxisLayout2D ) tile.getGlimpseLayout( );
        }
        else
        {
            throw new IllegalArgumentException( "GlimpseSurfaceTile must use a GlimpseAxisLayout2D for delivery of GlimpseMouseEvents." );
        }
    }

    @Override
    protected boolean isButtonDown( GlimpseMouseEvent e )
    {
        return e.isAnyButtonDown( );
    }

    @Override
    protected boolean isInterior( GlimpseMouseEvent e, GlimpseBounds bounds )
    {
        return bounds.contains( e.getX( ), e.getY( ) );
    }

    @Override
    protected boolean isValid( GlimpseMouseEvent e, GlimpseBounds bounds )
    {
        return e != null && bounds != null;
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEvent( GlimpseMouseEvent e, GlimpseTargetStack stack )
    {
        return e;
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEventWheel( GlimpseMouseEvent e, GlimpseTargetStack stack )
    {
        return e;
    }

    protected GlimpsePosition getGlimpsePosition( MouseEvent mouseEvent )
    {
        GlimpseTargetStack stack = tile.getTargetStack( );
        if ( stack == null ) return null;
        
        Position worldwindPosition = this.wwd.getCurrentPosition( );
        if ( worldwindPosition == null ) return null;

        // get the lat/lon of the mouse from the WorldWind
        double lat = worldwindPosition.getLatitude( ).getDegrees( );
        double lon = worldwindPosition.getLongitude( ).getDegrees( );
        LatLonGeo latlon = LatLonGeo.fromDeg( lat, lon );

        // convert the lat/lon into Glimpse coordinate space using the GeoProjection
        Vector2d glimpsePosition = this.projection.project( latlon );

        // calculate the x/y position in pixels of the mouse click relative to the GlimpseLayout
        Axis2D axis = this.layout.getAxis( stack );
        int posX = axis.getAxisX( ).valueToScreenPixel( glimpsePosition.getX( ) );
        int posY = axis.getAxisY( ).getSizePixels( ) - axis.getAxisY( ).valueToScreenPixel( glimpsePosition.getY( ) );

        return new GlimpsePosition( posX, posY );
    }

    protected GlimpseMouseEvent fromWorldwindMouseEvent( MouseEvent mouseEvent )
    {
        return fromWorldwindMouseEvent( mouseEvent, getGlimpsePosition( mouseEvent ) );
    }

    protected GlimpseMouseEvent fromWorldwindMouseEvent( MouseEvent mouseEvent, GlimpsePosition position )
    {
        GlimpseTargetStack stack = tile.getTargetStack( );
        if ( stack == null ) return null;
        if ( position == null ) return null;

        EnumSet<MouseButton> buttons = GlimpseMouseWrapper.getMouseButtons( mouseEvent );
        EnumSet<ModifierKey> modifiers = GlimpseMouseWrapper.getModifierKeys( mouseEvent );
        int clickCount = mouseEvent.getClickCount( );

        return new GlimpseMouseEvent( stack, modifiers, buttons, position.x, position.y, 0, clickCount );
    }

    protected GlimpseMouseEvent fromWorldwindMouseWheelEvent( MouseWheelEvent mouseEvent )
    {
        return fromWorldwindMouseWheelEvent( mouseEvent, getGlimpsePosition( mouseEvent ) );
    }

    protected GlimpseMouseEvent fromWorldwindMouseWheelEvent( MouseWheelEvent mouseEvent, GlimpsePosition position )
    {
        GlimpseTargetStack stack = tile.getTargetStack( );
        if ( stack == null ) return null;
        if ( position == null ) return null;

        EnumSet<MouseButton> buttons = GlimpseMouseWrapper.getMouseButtons( mouseEvent );
        EnumSet<ModifierKey> modifiers = GlimpseMouseWrapper.getModifierKeys( mouseEvent );
        int wheelRotation = mouseEvent.getWheelRotation( );
        int clickCount = mouseEvent.getClickCount( );

        return new GlimpseMouseEvent( stack, modifiers, buttons, position.x, position.y, wheelRotation, clickCount );
    }

    @Override
    public void mouseClicked( MouseEvent e )
    {
        // not handled by GlimpseMouseListener, use mousePressed/mouseReleased
    }

    @Override
    public void mouseEntered( MouseEvent e )
    {
        GlimpseMouseEvent e0 = fromWorldwindMouseEvent( e );
        if ( e0 != null ) mouseEntered0( e0 );
    }

    @Override
    public void mouseExited( MouseEvent e )
    {
        GlimpseMouseEvent e0 = fromWorldwindMouseEvent( e );
        if ( e0 != null ) mouseExited0( e0 );
    }

    @Override
    public void mousePressed( MouseEvent e )
    {
        GlimpseMouseEvent e0 = fromWorldwindMouseEvent( e );
        
        if ( e0 != null )
        {
            boolean handled = mousePressed0( e0 );
    
            // if Glimpse handled the MouseEvent, consume it so that WorldWind does not see it
            if ( handled ) e.consume( );
        }
    }

    @Override
    public void mouseReleased( MouseEvent e )
    {
        GlimpseMouseEvent e0 = fromWorldwindMouseEvent( e );
        
        if ( e0 != null )
        {
            boolean handled = mouseReleased0( e0 );
    
            // if Glimpse handled the MouseEvent, consume it so that WorldWind does not see it
            if ( handled ) e.consume( );
        }
    }

    @Override
    public void mouseDragged( MouseEvent e )
    {
        GlimpseMouseEvent e0 = fromWorldwindMouseEvent( e );
        
        if ( e0 != null )
        {
            boolean handled = mouseDragged0( e0 );
    
            // if Glimpse handled the MouseEvent, consume it so that WorldWind does not see it
            if ( handled ) e.consume( );
        }
    }

    @Override
    public void mouseMoved( MouseEvent e )
    {
        GlimpseMouseEvent e0 = fromWorldwindMouseEvent( e );
        
        if ( e0 != null )
        {
            boolean handled = mouseMoved0( e0 );
    
            // if Glimpse handled the MouseEvent, consume it so that WorldWind does not see it
            if ( handled ) e.consume( );
        }
    }

    @Override
    public void mouseWheelMoved( MouseWheelEvent e )
    {
        GlimpseMouseEvent e0 = fromWorldwindMouseWheelEvent( e );
        
        if ( e0 != null )
        {
            boolean handled = mouseWheelMoved0( e0 );
    
            // if Glimpse handled the MouseEvent, consume it so that WorldWind does not see it
            if ( handled ) e.consume( );
        }
    }
}

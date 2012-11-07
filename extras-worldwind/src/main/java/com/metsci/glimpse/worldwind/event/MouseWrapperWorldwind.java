package com.metsci.glimpse.worldwind.event;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EnumSet;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.event.mouse.Mouseable;
import com.metsci.glimpse.event.mouse.swing.GlimpseMouseWrapper;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * A mouse event handler which listens for java.awt mouse events from a WorldWind
 * and translates them into GlimpseMouseEvents delivered to a particular GlimpseAxisLayout2D
 * specified in MouseWrapperWorldwind's constructor.
 * 
 * 
 * @author ulman
 * @see com.metsci.glimpse.event.mouse.swing.MouseWrapperSwing
 * @see com.metsci.glimpse.event.mouse.MouseWrapper
 */
public class MouseWrapperWorldwind implements MouseWheelListener, MouseMotionListener, MouseListener
{
    protected WorldWindow wwd;
    protected GlimpseTargetStack stack;
    protected GlimpseAxisLayout2D layout;
    protected GeoProjection projection;

    public MouseWrapperWorldwind( WorldWindow wwd, GeoProjection projection, GlimpseTargetStack stack )
    {
        this.wwd = wwd;
        this.stack = stack;
        this.projection = projection;

        if ( stack == null || ! ( stack.getTarget( ) instanceof GlimpseAxisLayout2D ) )
        {
            throw new IllegalArgumentException( "GlimpseTargetStack must have a GlimpseAxisLayout2D on top" );
        }

        this.layout = ( GlimpseAxisLayout2D ) stack.getTarget( );
    }

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

    protected GlimpsePosition getGlimpsePosition( MouseEvent mouseEvent )
    {
        Position worldwindPosition = this.wwd.getCurrentPosition( );
        if ( worldwindPosition == null ) return null;

        // get the lat/lon of the mouse from the WorldWind
        double lat = worldwindPosition.getLatitude( ).getDegrees( );
        double lon = worldwindPosition.getLongitude( ).getDegrees( );
        LatLonGeo latlon = LatLonGeo.fromDeg( lat, lon );

        // convert the lat/lon into Glimpse coordinate space using the GeoProjection
        Vector2d glimpsePosition = this.projection.project( latlon );

        // calculate the x/y position in pixels of the mouse click relative to the GlimpseLayout
        Axis2D axis = this.layout.getAxis( this.stack );
        int posX = axis.getAxisX( ).valueToScreenPixel( glimpsePosition.getX( ) );
        int posY = axis.getAxisY( ).getSizePixels( ) - axis.getAxisY( ).valueToScreenPixel( glimpsePosition.getY( ) );

        return new GlimpsePosition( posX, posY );
    }

    protected GlimpseMouseEvent toGlimpseMouseEvent( MouseEvent mouseEvent )
    {
        return toGlimpseMouseEvent( mouseEvent, getGlimpsePosition( mouseEvent ) );
    }

    protected GlimpseMouseEvent toGlimpseMouseEvent( MouseEvent mouseEvent, GlimpsePosition position )
    {
        if ( position == null ) return null;
        
        EnumSet<MouseButton> buttons = GlimpseMouseWrapper.getMouseButtons( mouseEvent );
        EnumSet<ModifierKey> modifiers = GlimpseMouseWrapper.getModifierKeys( mouseEvent );
        int clickCount = mouseEvent.getClickCount( );

        return new GlimpseMouseEvent( stack, modifiers, buttons, position.x, position.y, 0, clickCount );
    }

    protected GlimpseMouseEvent toGlimpseMouseWheelEvent( MouseWheelEvent mouseEvent )
    {
        return toGlimpseMouseWheelEvent( mouseEvent, getGlimpsePosition( mouseEvent ) );
    }

    protected GlimpseMouseEvent toGlimpseMouseWheelEvent( MouseWheelEvent mouseEvent, GlimpsePosition position )
    {
        EnumSet<MouseButton> buttons = GlimpseMouseWrapper.getMouseButtons( mouseEvent );
        EnumSet<ModifierKey> modifiers = GlimpseMouseWrapper.getModifierKeys( mouseEvent );
        int wheelRotation = mouseEvent.getWheelRotation( );
        int clickCount = mouseEvent.getClickCount( );

        return new GlimpseMouseEvent( stack, modifiers, buttons, position.x, position.y, wheelRotation, clickCount );
    }

    protected Mouseable getMouseTarget( GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        GlimpseTarget target = stack.getTarget( );
        if ( target == null || ! ( target instanceof Mouseable ) ) return null;

        Mouseable mouseTarget = ( Mouseable ) target;

        return mouseTarget;
    }

    @Override
    public void mousePressed( MouseEvent mouseEvent )
    {
        GlimpseMouseEvent glimpseEvent = toGlimpseMouseEvent( mouseEvent );
        if ( glimpseEvent == null ) return;
        
        Mouseable mouseTarget = getMouseTarget( stack );
        mouseTarget.mousePressed( glimpseEvent );
    
        // consume the Swing MouseEvent if the GlimpseMouseEvent was marked
        // as handled (so that it doesn't also rotate the WorldWind globe)
        if ( glimpseEvent.isHandled( ) )
        {
            mouseEvent.consume( );
        }
    }

    @Override
    public void mouseReleased( MouseEvent mouseEvent )
    {
        GlimpseMouseEvent glimpseEvent = toGlimpseMouseEvent( mouseEvent );
        if ( glimpseEvent == null ) return;
        
        Mouseable mouseTarget = getMouseTarget( stack );
        mouseTarget.mouseReleased( glimpseEvent );
        
        if ( glimpseEvent.isHandled( ) )
        {
            mouseEvent.consume( );
        }
    }

    @Override
    public void mouseClicked( MouseEvent mouseEvent )
    {
        // not handled by GlimpseMouseListener, use mousePressed/mouseReleased
    }

    @Override
    public void mouseEntered( MouseEvent mouseEvent )
    {
        // currently not handled
        //
        // not sure what it would mean in this context
        // (perhaps the first time the mouse hovered over an area
        //  of the map covered by the projected GlimpseLayout?)
    }

    @Override
    public void mouseExited( MouseEvent mouseEvent )
    {
        // see mouseEntered
    }

    @Override
    public void mouseDragged( MouseEvent mouseEvent )
    {
        GlimpseMouseEvent glimpseEvent = toGlimpseMouseEvent( mouseEvent );
        if ( glimpseEvent == null ) return;
        
        Mouseable mouseTarget = getMouseTarget( stack );
        mouseTarget.mouseMoved( glimpseEvent );
        
        if ( glimpseEvent.isHandled( ) )
        {
            mouseEvent.consume( );
        }
    }

    @Override
    public void mouseMoved( MouseEvent mouseEvent )
    {
        GlimpseMouseEvent glimpseEvent = toGlimpseMouseEvent( mouseEvent );
        if ( glimpseEvent == null ) return;
        
        Mouseable mouseTarget = getMouseTarget( stack );
        mouseTarget.mouseMoved( glimpseEvent );
        
        if ( glimpseEvent.isHandled( ) )
        {
            mouseEvent.consume( );
        }
    }

    @Override
    public void mouseWheelMoved( MouseWheelEvent mouseEvent )
    {
        GlimpseMouseEvent glimpseEvent = toGlimpseMouseWheelEvent( mouseEvent );
        if ( glimpseEvent == null ) return;
        
        Mouseable mouseTarget = getMouseTarget( stack );
        mouseTarget.mouseWheelMoved( glimpseEvent );
        
        if ( glimpseEvent.isHandled( ) )
        {
            mouseEvent.consume( );
        }
    }
}

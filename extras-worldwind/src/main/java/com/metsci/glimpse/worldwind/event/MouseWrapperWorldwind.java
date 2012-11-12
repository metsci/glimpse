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
    protected GlimpseTargetStack stack;

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

    public MouseWrapperWorldwind( WorldWindow wwd, GeoProjection projection, GlimpseSurfaceTile tile )
    {
        super( tile.getGlimpseCanvas( ) );

        this.wwd = wwd;
        this.projection = projection;
        this.tile = tile;

        if ( tile.getGlimpseLayout( ) instanceof GlimpseAxisLayout2D )
        {
            this.layout = ( GlimpseAxisLayout2D ) tile.getGlimpseLayout( );
            this.stack = tile.getTargetStack( );
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
    protected GlimpseMouseEvent toLocalCoords( GlimpseMouseEvent e, GlimpseTargetStack stack )
    {
        return e;
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEvent( GlimpseMouseEvent e )
    {
        return e;
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

    protected GlimpseMouseEvent fromWorldwindMouseEvent( MouseEvent mouseEvent )
    {
        return fromWorldwindMouseEvent( mouseEvent, getGlimpsePosition( mouseEvent ) );
    }

    protected GlimpseMouseEvent fromWorldwindMouseEvent( MouseEvent mouseEvent, GlimpsePosition position )
    {
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
        mouseEntered0( fromWorldwindMouseEvent( e ) );
    }

    @Override
    public void mouseExited( MouseEvent e )
    {
        mouseExited0( fromWorldwindMouseEvent( e ) );
    }
    
    @Override
    public void mousePressed( MouseEvent e )
    {
        boolean handled = mousePressed0( fromWorldwindMouseEvent( e ) );
        
        // if Glimpse handled the MouseEvent, consume it so that WorldWind does not see it
        if ( handled ) e.consume( );
    }

    @Override
    public void mouseReleased( MouseEvent e )
    {
        boolean handled = mouseReleased0( fromWorldwindMouseEvent( e ) );
        
        // if Glimpse handled the MouseEvent, consume it so that WorldWind does not see it
        if ( handled ) e.consume( );
    }

    @Override
    public void mouseDragged( MouseEvent e )
    {
        boolean handled = mouseDragged0( fromWorldwindMouseEvent( e ) );
        
        // if Glimpse handled the MouseEvent, consume it so that WorldWind does not see it
        if ( handled ) e.consume( );
    }

    @Override
    public void mouseMoved( MouseEvent e )
    {
        boolean handled = mouseMoved0( fromWorldwindMouseEvent( e ) );
        
        // if Glimpse handled the MouseEvent, consume it so that WorldWind does not see it
        if ( handled ) e.consume( );
    }

    @Override
    public void mouseWheelMoved( MouseWheelEvent e )
    {
        boolean handled = mouseWheelMoved0( fromWorldwindMouseWheelEvent( e ) );
        
        // if Glimpse handled the MouseEvent, consume it so that WorldWind does not see it
        if ( handled ) e.consume( );
    }
}

package com.metsci.glimpse.event.mouse.newt;

import java.awt.Component;
import java.awt.event.MouseWheelEvent;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseWrapperImpl;
import com.metsci.glimpse.event.mouse.swing.GlimpseMouseWrapper;
import com.metsci.glimpse.event.mouse.swing.GlimpseSwingMouseEvent;
import com.metsci.glimpse.event.mouse.swing.GlimpseSwingMouseWheelEvent;

public class MouseWrapperNewt extends MouseWrapperImpl<MouseEvent> implements MouseListener
{

    public MouseWrapperNewt( GlimpseCanvas canvas )
    {
        super( canvas );
    }
    
    protected Component getComponent( MouseEvent e )
    {
        return (Component) e.getSource( );
    }

    @Override
    protected boolean isInterior( MouseEvent e, GlimpseBounds bounds )
    {
        return bounds.contains( e.getX( ), getComponent( e ).getBounds( ).height - e.getY( ) );
    }

    @Override
    protected boolean isButtonDown( MouseEvent e )
    {
        return ( e.getButtonsDown( ).length ) > 0;
    }

    @Override
    protected boolean isValid( MouseEvent e, GlimpseBounds bounds )
    {
        return getComponent( e ) != null;
    }

    protected MouseEvent toLocalCoords( MouseEvent e, GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        GlimpseBounds bounds = stack.getBounds( );

        if ( bounds == null ) return null;

        if ( getComponent( e ) == null ) return null;

        int parentHeight = getComponent( e ).getBounds( ).height;

        int eventType =  e.getEventType( );
        Component source = getComponent( e );
        long when = e.getWhen( );
        int modifiers = e.getModifiers( );
        int[] local_x = new int[] { e.getX( ) - bounds.getX( ) };
        int[] local_y = new int[] { e.getY( ) - ( parentHeight - ( bounds.getY( ) + bounds.getHeight( ) ) ) };
        float[] pressure = new float[] { e.getPressure( ) };
        int[] pointerIds = getPointerIds( e );
        int clickCount = e.getClickCount( );
        int button = e.getButton( );
        int rotation = e.getWheelRotation( );

        return new MouseEvent( eventType, source, when, modifiers, local_x, local_y, pressure, pointerIds, clickCount, button, rotation );
    }
    
    protected int[] getPointerIds( MouseEvent e )
    {
        int[] ids = new int[ e.getPointerCount( ) ];
        
        for ( int i = 0 ; i < e.getPointerCount( ) ; i++ )
        {
            ids[i] = e.getPointerId( i );
        }
        
        return ids;
    }

    protected MouseWheelEvent toLocalCoordsWheel( MouseEvent e, GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        if ( !(e instanceof MouseWheelEvent) ) return null;

        MouseWheelEvent wheelEvent = (MouseWheelEvent) e;

        GlimpseBounds bounds = stack.getBounds( );

        if ( bounds == null ) return null;

        if ( e.getComponent( ) == null ) return null;

        int parentHeight = e.getComponent( ).getBounds( ).height;

        Component source = e.getComponent( );
        int id = e.getID( );
        long when = e.getWhen( );
        int modifiers = e.getModifiers( );
        int local_x = e.getX( ) - bounds.getX( );
        int local_y = e.getY( ) - ( parentHeight - ( bounds.getY( ) + bounds.getHeight( ) ) );
        int clickCount = e.getClickCount( );
        boolean popupTrigger = e.isPopupTrigger( );
        int scrollType = wheelEvent.getScrollType( );
        int scrollAmount = wheelEvent.getScrollAmount( );
        int wheelRotation = wheelEvent.getWheelRotation( );

        MouseWheelEvent localEvent = new GlimpseSwingMouseWheelEvent( stack, source, id, when, modifiers, local_x, local_y, clickCount, popupTrigger, scrollType, scrollAmount, wheelRotation );

        return localEvent;
    }

    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent e )
    {
        return GlimpseMouseWrapper.fromMouseEvent( e );
    }

    protected GlimpseMouseEvent toGlimpseEventWheel( MouseEvent e )
    {
        return GlimpseMouseWrapper.fromMouseWheelEvent( (MouseWheelEvent) e );
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent e, GlimpseTargetStack stack )
    {
        return toGlimpseEvent( toLocalCoords( e, stack ) );
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEventWheel( MouseEvent e, GlimpseTargetStack stack )
    {
        return toGlimpseEventWheel( toLocalCoordsWheel( e, stack ) );
    }

    @Override
    public void mouseWheelMoved( MouseWheelEvent e )
    {
        mouseWheelMoved0( e );
    }

    @Override
    public void mouseClicked( MouseEvent e )
    {
        mouseClicked0( e );
    }

    @Override
    public void mousePressed( MouseEvent e )
    {
        mousePressed0( e );
    }

    @Override
    public void mouseReleased( MouseEvent e )
    {
        mouseReleased0( e );
    }

    @Override
    public void mouseEntered( MouseEvent e )
    {
        mouseEntered0( e );
    }

    @Override
    public void mouseExited( MouseEvent e )
    {
        mouseExited0( e );
    }

    @Override
    public void mouseDragged( MouseEvent e )
    {
        mouseDragged0( e );
    }

    @Override
    public void mouseMoved( MouseEvent e )
    {
        mouseMoved0( e );
    }
}

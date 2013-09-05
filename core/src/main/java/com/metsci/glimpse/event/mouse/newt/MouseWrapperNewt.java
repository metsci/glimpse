package com.metsci.glimpse.event.mouse.newt;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseEvent.PointerType;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import com.metsci.glimpse.canvas.NewtGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseWrapperImpl;

public class MouseWrapperNewt extends MouseWrapperImpl<MouseEvent> implements MouseListener
{
    protected GLWindow glWindow;

    public MouseWrapperNewt( NewtGlimpseCanvas canvas )
    {
        super( canvas );

        this.glWindow = canvas.getGLWindow( );
    }

    @Override
    protected boolean isInterior( MouseEvent e, GlimpseBounds bounds )
    {
        return bounds.contains( e.getX( ), glWindow.getHeight( ) - e.getY( ) );
    }

    @Override
    protected boolean isButtonDown( MouseEvent e )
    {
        return ( e.getButtonsDown( ).length ) > 0;
    }

    @Override
    protected boolean isValid( MouseEvent e, GlimpseBounds bounds )
    {
        return true;
    }

    protected MouseEvent toLocalCoords( MouseEvent e, GlimpseTargetStack stack )
    {
        if ( stack == null ) return null;

        GlimpseBounds bounds = stack.getBounds( );

        if ( bounds == null ) return null;

        int parentHeight = glWindow.getHeight( );

        short eventType = e.getEventType( );
        Object source = e.getSource( );
        long when = e.getWhen( );
        int modifiers = e.getModifiers( );
        int[] local_x = new int[] { e.getX( ) - bounds.getX( ) };
        int[] local_y = new int[] { e.getY( ) - ( parentHeight - ( bounds.getY( ) + bounds.getHeight( ) ) ) };
        float[] pressure = getPressure( e );
        float maxPressure = e.getMaxPressure( );
        PointerType[] types = getPointerTypes( e );
        short[] pointerIds = getPointerIds( e );
        short clickCount = e.getClickCount( );
        short button = e.getButton( );
        float[] rotation = e.getRotation( );
        float rotationScale = e.getRotationScale( );

        MouseEvent event =  new MouseEvent( eventType, source, when, modifiers, local_x, local_y, pressure, maxPressure, types, pointerIds, clickCount, button, rotation, rotationScale );
        event.setAttachment( stack );
        
        return event;
    }
    
    protected PointerType[] getPointerTypes( MouseEvent e )
    {
        PointerType[] types = new PointerType[e.getPointerCount( )];

        for ( int i = 0; i < e.getPointerCount( ); i++ )
        {
            types[i] = e.getPointerType( i );
        }

        return types;
    }
    
    protected float[] getPressure( MouseEvent e )
    {
        float[] ids = new float[e.getPointerCount( )];

        for ( int i = 0; i < e.getPointerCount( ); i++ )
        {
            ids[i] = e.getPressure( i, false );
        }

        return ids;
    }

    protected short[] getPointerIds( MouseEvent e )
    {
        short[] ids = new short[e.getPointerCount( )];

        for ( int i = 0; i < e.getPointerCount( ); i++ )
        {
            ids[i] = e.getPointerId( i );
        }

        return ids;
    }

    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent e )
    {
        return GlimpseMouseWrapper.fromMouseEvent( e );
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEvent( MouseEvent e, GlimpseTargetStack stack )
    {
        return toGlimpseEvent( toLocalCoords( e, stack ) );
    }

    @Override
    protected GlimpseMouseEvent toGlimpseEventWheel( MouseEvent e, GlimpseTargetStack stack )
    {
        return toGlimpseEvent( toLocalCoords( e, stack ) );
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

    @Override
    public void mouseWheelMoved( MouseEvent e )
    {
        mouseWheelMoved0( e );
    }
}
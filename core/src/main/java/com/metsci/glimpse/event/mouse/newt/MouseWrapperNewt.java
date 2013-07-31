package com.metsci.glimpse.event.mouse.newt;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import com.metsci.glimpse.canvas.SwingGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.MouseWrapperImpl;

public class MouseWrapperNewt extends MouseWrapperImpl<MouseEvent> implements MouseListener
{
    protected GLWindow glWindow;

    public MouseWrapperNewt( SwingGlimpseCanvas canvas )
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

        int eventType = e.getEventType( );
        Object source = e.getSource( );
        long when = e.getWhen( );
        int modifiers = e.getModifiers( );
        int[] local_x = new int[] { e.getX( ) - bounds.getX( ) };
        int[] local_y = new int[] { e.getY( ) - ( parentHeight - ( bounds.getY( ) + bounds.getHeight( ) ) ) };
        float[] pressure = new float[] { e.getPressure( ) };
        int[] pointerIds = getPointerIds( e );
        int clickCount = e.getClickCount( );
        int button = e.getButton( );
        int rotation = e.getWheelRotation( );

        MouseEvent event =  new MouseEvent( eventType, source, when, modifiers, local_x, local_y, pressure, pointerIds, clickCount, button, rotation );
        event.setAttachment( stack );
        
        return event;
    }

    protected int[] getPointerIds( MouseEvent e )
    {
        int[] ids = new int[e.getPointerCount( )];

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
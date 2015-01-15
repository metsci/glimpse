package com.metsci.glimpse.support.swing;

import javax.swing.SwingUtilities;

import com.jogamp.newt.event.MouseEvent;
import com.metsci.glimpse.canvas.NewtGlimpseCanvas;
import com.metsci.glimpse.event.mouse.newt.MouseWrapperNewt;

/**
 * A version of MouseWrapperNewt which fires Glimpse events on the Swing EDT
 * @author ulman
 */
public class MouseWrapperNewtSwingEDT extends MouseWrapperNewt
{

    public MouseWrapperNewtSwingEDT( NewtGlimpseCanvas canvas )
    {
        super( canvas );
    }

    @Override
    public void mouseClicked( final MouseEvent e )
    {
        SwingUtilities.invokeLater( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseClicked0( e );
            }
        } );
    }

    @Override
    public void mousePressed( final MouseEvent e )
    {
        SwingUtilities.invokeLater( new Runnable( )
        {
            @Override
            public void run( )
            {
                mousePressed0( e );
            }
        } );
    }

    @Override
    public void mouseReleased( final MouseEvent e )
    {
        SwingUtilities.invokeLater( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseReleased0( e );
            }
        } );
    }

    @Override
    public void mouseEntered( final MouseEvent e )
    {
        SwingUtilities.invokeLater( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseEntered0( e );
            }
        } );
    }

    @Override
    public void mouseExited( final MouseEvent e )
    {
        SwingUtilities.invokeLater( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseExited0( e );
            }
        } );
    }

    @Override
    public void mouseDragged( final MouseEvent e )
    {
        SwingUtilities.invokeLater( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseDragged0( e );
            }
        } );
    }

    @Override
    public void mouseMoved( final MouseEvent e )
    {
        SwingUtilities.invokeLater( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseMoved0( e );
            }
        } );
    }

    @Override
    public void mouseWheelMoved( final MouseEvent e )
    {
        SwingUtilities.invokeLater( new Runnable( )
        {
            @Override
            public void run( )
            {
                mouseWheelMoved0( e );
            }
        } );
    }
}

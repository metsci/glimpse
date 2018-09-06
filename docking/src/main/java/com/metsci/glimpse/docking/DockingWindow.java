package com.metsci.glimpse.docking;

import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowListener;

import javax.accessibility.Accessible;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.RootPaneContainer;
import javax.swing.WindowConstants;

public interface DockingWindow extends WindowConstants, Accessible, RootPaneContainer
{

    /**
     * Must return {@code this} in order for {@link DockingUtils#getAncestorOfClass(Class, java.awt.Component)}
     * calls to work correctly.
     */
    Window window( );

    MultiSplitPane docker( );

    boolean isMaximizedHorizontally( );

    boolean isMaximizedVertically( );

    /**
     * Returns the bounds the window would have in a "normal" state -- neither maximized nor iconified.
     */
    Rectangle getNormalBounds( );

    /**
     * @see JDialog#setTitle(String)
     * @see JFrame#setTitle(String)
     */
    void setTitle( String title );

    /**
     * @see JDialog#getTitle()
     * @see JFrame#getTitle()
     */
    String getTitle( );

    /**
     * @see JDialog#setDefaultCloseOperation(int)
     * @see JFrame#setDefaultCloseOperation(int)
     */
    void setDefaultCloseOperation( int operation );

    /**
     * @see Window#addWindowListener(WindowListener)
     */
    default void addWindowListener( WindowListener listener )
    {
        this.window( ).addWindowListener( listener );
    }

    /**
     * @see Window#setLocation(int, int)
     */
    default void setLocation( int x, int y )
    {
        this.window( ).setLocation( x, y );
    }

    /**
     * @see Window#pack( )
     */
    default void pack( )
    {
        this.window( ).pack( );
    }

    /**
     * @see Window#getBounds()
     */
    default Rectangle getBounds( )
    {
        return this.window( ).getBounds( );
    }

    /**
     * @see Window#setVisible(boolean)
     */
    default void setVisible( boolean visible )
    {
        this.window( ).setVisible( visible );
    }

    /**
     * @see Window#toFront( )
     */
    default void toFront( )
    {
        this.window( ).toFront( );
    }

    /**
     * @see Window#dispose()
     */
    default void dispose( )
    {
        this.window( ).dispose( );
    }

}

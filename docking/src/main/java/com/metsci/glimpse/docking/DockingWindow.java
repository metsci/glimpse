/*
 * Copyright (c) 2020, Metron, Inc.
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

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
package com.metsci.glimpse.docking;

import static java.awt.ComponentOrientation.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.jdesktop.swingx.MultiSplitLayout.ColSplit;
import org.jdesktop.swingx.MultiSplitLayout.Node;
import org.jdesktop.swingx.MultiSplitLayout.RowSplit;

public class DockingUtils
{

    public static <E> void addAll( List<E> list, int index, E... elements )
    {
        list.addAll( index, Arrays.asList( elements ) );
    }

    public static class IntAndIndex
    {
        public final int value;
        public final int index;

        public IntAndIndex( int value, int index )
        {
            this.value = value;
            this.index = index;
        }
    }

    public static IntAndIndex minValueAndIndex( int... vs )
    {
        int iBest = -1;
        int vBest = Integer.MAX_VALUE;
        for ( int i = 0; i < vs.length; i++ )
        {
            int v = vs[ i ];
            if ( v < vBest )
            {
                iBest = i;
                vBest = v;
            }
        }
        return new IntAndIndex( vBest, iBest );
    }

    public static Point pointRelativeToAncestor( MouseEvent ev, Component ancestor )
    {
        int i = ev.getX( );
        int j = ev.getY( );
        for ( Component c = ev.getComponent( ); c != ancestor; c = c.getParent( ) )
        {
            i += c.getX( );
            j += c.getY( );
        }
        return new Point( i, j );
    }

    public static int xAfter( Rectangle r )
    {
        return r.x + r.width;
    }

    public static int yAfter( Rectangle r )
    {
        return r.y + r.height;
    }

    public static int xAfter( Node node )
    {
        return xAfter( node.getBounds( ) );
    }

    public static int yAfter( Node node )
    {
        return yAfter( node.getBounds( ) );
    }

    public static RowSplit newRowSplit( Rectangle bounds, Node... children )
    {
        RowSplit row = new RowSplit( children );
        row.setBounds( bounds );
        return row;
    }

    public static ColSplit newColSplit( Rectangle bounds, Node... children )
    {
        ColSplit col = new ColSplit( children );
        col.setBounds( bounds );
        return col;
    }

    public static int iround( double d )
    {
        return Math.round( ( float ) d );
    }

    public static <E> ArrayList<E> newArrayList( )
    {
        return new ArrayList<E>( );
    }

    public static <E> ArrayList<E> newArrayList( Collection<? extends E> c )
    {
        return new ArrayList<E>( c );
    }

    public static <K,V> HashMap<K,V> newHashMap( )
    {
        return new HashMap<K,V>( );
    }

    public static <K,V> HashMap<K,V> newHashMap( Map<? extends K,? extends V> m )
    {
        return new HashMap<K,V>( m );
    }

    public static boolean areEqual( Object a, Object b )
    {
        return ( a == b || ( a != null && a.equals( b ) ) );
    }

    public static void requireSwingThread( )
    {
        if ( !SwingUtilities.isEventDispatchThread( ) )
        {
            throw new RuntimeException( "This operation is only allowed on the Swing/AWT event-dispatch thread" );
        }
    }

    public static interface Supplier<T>
    {
        T get( );
    }

    public static interface Runnable1<T>
    {
        void run( T t );
    }

    public static <T> Runnable partial( final Runnable1<T> runnable1, final T t )
    {
        return new Runnable( )
        {
            public void run( )
            {
                runnable1.run( t );
            }
        };
    }

    public static <T> T swingGet( final Supplier<T> supplier )
    {
        try
        {
            final AtomicReference<T> resultRef = new AtomicReference<T>( );
            SwingUtilities.invokeAndWait( new Runnable( )
            {
                public void run( )
                {
                    resultRef.set( supplier.get( ) );
                }
            } );
            return resultRef.get( );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public static void swingRun( final Runnable runnable )
    {
        try
        {
            SwingUtilities.invokeAndWait( new Runnable( )
            {
                public void run( )
                {
                    runnable.run( );
                }
            } );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public static <T> void swingRun( final Runnable1<T> runnable1, final T t )
    {
        swingRun( partial( runnable1, t ) );
    }

    public static JPopupMenu newButtonPopup( final JToggleButton button )
    {
        final JPopupMenu popup = new JPopupMenu( );

        button.addActionListener( new ActionListener( )
        {
            public void actionPerformed( ActionEvent ev )
            {
                if ( button.isSelected( ) )
                {
                    popup.show( button, 0, button.getHeight( ) );
                }
                else
                {
                    popup.setVisible( false );
                }
            }
        } );

        popup.addPopupMenuListener( new PopupMenuListener( )
        {
            public void popupMenuWillBecomeInvisible( PopupMenuEvent ev )
            {
                button.setSelected( false );

                // If this popup-hide was triggered by a mouse-press, then don't allow
                // that mouse-press to begin a click of the button (which would toggle
                // the popup back to visible again)
                //
                button.setEnabled( false );
                SwingUtilities.invokeLater( new Runnable( )
                {
                    public void run( )
                    {
                        button.setEnabled( true );
                    }
                } );
            }

            public void popupMenuWillBecomeVisible( PopupMenuEvent ev ) { }
            public void popupMenuCanceled( PopupMenuEvent ev ) { }
        } );

        return popup;
    }

    public static JToolBar newToolbar( boolean anchorRight )
    {
        JToolBar toolbar;

        if ( anchorRight )
        {
            toolbar = new JToolBar( )
            {
                protected void addImpl( Component c, Object constraints, int index )
                {
                    int reverseIndex;
                    if ( index == -1 )
                    {
                        reverseIndex = 0;
                    }
                    else
                    {
                        int oldIndex = getComponentIndex( c );
                        if ( oldIndex >= 0 && index > oldIndex )
                        {
                            index--;
                        }

                        reverseIndex = getComponentCount( ) - index;
                    }

                    super.addImpl( c, constraints, reverseIndex );
                }
            };
            toolbar.setComponentOrientation( RIGHT_TO_LEFT );
        }
        else
        {
            toolbar = new JToolBar( );
        }

        toolbar.setBorderPainted( false );
        toolbar.setFloatable( false );
        toolbar.setRollover( true );
        toolbar.setOpaque( false );
        return toolbar;
    }

    public static Color getUiColor( Object key, Color fallback )
    {
        Color color = UIManager.getColor( key );
        return ( color == null ? fallback : color );
    }

    public static File createAppDir( String appName )
    {
        String homePath = System.getProperty( "user.home" );
        if ( homePath == null ) throw new RuntimeException( "Property user.home is not defined" );

        File appDir = new File( homePath, "." + appName );
        appDir.mkdirs( );

        if ( !appDir.isDirectory( ) ) throw new RuntimeException( "Failed to create app dir: " + appDir.getAbsolutePath( ) );
        if ( !appDir.canRead( ) ) throw new RuntimeException( "Do not have read permission on app dir: " + appDir.getAbsolutePath( ) );
        if ( !appDir.canWrite( ) ) throw new RuntimeException( "Do not have write permission on app dir: " + appDir.getAbsolutePath( ) );

        return appDir;
    }

}

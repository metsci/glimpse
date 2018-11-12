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
package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingUtils.getAncestorOfClass;
import static java.awt.Frame.ICONIFIED;
import static java.lang.Math.round;
import static java.util.Collections.reverse;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import com.metsci.glimpse.util.var.Disposable;

/**
 * These utility methods aren't specific to docking, but are used internally by
 * the docking code. They are kept out of DockingUtils not to hide them, but to
 * avoid static-import collisions with other utility classes.
 *
 */
public class MiscUtils
{

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
            int v = vs[i];
            if ( v < vBest )
            {
                iBest = i;
                vBest = v;
            }
        }
        return new IntAndIndex( vBest, iBest );
    }

    public static boolean containsScreenPoint( Component c, Point pOnScreen )
    {
        if ( !c.isShowing( ) )
        {
            return false;
        }

        Window w = getAncestorOfClass( Window.class, c );
        if ( w == null || !w.isVisible( ) )
        {
            return false;
        }

        if ( w instanceof Frame && ( ( ( Frame ) w ).getExtendedState( ) & ICONIFIED ) != 0 )
        {
            return false;
        }

        Point pInComponent = convertPointFromScreen( pOnScreen, c );
        return c.contains( pInComponent );
    }

    public static Point convertPointFromScreen( Point pOnScreen, Component c )
    {
        Point pInC = new Point( pOnScreen );
        SwingUtilities.convertPointFromScreen( pInC, c );
        return pInC;
    }

    public static Point convertPointToScreen( Component c, Point pInC )
    {
        Point pOnScreen = new Point( pInC );
        SwingUtilities.convertPointToScreen( pOnScreen, c );
        return pOnScreen;
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

    public static Disposable onWindowStateChanged( Window w, Runnable fn )
    {
        return addWindowListener( w, new WindowAdapter( )
        {
            @Override
            public void windowStateChanged( WindowEvent ev )
            {
                fn.run( );
            }
        } );
    }

    public static Disposable addWindowListener( Window w, WindowListener listener )
    {
        w.addWindowListener( listener );

        return ( ) ->
        {
            w.removeWindowListener( listener );
        };
    }

    public static Disposable onComponentMoved( Component c, Runnable fn )
    {
        return addComponentListener( c, new ComponentAdapter( )
        {
            @Override
            public void componentMoved( ComponentEvent ev )
            {
                fn.run( );
            }
        } );
    }

    public static Disposable onComponentResized( Component c, Runnable fn )
    {
        return addComponentListener( c, new ComponentAdapter( )
        {
            @Override
            public void componentResized( ComponentEvent ev )
            {
                fn.run( );
            }
        } );
    }

    public static Disposable addComponentListener( Component c, ComponentListener listener )
    {
        c.addComponentListener( listener );

        return ( ) ->
        {
            c.removeComponentListener( listener );
        };
    }

    public static Border createEmptyBorder( int size )
    {
        return BorderFactory.createEmptyBorder( size, size, size, size );
    }

    public static Box createVerticalBox( Component... cs )
    {
        Box box = Box.createVerticalBox( );
        for ( Component c : cs )
            box.add( c );
        return box;
    }

    public static int iround( double d )
    {
        return round( ( float ) d );
    }

    public static boolean areEqual( Object a, Object b )
    {
        return ( a == b || ( a != null && a.equals( b ) ) );
    }

    public static <T> Set<T> union( Set<? extends T> a, Set<? extends T> b )
    {
        Set<T> union = new LinkedHashSet<T>( );
        if ( a != null ) union.addAll( a );
        if ( b != null ) union.addAll( b );
        return union;
    }

    public static <T> Set<T> intersection( Set<? extends T> a, Set<? extends T> b )
    {
        Set<T> intersection = new LinkedHashSet<T>( );
        if ( a != null && b != null )
        {
            intersection.addAll( a );
            intersection.retainAll( b );
        }
        return intersection;
    }

    public static <T> List<T> reversed( Collection<T> list )
    {
        List<T> reversed = new ArrayList<>( list );
        reverse( reversed );
        return reversed;
    }

}

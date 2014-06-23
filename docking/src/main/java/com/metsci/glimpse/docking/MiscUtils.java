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

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.jdesktop.swingx.MultiSplitLayout.ColSplit;
import org.jdesktop.swingx.MultiSplitLayout.Node;
import org.jdesktop.swingx.MultiSplitLayout.RowSplit;

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

    public static Rectangle innerBounds( JComponent c )
    {
        Insets insets = c.getInsets( );
        return new Rectangle( insets.left,
                              insets.top,
                              c.getWidth( ) - insets.left - insets.right,
                              c.getHeight( ) - insets.top - insets.bottom );
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

}

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

import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.TOP;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

public class DockingPane extends JPanel
{

    protected final int gapSize;
    protected Component root;
    protected final Set<Component> tiles;


    public DockingPane( int gapSize )
    {
        super( new BorderLayout( ) );

        this.gapSize = gapSize;

        this.root = null;
        this.tiles = new HashSet<>( );
    }

    public void addInitialTile( Component c )
    {
        if ( root != null ) throw new RuntimeException( "At least one tile already exists" );

        add( c );

        this.root = c;
        tiles.add( c );

        validate( );
        repaint( );
    }

    public void addEdgeTile( Component c, Side edgeOfPane )
    {
        addEdgeTile( c, edgeOfPane, 0.25 );
    }

    public void addEdgeTile( Component c, Side edgeOfPane, double extentFrac )
    {
        if ( root == null )
        {
            addInitialTile( c );
        }
        else
        {
            addNeighborTile( c, root, edgeOfPane, extentFrac );
        }
    }

    public void addNeighborTile( Component c, Component neighbor, Side sideOfNeighbor )
    {
        addNeighborTile( c, neighbor, sideOfNeighbor, 0.5 );
    }

    public void addNeighborTile( Component c, Component neighbor, Side sideOfNeighbor, double extentFrac )
    {
        if ( extentFrac < 0 || extentFrac > 1 ) throw new IllegalArgumentException( "Value for extentFrac is outside [0,1]: " + extentFrac );

        boolean arrangeVertically = ( sideOfNeighbor == TOP || sideOfNeighbor == BOTTOM );
        boolean newIsChildA = ( sideOfNeighbor == LEFT || sideOfNeighbor == TOP );
        double splitFrac = ( newIsChildA ? extentFrac : 1 - extentFrac );
        SplitPane newSplitPane = new SplitPane( arrangeVertically, splitFrac, gapSize );

        Container parent = neighbor.getParent( );
        if ( parent == this )
        {
            this.remove( neighbor );
            this.add( newSplitPane );
            this.root = newSplitPane;
        }
        else
        {
            Object constraints = ( ( SplitPane ) parent ).getConstraints( neighbor );
            parent.remove( neighbor );
            parent.add( newSplitPane, constraints );
        }

        newSplitPane.add( neighbor, ( newIsChildA ? "B" : "A" ) );
        newSplitPane.add( c, ( newIsChildA ? "A" : "B" ) );

        tiles.add( c );

        validate( );
        repaint( );
    }

    public Component findTileAt( int x, int y )
    {
        if ( contains( x, y ) )
        {
            return _findTileAt( this, x, y );
        }
        else
        {
            return null;
        }
    }

    protected Component _findTileAt( Container parent, int xInParent, int yInParent )
    {
        for ( int i = 0, N = parent.getComponentCount( ); i < N; i++ )
        {
            Component child = parent.getComponent( i );
            if ( child != null && child.isVisible( ) )
            {
                int xInChild = xInParent - child.getX( );
                int yInChild = yInParent - child.getY( );

                if ( child.contains( xInChild, yInChild ) )
                {
                    if ( tiles.contains( child ) )
                    {
                        return child;
                    }
                    else if ( child instanceof Container )
                    {
                        return _findTileAt( ( Container ) child, xInChild, yInChild );
                    }
                }
            }
        }
        return null;
    }

    public void removeTile( Component c )
    {
        if ( !tiles.contains( c ) ) throw new RuntimeException( "Component is not a tile" );

        Container parent = c.getParent( );
        if ( parent == this )
        {
            this.remove( c );
            this.root = null;
        }
        else
        {
            Component sibling = ( ( SplitPane ) parent ).getSibling( c );

            parent.removeAll( );

            Container grandparent = parent.getParent( );
            if ( grandparent == this )
            {
                this.remove( parent );
                this.add( sibling );
                this.root = sibling;
            }
            else
            {
                Object constraints = ( ( SplitPane ) grandparent ).getConstraints( parent );
                grandparent.remove( parent );
                grandparent.add( sibling, constraints );
            }
        }

        tiles.remove( c );

        validate( );
        repaint( );
    }

}

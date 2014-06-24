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

import static com.metsci.glimpse.docking.MiscUtils.createEmptyBorder;
import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.TOP;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

public class DockingPane extends JPanel
{

    protected final CardLayout layout;
    protected final JPanel maximizedTileCard;
    protected final JPanel allTilesCard;

    protected final Component maximizedPlaceholder;
    protected Component maximizedTile;

    protected final int gapSize;
    protected Component root;
    protected final Set<Component> tiles;


    public DockingPane( int gapSize )
    {
        this.layout = new CardLayout( );
        setLayout( layout );
        setBorder( createEmptyBorder( gapSize/2 ) );

        this.maximizedTileCard = new JPanel( new BorderLayout( ) );
        add( maximizedTileCard, "maximizedTile" );

        this.allTilesCard = new JPanel( new BorderLayout( ) );
        add( allTilesCard, "allTiles" );

        layout.show( this, "allTiles" );

        this.maximizedPlaceholder = new JPanel( );
        this.maximizedTile = null;

        this.gapSize = gapSize;
        this.root = null;
        this.tiles = new HashSet<>( );
    }

    public void addInitialTile( Component c )
    {
        if ( root != null ) throw new RuntimeException( "At least one tile already exists" );

        allTilesCard.add( c );

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

        if ( neighbor == maximizedTile )
        {
            neighbor = maximizedPlaceholder;
        }

        Container parent = neighbor.getParent( );
        if ( parent == allTilesCard )
        {
            allTilesCard.remove( neighbor );
            allTilesCard.add( newSplitPane );
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

    public int numTiles( )
    {
        return tiles.size( );
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

        if ( maximizedTile == c )
        {
            _unmaximizeTile( );
        }

        Container parent = c.getParent( );
        if ( parent == allTilesCard )
        {
            allTilesCard.remove( c );
            this.root = null;
        }
        else
        {
            Component sibling = ( ( SplitPane ) parent ).getSibling( c );

            parent.removeAll( );

            Container grandparent = parent.getParent( );
            if ( grandparent == allTilesCard )
            {
                allTilesCard.remove( parent );
                allTilesCard.add( sibling );
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

    public Component getMaximizedTile( )
    {
        return maximizedTile;
    }

    public void maximizeTile( Component c )
    {
        if ( !tiles.contains( c ) ) throw new RuntimeException( "Component is not a tile" );

        if ( maximizedTile == c )
        {
            return;
        }
        else if ( maximizedTile != null )
        {
            _unmaximizeTile( );
        }

        Container parent = c.getParent( );
        if ( parent == allTilesCard )
        {
            allTilesCard.add( maximizedPlaceholder );
            this.root = maximizedPlaceholder;
        }
        else
        {
            Object constraints = ( ( SplitPane ) parent ).getConstraints( c );
            parent.remove( c );
            parent.add( maximizedPlaceholder, constraints );
        }

        maximizedTileCard.add( c );
        layout.show( this, "maximizedTile" );

        this.maximizedTile = c;

        validate( );
        repaint( );
    }

    public void unmaximizeTile( )
    {
        if ( maximizedTile != null )
        {
            _unmaximizeTile( );
            validate( );
            repaint( );
        }
    }

    protected void _unmaximizeTile( )
    {
        maximizedTileCard.remove( maximizedTile );

        Container parent = maximizedPlaceholder.getParent( );
        if ( parent == allTilesCard )
        {
            allTilesCard.remove( maximizedPlaceholder );
            allTilesCard.add( maximizedTile );
            this.root = maximizedTile;
        }
        else
        {
            Object constraints = ( ( SplitPane ) parent ).getConstraints( maximizedPlaceholder );
            parent.remove( maximizedPlaceholder );
            parent.add( maximizedTile, constraints );
        }

        layout.show( this, "allTiles" );

        this.maximizedTile = null;
    }


    // Snapshots
    //

    public Node getSnapshot( )
    {
        return createSnapshot( root );
    }

    protected Node createSnapshot( Component c )
    {
        if ( c instanceof SplitPane )
        {
            SplitPane s = ( SplitPane ) c;
            return new Split( s.arrangeVertically, s.splitFrac, createSnapshot( s.getChildA( ) ), createSnapshot( s.getChildB( ) ) );
        }
        else if ( c == maximizedPlaceholder )
        {
            return new Leaf( maximizedTile, true );
        }
        else if ( c != null )
        {
            return new Leaf( c, false );
        }
        else
        {
            return null;
        }
    }

    public static abstract class Node
    { }

    public static class Split extends Node
    {
        public final boolean arrangeVertically;
        public final double splitFrac;
        public final Node childA;
        public final Node childB;

        public Split( boolean arrangeVertically, double splitFrac, Node childA, Node childB )
        {
            this.arrangeVertically = arrangeVertically;
            this.splitFrac = splitFrac;
            this.childA = childA;
            this.childB = childB;
        }
    }

    public static class Leaf extends Node
    {
        public final Component component;
        public final boolean isMaximized;

        public Leaf( Component component, boolean isMaximized )
        {
            this.component = component;
            this.isMaximized = isMaximized;
        }
    }

}

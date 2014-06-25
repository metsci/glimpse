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
import static com.metsci.glimpse.docking.SplitPane.CHILD_A;
import static com.metsci.glimpse.docking.SplitPane.CHILD_B;
import static java.util.Collections.unmodifiableSet;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JPanel;

public class DockingPane extends JPanel
{
    protected static final String MAXIMIZED_LEAF_CARD = "maximizedLeaf";
    protected static final String ALL_LEAVES_CARD = "allLeaves";


    protected final CardLayout layout;
    protected final JPanel maximizedLeafCard;
    protected final JPanel allLeavesCard;

    protected final Component maximizedPlaceholder;
    protected Component maximizedLeaf;

    protected final int gapSize;
    protected Component allLeavesRoot;
    protected final Set<Component> leaves;
    protected final Set<Component> leavesUnmod;


    public DockingPane( int gapSize )
    {
        this.layout = new CardLayout( );
        setLayout( layout );
        setBorder( createEmptyBorder( gapSize/2 ) );

        this.maximizedLeafCard = new JPanel( new BorderLayout( ) );
        add( maximizedLeafCard, MAXIMIZED_LEAF_CARD );

        this.allLeavesCard = new JPanel( new BorderLayout( ) );
        add( allLeavesCard, ALL_LEAVES_CARD );

        layout.show( this, ALL_LEAVES_CARD );

        this.maximizedPlaceholder = new JPanel( );
        this.maximizedLeaf = null;

        this.gapSize = gapSize;
        this.allLeavesRoot = null;
        this.leaves = new LinkedHashSet<>( );
        this.leavesUnmod = unmodifiableSet( leaves );
    }

    public void addInitialLeaf( Component c )
    {
        if ( allLeavesRoot != null || !leaves.isEmpty( ) ) throw new RuntimeException( "At least one leaf already exists" );

        allLeavesCard.add( c );

        this.allLeavesRoot = c;
        leaves.add( c );

        validate( );
        repaint( );
    }

    public void addEdgeLeaf( Component c, Side edgeOfPane )
    {
        addEdgeLeaf( c, edgeOfPane, 0.25 );
    }

    public void addEdgeLeaf( Component c, Side edgeOfPane, double extentFrac )
    {
        if ( allLeavesRoot == null )
        {
            addInitialLeaf( c );
        }
        else
        {
            addNeighborLeaf( c, allLeavesRoot, edgeOfPane, extentFrac );
        }
    }

    public void addNeighborLeaf( Component c, Component neighbor, Side sideOfNeighbor )
    {
        addNeighborLeaf( c, neighbor, sideOfNeighbor, 0.5 );
    }

    public void addNeighborLeaf( Component c, Component neighbor, Side sideOfNeighbor, double extentFrac )
    {
        if ( extentFrac < 0 || extentFrac > 1 ) throw new IllegalArgumentException( "Value for extentFrac is outside [0,1]: " + extentFrac );

        boolean arrangeVertically = ( sideOfNeighbor == TOP || sideOfNeighbor == BOTTOM );
        boolean newIsChildA = ( sideOfNeighbor == LEFT || sideOfNeighbor == TOP );
        double splitFrac = ( newIsChildA ? extentFrac : 1 - extentFrac );
        SplitPane newSplitPane = new SplitPane( arrangeVertically, splitFrac, gapSize );

        if ( neighbor == maximizedLeaf )
        {
            neighbor = maximizedPlaceholder;
        }

        Container parent = neighbor.getParent( );
        if ( parent == allLeavesCard )
        {
            allLeavesCard.remove( neighbor );
            allLeavesCard.add( newSplitPane );
            this.allLeavesRoot = newSplitPane;
        }
        else
        {
            Object constraints = ( ( SplitPane ) parent ).getConstraints( neighbor );
            parent.remove( neighbor );
            parent.add( newSplitPane, constraints );
        }

        newSplitPane.add( neighbor, ( newIsChildA ? CHILD_B : CHILD_A ) );
        newSplitPane.add( c, ( newIsChildA ? CHILD_A : CHILD_B ) );

        leaves.add( c );

        validate( );
        repaint( );
    }

    public int numLeaves( )
    {
        return leaves.size( );
    }

    public Set<Component> leaves( )
    {
        return leavesUnmod;
    }

    public Component findLeafAt( int x, int y )
    {
        if ( contains( x, y ) )
        {
            return _findLeafAt( this, x, y );
        }
        else
        {
            return null;
        }
    }

    protected Component _findLeafAt( Container parent, int xInParent, int yInParent )
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
                    if ( leaves.contains( child ) )
                    {
                        return child;
                    }
                    else if ( child instanceof Container )
                    {
                        return _findLeafAt( ( Container ) child, xInChild, yInChild );
                    }
                }
            }
        }
        return null;
    }

    public void removeLeaf( Component c )
    {
        if ( !leaves.contains( c ) ) throw new RuntimeException( "Component is not a leaf" );

        if ( maximizedLeaf == c )
        {
            _unmaximizeLeaf( );
        }

        Container parent = c.getParent( );
        if ( parent == allLeavesCard )
        {
            allLeavesCard.remove( c );
            this.allLeavesRoot = null;
        }
        else
        {
            Component sibling = ( ( SplitPane ) parent ).getSibling( c );

            parent.removeAll( );

            Container grandparent = parent.getParent( );
            if ( grandparent == allLeavesCard )
            {
                allLeavesCard.remove( parent );
                allLeavesCard.add( sibling );
                this.allLeavesRoot = sibling;
            }
            else
            {
                Object constraints = ( ( SplitPane ) grandparent ).getConstraints( parent );
                grandparent.remove( parent );
                grandparent.add( sibling, constraints );
            }
        }

        leaves.remove( c );

        validate( );
        repaint( );
    }

    public Component getMaximizedLeaf( )
    {
        return maximizedLeaf;
    }

    public void maximizeLeaf( Component c )
    {
        if ( !leaves.contains( c ) ) throw new RuntimeException( "Component is not a leaf" );

        if ( maximizedLeaf == c )
        {
            return;
        }
        else if ( maximizedLeaf != null )
        {
            _unmaximizeLeaf( );
        }

        Container parent = c.getParent( );
        if ( parent == allLeavesCard )
        {
            allLeavesCard.add( maximizedPlaceholder );
            this.allLeavesRoot = maximizedPlaceholder;
        }
        else
        {
            Object constraints = ( ( SplitPane ) parent ).getConstraints( c );
            parent.remove( c );
            parent.add( maximizedPlaceholder, constraints );
        }

        maximizedLeafCard.add( c );
        layout.show( this, MAXIMIZED_LEAF_CARD );

        this.maximizedLeaf = c;

        validate( );
        repaint( );
    }

    public void unmaximizeLeaf( )
    {
        if ( maximizedLeaf != null )
        {
            _unmaximizeLeaf( );
            validate( );
            repaint( );
        }
    }

    protected void _unmaximizeLeaf( )
    {
        maximizedLeafCard.remove( maximizedLeaf );

        Container parent = maximizedPlaceholder.getParent( );
        if ( parent == allLeavesCard )
        {
            allLeavesCard.remove( maximizedPlaceholder );
            allLeavesCard.add( maximizedLeaf );
            this.allLeavesRoot = maximizedLeaf;
        }
        else
        {
            Object constraints = ( ( SplitPane ) parent ).getConstraints( maximizedPlaceholder );
            parent.remove( maximizedPlaceholder );
            parent.add( maximizedLeaf, constraints );
        }

        layout.show( this, ALL_LEAVES_CARD );

        this.maximizedLeaf = null;
    }


    // Snapshots
    //

    public void restore( Node rootNode )
    {
        if ( allLeavesRoot != null || !leaves.isEmpty( ) ) throw new RuntimeException( "At least one leaf already exists" );

        Component newRoot = toComponentTree( rootNode, this.leaves );

        allLeavesCard.add( newRoot );
        this.allLeavesRoot = newRoot;

        validate( );
        repaint( );
    }

    protected Component toComponentTree( Node node, Set<Component> leaves_OUT )
    {
        if ( node instanceof Split )
        {
            Split split = ( Split ) node;
            Component childA = toComponentTree( split.childA, leaves_OUT );
            Component childB = toComponentTree( split.childB, leaves_OUT );

            if ( childA != null && childB != null )
            {
                SplitPane splitPane = new SplitPane( split.arrangeVertically, split.splitFrac, gapSize );
                splitPane.add( childA, CHILD_A );
                splitPane.add( childB, CHILD_B );
                return splitPane;
            }
            else if ( childA != null )
            {
                return childA;
            }
            else if ( childB != null )
            {
                return childB;
            }
            else
            {
                return null;
            }
        }
        else if ( node instanceof Leaf )
        {
            Component c = ( ( Leaf ) node ).component;
            if ( c != null )
            {
                leaves_OUT.add( c );
            }
            return c;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of " + Node.class.getName( ) + ": " + node.getClass( ).getName( ) );
        }
    }

    public Node snapshot( )
    {
        return createSnapshot( allLeavesRoot );
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
            return new Leaf( maximizedLeaf, true );
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

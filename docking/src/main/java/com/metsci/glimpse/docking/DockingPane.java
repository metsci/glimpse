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
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JRootPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;
import org.jdesktop.swingx.MultiSplitLayout.ColSplit;
import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;
import org.jdesktop.swingx.MultiSplitLayout.Node;
import org.jdesktop.swingx.MultiSplitLayout.RowSplit;
import org.jdesktop.swingx.MultiSplitLayout.Split;

import com.metsci.glimpse.docking.DockingPane.Arrangement.ArrangementLeaf;
import com.metsci.glimpse.docking.DockingPane.Arrangement.ArrangementNode;
import com.metsci.glimpse.docking.DockingPane.Arrangement.ArrangementSplit;
import com.metsci.glimpse.docking.DockingUtils.IntAndIndex;
import com.metsci.glimpse.docking.DockingUtils.Runnable1;
import com.metsci.glimpse.docking.DockingUtils.Supplier;

import static com.metsci.glimpse.docking.DockingThemes.*;
import static com.metsci.glimpse.docking.DockingUtils.*;
import static java.awt.Color.*;
import static java.awt.event.InputEvent.*;
import static java.awt.event.MouseEvent.*;
import static java.lang.Math.*;
import static javax.swing.BorderFactory.*;
import static javax.xml.bind.Marshaller.*;

public class DockingPane extends JRootPane
{

    protected final Map<ViewKey,View> viewsByKey;
    protected final Map<ViewKey,TileKey> tileKeys;
    protected final Map<TileKey,JButton> tileMaximizeButtons;

    protected final DockingTheme theme;
    protected final DockingIndicatorOverlay indicatorOverlay;
    protected final JXMultiSplitPane splitPane;

    protected TileKey maximizedTileKey;
    protected ArrangementNode arrangementBeforeMaximize;

    protected int nextLeafNumber;


    public DockingPane( )
    {
        this( defaultDockingTheme );
    }

    public DockingPane( DockingTheme theme )
    {
        this.viewsByKey = newHashMap( );
        this.tileKeys = newHashMap( );
        this.tileMaximizeButtons = newHashMap( );

        this.theme = theme;

        this.indicatorOverlay = new DockingIndicatorOverlay( black, 2, true );

        this.splitPane = new JXMultiSplitPane( new MultiSplitLayout( )
        {
            public void layoutContainer( Container parent )
            {
                // Set weights based on current sizes, so that the "layoutGrow" step
                // in super.layoutContainer will divvy up extra space sensibly
                //
                // This is not perfect. If you shrink the pane down far enough, some
                // tiles will hit their min sizes and stop shrinking. When you then
                // re-expand the pane, the relative sizes of such tiles won't go back
                // to what it was originally.
                //
                setWeights( getModel( ) );

                super.layoutContainer( parent );
            }

            protected void setWeights( Node node )
            {
                if ( node instanceof Split )
                {
                    Split split = ( Split ) node;

                    double totalExtent = 0;
                    for ( Node child : split.getChildren( ) )
                    {
                        if ( child instanceof Split || child instanceof Leaf )
                        {
                            double childExtent = ( split.isRowLayout( ) ? child.getBounds( ).width : child.getBounds( ).height );
                            totalExtent += childExtent;
                        }
                    }

                    for ( Node child : split.getChildren( ) )
                    {
                        if ( child instanceof Split || child instanceof Leaf )
                        {
                            double childExtent = ( split.isRowLayout( ) ? child.getBounds( ).width : child.getBounds( ).height );
                            child.setWeight( childExtent / totalExtent );
                            setWeights( child );
                        }
                    }
                }
            }
        } );
        splitPane.getMultiSplitLayout( ).setRemoveDividers( true );
        splitPane.getMultiSplitLayout( ).setFloatingDividers( false );
        splitPane.setDividerSize( theme.dividerSize );
        splitPane.setBorder( createEmptyBorder( theme.dividerSize, theme.dividerSize, theme.dividerSize, theme.dividerSize ) );

        setContentPane( splitPane );
        setGlassPane( indicatorOverlay );
        indicatorOverlay.setVisible( true );

        this.maximizedTileKey = null;
        this.arrangementBeforeMaximize = null;

        this.nextLeafNumber = 0;
    }

    public void addView( View view )
    {
        addView( view, chooseDefaultTile( view.viewKey ) );
    }

    public void addView( View view, TileKey tileKey )
    {
        int viewPos = tile( tileKey ).numViews( );
        addView( view, tileKey, viewPos );
    }

    public void addView( View view, TileKey tileKey, int viewPos )
    {
        if ( maximizedTileKey != null && !areEqual( tileKey, maximizedTileKey ) )
        {
            unmaximizeTile( );
        }

        ViewKey viewKey = view.viewKey;
        if ( viewsByKey.containsKey( viewKey ) ) throw new RuntimeException( "View-key has already been added: view-id = " + viewKey.viewId );

        viewsByKey.put( viewKey, view );
        tileKeys.put( viewKey, tileKey );

        tile( tileKey ).addView( view, viewPos );
    }

    public View removeView( ViewKey viewKey )
    {
        if ( !viewsByKey.containsKey( viewKey ) ) throw new RuntimeException( "No such view: view-id = " + viewKey.viewId );
        if ( !tileKeys.containsKey( viewKey ) ) throw new RuntimeException( "No tile-key for this view: view-id = " + viewKey.viewId );

        View view = viewsByKey.remove( viewKey );
        TileKey tileKey = tileKeys.remove( viewKey );

        tile( tileKey ).removeView( view );

        removeEmptyNodes( );

        return view;
    }

    public TileKey addNewTile( Side edgeOfPane )
    {
        Node neighbor = splitPane.getMultiSplitLayout( ).getModel( );
        return addNewTile( neighbor, edgeOfPane, 0.25 );
    }

    public TileKey addNewTile( TileKey neighborKey, Side sideOfNeighbor )
    {
        Node neighbor = splitPane.getMultiSplitLayout( ).getNodeForName( neighborKey.leafId );
        return addNewTile( neighbor, sideOfNeighbor, 0.5 );
    }

    protected TileKey addNewTile( Node neighbor, Side sideOfNeighbor, double extentFraction )
    {
        if ( extentFraction < 0 || extentFraction > 1 ) throw new IllegalArgumentException( "Value for extentFraction is outside [0,1]: " + extentFraction );

        if ( maximizedTileKey != null )
        {
            unmaximizeTile( );
        }

        String newLeafId = nextLeafId( );
        Leaf newLeaf = new Leaf( newLeafId );
        Divider newDivider = new Divider( );
        Split parent = neighbor.getParent( );

        Rectangle bounds = neighbor.getBounds( );

        if ( parent == null )
        {
            switch ( sideOfNeighbor )
            {
                case LEFT:   splitPane.setModel( newRowSplit( bounds, newLeaf, newDivider, neighbor ) ); break;
                case RIGHT:  splitPane.setModel( newRowSplit( bounds, neighbor, newDivider, newLeaf ) ); break;
                case TOP:    splitPane.setModel( newColSplit( bounds, newLeaf, newDivider, neighbor ) ); break;
                case BOTTOM: splitPane.setModel( newColSplit( bounds, neighbor, newDivider, newLeaf ) ); break;
            }
        }
        else
        {
            List<Node> siblings = parent.getChildren( );
            int neighborIndex = siblings.indexOf( neighbor );

            // Calling parent.setChildren( ) has the side effect of calling child.setParent( null )
            // for each child. When a child is becoming a grandchild, this can result in a grandchild
            // with a null parent -- unless we pre-emptively clear parent's child list up front.
            //
            parent.setChildren( Collections.<Node>emptyList( ) );

            if ( parent.isRowLayout( ) )
            {
                switch ( sideOfNeighbor )
                {
                    case LEFT:   addAll( siblings, neighborIndex, newLeaf, newDivider ); break;
                    case RIGHT:  addAll( siblings, neighborIndex+1, newDivider, newLeaf ); break;
                    case TOP:    siblings.set( neighborIndex, newColSplit( bounds, newLeaf, newDivider, neighbor ) ); break;
                    case BOTTOM: siblings.set( neighborIndex, newColSplit( bounds, neighbor, newDivider, newLeaf ) ); break;
                }
            }
            else
            {
                switch ( sideOfNeighbor )
                {
                    case LEFT:   siblings.set( neighborIndex, newRowSplit( bounds, newLeaf, newDivider, neighbor ) ); break;
                    case RIGHT:  siblings.set( neighborIndex, newRowSplit( bounds, neighbor, newDivider, newLeaf ) ); break;
                    case TOP:    addAll( siblings, neighborIndex, newLeaf, newDivider ); break;
                    case BOTTOM: addAll( siblings, neighborIndex+1, newDivider, newLeaf ); break;
                }
            }

            parent.setChildren( siblings );
        }

        switch ( sideOfNeighbor )
        {
            case LEFT:   splitBounds( bounds, true,  extentFraction,   newLeaf,  newDivider, neighbor ); break;
            case RIGHT:  splitBounds( bounds, true,  1-extentFraction, neighbor, newDivider, newLeaf  ); break;
            case TOP:    splitBounds( bounds, false, extentFraction,   newLeaf,  newDivider, neighbor ); break;
            case BOTTOM: splitBounds( bounds, false, 1-extentFraction, neighbor, newDivider, newLeaf  ); break;
        }

        return initTile( newLeafId );
    }

    public void maximizeTile( TileKey tileKey )
    {
        if ( maximizedTileKey != null )
        {
            unmaximizeTile( );
        }

        this.maximizedTileKey = tileKey;
        this.arrangementBeforeMaximize = captureArrangement( );

        // Probably safer to always have at least one leaf visible
        MultiSplitLayout layout = splitPane.getMultiSplitLayout( );
        layout.displayNode( tileKey.leafId, true );
        for ( TileKey tileKey0 : tileKeys.values( ) )
        {
            if ( !areEqual( tileKey0, tileKey ) )
            {
                layout.displayNode( tileKey0.leafId, false );
            }
        }

        refreshMaximizeButtons( );
    }

    public void unmaximizeTile( )
    {
        if ( maximizedTileKey != null )
        {
            // XXX: This isn't enough -- views may have changed order as well

            // In maximized tile, selected view should stay selected
            View selectedView = tile( maximizedTileKey ).selectedView( );

            // Must do this first, or restoreArrangement would call unmaximizeTile and trigger runaway recursion
            this.maximizedTileKey = null;

            restoreArrangement( arrangementBeforeMaximize );
            this.arrangementBeforeMaximize = null;

            tile( tileKeys.get( selectedView.viewKey ) ).selectView( selectedView );

            refreshMaximizeButtons( );
        }
    }

    protected void splitBounds( Rectangle b, boolean isHorizSplit, double fraction1, Node node1, Divider nodeD, Node node2 )
    {
        if ( isHorizSplit )
        {
            int wD = splitPane.getDividerSize( );
            int wBoth = b.width - wD;
            int w1 = max( 0, min( wBoth, iround( wBoth * fraction1 ) ) );
            int w2 = wBoth - w1;

            setNodeBounds( node1, new Rectangle( b.x,             b.y, w1, b.height ) );
            setNodeBounds( nodeD, new Rectangle( xAfter( node1 ), b.y, wD, b.height ) );
            setNodeBounds( node2, new Rectangle( xAfter( nodeD ), b.y, w2, b.height ) );
        }
        else
        {
            int hD = splitPane.getDividerSize( );
            int hBoth = b.height - hD;
            int h1 = max( 0, min( hBoth, iround( hBoth * fraction1 ) ) );
            int h2 = hBoth - h1;

            setNodeBounds( node1, new Rectangle( b.x, b.y,             b.width, h1 ) );
            setNodeBounds( nodeD, new Rectangle( b.x, yAfter( node1 ), b.width, hD ) );
            setNodeBounds( node2, new Rectangle( b.x, yAfter( nodeD ), b.width, h2 ) );
        }
    }

    protected String nextLeafId( )
    {
        int leafNumber = ( this.nextLeafNumber++ );
        return ( "Leaf_" + leafNumber );
    }

    protected void refreshMaximizeButtons( )
    {
        for ( TileKey tileKey : tileKeys.values( ) )
        {
            refreshMaximizeButton( tileKey );
        }
    }

    protected void refreshMaximizeButton( final TileKey tileKey )
    {
        final JButton button = tileMaximizeButtons.get( tileKey );

        for ( ActionListener l : button.getActionListeners( ) )
        {
            button.removeActionListener( l );
        }

        if ( maximizedTileKey == null )
        {
            button.setIcon( theme.maximizeIcon );
            button.addActionListener( new ActionListener( )
            {
                public void actionPerformed( ActionEvent ev )
                {
                    maximizeTile( tileKey );
                    button.setIcon( theme.restoreIcon );
                    button.getModel( ).setRollover( false );
                }
            } );
        }
        else
        {
            button.setIcon( theme.restoreIcon );
            button.addActionListener( new ActionListener( )
            {
                public void actionPerformed( ActionEvent ev )
                {
                    unmaximizeTile( );
                    button.setIcon( theme.maximizeIcon );
                    button.getModel( ).setRollover( false );
                }
            } );
        }
    }

    protected Tile newTile( final TileKey tileKey )
    {
        JButton maximizeButton = new JButton( theme.maximizeIcon );
        tileMaximizeButtons.put( tileKey, maximizeButton );
        refreshMaximizeButton( tileKey );

        return new Tile( theme, maximizeButton );
    }

    protected TileKey initTile( String leafId )
    {
        TileKey tileKey = new TileKey( leafId );

        Tile tile = newTile( tileKey );
        tile.addDockingMouseAdapter( new DockingMouseAdapter( tile ) );

        splitPane.add( tile, leafId );

        return tileKey;
    }

    protected TileKey chooseDefaultTile( ViewKey viewKey )
    {
        Leaf tileLeaf = firstTileLeaf( splitPane.getMultiSplitLayout( ).getModel( ) );
        if ( tileLeaf != null ) return new TileKey( tileLeaf.getName( ) );

        return initTile( setSolitaryLeaf( ) );
    }

    protected boolean tileExists( TileKey tileKey )
    {
        return ( tileKey != null && isTileLeaf( splitPane.getMultiSplitLayout( ).getNodeForName( tileKey.leafId ) ) );
    }

    protected Leaf firstTileLeaf( Node node )
    {
        if ( isTileLeaf( node ) )
        {
            return ( Leaf ) node;
        }
        else if ( node instanceof Split )
        {
            Split split = ( Split ) node;
            for ( Node child : split.getChildren( ) )
            {
                Leaf leaf = firstTileLeaf( child );
                if ( leaf != null ) return leaf;
            }
            return null;
        }
        else
        {
            return null;
        }
    }

    protected boolean isTileLeaf( Node node )
    {
        return ( node instanceof Leaf && splitPane.getMultiSplitLayout( ).getComponentForNode( node ) instanceof Tile );
    }

    protected Tile tile( TileKey tileKey )
    {
        MultiSplitLayout layout = splitPane.getMultiSplitLayout( );

        Node node = layout.getNodeForName( tileKey.leafId );
        if ( node == null ) throw new RuntimeException( "No layout node for this tile-key: leaf-id = " + tileKey.leafId );

        Component component = layout.getComponentForNode( node );
        if ( component == null ) throw new RuntimeException( "No component for this tile-key: leaf-id = " + tileKey.leafId );
        if ( !( component instanceof Tile ) ) throw new RuntimeException( "Component for this tile-key is not a Tile: leaf-id = " + tileKey.leafId );

        return ( Tile ) component;
    }

    protected void removeEmptyNodes( )
    {
        MultiSplitLayout layout = splitPane.getMultiSplitLayout( );

        Node model = layout.getModel( );
        if ( model instanceof Split )
        {
            Node newModel = pruneEmptyNodes( ( Split ) model );
            if ( newModel == null )
            {
                setSolitaryLeaf( );
            }
            else if ( newModel != model )
            {
                newModel.setParent( null );
                layout.setModel( newModel );
            }
        }
        else if ( model instanceof Leaf )
        {
            Leaf leaf = ( Leaf ) model;
            if ( !hasViews( leaf ) )
            {
                removeLeafComponent( ( Leaf ) model );
                setSolitaryLeaf( );
            }
        }
        else
        {
            setSolitaryLeaf( );
        }
    }

    protected Node pruneEmptyNodes( Split split )
    {
        List<Node> nonEmptyChildren = newArrayList( );

        Map<Node,Node> childReplacements = newHashMap( );
        for ( Node child : split.getChildren( ) )
        {
            if ( child instanceof Leaf )
            {
                if ( hasViews( ( Leaf ) child ) )
                {
                    nonEmptyChildren.add( child );
                }
                else
                {
                    childReplacements.put( child, null );
                }
            }
            else if ( child instanceof Split )
            {
                Node newChild = pruneEmptyNodes( ( Split ) child );
                if ( newChild == null )
                {
                    childReplacements.put( child, null );
                }
                else
                {
                    nonEmptyChildren.add( newChild );
                    if ( newChild != child )
                    {
                        childReplacements.put( child, newChild );
                    }
                }
            }
            else
            {
                // Doesn't contain views, but shouldn't be removed either
            }
        }

        for ( Entry<Node,Node> en : childReplacements.entrySet( ) )
        {
            Node oldChild = en.getKey( );
            if ( oldChild instanceof Leaf )
            {
                removeLeafComponent( ( Leaf  ) oldChild );
            }

            Node newChild = en.getValue( );
            if ( newChild == null )
            {
                List<Node> children = split.getChildren( );
                int iR = children.indexOf( oldChild );
                Rectangle bR = oldChild.getBounds( );
                int dividerSize = splitPane.getDividerSize( );

                Node n1 = ( iR >= 2 ? children.get( iR - 2 ) : null );
                Node d1 = ( iR >= 2 ? children.get( iR - 1 ) : null );
                Node d2 = ( iR < children.size( ) - 2 ? children.get( iR + 1 ) : null );
                Node n2 = ( iR < children.size( ) - 2 ? children.get( iR + 2 ) : null );

                if ( n1 != null && n2 != null )
                {
                    Rectangle b1 = n1.getBounds( );
                    Rectangle b2 = n2.getBounds( );
                    if ( split.isRowLayout( ) )
                    {
                        int wExtra = dividerSize + bR.width;
                        int wExtra1 = ( b1.width * wExtra ) / ( b1.width + b2.width );
                        int w1 = b1.width + wExtra1;
                        int w2 = ( wExtra - wExtra1 ) + b2.width;

                        // Only one of the dividers will survive the removal,
                        // so place them both immediately after n1.
                        //
                        setNodeBounds( n1, new Rectangle( b1.x,         b1.y, w1,          b1.height ) );
                        setNodeBounds( d1, new Rectangle( xAfter( n1 ), b1.y, dividerSize, b1.height ) );
                        setNodeBounds( d2, new Rectangle( xAfter( n1 ), b2.y, dividerSize, b2.height ) );
                        setNodeBounds( n2, new Rectangle( xAfter( d2 ), b2.y, w2,          b2.height ) );
                    }
                    else
                    {
                        int hExtra = dividerSize + bR.height;
                        int hExtra1 = ( b1.height * hExtra ) / ( b1.height + b2.height );
                        int h1 = b1.height + hExtra1;
                        int h2 = ( hExtra - hExtra1 ) + b2.height;

                        // Only one of the dividers will survive the removal,
                        // so place them both immediately after n1.
                        //
                        setNodeBounds( n1, new Rectangle( b1.x, b1.y,         b1.width, h1          ) );
                        setNodeBounds( d1, new Rectangle( b1.x, yAfter( n1 ), b1.width, dividerSize ) );
                        setNodeBounds( d2, new Rectangle( b2.x, yAfter( n1 ), b2.width, dividerSize ) );
                        setNodeBounds( n2, new Rectangle( b2.x, yAfter( d2 ), b2.width, h2          ) );
                    }
                }
                else if ( n1 != null )
                {
                    Rectangle b1 = n1.getBounds( );
                    if ( split.isRowLayout( ) )
                    {
                        int w1 = b1.width + dividerSize + bR.width;
                        setNodeBounds( n1, new Rectangle( b1.x, b1.y, w1, b1.height ) );
                    }
                    else
                    {
                        int h1 = b1.height + dividerSize + bR.height;
                        setNodeBounds( n1, new Rectangle( b1.x, b1.y, b1.width, h1 ) );
                    }
                }
                else if ( n2 != null )
                {
                    Rectangle b2 = n2.getBounds( );
                    if ( split.isRowLayout( ) )
                    {
                        int w2 = bR.width + dividerSize + b2.width;
                        setNodeBounds( n2, new Rectangle( bR.x, bR.y, w2, b2.height ) );
                    }
                    else
                    {
                        int h2 = bR.height + dividerSize + b2.height;
                        setNodeBounds( n2, new Rectangle( bR.x, bR.y, b2.width, h2 ) );
                    }
                }
                else
                {
                    // If oldChild had no siblings, split will get removed anyway, so its bounds don't matter
                }

                split.remove( oldChild );
            }
            else
            {
                split.replace( oldChild, newChild );

                // Since this is a depth-first prune, newChild will already have had its bounds
                // recalculated. And, since we only reach this point when newChild is the only
                // non-empty child of oldChild, newChild's bounds are already the same as oldChild's.
                //
                // This means we don't have to recalculate sizes for newChild or its descendants.
                //
            }
        }

        switch ( nonEmptyChildren.size( ) )
        {
            case 0: return null;
            case 1: return nonEmptyChildren.get( 0 );
            default: return split;
        }
    }

    protected void removeLeafComponent( Leaf leaf )
    {
        tileMaximizeButtons.remove( new TileKey( leaf.getName( ) ) );

        Component component = splitPane.getMultiSplitLayout( ).getComponentForNode( leaf );
        if ( component != null )
        {
            splitPane.getMultiSplitLayout( ).removeLayoutComponent( component );
            splitPane.remove( component );
        }
    }

    protected boolean hasViews( Leaf leaf )
    {
        Tile tile = ( Tile ) splitPane.getMultiSplitLayout( ).getComponentForNode( leaf );
        return ( tile != null && tile.numViews( ) > 0 );
    }

    protected String setSolitaryLeaf( )
    {
        Leaf tileLeaf = firstTileLeaf( splitPane.getMultiSplitLayout( ).getModel( ) );
        if ( tileLeaf != null ) throw new RuntimeException( "setSolitaryLeaf was called, but there are existing tiles" );

        String leafId = nextLeafId( );
        Leaf leaf = new Leaf( leafId );
        splitPane.getMultiSplitLayout( ).setModel( leaf );
        leaf.setBounds( getBounds( ) );
        return leafId;
    }

    protected void setNodeBounds( Node node, Rectangle newBounds )
    {
        if ( node instanceof Split )
        {
            Split split = ( Split ) node;

            int numDividers = 0;
            for ( Node child : split.getChildren( ) )
            {
                if ( child instanceof Divider )
                {
                    numDividers++;
                }
            }

            int dividerSize = splitPane.getDividerSize( );
            Rectangle oldBounds = split.getBounds( );

            if ( split.isRowLayout( ) )
            {
                int oldContentWidth = oldBounds.width - numDividers*dividerSize;
                int newContentWidth = newBounds.width - numDividers*dividerSize;

                int cx = 0;
                int numDividersSoFar = 0;
                for ( Node child : split.getChildren( ) )
                {
                    Rectangle b = child.getBounds( );
                    int x = newBounds.x + cx + numDividersSoFar*dividerSize;

                    if ( child instanceof Divider )
                    {
                        child.setBounds( new Rectangle( x, newBounds.y, dividerSize, newBounds.height ) );
                        numDividersSoFar++;
                    }
                    else
                    {
                        int cxNext = ( newContentWidth * ( ( b.x - oldBounds.x ) + b.width - numDividersSoFar*dividerSize ) ) / oldContentWidth;
                        setNodeBounds( child, new Rectangle( x, newBounds.y, cxNext-cx, newBounds.height ) );
                        cx = cxNext;
                    }
                }
            }
            else
            {
                int oldContentHeight = oldBounds.height - numDividers*dividerSize;
                int newContentHeight = newBounds.height - numDividers*dividerSize;

                int cy = 0;
                int numDividersSoFar = 0;
                for ( Node child : split.getChildren( ) )
                {
                    Rectangle b = child.getBounds( );
                    int y = newBounds.y + cy + numDividersSoFar*dividerSize;

                    if ( child instanceof Divider )
                    {
                        child.setBounds( new Rectangle( newBounds.x, y, newBounds.width, dividerSize ) );
                        numDividersSoFar++;
                    }
                    else
                    {
                        int cyNext = ( newContentHeight * ( ( b.y - oldBounds.y ) + b.height - numDividersSoFar*dividerSize ) ) / oldContentHeight;
                        setNodeBounds( child, new Rectangle( newBounds.x, y, newBounds.width, cyNext-cy ) );
                        cy = cyNext;
                    }
                }
            }
        }

        node.setBounds( newBounds );
    }






    protected class DockingMouseAdapter extends MouseAdapter
    {
        protected final Tile tile;

        protected boolean dragging = false;
        protected ViewKey draggedViewKey = null;


        public DockingMouseAdapter( Tile tile )
        {
            this.tile = tile;
            this.dragging = false;
            this.draggedViewKey = null;
        }

        @Override
        public void mousePressed( MouseEvent ev )
        {
            int buttonsDown = ( ev.getModifiersEx( ) & ( BUTTON1_DOWN_MASK | BUTTON2_DOWN_MASK | BUTTON3_DOWN_MASK ) );
            if ( buttonsDown == BUTTON1_DOWN_MASK )
            {
                Point p = pointRelativeToAncestor( ev, tile );
                int viewNum = tile.viewNumForTabAt( p.x, p.y );
                if ( 0 <= viewNum && viewNum < tile.numViews( ) )
                {
                    this.draggedViewKey = tile.view( viewNum ).viewKey;
                    this.dragging = false;
                }
            }
        }

        @Override
        public void mouseDragged( MouseEvent ev )
        {
            if ( draggedViewKey != null )
            {
                this.dragging = true;
                LandingRegion landingRegion = findLandingRegion( draggedViewKey, pointRelativeToAncestor( ev, splitPane ) );
                Rectangle indicator = ( landingRegion == null ? tile( tileKeys.get( draggedViewKey ) ).getBounds( ) : landingRegion.getIndicator( ) );
                indicatorOverlay.setDockingIndicatorRectangle( indicator );
            }
        }

        @Override
        public void mouseReleased( MouseEvent ev )
        {
            if ( ev.getButton( ) == BUTTON1 && dragging )
            {
                LandingRegion landingRegion = findLandingRegion( draggedViewKey, pointRelativeToAncestor( ev, splitPane ) );
                if ( landingRegion != null )
                {
                    // This will remove empty tiles before placing the view.
                    //
                    // This would cause problems if a view were placed relative to its
                    // own tile, and there were no other views in the tile. We get away
                    // with it, though, because we don't allow the dragged view to land
                    // beside its own tile, unless the tile contains other views.
                    //
                    View view = removeView( draggedViewKey );

                    landingRegion.placeView( view );
                }

                this.dragging = false;
                this.draggedViewKey = null;
                indicatorOverlay.setDockingIndicatorRectangle( null );
            }
        }

        protected LandingRegion findLandingRegion( ViewKey draggedViewKey, Point dragPoint )
        {

            TileKey fromTileKey = tileKeys.get( draggedViewKey );

            TileKey toTileKey = null;
            Tile toTile = null;
            for ( TileKey tileKey : tileKeys.values( ) )
            {
                Tile tile = tile( tileKey );

                // Find coords relative to tile
                int i = dragPoint.x - tile.getX( );
                int j = dragPoint.y - tile.getY( );
                int w = tile.getWidth( );
                int h = tile.getHeight( );
                if ( 0 <= i && i < w && 0 <= j && j < h )
                {
                    toTile = tile;
                    toTileKey = tileKey;
                }
            }


            // On own tile, which has no other views
            //
            if ( toTileKey != null && toTileKey.equals( fromTileKey ) && toTile.numViews( ) == 1 )
            {
                return null;
            }


            // On an existing tab
            //
            if ( toTileKey != null )
            {
                // Find coords relative to tile
                int i = dragPoint.x - toTile.getX( );
                int j = dragPoint.y - toTile.getY( );
                int viewNum = toTile.viewNumForTabAt( i, j );
                if ( 0 <= viewNum && viewNum < toTile.numViews( ) )
                {
                    return new InExistingTile( toTileKey, viewNum );
                }
            }


            // Near edge of docking-pane
            //
            {
                // Coords relative to splitPane
                int i = dragPoint.x;
                int j = dragPoint.y;
                int w = splitPane.getWidth( );
                int h = splitPane.getHeight( );
                if ( 0 <= i && i < w && 0 <= j && j < h )
                {
                    int dLeft = i;
                    int dRight = w - 1 - i;
                    int dTop = j;
                    int dBottom = h - 1 - j;

                    IntAndIndex closest = minValueAndIndex( dLeft, dRight, dTop, dBottom );
                    if ( closest.value < 16 )
                    {
                        switch ( closest.index )
                        {
                            case 0: return new EdgeOfDockingPane( Side.LEFT );
                            case 1: return new EdgeOfDockingPane( Side.RIGHT );
                            case 2: return new EdgeOfDockingPane( Side.TOP );
                            case 3: return new EdgeOfDockingPane( Side.BOTTOM );
                        }
                    }
                }
            }


            // Near the edge of an existing tile
            //
            if ( toTile != null )
            {
                // Find coords relative to tile
                int i = dragPoint.x - toTile.getX( );
                int j = dragPoint.y - toTile.getY( );
                int w = toTile.getWidth( );
                int h = toTile.getHeight( );

                int dLeft = i;
                int dRight = w - 1 - i;
                int dTop = j;
                int dBottom = h - 1 - j;
                IntAndIndex closest = minValueAndIndex( dLeft, dRight, dTop, dBottom );

                if ( closest.value < 64 )
                {
                    switch ( closest.index )
                    {
                        case 0: return new BesideExistingTile( toTileKey, Side.LEFT );
                        case 1: return new BesideExistingTile( toTileKey, Side.RIGHT );
                        case 2: return new BesideExistingTile( toTileKey, Side.TOP );
                        case 3: return new BesideExistingTile( toTileKey, Side.BOTTOM );
                    }
                }
            }


            // In an existing tile, but not the one we started from, and not near the edge
            //
            if ( toTileKey != null && !toTileKey.equals( fromTileKey ) )
            {
                return new LastInExistingTile( toTileKey );
            }


            // Nowhere else to land, except back where we started
            //
            return null;
        }


        protected abstract class LandingRegion
        {
            public abstract Rectangle getIndicator( );
            public abstract void placeView( View view );
        }

        protected class EdgeOfDockingPane extends LandingRegion
        {
            public final Side edgeOfPane;

            public EdgeOfDockingPane( Side edgeOfPane )
            {
                this.edgeOfPane = edgeOfPane;
            }

            @Override
            public Rectangle getIndicator( )
            {
                int w = splitPane.getWidth( );
                int h = splitPane.getHeight( );

                switch ( edgeOfPane )
                {
                    case LEFT:   return new Rectangle( 0,       0,       64,  h );
                    case RIGHT:  return new Rectangle( w-1-64,  0,       64,  h );
                    case TOP:    return new Rectangle( 0,       0,       w,  64 );
                    case BOTTOM: return new Rectangle( 0,       h-1-64,  w,  64 );

                    default: return null;
                }
            }

            @Override
            public void placeView( View view )
            {
                TileKey newTileKey = addNewTile( edgeOfPane );
                addView( view, newTileKey );
            }
        }

        protected class BesideExistingTile extends LandingRegion
        {
            public final TileKey neighborKey;
            public final Side sideOfNeighbor;

            public BesideExistingTile( TileKey neighborKey, Side sideOfNeighbor )
            {
                this.neighborKey = neighborKey;
                this.sideOfNeighbor = sideOfNeighbor;
            }

            @Override
            public Rectangle getIndicator( )
            {
                Tile neighbor = tile( neighborKey );
                int x = neighbor.getX( );
                int y = neighbor.getY( );
                int w = neighbor.getWidth( );
                int h = neighbor.getHeight( );

                switch ( sideOfNeighbor )
                {
                    case LEFT:   return new Rectangle( x,       y,       w/2, h );
                    case RIGHT:  return new Rectangle( x+w-w/2, y,       w/2, h );
                    case TOP:    return new Rectangle( x,       y,       w,   h/2 );
                    case BOTTOM: return new Rectangle( x,       y+h-h/2, w,   h/2 );

                    default: return null;
                }
            }

            @Override
            public void placeView( View view )
            {
                TileKey newTileKey = addNewTile( neighborKey, sideOfNeighbor );
                addView( view, newTileKey );
            }
        }

        protected class InExistingTile extends LandingRegion
        {
            public final TileKey tileKey;
            public final int viewNum;

            public InExistingTile( TileKey tileKey, int viewNum )
            {
                this.tileKey = tileKey;
                this.viewNum = viewNum;
            }

            @Override
            public Rectangle getIndicator( )
            {
                Tile tile = tile( tileKey );
                Rectangle tabBounds = tile.viewTabBounds( viewNum );
                if ( tabBounds == null )
                {
                    return tile.getBounds( );
                }
                else
                {
                    return new Rectangle( tile.getX( )+tabBounds.x, tile.getY( )+tabBounds.y, tabBounds.width, tabBounds.height );
                }
            }

            @Override
            public void placeView( View view )
            {
                // If you think about it, you'll wonder why we always insert at viewNum --
                // if we're moving a view a few tabs to the right, then the view's original
                // tab will get removed first, which will change the tab numbers!
                //
                // This approach places the tab to the right of the indicated tab instead of
                // its left, which is a little weird. However, the indicated tab has shifted
                // to the left -- so if we placed the tab to the left of the indicated tab,
                // it would not end up under the mouse, which feels pretty unsettling.
                //
                addView( view, tileKey, viewNum );
                tile( tileKey ).selectView( view );
            }
        }

        protected class LastInExistingTile extends LandingRegion
        {
            public final TileKey tileKey;

            public LastInExistingTile( TileKey tileKey )
            {
                this.tileKey = tileKey;
            }

            @Override
            public Rectangle getIndicator( )
            {
                return tile( tileKey ).getBounds( );
            }

            @Override
            public void placeView( View view )
            {
                addView( view, tileKey );
                tile( tileKey ).selectView( view );
            }
        }
    }






    public final Supplier<ArrangementNode> captureArrangement = new Supplier<ArrangementNode>( )
    {
        public ArrangementNode get( )
        {
            return captureArrangement( );
        }
    };

    public final Runnable1<ArrangementNode> restoreArrangement = new Runnable1<ArrangementNode>( )
    {
        public void run( ArrangementNode arrangement )
        {
            restoreArrangement( arrangement );
        }
    };

    public ArrangementNode captureArrangement( )
    {
        requireSwingThread( );

        return toArrangement( splitPane.getMultiSplitLayout( ).getModel( ) );
    }

    public void restoreArrangement( ArrangementNode arrangement )
    {
        requireSwingThread( );


        Map<ViewKey,View> views = newHashMap( viewsByKey );

        for ( ViewKey viewKey : views.keySet( ) ) removeView( viewKey );

        Map<String,ArrangementLeaf> leavesById = newHashMap( );
        splitPane.setModel( fromArrangement( arrangement, innerBounds( splitPane ), leavesById ) );

        for ( Entry<String,ArrangementLeaf> en : leavesById.entrySet( ) )
        {
            String leafId = en.getKey( );
            ArrangementLeaf arrLeaf = en.getValue( );

            TileKey tileKey = initTile( leafId );
            for ( String viewId : arrLeaf.viewIds )
            {
                View view = views.get( new ViewKey( viewId ) );
                if ( view != null ) addView( view, tileKey );
            }

            View selectedView = views.get( new ViewKey( arrLeaf.selectedViewId ) );
            if ( selectedView != null ) tile( tileKey ).selectView( selectedView );
        }


        // Old tile borders sometimes remain visible in the dividers
        repaint( );
    }

    protected ArrangementNode toArrangement( Node node )
    {
        if ( node instanceof Split )
        {
            Split split = ( Split ) node;
            ArrangementSplit arrSplit = new ArrangementSplit( );

            arrSplit.isRow = split.isRowLayout( );

            for ( Node child : split.getChildren( ) )
            {
                ArrangementNode arrChild = toArrangement( child );
                if ( arrChild != null )
                {
                    arrChild.extent = ( arrSplit.isRow ? child.getBounds( ).width : child.getBounds( ).height );
                    arrSplit.childNodes.add( arrChild );
                }
            }

            return arrSplit;
        }
        else if ( node instanceof Leaf )
        {
            Leaf leaf = ( Leaf ) node;
            ArrangementLeaf arrLeaf = new ArrangementLeaf( );

            Tile tile = tile( new TileKey( leaf.getName( ) ) );
            for ( int viewNum = 0; viewNum < tile.numViews( ); viewNum++ )
            {
                View view = tile.view( viewNum );
                arrLeaf.viewIds.add( view.viewKey.viewId );
            }

            View selectedView = tile.selectedView( );
            arrLeaf.selectedViewId = ( selectedView == null ? null : selectedView.viewKey.viewId );

            return arrLeaf;
        }
        else
        {
            return null;
        }
    }

    protected Node fromArrangement( ArrangementNode arrNode, Rectangle bounds, Map<String,ArrangementLeaf> leavesById_OUT )
    {
        if ( arrNode instanceof ArrangementSplit )
        {
            ArrangementSplit arrSplit = ( ArrangementSplit ) arrNode;

            int divPixels = splitPane.getDividerSize( );
            int totalDivs = max( 0, arrSplit.childNodes.size( ) - 1 );
            int totalContentPixels = ( arrSplit.isRow ? bounds.width : bounds.height ) - totalDivs*divPixels;

            double totalContent = 0;
            for ( ArrangementNode cChild : arrSplit.childNodes )
            {
                totalContent += cChild.extent;
            }

            int divsSoFar = 0;
            int startPixel = 0;
            double contentSoFar = 0;
            List<Node> children = newArrayList( );
            for ( ArrangementNode arrChild : arrSplit.childNodes )
            {
                if ( !children.isEmpty( ) )
                {
                    int xDiv = ( arrSplit.isRow ? startPixel    : 0            ) + bounds.x;
                    int yDiv = ( arrSplit.isRow ? 0             : startPixel   ) + bounds.y;
                    int wDiv = ( arrSplit.isRow ? divPixels     : bounds.width );
                    int hDiv = ( arrSplit.isRow ? bounds.height : divPixels    );

                    Divider divider = new Divider( );
                    divider.setBounds( new Rectangle( xDiv, yDiv, wDiv, hDiv ) );
                    children.add( divider );

                    startPixel += divPixels;
                    divsSoFar++;
                }

                int endPixel = iround( ( ( contentSoFar + arrChild.extent ) / totalContent ) * totalContentPixels ) + divsSoFar*divPixels;

                int xChild = ( arrSplit.isRow ? startPixel          : 0                   ) + bounds.x;
                int yChild = ( arrSplit.isRow ? 0                   : startPixel          ) + bounds.y;
                int wChild = ( arrSplit.isRow ? endPixel-startPixel : bounds.width        );
                int hChild = ( arrSplit.isRow ? bounds.height       : endPixel-startPixel );

                Node child = fromArrangement( arrChild, new Rectangle( xChild, yChild, wChild, hChild ), leavesById_OUT );
                children.add( child );

                startPixel = endPixel;
                contentSoFar += arrChild.extent;
            }

            Split split = ( arrSplit.isRow ? new RowSplit( ) : new ColSplit( ) );
            split.setChildren( children );
            split.setBounds( bounds );
            return split;
        }
        else if ( arrNode instanceof ArrangementLeaf )
        {
            ArrangementLeaf arrLeaf = ( ArrangementLeaf ) arrNode;
            String leafId = nextLeafId( );

            leavesById_OUT.put( leafId, arrLeaf );

            Leaf leaf = new Leaf( leafId );
            leaf.setBounds( bounds );
            return leaf;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of ArrangementNode: " + arrNode.getClass( ).getName( ) );
        }
    }

    public static class Arrangement
    {
        @XmlType( name="Node" )
        public static class ArrangementNode
        {
            public double extent = 1;
        }

        @XmlType( name="Split" )
        public static class ArrangementSplit extends ArrangementNode
        {
            public boolean isRow = false;

            @XmlElementWrapper( name="children" )
            @XmlElement( name="child" )
            public List<ArrangementNode> childNodes = newArrayList( );
        }

        @XmlType( name="Leaf" )
        public static class ArrangementLeaf extends ArrangementNode
        {
            @XmlElementWrapper( name="views" )
            @XmlElement( name="view" )
            public List<String> viewIds = newArrayList( );

            @XmlElement( name="selectedView" )
            public String selectedViewId = null;
        }

        public static Marshaller newJaxbMarshaller( ) throws IOException, JAXBException
        {
            Marshaller marshaller = JAXBContext.newInstance( ArrangementNode.class, ArrangementSplit.class, ArrangementLeaf.class ).createMarshaller( );
            marshaller.setProperty( JAXB_FORMATTED_OUTPUT, true );
            return marshaller;
        }

        public static JAXBElement<ArrangementNode> newJaxbRoot( ArrangementNode model )
        {
            return new JAXBElement<ArrangementNode>( new QName( "model" ), ArrangementNode.class, model );
        }

        public static void writeDockingArrangementXml( ArrangementNode model, File file ) throws JAXBException, IOException
        {
            newJaxbMarshaller( ).marshal( newJaxbRoot( model ), file );
        }

        public static void writeDockingArrangementXml( ArrangementNode model, Writer writer ) throws JAXBException, IOException
        {
            newJaxbMarshaller( ).marshal( newJaxbRoot( model ), writer );
        }

        public static void writeDockingArrangementXml( ArrangementNode model, OutputStream stream ) throws JAXBException, IOException
        {
            newJaxbMarshaller( ).marshal( newJaxbRoot( model ), stream );
        }

        public static Unmarshaller newJaxbUnmarshaller( ) throws JAXBException, IOException
        {
            Unmarshaller unmarshaller = JAXBContext.newInstance( ArrangementNode.class, ArrangementSplit.class, ArrangementLeaf.class ).createUnmarshaller( );
            return unmarshaller;
        }

        protected static ArrangementNode castToArrangementNode( Object object )
        {
            if ( object instanceof ArrangementNode )
            {
                return ( ArrangementNode ) object;
            }
            else if ( object instanceof JAXBElement )
            {
                return castToArrangementNode( ( ( JAXBElement<?> ) object ).getValue( ) );
            }
            else
            {
                throw new ClassCastException( "Object is neither an ArrangementNode nor a JAXBElement: classname = " + object.getClass( ).getName( ) );
            }
        }

        public static ArrangementNode readDockingArrangementXml( URL url ) throws JAXBException, IOException
        {
            return castToArrangementNode( newJaxbUnmarshaller( ).unmarshal( url ) );
        }

        public static ArrangementNode readDockingArrangementXml( File file ) throws JAXBException, IOException
        {
            return castToArrangementNode( newJaxbUnmarshaller( ).unmarshal( file ) );
        }

        public static ArrangementNode readDockingArrangementXml( Reader reader ) throws JAXBException, IOException
        {
            return castToArrangementNode( newJaxbUnmarshaller( ).unmarshal( reader ) );
        }

        public static ArrangementNode readDockingArrangementXml( InputStream stream ) throws JAXBException, IOException
        {
            return castToArrangementNode( newJaxbUnmarshaller( ).unmarshal( stream ) );
        }
    }

}

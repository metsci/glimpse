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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import com.metsci.glimpse.docking.DockingPane.Config.ConfigLeaf;
import com.metsci.glimpse.docking.DockingPane.Config.ConfigNode;
import com.metsci.glimpse.docking.DockingPane.Config.ConfigSplit;
import com.metsci.glimpse.docking.DockingUtils.IntAndIndex;
import com.metsci.glimpse.docking.DockingUtils.Runnable1;
import com.metsci.glimpse.docking.DockingUtils.Supplier;

import static com.metsci.glimpse.docking.DockingUtils.*;
import static java.awt.Color.*;
import static java.awt.event.InputEvent.*;
import static java.awt.event.MouseEvent.*;
import static java.lang.Math.*;
import static javax.xml.bind.Marshaller.*;

public abstract class DockingPane<T extends Component & Tile> extends JRootPane
{

    protected final Class<T> tileClass;

    protected final Map<ViewKey,View> viewsByKey;
    protected final Map<ViewKey,TileKey> tileKeys;

    protected final Map<ViewKey,TileKey> defaultTileKeys;

    protected final DockingIndicatorOverlay indicatorOverlay;
    protected final JXMultiSplitPane splitPane;

    protected int nextLeafNumber;


    public DockingPane( Class<T> tileClass )
    {
        this.tileClass = tileClass;

        this.viewsByKey = newHashMap( );
        this.tileKeys = newHashMap( );

        this.defaultTileKeys = newHashMap( );


        this.indicatorOverlay = new DockingIndicatorOverlay( black, 2, true );
        this.splitPane = new JXMultiSplitPane( );
        splitPane.getMultiSplitLayout( ).setRemoveDividers( true );
        splitPane.getMultiSplitLayout( ).setFloatingDividers( false );

        setContentPane( splitPane );
        setGlassPane( indicatorOverlay );
        indicatorOverlay.setVisible( true );


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

    protected abstract T newTile( );

    protected TileKey initTile( String leafId )
    {
        T tile = newTile( );
        tile.addDockingMouseAdapter( new DockingMouseAdapter( tile ) );

        splitPane.add( tile, leafId );

        return new TileKey( leafId );
    }

    protected TileKey chooseDefaultTile( ViewKey viewKey )
    {
        TileKey defaultTileKey = defaultTileKeys.get( viewKey );
        if ( tileExists( defaultTileKey ) ) return defaultTileKey;

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

    protected T tile( TileKey tileKey )
    {
        MultiSplitLayout layout = splitPane.getMultiSplitLayout( );

        Node node = layout.getNodeForName( tileKey.leafId );
        if ( node == null ) throw new RuntimeException( "No layout node for this tile-key: leaf-id = " + tileKey.leafId );

        Component component = layout.getComponentForNode( node );
        if ( component == null ) throw new RuntimeException( "No component for this tile-key: leaf-id = " + tileKey.leafId );
        if ( !tileClass.isInstance( component ) ) throw new RuntimeException( "Component for this tile-key is not a Tile: leaf-id = " + tileKey.leafId );

        return tileClass.cast( component );
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
        protected final T tile;

        protected boolean dragging = false;
        protected ViewKey draggedViewKey = null;


        public DockingMouseAdapter( T tile )
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
            T toTile = null;
            for ( TileKey tileKey : tileKeys.values( ) )
            {
                T tile = tile( tileKey );

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
                T neighbor = tile( neighborKey );
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
                T tile = tile( tileKey );
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






    public final Supplier<ConfigNode> captureConfig = new Supplier<ConfigNode>( )
    {
        public ConfigNode get( )
        {
            return captureConfig( );
        }
    };

    public final Runnable1<ConfigNode> restoreConfig = new Runnable1<ConfigNode>( )
    {
        public void run( ConfigNode config )
        {
            restoreConfig( config );
        }
    };

    public ConfigNode captureConfig( )
    {
        requireSwingThread( );

        return toConfigNode( splitPane.getMultiSplitLayout( ).getModel( ) );
    }

    public void restoreConfig( ConfigNode config )
    {
        requireSwingThread( );

        Collection<View> views = newArrayList( viewsByKey.values( ) );
        for ( View view : views ) removeView( view.viewKey );

        Map<ViewKey,String> leafIds = newHashMap( );
        splitPane.setModel( fromConfigNode( config, new Rectangle( 0, 0, splitPane.getWidth( ), splitPane.getHeight( ) ), leafIds ) );

        defaultTileKeys.clear( );
        for ( Entry<ViewKey,String> en : leafIds.entrySet( ) )
        {
            ViewKey viewKey = en.getKey( );
            String leafId = en.getValue( );
            defaultTileKeys.put( viewKey, initTile( leafId ) );
        }

        for ( View view : views ) addView( view );

        // Some old tabbed-pane borders remain visible in the dividers
        repaint( );
    }

    protected ConfigNode toConfigNode( Node node )
    {
        if ( node instanceof Split )
        {
            Split split = ( Split ) node;
            ConfigSplit cSplit = new ConfigSplit( );

            cSplit.isRow = split.isRowLayout( );

            for ( Node child : split.getChildren( ) )
            {
                ConfigNode cChild = toConfigNode( child );
                if ( cChild != null )
                {
                    cChild.extent = ( cSplit.isRow ? child.getBounds( ).width : child.getBounds( ).height );
                    cSplit.childNodes.add( cChild );
                }
            }

            return cSplit;
        }
        else if ( node instanceof Leaf )
        {
            Leaf leaf = ( Leaf ) node;
            ConfigLeaf cLeaf = new ConfigLeaf( );

            Tile tile = tile( new TileKey( leaf.getName( ) ) );
            for ( int viewNum = 0; viewNum < tile.numViews( ); viewNum++ )
            {
                View view = tile.view( viewNum );
                cLeaf.viewIds.add( view.viewKey.viewId );
            }

            View selectedView = tile.selectedView( );
            cLeaf.selectedViewId = ( selectedView == null ? null : selectedView.viewKey.viewId );

            return cLeaf;
        }
        else
        {
            return null;
        }
    }

    protected Node fromConfigNode( ConfigNode cNode, Rectangle bounds, Map<ViewKey,String> leafIdsOut )
    {
        if ( cNode instanceof ConfigSplit )
        {
            ConfigSplit cSplit = ( ConfigSplit ) cNode;

            int divPixels = splitPane.getDividerSize( );
            int totalDivs = max( 0, cSplit.childNodes.size( ) - 1 );
            int totalContentPixels = ( cSplit.isRow ? bounds.width : bounds.height ) - totalDivs*divPixels;

            double totalContent = 0;
            for ( ConfigNode cChild : cSplit.childNodes )
            {
                totalContent += cChild.extent;
            }

            int divsSoFar = 0;
            int startPixel = 0;
            double contentSoFar = 0;
            List<Node> children = newArrayList( );
            for ( ConfigNode cChild : cSplit.childNodes )
            {
                if ( !children.isEmpty( ) )
                {
                    int xDiv = ( cSplit.isRow ? startPixel    : 0            );
                    int yDiv = ( cSplit.isRow ? 0             : startPixel   );
                    int wDiv = ( cSplit.isRow ? divPixels     : bounds.width );
                    int hDiv = ( cSplit.isRow ? bounds.height : divPixels    );

                    Divider divider = new Divider( );
                    divider.setBounds( new Rectangle( xDiv, yDiv, wDiv, hDiv ) );
                    children.add( divider );

                    startPixel += divPixels;
                    divsSoFar++;
                }

                int endPixel = iround( ( ( contentSoFar + cChild.extent ) / totalContent ) * totalContentPixels ) + divsSoFar*divPixels;

                int xChild = ( cSplit.isRow ? startPixel          : 0 );
                int yChild = ( cSplit.isRow ? 0                   : startPixel );
                int wChild = ( cSplit.isRow ? endPixel-startPixel : bounds.width );
                int hChild = ( cSplit.isRow ? bounds.height       : endPixel-startPixel );

                Node child = fromConfigNode( cChild, new Rectangle( xChild, yChild, wChild, hChild ), leafIdsOut );
                children.add( child );

                startPixel = endPixel;
                contentSoFar += cChild.extent;
            }

            Split split = ( cSplit.isRow ? new RowSplit( ) : new ColSplit( ) );
            split.setChildren( children );
            split.setBounds( bounds );
            return split;
        }
        else if ( cNode instanceof ConfigLeaf )
        {
            ConfigLeaf cLeaf = ( ConfigLeaf ) cNode;
            String leafId = nextLeafId( );

            // XXX: Honor tab order
            for ( String viewId : cLeaf.viewIds )
            {
                ViewKey viewKey = new ViewKey( viewId );
                leafIdsOut.put( viewKey, leafId );
            }

            // XXX: Honor selectedViewKey

            Leaf leaf = new Leaf( leafId );
            leaf.setBounds( bounds );
            return leaf;
        }
        else
        {
            throw new RuntimeException( "Unrecognized subclass of ConfigNode: " + cNode.getClass( ).getName( ) );
        }
    }

    public static class Config
    {
        @XmlType( name="Node" )
        public static class ConfigNode
        {
            public double extent = 1;
        }

        @XmlType( name="Split" )
        public static class ConfigSplit extends ConfigNode
        {
            public boolean isRow = false;

            @XmlElementWrapper( name="children" )
            @XmlElement( name="child" )
            public List<ConfigNode> childNodes = newArrayList( );
        }

        @XmlType( name="Leaf" )
        public static class ConfigLeaf extends ConfigNode
        {
            @XmlElementWrapper( name="views" )
            @XmlElement( name="view" )
            public List<String> viewIds = newArrayList( );

            @XmlElement( name="selectedView" )
            public String selectedViewId = null;
        }

        public static Marshaller newJaxbMarshaller( ) throws IOException, JAXBException
        {
            Marshaller marshaller = JAXBContext.newInstance( ConfigNode.class, ConfigSplit.class, ConfigLeaf.class ).createMarshaller( );
            marshaller.setProperty( JAXB_FORMATTED_OUTPUT, true );
            return marshaller;
        }

        public static JAXBElement<ConfigNode> newJaxbRoot( ConfigNode model )
        {
            return new JAXBElement<ConfigNode>( new QName( "model" ), ConfigNode.class, model );
        }

        public static void writeDockingConfigXml( ConfigNode model, File file ) throws JAXBException, IOException
        {
            newJaxbMarshaller( ).marshal( newJaxbRoot( model ), file );
        }

        public static void writeDockingConfigXml( ConfigNode model, Writer writer ) throws JAXBException, IOException
        {
            newJaxbMarshaller( ).marshal( newJaxbRoot( model ), writer );
        }

        public static void writeDockingConfigXml( ConfigNode model, OutputStream stream ) throws JAXBException, IOException
        {
            newJaxbMarshaller( ).marshal( newJaxbRoot( model ), stream );
        }

        public static Unmarshaller newJaxbUnmarshaller( ) throws JAXBException, IOException
        {
            Unmarshaller unmarshaller = JAXBContext.newInstance( ConfigNode.class, ConfigSplit.class, ConfigLeaf.class ).createUnmarshaller( );
            return unmarshaller;
        }

        protected static ConfigNode castToConfigNode( Object object )
        {
            if ( object instanceof ConfigNode )
            {
                return ( ConfigNode ) object;
            }
            else if ( object instanceof JAXBElement )
            {
                return castToConfigNode( ( ( JAXBElement<?> ) object ).getValue( ) );
            }
            else
            {
                throw new ClassCastException( "Object is neither a ConfigNode nor a JAXBElement: classname = " + object.getClass( ).getName( ) );
            }
        }

        public static ConfigNode readDockingConfigXml( URL url ) throws JAXBException, IOException
        {
            return castToConfigNode( newJaxbUnmarshaller( ).unmarshal( url ) );
        }

        public static ConfigNode readDockingConfigXml( File file ) throws JAXBException, IOException
        {
            return castToConfigNode( newJaxbUnmarshaller( ).unmarshal( file ) );
        }

        public static ConfigNode readDockingConfigXml( Reader reader ) throws JAXBException, IOException
        {
            return castToConfigNode( newJaxbUnmarshaller( ).unmarshal( reader ) );
        }

        public static ConfigNode readDockingConfigXml( InputStream stream ) throws JAXBException, IOException
        {
            return castToConfigNode( newJaxbUnmarshaller( ).unmarshal( stream ) );
        }
    }

}

package com.metsci.glimpse.docking2;

import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.TOP;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

import com.metsci.glimpse.docking.Side;

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
        this.tiles = new HashSet<Component>( );
    }

    public void addInitialTile( Component c )
    {
        if ( root != null ) throw new RuntimeException( "At least one tile already exists" );

        add( c );

        this.root = c;
        tiles.add( c );
    }

    public void addEdgeTile( Component c, Side edgeOfPane )
    {
        addNeighborTile( c, root, edgeOfPane, 0.25 );
    }

    public void addEdgeTile( Component c, Side edgeOfPane, double extentFrac )
    {
        addNeighborTile( c, root, edgeOfPane, extentFrac );
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
            parent.remove( neighbor );
            parent.add( newSplitPane );
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

}

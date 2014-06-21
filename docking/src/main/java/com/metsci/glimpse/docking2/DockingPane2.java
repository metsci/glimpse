package com.metsci.glimpse.docking2;

import static com.metsci.glimpse.docking.Side.BOTTOM;
import static com.metsci.glimpse.docking.Side.LEFT;
import static com.metsci.glimpse.docking.Side.TOP;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.Side;
import com.metsci.glimpse.docking.TileKey;

public class DockingPane2 extends JPanel
{

    protected final DockingTheme theme;
    protected final TileKeyGenerator tileKeyGen;
    protected final Map<TileKey,Component> tileComponents;
    protected final Map<Component,TileKey> tileKeys;
    protected Component root;


    public DockingPane2( DockingTheme theme, TileKeyGenerator tileKeyGen )
    {
        super( new BorderLayout( ) );

        this.theme = theme;
        this.tileKeyGen = tileKeyGen;
        this.tileComponents = new HashMap<TileKey,Component>( );
        this.tileKeys = new HashMap<Component,TileKey>( );

        this.root = null;
    }

    public TileKey addInitialTile( Component c )
    {
        if ( root != null ) throw new RuntimeException( "addInitialTile was called, but some tiles already exist" );

        add( c );
        this.root = c;

        TileKey newTileKey = tileKeyGen.newTileKey( );
        tileComponents.put( newTileKey, c );
        tileKeys.put( c, newTileKey );
        return newTileKey;
    }

    public TileKey addEdgeTile( Component c, Side edgeOfPane )
    {
        return addEdgeTile( c, edgeOfPane, 0.25 );
    }

    public TileKey addEdgeTile( Component c, Side edgeOfPane, double extentFrac )
    {
        return addAdjacentTile( c, root, edgeOfPane, extentFrac );
    }

    public TileKey addNeighborTile( Component c, TileKey neighborKey, Side sideOfNeighbor )
    {
        return addNeighborTile( c, neighborKey, sideOfNeighbor, 0.5 );
    }

    public TileKey addNeighborTile( Component c, TileKey neighborKey, Side sideOfNeighbor, double extentFrac )
    {
        Component neighbor = tileComponents.get( neighborKey );
        return addAdjacentTile( c, neighbor, sideOfNeighbor, extentFrac );
    }

    protected TileKey addAdjacentTile( Component c, Component neighbor, Side sideOfNeighbor, double extentFrac )
    {
        if ( extentFrac < 0 || extentFrac > 1 ) throw new IllegalArgumentException( "Value for extentFrac is outside [0,1]: " + extentFrac );

        boolean arrangeVertically = ( sideOfNeighbor == TOP || sideOfNeighbor == BOTTOM );
        boolean newIsChildA = ( sideOfNeighbor == LEFT || sideOfNeighbor == TOP );
        double splitFrac = ( newIsChildA ? extentFrac : 1 - extentFrac );
        SplitPane newSplitPane = new SplitPane( arrangeVertically, splitFrac, theme.dividerSize );

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

        TileKey newTileKey = tileKeyGen.newTileKey( );
        tileComponents.put( newTileKey, c );
        tileKeys.put( c, newTileKey );
        return newTileKey;
    }

    public TileKey findTileAt( int x, int y )
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

    protected TileKey _findTileAt( Container parent, int xInParent, int yInParent )
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
                    TileKey tileKey = tileKeys.get( child );
                    if ( tileKey != null )
                    {
                        return tileKey;
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

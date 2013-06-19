package com.metsci.glimpse.docking;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.util.Map;

import javax.swing.JTabbedPane;

import static com.metsci.glimpse.docking.DockingUtils.*;

public class BasicTile extends JTabbedPane implements Tile
{

    protected final Map<Component,View> viewsByComponent;


    public BasicTile( )
    {
        this.viewsByComponent = newHashMap( );
    }

    @Override
    public int numViews( )
    {
        return getTabCount( );
    }

    @Override
    public void addView( View view, int viewNum )
    {
        if ( viewsByComponent.containsKey( view.component ) ) throw new RuntimeException( "View already exists in this tile: view-id = " + view.viewKey.viewId );

        viewsByComponent.put( view.component, view );
        insertTab( view.title, view.icon, view.component, view.tooltip, viewNum );
    }

    @Override
    public void removeView( View view )
    {
        if ( !viewsByComponent.containsKey( view.component ) ) throw new RuntimeException( "View does not exist in this tile: view-id = " + view.viewKey.viewId );

        viewsByComponent.remove( view.component );
        remove( view.component );
    }

    @Override
    public View view( int viewNum )
    {
        return viewsByComponent.get( getComponentAt( viewNum ) );
    }

    @Override
    public void selectView( View view )
    {
        setSelectedComponent( view.component );
    }

    @Override
    public View selectedView( )
    {
        return viewsByComponent.get( getSelectedComponent( ) );
    }

    @Override
    public int viewNumForTabAt( int x, int y )
    {
        return getUI( ).tabForCoordinate( this, x, y );
    }

    @Override
    public Rectangle viewTabBounds( int viewNum )
    {
        return getBoundsAt( viewNum );
    }

    @Override
    public void addDockingMouseAdapter( MouseAdapter mouseAdapter )
    {
        addMouseListener( mouseAdapter );
        addMouseMotionListener( mouseAdapter );
    }

}

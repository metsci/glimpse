package com.metsci.glimpse.docking;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;

public interface Tile
{

    int numViews( );
    View view( int viewNum );
    View selectedView( );

    void addView( View view, int viewNum );
    void removeView( View view );
    void selectView( View view );

    /**
     * Coords are interpreted as being relative to this tile
     */
    int viewNumForTabAt( int x, int y );

    /**
     * Coords are given relative to this tile
     */
    Rectangle viewTabBounds( int viewNum );

    void addDockingMouseAdapter( MouseAdapter mouseAdapter );

}

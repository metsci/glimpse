package com.metsci.glimpse.docking2;

import static com.metsci.glimpse.docking.MiscUtils.pointRelativeToAncestor;
import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
import static java.awt.event.InputEvent.BUTTON3_DOWN_MASK;
import static java.awt.event.MouseEvent.BUTTON1;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.metsci.glimpse.docking.Tile;
import com.metsci.glimpse.docking.ViewKey;
import com.metsci.glimpse.docking2.LandingRegions.LandingRegion;

public class DockingMouseAdapter2 extends MouseAdapter
{

    protected final Tile tile;

    protected boolean dragging = false;
    protected ViewKey draggedViewKey = null;


    public DockingMouseAdapter2( Tile tile )
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
            LandingRegion landingRegion = findLandingRegion( draggedViewKey, ev );
            Rectangle indicator = ( landingRegion == null ? tile.getBounds( ) : landingRegion.getIndicator( ) );
            // XXX
            //indicatorOverlay.setDockingIndicatorRectangle( indicator );
        }
    }

    @Override
    public void mouseReleased( MouseEvent ev )
    {
        if ( ev.getButton( ) == BUTTON1 && dragging )
        {
            LandingRegion landingRegion = findLandingRegion( draggedViewKey, ev );
            // XXX
//            if ( landingRegion != null )
//            {
//                // This will remove empty tiles before placing the view.
//                //
//                // This would cause problems if a view were placed relative to its
//                // own tile, and there were no other views in the tile. We get away
//                // with it, though, because we don't allow the dragged view to land
//                // beside its own tile, unless the tile contains other views.
//                //
//                View view = removeView( draggedViewKey );
//
//                landingRegion.placeView( view );
//            }

            this.dragging = false;
            this.draggedViewKey = null;
            // XXX
//            indicatorOverlay.setDockingIndicatorRectangle( null );
        }
    }

    protected LandingRegion findLandingRegion( ViewKey draggedViewKey, MouseEvent ev )
    {
        // XXX
        return null;
    }

}

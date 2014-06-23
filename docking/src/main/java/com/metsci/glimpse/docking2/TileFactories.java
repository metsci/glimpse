package com.metsci.glimpse.docking2;

import com.metsci.glimpse.docking.Tile;

public class TileFactories
{


    public static interface TileFactory
    {
        Tile newTile( );
    }


    public static class TileFactoryStandard implements TileFactory
    {
        public final DockingPaneGroup dockerGroup;

        public TileFactoryStandard( DockingPaneGroup dockerGroup )
        {
            this.dockerGroup = dockerGroup;
        }

        @Override
        public Tile newTile( )
        {
            Tile tile = new Tile( dockerGroup.theme );
            tile.addDockingMouseAdapter( new DockingMouseAdapter( tile, dockerGroup, this ) );
            return tile;
        }
    }


}

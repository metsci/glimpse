package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingUtils.*;

public class TileKey
{

    public final String leafId;


    public TileKey( String leafId )
    {
        this.leafId = leafId;
    }

    @Override
    public int hashCode( )
    {
        int prime = 3331;
        int result = 1;
        result = prime * result + ( leafId == null ? 0 : leafId.hashCode( ) );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != getClass( ) ) return false;

        TileKey other = ( TileKey ) o;
        return areEqual( other.leafId, leafId );
    }

}

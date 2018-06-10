package com.metsci.glimpse.charts.bathy;

public class TileKey
{
    public final double minLat;
    public final double maxLat;
    public final double minLon;
    public final double maxLon;

    public TileKey( String id, double minLat, double maxLat, double minLon, double maxLon )
    {
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
    }

    @Override
    public int hashCode( )
    {
        int hash = 0;
        hash += 31 * Double.hashCode( minLat );
        hash += 31 * Double.hashCode( maxLat );
        hash += 31 * Double.hashCode( minLon );
        hash += 31 * Double.hashCode( maxLon );
        return hash;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof TileKey )
        {
            TileKey other = ( TileKey ) obj;
            return minLat == other.minLat &&
                    maxLat == other.maxLat &&
                    minLon == other.minLon &&
                    maxLon == other.maxLon;
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString( )
    {
        return String.format( "TileKey[%f,%f to %f,%f]", minLat, minLon, maxLat, maxLon );
    }
}
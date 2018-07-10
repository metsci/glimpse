package com.metsci.glimpse.charts.bathy;

import static com.metsci.glimpse.util.units.Length.toKilometers;

/**
 * A bounding box for a tile. Tiles are expected to be in lat/lon space and must not cross the antimeridian.
 */
public class TileKey
{
    public final double lengthScale;
    public final double minLat;
    public final double maxLat;
    public final double minLon;
    public final double maxLon;

    public TileKey( double lengthScale, double minLat, double maxLat, double minLon, double maxLon )
    {
        this.lengthScale = lengthScale;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
    }

    @Override
    public int hashCode( )
    {
        int hash = 0;
        hash += 31 * Double.hashCode( lengthScale );
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
            return lengthScale == other.lengthScale &&
                    minLat == other.minLat &&
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
        return String.format( "TileKey[scale=%f; lat=%f,%f; lon=%f,%f]", toKilometers( lengthScale ), minLat, maxLat, minLon, maxLon );
    }
}
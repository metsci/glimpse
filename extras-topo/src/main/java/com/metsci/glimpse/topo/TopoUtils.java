package com.metsci.glimpse.topo;

import static java.lang.Math.max;
import static java.lang.Math.min;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.topo.io.TopoDataType;
import com.metsci.glimpse.topo.proj.NormalCylindricalProjection;

public class TopoUtils
{

    public static LatLonBox axisBounds( Axis2D axis, NormalCylindricalProjection proj )
    {
        double lonA_DEG = proj.xToLon_DEG( axis.getMinX( ) );
        double lonB_DEG = proj.xToLon_DEG( axis.getMaxX( ) );
        double latA_DEG = proj.yToLat_DEG( axis.getMinY( ) );
        double latB_DEG = proj.yToLat_DEG( axis.getMaxY( ) );

        double northLat_DEG = max( latA_DEG, latB_DEG );
        double southLat_DEG = min( latA_DEG, latB_DEG );
        double eastLon_DEG = max( lonA_DEG, lonB_DEG );
        double westLon_DEG = min( lonA_DEG, lonB_DEG );

        return new LatLonBox( northLat_DEG, southLat_DEG, eastLon_DEG, westLon_DEG );
    }

    public static LatLonBox intersect( LatLonBox box, TopoTileBounds tile )
    {
        double northLat_DEG = max( box.northLat_DEG, tile.northLat_DEG );
        double southLat_DEG = min( box.southLat_DEG, tile.southLat_DEG );
        double eastLon_DEG = max( box.eastLon_DEG, tile.eastLon_DEG );
        double westLon_DEG = min( box.westLon_DEG, tile.westLon_DEG );

        return new LatLonBox( northLat_DEG, southLat_DEG, eastLon_DEG, westLon_DEG );
    }

    public static float dataDenormFactor( TopoDataType dataType )
    {
        switch ( dataType )
        {
            case TOPO_I2: return 32767f;
            case TOPO_F4: return 1f;
            default: throw new RuntimeException( "Unrecognized data type: " + dataType );
        }
    }

}

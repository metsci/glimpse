package com.metsci.glimpse.support.projection;

import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

public class GeoReprojection implements Projection
{
    protected Projection flat;
    protected GeoProjection from;
    protected GeoProjection to;

    protected double sizeDownsample;

    /**
     * <p>Builds a projection which maps a texture which was rendered in one GeoProjection space
     * into another GeoProjection space.</p>
     * 
     * <p>A common usage. This would take a texture which had been
     * {@code
     * GeoProjection from = new TangentPlane( LatLonGeo.fromDegrees( 0, 0 ) );
     * Axis2D axis = new Axis2D( );
     * Projection flat = new FlatProjection( ... );
     * GeoProjection to = new MercatorProjection( );
     * new GeoReprojection( flat, from, to );
     * }
     * 
     * @param flat mapping from texture space to flat projection space
     * @param from current mapping from flat projection space to lat/lon space
     * @param to desired mapping from flat projection space to lat/lon space
     * @param sizeX the discretization of the reprojection in the x direction
     * @param sizeY the discretization of the reprojection in the y direction
     */
    public GeoReprojection( Projection flat, GeoProjection from, GeoProjection to, double sizeDownsample )
    {
        this.from = from;
        this.to = to;
        
        this.flat = flat;

        this.sizeDownsample = sizeDownsample;
    }

    @Override
    public void getVertexXY( double dataFractionX, double dataFractionY, float[] resultXY )
    {
        // use the flat projection to convert from texture space to flat projection space
        // (of the 'from' projection)
        flat.getVertexXY( dataFractionX, dataFractionY, resultXY );

        // convert from the flat projection space of the 'from' projection to flat
        // projection space of the 'to' projection
        Vector2d v = to.reprojectFrom( resultXY[0], resultXY[1], from );

        // return the results
        resultXY[0] = ( float ) v.getX( );
        resultXY[1] = ( float ) v.getY( );
    }

    @Override
    public void getVertexXYZ( double dataFractionX, double dataFractionY, float[] resultXYZ )
    {
        getVertexXYZ( dataFractionX, dataFractionY, resultXYZ );
        resultXYZ[2] = 0;
    }

    @Override
    public int getSizeX( int textureSizeX )
    {
        return (int) Math.max( 1, textureSizeX * sizeDownsample );
    }

    @Override
    public int getSizeY( int textureSizeY )
    {
        return (int) Math.max( 1, textureSizeY * sizeDownsample );
    }

}

package com.metsci.glimpse.charts.slippy;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * Locks the Axis2D so that zoom events always go up/down a single zoom level.
 * This is more important for the map tiles than for the imagery.
 * 
 * @author oren
 */
public class SlippyAxisMouseListener2D extends AxisMouseListener2D
{
    private static final double LOG2 = Math.log( 2 );
    private static final int MAX_ZOOM = 19;

    private final GeoProjection geoProj;
    private double xSpan;
    private double ySpan;

    public SlippyAxisMouseListener2D( GeoProjection geoProj )
    {
        if ( geoProj == null )
        {
            throw new IllegalArgumentException( "geo projection cannot be null" );
        }

        this.geoProj = geoProj;
    }

    @Override
    public void mouseWheelMoved( GlimpseMouseEvent event )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( event );
        if ( layout == null ) return;

        Axis2D axis2D = layout.getAxis( event.getTargetStack( ) );

        this.update( axis2D, event.getWheelIncrement( ), false );

        this.mouseWheelMoved( event, axis2D.getAxisX( ), true );
        this.mouseWheelMoved( event, axis2D.getAxisY( ), false );
        this.applyAndUpdate( axis2D.getAxisX( ), axis2D.getAxisY( ) );
    }

    protected void update( Axis2D axis2D, double zoomIncrement, boolean force )
    {
        double xTileDim = axis2D.getAxisX( ).getSizePixels( ) / 256.;
        double yTileDim = axis2D.getAxisY( ).getSizePixels( ) / 256.;

        double minX = axis2D.getAxisX( ).getMin( );
        double maxX = axis2D.getAxisX( ).getMax( );
        double minY = axis2D.getAxisY( ).getMin( );
        double maxY = axis2D.getAxisY( ).getMax( );

        LatLonGeo center = geoProj.unproject( ( minX + maxX ) / 2., ( minY + maxY ) / 2. );
        LatLonGeo ne = geoProj.unproject( maxX, maxY );
        LatLonGeo sw = geoProj.unproject( minX, minY );

        double east = ne.getLonDeg( );
        double west = sw.getLonDeg( );
        double lonSizeDeg = ( east - west ) / xTileDim;

        double zoomApprox = Math.log( 360 / lonSizeDeg ) / LOG2;

        int currentZoom = ( int ) Math.round( zoomApprox );

        int zoom = currentZoom;
        if ( zoomIncrement == 0 )
        {
            //nothing
        }
        else if ( zoomIncrement > 0 )
        {
            zoom--;
        }
        else
        {
            zoom++;

        }
        zoom = Math.max( 0, Math.min( zoom, MAX_ZOOM ) );

        if ( !force && zoom == currentZoom )
        {
            return;
        }
        double zoomFactor = 1 << zoom;

        double lonTileSizeDeg = 360 / zoomFactor;
        double latTileSizeDeg = 170.1022 / zoomFactor;
        
        double dLat = ( yTileDim / 2. ) * latTileSizeDeg;
        double dLon = ( xTileDim / 2. ) * lonTileSizeDeg;
        Vector2d maxVec = geoProj.project( LatLonGeo.fromDeg( center.getLatDeg( ) + dLat, center.getLonDeg( ) + dLon ) );
        Vector2d minVec = geoProj.project( LatLonGeo.fromDeg( center.getLatDeg( ) - dLat, center.getLonDeg( ) - dLon ) );

        //The data should *probably* be aspect ratio locked anyway, so... these are the same?
        this.xSpan = maxVec.getX( ) - minVec.getX( );
        this.ySpan = maxVec.getY( ) - minVec.getY( );
    }

    @Override
    public void zoom( Axis1D axis, boolean horizontal, double zoomIncrements, int posX, int posY )
    {
        if ( !allowZoom ) return;

        int mousePosPixels = getDim( horizontal, posX, axis.getSizePixels( ) - posY );
        double span = ( horizontal ? xSpan : ySpan );
        double mousePct = mousePosPixels / ( double ) axis.getSizePixels( );
        double mousePosValue = axis.screenPixelToValue( mousePosPixels );

        double newMinValue = mousePosValue - ( mousePct * span );
        double newMaxValue = mousePosValue + ( ( 1 - mousePct ) * span );

        axis.setMin( newMinValue );
        axis.setMax( newMaxValue );
    }
}

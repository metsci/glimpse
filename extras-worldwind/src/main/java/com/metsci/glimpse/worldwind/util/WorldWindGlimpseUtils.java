package com.metsci.glimpse.worldwind.util;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;
import com.metsci.glimpse.worldwind.event.MouseWrapperWorldwind;
import com.metsci.glimpse.worldwind.tile.GlimpseSurfaceTile;

public class WorldWindGlimpseUtils
{
    /**
     * Establishes a bridge which passes Swing MouseEvents which occur in the provided WorldWindow
     * to any GlimpseMouseListeners attached to the provided GlimpseSurfaceTile.
     * 
     * @param wwd
     * @param projection
     * @param tile
     */
    public static void linkMouseEvents( WorldWindow wwd, GeoProjection projection, GlimpseSurfaceTile tile )
    {
        MouseWrapperWorldwind.linkMouseEvents( wwd, projection, tile );
    }
    
    /**
     * Links two WorldWindows so that the eye position of the master window is copied to the
     * eye position of the slave. Does not set up the reverse link. However, the caller may call
     * again with the arguments reversed to have both windows reflect each others position.
     * 
     * @param master
     * @param slave
     */
    public static void linkWorldWindToWorldWind( final WorldWindow master, final WorldWindow slave )
    {
        master.addPositionListener( new PositionListener( )
        {
            @Override
            public void moved( PositionEvent event )
            {
                Position pos = master.getView( ).getCurrentEyePosition( );
                slave.getView( ).setEyePosition( pos );
            }
        } );
    }

    /**
     * Sets up a listener to update the center of the provided axes to reflect the current eye
     * position of the WorldWindow. The zoom level of the axes is never changed.
     * 
     * @param wwd
     * @param projection
     * @param axis
     */
    public static void linkAxisToWorldWind( final WorldWindow wwd, final GeoProjection projection, final Axis2D axis )
    {
        wwd.addPositionListener( new PositionListener( )
        {
            @Override
            public void moved( PositionEvent event )
            {
                Position pos = wwd.getView( ).getCurrentEyePosition( );
                LatLonGeo latlon = fromPosition( pos );
                Vector2d center = projection.project( latlon );

                double diffX = axis.getMaxX( ) - axis.getMinX( );
                double diffY = axis.getMaxY( ) - axis.getMinY( );

                double newMinX = center.getX( ) - diffX / 2.0;
                double newMaxX = center.getX( ) + diffX / 2.0;

                double newMinY = center.getY( ) - diffY / 2.0;
                double newMaxY = center.getY( ) + diffY / 2.0;

                axis.set( newMinX, newMaxX, newMinY, newMaxY );
                axis.validate( );
            }
        } );
    }

    /**
     * Sets up a listener to update the eye position of the WorldWindow to match the
     * center of the provided Glimpse Axis2D (for the given GeoProjection). The elevation
     * of the WorldWindow is never adjusted.
     * 
     * @param wwd
     * @param projection
     * @param axis
     */
    public static void linkWorldWindToAxis( final WorldWindow wwd, final GeoProjection projection, final Axis2D axis )
    {
        axis.addAxisListener( new AxisListener2D( )
        {
            @Override
            public void axisUpdated( Axis2D axis )
            {
                double diffX = axis.getMaxX( ) - axis.getMinX( );
                double diffY = axis.getMaxY( ) - axis.getMinY( );

                double centerX = axis.getMinX( ) + diffX / 2.0;
                double centerY = axis.getMinY( ) + diffY / 2.0;

                LatLonGeo center = projection.unproject( centerX, centerY );
                double elevation = wwd.getView( ).getEyePosition( ).getElevation( );
                Position pos = toPosition( center, elevation );

                wwd.getView( ).setEyePosition( pos );
            }
        } );
    }

    public static LatLonGeo fromPosition( Position position )
    {
        return LatLonGeo.fromDeg( position.getLatitude( ).getDegrees( ), position.getLongitude( ).getDegrees( ) );
    }

    public static Position toPosition( LatLonGeo latlongeo, double elevation )
    {
        return Position.fromDegrees( latlongeo.getLatDeg( ), latlongeo.getLonDeg( ), elevation );
    }

    public static Position toPosition( LatLonGeo latlongeo )
    {
        return Position.fromDegrees( latlongeo.getLatDeg( ), latlongeo.getLonDeg( ) );
    }

    public static LatLonGeo fromLatLon( LatLon latlon )
    {
        return LatLonGeo.fromDeg( latlon.latitude.getDegrees( ), latlon.longitude.getDegrees( ) );
    }

    public static LatLon toLatLon( LatLonGeo latlongeo )
    {
        return LatLon.fromDegrees( latlongeo.getLatDeg( ), latlongeo.getLonDeg( ) );
    }
}

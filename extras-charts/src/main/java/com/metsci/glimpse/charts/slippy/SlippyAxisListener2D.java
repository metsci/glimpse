package com.metsci.glimpse.charts.slippy;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * Initializes the Axis2D so that zoom events always go up/down a single zoom level.
 * This is more important for the map tiles than for the imagery.
 * 
 * @author ulman
 */
public class SlippyAxisListener2D extends SlippyAxisMouseListener2D implements AxisListener2D
{
    public SlippyAxisListener2D( GeoProjection geoProj )
    {
        super( geoProj );
    }

    @Override
    public void axisUpdated( Axis2D axis )
    {
        //TODO It's possible that these children could come from different
        //     parent Axis2D if the SlippyMapPainter is being used in multiple
        //     GlimpseLayouts. I'm not sure whether this would be a problem
        //     in practice, but it's not really an issue since SlippyMapPainter
        //     isn't intended to work with painter re-targeting anyway.
        Axis1D childX = getInitializedChild( axis.getAxisX( ) );
        Axis1D childY = getInitializedChild( axis.getAxisY( ) );

        if ( childX != null && childY != null )
        {
            // only fire once to setup initial axis zoom level, then remove ourself as a listener
            axis.removeAxisListener( this );
            
            this.update( new Axis2D( childX, childY ), 0, true );
            
            int centerX = childX.getSizePixels( ) / 2;
            int centerY = childY.getSizePixels( ) / 2;
            
            this.zoom( childX, true, 0, centerX, centerY );
            this.zoom( childY, false, 0, centerX, centerY );
            this.applyAndUpdate( childX, childY );
        }
    }
    
    protected Axis1D getInitializedChild( Axis1D parent )
    {
        for ( Axis1D child : parent.getChildren( ) )
        {
            if ( child.isInitialized( ) )
            {
                return child;
            }
        }
        
        return null;
    }
}

/*
 * Copyright (c) 2016 Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

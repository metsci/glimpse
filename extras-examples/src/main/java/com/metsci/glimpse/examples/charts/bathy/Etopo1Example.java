/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.examples.charts.bathy;

import static com.metsci.glimpse.examples.Example.showWithSwing;
import static com.metsci.glimpse.util.logging.LoggerUtils.setTerseConsoleLogger;

import java.util.logging.Level;

import com.metsci.glimpse.charts.bathy.Etopo1Painter;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.plot.MapPlot2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.units.Length;

/**
 * @author borkholder
 */
public class Etopo1Example implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        setTerseConsoleLogger( Level.FINE );
        showWithSwing( new Etopo1Example( ) );
    }

    @Override
    public MapPlot2D getLayout( )
    {
        return getLayout( new TangentPlane( LatLonGeo.fromDeg( 20.14, -79.23 ) ) );
    }

    public MapPlot2D getLayout( GeoProjection projection )
    {
        MapPlot2D plot = new MapPlot2D( projection );
        plot.getLayoutCenter( ).addPainter( new Etopo1Painter( projection ) );
        plot.getAxis( ).set( 0, Length.fromNauticalMiles( 300 ), 0, Length.fromNauticalMiles( 300 ) );
        plot.getAxis( ).validate( );
        return plot;
    }
}

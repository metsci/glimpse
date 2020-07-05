/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.util.ImmutableCollectionUtils.mapMinus;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.mapWith;
import static com.metsci.glimpse.util.PredicateUtils.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.layers.Facet;
import com.metsci.glimpse.layers.View;
import com.metsci.glimpse.layers.geo.GeoView;
import com.metsci.glimpse.layers.time.TimelineView;
import com.metsci.glimpse.util.GeneralUtils;
import com.metsci.glimpse.util.var.ReadableVar;
import com.metsci.glimpse.util.var.Var;

public class ExampleLayer extends Layer
{

    protected ExampleStyle style;

    protected final List<ExamplePoint> points;

    protected final Var<ImmutableMap<View,ExampleFacet>> facets;


    public ExampleLayer( String title, float[] rgba )
    {
        this.title.set( title );

        this.style = new ExampleStyle( );
        this.style.rgbaInsideTWindow = Arrays.copyOf( rgba, 4 );
        this.style.rgbaOutsideTWindow = GeneralUtils.floats( 0.4f + 0.6f*rgba[0], 0.4f + 0.6f*rgba[1], 0.4f + 0.6f*rgba[2], 0.4f*rgba[3] );

        this.points = new ArrayList<>( );

        this.facets = new Var<>( ImmutableMap.of( ), notNull );
    }

    @Override
    public ReadableVar<? extends Map<? extends View,? extends Facet>> facets( )
    {
        return this.facets;
    }

    @Override
    public void installTo( View view )
    {
        if ( !this.facets.v( ).containsKey( view ) )
        {
            if ( view instanceof GeoView )
            {
                GeoView geo = ( GeoView ) view;
                ExampleFacet facet = new ExampleGeoFacet( this, geo, this.style );
                this.facets.update( ( v ) -> mapWith( v, view, facet ) );
                for ( ExamplePoint point : this.points )
                {
                    facet.addPoint( point );
                }
            }

            if ( view instanceof TimelineView )
            {
                TimelineView timeline = ( TimelineView ) view;
                ExampleFacet facet = new ExampleTimelineFacet( this, timeline, this.style );
                this.facets.update( ( v ) -> mapWith( v, view, facet ) );
                for ( ExamplePoint point : this.points )
                {
                    facet.addPoint( point );
                }
            }
        }
    }

    @Override
    public void uninstallFrom( View view, boolean isReinstall )
    {
        if ( this.facets.v( ).containsKey( view ) )
        {
            Facet facet = this.facets.v( ).get( view );
            facet.dispose( isReinstall );

            this.facets.update( ( v ) -> mapMinus( v, view ) );
        }
    }

    public void addPoint( ExamplePoint point )
    {
        this.points.add( point );

        for ( ExampleFacet facet : this.facets.v( ).values( ) )
        {
            facet.addPoint( point );
        }
    }

}

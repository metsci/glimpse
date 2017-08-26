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
package com.metsci.glimpse.layers.geo;

import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.layers.geo.GeoTrait.requireGeoTrait;
import static com.metsci.glimpse.support.DisposableUtils.addGlimpsePainter;
import static com.jogamp.opengl.GLProfile.GL3;

import java.util.Collection;

import com.jogamp.opengl.GLProfile;
import javax.swing.Icon;

import com.google.common.collect.ImmutableSet;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layers.GlimpseCanvasView;
import com.metsci.glimpse.layers.ViewOption;
import com.metsci.glimpse.layers.misc.CompositeCursorLabelPainter;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.util.var.Disposable;

public class GeoView extends GlimpseCanvasView
{

    public MultiAxisPlot2D plot;
    public GridPainter gridPainter;
    public DelegatePainter dataPainter;
    public CrosshairPainter crosshairPainter;
    public CompositeCursorLabelPainter cursorTextPainter;
    public BorderPainter borderPainter;

    public AxisInfo xAxisInfo;
    public AxisInfo yAxisInfo;
    
    
    public GeoView( ViewOption... viewOptions )
    {
        this( ImmutableSet.copyOf( viewOptions ) );
    }

    public GeoView( Collection<? extends ViewOption> viewOptions )
    {
        super( GLProfile.get( GL3 ), viewOptions );

        this.title.set( "Geo" );

        this.plot = null;
        this.gridPainter = null;
        this.dataPainter = null;
        this.crosshairPainter = null;
        this.cursorTextPainter = null;
        this.borderPainter = null;
    }

    @Override
    public Icon getIcon( )
    {
        return requireIcon( "fugue-icons/map.png" );
    }

    @Override
    protected void doContextReady( GlimpseContext context )
    {
        this.plot = new MultiAxisPlot2D( );
        this.plot.getCenterAxis( ).lockAspectRatioXY( 1.0 );
        Axis1D xAxis = this.plot.getCenterAxisX( );
        Axis1D yAxis = this.plot.getCenterAxisY( );
        this.xAxisInfo = this.plot.createAxisBottom( "xBottom", xAxis, new AxisMouseListener1D( ) );
        this.yAxisInfo = this.plot.createAxisLeft( "yLeft", yAxis, new AxisMouseListener1D( ) );

        //TODO Adding zOrder arguments to the painters fixes issue where
        //     cursor labels were appearing behind painters in dataPainter
        //     DelegatePainter. However, I'm unsure why the zOrder arguments
        //     are necessary as GlimpseLayouts default to the order painters
        //     were added. --Geoff
        this.gridPainter = new GridPainter( xAxisInfo.getTickHandler( ), yAxisInfo.getTickHandler( ) );
        this.plot.addPainter( this.gridPainter, -10 );

        this.dataPainter = new DelegatePainter( );
        this.plot.addPainter( this.dataPainter, 0 );

        this.crosshairPainter = new CrosshairPainter( );
        this.plot.addPainter( this.crosshairPainter, 10 );

        this.cursorTextPainter = new CompositeCursorLabelPainter( );
        this.cursorTextPainter.setXYLabels( xAxisInfo, yAxisInfo );
        this.plot.addPainter( this.cursorTextPainter, 10 );

        this.borderPainter = new BorderPainter( );
        this.plot.addPainter( this.borderPainter, 20 );

        this.canvas.addLayout( this.plot );
    }

    @Override
    public void doInit( )
    {
        GeoTrait geoTrait = requireGeoTrait( this );
        this.plot.getCenterAxis( ).setParent( geoTrait.axis );
    }

    @Override
    protected void doContextDying( GlimpseContext context )
    {
        this.canvas.removeLayout( this.plot );

        this.plot.dispose( context );

        this.plot = null;
        this.gridPainter = null;
        this.dataPainter = null;
        this.crosshairPainter = null;
        this.cursorTextPainter = null;
        this.borderPainter = null;
    }

    @Override
    public GeoView copy( )
    {
        return new GeoView( this.viewOptions );
    }

    public Disposable addDataPainter( GlimpsePainter painter )
    {
        return addGlimpsePainter( this.dataPainter, painter );
    }

}

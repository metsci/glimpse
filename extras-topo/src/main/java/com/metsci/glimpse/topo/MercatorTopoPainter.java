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
package com.metsci.glimpse.topo;

import static com.metsci.glimpse.core.support.wrapped.WrappedGlimpseContext.isFirstWrappedTile;
import static com.metsci.glimpse.topo.TopoColorUtils.bathyColorTable;
import static com.metsci.glimpse.topo.TopoColorUtils.bathyColormapMinValue;
import static com.metsci.glimpse.topo.TopoColorUtils.topoColorTable;
import static com.metsci.glimpse.topo.TopoColorUtils.topoColormapMaxValue;
import static com.metsci.glimpse.topo.TopoPainterConfig.topoPainterConfig_DEFAULT;
import static com.metsci.glimpse.topo.TopoUtils.axisBounds;
import static com.metsci.glimpse.util.units.Angle.radiansToDegrees;
import static java.lang.Math.min;

import java.util.List;

import com.jogamp.opengl.GL3;
import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.topo.io.TopoDataset;
import com.metsci.glimpse.topo.proj.MercatorNormalCylindricalProjection;
import com.metsci.glimpse.util.primitives.sorted.SortedDoubles;

public class MercatorTopoPainter extends GlimpsePainterBase
{

    protected final MercatorNormalCylindricalProjection proj;
    protected final TopoTileCache cache;
    protected final MercatorTopoProgram prog;

    protected long frameNum;


    public MercatorTopoPainter( TopoDataset dataset, MercatorNormalCylindricalProjection proj )
    {
        this( dataset, proj, topoPainterConfig_DEFAULT );
    }

    public MercatorTopoPainter( TopoDataset dataset, MercatorNormalCylindricalProjection proj, TopoPainterConfig config )
    {
        this.proj = proj;
        this.cache = new TopoTileCache( dataset, this.proj, config );
        this.prog = new MercatorTopoProgram( 2, 3, 4, bathyColorTable( ), topoColorTable( ), bathyColormapMinValue, topoColormapMaxValue );

        this.frameNum = 0;
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        if ( isFirstWrappedTile( context ) )
        {
            this.frameNum++;
        }

        GL3 gl = context.getGL( ).getGL3( );
        Axis2D axis = requireAxis2D( context );

        LatLonBox viewBounds = axisBounds( axis, this.proj );

        // TODO: Is there a better way to choose levelNum than maxPixelSize_DEG?
        Axis1D yAxis = axis.getAxisY( );
        double dyPerPixel = 1.0 / yAxis.getPixelsPerValue( );
        double maxPixelSize_DEG = dyPerPixel * this.cache.proj.maxDlatDy_RAD( yAxis.getMin( ), yAxis.getMax( ) ) * radiansToDegrees;

        SortedDoubles levelCellSizes_DEG = this.cache.levels.cellSizes_DEG;
        int levelNum = min( levelCellSizes_DEG.n( ) - 1, levelCellSizes_DEG.indexAtOrAfter( maxPixelSize_DEG ) );

        List<TopoDeviceTile> dTilesToDraw = this.cache.update( gl, this.frameNum, viewBounds, levelNum );

        this.prog.begin( context, this.proj );
        try
        {
            for ( TopoDeviceTile dTile : dTilesToDraw )
            {
                this.prog.draw( context, dTile );
            }
        }
        finally
        {
            this.prog.end( context );
        }
    }

    @Override
    protected void doDispose( GlimpseContext context )
    {
        this.cache.dispose( context );
        this.prog.dispose( context );
    }

}

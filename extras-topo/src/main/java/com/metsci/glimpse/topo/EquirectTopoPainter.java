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
package com.metsci.glimpse.topo;

import static com.metsci.glimpse.support.wrapped.WrappedGlimpseContext.isFirstWrappedTile;
import static com.metsci.glimpse.topo.TopoColorUtils.bathyColorTable;
import static com.metsci.glimpse.topo.TopoColorUtils.bathyColormapMinValue;
import static com.metsci.glimpse.topo.TopoColorUtils.topoColorTable;
import static com.metsci.glimpse.topo.TopoColorUtils.topoColormapMaxValue;
import static com.metsci.glimpse.topo.TopoPainterConfig.topoPainterConfig_DEFAULT;
import static com.metsci.glimpse.topo.TopoUtils.axisBounds;
import static java.lang.Math.min;

import java.util.List;

import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.topo.io.TopoDataset;
import com.metsci.glimpse.topo.proj.EquirectProjection;
import com.metsci.glimpse.util.primitives.sorted.SortedDoubles;

public class EquirectTopoPainter extends GlimpsePainterBase
{

    protected final TopoTileCache cache;
    protected final EquirectTopoProgram prog;

    protected long frameNum;


    public EquirectTopoPainter( TopoDataset dataset, EquirectProjection proj )
    {
        this( dataset, proj, topoPainterConfig_DEFAULT );
    }

    public EquirectTopoPainter( TopoDataset dataset, EquirectProjection proj, TopoPainterConfig config )
    {
        this.cache = new TopoTileCache( dataset, proj, config );
        this.prog = new EquirectTopoProgram( 2, 3, 4, bathyColorTable( ), topoColorTable( ), bathyColormapMinValue, topoColormapMaxValue );

        this.frameNum = 0;
    }

    @Override
    protected void doPaintTo( GlimpseContext context )
    {
        // TODO: Support cleaner per-frame calls, somehow
        if ( isFirstWrappedTile( context ) )
        {
            this.frameNum++;
        }

        GL3 gl = context.getGL( ).getGL3( );
        Axis2D axis = requireAxis2D( context );

        LatLonBox viewBounds = axisBounds( axis, this.cache.proj );

        double pixelSize_DEG = 1.0 / axis.getAxisX( ).getPixelsPerValue( );
        SortedDoubles levelCellSizes_DEG = this.cache.levels.cellSizes_DEG;
        int levelNum = min( levelCellSizes_DEG.n( ) - 1, levelCellSizes_DEG.indexAtOrAfter( pixelSize_DEG ) );

        List<TopoDeviceTile> dTilesToDraw = this.cache.update( gl, this.frameNum, viewBounds, levelNum );

        this.prog.begin( context );
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

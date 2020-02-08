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

import static com.metsci.glimpse.core.axis.UpdateMode.CenterScale;
import static com.metsci.glimpse.util.GeneralUtils.floats;
import static com.metsci.glimpse.util.math.MathConstants.HALF_PI;
import static java.lang.Math.PI;

import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.axis.WrappedAxis1D;
import com.metsci.glimpse.core.painter.base.GlimpsePainter;
import com.metsci.glimpse.core.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.core.painter.decoration.BorderPainter;
import com.metsci.glimpse.core.painter.group.WrappedPainter;
import com.metsci.glimpse.core.plot.MultiAxisPlot2D;
import com.metsci.glimpse.topo.io.TopoDataset;
import com.metsci.glimpse.topo.proj.EquirectNormalCylindricalProjection;
import com.metsci.glimpse.topo.proj.MercatorNormalCylindricalProjection;
import com.metsci.glimpse.topo.proj.NormalCylindricalProjection;

public class TopoQuickUtils
{

    public static MultiAxisPlot2D quickTopoPlot( TopoDataset topoDataset, NormalCylindricalProjection proj, GlimpsePainter... painters )
    {
        MultiAxisPlot2D plot = new MultiAxisPlot2D( )
        {
            @Override
            protected void initializeCenterAxis( )
            {
                this.centerAxisX = new WrappedAxis1D( proj.lonToX( -PI ), proj.lonToX( +PI ) );
                this.centerAxisY = new Axis1D( );
            }
        };

        plot.setShowTitle( false );
        plot.setBorderSize( 5 );

        Axis2D axis = plot.getCenterAxis( );
        axis.lockAspectRatioXY( 1.0 );
        axis.getAxisX( ).setUpdateMode( CenterScale );
        axis.getAxisY( ).setUpdateMode( CenterScale );
        axis.set( proj.lonToX( -PI ), proj.lonToX( +PI ), proj.latToY( -HALF_PI ), proj.latToY( +HALF_PI ) );
        axis.validate( );

        BackgroundPainter backgroundPainter = new BackgroundPainter( );
        backgroundPainter.setColor( floats( 0.7f, 0.7f, 0.7f, 1 ) );

        GlimpsePainter topoPainter = createTopoPainter( topoDataset, proj );

        WrappedPainter wrappedPainter = new WrappedPainter( true );
        wrappedPainter.addPainter( backgroundPainter );
        wrappedPainter.addPainter( topoPainter );
        for ( GlimpsePainter painter : painters )
        {
            wrappedPainter.addPainter( painter );
        }

        plot.getLayoutCenter( ).addPainter( wrappedPainter );
        plot.getLayoutCenter( ).addPainter( new BorderPainter( ) );

        return plot;
    }

    public static GlimpsePainter createTopoPainter( TopoDataset topoDataset, NormalCylindricalProjection proj )
    {
        // TODO: Make this less awkward
        if ( proj instanceof EquirectNormalCylindricalProjection )
        {
            return new EquirectTopoPainter( topoDataset, ( EquirectNormalCylindricalProjection ) proj );
        }
        else if ( proj instanceof MercatorNormalCylindricalProjection )
        {
            return new MercatorTopoPainter( topoDataset, ( MercatorNormalCylindricalProjection ) proj );
        }
        else
        {
            throw new RuntimeException( "Unrecognized projection subclass: " + proj.getClass( ).getName( ) );
        }
    }

}

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
package com.metsci.glimpse.plot.stacked;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.support.settings.LookAndFeel;

public class PlotInfoWrapper implements PlotInfo
{
    public PlotInfo info;

    public PlotInfoWrapper( PlotInfo info )
    {
        this.info = info;
    }

    @Override
    public String getLayoutData( )
    {
        return info.getLayoutData( );
    }

    @Override
    public void setLayoutData( String layoutData )
    {
        info.setLayoutData( layoutData );
    }

    @Override
    public StackedPlot2D getStackedPlot( )
    {
        return info.getStackedPlot( );
    }

    @Override
    public Object getId( )
    {
        return info.getId( );
    }

    @Override
    public int getOrder( )
    {
        return info.getOrder( );
    }

    @Override
    public int getSize( )
    {
        return info.getSize( );
    }

    @Override
    public void setOrder( int order )
    {
        info.setOrder( order );
    }

    @Override
    public void setSize( int size )
    {
        info.setSize( size );
    }

    @Override
    public boolean isGrow( )
    {
        return info.isGrow( );
    }

    @Override
    public void setGrow( boolean grow )
    {
        info.setGrow( grow );
    }

    @Override
    public void setPlotSpacing( int spacing )
    {
        info.setPlotSpacing( spacing );
    }

    @Override
    public int getPlotSpacing( )
    {
        return info.getPlotSpacing( );
    }

    @Override
    public GlimpseAxisLayout2D getLayout( )
    {
        return info.getLayout( );
    }

    @Override
    public GlimpseAxisLayout2D getBaseLayout( )
    {
        return info.getBaseLayout( );
    }

    @Override
    public Axis1D getCommonAxis( GlimpseTargetStack stack )
    {
        return info.getCommonAxis( stack );
    }

    @Override
    public Axis1D getOrthogonalAxis( GlimpseTargetStack stack )
    {
        return info.getOrthogonalAxis( stack );
    }

    @Override
    public Axis1D getCommonAxis( )
    {
        return info.getCommonAxis( );
    }

    @Override
    public Axis1D getOrthogonalAxis( )
    {
        return info.getOrthogonalAxis( );
    }

    @Override
    public void addLayout( GlimpseAxisLayout2D childLayout )
    {
        info.addLayout( childLayout );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        info.setLookAndFeel( laf );
    }

    @Override
    public void updateLayout( int index )
    {
        info.updateLayout( index );
    }

    @Override
    public void setVisible( boolean visible )
    {
        info.setVisible( visible );
    }

    @Override
    public boolean isVisible( )
    {
        return info.isVisible( );
    }

    @Override
    public boolean isExpanded( )
    {
        return info.isExpanded( );
    }

    @Override
    public void setParent( PlotInfo parent )
    {
        info.setParent( parent );
    }

    @Override
    public PlotInfo getParent( )
    {
        return info.getParent( );
    }

    /**
     * @deprecated {@link #removePlot()}
     */
    @Override
    public void deletePlot( )
    {
        removePlot( );
    }

    @Override
    public void removePlot( )
    {
        info.removePlot( );
    }

    @Override
    public void setIndentLevel( int level )
    {
        info.setIndentLevel( level );
    }

    @Override
    public int getIndentLevel( )
    {
        return info.getIndentLevel( );
    }
}